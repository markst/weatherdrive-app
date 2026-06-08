package com.weatherdrive.ui.components

import android.graphics.Bitmap
import coil3.size.Size
import coil3.transform.Transformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

actual fun blurTransformation(radius: Float): Transformation = AndroidBlurTransformation(radius)

private class AndroidBlurTransformation(private val radius: Float) : Transformation() {
    override val cacheKey = "BlurTransformation($radius)"

    override suspend fun transform(input: coil3.Bitmap, size: Size): coil3.Bitmap =
        withContext(Dispatchers.IO) {
            val mutable = input.copy(Bitmap.Config.ARGB_8888, true)
            stackBlur(mutable, radius.toInt().coerceIn(1, 200))
            mutable
        }

    private fun stackBlur(bitmap: Bitmap, radius: Int) {
        val w = bitmap.width
        val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        val wm = w - 1
        val hm = h - 1
        val div = radius + radius + 1
        val r = IntArray(w * h)
        val g = IntArray(w * h)
        val b = IntArray(w * h)
        val divsum = (radius + 1) * (radius + 1)
        val dv = IntArray(256 * divsum) { it / divsum }
        val stack = Array(div) { IntArray(3) }

        // Horizontal pass
        for (y in 0 until h) {
            val yOff = y * w
            var rsum = 0; var gsum = 0; var bsum = 0
            var routsum = 0; var goutsum = 0; var boutsum = 0
            var rinsum = 0; var ginsum = 0; var binsum = 0

            for (i in -radius..radius) {
                val p = pixels[yOff + minOf(wm, maxOf(i, 0))]
                val sir = stack[i + radius]
                sir[0] = (p shr 16) and 0xff
                sir[1] = (p shr 8) and 0xff
                sir[2] = p and 0xff
                val rbs = radius + 1 - abs(i)
                rsum += sir[0] * rbs; gsum += sir[1] * rbs; bsum += sir[2] * rbs
                if (i > 0) { rinsum += sir[0]; ginsum += sir[1]; binsum += sir[2] }
                else { routsum += sir[0]; goutsum += sir[1]; boutsum += sir[2] }
            }
            var sp = radius
            for (x in 0 until w) {
                r[yOff + x] = dv[rsum]; g[yOff + x] = dv[gsum]; b[yOff + x] = dv[bsum]
                rsum -= routsum; gsum -= goutsum; bsum -= boutsum
                val ss = (sp - radius + div) % div
                val sir = stack[ss]
                routsum -= sir[0]; goutsum -= sir[1]; boutsum -= sir[2]
                val p = pixels[yOff + minOf(x + radius + 1, wm)]
                sir[0] = (p shr 16) and 0xff; sir[1] = (p shr 8) and 0xff; sir[2] = p and 0xff
                rinsum += sir[0]; ginsum += sir[1]; binsum += sir[2]
                rsum += rinsum; gsum += ginsum; bsum += binsum
                sp = (sp + 1) % div
                val sn = stack[sp]
                routsum += sn[0]; goutsum += sn[1]; boutsum += sn[2]
                rinsum -= sn[0]; ginsum -= sn[1]; binsum -= sn[2]
            }
        }

        // Vertical pass
        for (x in 0 until w) {
            var rsum = 0; var gsum = 0; var bsum = 0
            var routsum = 0; var goutsum = 0; var boutsum = 0
            var rinsum = 0; var ginsum = 0; var binsum = 0

            for (i in -radius..radius) {
                val row = minOf(hm, maxOf(i, 0))
                val sir = stack[i + radius]
                sir[0] = r[row * w + x]; sir[1] = g[row * w + x]; sir[2] = b[row * w + x]
                val rbs = radius + 1 - abs(i)
                rsum += sir[0] * rbs; gsum += sir[1] * rbs; bsum += sir[2] * rbs
                if (i > 0) { rinsum += sir[0]; ginsum += sir[1]; binsum += sir[2] }
                else { routsum += sir[0]; goutsum += sir[1]; boutsum += sir[2] }
            }
            var sp = radius
            for (y in 0 until h) {
                pixels[y * w + x] = 0xff000000.toInt() or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]
                rsum -= routsum; gsum -= goutsum; bsum -= boutsum
                val ss = (sp - radius + div) % div
                val sir = stack[ss]
                routsum -= sir[0]; goutsum -= sir[1]; boutsum -= sir[2]
                val nextRow = minOf(y + radius + 1, hm)
                sir[0] = r[nextRow * w + x]; sir[1] = g[nextRow * w + x]; sir[2] = b[nextRow * w + x]
                rinsum += sir[0]; ginsum += sir[1]; binsum += sir[2]
                rsum += rinsum; gsum += ginsum; bsum += binsum
                sp = (sp + 1) % div
                val sn = stack[sp]
                routsum += sn[0]; goutsum += sn[1]; boutsum += sn[2]
                rinsum -= sn[0]; ginsum -= sn[1]; binsum -= sn[2]
            }
        }

        bitmap.setPixels(pixels, 0, w, 0, 0, w, h)
    }
}
