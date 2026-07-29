package com.example.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream

data class PageContent(
    val title: String,
    val chapter: String,
    val subtitle: String,
    val paragraphs: List<String>
)

object SamplePdfGenerator {

    fun getOrCreateSamplePdf(context: Context): File {
        val sampleFile = File(context.filesDir, "PDF_Master_Welcome_Guide.pdf")
        if (sampleFile.exists() && sampleFile.length() > 0) {
            return sampleFile
        }

        val pdfDocument = PdfDocument()

        val pageWidth = 595 // Standard A4 width in points (72 dpi)
        val pageHeight = 842 // Standard A4 height in points

        // Pages content
        val pagesData = listOf(
            PageContent(
                title = "Welcome to PDF Master",
                chapter = "Getting Started",
                subtitle = "Your All-in-One Professional PDF Reader & Document Hub",
                paragraphs = listOf(
                    "PDF Master is engineered for ultimate performance, smooth continuous reading, instant search, page bookmarks, and offline privacy.",
                    "This sample document demonstrates the rich capabilities of PDF Master, including table of contents, high-resolution zooming, page navigation, and text search.",
                    "Key Highlights:",
                    "• Fast, offline-first native Android rendering engine",
                    "• Continuous vertical scroll & single-page horizontal swipe",
                    "• Full-text keyword search with match highlighting",
                    "• Quick bookmarks with personalized notes",
                    "• Interactive thumbnail drawer & chapter navigation",
                    "• Light, Dark & System Adaptive themes"
                )
            ),
            PageContent(
                title = "Chapter 1: Reading Modes & Zooming",
                chapter = "Chapter 1",
                subtitle = "Tailor Your Reading Experience to Any Screen Size",
                paragraphs = listOf(
                    "Whether you are reading school notes, business reports, or eBooks, PDF Master offers multiple view options:",
                    "1. Continuous Vertical Scroll Mode: Scroll through long documents seamlessly without page flip interruptions.",
                    "2. Single-Page Mode: Focus on one page at a time with quick page buttons.",
                    "3. Horizontal Swipe Mode: Natural book-flipping swipe gestures for tablets and smartphones.",
                    "Dynamic Zoom & Fit Modes:",
                    "Use pinch-to-zoom gestures or double-tap anywhere on the page to zoom between 75% and 200%. Tap 'Fit Width' or 'Fit Page' in the zoom bar for automatic alignment."
                )
            ),
            PageContent(
                title = "Chapter 2: Text Search & Outlines",
                chapter = "Chapter 2",
                subtitle = "Locate Key Keywords & Jump to Chapters Instantly",
                paragraphs = listOf(
                    "Searching inside PDFs:",
                    "Tap the Search icon on the top toolbar to search any term across the entire document. Matches are highlighted instantly with 'Result X of Y' counter.",
                    "Options available:",
                    "• Case-Sensitive Matching",
                    "• Whole-Word Filtering",
                    "• Next & Previous Result Jump",
                    "Table of Contents Navigation:",
                    "Access the TOC outline drawer to view chapters, sections, and subsections. Tapping any entry navigates directly to that page."
                )
            ),
            PageContent(
                title = "Chapter 3: Bookmarks & Position Saving",
                chapter = "Chapter 3",
                subtitle = "Never Lose Your Place in a Book or Lecture",
                paragraphs = listOf(
                    "Automatic Resume Reading:",
                    "PDF Master automatically saves your last read page. When reopening a document, you will see a prompt: 'Continue reading from page X?'.",
                    "Personalized Bookmarks:",
                    "Tap the Bookmark icon on any page to record important formulas, notes, or references.",
                    "Each bookmark records:",
                    "• Document Title & Page Number",
                    "• Date & Time Stamp",
                    "• Custom User Note (e.g., 'Important algebra formula' or 'Exam Topic')",
                    "Manage your bookmarks anytime from the Home screen or Reader drawer."
                )
            ),
            PageContent(
                title = "Chapter 4: File Privacy & Information",
                chapter = "Chapter 4",
                subtitle = "100% On-Device Local Processing",
                paragraphs = listOf(
                    "Your Privacy Matters:",
                    "Your PDF documents stay 100% on your Android device. PDF Master processes files locally without uploading or sharing your personal files.",
                    "Document Metadata Info:",
                    "Tap the Info menu to view detailed file properties including file size, page count, document title, creation date, and modification timestamps.",
                    "Password Protected Files:",
                    "PDF Master supports encrypted PDFs with a secure password modal."
                )
            ),
            PageContent(
                title = "Summary & Quick Shortcuts",
                chapter = "Reference",
                subtitle = "Essential Toolbar Shortcuts",
                paragraphs = listOf(
                    "Top Toolbar Actions:",
                    "• Back (Home Screen) | Search | Bookmark | TOC Outline | Thumbnail Grid | Document Info",
                    "Bottom Toolbar Actions:",
                    "• Previous Page | Page Counter (Click to Jump) | Next Page | Zoom Out | Zoom % | Zoom In",
                    "Enjoy reading your documents with PDF Master!"
                )
            )
        )

        val headerPaint = Paint().apply {
            color = Color.parseColor("#1E3A8A")
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 22f
            isAntiAlias = true
        }

        val chapterPaint = Paint().apply {
            color = Color.parseColor("#7C3AED")
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 12f
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.parseColor("#475569")
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            textSize = 14f
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.parseColor("#1E293B")
            typeface = Typeface.DEFAULT
            textSize = 13f
            isAntiAlias = true
        }

        val footerPaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            typeface = Typeface.DEFAULT
            textSize = 10f
            isAntiAlias = true
        }

        pagesData.forEachIndexed { index, content ->
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            // Background
            canvas.drawColor(Color.WHITE)

            // Header banner background
            val bannerPaint = Paint().apply {
                color = Color.parseColor("#F1F5F9")
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 90f, bannerPaint)

            val linePaint = Paint().apply {
                color = Color.parseColor("#2563EB")
                strokeWidth = 3f
            }
            canvas.drawLine(0f, 90f, pageWidth.toFloat(), 90f, linePaint)

            // Chapter tag badge
            val badgePaint = Paint().apply {
                color = Color.parseColor("#7C3AED")
                style = Paint.Style.FILL
            }
            val badgeRect = RectF(40f, 20f, 150f, 38f)
            canvas.drawRoundRect(badgeRect, 8f, 8f, badgePaint)

            val badgeTextPaint = Paint().apply {
                color = Color.WHITE
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText(content.chapter.uppercase(), 48f, 33f, badgeTextPaint)

            // Title
            canvas.drawText(content.title, 40f, 65f, headerPaint)

            // Subtitle
            canvas.drawText(content.subtitle, 40f, 120f, subtitlePaint)

            // Horizontal separator
            val sepPaint = Paint().apply {
                color = Color.parseColor("#E2E8F0")
                strokeWidth = 1f
            }
            canvas.drawLine(40f, 135f, pageWidth - 40f, 135f, sepPaint)

            // Body Paragraphs
            var currentY = 165f
            for (p in content.paragraphs) {
                if (p.startsWith("•") || p.startsWith("1.") || p.startsWith("2.") || p.startsWith("3.")) {
                    bodyPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    bodyPaint.color = Color.parseColor("#0F172A")
                } else if (p.endsWith(":")) {
                    bodyPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    bodyPaint.color = Color.parseColor("#1E3A8A")
                } else {
                    bodyPaint.typeface = Typeface.DEFAULT
                    bodyPaint.color = Color.parseColor("#334155")
                }

                // Wrap text manually for lines
                val maxTextWidth = pageWidth - 80f
                val words = p.split(" ")
                var currentLine = ""

                for (word in words) {
                    val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                    if (bodyPaint.measureText(testLine) > maxTextWidth) {
                        canvas.drawText(currentLine, 40f, currentY, bodyPaint)
                        currentY += 20f
                        currentLine = word
                    } else {
                        currentLine = testLine
                    }
                }
                if (currentLine.isNotEmpty()) {
                    canvas.drawText(currentLine, 40f, currentY, bodyPaint)
                    currentY += 26f
                }
            }

            // Decorative card box on page
            if (index == 0) {
                val boxPaint = Paint().apply {
                    color = Color.parseColor("#EFF6FF")
                    style = Paint.Style.FILL
                }
                val boxBorder = Paint().apply {
                    color = Color.parseColor("#BFDBFE")
                    style = Paint.Style.STROKE
                    strokeWidth = 1.5f
                }
                val cardRect = RectF(40f, currentY + 10f, pageWidth - 40f, currentY + 110f)
                canvas.drawRoundRect(cardRect, 12f, 12f, boxPaint)
                canvas.drawRoundRect(cardRect, 12f, 12f, boxBorder)

                val notePaint = Paint().apply {
                    color = Color.parseColor("#1D4ED8")
                    textSize = 12f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    isAntiAlias = true
                }
                canvas.drawText("⚡ Quick Start Tip:", 55f, currentY + 38f, notePaint)

                val noteSub = Paint().apply {
                    color = Color.parseColor("#1E293B")
                    textSize = 11f
                    isAntiAlias = true
                }
                canvas.drawText("Tap the floating action button on the Home screen or pick any PDF file", 55f, currentY + 62f, noteSub)
                canvas.drawText("from your internal storage to start reading immediately!", 55f, currentY + 82f, noteSub)
            }

            // Footer
            canvas.drawLine(40f, pageHeight - 50f, pageWidth - 40f, pageHeight - 50f, sepPaint)
            canvas.drawText("PDF Master • Official Sample Document", 40f, pageHeight - 30f, footerPaint)
            val pageNumText = "Page ${index + 1} of ${pagesData.size}"
            canvas.drawText(pageNumText, pageWidth - 40f - footerPaint.measureText(pageNumText), pageHeight - 30f, footerPaint)

            pdfDocument.finishPage(page)
        }

        try {
            val fos = FileOutputStream(sampleFile)
            pdfDocument.writeTo(fos)
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }

        return sampleFile
    }
}
