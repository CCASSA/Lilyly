package com.lilyly.app
import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
@RunWith(AndroidJUnit4::class)
class ReadingPassagesJourneyTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    private var previous=emptyMap<String,String>()
    @org.junit.After fun restoreFixture() {if(previous.isNotEmpty())rule.runOnUiThread {SecurePreferences(rule.activity).writeBatch(previous)}}
    @Test fun editAndRemoveSavedWordsPreservesBookAndJournalCopy() {
        lateinit var store:AppStore
        var opened=""
        var journal=""
        rule.runOnUiThread {
            val secure=SecurePreferences(rule.activity)
            previous=mapOf("books" to secure.get("books","[]"),"bookNotes" to secure.get("bookNotes","[]"),"journal" to secure.get("journal","[]"))
            secure.writeBatch(mapOf("books" to "[]","bookNotes" to "[]"))
            store=AppStore(rule.activity)
            store.saveBook(LibraryBook(id="passage-fixture",title="A moonlit book",format="EPUB",units=4,position=1,bookmarks=setOf(0)))
            store.saveBookNote(BookNote(id="words",bookId="passage-fixture",position=2,quote="The silver garden",note="First thought"))
            rule.activity.setContent {LilylyTheme(true){ReadingPassagesScreen(store,{}, {opened=it.bookId+":"+it.position},{journal=it.id})}}
        }
        rule.onNodeWithTag("reading-passages").performScrollToNode(hasText("Return to book"))
        rule.onNodeWithText("Return to book").performScrollTo().performClick()
        rule.runOnUiThread {assertEquals("passage-fixture:2",opened)}
        rule.onNodeWithText("Send to journal").performScrollTo().performClick()
        rule.onNodeWithText("Edit saved words").performScrollTo().performClick()
        rule.onNodeWithText("Reading thoughts").performTextReplacement("A second thought")
        rule.onNodeWithText("Keep changes").performClick()
        rule.runOnUiThread {
            val restored=AppStore(rule.activity)
            assertEquals("A second thought",restored.bookNotes.single().note)
            assertEquals(journal,restored.bookNotes.single().journalId)
            assertTrue(restored.journalEntries.first {it.id==journal}.body.contains("First thought"))
        }
        rule.onNodeWithText("Remove").performScrollTo().performClick()
        rule.onNodeWithText("Keep them").performClick()
        rule.runOnUiThread {assertEquals(1,AppStore(rule.activity).bookNotes.size)}
        rule.onNodeWithText("Remove").performScrollTo().performClick()
        rule.onNodeWithText("Remove saved words").performClick()
        rule.runOnUiThread {
            val restored=AppStore(rule.activity)
            assertTrue(restored.bookNotes.isEmpty())
            assertEquals(setOf(0),restored.books.single().bookmarks)
            assertEquals(1,restored.books.single().position)
            assertTrue(restored.journalEntries.any {it.id==journal})
        }
        rule.onNodeWithText("Your remembered words will gather here.",substring=true).assertIsDisplayed()
    }
}
