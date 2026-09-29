package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.Invoice
import com.example.data.model.InvoiceLineItemSerializer
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object InvoicePdfGenerator {

    fun generateInvoicePdf(context: Context, invoice: Invoice): File {
        val invoicesDir = File(context.cacheDir, "invoices")
        if (!invoicesDir.exists()) invoicesDir.mkdirs()

        val file = File(invoicesDir, "${invoice.invoiceNumber}.pdf")

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (in points)
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Header Banner
        paint.color = Color.rgb(2, 119, 189) // Deep AC Blue
        canvas.drawRect(0f, 0f, 595f, 95f, paint)

        paint.color = Color.WHITE
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("SRI VEL ENTERPRISES", 36f, 42f, paint)

        paint.textSize = 12f
        paint.isFakeBoldText = false
        canvas.drawText("AC Services & Repairs | Installation, Maintenance & Overhaul", 36f, 62f, paint)
        canvas.drawText("GSTIN: 33ABCDE1234F1Z5 | Ph: +91 98401 23456 | Chennai", 36f, 78f, paint)

        // "TAX INVOICE" Badge on right
        paint.textSize = 16f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("TAX INVOICE", 559f, 46f, paint)
        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText(invoice.invoiceNumber, 559f, 64f, paint)
        paint.textAlign = Paint.Align.LEFT

        // 2. Invoice Details & Customer Info
        var y = 130f
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("BILL TO:", 36f, y, paint)

        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("INVOICE DETAILS:", 559f, y, paint)
        paint.textAlign = Paint.Align.LEFT

        y += 18f
        paint.isFakeBoldText = false
        paint.textSize = 11f
        paint.color = Color.rgb(51, 65, 85)
        canvas.drawText(invoice.customerName, 36f, y, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Date: ${invoice.invoiceDate}", 559f, y, paint)
        paint.textAlign = Paint.Align.LEFT

        y += 16f
        canvas.drawText("Ph: +91 ${invoice.customerPhone}", 36f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Payment Status: ${invoice.paymentStatus.label}", 559f, y, paint)
        paint.textAlign = Paint.Align.LEFT

        y += 16f
        canvas.drawText(invoice.customerAddress.take(45), 36f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Payment Mode: ${invoice.paymentMethod}", 559f, y, paint)
        paint.textAlign = Paint.Align.LEFT

        // 3. Table Header
        y += 35f
        paint.color = Color.rgb(241, 245, 249) // Light grey table header
        canvas.drawRect(36f, y - 14f, 559f, y + 14f, paint)

        paint.color = Color.rgb(15, 23, 42)
        paint.isFakeBoldText = true
        paint.textSize = 10f
        canvas.drawText("ITEM / SERVICE DESCRIPTION", 46f, y + 4f, paint)
        canvas.drawText("QTY", 370f, y + 4f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("RATE (₹)", 460f, y + 4f, paint)
        canvas.drawText("AMOUNT (₹)", 549f, y + 4f, paint)
        paint.textAlign = Paint.Align.LEFT

        // Line items
        val items = InvoiceLineItemSerializer.deserialize(invoice.lineItemsJson)
        paint.isFakeBoldText = false
        paint.textSize = 10f

        val linePaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 1f
        }

        items.forEach { item ->
            y += 26f
            paint.color = Color.rgb(30, 41, 59)
            canvas.drawText(item.description.take(48), 46f, y, paint)
            canvas.drawText("${item.quantity}", 375f, y, paint)

            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(String.format(Locale.US, "%.2f", item.unitPrice), 460f, y, paint)
            canvas.drawText(String.format(Locale.US, "%.2f", item.total), 549f, y, paint)
            paint.textAlign = Paint.Align.LEFT

            canvas.drawLine(36f, y + 8f, 559f, y + 8f, linePaint)
        }

        // 4. Totals Block
        y += 30f
        val summaryX = 390f
        paint.textSize = 10f
        paint.color = Color.rgb(71, 85, 105)
        canvas.drawText("Subtotal:", summaryX, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(String.format(Locale.US, "₹%.2f", invoice.subtotal), 549f, y, paint)
        paint.textAlign = Paint.Align.LEFT

        y += 18f
        canvas.drawText("GST (${invoice.taxRatePercent}%):", summaryX, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(String.format(Locale.US, "₹%.2f", invoice.taxAmount), 549f, y, paint)
        paint.textAlign = Paint.Align.LEFT

        if (invoice.discountAmount > 0) {
            y += 18f
            paint.color = Color.rgb(46, 125, 50)
            canvas.drawText("Special Discount:", summaryX, y, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(String.format(Locale.US, "-₹%.2f", invoice.discountAmount), 549f, y, paint)
            paint.textAlign = Paint.Align.LEFT
        }

        y += 24f
        paint.color = Color.rgb(2, 119, 189)
        paint.isFakeBoldText = true
        paint.textSize = 14f
        canvas.drawText("Total Amount:", summaryX, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(String.format(Locale.US, "₹%.2f", invoice.totalAmount), 549f, y, paint)
        paint.textAlign = Paint.Align.LEFT

        // 5. Payment Details & Warranty Box
        y += 40f
        paint.color = Color.rgb(240, 249, 255)
        canvas.drawRoundRect(36f, y, 559f, y + 75f, 8f, 8f, paint)

        val borderPaint = Paint().apply {
            color = Color.rgb(186, 230, 253)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRoundRect(36f, y, 559f, y + 75f, 8f, 8f, borderPaint)

        paint.color = Color.rgb(2, 119, 189)
        paint.textSize = 10f
        paint.isFakeBoldText = true
        canvas.drawText("BHIM UPI / ONLINE PAYMENT DETAILS:", 48f, y + 20f, paint)

        paint.color = Color.rgb(30, 41, 59)
        paint.isFakeBoldText = false
        paint.textSize = 9.5f
        canvas.drawText("UPI VPA: srivelenterprises@icici | Account: Sri Vel Enterprises", 48f, y + 36f, paint)
        canvas.drawText("Scan the QR code in your Sri Vel mobile app to instantly pay and clear invoice.", 48f, y + 50f, paint)
        canvas.drawText("Warranty: 30-Day Guarantee on gas pressure, leak sealants and repair works.", 48f, y + 64f, paint)

        // 6. Signatures / Footer
        y += 120f
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 9f
        canvas.drawText("Customer Signature", 70f, y, paint)
        canvas.drawLine(40f, y - 10f, 160f, y - 10f, linePaint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("For SRI VEL ENTERPRISES", 529f, y - 18f, paint)
        canvas.drawText("Authorized Signatory", 529f, y, paint)
        canvas.drawLine(410f, y - 10f, 559f, y - 10f, linePaint)
        paint.textAlign = Paint.Align.LEFT

        // Bottom footer
        y += 45f
        paint.color = Color.rgb(148, 163, 184)
        paint.textSize = 8.5f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Thank you for choosing Sri Vel Enterprises AC Services & Repairs! 24x7 Helpline: 9840123456", 297f, y, paint)

        pdfDocument.finishPage(page)

        val outputStream = FileOutputStream(file)
        pdfDocument.writeTo(outputStream)
        outputStream.flush()
        outputStream.close()
        pdfDocument.close()

        return file
    }

    fun sharePdfInvoice(context: Context, invoice: Invoice) {
        val file = generateInvoicePdf(context, invoice)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Invoice ${invoice.invoiceNumber} - Sri Vel Enterprises AC Services")
            putExtra(Intent.EXTRA_TEXT, "Dear ${invoice.customerName},\n\nPlease find attached your digital invoice ${invoice.invoiceNumber} for AC Service by Sri Vel Enterprises.\nTotal Amount: ₹${invoice.totalAmount}\nStatus: ${invoice.paymentStatus.label}.\n\nThank you for choosing Sri Vel Enterprises!")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Share Invoice PDF"))
    }
}
