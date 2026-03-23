package com.weatherdrive.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.weatherdrive.model.CategoryNode
import com.weatherdrive.model.Show
import com.weatherdrive.model.YearNode
import com.weatherdrive.viewmodel.HomeViewModel
import com.weatherdrive.viewmodel.UiState
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel<HomeViewModel>(),
    onShowClick: (Show) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WeatherDrive") }
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is UiState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                is UiState.Error -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Error: ${state.message}",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is UiState.Success -> if (state.treeNodes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No shows found.",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    ExpandableTree(state.treeNodes, onShowClick)
                }
            }
        }
    }
}

@Composable
private fun ExpandableTree(treeNodes: List<YearNode>, onShowClick: (Show) -> Unit) {
    LazyColumn {
        items(treeNodes, key = { it.year }) { yearNode ->
            YearRow(yearNode, onShowClick)
        }
    }
}

@Composable
private fun YearRow(yearNode: YearNode, onShowClick: (Show) -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(true) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${if (expanded) "▼" else "▶"} ${yearNode.year}",
                style = MaterialTheme.typography.titleLarge
            )
        }
        HorizontalDivider()

        if (expanded) {
            yearNode.children.forEach { categoryNode ->
                CategoryRow(categoryNode, onShowClick)
            }
        }
    }
}

@Composable
private fun CategoryRow(categoryNode: CategoryNode, onShowClick: (Show) -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(start = 32.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${if (expanded) "▼" else "▶"} ${categoryNode.category.replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.titleMedium
            )
        }
        HorizontalDivider(modifier = Modifier.padding(start = 32.dp))

        if (expanded) {
            categoryNode.children.forEach { show ->
                ShowRow(show, onShowClick)
            }
        }
    }
}

@Composable
private fun ShowRow(show: Show, onShowClick: (Show) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onShowClick(show) }
            .padding(start = 48.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!show.thumbnail.isNullOrBlank()) {
            AsyncImage(
                model = show.thumbnail,
                contentDescription = show.title,
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.small)
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        Text(
            text = show.title,
            style = MaterialTheme.typography.bodyMedium
        )
    }
    HorizontalDivider(modifier = Modifier.padding(start = 48.dp))
}
