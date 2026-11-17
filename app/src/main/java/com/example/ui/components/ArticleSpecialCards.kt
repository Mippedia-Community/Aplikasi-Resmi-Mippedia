package com.example.ui.components

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.CareerCell
import com.example.model.CareerTableSection
import com.example.model.SportsSeasonStatCard
import com.example.model.WikiRelatedArticle
import com.example.ui.theme.MippediaAmber
import com.example.ui.theme.MippediaTealAccent
import kotlinx.coroutines.delay

@Composable
fun SportsSeasonStatsDeck(
    seasonStats: List<SportsSeasonStatCard>,
    modifier: Modifier = Modifier
) {
    if (seasonStats.isEmpty()) return
    var showAll by remember(seasonStats) { mutableStateOf(false) }
    val visibleStats = if (showAll) seasonStats else listOf(seasonStats.first())

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.SportsSoccer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Statistik Musim Terbaru",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        visibleStats.forEach { stat ->
            SingleSeasonStatCard(stat = stat)
        }

        if (seasonStats.size > 1) {
            FilledTonalButton(
                onClick = { showAll = !showAll },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Icon(
                    imageVector = if (showAll) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (showAll) "Tampilkan Lebih Sedikit" else "Lihat Kompetisi Lainnya (${seasonStats.size - 1})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SingleSeasonStatCard(stat: SportsSeasonStatCard) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Distinctive Gradient Athletic Header (different from web HTML!)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF0F172A), Color(0xFF1E3A5F))
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stat.headerTitle,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 0.6.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF38BDF8).copy(alpha = 0.22f)
                    ) {
                        Text(
                            text = "LIVE STATS",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7DD3FC),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // 4 KPI Metric Pills
            if (stat.metrics.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    stat.metrics.forEach { (label, value) ->
                        val lower = label.lowercase()
                        val accentBg = when {
                            lower.contains("gol") -> Color(0xFF16A34A).copy(alpha = 0.12f)
                            lower.contains("assist") -> Color(0xFF2563EB).copy(alpha = 0.12f)
                            lower.contains("rating") -> Color(0xFF0284C7)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                        }
                        val valueColor = when {
                            lower.contains("gol") -> Color(0xFF15803D)
                            lower.contains("assist") -> Color(0xFF1D4ED8)
                            lower.contains("rating") -> Color.White
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                        val labelColor = if (lower.contains("rating")) {
                            Color.White.copy(alpha = 0.85f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = accentBg,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = label.replace("RATA2 ", ""),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = labelColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = value,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = valueColor,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Detailed Stat Rows
            if (stat.details.isNotEmpty()) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                    modifier = Modifier.padding(horizontal = 14.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    stat.details.forEachIndexed { idx, (key, valStr) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (idx % 2 == 0) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                    else Color.Transparent
                                )
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = valStr,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CareerAndAchievementsDeck(
    sections: List<CareerTableSection>,
    onArticleLinkClick: (String) -> Unit,
    onRedlinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (sections.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        sections.forEach { section ->
            val lowerTitle = section.title.lowercase()
            val isAchievement = lowerTitle.contains("prestasi") || lowerTitle.contains("penghargaan") || lowerTitle.contains("gelar")
            val isEsports = lowerTitle.contains("profesional") || section.headers.any { it.contains("Turnamen", ignoreCase = true) }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Section Banner Header (distinct Material 3 Android style)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isAchievement) MippediaAmber.copy(alpha = 0.14f)
                                else MippediaTealAccent.copy(alpha = 0.13f)
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isAchievement) MippediaAmber.copy(alpha = 0.25f) else MippediaTealAccent.copy(alpha = 0.22f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = when {
                                            isAchievement -> Icons.Default.EmojiEvents
                                            isEsports -> Icons.Default.SportsEsports
                                            else -> Icons.Default.Timeline
                                        },
                                        contentDescription = null,
                                        tint = if (isAchievement) Color(0xFFB45309) else MippediaTealAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Text(
                                text = section.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "${section.rows.size} entri",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Body Rows
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isAchievement) {
                            section.rows.forEach { row ->
                                AchievementEntryCard(
                                    row = row,
                                    onArticleLinkClick = onArticleLinkClick,
                                    onRedlinkClick = onRedlinkClick
                                )
                            }
                        } else {
                            section.rows.forEach { row ->
                                CareerTimelineRow(
                                    row = row,
                                    onArticleLinkClick = onArticleLinkClick,
                                    onRedlinkClick = onRedlinkClick
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CareerTimelineRow(
    row: List<CareerCell>,
    onArticleLinkClick: (String) -> Unit,
    onRedlinkClick: (String) -> Unit
) {
    val yearCell = row.getOrNull(0)
    val teamCell = row.getOrNull(1)
    val statCell = row.getOrNull(2)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Year Pill
            if (yearCell != null && yearCell.text.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.widthIn(min = 76.dp, max = 96.dp)
                ) {
                    Text(
                        text = yearCell.text,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }

            // Team / Club Name (clickable if wiki link)
            if (teamCell != null) {
                ClickableCellText(
                    cell = teamCell,
                    fontWeight = FontWeight.Bold,
                    onArticleLinkClick = onArticleLinkClick,
                    onRedlinkClick = onRedlinkClick,
                    modifier = Modifier.weight(1f)
                )
            }

            // Appearances / Goals / Assists
            if (statCell != null && statCell.text.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = statCell.text,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AchievementEntryCard(
    row: List<CareerCell>,
    onArticleLinkClick: (String) -> Unit,
    onRedlinkClick: (String) -> Unit
) {
    // Prestasi / Prestasi utama has [Tim, Kompetisi/Turnamen, Tahun]
    val teamCell = row.getOrNull(0)
    val competitionCell = row.getOrNull(1)
    val yearCell = row.getOrNull(2)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                if (competitionCell != null) {
                    ClickableCellText(
                        cell = competitionCell,
                        fontWeight = FontWeight.Bold,
                        onArticleLinkClick = onArticleLinkClick,
                        onRedlinkClick = onRedlinkClick,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (teamCell != null && teamCell.text.isNotBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.let { base ->
                            if (!teamCell.targetTitle.isNullOrBlank()) {
                                base.clickable {
                                    if (teamCell.isRedlink) onRedlinkClick(teamCell.targetTitle)
                                    else onArticleLinkClick(teamCell.targetTitle)
                                }
                            } else base
                        }
                    ) {
                        Text(
                            text = teamCell.text,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (yearCell != null && yearCell.text.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MippediaAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = yearCell.text,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ClickableCellText(
    cell: CareerCell,
    fontWeight: FontWeight,
    onArticleLinkClick: (String) -> Unit,
    onRedlinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val target = cell.targetTitle
    val textColor = when {
        target == null -> MaterialTheme.colorScheme.onSurface
        cell.isRedlink -> Color(0xFFB91C1C)
        else -> MaterialTheme.colorScheme.primary
    }
    Text(
        text = cell.text,
        style = MaterialTheme.typography.bodySmall.copy(
            fontWeight = fontWeight,
            textDecoration = if (target != null && !cell.isRedlink) TextDecoration.None else TextDecoration.None
        ),
        color = textColor,
        modifier = modifier.let { base ->
            if (!target.isNullOrBlank()) {
                base.clickable {
                    if (cell.isRedlink) onRedlinkClick(target)
                    else onArticleLinkClick(target)
                }
            } else base
        }
    )
}

@Composable
fun RelatedArticleRichCard(
    item: WikiRelatedArticle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!item.thumbnailUrl.isNullOrBlank()) {
                AsyncImage(
                    model = item.thumbnailUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    modifier = Modifier.size(60.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Article,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!item.description.isNullOrBlank()) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 17.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun ArticleAudioMiniPlayer(
    audioUrl: String,
    audioTitle: String,
    onOpenInCommons: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPrepared by remember(audioUrl) { mutableStateOf(false) }
    var isPlaying by remember(audioUrl) { mutableStateOf(false) }
    var isError by remember(audioUrl) { mutableStateOf(false) }
    var currentMs by remember(audioUrl) { mutableIntStateOf(0) }
    var durationMs by remember(audioUrl) { mutableIntStateOf(0) }

    val player = remember(audioUrl) {
        MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setOnPreparedListener { mp ->
                isPrepared = true
                durationMs = mp.duration.coerceAtLeast(0)
                mp.start()
                isPlaying = true
            }
            setOnCompletionListener {
                isPlaying = false
                currentMs = 0
            }
            setOnErrorListener { _, _, _ ->
                isError = true
                isPlaying = false
                true
            }
            try {
                setDataSource(audioUrl)
                prepareAsync()
            } catch (_: Exception) {
                isError = true
            }
        }
    }

    DisposableEffect(player) {
        onDispose {
            try {
                if (player.isPlaying) player.stop()
                player.release()
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(isPlaying, isPrepared) {
        while (isPlaying && isPrepared) {
            try {
                currentMs = player.currentPosition
            } catch (_: Exception) {}
            delay(200)
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledIconButton(
                    onClick = {
                        if (isPrepared) {
                            if (isPlaying) {
                                player.pause()
                                isPlaying = false
                            } else {
                                player.start()
                                isPlaying = true
                            }
                        }
                    },
                    enabled = isPrepared && !isError,
                    modifier = Modifier.size(44.dp)
                ) {
                    if (!isPrepared && !isError) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.VolumeUp,
                            contentDescription = if (isPlaying) "Jeda" else "Putar Audio"
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = audioTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isError) "Gagal memuat audio" else if (!isPrepared) "Memuat suara..." else "${formatMiniTime(currentMs)} / ${formatMiniTime(durationMs)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                    )
                }

                IconButton(onClick = onOpenInCommons) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "Detail Berkas Audio",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup Pemutar Audio",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isPrepared && durationMs > 0) {
                val safeMax = durationMs.coerceAtLeast(1).toFloat()
                Slider(
                    value = currentMs.toFloat().coerceIn(0f, safeMax),
                    onValueChange = { pos ->
                        currentMs = pos.toInt()
                        player.seekTo(currentMs)
                    },
                    valueRange = 0f..safeMax,
                    modifier = Modifier.height(24.dp)
                )
            }
        }
    }
}

private fun formatMiniTime(ms: Int): String {
    val sec = (ms / 1000).coerceAtLeast(0)
    return "%02d:%02d".format(sec / 60, sec % 60)
}
