package com.arslan.shizuwall.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.arslan.shizuwall.R

class StarFieldView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val star: Drawable? =
        ContextCompat.getDrawable(context, R.drawable.ic_picon_star)?.mutate()

    private val density = resources.displayMetrics.density

    private val xs = listOf(0.04f, 0.13f, 0.22f, 0.31f, 0.40f, 0.49f, 0.58f, 0.67f)

    private val ys = listOf(0.20f, 0.74f, 0.30f, 0.84f, 0.18f, 0.70f, 0.26f, 0.80f)

    private val sizes = listOf(16f, 22f, 13f, 19f, 24f, 14f, 20f, 17f)

    private val angles = listOf(-18f, 12f, 40f, -8f, 26f, -34f, 6f, 20f)

    var starColor: Int = 0
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }

    override fun onDraw(canvas: Canvas) {
        val drawable = star ?: return
        if (width == 0 || height == 0) return
        xs.forEachIndexed { index, x ->
            val size = sizes[index] * density
            val half = size / 2f
            val cx = x * width
            val cy = ys[index] * height
            val alpha = if (x >= TEXT_ZONE_START) DIM_ALPHA else FULL_ALPHA
            drawable.setBounds(0, 0, size.toInt(), size.toInt())
            drawable.setTint(ColorUtils.setAlphaComponent(starColor, alpha))
            canvas.save()
            canvas.translate(cx - half, cy - half)
            canvas.rotate(angles[index], half, half)
            drawable.draw(canvas)
            canvas.restore()
        }
    }

    private companion object {
        const val TEXT_ZONE_START = 0.16f
        const val FULL_ALPHA = 92
        const val DIM_ALPHA = 34
    }
}
