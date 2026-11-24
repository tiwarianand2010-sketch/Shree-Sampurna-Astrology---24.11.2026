package com.example.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import com.example.astro.ComprehensiveReportBuilder
import com.example.astro.VedicCalculator
import com.example.core.AccountMethod
import com.example.core.AppLanguage
import com.example.core.AstrologerAccount
import com.example.data.PlacesRepository
import com.example.model.BirthInput
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.shadow.api.Shadow
import java.io.ByteArrayOutputStream
import java.io.OutputStream

@Implements(PdfDocument::class)
class TestShadowPdfDocument {
    private val finishedPages = mutableListOf<PdfDocument.PageInfo>()
    private var currentPage: PdfDocument.Page? = null
    private var closed = false

    @Implementation
    fun __constructor__() {
        // Avoid nativeCreateDocument() returning 0 in JVM
    }

    @Implementation
    fun startPage(pageInfo: PdfDocument.PageInfo): PdfDocument.Page {
        check(!closed) { "document is closed!" }
        check(currentPage == null) { "previous page not finished" }
        val bmp = Bitmap.createBitmap(
            pageInfo.pageWidth.coerceAtLeast(1),
            pageInfo.pageHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bmp)
        val ctor = PdfDocument.Page::class.java.getDeclaredConstructor(
            Canvas::class.java,
            PdfDocument.PageInfo::class.java
        ).apply { isAccessible = true }
        val page = ctor.newInstance(canvas, pageInfo)
        currentPage = page
        return page
    }

    @Implementation
    fun finishPage(page: PdfDocument.Page) {
        check(!closed) { "document is closed!" }
        finishedPages.add(page.info)
        currentPage = null
    }

    @Implementation
    fun writeTo(out: OutputStream) {
        check(!closed) { "document is closed!" }
        check(currentPage == null) { "page not finished before writeTo" }
        val header = "%PDF-1.4\n% Pages=${finishedPages.size}\n".toByteArray()
        out.write(header)
        repeat(finishedPages.size) { idx ->
            out.write("%%Page: ${idx + 1}\n".toByteArray())
        }
        out.write("%%EOF\n".toByteArray())
    }

    @Implementation
    fun close() {
        closed = true
        currentPage = null
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], shadows = [TestShadowPdfDocument::class])
class PdfReportGeneratorTest {

    @Test
    fun `all 21 pdf modules draw cleanly onto android canvas with astrologer branding`() {
        val delhi = PlacesRepository.defaultPlace
        val kundali = VedicCalculator.calculateKundali(
            BirthInput("Acharya Canvas Demo", 1993, 7, 21, 10, 15, delhi),
            prashnaNumber = 55
        )
        val bundle = ComprehensiveReportBuilder.build(kundali)
        val astro = AstrologerAccount(
            astrologerName = "Pt. Raghav Sharma",
            mobileNumber = "+91 9876543210",
            studioName = "Shri Sampurna Astrology",
            accountMethod = AccountMethod.MOBILE
        )
        val p = PdfCanvasHelpers.Paints()
        val bmp = Bitmap.createBitmap(PdfCanvasHelpers.PAGE_WIDTH, PdfCanvasHelpers.PAGE_HEIGHT, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)

        PdfSectionsPart1.drawModule1BirthPanchangPage(c, p, astro, kundali, bundle, 1, 18, false)
        PdfSectionsPart1.drawModule2ChartsAndPositionsPage(c, p, astro, kundali, bundle, 2, 18)
        PdfSectionsPart1.drawModule3PhaladeshPage(c, p, astro, kundali, bundle, 4, 18)
        PdfSectionsPart1.drawModule4YoginiDashaPage(c, p, astro, kundali, bundle, 5, 18)
        PdfSectionsPart1.drawModule5BcpPage(c, p, astro, kundali, bundle, 6, 18)
        PdfSectionsPart1.drawModule6CuspsAndKarakasPage(c, p, astro, kundali, bundle, 7, 18)
        PdfSectionsPart1.drawModule7NadiPage(c, p, astro, kundali, bundle, 8, 18)
        PdfSectionsPart1.drawModule8KpExtendedPage(c, p, astro, kundali, bundle, 9, 18)
        PdfSectionsPart1.drawModule9And10LagnasAndYogasPage(c, p, astro, kundali, bundle, 10, 18)
        PdfSectionsPart1.drawModule11All20VargasMatrixPage(c, p, astro, kundali, bundle, 11, 18)

        PdfSectionsPart2.drawModule12AshtakavargaShadbalaPage(c, p, astro, kundali, bundle, 12, 18)
        PdfSectionsPart2.drawModule13And14GemsAndVedicNumPage(c, p, astro, kundali, bundle, 13, 18)
        PdfSectionsPart2.drawModule15LoShuExtendedPage(c, p, astro, kundali, bundle, 14, 18)
        PdfSectionsPart2.drawModule16SarvatobhadraKotaPage(c, p, astro, kundali, bundle, 15, 18)
        PdfSectionsPart2.drawModule17JaiminiPage(c, p, astro, kundali, bundle, 16, 18)
        PdfSectionsPart2.drawModule18And19LalKitabAndBnnPage(c, p, astro, kundali, bundle, 17, 18)
        PdfSectionsPart2.drawModule20TajikaVarshaphalPage(c, p, astro, kundali, bundle, 18, 18)
        PdfSectionsPart2.drawModule21CrystalsAndRemedyTrackerPage(c, p, astro, kundali, bundle, 19, 250)

        assertEquals("Pt. Raghav Sharma", astro.effectiveHeaderName())
    }

    @Test
    fun `summary 18-page and brihat 250-page pdf generation progress completes without errors`() {
        val delhi = PlacesRepository.defaultPlace
        val kundali = VedicCalculator.calculateKundali(
            BirthInput("Raghav Demo", 1995, 6, 12, 9, 30, delhi)
        )
        val astro = AstrologerAccount(
            astrologerName = "Pt. Raghav Sharma",
            mobileNumber = "+91 9876543210",
            studioName = "Shri Sampurna Astrology",
            accountMethod = AccountMethod.MOBILE
        )

        var lastSummaryPage = 0
        val summaryBytes = ByteArrayOutputStream()
        runBlocking {
            PdfReportGenerator.generateSummaryPdf(
                result = kundali,
                language = AppLanguage.EN,
                outputStream = summaryBytes,
                astrologer = astro
            ) { page, _ -> lastSummaryPage = page }
        }
        assertEquals(18, lastSummaryPage)
        assertTrue(summaryBytes.toString().contains("% Pages=18"))

        var lastBrihatPage = 0
        val brihatBytes = ByteArrayOutputStream()
        runBlocking {
            PdfReportGenerator.generateBrihat250Pdf(
                result = kundali,
                language = AppLanguage.EN,
                outputStream = brihatBytes,
                astrologer = astro
            ) { page, _ -> lastBrihatPage = page }
        }
        assertEquals(250, lastBrihatPage)
        assertTrue(brihatBytes.toString().contains("% Pages=250"))
    }
}
