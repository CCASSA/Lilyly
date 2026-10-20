package com.lilyly.app

import java.util.Locale

val libraryStatuses = listOf("All books", "Unopened", "Reading", "Finished")
val librarySorts = listOf("Recently read", "Recently added", "Title", "Author")
fun libraryStatus(book:LibraryBook) = when {
    book.completed -> "Finished"
    book.lastReadAt.isBlank() -> "Unopened"
    else -> "Reading"
}
fun libraryCollections(books:List<LibraryBook>):List<String> =
    books.map {it.collection}.filter {it.isNotBlank()}.distinct().sortedWith(compareBy<String> {it.lowercase(Locale.ROOT)}.thenBy {it})

/** Uses stable book identities. Filtering never edits the library or its reading positions. */
fun librarySelection(books:List<LibraryBook>,query:String="",favorites:Boolean=false,
    status:String="All books",collection:String?=null,sort:String="Recently read"):List<LibraryBook> {
    val needle=query.trim()
    val rows=books.filter {
        (!favorites || it.favorite) &&
        (status=="All books" || libraryStatus(it)==status) &&
        (collection==null || it.collection==collection) &&
        listOf(it.title,it.author,it.collection).any {field ->field.contains(needle,true)}
    }
    val title=compareBy<LibraryBook> {it.title.lowercase(Locale.ROOT)}.thenBy {it.id}
    return when(sort) {
        "Title" -> rows.sortedWith(title)
        "Author" -> rows.sortedWith(compareBy<LibraryBook> {it.author.lowercase(Locale.ROOT)}.then(title))
        "Recently added" -> rows.sortedWith(compareByDescending<LibraryBook> {it.addedAt}.then(title))
        else -> rows.sortedWith(compareByDescending<LibraryBook> {it.lastReadAt}.thenByDescending {it.addedAt}.then(title))
    }
}
