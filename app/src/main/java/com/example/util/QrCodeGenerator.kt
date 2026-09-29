package com.example.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import java.security.MessageDigest

object QrCodeGenerator {

    /**
     * Generates a realistic, high-resolution QR Code Bitmap (29x29 matrix)
     * with authentic finder patterns, timing tracks, alignment pattern,
     * and deterministic payload data modules.
     */
    fun generateQrBitmap(payload: String, sizePx: Int = 512): Bitmap {
        val matrixSize = 29
        val matrix = Array(matrixSize) { BooleanArray(matrixSize) { false } }
        val reserved = Array(matrixSize) { BooleanArray(matrixSize) { false } }

        fun placeFinderPattern(row: Int, col: Int) {
            for (r in 0..6) {
                for (c in 0..6) {
                    val isBorder = (r == 0 || r == 6 || c == 0 || c == 6)
                    val isCenter = (r in 2..4 && c in 2..4)
                    matrix[row + r][col + c] = isBorder || isCenter
                    reserved[row + r][col + c] = true
                }
            }
            // Separator around finder
            for (r in -1..7) {
                for (c in -1..7) {
                    val cr = row + r
                    val cc = col + c
                    if (cr in 0 until matrixSize && cc in 0 until matrixSize) {
                        reserved[cr][cc] = true
                    }
                }
            }
        }

        // Top-left finder
        placeFinderPattern(0, 0)
        // Top-right finder
        placeFinderPattern(0, matrixSize - 7)
        // Bottom-left finder
        placeFinderPattern(matrixSize - 7, 0)

        // Alignment pattern at (20, 20) for 29x29
        val alignR = 20
        val alignC = 20
        for (r in -2..2) {
            for (c in -2..2) {
                val isOuter = (r == -2 || r == 2 || c == -2 || c == 2)
                val isCenter = (r == 0 && c == 0)
                matrix[alignR + r][alignC + c] = isOuter || isCenter
                reserved[alignR + r][alignC + c] = true
            }
        }

        // Timing patterns
        for (i in 8 until matrixSize - 8) {
            val bit = (i % 2 == 0)
            if (!reserved[6][i]) {
                matrix[6][i] = bit
                reserved[6][i] = true
            }
            if (!reserved[i][6]) {
                matrix[i][6] = bit
                reserved[i][6] = true
            }
        }

        // Dark module
        matrix[matrixSize - 8][8] = true
        reserved[matrixSize - 8][8] = true

        // Generate data stream using SHA-256 of payload repeated
        val md = MessageDigest.getInstance("SHA-256")
        val hash = md.digest(payload.toByteArray(Charsets.UTF_8))
        val extendedBytes = ByteArray(128)
        for (i in extendedBytes.indices) {
            extendedBytes[i] = (hash[i % hash.size].toInt() xor (i * 31 + payload.length)).toByte()
        }

        var bitIndex = 0
        for (r in 0 until matrixSize) {
            for (c in 0 until matrixSize) {
                if (!reserved[r][c]) {
                    val byteVal = extendedBytes[(bitIndex / 8) % extendedBytes.size].toInt() and 0xFF
                    val bitVal = (byteVal shr (bitIndex % 8)) and 1
                    // Apply standard mask pattern (row + col) % 2 == 0
                    val mask = ((r + c) % 2 == 0)
                    matrix[r][c] = (bitVal == 1) xor mask
                    bitIndex++
                }
            }
        }

        // Draw onto bitmap
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // White background with quiet zone
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, sizePx.toFloat(), sizePx.toFloat(), paint)

        val quietZone = 2
        val totalCells = matrixSize + quietZone * 2
        val cellSize = sizePx.toFloat() / totalCells

        paint.color = Color.rgb(15, 23, 42) // Deep Slate / Dark Navy for clean contrast
        for (r in 0 until matrixSize) {
            for (c in 0 until matrixSize) {
                if (matrix[r][c]) {
                    val left = (c + quietZone) * cellSize
                    val top = (r + quietZone) * cellSize
                    // Render slightly rounded modules for modern look
                    canvas.drawRoundRect(
                        RectF(left, top, left + cellSize, top + cellSize),
                        cellSize * 0.2f,
                        cellSize * 0.2f,
                        paint
                    )
                }
            }
        }

        // Center badge for BHIM UPI branding
        val centerBadgeSize = cellSize * 5.5f
        val centerLeft = (sizePx - centerBadgeSize) / 2f
        val centerTop = (sizePx - centerBadgeSize) / 2f

        val bgBadgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            RectF(centerLeft, centerTop, centerLeft + centerBadgeSize, centerTop + centerBadgeSize),
            12f,
            12f,
            bgBadgePaint
        )

        val borderBadgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(2, 119, 189) // AC Blue
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(
            RectF(centerLeft, centerTop, centerLeft + centerBadgeSize, centerTop + centerBadgeSize),
            12f,
            12f,
            borderBadgePaint
        )

        // Draw UPI symbol / text in badge
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(2, 119, 189)
            textSize = centerBadgeSize * 0.45f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        val fontMetrics = textPaint.fontMetrics
        val textY = centerTop + (centerBadgeSize / 2f) - ((fontMetrics.ascent + fontMetrics.descent) / 2f)
        canvas.drawText("UPI", sizePx / 2f, textY, textPaint)

        return bitmap
    }
}
