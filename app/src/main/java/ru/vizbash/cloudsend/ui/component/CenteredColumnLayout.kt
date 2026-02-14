package ru.vizbash.cloudsend.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private val ImageSize = 120.dp

@Composable
fun CenteredColumnLayout(
    image: @Composable () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    text: String? = null,
    actions: @Composable ColumnScope.() -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .height(300.dp)
                .widthIn(max = 300.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.size(ImageSize),
                contentAlignment = Alignment.Center,
            ) {
                image()
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            if (text != null) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
            actions()
        }
    }
}

@Composable
fun CenteredColumnLayoutArt(
    painter: Painter,
    modifier: Modifier = Modifier,
) {
    Image(
        modifier = modifier.size(ImageSize),
        painter = painter,
        contentDescription = null,
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.outlineVariant)
    )
}