package com.autowalls.app

import android.graphics.Bitmap
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.graphics.asAndroidBitmap
import coil.Coil
import coil.ImageLoader
import com.autowalls.app.ui.screens.category.CategoriesScreen
import com.autowalls.app.ui.screens.category.CategoryScreen
import com.autowalls.app.ui.screens.credits.CreditsScreen
import com.autowalls.app.ui.screens.detail.DetailScreen
import com.autowalls.app.ui.screens.home.HomeScreen
import com.autowalls.app.ui.screens.library.LibraryScreen
import com.autowalls.app.ui.screens.search.SearchScreen
import com.autowalls.app.ui.theme.AutoWallsTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

/**
 * Renders the real Compose screens headlessly and writes PNGs to build/screenshots.
 * Images are served from the bundled test photographs, so no network is used.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
class ScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    private val outDir = File("build/screenshots").apply { mkdirs() }
    private val photoDir = File("build/test-photos").apply { mkdirs() }

    @Before
    fun setUp() {
        val photos = listOf("car0.jpg", "car1.jpg", "car2.jpg", "car3.jpg", "car4.jpg", "car5.jpg")
        photos.forEach { name ->
            javaClass.classLoader!!.getResourceAsStream("photos/$name")!!.use { input ->
                File(photoDir, name).outputStream().use { out -> input.copyTo(out) }
            }
        }
        Coil.setImageLoader(
            ImageLoader.Builder(androidx.test.core.app.ApplicationProvider.getApplicationContext())
                .components { add(LocalPhotoInterceptor(photoDir)) }
                .build()
        )
    }

    @Test
    fun home() = capture("01-home") {
        HomeScreen(
            state = SampleData.homeState(),
            onRefresh = {}, onOpenSearch = {}, onOpenWallpaper = {},
            onOpenCategory = {}, onOpenCategories = {}, onOpenCredits = {},
            onToggleFavorite = {}
        )
    }

    @Test
    fun detail() = capture("02-detail") {
        DetailScreen(
            state = SampleData.detailState(),
            onBack = {}, onToggleFavorite = {}, onDownload = {},
            onSetWallpaper = {}, onSaveToGallery = {}, onShare = {},
            onCopyAttribution = {}, onRetry = {}, onConsumeMessage = {}
        )
    }

    @Test
    fun search() = capture("03-search") {
        SearchScreen(
            state = SampleData.searchState(),
            onQueryChange = {}, onSubmit = {}, onFilter = {}, onClearFilter = {},
            onBack = {}, onOpenWallpaper = {}, onToggleFavorite = {},
            onLoadMore = {}, onRetry = {}
        )
    }

    @Test
    fun category() = capture("04-category") {
        CategoryScreen(
            state = SampleData.categoryState(),
            onBack = {}, onOpenWallpaper = {}, onToggleFavorite = {},
            onLoadMore = {}, onRetry = {}
        )
    }

    @Test
    fun library() = capture("05-saved") {
        LibraryScreen(
            state = SampleData.libraryState(),
            onOpenWallpaper = {}, onToggleFavorite = {},
            onRemoveDownload = {}, onClearRecents = {}
        )
    }

    @Test
    fun credits() = capture("06-credits") {
        CreditsScreen(state = SampleData.creditsState())
    }

    @Test
    fun categories() = capture("07-categories") {
        CategoriesScreen(onBack = {}, onOpenCategory = {})
    }

    private fun capture(name: String, content: @androidx.compose.runtime.Composable () -> Unit) {
        // Shimmer placeholders animate forever, so the composition never goes idle.
        // Drive the frame clock manually instead of waiting for idle.
        rule.mainClock.autoAdvance = false
        rule.setContent { AutoWallsTheme { content() } }
        repeat(16) {
            rule.mainClock.advanceTimeBy(100)
            Thread.sleep(25)   // let Coil's IO coroutines deliver the images
        }
        val bitmap: Bitmap = rule.onRoot().captureToImage().asAndroidBitmap()
        FileOutputStream(File(outDir, "$name.png")).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        println("captured $name ${bitmap.width}x${bitmap.height}")
    }
}
