package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.ui.theme.MippediaAmber
import com.example.ui.theme.MippediaBlueDark
import com.example.ui.theme.MippediaBluePrimary
import com.example.ui.theme.MippediaGreen
import com.example.ui.theme.MippediaTealAccent
import kotlinx.coroutines.delay

data class OnboardingStep(
    val title: String,
    val subtitle: String,
    val description: String,
    val stepsGuide: List<String>,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun OnboardingDialog(
    skipSplash: Boolean = false,
    onComplete: () -> Unit
) {
    var isSplashStage by remember(skipSplash) { mutableStateOf(!skipSplash) }
    var currentStepIndex by remember { mutableIntStateOf(0) }

    val logoScale = remember { Animatable(0.4f) }
    val logoAlpha = remember { Animatable(0f) }

    LaunchedEffect(skipSplash) {
        if (!skipSplash) {
            logoAlpha.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))
            logoScale.animateTo(1.08f, animationSpec = tween(600, easing = FastOutSlowInEasing))
            logoScale.animateTo(1.0f, animationSpec = tween(250))
            delay(900)
            logoAlpha.animateTo(0f, animationSpec = tween(400))
            isSplashStage = false
        }
    }

    val steps = listOf(
        OnboardingStep(
            title = "Selamat Datang di Mippedia",
            subtitle = "Panduan Pemakaian Aplikasi",
            description = "Mippedia adalah ensiklopedia daring bebas multibahasa yang tersinkronisasi langsung dengan situs id.mippedia.org, en.mippedia.org, dan commons.mippedia.org.",
            stepsGuide = listOf(
                "Jelajahi sorotan Artikel Bulanan, Gambar Bulanan, dan peristiwa Hari Ini Dalam Sejarah langsung di halaman utama.",
                "Ganti bahasa ensiklopedia (Indonesia 🇮🇩 ↔ English 🇬🇧) kapan saja melalui tombol pill di sudut kanan atas.",
                "Tekan ikon Acak (🔀) di sebelah bahasa untuk membaca artikel kejutan secara instan."
            ),
            icon = Icons.Outlined.AutoStories,
            color = MippediaBluePrimary
        ),
        OnboardingStep(
            title = "Cara Mencari Artikel & Berkas",
            subtitle = "Pencarian Cepat & Filter Pintar",
            description = "Temukan tokoh, sejarah, statistik olahraga, kategori, maupun berkas media Commons dalam hitungan detik.",
            stepsGuide = listOf(
                "Ketuk tab 'Cari' (🔍) pada bilah navigasi di bagian bawah layar.",
                "Ketik judul atau kata kunci pada kolom pencarian — hasil akan muncul otomatis secara waktu nyata.",
                "Gunakan filter 'Semua', 'Artikel', 'Berkas (Commons)', atau 'Kategori' untuk mempersempit hasil."
            ),
            icon = Icons.Outlined.ManageSearch,
            color = Color(0xFF0284C7)
        ),
        OnboardingStep(
            title = "Cara Masuk & Daftar Akun",
            subtitle = "Sinkron Dengan Akun Web Asli",
            description = "Anda bisa langsung membaca sebagai Akun Sementara (IP), atau membuat akun resmi agar kontribusi tercatat dengan nama pengguna Anda sendiri.",
            stepsGuide = listOf(
                "Buka tab 'Akun' (👤) di pojok kanan bawah bilah navigasi.",
                "Untuk pengguna baru: ketuk tombol 'Daftar Akun', isi nama pengguna, kata sandi, dan email opsional — langsung terdaftar tanpa CAPTCHA.",
                "Untuk pengguna lama: ketuk tombol 'Masuk', atau gunakan 'Lupa Kata Sandi?' di halaman login jika lupa sandi."
            ),
            icon = Icons.Outlined.PersonAddAlt,
            color = Color(0xFF6A1B9A)
        ),
        OnboardingStep(
            title = "Membaca, Tabel Khusus & Audio",
            subtitle = "Fitur Artikel Lengkap",
            description = "Nikmati halaman artikel dengan kontrol baca lengkap, pemutar suara pelafalan, dan tabel statistik yang tertata rapi.",
            stepsGuide = listOf(
                "Gunakan bilah alat artikel untuk membuka Daftar Isi (☰), Halaman Terkait (↗️), atau mengubah Ukuran & Jenis Huruf (Aa).",
                "Jika artikel memiliki statistik atlet atau karier, ketuk 'Tampilkan' pada kartu Tabel Khusus & Statistik untuk membukanya.",
                "Ketuk tombol speaker (🔊 simak) untuk memutar suara lewat pemutar mini, dan pakai tombol 'Ke Atas' saat menggulir jauh."
            ),
            icon = Icons.Outlined.GraphicEq,
            color = MippediaTealAccent
        ),
        OnboardingStep(
            title = "Mippedia Commons",
            subtitle = "Galeri Foto, Audio & Video",
            description = "Jelajahi repositori media terpusat serta cek halaman artikel mana saja yang memakai berkas tersebut.",
            stepsGuide = listOf(
                "Ketuk tab 'Commons' (🖼️) di bilah bawah untuk membuka galeri berkas multimedia terbaru.",
                "Ketuk gambar, foto infobox, atau berkas audio/video mana saja untuk melihat resolusi penuh, lisensi, dan pengunggahnya.",
                "Gulir ke bagian 'Penggunaan Berkas di Artikel' untuk melompat langsung ke artikel terkait."
            ),
            icon = Icons.Outlined.PhotoLibrary,
            color = MippediaGreen
        )
    )

    Dialog(
        onDismissRequest = onComplete,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(MippediaBlueDark, Color(0xFF071926))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSplashStage) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .scale(logoScale.value)
                        .alpha(logoAlpha.value)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.mippedia_logo),
                        contentDescription = "Logo Mippedia",
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                    )
                    Text(
                        text = "MIPPEDIA",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 4.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "Ensiklopedia Bebas Multibahasa",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            } else {
                AnimatedContent(
                    targetState = currentStepIndex,
                    transitionSpec = {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    },
                    label = "OnboardingSlide"
                ) { stepIdx ->
                    val step = steps[stepIdx]

                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .widthIn(max = 520.dp)
                            .heightIn(max = 660.dp)
                            .padding(vertical = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Top Bar: Step badge & Always-accessible Close button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = step.color.copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = "Langkah ${stepIdx + 1} dari ${steps.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = step.color,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                IconButton(
                                    onClick = onComplete,
                                    modifier = Modifier.testTag("tutorial_close_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Tutup Tutorial",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Scrollable tutorial body so every step fits comfortably on any screen
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f, fill = false)
                                    .verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = step.color.copy(alpha = 0.15f),
                                    modifier = Modifier.size(68.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = step.icon,
                                            contentDescription = null,
                                            tint = step.color,
                                            modifier = Modifier.size(34.dp)
                                        )
                                    }
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = step.title,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = step.subtitle,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = step.color,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Text(
                                    text = step.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 19.sp
                                )

                                // Numbered Actionable Instructions
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    step.stepsGuide.forEachIndexed { idx, tip ->
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                                verticalAlignment = Alignment.Top,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = step.color,
                                                    modifier = Modifier
                                                        .padding(top = 1.dp)
                                                        .size(20.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = "${idx + 1}",
                                                            color = Color.White,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = tip,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    lineHeight = 18.sp,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Step Dots Indicator
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                    steps.indices.forEach { index ->
                                        val isSelected = index == stepIdx
                                        Box(
                                            modifier = Modifier
                                                .height(8.dp)
                                                .width(if (isSelected) 24.dp else 8.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) step.color else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                                )
                                        )
                                    }
                                }
                            }

                            // Bottom Navigation Controls (Back + Next/Finish)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (stepIdx > 0) {
                                    OutlinedButton(
                                        onClick = { currentStepIndex-- },
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.height(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Sebelumnya",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else {
                                    TextButton(
                                        onClick = onComplete,
                                        modifier = Modifier.height(48.dp)
                                    ) {
                                        Text("Lewati")
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (stepIdx < steps.size - 1) {
                                            currentStepIndex++
                                        } else {
                                            onComplete()
                                        }
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("tutorial_next_button")
                                ) {
                                    Text(
                                        text = if (stepIdx < steps.size - 1) "Lanjutkan" else "Mulai Jelajahi Mippedia",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    if (stepIdx < steps.size - 1) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
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
}

@Composable
fun PostAuthTutorialDialog(
    username: String,
    onDismiss: () -> Unit
) {
    var stepIdx by remember { mutableIntStateOf(0) }

    val postAuthSteps = listOf(
        OnboardingStep(
            title = "Akun Berhasil Terhubung!",
            subtitle = "Selamat datang, $username",
            description = "Kini Anda memiliki akses penuh ke seluruh fitur kolaborasi dan personalisasi di Mippedia Android.",
            stepsGuide = listOf(
                "Nama pengguna resmi '$username' sekarang akan tercatat secara aman pada setiap suntingan artikel yang Anda terbitkan.",
                "Semua pengaturan huruf, tema, dan artikel tersimpan kini aktif di menu Akun Anda."
            ),
            icon = Icons.Outlined.VerifiedUser,
            color = MippediaGreen
        ),
        OnboardingStep(
            title = "Menyunting & Menyimpan Artikel",
            subtitle = "Fitur Baru Pada Halaman Artikel",
            description = "Dua tombol penting kini siap Anda gunakan setiap kali membuka artikel:",
            stepsGuide = listOf(
                "Ikon Pensil (✏️): Membuka editor wikitext lengkap untuk memperbaiki atau menambah isi artikel beserta ringkasan suntingan.",
                "Ikon Penanda (🔖): Muncul khusus untuk akun terdaftar agar Anda bisa menyimpan artikel favorit ke daftar 'Artikel Tersimpan'."
            ),
            icon = Icons.Outlined.EditNote,
            color = MippediaAmber
        ),
        OnboardingStep(
            title = "Profil Saya, Notifikasi & Pengaturan",
            subtitle = "Kelola Identitas & Tampilan Baca",
            description = "Tab Akun kini menampilkan panel khusus anggota terdaftar untuk memantau aktivitas Anda:",
            stepsGuide = listOf(
                "Ketuk 'Profil Saya' untuk melihat halaman pengguna resmi, hak grup, serta daftar riwayat kontribusi Anda.",
                "Ketuk ikon Lonceng (🔔) untuk memeriksa notifikasi dan ketuk 'Pengaturan' untuk mengatur jenis huruf, ukuran tulisan, mode gelap, hemat kuota, serta bersihkan cache."
            ),
            icon = Icons.Outlined.Tune,
            color = MippediaBluePrimary
        )
    )

    val step = postAuthSteps[stepIdx]

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(18.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = step.color.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = "Panduan Akun ${stepIdx + 1}/${postAuthSteps.size}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = step.color,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("post_auth_tutorial_close_button")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup Panduan")
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = step.color.copy(alpha = 0.14f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = step.icon,
                                    contentDescription = null,
                                    tint = step.color,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Text(
                            text = step.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = step.subtitle,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = step.color,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = step.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 19.sp
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        step.stepsGuide.forEachIndexed { i, item ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = step.color,
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${i + 1}",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Text(
                                        text = item,
                                        style = MaterialTheme.typography.bodySmall,
                                        lineHeight = 18.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (stepIdx > 0) {
                            OutlinedButton(
                                onClick = { stepIdx-- },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(46.dp)
                            ) {
                                Text("Kembali")
                            }
                        } else {
                            TextButton(
                                onClick = onDismiss,
                                modifier = Modifier.height(46.dp)
                            ) {
                                Text("Tutup")
                            }
                        }

                        Button(
                            onClick = {
                                if (stepIdx < postAuthSteps.size - 1) stepIdx++ else onDismiss()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Text(
                                text = if (stepIdx < postAuthSteps.size - 1) "Selanjutnya" else "Mengerti, Mulai Sekarang!",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
