package com.example.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object SignUp : Screen("signup")
    data object Home : Screen("home")
    data object Archive : Screen("archive")
    data object Calendar : Screen("calendar")
    data object Search : Screen("search")
    data object Stats : Screen("stats")
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")
    data object Editor : Screen("editor?entryId={entryId}&dateMillis={dateMillis}") {
        fun createRoute(entryId: String? = null, dateMillis: Long? = null): String {
            val params = mutableListOf<String>()
            if (entryId != null) params.add("entryId=$entryId")
            if (dateMillis != null) params.add("dateMillis=$dateMillis")
            return if (params.isEmpty()) "editor" else "editor?${params.joinToString("&")}"
        }
    }
}
