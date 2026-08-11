package com.weatherdrive.ui.theme

import androidx.compose.ui.unit.dp

object PlayerDimens {
    val minimizedHeight = 60.dp
    val maximizedHeight = 650.dp
    val bottomPadding = 120.dp
    val expandedBottomPadding = 8.dp
    val cornerRadius = minimizedHeight / 2

    /** Extra scroll clearance so list content isn't obscured by the mini player + tab bar. */
    val listBottomClearance = 160.dp
}
