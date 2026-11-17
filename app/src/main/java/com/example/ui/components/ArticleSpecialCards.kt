package com.example.ui.components

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
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
import com.example.ui.theme.MippediaGreen
import com.example.ui.theme.MippediaTealAccent
import kotlinx.coroutines.delay

@Composable
fun SpecialTablesCollapsibleSection(
    seasonStats: List<SportsSeasonStatCard>,
    careerSections: List<CareerTableSection>,
    articleKey: String,
    onArticleLinkClick: (String) -> Unit,
    onRedlinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (seasonStats.isEmpty() && careerSections.isEmpty()) return

    // Hidden by default as requested; user chooses when to expand/collapse
    var expanded by remember(articleKey) { mutableStateOf(false) }

    val sectionTitles = buildList {
        if (seasonStats.isNotEmpty()) add("Statistik Musim (${seasonStats.size})")
        careerSections.forEach { add(it.title) }
    }
    val totalModules = (if (seasonStats.isNotEmpty()) 1 else 0) + careerSections.size

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.TableChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tabel Khusus & Statistik",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$totalModules bagian data tersedia",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                FilledTonalButton(
                    onClick = { expanded = !expanded },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier
                        .heightIn(min = 42.dp)
                        .testTag("toggle_special_tables_button")
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (expanded) "Sembunyikan" else "Tampilkan",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (!expanded && sectionTitles.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(sectionTitles) { label ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (seasonStats.isNotEmpty()) {
                        SportsSeasonStatsDeck(seasonStats = seasonStats)
                    }
                    if (careerSections.isNotEmpty()) {
                        CareerAndAchievementsDeck(
                            sections = careerSections,
                            onArticleLinkClick = onArticleLinkClick,
                            onRedlinkClick = onRedlinkClick
                        )
                    }
                }
            }
        }
    }
}

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
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(0.55f)
                            )
                            Text(
                                text = valStr,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(0.45f)
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
            val isMatchSection = lowerTitle.contains("pertandingan") || lowerTitle.contains("rekor bertarung") ||
                section.headers.any { h -> h.equals("Hasil", ignoreCase = true) || h.equals("Lawan", ignoreCase = true) }
            val isInfoSection = lowerTitle.contains("informasi") || lowerTitle.contains("media sosial")

            val accentColor = when {
                isAchievement -> Color(0xFFB45309)
                isMatchSection -> Color(0xFF0369A1)
                else -> MippediaTealAccent
            }
            val headerTint = when {
                isAchievement -> MippediaAmber.copy(alpha = 0.14f)
                isMatchSection -> Color(0xFF0284C7).copy(alpha = 0.12f)
                else -> MippediaTealAccent.copy(alpha = 0.13f)
            }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Section Banner Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(headerTint)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = accentColor.copy(alpha = 0.18f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = when {
                                            isAchievement -> Icons.Default.EmojiEvents
                                            isEsports -> Icons.Default.SportsEsports
                                            isMatchSection -> Icons.Default.SportsMma
                                            isInfoSection -> Icons.Default.Insights
                                            else -> Icons.Default.Timeline
                                        },
                                        contentDescription = null,
                                        tint = accentColor,
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
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                        ) {
                            Text(
                                text = "${section.rows.size} entri",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Body Rows routed intelligently by row cardinality & cell structure
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        section.rows.forEach { row ->
                            val nonEmpty = row.filter { it.text.isNotBlank() }
                            when {
                                nonEmpty.isEmpty() -> Unit
                                nonEmpty.size == 1 -> {
                                    SingleSpanInfoRow(
                                        cell = nonEmpty.first(),
                                        onArticleLinkClick = onArticleLinkClick,
                                        onRedlinkClick = onRedlinkClick
                                    )
                                }
                                nonEmpty.size == 2 -> {
                                    KeyValueCareerRow(
                                        labelCell = nonEmpty[0],
                                        valueCell = nonEmpty[1],
                                        onArticleLinkClick = onArticleLinkClick,
                                        onRedlinkClick = onRedlinkClick
                                    )
                                }
                                isAchievement -> {
                                    AchievementEntryCard(
                                        row = nonEmpty,
                                        onArticleLinkClick = onArticleLinkClick,
                                        onRedlinkClick = onRedlinkClick
                                    )
                                }
                                isMatchSection || shouldUseStackedCard(nonEmpty, section.headers) -> {
                                    StructuredDetailEntryCard(
                                        row = nonEmpty,
                                        headers = section.headers,
                                        onArticleLinkClick = onArticleLinkClick,
                                        onRedlinkClick = onRedlinkClick
                                    )
                                }
                                else -> {
                                    CareerTimelineRow(
                                        row = nonEmpty,
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
}

private fun shouldUseStackedCard(row: List<CareerCell>, headers: List<String>): Boolean {
    if (row.size > 3) return true
    if (headers.any { h ->
            val hl = h.lowercase()
            hl.contains("hasil") || hl.contains("lawan") || hl.contains("ajang") || hl.contains("acara")
        }
    ) return true
    val firstLen = row.getOrNull(0)?.text?.length ?: 0
    val secondLen = row.getOrNull(1)?.text?.length ?: 0
    val thirdLen = row.getOrNull(2)?.text?.length ?: 0
    return firstLen > 14 || thirdLen > 20 || (firstLen > 10 && secondLen > 16)
}

@Composable
private fun SingleSpanInfoRow(
    cell: CareerCell,
    onArticleLinkClick: (String) -> Unit,
    onRedlinkClick: (String) -> Unit
) {
    val isShortSubheader = cell.isHeaderCell || (cell.text.length <= 36 && !cell.text.contains("data akurat", ignoreCase = true) && !cell.text.endsWith("."))
    if (isShortSubheader) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 2.dp, start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
            ClickableCellText(
                cell = cell,
                fontWeight = FontWeight.Bold,
                onArticleLinkClick = onArticleLinkClick,
                onRedlinkClick = onRedlinkClick
            )
        }
    } else {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(15.dp)
                )
                ClickableCellText(
                    cell = cell,
                    fontWeight = FontWeight.Normal,
                    onArticleLinkClick = onArticleLinkClick,
                    onRedlinkClick = onRedlinkClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun KeyValueCareerRow(
    labelCell: CareerCell,
    valueCell: CareerCell,
    onArticleLinkClick: (String) -> Unit,
    onRedlinkClick: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ClickableCellText(
                cell = labelCell,
                fontWeight = FontWeight.Medium,
                customColor = MaterialTheme.colorScheme.onSurfaceVariant,
                onArticleLinkClick = onArticleLinkClick,
                onRedlinkClick = onRedlinkClick,
                modifier = Modifier.weight(0.48f)
            )
            ClickableCellText(
                cell = valueCell,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                onArticleLinkClick = onArticleLinkClick,
                onRedlinkClick = onRedlinkClick,
                modifier = Modifier.weight(0.52f)
            )
        }
    }
}

@Composable
private fun StructuredDetailEntryCard(
    row: List<CareerCell>,
    headers: List<String>,
    onArticleLinkClick: (String) -> Unit,
    onRedlinkClick: (String) -> Unit
) {
    val firstCell = row.getOrNull(0)
    val secondCell = row.getOrNull(1)
    val thirdCell = row.getOrNull(2)
    val extraCells = if (row.size > 3) row.drop(3) else emptyList()

    val firstLower = firstCell?.text?.lowercase().orEmpty()
    val isWin = firstLower.contains("menang") || firstLower.contains("win") || firstLower.contains("✓")
    val isLoss = firstLower.contains("kalah") || firstLower.contains("loss") || firstLower.contains("✗")
    val badgeBg = when {
        isWin -> MippediaGreen.copy(alpha = 0.14f)
        isLoss -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    }
    val badgeFg = when {
        isWin -> Color(0xFF15803D)
        isLoss -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Status / Header Badge
            if (firstCell != null && firstCell.text.isNotBlank()) {
                val h0 = headers.getOrNull(0)?.takeIf { it.isNotBlank() }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (h0 != null) "$h0: ${firstCell.text}" else firstCell.text,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = badgeFg,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Prominent Primary Subject (Lawan / Tim / Judul) — Full width so words never wrap letter-by-letter!
            if (secondCell != null && secondCell.text.isNotBlank()) {
                val h1 = headers.getOrNull(1)?.takeIf { it.isNotBlank() }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (h1 != null) {
                        Text(
                            text = h1.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    ClickableCellText(
                        cell = secondCell,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15,
                        onArticleLinkClick = onArticleLinkClick,
                        onRedlinkClick = onRedlinkClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Event / Ajang / Detail Metadata
            if (thirdCell != null && thirdCell.text.isNotBlank()) {
                val h2 = headers.getOrNull(2)?.takeIf { it.isNotBlank() }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        if (h2 != null) {
                            Text(
                                text = h2,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        ClickableCellText(
                            cell = thirdCell,
                            fontWeight = FontWeight.Medium,
                            onArticleLinkClick = onArticleLinkClick,
                            onRedlinkClick = onRedlinkClick,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Any extra trailing columns beyond 3
            if (extraCells.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    extraCells.forEachIndexed { extraIdx, extraCell ->
                        val headerName = headers.getOrNull(extraIdx + 3)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                                if (!headerName.isNullOrBlank()) {
                                    Text(
                                        text = headerName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                ClickableCellText(
                                    cell = extraCell,
                                    fontWeight = FontWeight.SemiBold,
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
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (yearCell != null && yearCell.text.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.widthIn(min = 72.dp, max = 106.dp)
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

            if (teamCell != null) {
                ClickableCellText(
                    cell = teamCell,
                    fontWeight = FontWeight.Bold,
                    onArticleLinkClick = onArticleLinkClick,
                    onRedlinkClick = onRedlinkClick,
                    modifier = Modifier.weight(1f)
                )
            }

            if (statCell != null && statCell.text.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.widthIn(max = 110.dp)
                ) {
                    Text(
                        text = statCell.text,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End,
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
    val teamCell = row.getOrNull(0)
    val competitionCell = row.getOrNull(1)
    val yearCell = row.getOrNull(2)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
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
                        modifier = Modifier
                            .widthIn(max = 140.dp)
                            .let { base ->
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
    modifier: Modifier = Modifier,
    fontSize: Int = 13,
    textAlign: TextAlign = TextAlign.Start,
    customColor: Color? = null
) {
    val target = cell.targetTitle
    val textColor = when {
        target != null && cell.isRedlink -> Color(0xFFB91C1C)
        target != null -> MaterialTheme.colorScheme.primary
        customColor != null -> customColor
        else -> MaterialTheme.colorScheme.onSurface
    }
    Text(
        text = cell.text,
        style = MaterialTheme.typography.bodySmall.copy(
            fontSize = fontSize.sp,
            fontWeight = fontWeight,
            lineHeight = (fontSize + 5).sp,
            textDecoration = TextDecoration.None
        ),
        textAlign = textAlign,
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
