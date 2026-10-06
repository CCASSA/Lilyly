package com.lilyly.app

import java.io.ByteArrayInputStream
import java.net.URI
import java.util.zip.ZipFile
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element

/** Reads only packaged resources; never extracts archive paths or fetches network content. */
class EpubPackage(private val zip: ZipFile) {
    data class Chapter(val title: String, val html: String)
    data class Contents(val title: String, val author: String, val chapters: List<Chapter>, val cover: ByteArray?)
    private var budget = 30 * 1024 * 1024
    private fun read(path: String, limit: Int = 2 * 1024 * 1024): ByteArray {
        require(!path.startsWith("/") && path.split('/').none { it == ".." }) { "Invalid book resource path" }
        val entry = zip.getEntry(path) ?: error("A book resource is missing: $path")
        require(entry.size <= limit) { "This book contains an unusually large section" }
        val data = zip.getInputStream(entry).use { it.readBounded(limit + 1) }
        require(data.size <= limit && data.size <= budget) { "This book is too large to import safely" }
        budget -= data.size
        return data
    }
    private fun xml(bytes: ByteArray): org.w3c.dom.Document {
        require(!bytes.filter { it != 0.toByte() }.toByteArray().toString(Charsets.ISO_8859_1).contains("<!DOCTYPE", ignoreCase = true)) { "External XML declarations are not supported" }
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true
        factory.isExpandEntityReferences = false
        // Android's bundled parser does not implement all desktop JAXP feature flags.
        // DOCTYPE is rejected above; the resolver also denies every external entity.
        runCatching { factory.setFeature("http://xml.org/sax/features/external-general-entities", false) }
        runCatching { factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false) }
        val builder = factory.newDocumentBuilder()
        builder.setEntityResolver { _, _ -> throw org.xml.sax.SAXException("External XML entities are not supported") }
        return builder.parse(ByteArrayInputStream(bytes))
    }
    private fun org.w3c.dom.Document.elements(name: String): List<Element> {
        val nodes = getElementsByTagNameNS("*", name)
        return (0 until nodes.length).map { nodes.item(it) as Element }
    }
    private fun resolve(base: String, href: String): String {
        val uri = URI(base).resolve(href).normalize()
        require(uri.scheme == null && uri.authority == null && uri.path != null) { "Remote book resources are not imported" }
        return uri.path.also { require(!it.startsWith("/") && it.split('/').none { p -> p == ".." }) { "Invalid book path" } }
    }
    fun parse(): Contents {
        require(zip.size() <= 5000) { "This EPUB has too many resources" }
        require(zip.getEntry("META-INF/encryption.xml") == null) { "This EPUB declares encrypted resources. Import a DRM-free, unencrypted edition." }
        val container = xml(read("META-INF/container.xml"))
        val root = container.elements("rootfile").firstOrNull()?.getAttribute("full-path") ?: error("The EPUB package could not be found")
        val pkg = xml(read(root))
        val manifest = pkg.elements("item").associateBy { it.getAttribute("id") }
        val spine = pkg.elements("itemref").filter { it.getAttribute("linear") != "no" }
        require(spine.isNotEmpty() && spine.size <= 1500) { "The EPUB has no readable spine, or is too large" }
        val chapters = spine.mapIndexedNotNull { index, ref ->
            val item = manifest[ref.getAttribute("idref")] ?: error("Incomplete EPUB reading order")
            if(item.getAttribute("media-type") !in listOf("application/xhtml+xml","text/html")) return@mapIndexedNotNull null
            val html = read(resolve(root,item.getAttribute("href"))).toString(Charsets.UTF_8)
            val title = Regex("<title[^>]*>(.*?)</title>", setOf(RegexOption.IGNORE_CASE,RegexOption.DOT_MATCHES_ALL)).find(html)?.groupValues?.get(1)?.replace(Regex("<[^>]+>"),"")?.take(200) ?: "Section ${index+1}"
            Chapter(title, html)
        }
        require(chapters.isNotEmpty()) { "This EPUB does not contain supported text chapters" }
        val coverId = pkg.elements("meta").firstOrNull { it.getAttribute("name") == "cover" }?.getAttribute("content")
        val coverItem = manifest.values.firstOrNull { "cover-image" in it.getAttribute("properties").split(' ') } ?: manifest[coverId]
        val cover = coverItem?.takeIf { it.getAttribute("media-type") in listOf("image/jpeg","image/png","image/webp") }?.let { runCatching { read(resolve(root,it.getAttribute("href")),8*1024*1024) }.getOrNull() }
        return Contents(pkg.elements("title").firstOrNull()?.textContent?.trim().orEmpty(),pkg.elements("creator").firstOrNull()?.textContent?.trim().orEmpty(),chapters,cover)
    }
}

internal fun java.io.InputStream.readBounded(limit: Int): ByteArray {
    val out = java.io.ByteArrayOutputStream()
    val buffer = ByteArray(8192)
    while(out.size() < limit) {
        val count = read(buffer, 0, minOf(buffer.size, limit - out.size()))
        if(count < 0) break
        out.write(buffer, 0, count)
    }
    return out.toByteArray()
}
