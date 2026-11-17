package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.InfoDocumentType
import com.example.ui.theme.MippediaAmber
import com.example.ui.theme.MippediaBlueDark
import com.example.ui.theme.MippediaBluePrimary
import com.example.ui.theme.MippediaGreen
import com.example.ui.theme.MippediaTealAccent

private data class DocumentSection(
    val heading: String,
    val icon: ImageVector,
    val accent: Color,
    val paragraphs: List<String>,
    val bullets: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoDocumentScreen(
    documentType: InfoDocumentType,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val (pageTitle, badgeText, heroTitle, heroSubtitle, heroIcon, gradientColors, sections, footerUrl, footerLabel) = when (documentType) {
        InfoDocumentType.ABOUT -> InfoDocBundle(
            pageTitle = "Tentang Mippedia",
            badgeText = "PROFIL & EKOSISTEM RESMI",
            heroTitle = "Ensiklopedia Daring Bebas & Terbuka Multibahasa",
            heroSubtitle = "Dikelola dan dikembangkan secara independen oleh Mippedia Community untuk menghadirkan pengetahuan tepercaya, cepat, dan mudah diakses bagi semua orang.",
            heroIcon = Icons.Outlined.AutoStories,
            gradientColors = listOf(MippediaBlueDark, MippediaBluePrimary),
            sections = listOf(
                DocumentSection(
                    heading = "Visi & Misi Mippedia",
                    icon = Icons.Outlined.Public,
                    accent = MippediaBluePrimary,
                    paragraphs = listOf(
                        "Mippedia berdiri di atas keyakinan bahwa akses terhadap pengetahuan yang akurat, netral, dan terverifikasi adalah hak dasar setiap individu. Seluruh artikel ditulis secara kolaboratif oleh kontributor sukarelawan dari berbagai latar belakang keilmuan.",
                        "Melalui aplikasi Android resmi ini, seluruh struktur ensiklopedia MediaWiki dihadirkan dalam pengalaman seluler yang bersih, responsif, ringan, serta terhubung secara waktu nyata (real-time) dengan peladen utama Mippedia."
                    )
                ),
                DocumentSection(
                    heading = "Ekosistem Tiga Pilar Mippedia",
                    icon = Icons.Outlined.Hub,
                    accent = MippediaTealAccent,
                    paragraphs = listOf(
                        "Seluruh jaringan pengetahuan Mippedia tersusun atas tiga layanan inti yang saling terintegrasi secara sinkron:"
                    ),
                    bullets = listOf(
                        "Mippedia Indonesia (id.mippedia.org): Edisi ensiklopedia berbahasa Indonesia yang memuat biografi, olahraga, sains, sejarah, teknologi, budaya, dan topik lokal Nusantara.",
                        "Mippedia English (en.mippedia.org): Edisi ensiklopedia berbahasa Inggris untuk jangkauan kolaborasi dan rujukan global.",
                        "Mippedia Commons (commons.mippedia.org): Repositori terpusat untuk seluruh berkas foto, ilustrasi vektor, audio pelafalan, dan video berlisensi bebas yang digunakan lintas bahasa."
                    )
                ),
                DocumentSection(
                    heading = "Pilar Prinsip Penyuntingan",
                    icon = Icons.Outlined.Verified,
                    accent = MippediaGreen,
                    paragraphs = listOf(
                        "Setiap kontribusi artikel dan perubahan di Mippedia berpedoman pada standar kualitas ensiklopedis yang ketat:"
                    ),
                    bullets = listOf(
                        "Sudut Pandang Netral (Neutral Point of View): Artikel disajikan secara berimbang berdasarkan fakta dan tidak memihak.",
                        "Sumber Tepercaya & Pemastian: Klaim dan angka statistik merujuk kepada sumber sekunder maupun resmi yang dapat diperiksa publik.",
                        "Transparansi Riwayat Revisi: Setiap suntingan tercatat secara permanen bersama nama kontributor, stempel waktu, dan ringkasan perubahan.",
                        "Perlindungan Halaman Cerdas: Artikel sensitif atau halaman indeks sistem dilindungi sesuai hak pengguna untuk menjaga stabilitas informasi."
                    )
                ),
                DocumentSection(
                    heading = "Lisensi Bebas & Akses Terbuka",
                    icon = Icons.Outlined.Copyright,
                    accent = Color(0xFF6366F1),
                    paragraphs = listOf(
                        "Seluruh teks artikel di Mippedia diterbitkan di bawah lisensi Creative Commons Attribution-ShareAlike 4.0 International (CC BY-SA 4.0), sedangkan berkas media di Commons mengikuti lisensi terbuka yang tertera di halaman deskripsi berkas masing-masing.",
                        "Anda bebas membaca, menyalin, mengutip, maupun menyunting kembali konten Mippedia selama mencantumkan atribusi sumber yang semestinya dan meneruskan lisensi yang serupa."
                    )
                ),
                DocumentSection(
                    heading = "Tentang Aplikasi Android v1.7.2",
                    icon = Icons.Outlined.PhoneAndroid,
                    accent = MippediaBluePrimary,
                    paragraphs = listOf(
                        "Versi seluler ini menghadirkan pengurai infobox atlet & statistik musim, pemutar audio & video inline, pencarian multibahasa cepat, penyimpanan artikel luring (bookmark), editor wikitext terintegrasi, serta sinkronisasi akun langsung melalui API MediaWiki resmi."
                    )
                )
            ),
            footerUrl = "https://id.mippedia.org/wiki/Mippedia:Tentang",
            footerLabel = "Buka Halaman Resmi di Web"
        )

        InfoDocumentType.PRIVACY -> InfoDocBundle(
            pageTitle = "Kebijakan Privasi",
            badgeText = "PERLINDUNGAN DATA PENGGUNA",
            heroTitle = "Komitmen Privasi & Transparansi Data",
            heroSubtitle = "Kami menjaga hak privasi pembaca dan kontributor dengan pengumpulan data seminimal mungkin serta keamanan transmisi terenkripsi penuh.",
            heroIcon = Icons.Outlined.Security,
            gradientColors = listOf(Color(0xFF064E3B), Color(0xFF0D9488)),
            sections = listOf(
                DocumentSection(
                    heading = "1. Kebebasan Membaca Tanpa Pelacakan",
                    icon = Icons.Outlined.VisibilityOff,
                    accent = MippediaTealAccent,
                    paragraphs = listOf(
                        "Anda dapat membaca seluruh artikel, mencari informasi, memutar berkas media Commons, dan menjelajahi kategori di Mippedia tanpa perlu mendaftar akun atau menyerahkan data identitas pribadi.",
                        "Aplikasi Mippedia Android tidak menanamkan SDK pelacak iklan pihak ketiga maupun menjual riwayat bacaan Anda kepada pihak mana pun."
                    )
                ),
                DocumentSection(
                    heading = "2. Informasi yang Dikumpulkan Saat Masuk & Mendaftar",
                    icon = Icons.Outlined.Badge,
                    accent = MippediaBluePrimary,
                    paragraphs = listOf(
                        "Apabila Anda memilih membuat atau masuk ke akun terdaftar Mippedia, data dikirimkan langsung secara terenkripsi HTTPS ke peladen MediaWiki resmi:"
                    ),
                    bullets = listOf(
                        "Nama pengguna publik yang Anda pilih sendiri untuk atribusi suntingan.",
                        "Kata sandi yang diproses langsung oleh mekanisme otentikasi MediaWiki dan tidak pernah disimpan dalam bentuk teks biasa di aplikasi.",
                        "Alamat surel (opsional saat registrasi) yang hanya digunakan peladen untuk pemulihan kata sandi dan notifikasi akun bila diaktifkan."
                    )
                ),
                DocumentSection(
                    heading = "3. Penyuntingan Anonim / Akun Sementara",
                    icon = Icons.Outlined.History,
                    accent = MippediaAmber,
                    paragraphs = listOf(
                        "Jika Anda melakukan penyuntingan artikel tanpa masuk ke akun terdaftar, sistem MediaWiki secara otomatis mencatat alamat IP koneksi Anda atau pengenal Akun Sementara pada log riwayat revisi halaman yang bersifat publik.",
                        "Pencatatan ini bertujuan semata untuk mencegah penyalahgunaan, spam, serta menjaga akuntabilitas kolaborasi ensiklopedia terbuka. Aplikasi selalu menampilkan peringatan konfirmasi sebelum Anda menyunting sebagai anonim."
                    )
                ),
                DocumentSection(
                    heading = "4. Penyimpanan Lokal & Kuki Sesi di Perangkat",
                    icon = Icons.Outlined.Storage,
                    accent = Color(0xFF6366F1),
                    paragraphs = listOf(
                        "Aplikasi menyimpan beberapa data secara lokal di dalam memori internal perangkat Android Anda semata untuk kenyamanan dan kecepatan:"
                    ),
                    bullets = listOf(
                        "Kuki Sesi MediaWiki (Persisted CookieJar): Menjaga status login Anda tetap aktif dengan aman di perangkat.",
                        "Preferensi Tampilan & Mode Hemat Kuota: Ukuran teks, jenis huruf, tema terang/gelap, dan status tutorial.",
                        "Artikel Tersimpan (Bookmark) & Riwayat Pencarian: Disimpan lokal dan dapat dihapus kapan saja melalui menu Riwayat atau Pengaturan.",
                        "Cache Halaman & Gambar HTTP: Dapat dibersihkan secara menyeluruh dengan satu ketukan tombol Bersihkan Cache di menu Pengaturan."
                    )
                ),
                DocumentSection(
                    heading = "5. Keamanan Transmisi & Hak Kontrol Anda",
                    icon = Icons.Outlined.Lock,
                    accent = MippediaGreen,
                    paragraphs = listOf(
                        "Seluruh komunikasi antara aplikasi dan peladen id.mippedia.org, en.mippedia.org, serta commons.mippedia.org menggunakan koneksi HTTPS/TLS terenkripsi dan menonaktifkan eksekusi skrip berisiko pada penampil artikel.",
                        "Anda memegang kendali penuh untuk keluar dari akun (logout), menghapus riwayat pencarian, menghapus artikel tersimpan, atau membersihkan cache kapan saja."
                    )
                )
            ),
            footerUrl = "https://id.mippedia.org/wiki/Mippedia:Kebijakan_privasi",
            footerLabel = "Lihat Kebijakan Privasi Lengkap di Web"
        )

        InfoDocumentType.DISCLAIMER -> InfoDocBundle(
            pageTitle = "Penyangkalan Umum",
            badgeText = "KETENTUAN & BATASAN TANGGUNG JAWAB",
            heroTitle = "Penyangkalan Umum (General Disclaimer)",
            heroSubtitle = "Pemahaman penting mengenai sifat kolaboratif ensiklopedia terbuka, akurasi konten waktu nyata, serta batasan tanggung jawab hukum dan profesional.",
            heroIcon = Icons.Outlined.Gavel,
            gradientColors = listOf(Color(0xFF312E81), Color(0xFF4338CA)),
            sections = listOf(
                DocumentSection(
                    heading = "1. Sifat Kolaboratif & Perubahan Konten Terbuka",
                    icon = Icons.Outlined.Groups,
                    accent = Color(0xFF4F46E5),
                    paragraphs = listOf(
                        "Mippedia adalah ensiklopedia daring bebas yang artikelnya ditulis dan dimutakhirkan secara kolaboratif oleh para sukarelawan komunitas. Siapa pun dengan akses internet berpotensi menyunting halaman yang tidak dilindungi.",
                        "Akibat sifat terbuka tersebut, meskipun komunitas dan pengurus aktif melakukan peninjauan berkala, suatu artikel mungkin saja sedang dalam proses perbaikan, belum lengkap, atau sempat mengalami suntingan yang keliru sebelum ditinjau kembali."
                    )
                ),
                DocumentSection(
                    heading = "2. Tidak Ada Jaminan Mutlak atas Akurasi",
                    icon = Icons.Outlined.FactCheck,
                    accent = MippediaBluePrimary,
                    paragraphs = listOf(
                        "Baik Mippedia Community, para kontributor, maupun pengembang aplikasi Android Mippedia tidak memberikan jaminan tersurat maupun tersirat atas ketepatan mutlak, kelengkapan, keterbaruan, atau keandalan setiap informasi maupun angka statistik yang dimuat dalam artikel.",
                        "Pembaca sangat disarankan untuk selalu memeriksa rujukan sumber primer apabila memerlukan kepastian data untuk keperluan akademik, jurnalistik, maupun riset resmi."
                    )
                ),
                DocumentSection(
                    heading = "3. Bukan Pengganti Nasihat Profesional (Medis, Hukum, Keuangan)",
                    icon = Icons.Outlined.WarningAmber,
                    accent = MippediaAmber,
                    paragraphs = listOf(
                        "Seluruh informasi yang tersedia di Mippedia disediakan semata-mata untuk tujuan pengetahuan umum dan edukasi. Konten artikel TIDAK DAPAT dijadikan sebagai nasihat medis, diagnosis kesehatan, konsultasi hukum, saran investasi, maupun panduan profesional berlisensi.",
                        "Selalu konsultasikan keputusan krusial Anda dengan dokter, pengacara, konsultan keuangan, atau tenaga ahli profesional yang berkualifikasi resmi."
                    )
                ),
                DocumentSection(
                    heading = "4. Konten Berkas Commons & Tautan Eksternal",
                    icon = Icons.Outlined.Link,
                    accent = MippediaTealAccent,
                    paragraphs = listOf(
                        "Berkas gambar, audio, dan video pada Mippedia Commons serta tautan pranala luar menuju situs web pihak ketiga ditampilkan sebagai pelengkap referensi. Mippedia tidak bertanggung jawab atas ketersediaan, kebijakan, maupun perubahan isi situs eksternal di luar domain mippedia.org."
                    )
                ),
                DocumentSection(
                    heading = "5. Tanggung Jawab Pribadi Pembaca & Kontributor",
                    icon = Icons.Outlined.VerifiedUser,
                    accent = MippediaGreen,
                    paragraphs = listOf(
                        "Segala bentuk penafsiran, pengambilan keputusan, atau penggunaan informasi yang bersumber dari Mippedia merupakan tanggung jawab pribadi sepenuhnya dari pembaca. Setiap kontributor bertanggung jawab penuh atas materi dan kepatuhan hak cipta dari suntingan yang diterbitkannya."
                    )
                )
            ),
            footerUrl = "https://id.mippedia.org/wiki/Mippedia:Penyangkalan_umum",
            footerLabel = "Baca Ketentuan Resmi di Web"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = pageTitle,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("info_doc_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Banner Card
            Card(
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(gradientColors))
                        .padding(22.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.18f),
                                modifier = Modifier.size(50.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = heroIcon,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.16f)
                            ) {
                                Text(
                                    text = badgeText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.8.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = heroTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            lineHeight = 28.sp
                        )

                        Text(
                            text = heroSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.88f),
                            lineHeight = 21.sp
                        )
                    }
                }
            }

            // Detailed Sections
            sections.forEach { sec ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = sec.accent.copy(alpha = 0.14f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = sec.icon,
                                        contentDescription = null,
                                        tint = sec.accent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Text(
                                text = sec.heading,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        sec.paragraphs.forEach { para ->
                            Text(
                                text = para,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 22.sp
                            )
                        }

                        if (sec.bullets.isNotEmpty()) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                sec.bullets.forEach { bullet ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(top = 7.dp)
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(sec.accent)
                                            )
                                            Text(
                                                text = bullet,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                lineHeight = 19.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Footer official link & back CTA
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Dokumen ini disusun oleh Mippedia Community untuk keterbukaan informasi pada aplikasi Mippedia Android.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(footerUrl)))
                                } catch (_: Exception) {}
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(footerLabel, maxLines = 1)
                        }
                        Button(
                            onClick = onBack,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("info_doc_close_button")
                        ) {
                            Text("Tutup")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private data class InfoDocBundle(
    val pageTitle: String,
    val badgeText: String,
    val heroTitle: String,
    val heroSubtitle: String,
    val heroIcon: ImageVector,
    val gradientColors: List<Color>,
    val sections: List<DocumentSection>,
    val footerUrl: String,
    val footerLabel: String
)
