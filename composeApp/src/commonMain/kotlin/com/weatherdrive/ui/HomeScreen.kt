package com.weatherdrive.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.weatherdrive.model.CategoryNode
import com.weatherdrive.model.ShowSummary
import com.weatherdrive.model.YearNode
import com.weatherdrive.ui.theme.PlayerDimens
import com.weatherdrive.viewmodel.HomeViewModel
import com.weatherdrive.viewmodel.UiState
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel<HomeViewModel>(),
    onShowClick: (ShowSummary) -> Unit = {},
    showTopBar: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsState()
    val density = LocalDensity.current
    var topBarHeightPx by remember { mutableStateOf(0) }
    val background = MaterialTheme.colorScheme.background

    val safeTopPadding = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding()
    val topBarHeight = if (topBarHeightPx > 0) with(density) { topBarHeightPx.toDp() } else 0.dp
    // Extra height below the bar so the gradient can finish fading past the title.
    val fadeOverlayHeight = if (showTopBar) {
        (if (topBarHeight > 0.dp) topBarHeight else safeTopPadding) + 40.dp
    } else {
        safeTopPadding + 40.dp
    }

    val listTopPadding = when {
        showTopBar && topBarHeight > 0.dp -> topBarHeight
        !showTopBar -> safeTopPadding
        else -> 0.dp
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
    ) {
        when (val state = uiState) {
            is UiState.Loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            is UiState.Error -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Error: ${state.message}",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            is UiState.Success -> if (state.treeNodes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No shows found.",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                ExpandableTree(
                    state.treeNodes,
                    viewModel::loadYear,
                    onShowClick,
                    PaddingValues(top = listTopPadding)
                )
            }
        }

        // Solid black at the top, fading to transparent below the title.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(fadeOverlayHeight)
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Black,
                        0.45f to Color.Black.copy(alpha = 0.7f),
                        1.0f to Color.Transparent,
                    )
                )
        )

        if (showTopBar) {
            TopAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { topBarHeightPx = it.height },
                title = {
                    Text(
                        "Browse",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    }
}

@Composable
private fun ExpandableTree(
    treeNodes: List<YearNode>,
    onLoadYear: (String) -> Unit,
    onShowClick: (ShowSummary) -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(treeNodes, key = { it.year }) { yearNode ->
            // Only the newest year opens by default; the rest load when expanded.
            YearRow(yearNode, yearNode.year == treeNodes.first().year, onLoadYear, onShowClick)
        }
        item { Spacer(modifier = Modifier.height(PlayerDimens.listBottomClearance)) }
    }
}

@Composable
private fun YearRow(
    yearNode: YearNode,
    initiallyExpanded: Boolean,
    onLoadYear: (String) -> Unit,
    onShowClick: (ShowSummary) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
    LaunchedEffect(expanded) {
        if (expanded) onLoadYear(yearNode.year)
    }

    Column(modifier = Modifier.animateContentSize()) {
        // Year header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { expanded = !expanded }
                .padding(vertical = 14.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = yearNode.year,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Black
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }

        if (expanded) {
            val categories = yearNode.children
            when {
                categories != null -> categories.forEach { categoryNode ->
                    CategoryRow(categoryNode, onShowClick)
                }
                yearNode.loadFailed -> Text(
                    text = "Couldn't load ${yearNode.year}. Tap to retry.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .clickable { onLoadYear(yearNode.year) }
                        .padding(horizontal = 8.dp, vertical = 10.dp)
                )
                else -> CircularProgressIndicator(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 10.dp)
                        .size(24.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

@Composable
private fun CategoryRow(categoryNode: CategoryNode, onShowClick: (ShowSummary) -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Column(modifier = Modifier.animateContentSize()) {
        // Category header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable { expanded = !expanded }
                .padding(start = 8.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = categoryNode.category.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(8.dp))
            // Count badge
            Box(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${categoryNode.children.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        if (expanded) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            ) {
                categoryNode.children.forEach { show ->
                    ShowCard(show, onShowClick)
                }
            }
        }
    }
}

@Composable
private fun ShowCard(show: ShowSummary, onShowClick: (ShowSummary) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onShowClick(show) },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = show.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                show.date?.formatted?.takeIf { it.isNotBlank() }?.let { dateStr ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
