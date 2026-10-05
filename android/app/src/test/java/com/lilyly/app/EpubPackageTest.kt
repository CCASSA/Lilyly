package com.lilyly.app

import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.util.zip.*

class EpubPackageTest {
    private fun fixture(href: String="second.xhtml", encrypted: Boolean=false, body: (ZipFile)->Unit) {
        val file=File.createTempFile("lilyly-epub", ".epub")
        try {
            ZipOutputStream(file.outputStream()).use { z ->
                fun entry(name:String,value:String) { z.putNextEntry(ZipEntry(name));z.write(value.toByteArray());z.closeEntry() }
                entry("META-INF/container.xml","""<container xmlns="urn:oasis:names:tc:opendocument:xmlns:container"><rootfiles><rootfile full-path="OPS/book.opf"/></rootfiles></container>""")
                entry("OPS/book.opf","""<package xmlns="http://www.idpf.org/2007/opf"><metadata xmlns:dc="http://purl.org/dc/elements/1.1/"><dc:title>Garden</dc:title><dc:creator>Fixture</dc:creator></metadata><manifest><item id="one" href="first.xhtml" media-type="application/xhtml+xml"/><item id="two" href="$href" media-type="application/xhtml+xml"/></manifest><spine><itemref idref="two"/><itemref idref="one"/></spine></package>""")
                entry("OPS/first.xhtml","<html><title>First</title><body>One</body></html>")
                entry("OPS/second.xhtml","<html><title>Second</title><body>Two</body></html>")
                if(encrypted) entry("META-INF/encryption.xml","<encryption/>")
            }
            ZipFile(file).use(body)
        } finally { file.delete() }
    }
    @Test fun respectsSpineOrderInsteadOfArchiveOrder() { fixture { zip ->
        val epub=EpubPackage(zip).parse()
        assertEquals("Garden",epub.title);assertEquals("Fixture",epub.author)
        assertEquals(listOf("Second","First"),epub.chapters.map { it.title })
    } }
    @Test fun rejectsRemoteAndEscapingResourcePaths() {
        listOf("https://example.com/book.xhtml","../../secret.xhtml").forEach { path -> fixture(path) { zip ->
            assertTrue(runCatching { EpubPackage(zip).parse() }.isFailure)
        } }
    }
    @Test fun doesNotPretendToReadEncryptedBooks() { fixture(encrypted=true) { zip ->
        val failure=runCatching { EpubPackage(zip).parse() }.exceptionOrNull()
        assertTrue(failure?.message.orEmpty().contains("encrypted"))
    } }
}
