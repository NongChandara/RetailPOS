package com.example.printer

import android.graphics.Bitmap
import android.graphics.Color
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

/**
 * High-performance ESC/POS byte command builder for 58mm and 80mm thermal receipt printers.
 * Implements standard ESC/POS commands, two-column justification, line dividers,
 * cash drawer kick, cut paper, buzzer alert, and raster bitmap printing.
 */
class EscPosCommandBuilder(
    val paperWidth: PrinterPaperWidth = PrinterPaperWidth.MM_80,
    private val charset: Charset = Charsets.UTF_8
) {
    private val output = ByteArrayOutputStream()

    val maxColumns: Int
        get() = paperWidth.charWidth

    fun reset(): EscPosCommandBuilder {
        output.reset()
        // ESC @: Initialize printer
        output.write(byteArrayOf(0x1B, 0x40))
        return this
    }

    fun alignLeft(): EscPosCommandBuilder {
        // ESC a 0
        output.write(byteArrayOf(0x1B, 0x61, 0x00))
        return this
    }

    fun alignCenter(): EscPosCommandBuilder {
        // ESC a 1
        output.write(byteArrayOf(0x1B, 0x61, 0x01))
        return this
    }

    fun alignRight(): EscPosCommandBuilder {
        // ESC a 2
        output.write(byteArrayOf(0x1B, 0x61, 0x02))
        return this
    }

    fun bold(enable: Boolean): EscPosCommandBuilder {
        // ESC E n
        output.write(byteArrayOf(0x1B, 0x45, if (enable) 0x01 else 0x00))
        return this
    }

    fun underline(enable: Boolean): EscPosCommandBuilder {
        // ESC - n
        output.write(byteArrayOf(0x1B, 0x2D, if (enable) 0x01 else 0x00))
        return this
    }

    fun invert(enable: Boolean): EscPosCommandBuilder {
        // GS B n (white-on-black reverse print)
        output.write(byteArrayOf(0x1D, 0x42, if (enable) 0x01 else 0x00))
        return this
    }

    fun textNormal(): EscPosCommandBuilder {
        // GS ! 0
        output.write(byteArrayOf(0x1D, 0x21, 0x00))
        bold(false)
        invert(false)
        underline(false)
        return this
    }

    fun textDoubleSize(): EscPosCommandBuilder {
        // GS ! 0x11 (Double width + Double height)
        output.write(byteArrayOf(0x1D, 0x21, 0x11))
        return this
    }

    fun textDoubleHeight(): EscPosCommandBuilder {
        // GS ! 0x01 (Normal width + Double height)
        output.write(byteArrayOf(0x1D, 0x21, 0x01))
        return this
    }

    fun textDoubleWidth(): EscPosCommandBuilder {
        // GS ! 0x10 (Double width + Normal height)
        output.write(byteArrayOf(0x1D, 0x21, 0x10))
        return this
    }

    fun text(text: String): EscPosCommandBuilder {
        output.write(text.toByteArray(charset))
        return this
    }

    fun line(text: String = ""): EscPosCommandBuilder {
        if (text.isNotEmpty()) {
            output.write(text.toByteArray(charset))
        }
        output.write(0x0A) // LF
        return this
    }

    fun feed(lines: Int = 1): EscPosCommandBuilder {
        if (lines <= 0) return this
        // ESC d n
        output.write(byteArrayOf(0x1B, 0x64, lines.coerceIn(1, 10).toByte()))
        return this
    }

    fun divider(char: Char = '-'): EscPosCommandBuilder {
        val s = char.toString().repeat(maxColumns)
        line(s)
        return this
    }

    fun doubleDivider(): EscPosCommandBuilder {
        return divider('=')
    }

    fun dashedDivider(): EscPosCommandBuilder {
        return divider('-')
    }

    /**
     * Prints a row with [left] aligned to the left and [right] aligned to the right.
     * Truncates or wraps gracefully if the combined width exceeds [maxColumns].
     */
    fun twoColumns(left: String, right: String): EscPosCommandBuilder {
        val rightLen = right.length
        val availableForLeft = maxColumns - rightLen - 1
        val safeLeft = if (left.length > availableForLeft) {
            left.take((availableForLeft - 1).coerceAtLeast(1)) + "…"
        } else {
            left
        }
        val spacesCount = (maxColumns - safeLeft.length - rightLen).coerceAtLeast(1)
        val lineStr = safeLeft + " ".repeat(spacesCount) + right
        line(lineStr)
        return this
    }

    /**
     * Formats 3 columns (e.g. Qty, Item Name, Total).
     */
    fun threeColumns(qtyCol: String, nameCol: String, priceCol: String): EscPosCommandBuilder {
        val qtyWidth = 4
        val priceWidth = if (paperWidth == PrinterPaperWidth.MM_58) 9 else 12
        val nameWidth = (maxColumns - qtyWidth - priceWidth).coerceAtLeast(8)

        val paddedQty = qtyCol.padEnd(qtyWidth)
        val safeName = if (nameCol.length > nameWidth) {
            nameCol.take(nameWidth - 1) + "…"
        } else {
            nameCol.padEnd(nameWidth)
        }
        val paddedPrice = priceCol.padStart(priceWidth)

        line("$paddedQty$safeName$paddedPrice")
        return this
    }

    /**
     * Kicks open standard receipt printer RJ-11 cash drawer (Pin 2 and Pin 5 pulse).
     */
    fun openCashDrawer(): EscPosCommandBuilder {
        // ESC p 0 25 250 (Pin 2, t1=50ms, t2=500ms)
        output.write(byteArrayOf(0x1B, 0x70, 0x00, 0x19, 0xFA.toByte()))
        // ESC p 1 25 250 (Pin 5)
        output.write(byteArrayOf(0x1B, 0x70, 0x01, 0x19, 0xFA.toByte()))
        return this
    }

    /**
     * Beeper / Buzzer alert for kitchen orders.
     */
    fun beepBuzzer(): EscPosCommandBuilder {
        // ESC B 2 2 (2 beeps)
        output.write(byteArrayOf(0x1B, 0x42, 0x02, 0x02))
        return this
    }

    /**
     * Feeds paper and performs full or partial cut.
     */
    fun cutPaper(fullCut: Boolean = false): EscPosCommandBuilder {
        feed(3)
        // GS V m
        if (fullCut) {
            output.write(byteArrayOf(0x1D, 0x56, 0x00)) // Full cut
        } else {
            output.write(byteArrayOf(0x1D, 0x56, 0x01)) // Partial cut
        }
        return this
    }

    /**
     * Prints QR code using standard ESC/POS GS ( k commands.
     */
    fun qrCode(data: String, moduleSize: Int = 4): EscPosCommandBuilder {
        val bytes = data.toByteArray(Charsets.ISO_8859_1)
        val len = bytes.size + 3
        val pL = (len and 0xFF).toByte()
        val pH = ((len shr 8) and 0xFF).toByte()

        // 1. Set QR Model 2
        output.write(byteArrayOf(0x1D, 0x28, 0x6B, 0x04, 0x00, 0x31, 0x41, 0x32, 0x00))

        // 2. Set Module Size (1 to 8)
        val size = moduleSize.coerceIn(2, 8).toByte()
        output.write(byteArrayOf(0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x43, size))

        // 3. Set Error Correction Level M (0x31) or L (0x30)
        output.write(byteArrayOf(0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x45, 0x31))

        // 4. Store Data in QR Symbol Storage Area
        output.write(byteArrayOf(0x1D, 0x28, 0x6B, pL, pH, 0x31, 0x50, 0x30))
        output.write(bytes)

        // 5. Print the Symbol Data in the Symbol Storage Area
        output.write(byteArrayOf(0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x30))
        output.write(0x0A)
        return this
    }

    /**
     * Converts an Android Bitmap into an ESC/POS monochrome raster bit image (GS v 0).
     * This ensures any custom graphic, logo, or Khmer text prints crisply on any thermal printer.
     */
    fun rasterBitmap(bitmap: Bitmap, targetWidth: Int = paperWidth.dotWidth): EscPosCommandBuilder {
        val width = bitmap.width
        val height = bitmap.height

        // Calculate scale to fit targetWidth
        val scale = targetWidth.toFloat() / width
        val finalWidth = targetWidth
        val finalHeight = (height * scale).toInt()

        val scaled = Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)
        val widthBytes = (finalWidth + 7) / 8

        // GS v 0 m xL xH yL yH
        val xL = (widthBytes and 0xFF).toByte()
        val xH = ((widthBytes shr 8) and 0xFF).toByte()
        val yL = (finalHeight and 0xFF).toByte()
        val yH = ((finalHeight shr 8) and 0xFF).toByte()

        output.write(byteArrayOf(0x1D, 0x76, 0x30, 0x00, xL, xH, yL, yH))

        val pixels = IntArray(finalWidth * finalHeight)
        scaled.getPixels(pixels, 0, finalWidth, 0, 0, finalWidth, finalHeight)

        for (y in 0 until finalHeight) {
            for (xByte in 0 until widthBytes) {
                var byteVal = 0
                for (bit in 0 until 8) {
                    val x = xByte * 8 + bit
                    if (x < finalWidth) {
                        val pixel = pixels[y * finalWidth + x]
                        val r = Color.red(pixel)
                        val g = Color.green(pixel)
                        val b = Color.blue(pixel)
                        val luminance = (0.299 * r + 0.587 * g + 0.114 * b)
                        if (luminance < 160) {
                            byteVal = byteVal or (1 shl (7 - bit))
                        }
                    }
                }
                output.write(byteVal)
            }
        }
        if (scaled != bitmap) {
            scaled.recycle()
        }
        output.write(0x0A)
        return this
    }

    fun build(): ByteArray {
        return output.toByteArray()
    }
}
