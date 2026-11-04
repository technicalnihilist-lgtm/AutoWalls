package com.autowalls.app.ui.navigation

object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val LIBRARY = "library"
    const val CREDITS = "credits"
    const val CATEGORIES = "categories"

    const val ARG_CATEGORY_ID = "categoryId"
    const val ARG_WALLPAPER_ID = "wallpaperId"

    const val CATEGORY = "category/{$ARG_CATEGORY_ID}"
    const val DETAIL = "detail/{$ARG_WALLPAPER_ID}"

    fun category(categoryId: String) = "category/$categoryId"
    fun detail(wallpaperId: Long) = "detail/$wallpaperId"
}
