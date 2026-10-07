package com.lilyly.app

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

private data class NavItem(val route: String, val label: String, val icon: @Composable () -> Unit)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LilylyApp(store: AppStore, openMedication: Boolean = false) {
    LilylyTheme(store.darkTheme) {
        var route by rememberSaveable { mutableStateOf(if(openMedication) "sanctuary" else "home") }
        var section by rememberSaveable { mutableStateOf("Journal") }
        var editingId by rememberSaveable { mutableStateOf<String?>(null) }

        var initialBookId by rememberSaveable { mutableStateOf<String?>(null) }
        var sanctuaryTab by rememberSaveable { mutableStateOf(if(openMedication) "Medication" else "Check-in") }

        val topLevel = route in listOf("home", "journal", "cycle", "sanctuary", "more")
        val navItems = listOf(
            NavItem("home", "Home") { Icon(Icons.Default.Home, null) },
            NavItem("journal", "Journal") { Icon(Icons.Default.AutoStories, null) },
            NavItem("cycle", "Cycle") { Icon(Icons.Default.LocalFlorist, null) },
            NavItem("sanctuary", "Sanctuary") { Icon(Icons.Default.Psychology, null) },
            NavItem("more", "More") { Icon(Icons.Default.MoreHoriz, null) }
        )

        Scaffold(
            modifier = Modifier.imePadding(),
            topBar = {
                if (topLevel) {
                    TopAppBar(
                        title = { Text(if (route == "home") "Lilyly" else navItems.firstOrNull { it.route == route }?.label ?: "Lilyly") },
                        actions = {
                            IconButton(onClick = { route = "search" }) { Icon(Icons.Default.Search, "Search") }
                            IconButton(onClick = { store.setTheme(!store.darkTheme) }) { Icon(Icons.Default.DarkMode, "Theme") }
                        }
                    )
                }
            },
            bottomBar = {
                if (topLevel) {
                    BottomAppBar {
                        navItems.forEach { item ->
                            NavigationBarItem(
                                selected = route == item.route,
                                onClick = {
                                    route = item.route
                                    if(item.route == "sanctuary") sanctuaryTab="Check-in"
                                    if (item.route == "journal") section = "Journal"
                                },
                                icon = item.icon,
                                label = { Text(item.label) }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(if (topLevel) padding else PaddingValues())) {
                when (route) {
                    "home" -> LibraryHomeScreen(
                        store = store,
                        onOpenSection = { s -> section = s; route = if(s == "Dreams") "sleep" else "section" },
                        onOpenCycle = { route = "cycle" },
                        onOpenSanctuary = { sanctuaryTab="Check-in";route = "sanctuary" },
                        onOpenTarot = { route = "tarot" },
                        onOpenBookshelf = { initialBookId=null;route = "bookshelf" },
                        onOpenCalendar = { route = "calendar" },
                        onReadBook = { id -> initialBookId=id;route="bookshelf" },
                        onNewPage = { editingId=null;section="Journal";route="editor" },
                        onMedication = { sanctuaryTab="Medication";route="sanctuary" },
                        onOpenEntry = { entry -> editingId=entry.id;section=entry.section;route="editor" }
                    )
                    "journal" -> JournalListScreen(store, "Journal", onEdit = { editingId = it; section = "Journal"; route = "editor" }, onNew = { editingId = null; section = "Journal"; route = "editor" })
                    "section" -> JournalListScreen(store, section, onBack = { route = "home" }, onEdit = { editingId = it; route = "editor" }, onNew = { editingId = null; route = "editor" })
                    "editor" -> JournalEditorScreen(store, editingId, section, onDone = { route = if (section == "Journal") "journal" else "section" }, onBack = { route = if (section == "Journal") "journal" else "section" })
                    "cycle" -> CycleScreen(store)
                    "sanctuary" -> SanctuaryScreen(store, sanctuaryTab)
                    "more" -> MoreHubScreen(
                        store = store,
                        onOpenSection = { s -> section = s; route = if(s == "Dreams") "sleep" else "section" },
                        onOpenTarot = { route = "tarot" },
                        onOpenBookshelf = { initialBookId=null;route = "bookshelf" },
                        onOpenCalendar = { route = "calendar" },
                        onOpenSettings = { route = "settings" }
                    )
                    "tarot" -> TarotScreen(store, onBack = { route = "more" }, onJournal = { entry -> section = entry.section; editingId = entry.id; route = "editor" })
                    "bookshelf" -> BookshelfScreen(store, initialBookId=initialBookId, onBack = { route = "more" }, onJournal = { entry -> section = entry.section; editingId = entry.id; route = "editor" })
                    "sleep" -> SleepScreen(store, onBack = { route = "more" }, onJournal = { entry -> section = entry.section; editingId = entry.id; route = "editor" }, onDreamPages = { section = "Dreams"; route = "section" })
                    "calendar" -> MagicalCalendarScreen(store, onBack = { route = "more" })
                    "settings" -> SettingsScreen(store, onBack = { route = "more" })
                    "search" -> SearchScreen(store, onBack = { route = "home" }, onOpenJournal = { entry -> section = entry.section; editingId = entry.id; route = "editor" })
                }
            }
        }
    }
}
