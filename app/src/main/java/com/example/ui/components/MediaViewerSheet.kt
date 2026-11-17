package com.example.ui.components

import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.model.WikiMediaItem
import com.example.model.WikiSearchResult
import com.example.ui.theme.MippediaTealAccent
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaViewerScreen(
    media: WikiMediaItem,
    mediaUsage: List<WikiSearchResult> = emptyList(),
    isUsageLoading: Boolean = false,
    onArticleClick: (String, String?) -> Unit = { _, _ -> },
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showUsagePanel by remember(media.title) { mutableStateOf(true) }

    // Smooth Android back button handling - never closes prematurely by accidental swipe
    BackHandler(onBack = onClose)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = when {
                                media.isAudio -> "Pemutar Audio Commons"
                                media.isVideo -> "Pemutar Video Commons"
                                else -> "Penampil Berkas Media"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = media.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, media.url)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Bagikan Tautan Berkas"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Bagikan Tautan")
                    }
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(media.descriptionUrl.ifEmpty { media.url }))
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = "Buka di Browser")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Repositori Tag
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MippediaTealAccent.copy(alpha = 0.15f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = when {
                            media.isAudio -> Icons.Outlined.AudioFile
                            media.isVideo -> Icons.Outlined.Videocam
                            else -> Icons.Outlined.Image
                        },
                        contentDescription = "Commons Media",
                        tint = MippediaTealAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Repositori Bersama: Mippedia Commons",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MippediaTealAccent
                    )
                }
            }

            // Media Stage: Audio Player, Video Player, or High-Res Image Viewer
            when {
                media.isAudio -> {
                    CommonsAudioPlayerStage(media = media)
                }
                media.isVideo -> {
                    CommonsVideoPlayerStage(media = media)
                }
                else -> {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Black),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 260.dp, max = 420.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            AsyncImage(
                                model = media.url,
                                contentDescription = media.title,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // Media Title
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = media.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Action Buttons: Unduh Berkas & Artikel Pengguna aligned side-by-side!
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            try {
                                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                val cleanName = media.title
                                    .replace("File:", "", ignoreCase = true)
                                    .replace("Berkas:", "", ignoreCase = true)
                                    .replace("/", "_")
                                    .replace(" ", "_")
                                    .trim()
                                val urlPath = media.url.substringBefore('?').lowercase()
                                val extension = when {
                                    urlPath.endsWith(".svg") -> ".svg"
                                    urlPath.endsWith(".png") -> ".png"
                                    urlPath.endsWith(".webp") -> ".webp"
                                    urlPath.endsWith(".gif") -> ".gif"
                                    urlPath.endsWith(".ogg") -> ".ogg"
                                    urlPath.endsWith(".oga") -> ".oga"
                                    urlPath.endsWith(".mp3") -> ".mp3"
                                    urlPath.endsWith(".wav") -> ".wav"
                                    urlPath.endsWith(".mp4") -> ".mp4"
                                    urlPath.endsWith(".webm") -> ".webm"
                                    urlPath.endsWith(".ogv") -> ".ogv"
                                    media.isAudio -> ".ogg"
                                    media.isVideo -> ".mp4"
                                    else -> ".jpg"
                                }
                                val targetDir = when {
                                    media.isAudio -> Environment.DIRECTORY_MUSIC
                                    media.isVideo -> Environment.DIRECTORY_MOVIES
                                    else -> Environment.DIRECTORY_PICTURES
                                }
                                val fileName = if (cleanName.contains(".")) cleanName else "$cleanName$extension"
                                val request = DownloadManager.Request(Uri.parse(media.url)).apply {
                                    setTitle(fileName)
                                    setDescription("Mengunduh berkas media dari Mippedia Commons...")
                                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                    setDestinationInExternalPublicDir(targetDir, "Mippedia/$fileName")
                                }
                                downloadManager.enqueue(request)
                                Toast.makeText(context, "Mulai mengunduh: $fileName", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Gagal mengunduh: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Unduh Berkas",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Unduh Berkas",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    FilledTonalButton(
                        onClick = { showUsagePanel = !showUsagePanel },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Article,
                            contentDescription = "Penggunaan di Artikel",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val countLabel = if (isUsageLoading) "..." else "${mediaUsage.size}"
                        Text(
                            text = "Digunakan ($countLabel)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("URL Berkas Mippedia", media.url)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Tautan berkas disalin ke papan klip", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Salin Tautan", fontSize = 12.sp)
                    }

                    FilledTonalButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(media.descriptionUrl.ifEmpty { media.url }))
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buka Web", fontSize = 12.sp)
                    }
                }
            }

            // Synchronized Real-time Article Usage List ("Artikel yang Menggunakan Berkas Ini")
            AnimatedVisibility(visible = showUsagePanel) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Article,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Artikel yang Menggunakan Berkas Ini",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (isUsageLoading) {
                                SmallSpinningLoader(modifier = Modifier.size(16.dp))
                            }
                        }

                        if (isUsageLoading && mediaUsage.isEmpty()) {
                            Text(
                                text = "Menyinkronkan daftar artikel dari Mippedia...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else if (mediaUsage.isEmpty()) {
                            Text(
                                text = "Belum ada artikel yang menautkan atau menyematkan berkas ini.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            mediaUsage.forEach { usage ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 1.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onArticleClick(usage.title, usage.shortDescription)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = usage.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (usage.snippet.isNotBlank()) {
                                                Text(
                                                    text = usage.snippet,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = Icons.Default.ArrowForwardIos,
                                            contentDescription = "Buka Artikel",
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Detailed Metadata Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Informasi & Rincian Berkas",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    MetadataRow(label = "Nama Berkas", value = media.title)
                    MetadataRow(label = "Repositori", value = "Mippedia Commons")
                    if (media.width > 0 && media.height > 0) {
                        MetadataRow(label = "Dimensi", value = "${media.width} × ${media.height} piksel")
                    }
                    if (media.sizeBytes > 0) {
                        val sizeKb = media.sizeBytes / 1024
                        val sizeText = if (sizeKb > 1024) "${String.format("%.1f", sizeKb / 1024f)} MB" else "$sizeKb KB"
                        MetadataRow(label = "Ukuran Data", value = sizeText)
                    }
                    if (media.mime.isNotBlank()) {
                        MetadataRow(label = "Format MIME", value = media.mime)
                    }
                    if (media.user.isNotBlank()) {
                        MetadataRow(label = "Pengunggah Berkas", value = media.user)
                    }
                    if (media.timestamp.isNotBlank()) {
                        MetadataRow(label = "Waktu Unggah", value = media.timestamp.take(10))
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun CommonsAudioPlayerStage(media: WikiMediaItem) {
    var isPrepared by remember(media.url) { mutableStateOf(false) }
    var isPlaying by remember(media.url) { mutableStateOf(false) }
    var isError by remember(media.url) { mutableStateOf(false) }
    var currentPosMs by remember(media.url) { mutableIntStateOf(0) }
    var durationMs by remember(media.url) { mutableIntStateOf(0) }

    val mediaPlayer = remember(media.url) {
        MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setOnPreparedListener { mp ->
                isPrepared = true
                durationMs = mp.duration.coerceAtLeast(0)
            }
            setOnCompletionListener {
                isPlaying = false
                currentPosMs = 0
            }
            setOnErrorListener { _, _, _ ->
                isError = true
                isPlaying = false
                true
            }
            try {
                setDataSource(media.url)
                prepareAsync()
            } catch (_: Exception) {
                isError = true
            }
        }
    }

    DisposableEffect(mediaPlayer) {
        onDispose {
            try {
                if (mediaPlayer.isPlaying) mediaPlayer.stop()
                mediaPlayer.release()
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(isPlaying, isPrepared) {
        while (isPlaying && isPrepared) {
            try {
                currentPosMs = mediaPlayer.currentPosition
            } catch (_: Exception) {}
            delay(250)
        }
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF0F172A), Color(0xFF0F766E))
                    )
                )
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.14f),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Text(
                text = media.title.removePrefix("Berkas:").removePrefix("File:"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (isError) {
                Text(
                    text = "Format audio tidak dapat diputar langsung di perangkat ini. Gunakan tombol Unduh atau Buka Web.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFCA5A5)
                )
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    val safeMax = durationMs.coerceAtLeast(1).toFloat()
                    Slider(
                        value = currentPosMs.toFloat().coerceIn(0f, safeMax),
                        onValueChange = { pos ->
                            if (isPrepared) {
                                currentPosMs = pos.toInt()
                                mediaPlayer.seekTo(currentPosMs)
                            }
                        },
                        valueRange = 0f..safeMax,
                        enabled = isPrepared,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color(0xFF5EEAD4),
                            inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatDurationMs(currentPosMs),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            text = if (isPrepared) formatDurationMs(durationMs) else "Memuat audio...",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                FilledIconButton(
                    onClick = {
                        if (isPrepared) {
                            if (isPlaying) {
                                mediaPlayer.pause()
                                isPlaying = false
                            } else {
                                mediaPlayer.start()
                                isPlaying = true
                            }
                        }
                    },
                    enabled = isPrepared,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF0F766E)
                    ),
                    modifier = Modifier.size(56.dp)
                ) {
                    if (!isPrepared) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF0F766E)
                        )
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Jeda Audio" else "Putar Audio",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommonsVideoPlayerStage(media: WikiMediaItem) {
    var isBuffering by remember(media.url) { mutableStateOf(true) }
    var hasError by remember(media.url) { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (hasError) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Videocam,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(38.dp)
                    )
                    Text(
                        text = "Video tidak dapat diputar langsung. Anda dapat mengunduh atau membukanya di web.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            } else {
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            val controller = MediaController(ctx)
                            controller.setAnchorView(this)
                            setMediaController(controller)
                            setVideoURI(Uri.parse(media.url))
                            setOnPreparedListener { mp ->
                                isBuffering = false
                                mp.isLooping = false
                                start()
                            }
                            setOnErrorListener { _, _, _ ->
                                isBuffering = false
                                hasError = true
                                true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                if (isBuffering) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}

private fun formatDurationMs(ms: Int): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    val mins = totalSec / 60
    val secs = totalSec % 60
    return "%02d:%02d".format(mins, secs)
}

// Backward compatibility alias
@Composable
fun MediaViewerSheet(
    media: WikiMediaItem,
    onClose: () -> Unit
) {
    MediaViewerScreen(
        media = media,
        onClose = onClose
    )
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.6f)
        )
    }
}
