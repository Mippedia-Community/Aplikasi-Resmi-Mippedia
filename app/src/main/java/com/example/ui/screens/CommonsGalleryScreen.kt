package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.WikiMediaItem
import com.example.ui.components.SmallSpinningLoader
import com.example.ui.theme.MippediaTealAccent

private enum class CommonsMediaFilter(val label: String) {
    ALL("Semua"),
    IMAGES("Gambar"),
    AUDIO("Audio"),
    VIDEO("Video")
}

@Composable
fun CommonsGalleryScreen(
    gallery: List<WikiMediaItem>,
    isLoading: Boolean,
    onMediaClick: (WikiMediaItem) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(CommonsMediaFilter.ALL) }

    val filteredGallery = remember(gallery, selectedFilter) {
        when (selectedFilter) {
            CommonsMediaFilter.ALL -> gallery
            CommonsMediaFilter.IMAGES -> gallery.filter { it.isImage }
            CommonsMediaFilter.AUDIO -> gallery.filter { it.isAudio }
            CommonsMediaFilter.VIDEO -> gallery.filter { it.isVideo }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Commons Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = null,
                            tint = MippediaTealAccent,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "Mippedia Commons",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Repositori Gambar, Audio & Video Bebas",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (isLoading) {
                        SmallSpinningLoader()
                    }
                }

                // Filter chips for Images / Audio / Video
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(CommonsMediaFilter.entries.size) { index ->
                        val filter = CommonsMediaFilter.entries[index]
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter.label, fontSize = 12.sp) },
                            leadingIcon = when (filter) {
                                CommonsMediaFilter.AUDIO -> {
                                    { Icon(Icons.Default.AudioFile, contentDescription = null, modifier = Modifier.size(15.dp)) }
                                }
                                CommonsMediaFilter.VIDEO -> {
                                    { Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(15.dp)) }
                                }
                                CommonsMediaFilter.IMAGES -> {
                                    { Icon(Icons.Outlined.Image, contentDescription = null, modifier = Modifier.size(15.dp)) }
                                }
                                CommonsMediaFilter.ALL -> null
                            },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        }

        // Media Grid
        if (gallery.isEmpty() && isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SmallSpinningLoader(modifier = Modifier.size(32.dp), strokeWidth = 3f)
                    Text(
                        text = "Memuat galeri berkas dari Commons...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (filteredGallery.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = when (selectedFilter) {
                            CommonsMediaFilter.AUDIO -> Icons.Default.AudioFile
                            CommonsMediaFilter.VIDEO -> Icons.Default.Videocam
                            else -> Icons.Outlined.Image
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(44.dp)
                    )
                    Text(
                        text = "Belum ada berkas ${selectedFilter.label.lowercase()} pada muatan saat ini.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!isLoading) {
                        OutlinedButton(onClick = onLoadMore) {
                            Text("Muat Lebih Banyak Berkas")
                        }
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredGallery) { item ->
                    Card(
                        onClick = { onMediaClick(item) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column {
                            if (item.isAudio || item.isVideo) {
                                val ext = item.title.substringAfterLast('.', "").uppercase().ifBlank {
                                    if (item.isAudio) "AUDIO" else "VIDEO"
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                        .background(
                                            Brush.linearGradient(
                                                colors = if (item.isAudio) {
                                                    listOf(Color(0xFF0F766E), Color(0xFF0284C7))
                                                } else {
                                                    listOf(Color(0xFF1E293B), Color(0xFF4F46E5))
                                                }
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (item.isAudio) Icons.Default.GraphicEq else Icons.Default.PlayCircleOutline,
                                        contentDescription = item.title,
                                        tint = Color.White,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color.Black.copy(alpha = 0.45f),
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = if (item.isAudio) "🔊 $ext" else "🎬 $ext",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            } else {
                                AsyncImage(
                                    model = item.url,
                                    contentDescription = item.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                )
                            }
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = item.title.removePrefix("Berkas:").removePrefix("File:"),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Oleh ${item.user.ifEmpty { "Pengguna" }}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Load More item
                item(span = { GridItemSpan(2) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            SmallSpinningLoader()
                        } else {
                            OutlinedButton(onClick = onLoadMore) {
                                Text("Muat Lebih Banyak Berkas")
                            }
                        }
                    }
                }
            }
        }
    }
}
