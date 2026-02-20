package com.weatherdrive.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.weatherdrive.model.CategoryNode
import com.weatherdrive.model.Show
import com.weatherdrive.model.YearNode
import com.weatherdrive.viewmodel.HomeViewModel

@Composable
fun HomeScreen(viewModel: HomeViewModel = remember { HomeViewModel() }) {
    val treeNodes by viewModel.treeNodes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Surface(modifier = Modifier.fillMaxWidth()) {
        when {
            isLoading -> CircularProgressIndicator(modifier = Modifier.padding(32.dp))
            error != null -> Text(
                text = "Error: $error",
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.error
            )
            treeNodes.isEmpty() -> Text(
                text = "No shows found.",
                modifier = Modifier.padding(16.dp)
            )
            else -> ExpandableTree(treeNodes)
        }
    }
}

@Composable
private fun ExpandableTree(treeNodes: List<YearNode>) {
    LazyColumn {
        items(treeNodes) { yearNode ->
            YearRow(yearNode)
        }
    }
}

@Composable
private fun YearRow(yearNode: YearNode) {
    var expanded by remember { mutableStateOf(true) }

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
                CategoryRow(categoryNode)
            }
        }
    }
}

@Composable
private fun CategoryRow(categoryNode: CategoryNode) {
    var expanded by remember { mutableStateOf(false) }

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
                ShowRow(show)
            }
        }
    }
}

@Composable
private fun ShowRow(show: Show) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 48.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!show.thumbnail.isNullOrBlank()) {
            AsyncImage(
                model = show.thumbnail,
                contentDescription = show.title,
                modifier = Modifier.size(56.dp)
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
