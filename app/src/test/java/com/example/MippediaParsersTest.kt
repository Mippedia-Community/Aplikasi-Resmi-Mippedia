package com.example

import com.example.model.WikiMediaItem
import com.example.network.MippediaHtmlParsers
import org.junit.Assert.*
import org.junit.Test

class MippediaParsersTest {

    @Test
    fun `parseMonthlyFeaturesFromMainPage extracts live Nailoong and Aziz Calim without fallbacks`() {
        val sampleMainPageHtml = """
            <div class="mp-header">ARTIKEL BULANAN</div>
            <div>
              <p><a href="/wiki/Berkas:Patung_Nailoong.jpg"><img src="//commons.mippedia.org/images/thumb/1/12/Patung_Nailoong.jpg/200px-Patung_Nailoong.jpg" srcset="//commons.mippedia.org/images/thumb/1/12/Patung_Nailoong.jpg/300px-Patung_Nailoong.jpg 1.5x"/></a></p>
              <p><b><a href="/wiki/Nailoong" title="Nailoong">Nailoong</a></b> adalah karakter dinosaurus kuning menggemaskan yang sangat populer di media sosial. (<a href="/wiki/Nailoong">Selengkapnya...</a>)</p>
              <p><a href="/wiki/Mippedia:Artikel_bulanan">Artikel sebelumnya</a></p>
            </div>
            <div class="mp-header">HARI INI DALAM SEJARAH</div>
            <div><p>Hari ini</p></div>
            <div class="mp-header">GAMBAR BULANAN</div>
            <div>
              <figure>
                <a href="/wiki/Berkas:Aziz_Calim.jpg" class="mw-file-description" resource="/wiki/Berkas:Aziz_Calim.jpg">
                  <img src="//commons.mippedia.org/images/thumb/a/ab/Aziz_Calim.jpg/300px-Aziz_Calim.jpg" />
                </a>
                <figcaption>Petarung MMA Indonesia Aziz Calim saat bertanding di One Pride MMA.</figcaption>
              </figure>
              <small>Oleh : Mippedia Commons • Lisensi : CC BY-SA 4.0</small>
            </div>
            <div class="mp-header">STATISTIK</div>
        """.trimIndent()

        val (article, image) = MippediaHtmlParsers.parseMonthlyFeaturesFromMainPage(sampleMainPageHtml)

        assertNotNull(article)
        assertEquals("Nailoong", article?.targetArticleTitle)
        assertEquals("Nailoong", article?.title)
        assertTrue(article?.summary?.contains("karakter dinosaurus kuning") == true)
        assertEquals(
            "https://commons.mippedia.org/images/thumb/1/12/Patung_Nailoong.jpg/300px-Patung_Nailoong.jpg",
            article?.imageUrl
        )

        assertNotNull(image)
        assertEquals("Berkas:Aziz Calim.jpg", image?.targetArticleTitle)
        assertEquals("Aziz Calim", image?.title)
        assertEquals("Petarung MMA Indonesia Aziz Calim saat bertanding di One Pride MMA.", image?.summary)
        assertEquals("Oleh: Mippedia Commons • Lisensi: CC BY-SA 4.0", image?.attribution)
        assertEquals("https://commons.mippedia.org/images/thumb/a/ab/Aziz_Calim.jpg/300px-Aziz_Calim.jpg", image?.imageUrl)

        // Ensure empty/unmatched HTML returns nulls rather than planted defaults
        val (emptyArt, emptyImg) = MippediaHtmlParsers.parseMonthlyFeaturesFromMainPage("<html><body>Empty</body></html>")
        assertNull(emptyArt)
        assertNull(emptyImg)
    }

    @Test
    fun `parseOnThisDayFromMainPage parses categories, years, clickable links, and archive nav`() {
        val sampleHtml = """
            <div class="mp-header">HARI INI DALAM SEJARAH</div>
            <div>
              <div style="font-weight:bold; color:#2d6a4f;">⚽ Olahraga &amp; Rekor Dunia</div>
              <div>
                <ul>
                  <li><b>2022</b> – <a href="/wiki/Argentina" title="Argentina">Tim nasional Argentina</a> memenangkan <a href="/wiki/Piala_Dunia_FIFA_2022">Piala Dunia FIFA</a>.</li>
                  <li><b>1998</b> – Rekor maraton baru dicatat.</li>
                </ul>
              </div>
              <div><a href="/wiki/Mippedia:Hari_ini_dalam_sejarah/16_November">Kemarin</a> • <a href="/wiki/Mippedia:Hari_ini_dalam_sejarah/18_November">Besok</a></div>
              <div style="font-weight:bold; color:#2d6a4f;">🔬 Sains &amp; Teknologi</div>
              <div>
                <ul>
                  <li><b>1969</b> – Peluncuran <a href="/wiki/Apollo_12">Apollo 12</a> menuju Bulan.</li>
                </ul>
              </div>
            </div>
            <div class="mp-header">GAMBAR BULANAN</div>
        """.trimIndent()

        val otd = MippediaHtmlParsers.parseOnThisDayFromMainPage(sampleHtml)
        assertNotNull(otd)
        assertEquals(2, otd!!.categories.size)

        val sports = otd.categories[0]
        assertEquals("⚽ Olahraga & Rekor Dunia", sports.categoryTitle)
        assertEquals(2, sports.events.size)
        assertEquals("2022", sports.events[0].year)
        assertTrue(sports.events[0].text.contains("Tim nasional Argentina memenangkan Piala Dunia FIFA."))
        assertEquals(2, sports.events[0].links.size)
        assertEquals("Argentina", sports.events[0].links[0].targetTitle)
        assertEquals("Mippedia:Hari ini dalam sejarah/16 November", sports.yesterdayTitle)
        assertEquals("Mippedia:Hari ini dalam sejarah/18 November", sports.tomorrowTitle)

        val sci = otd.categories[1]
        assertEquals("🔬 Sains & Teknologi", sci.categoryTitle)
        assertEquals("1969", sci.events[0].year)
        assertEquals("Apollo 12", sci.events[0].links[0].targetTitle)
    }

    @Test
    fun `extractInfobox parses nested season stats, collapsible cards, career tables, and redlinks`() {
        val sampleAthleteHtml = """
            <table class="infobox">
              <tbody>
                <tr><th colspan="2" class="infobox-above">Cristiano Ronaldo</th></tr>
                <tr><td colspan="2"><img src="//commons.mippedia.org/images/thumb/c/c1/Ronaldo.jpg/220px-Ronaldo.jpg"/></td></tr>
                <tr><th>Nama lengkap</th><td>Cristiano Ronaldo dos Santos Aveiro</td></tr>
                <tr><th>Posisi bermain</th><td>Penyerang</td></tr>
                <tr>
                  <td colspan="2">
                    <div style="border: 1px solid #cbd5e1; border-radius: 8px; padding: 8px;">
                      <div style="font-weight: bold;">STATISTIK MUSIM 2024–2025</div>
                      <div style="display: flex;">
                        <div><div>TAMPIL</div><span>35</span></div>
                        <div><div>GOL</div><span>28 ⚽</span></div>
                        <div><div>ASSIST</div><span>6 🅰️</span></div>
                        <div><div>RATA2 RATING</div><span>7.9</span></div>
                      </div>
                      <div style="display:flex; justify-content: space-between;"><span>Kompetisi</span><span>Liga Pro Saudi</span></div>
                    </div>
                    <div class="mw-collapsible mw-collapsed">
                      <div class="mw-collapsible-toggle">Selengkapnya</div>
                      <div class="mw-collapsible-content">
                        <div style="border: 1px solid #cbd5e1; border-radius: 8px; padding: 8px;">
                          <div style="font-weight: bold;">STATISTIK MUSIM 2023–2024</div>
                          <div style="display: flex;">
                            <div><div>TAMPIL</div><span>45</span></div>
                            <div><div>GOL</div><span>44 ⚽</span></div>
                          </div>
                          <div style="display:flex; justify-content: space-between;"><span>Klub</span><span>Al-Nassr</span></div>
                        </div>
                      </div>
                    </div>
                  </td>
                </tr>
                <tr><th colspan="2" class="infobox-header">Karier senior</th></tr>
                <tr>
                  <td colspan="2">
                    <table>
                      <tr style="font-weight:bold;"><th>Tahun</th><th>Tim</th><th>Tampil (G, A)</th></tr>
                      <tr><td>2021–2022</td><td><a href="/wiki/Manchester_United_F.C.">Manchester United</a></td><td>40 (19, 3 a)</td></tr>
                      <tr><td>2023–</td><td><a href="/w/index.php?title=Al-Nassr_FC&amp;action=edit&amp;redlink=1">Al-Nassr</a></td><td>65 (58, 15 a)</td></tr>
                    </table>
                  </td>
                </tr>
                <tr><th colspan="2" class="infobox-header">Prestasi utama</th></tr>
                <tr>
                  <td colspan="2">
                    <table>
                      <tr style="font-weight:bold;"><th>Tim</th><th>Turnamen</th><th>Tahun</th></tr>
                      <tr><td><a href="/w/index.php?title=RRQ_Hoshi&amp;redlink=1">RRQ Hoshi</a></td><td><a href="/wiki/MPL_Indonesia">MPL Indonesia Musim 9</a> (Juara 1)</td><td>2022</td></tr>
                    </table>
                  </td>
                </tr>
              </tbody>
            </table>
        """.trimIndent()

        val infobox = MippediaHtmlParsers.extractInfobox(sampleAthleteHtml)
        assertNotNull(infobox)
        assertEquals("Cristiano Ronaldo", infobox!!.title)
        assertEquals("https://commons.mippedia.org/images/thumb/c/c1/Ronaldo.jpg/220px-Ronaldo.jpg", infobox.imageUrl)

        // Standard top-level rows preserved, nested tables stripped out
        assertEquals(2, infobox.items.size)
        assertEquals("Nama lengkap" to "Cristiano Ronaldo dos Santos Aveiro", infobox.items[0])
        assertEquals("Posisi bermain" to "Penyerang", infobox.items[1])

        // Both main and collapsible season stat cards captured!
        assertEquals(2, infobox.seasonStats.size)
        assertEquals("STATISTIK MUSIM 2024–2025", infobox.seasonStats[0].headerTitle)
        assertEquals(4, infobox.seasonStats[0].metrics.size)
        assertEquals("GOL" to "28", infobox.seasonStats[0].metrics[1])
        assertEquals("Kompetisi" to "Liga Pro Saudi", infobox.seasonStats[0].details[0])

        assertEquals("STATISTIK MUSIM 2023–2024", infobox.seasonStats[1].headerTitle)
        assertEquals("TAMPIL" to "45", infobox.seasonStats[1].metrics[0])

        // Both career & achievements tables captured with redlink & link metadata
        assertEquals(2, infobox.careerSections.size)
        val senior = infobox.careerSections[0]
        assertEquals("Karier senior", senior.title)
        assertEquals(listOf("Tahun", "Tim", "Tampil (G, A)"), senior.headers)
        assertEquals(2, senior.rows.size)
        assertEquals("Manchester United F.C.", senior.rows[0][1].targetTitle)
        assertFalse(senior.rows[0][1].isRedlink)
        assertEquals("Al-Nassr FC", senior.rows[1][1].targetTitle)
        assertTrue(senior.rows[1][1].isRedlink)

        val achievements = infobox.careerSections[1]
        assertEquals("Prestasi utama", achievements.title)
        assertEquals("MPL Indonesia", achievements.rows[0][1].targetTitle)
        assertTrue(achievements.rows[0][0].isRedlink)
    }

    @Test
    fun `extractRelatedTitlesFromPageHtml parses wgRelatedArticles config`() {
        val html = """
            <script>RLCONF={"wgPageName":"Rumi_Haitami","wgRelatedArticles":{"Album musik":true,"Rumi Haitami Production (label rekaman)":true,"Diskografi Rumi Haitami":true},"wgTitle":"Rumi Haitami"};</script>
        """.trimIndent()

        val titles = MippediaHtmlParsers.extractRelatedTitlesFromPageHtml(html)
        assertEquals(
            listOf("Album musik", "Rumi Haitami Production (label rekaman)", "Diskografi Rumi Haitami"),
            titles
        )
    }

    @Test
    fun `WikiMediaItem correctly distinguishes audio, video, and image files`() {
        val audioOgg = WikiMediaItem(title = "File:Mippedia teks sound.ogg", url = "https://commons.mippedia.org/images/a/a7/Mippedia_teks_sound.ogg", mime = "application/ogg")
        assertTrue(audioOgg.isAudio)
        assertFalse(audioOgg.isVideo)
        assertFalse(audioOgg.isImage)

        val videoMp4 = WikiMediaItem(title = "File:Highlight.mp4", url = "https://commons.mippedia.org/images/b/b2/Highlight.mp4", mime = "video/mp4")
        assertTrue(videoMp4.isVideo)
        assertFalse(videoMp4.isAudio)
        assertFalse(videoMp4.isImage)

        val imgJpg = WikiMediaItem(title = "File:Aziz Calim.jpg", url = "https://commons.mippedia.org/images/a/ab/Aziz_Calim.jpg", mime = "image/jpeg")
        assertTrue(imgJpg.isImage)
        assertFalse(imgJpg.isAudio)
        assertFalse(imgJpg.isVideo)
    }

    @Test
    fun `extractInfobox parses combat match table and colspan info rows cleanly`() {
        val sampleFighterHtml = """
            <table class="infobox">
              <tbody>
                <tr><th colspan="2" class="infobox-above">Aziz Calim</th></tr>
                <tr><th colspan="2" class="infobox-header">Statistik pertandingan</th></tr>
                <tr>
                  <td colspan="2">
                    <table>
                      <tr><th>Hasil</th><th>Lawan</th><th>Ajang</th></tr>
                      <tr><td>✓ Menang (2-0) 5 ronde (3 menit) Keputusan Mutlak (UD)</td><td><a href="/w/index.php?title=Ezmir_Danial&amp;redlink=1">Ezmir Danial</a></td><td>Byon Combat Showbiz Vol. 5 (28 Juni 2025) • Duel Internasional 61 kg</td></tr>
                    </table>
                  </td>
                </tr>
                <tr><th colspan="2" class="infobox-header">Informasi lainnya</th></tr>
                <tr>
                  <td colspan="2">
                    <table>
                      <tr><th>Pengikut Instagram</th><td>361 RB</td></tr>
                      <tr><th colspan="2">Informasi sosial</th></tr>
                      <tr><td colspan="2">Data akurat per 20 Oktober 2026 dari Instagram, YouTube, Facebook, TikTok.</td></tr>
                    </table>
                  </td>
                </tr>
              </tbody>
            </table>
        """.trimIndent()

        val infobox = MippediaHtmlParsers.extractInfobox(sampleFighterHtml)
        assertNotNull(infobox)
        assertEquals(2, infobox!!.careerSections.size)

        val matches = infobox.careerSections[0]
        assertEquals("Statistik pertandingan", matches.title)
        assertEquals(listOf("Hasil", "Lawan", "Ajang"), matches.headers)
        assertEquals(1, matches.rows.size)
        assertEquals("Ezmir Danial", matches.rows[0][1].text)
        assertEquals("Ezmir Danial", matches.rows[0][1].targetTitle)
        assertTrue(matches.rows[0][1].isRedlink)

        val otherInfo = infobox.careerSections[1]
        assertEquals("Informasi lainnya", otherInfo.title)
        assertTrue(otherInfo.headers.isEmpty())
        assertEquals(3, otherInfo.rows.size)
        assertEquals(2, otherInfo.rows[0].size)
        assertEquals("Pengikut Instagram", otherInfo.rows[0][0].text)
        assertEquals("361 RB", otherInfo.rows[0][1].text)
        assertEquals(1, otherInfo.rows[1].size)
        assertEquals(2, otherInfo.rows[1][0].colSpan)
        assertTrue(otherInfo.rows[1][0].isHeaderCell)
        assertEquals(1, otherInfo.rows[2].size)
        assertEquals(2, otherInfo.rows[2][0].colSpan)
        assertFalse(otherInfo.rows[2][0].isHeaderCell)
    }
}
