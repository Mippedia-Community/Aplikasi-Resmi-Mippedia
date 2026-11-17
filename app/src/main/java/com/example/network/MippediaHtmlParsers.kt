package com.example.network

import com.example.model.*
import java.net.URLDecoder
import java.util.regex.Pattern

object MippediaHtmlParsers {

    fun unescapeHtmlEntities(input: String): String {
        return input
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&#039;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&nbsp;", " ")
            .replace("&#160;", " ")
            .replace("&ndash;", "–")
            .replace("&mdash;", "—")
            .replace("&bull;", "•")
            .replace("&rarr;", "→")
            .replace("&larr;", "←")
    }

    fun stripHtml(input: String): String {
        return unescapeHtmlEntities(input.replace(Regex("<[^>]+>"), " "))
            .replace(Regex("\\s+"), " ")
            .replace(Regex("\\s+([.,;:!?)])"), "$1")
            .replace(Regex("\\(\\s+"), "(")
            .trim()
    }

    fun normalizeMediaUrl(rawSrc: String): String {
        val trimmed = unescapeHtmlEntities(rawSrc.trim())
        return when {
            trimmed.startsWith("//") -> "https:$trimmed"
            trimmed.startsWith("/") -> "https://commons.mippedia.org$trimmed"
            else -> trimmed
        }
    }

    fun extractBestImageUrl(htmlFragment: String): String? {
        // Prefer high-resolution srcset if available
        val srcsetMatcher = Pattern.compile("srcset=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE).matcher(htmlFragment)
        if (srcsetMatcher.find()) {
            val srcsetVal = srcsetMatcher.group(1) ?: ""
            val candidates = srcsetVal.split(",")
                .map { it.trim().substringBefore(" ").trim() }
                .filter { it.isNotBlank() }
            val best = candidates.lastOrNull()
            if (!best.isNullOrBlank()) {
                return normalizeMediaUrl(best)
            }
        }
        val srcMatcher = Pattern.compile("src=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE).matcher(htmlFragment)
        while (srcMatcher.find()) {
            val rawSrc = srcMatcher.group(1) ?: continue
            val lower = rawSrc.lowercase()
            if (lower.contains("transparent") || lower.contains("1px")) continue
            return normalizeMediaUrl(rawSrc)
        }
        return null
    }

    fun decodeWikiTarget(rawHref: String): Pair<String?, Boolean> {
        val href = unescapeHtmlEntities(rawHref.trim())
        return when {
            href.contains("redlink=1", ignoreCase = true) || (href.contains("action=edit", ignoreCase = true) && href.contains("title=", ignoreCase = true)) -> {
                val rawTitle = href.substringAfter("title=", "").substringBefore("&").replace('+', ' ')
                val decoded = try { URLDecoder.decode(rawTitle, "UTF-8") } catch (_: Exception) { rawTitle }
                Pair(decoded.replace('_', ' ').trim().ifBlank { null }, true)
            }
            href.contains("/wiki/") -> {
                val rawTitle = href.substringAfter("/wiki/").substringBefore("#").substringBefore("?")
                val decoded = try { URLDecoder.decode(rawTitle, "UTF-8") } catch (_: Exception) { rawTitle }
                Pair(decoded.replace('_', ' ').trim().ifBlank { null }, false)
            }
            else -> Pair(null, false)
        }
    }

    /**
     * Extracts content inside a balanced <tagName ...>...</tagName> starting at [openTagIndex].
     */
    fun extractBalancedElement(html: String, openTagIndex: Int, tagName: String): Pair<String, Int>? {
        if (openTagIndex < 0 || openTagIndex >= html.length) return null
        val lower = html.lowercase()
        val tagLower = tagName.lowercase()
        val openPattern = "<$tagLower"
        val closePattern = "</$tagLower>"

        var depth = 0
        var pos = openTagIndex
        val len = lower.length

        while (pos < len) {
            val nextOpen = lower.indexOf(openPattern, pos)
            val nextClose = lower.indexOf(closePattern, pos)

            if (nextClose == -1) return null

            if (nextOpen != -1 && nextOpen < nextClose) {
                // Verify tag boundary after tagName (' ', '>', '\n', '\r', '\t', '/')
                val afterIdx = nextOpen + openPattern.length
                val nextChar = if (afterIdx < len) lower[afterIdx] else '>'
                if (nextChar == ' ' || nextChar == '>' || nextChar == '\n' || nextChar == '\r' || nextChar == '\t' || nextChar == '/') {
                    val gtIdx = lower.indexOf('>', afterIdx)
                    if (gtIdx == -1) return null
                    val selfClosing = gtIdx > 0 && lower[gtIdx - 1] == '/'
                    if (!selfClosing) {
                        depth++
                    }
                    pos = gtIdx + 1
                } else {
                    pos = afterIdx
                }
            } else {
                depth--
                val endPos = nextClose + closePattern.length
                if (depth <= 0) {
                    return Pair(html.substring(openTagIndex, endPos), endPos)
                }
                pos = endPos
            }
        }
        return null
    }

    /**
     * Parses ARTIKEL BULANAN and GAMBAR BULANAN dynamically from live Halaman_Utama HTML.
     * Returns null entries if section is missing — never plants stale/hardcoded fallback items.
     */
    fun parseMonthlyFeaturesFromMainPage(html: String): Pair<MonthlyFeature?, MonthlyFeature?> {
        if (html.isBlank()) return Pair(null, null)

        var monthlyArticle: MonthlyFeature? = null
        var monthlyImage: MonthlyFeature? = null

        // 1. ARTIKEL BULANAN slice
        val abHeaderIdx = html.indexOf("ARTIKEL BULANAN", ignoreCase = true)
        if (abHeaderIdx >= 0) {
            val afterHeader = html.indexOf("</div>", abHeaderIdx)
            val sliceStart = if (afterHeader >= 0) afterHeader + 6 else abHeaderIdx
            val nextBannerIdx = listOf(
                html.indexOf("HARI INI DALAM SEJARAH", sliceStart, ignoreCase = true),
                html.indexOf("GAMBAR BULANAN", sliceStart, ignoreCase = true),
                html.indexOf("ALAT  LANGSUNG", sliceStart, ignoreCase = true),
                html.indexOf("ALAT LANGSUNG", sliceStart, ignoreCase = true)
            ).filter { it > sliceStart }.minOrNull() ?: minOf(html.length, sliceStart + 5000)

            val abSection = html.substring(sliceStart, nextBannerIdx)
            val abImg = extractBestImageUrl(abSection)

            // Find first real article link (exclude File/Berkas/Special/Istimewa/Mippedia archive nav)
            var targetTitle: String? = null
            val linkMatcher = Pattern.compile("<a[^>]*href=[\"']([^\"']+)[\"'][^>]*>(.*?)</a>", Pattern.DOTALL).matcher(abSection)
            while (linkMatcher.find()) {
                val (decodedTitle, isRed) = decodeWikiTarget(linkMatcher.group(1) ?: "")
                if (decodedTitle != null && !isRed) {
                    val low = decodedTitle.lowercase()
                    if (!low.startsWith("berkas:") && !low.startsWith("file:") &&
                        !low.startsWith("istimewa:") && !low.startsWith("special:") &&
                        !low.startsWith("mippedia:")
                    ) {
                        targetTitle = decodedTitle
                        break
                    }
                }
            }

            // Extract main paragraph summary cleanly
            val pMatcher = Pattern.compile("<p[^>]*>(.*?)</p>", Pattern.DOTALL).matcher(abSection)
            var rawSummary = ""
            while (pMatcher.find()) {
                val text = stripHtml(pMatcher.group(1) ?: "")
                if (text.length > 25 && !text.contains("Artikel sebelumnya", ignoreCase = true)) {
                    rawSummary = text
                        .replace(Regex("\\(\\s*Selengkapnya\\.*\\s*\\)", RegexOption.IGNORE_CASE), "")
                        .replace(Regex("\\s+"), " ")
                        .trim()
                    break
                }
            }
            if (rawSummary.isBlank()) {
                rawSummary = stripHtml(abSection)
                    .substringBefore("←")
                    .replace(Regex("\\(\\s*Selengkapnya\\.*\\s*\\)", RegexOption.IGNORE_CASE), "")
                    .trim()
            }

            if (!targetTitle.isNullOrBlank() || rawSummary.isNotBlank()) {
                monthlyArticle = MonthlyFeature(
                    title = targetTitle ?: "Artikel Bulanan",
                    summary = rawSummary.take(300),
                    imageUrl = abImg,
                    targetArticleTitle = targetTitle
                )
            }
        }

        // 2. GAMBAR BULANAN slice
        val gbHeaderIdx = html.indexOf("GAMBAR BULANAN", ignoreCase = true)
        if (gbHeaderIdx >= 0) {
            val afterHeader = html.indexOf("</div>", gbHeaderIdx)
            val sliceStart = if (afterHeader >= 0) afterHeader + 6 else gbHeaderIdx
            val nextBannerIdx = listOf(
                html.indexOf("ALAT  LANGSUNG", sliceStart, ignoreCase = true),
                html.indexOf("ALAT LANGSUNG", sliceStart, ignoreCase = true),
                html.indexOf("STATISTIK", sliceStart, ignoreCase = true)
            ).filter { it > sliceStart }.minOrNull() ?: minOf(html.length, sliceStart + 5000)

            val gbSection = html.substring(sliceStart, nextBannerIdx)
            val gbImg = extractBestImageUrl(gbSection)

            // Find exact Commons file title from resource="/wiki/Berkas:..." or href="/wiki/Berkas:..." or img URL
            var fileTitle: String? = null
            val resMatcher = Pattern.compile("resource=[\"']/wiki/([^\"']+)[\"']").matcher(gbSection)
            if (resMatcher.find()) {
                val raw = try { URLDecoder.decode(resMatcher.group(1) ?: "", "UTF-8") } catch (_: Exception) { resMatcher.group(1) ?: "" }
                if (raw.isNotBlank()) fileTitle = raw.replace('_', ' ')
            }
            if (fileTitle.isNullOrBlank()) {
                val fileHrefMatcher = Pattern.compile("href=[\"']/wiki/((?:Berkas|File):[^\"']+)[\"']", Pattern.CASE_INSENSITIVE).matcher(gbSection)
                if (fileHrefMatcher.find()) {
                    val raw = try { URLDecoder.decode(fileHrefMatcher.group(1) ?: "", "UTF-8") } catch (_: Exception) { fileHrefMatcher.group(1) ?: "" }
                    if (raw.isNotBlank()) fileTitle = raw.replace('_', ' ')
                }
            }
            if (fileTitle.isNullOrBlank() && !gbImg.isNullOrBlank()) {
                val fileName = deriveFilenameFromUrl(gbImg)
                if (!fileName.isNullOrBlank()) fileTitle = "Berkas:$fileName"
            }

            // Extract caption from <figcaption>
            var captionText = ""
            val capMatcher = Pattern.compile("<figcaption[^>]*>(.*?)</figcaption>", Pattern.DOTALL or Pattern.CASE_INSENSITIVE).matcher(gbSection)
            if (capMatcher.find()) {
                captionText = stripHtml(capMatcher.group(1) ?: "")
            }

            // Extract attribution from <small>Oleh : ... • Lisensi : ...</small>
            var attribution: String? = null
            val smallMatcher = Pattern.compile("<small[^>]*>(.*?)</small>", Pattern.DOTALL or Pattern.CASE_INSENSITIVE).matcher(gbSection)
            if (smallMatcher.find()) {
                val txt = stripHtml(smallMatcher.group(1) ?: "")
                if (txt.isNotBlank()) attribution = txt
            }

            if (captionText.isBlank()) {
                captionText = stripHtml(gbSection)
                    .substringBefore("←")
                    .substringBefore("Oleh")
                    .trim()
            }

            val displayTitle = fileTitle
                ?.removePrefix("Berkas:")
                ?.removePrefix("File:")
                ?.substringBeforeLast('.')
                ?.trim()
                ?.ifBlank { "Gambar Pilihan Mippedia" }
                ?: "Gambar Pilihan Mippedia"

            if (!gbImg.isNullOrBlank() || !fileTitle.isNullOrBlank()) {
                monthlyImage = MonthlyFeature(
                    title = displayTitle,
                    summary = captionText.ifBlank { displayTitle }.take(260),
                    imageUrl = gbImg,
                    targetArticleTitle = fileTitle ?: gbImg,
                    attribution = attribution
                )
            }
        }

        return Pair(monthlyArticle, monthlyImage)
    }

    /**
     * Parses HARI INI DALAM SEJARAH section dynamically from Halaman_Utama HTML.
     */
    fun parseOnThisDayFromMainPage(html: String): OnThisDayData? {
        val headerIdx = html.indexOf("HARI INI DALAM SEJARAH", ignoreCase = true)
        if (headerIdx < 0) return null

        val afterHeader = html.indexOf("</div>", headerIdx)
        val startIdx = if (afterHeader >= 0) afterHeader + 6 else headerIdx
        val endIdx = listOf(
            html.indexOf("GAMBAR BULANAN", startIdx, ignoreCase = true),
            html.indexOf("ALAT  LANGSUNG", startIdx, ignoreCase = true),
            html.indexOf("ALAT LANGSUNG", startIdx, ignoreCase = true)
        ).filter { it > startIdx }.minOrNull() ?: minOf(html.length, startIdx + 12000)

        val sectionHtml = html.substring(startIdx, endIdx)

        // Match each bold category header followed by <ul>...</ul>
        val catPattern = Pattern.compile(
            "<div[^>]*font-weight\\s*:\\s*bold[^>]*>(.*?)</div>\\s*(?:<div[^>]*>\\s*)?<ul>(.*?)</ul>",
            Pattern.DOTALL or Pattern.CASE_INSENSITIVE
        )
        val catMatcher = catPattern.matcher(sectionHtml)
        data class RawCatMatch(val start: Int, val end: Int, val title: String, val ulHtml: String)
        val rawMatches = mutableListOf<RawCatMatch>()
        while (catMatcher.find()) {
            val catTitle = stripHtml(catMatcher.group(1) ?: "")
            val ulHtml = catMatcher.group(2) ?: ""
            if (catTitle.isNotBlank() && ulHtml.isNotBlank()) {
                rawMatches.add(RawCatMatch(catMatcher.start(), catMatcher.end(), catTitle, ulHtml))
            }
        }

        val categories = mutableListOf<OnThisDayCategory>()
        rawMatches.forEachIndexed { idx, raw ->
            val navEnd = if (idx + 1 < rawMatches.size) rawMatches[idx + 1].start else sectionHtml.length
            val navHtml = if (raw.end < navEnd) sectionHtml.substring(raw.end, navEnd) else ""

            val events = mutableListOf<OnThisDayEvent>()
            val liMatcher = Pattern.compile("<li[^>]*>(.*?)</li>", Pattern.DOTALL or Pattern.CASE_INSENSITIVE).matcher(raw.ulHtml)
            while (liMatcher.find()) {
                val liInner = liMatcher.group(1) ?: continue
                val boldYearMatcher = Pattern.compile("<b>\\s*(\\d{3,4})\\s*</b>", Pattern.CASE_INSENSITIVE).matcher(liInner)
                var year = ""
                var bodyHtml = liInner
                if (boldYearMatcher.find()) {
                    year = boldYearMatcher.group(1) ?: ""
                    bodyHtml = liInner.substring(boldYearMatcher.end())
                }
                val cleanBody = stripHtml(bodyHtml)
                    .removePrefix("–")
                    .removePrefix("-")
                    .removePrefix("—")
                    .trim()

                if (year.isBlank() && cleanBody.contains("–")) {
                    val split = cleanBody.split("–", limit = 2)
                    if (split[0].trim().all { it.isDigit() } && split[0].trim().length in 3..4) {
                        year = split[0].trim()
                    }
                }

                // Extract clickable article links inside the event item
                val links = mutableListOf<OnThisDayLink>()
                val aMatcher = Pattern.compile("<a[^>]*href=[\"']([^\"']+)[\"'][^>]*>(.*?)</a>", Pattern.DOTALL).matcher(liInner)
                while (aMatcher.find()) {
                    val (target, isRed) = decodeWikiTarget(aMatcher.group(1) ?: "")
                    val label = stripHtml(aMatcher.group(2) ?: "")
                    if (!target.isNullOrBlank() && !isRed && label.isNotBlank()) {
                        links.add(OnThisDayLink(label = label, targetTitle = target))
                    }
                }

                if (cleanBody.isNotBlank()) {
                    events.add(OnThisDayEvent(year = year, text = cleanBody, links = links))
                }
            }

            var yesterdayTitle: String? = null
            var tomorrowTitle: String? = null
            if (navHtml.contains("Kemarin", ignoreCase = true) || navHtml.contains("Besok", ignoreCase = true)) {
                val navMatcher = Pattern.compile("<a[^>]*href=[\"']([^\"']+)[\"'][^>]*>(.*?)</a>", Pattern.DOTALL).matcher(navHtml)
                while (navMatcher.find()) {
                    val (target, _) = decodeWikiTarget(navMatcher.group(1) ?: "")
                    val lbl = stripHtml(navMatcher.group(2) ?: "")
                    if (target != null) {
                        if (lbl.contains("Kemarin", ignoreCase = true)) yesterdayTitle = target
                        if (lbl.contains("Besok", ignoreCase = true)) tomorrowTitle = target
                    }
                }
            }

            if (events.isNotEmpty()) {
                categories.add(
                    OnThisDayCategory(
                        categoryTitle = raw.title,
                        events = events,
                        yesterdayTitle = yesterdayTitle,
                        tomorrowTitle = tomorrowTitle
                    )
                )
            }
        }

        if (categories.isEmpty()) return null
        return OnThisDayData(categories = categories)
    }

    /**
     * Parses complete Infobox including nested Sports Season Stats cards and Career/Achievements tables.
     */
    fun extractInfobox(html: String): WikiInfobox? {
        try {
            val startMatcher = Pattern.compile("<table[^>]*class=[\"'][^\"']*infobox[^\"']*[\"'][^>]*>", Pattern.CASE_INSENSITIVE).matcher(html)
            if (!startMatcher.find()) return null

            val balanced = extractBalancedElement(html, startMatcher.start(), "table") ?: return null
            val tableHtml = balanced.first

            // Title from <th class="infobox-above">
            var title = ""
            val titleMatcher = Pattern.compile("<th[^>]*class=[\"'][^\"']*infobox-above[^\"']*[\"'][^>]*>(.*?)</th>", Pattern.DOTALL or Pattern.CASE_INSENSITIVE).matcher(tableHtml)
            if (titleMatcher.find()) {
                title = stripHtml(titleMatcher.group(1) ?: "")
            }

            // Main portrait image
            var imgUrl: String? = null
            val imgMatcher = Pattern.compile("<img[^>]*src=[\"']([^\"']+)[\"']", Pattern.DOTALL or Pattern.CASE_INSENSITIVE).matcher(tableHtml)
            while (imgMatcher.find()) {
                val src = imgMatcher.group(1) ?: continue
                if (!src.contains("icon", ignoreCase = true) && !src.endsWith(".svg", ignoreCase = true)) {
                    imgUrl = normalizeMediaUrl(src)
                    break
                }
            }

            // 1. Parse Season Stats cards ("STATISTIK MUSIM ...")
            val seasonStats = extractSeasonStatsCards(tableHtml)

            // 2. Parse nested Career & Achievement tables
            val careerSections = extractCareerTableSections(tableHtml)

            // 3. Parse standard top-level <th> + <td> rows (exclude nested tables and stat divs)
            val strippedNested = removeNestedTablesAndStats(tableHtml)
            val items = mutableListOf<Pair<String, String>>()
            val rowMatcher = Pattern.compile("<tr[^>]*>(.*?)</tr>", Pattern.DOTALL or Pattern.CASE_INSENSITIVE).matcher(strippedNested)
            while (rowMatcher.find()) {
                val rowHtml = rowMatcher.group(1) ?: continue
                // Skip header-only rows (colspan="2")
                if (rowHtml.contains("infobox-header", ignoreCase = true)) continue
                val thMatcher = Pattern.compile("<th[^>]*>(.*?)</th>", Pattern.DOTALL or Pattern.CASE_INSENSITIVE).matcher(rowHtml)
                val tdMatcher = Pattern.compile("<td[^>]*>(.*?)</td>", Pattern.DOTALL or Pattern.CASE_INSENSITIVE).matcher(rowHtml)
                if (thMatcher.find() && tdMatcher.find()) {
                    val key = stripHtml(thMatcher.group(1) ?: "")
                    val value = stripHtml(tdMatcher.group(1) ?: "")
                    if (key.isNotBlank() && value.isNotBlank() && key != title) {
                        items.add(Pair(key, value))
                    }
                }
            }

            if (items.isEmpty() && seasonStats.isEmpty() && careerSections.isEmpty() && imgUrl == null && title.isBlank()) {
                return null
            }
            return WikiInfobox(
                title = title,
                imageUrl = imgUrl,
                items = items,
                seasonStats = seasonStats,
                careerSections = careerSections
            )
        } catch (_: Exception) {
            return null
        }
    }

    private fun extractSeasonStatsCards(html: String): List<SportsSeasonStatCard> {
        val result = mutableListOf<SportsSeasonStatCard>()
        val headerRegex = Pattern.compile("STATISTIK\\s+MUSIM[^<]{2,80}", Pattern.CASE_INSENSITIVE)
        val matcher = headerRegex.matcher(html)
        var searchFrom = 0

        while (matcher.find(searchFrom)) {
            val matchStart = matcher.start()
            // Walk outward up to 3 enclosing <div> ancestors to find the single card container wrapping header + metrics + details
            var chosenCard: Pair<String, Int>? = null
            var probePos = matchStart - 1
            for (step in 0..2) {
                val divStart = html.lastIndexOf("<div", probePos)
                if (divStart < 0) break
                val balanced = extractBalancedElement(html, divStart, "div")
                if (balanced != null) {
                    val candidateHtml = balanced.first
                    val hasMetricsOrDetails = candidateHtml.contains("TAMPIL", ignoreCase = true) ||
                        candidateHtml.contains("GOL", ignoreCase = true) ||
                        candidateHtml.contains("RATING", ignoreCase = true) ||
                        candidateHtml.contains("justify-content", ignoreCase = true)
                    if (hasMetricsOrDetails) {
                        chosenCard = balanced
                        break
                    }
                }
                probePos = divStart - 1
            }
            if (chosenCard == null) {
                searchFrom = matcher.end()
                continue
            }
            val cardHtml = chosenCard.first
            searchFrom = maxOf(matcher.end(), chosenCard.second)

            val headerTitle = stripHtml(matcher.group())

            // Metrics: TAMPIL, GOL, ASSIST, RATA2 RATING
            val metrics = mutableListOf<Pair<String, String>>()
            val knownLabels = listOf("TAMPIL", "GOL", "ASSIST", "RATA2 RATING", "RATING")
            val metricDivMatcher = Pattern.compile("<div>\\s*<div[^>]*>([^<]+)</div>\\s*<span[^>]*>(.*?)</span>\\s*</div>", Pattern.DOTALL or Pattern.CASE_INSENSITIVE).matcher(cardHtml)
            while (metricDivMatcher.find()) {
                val lbl = stripHtml(metricDivMatcher.group(1) ?: "")
                val valRaw = stripHtml(metricDivMatcher.group(2) ?: "")
                    .replace("⚽", "")
                    .replace("🅰️", "")
                    .replace("🅰", "")
                    .trim()
                if (lbl.isNotBlank() && valRaw.isNotBlank() && knownLabels.any { lbl.contains(it, ignoreCase = true) }) {
                    metrics.add(Pair(lbl, valRaw))
                }
            }

            // Key-value detail rows inside card: <span>Label</span><span>Value</span>
            val details = mutableListOf<Pair<String, String>>()
            val detailRowMatcher = Pattern.compile("<div[^>]*justify-content:\\s*space-between[^>]*>\\s*<span[^>]*>(.*?)</span>\\s*<span[^>]*>(.*?)</span>\\s*</div>", Pattern.DOTALL or Pattern.CASE_INSENSITIVE).matcher(cardHtml)
            while (detailRowMatcher.find()) {
                val k = stripHtml(detailRowMatcher.group(1) ?: "")
                val v = stripHtml(detailRowMatcher.group(2) ?: "")
                if (k.isNotBlank() && v.isNotBlank()) {
                    details.add(Pair(k, v))
                }
            }

            if (metrics.isNotEmpty() || details.isNotEmpty()) {
                result.add(SportsSeasonStatCard(headerTitle = headerTitle, metrics = metrics, details = details))
            }
        }
        return result
    }

    private fun extractCareerTableSections(infoboxHtml: String): List<CareerTableSection> {
        val sections = mutableListOf<CareerTableSection>()
        // Match <th colspan="2" class="infobox-header" ...>TITLE</th></tr><tr><td ...><table ...>...</table></td></tr>
        val headerPattern = Pattern.compile(
            "<th[^>]*class=[\"'][^\"']*infobox-header[^\"']*[\"'][^>]*>(.*?)</th>\\s*</tr>\\s*<tr[^>]*>\\s*<td[^>]*>\\s*<table",
            Pattern.DOTALL or Pattern.CASE_INSENSITIVE
        )
        val matcher = headerPattern.matcher(infoboxHtml)

        while (matcher.find()) {
            val sectionTitle = stripHtml(matcher.group(1) ?: "")
            val nestedTableStart = matcher.end() - "<table".length
            val nestedBalanced = extractBalancedElement(infoboxHtml, nestedTableStart, "table") ?: continue
            val nestedTableHtml = nestedBalanced.first

            val rowMatcher = Pattern.compile("<tr[^>]*>(.*?)</tr>", Pattern.DOTALL or Pattern.CASE_INSENSITIVE).matcher(nestedTableHtml)
            val headers = mutableListOf<String>()
            val rows = mutableListOf<List<CareerCell>>()

            var isFirstRow = true
            while (rowMatcher.find()) {
                val trHtml = rowMatcher.group(1) ?: continue
                val cells = mutableListOf<CareerCell>()
                val tdMatcher = Pattern.compile("<(t[dh])([^>]*)>(.*?)</\\1>", Pattern.DOTALL or Pattern.CASE_INSENSITIVE).matcher(trHtml)
                while (tdMatcher.find()) {
                    val tagType = (tdMatcher.group(1) ?: "td").lowercase()
                    val attrs = tdMatcher.group(2) ?: ""
                    val cellHtml = tdMatcher.group(3) ?: ""
                    val cellText = stripHtml(cellHtml)
                    var targetTitle: String? = null
                    var isRedlink = false

                    val colspanMatch = Pattern.compile("colspan\\s*=\\s*[\"']?(\\d+)[\"']?", Pattern.CASE_INSENSITIVE).matcher(attrs)
                    val colSpan = if (colspanMatch.find()) (colspanMatch.group(1)?.toIntOrNull() ?: 1) else 1

                    val aMatcher = Pattern.compile("<a[^>]*href=[\"']([^\"']+)[\"'][^>]*>", Pattern.CASE_INSENSITIVE).matcher(cellHtml)
                    if (aMatcher.find()) {
                        val (t, red) = decodeWikiTarget(aMatcher.group(1) ?: "")
                        targetTitle = t
                        isRedlink = red
                    }
                    cells.add(
                        CareerCell(
                            text = cellText,
                            targetTitle = targetTitle,
                            isRedlink = isRedlink,
                            colSpan = colSpan,
                            isHeaderCell = (tagType == "th")
                        )
                    )
                }

                if (cells.isEmpty()) continue

                val allHeaderTags = cells.size >= 2 && cells.all { it.isHeaderCell }
                val knownHeaderWords = setOf(
                    "tahun", "tim", "klub", "kompetisi", "turnamen", "hasil", "lawan", "ajang",
                    "rekor", "musim", "peran", "acara", "lokasi", "catatan", "metode", "ronde",
                    "waktu", "negara", "peringkat", "posisi", "gelar", "statistik"
                )
                val matchesHeaderWords = cells.count { c ->
                    val l = c.text.lowercase()
                    l in knownHeaderWords || l.startsWith("tampil") || l.startsWith("penampilan")
                } >= 2

                val looksLikeHeader = isFirstRow && (
                    allHeaderTags ||
                    matchesHeaderWords ||
                    (cells.size >= 2 && (trHtml.contains("font-weight:bold", ignoreCase = true) || trHtml.contains("font-weight: bold", ignoreCase = true)))
                )
                isFirstRow = false

                if (looksLikeHeader) {
                    headers.addAll(cells.map { it.text })
                } else {
                    if (cells.any { it.text.isNotBlank() }) {
                        rows.add(cells)
                    }
                }
            }

            if (sectionTitle.isNotBlank() && rows.isNotEmpty()) {
                sections.add(CareerTableSection(title = sectionTitle, headers = headers, rows = rows))
            }
        }
        return sections
    }

    private fun removeNestedTablesAndStats(infoboxHtml: String): String {
        // Strip outer <table> tag so we only search inner nested <table> tags
        val firstGt = infoboxHtml.indexOf('>')
        val lastClose = infoboxHtml.lastIndexOf("</table>", ignoreCase = true)
        if (firstGt < 0 || lastClose <= firstGt) return infoboxHtml

        val inner = infoboxHtml.substring(firstGt + 1, lastClose)
        val sb = StringBuilder()
        var pos = 0
        while (pos < inner.length) {
            val nextTable = inner.indexOf("<table", pos, ignoreCase = true)
            if (nextTable == -1) {
                sb.append(inner.substring(pos))
                break
            }
            sb.append(inner, pos, nextTable)
            val balanced = extractBalancedElement(inner, nextTable, "table")
            pos = balanced?.second ?: (nextTable + 6)
        }
        return sb.toString()
            .replace(Regex("<div[^>]*class=[\"'][^\"']*mw-collapsible[^\"']*[\"'][^>]*>.*?</div>\\s*</div>", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE)), "")
    }

    /**
     * Extracts real-time RelatedArticles titles from wiki page HTML (`wgRelatedArticles` JS config).
     */
    fun extractRelatedTitlesFromPageHtml(pageHtml: String): List<String> {
        val pattern = Pattern.compile("\"wgRelatedArticles\"\\s*:\\s*\\{([^}]*)\\}")
        val matcher = pattern.matcher(pageHtml)
        if (!matcher.find()) return emptyList()

        val objBody = matcher.group(1) ?: return emptyList()
        val titles = mutableListOf<String>()
        val keyMatcher = Pattern.compile("\"((?:\\\\\"|[^\"])+)\"\\s*:\\s*true").matcher(objBody)
        while (keyMatcher.find()) {
            val raw = keyMatcher.group(1)
                ?.replace("\\\"", "\"")
                ?.replace("\\\\", "\\")
                ?.trim()
            if (!raw.isNullOrBlank() && !titles.contains(raw)) {
                titles.add(raw)
            }
        }
        return titles
    }

    fun deriveFilenameFromUrl(url: String): String? {
        val decoded = try { URLDecoder.decode(url, "UTF-8") } catch (_: Exception) { url }
        val cleanPath = decoded.substringBefore('?')
        val segments = cleanPath.split("/").filter { it.isNotBlank() }
        val thumbIdx = segments.indexOf("thumb")
        val name = if (thumbIdx >= 0 && segments.size > thumbIdx + 3) {
            segments[thumbIdx + 3]
        } else {
            segments.lastOrNull()?.replace(Regex("^\\d+px-"), "")
        }
        return name?.replace('_', ' ')?.trim()?.ifBlank { null }
    }
}
