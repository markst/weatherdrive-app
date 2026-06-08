package com.weatherdrive.ui.components

import coil3.size.Size
import coil3.transform.Transformation
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skia.impl.use

actual fun blurTransformation(radius: Float): Transformation = IosBlurTransformation(radius)

private class IosBlurTransformation(private val radius: Float) : Transformation() {
    override val cacheKey = "BlurTransformation($radius)"

    override suspend fun transform(input: coil3.Bitmap, size: Size): coil3.Bitmap {
        val output = org.jetbrains.skia.Bitmap().apply {
            allocN32Pixels(input.width, input.height)
        }
        val canvas = Canvas(output)
        val image = Image.makeFromBitmap(input)
        val src = Rect.makeWH(input.width.toFloat(), input.height.toFloat())
        val dst = Rect.makeWH(output.width.toFloat(), output.height.toFloat())
        Paint().use { paint ->
            paint.imageFilter = ImageFilter.makeBlur(radius, radius, FilterTileMode.CLAMP)
            canvas.drawImageRect(image, src, dst, paint)
        }
        image.close()
        return output
    }
}
