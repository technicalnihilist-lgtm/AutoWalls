package com.autowalls.app.ui.screens.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.autowalls.app.R
import com.autowalls.app.domain.model.CategoryGroup
import com.autowalls.app.domain.model.VehicleCatalog
import com.autowalls.app.domain.model.VehicleCategory
import com.autowalls.app.ui.theme.AutoWallsColors

/** Every category, grouped exactly as the brief defines them. */
@Composable
fun CategoriesScreen(
    onBack: () -> Unit,
    onOpenCategory: (VehicleCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AutoWallsColors.Black)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = "Back",
                    tint = AutoWallsColors.TextPrimary
                )
            }
            Column {
                Text(
                    text = "EXPLORE",
                    style = MaterialTheme.typography.labelSmall,
                    color = AutoWallsColors.Champagne
                )
                Text(
                    text = "All categories",
                    style = MaterialTheme.typography.headlineMedium,
                    color = AutoWallsColors.TextPrimary
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CategoryGroup.entries.forEach { group ->
                item(key = "header-${group.name}") {
                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = group.label.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = AutoWallsColors.Champagne
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "${VehicleCatalog.byGroup(group).size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = AutoWallsColors.TextTertiary
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                }

                items(VehicleCatalog.byGroup(group), key = { it.id }) { category ->
                    CategoryTile(
                        name = category.name,
                        group = group.label,
                        onClick = { onOpenCategory(category) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
