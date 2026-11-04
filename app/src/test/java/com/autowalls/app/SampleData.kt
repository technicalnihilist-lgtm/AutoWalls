package com.autowalls.app

import com.autowalls.app.data.download.DownloadState
import com.autowalls.app.domain.model.Wallpaper
import com.autowalls.app.ui.screens.category.CategoryUiState
import com.autowalls.app.ui.screens.credits.CreditsUiState
import com.autowalls.app.ui.screens.detail.DetailUiState
import com.autowalls.app.ui.screens.home.HomeUiState
import com.autowalls.app.ui.screens.home.SectionState
import com.autowalls.app.ui.screens.library.LibraryUiState
import com.autowalls.app.ui.screens.search.SearchUiState
import com.autowalls.app.domain.model.VehicleCatalog

/**
 * Deterministic sample state for the screenshot tests. The photographs and their
 * credits are real Wikimedia Commons images (see screenshots/CREDITS.md).
 */
object SampleData {

    private fun wallpaper(
        id: Long,
        name: String,
        artist: String,
        license: String,
        licenseUrl: String,
        source: String,
        categoryId: String,
        categoryName: String,
        photo: String,
        width: Int = 1200,
        height: Int = 800
    ) = Wallpaper(
        id = id,
        title = "File:$name",
        displayName = name.substringBeforeLast('.'),
        fullUrl = LocalPhotoInterceptor.url(photo),
        previewUrl = LocalPhotoInterceptor.url(photo),
        thumbUrl = LocalPhotoInterceptor.url(photo),
        width = width,
        height = height,
        mime = "image/jpeg",
        fileSizeBytes = 2_400_000,
        artist = artist,
        credit = "Own work",
        description = "Photographed at a public motor show.",
        licenseName = license,
        licenseUrl = licenseUrl,
        sourcePage = source,
        uploadedBy = artist,
        dateOriginal = "2018-03-06",
        categoryId = categoryId,
        categoryName = categoryName
    )

    val lamborghini = wallpaper(
        1, "Lamborghini Aventador S Roadster Back IMG 0717.jpg", "Alexander Migl",
        "CC BY-SA 4.0", "https://creativecommons.org/licenses/by-sa/4.0/",
        "https://commons.wikimedia.org/wiki/File:Lamborghini_Aventador_S_Roadster_Back_IMG_0717.jpg",
        "lamborghini", "Lamborghini", "car0.jpg", 5459, 3616
    )

    val ferrari = wallpaper(
        2, "Ferrari-Monaco-4071026.jpg", "ermell",
        "CC BY-SA 4.0", "https://creativecommons.org/licenses/by-sa/4.0/",
        "https://commons.wikimedia.org/wiki/File:Ferrari-Monaco-4071026.jpg",
        "ferrari", "Ferrari", "car1.jpg", 4000, 3000
    )

    val porsche = wallpaper(
        3, "Porsche 911 Carrera RSR, Bj. 1974 (2011-08-13 Sp).JPG", "Lothar Spurzem",
        "CC BY-SA 2.0 de", "https://creativecommons.org/licenses/by-sa/2.0/de/deed.en",
        "https://commons.wikimedia.org/wiki/File:Porsche_911_Carrera_RSR,_Bj._1974_(2011-08-13_Sp).JPG",
        "porsche", "Porsche", "car2.jpg", 3000, 2000
    )

    val ducati = wallpaper(
        4, "Ducati 1199 Panigale S.jpg", "Klaus Nahr",
        "CC BY-SA 2.0", "https://creativecommons.org/licenses/by-sa/2.0/",
        "https://commons.wikimedia.org/wiki/File:Ducati_1199_Panigale_S.jpg",
        "ducati", "Ducati", "car3.jpg", 3000, 2000
    )

    val bugatti = wallpaper(
        5, "Bugatti Chiron, GIMS 2018, Le Grand-Saconnex (1X7A1765).jpg", "Matti Blume",
        "CC BY-SA 4.0", "https://creativecommons.org/licenses/by-sa/4.0/",
        "https://commons.wikimedia.org/wiki/File:Bugatti_Chiron,_GIMS_2018,_Le_Grand-Saconnex_(1X7A1765).jpg",
        "bugatti", "Bugatti", "car4.jpg", 6000, 4000
    )

    val rollsRoyce = wallpaper(
        6, "Wooden-spoked-wheel on a 1909 Rolls-Royce Silver Ghost.jpg", "DeFacto",
        "CC BY-SA 4.0", "https://creativecommons.org/licenses/by-sa/4.0/",
        "https://commons.wikimedia.org/wiki/File:Wooden-spoked-wheel_on_a_1909_Rolls-Royce_Silver_Ghost.jpg",
        "rolls-royce", "Rolls-Royce", "car5.jpg", 4000, 3000
    )

    val all = listOf(lamborghini, ferrari, porsche, bugatti, ducati, rollsRoyce)

    fun homeState() = HomeUiState(
        daily = lamborghini,
        dailyLoading = false,
        featured = SectionState(items = listOf(ferrari, porsche, bugatti), loading = false),
        trending = SectionState(items = listOf(bugatti, lamborghini, rollsRoyce), loading = false),
        latest = SectionState(items = listOf(ducati, porsche, ferrari, rollsRoyce), loading = false),
        motorcycles = SectionState(items = listOf(ducati, ducati), loading = false),
        luxury = SectionState(items = listOf(rollsRoyce, lamborghini), loading = false),
        recent = listOf(ferrari, bugatti),
        favorites = setOf(ferrari.id, bugatti.id),
        refreshing = false
    )

    fun detailState() = DetailUiState(
        wallpaper = bugatti,
        loading = false,
        isFavorite = true,
        download = DownloadState.Completed("/data/user/0/com.autowalls.app/files/bugatti.jpg")
    )

    fun searchState() = SearchUiState(
        query = "Bugatti",
        items = listOf(bugatti, lamborghini, ferrari, porsche, rollsRoyce, ducati),
        loading = false,
        hasSearched = true,
        favorites = setOf(bugatti.id)
    )

    fun libraryState() = LibraryUiState(
        favorites = listOf(ferrari, bugatti, lamborghini),
        downloads = listOf(bugatti, porsche),
        recents = listOf(lamborghini, ducati, ferrari),
        downloadedIds = setOf(bugatti.id, porsche.id)
    )

    fun creditsState() = CreditsUiState(credited = all.sortedBy { it.artist })

    fun categoryState() = CategoryUiState(
        category = VehicleCatalog.byId("supercars"),
        items = listOf(lamborghini, ferrari, bugatti, porsche, rollsRoyce, ducati),
        loading = false,
        totalLoaded = 6,
        favorites = setOf(bugatti.id)
    )
}
