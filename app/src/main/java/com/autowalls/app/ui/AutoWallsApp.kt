package com.autowalls.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.autowalls.app.AutoWallsApplication
import com.autowalls.app.R
import com.autowalls.app.ui.components.GlassSurface
import com.autowalls.app.ui.navigation.Routes
import com.autowalls.app.ui.screens.category.CategoriesScreen
import com.autowalls.app.ui.screens.category.CategoryScreen
import com.autowalls.app.ui.screens.category.CategoryViewModel
import com.autowalls.app.ui.screens.credits.CreditsScreen
import com.autowalls.app.ui.screens.credits.CreditsViewModel
import com.autowalls.app.ui.screens.detail.DetailScreen
import com.autowalls.app.ui.screens.detail.DetailViewModel
import com.autowalls.app.ui.screens.home.HomeScreen
import com.autowalls.app.ui.screens.home.HomeViewModel
import com.autowalls.app.ui.screens.library.LibraryScreen
import com.autowalls.app.ui.screens.library.LibraryViewModel
import com.autowalls.app.ui.screens.search.SearchScreen
import com.autowalls.app.ui.screens.search.SearchViewModel
import com.autowalls.app.ui.theme.AutoWallsColors

@Composable
fun AutoWallsApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = currentRoute in setOf(
        Routes.HOME, Routes.SEARCH, Routes.LIBRARY, Routes.CREDITS
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AutoWallsColors.Black)
    ) {
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Routes.HOME) {
                val viewModel: HomeViewModel = viewModel()
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                HomeScreen(
                    state = state,
                    onRefresh = viewModel::refresh,
                    onOpenSearch = { navController.navigate(Routes.SEARCH) },
                    onOpenWallpaper = { navController.navigate(Routes.detail(it.id)) },
                    onOpenCategory = { navController.navigate(Routes.category(it.id)) },
                    onOpenCategories = { navController.navigate(Routes.CATEGORIES) },
                    onOpenCredits = { navController.navigate(Routes.CREDITS) },
                    onToggleFavorite = viewModel::toggleFavorite
                )
            }

            composable(Routes.SEARCH) {
                val viewModel: SearchViewModel = viewModel()
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                SearchScreen(
                    state = state,
                    onQueryChange = viewModel::onQueryChange,
                    onSubmit = viewModel::submit,
                    onFilter = viewModel::applyFilter,
                    onClearFilter = viewModel::clearFilter,
                    onBack = { navController.popBackStack() },
                    onOpenWallpaper = { navController.navigate(Routes.detail(it.id)) },
                    onToggleFavorite = viewModel::toggleFavorite,
                    onLoadMore = viewModel::loadMore,
                    onRetry = viewModel::retry
                )
            }

            composable(Routes.CATEGORIES) {
                CategoriesScreen(
                    onBack = { navController.popBackStack() },
                    onOpenCategory = { navController.navigate(Routes.category(it.id)) }
                )
            }

            composable(
                route = Routes.CATEGORY,
                arguments = listOf(navArgument(Routes.ARG_CATEGORY_ID) { type = NavType.StringType })
            ) { entry ->
                val categoryId = entry.arguments?.getString(Routes.ARG_CATEGORY_ID).orEmpty()
                val viewModel = categoryViewModel(categoryId)
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                CategoryScreen(
                    state = state,
                    onBack = { navController.popBackStack() },
                    onOpenWallpaper = { navController.navigate(Routes.detail(it.id)) },
                    onToggleFavorite = viewModel::toggleFavorite,
                    onLoadMore = viewModel::loadMore,
                    onRetry = { viewModel.load(reset = true) }
                )
            }

            composable(
                route = Routes.DETAIL,
                arguments = listOf(navArgument(Routes.ARG_WALLPAPER_ID) { type = NavType.LongType })
            ) { entry ->
                val wallpaperId = entry.arguments?.getLong(Routes.ARG_WALLPAPER_ID) ?: 0L
                val viewModel = detailViewModel(wallpaperId)
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                DetailScreen(
                    state = state,
                    onBack = { navController.popBackStack() },
                    onToggleFavorite = viewModel::toggleFavorite,
                    onDownload = viewModel::download,
                    onSetWallpaper = viewModel::setAsWallpaper,
                    onSaveToGallery = viewModel::saveToGallery,
                    onShare = viewModel::share,
                    onCopyAttribution = viewModel::copyAttribution,
                    onRetry = viewModel::retry,
                    onConsumeMessage = viewModel::consumeMessage
                )
            }

            composable(Routes.LIBRARY) {
                val viewModel: LibraryViewModel = viewModel()
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                LibraryScreen(
                    state = state,
                    onOpenWallpaper = { navController.navigate(Routes.detail(it.id)) },
                    onToggleFavorite = viewModel::toggleFavorite,
                    onRemoveDownload = viewModel::removeDownload,
                    onClearRecents = viewModel::clearRecents
                )
            }

            composable(Routes.CREDITS) {
                val viewModel: CreditsViewModel = viewModel()
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                CreditsScreen(state = state)
            }
        }

        if (showBottomBar) {
            BottomBar(
                currentRoute = currentRoute,
                onNavigate = { route -> navController.navigateToTab(route) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun categoryViewModel(categoryId: String): CategoryViewModel {
    val app = LocalContext.current.applicationContext as AutoWallsApplication
    return viewModel(
        key = "category-$categoryId",
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                CategoryViewModel(app, categoryId) as T
        }
    )
}

@Composable
private fun detailViewModel(wallpaperId: Long): DetailViewModel {
    val app = LocalContext.current.applicationContext as AutoWallsApplication
    return viewModel(
        key = "detail-$wallpaperId",
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                DetailViewModel(app, wallpaperId) as T
        }
    )
}

private data class TabItem(val route: String, val label: String, val icon: Painter)

@Composable
private fun bottomTabs(): List<TabItem> = listOf(
    TabItem(Routes.HOME, "Home", painterResource(R.drawable.ic_home)),
    TabItem(Routes.SEARCH, "Search", painterResource(R.drawable.ic_search)),
    TabItem(Routes.LIBRARY, "Saved", painterResource(R.drawable.ic_bookmark)),
    TabItem(Routes.CREDITS, "Credits", painterResource(R.drawable.ic_shield))
)

@Composable
private fun BottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        GlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp),
            shape = RoundedCornerShape(22.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                bottomTabs().forEach { tab ->
                    val selected = currentRoute == tab.route
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onNavigate(tab.route) }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            painter = tab.icon,
                            contentDescription = tab.label,
                            tint = if (selected) AutoWallsColors.ChampagneBright
                            else AutoWallsColors.TextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) AutoWallsColors.ChampagneBright
                            else AutoWallsColors.TextTertiary,
                            fontSize = 8.sp
                        )
                    }
                }
            }
        }
    }
}
