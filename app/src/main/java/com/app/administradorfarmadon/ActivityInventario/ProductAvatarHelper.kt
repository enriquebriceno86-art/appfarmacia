package com.app.administradorfarmadon.ActivityInventario

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.core.graphics.ColorUtils
import com.bumptech.glide.Glide
import java.util.Locale
import kotlin.math.min

object ProductAvatarHelper {

    fun getProductInitial(name: String?): String {
        val trimmed = name?.trim().orEmpty()
        if (trimmed.isBlank()) return "?"
        return trimmed.first().uppercaseChar().toString()
    }

    fun createAvatarDrawable(
        context: Context,
        name: String?,
        category: String?
    ): Drawable {
        val (startColor, endColor, textColor) = resolvePalette(category)
        return ProductAvatarDrawable(
            context = context,
            initial = getProductInitial(name),
            startColor = startColor,
            endColor = endColor,
            textColor = textColor
        )
    }

    fun loadInto(
        imageView: ImageView,
        imageUrl: String?,
        productName: String?,
        category: String?,
        imageUri: Uri? = null
    ) {
        val avatar = createAvatarDrawable(imageView.context, productName, category)
        val model: Any = when {
            imageUri != null -> imageUri
            !imageUrl.isNullOrBlank() -> imageUrl.trim()
            else -> avatar
        }
        Glide.with(imageView)
            .load(model)
            .placeholder(avatar)
            .error(avatar)
            .fallback(avatar)
            .centerCrop()
            .dontAnimate()
            .into(imageView)
    }

    private fun resolvePalette(category: String?): Triple<Int, Int, Int> {
        val normalized = category
            ?.trim()
            ?.lowercase(Locale.getDefault())
            .orEmpty()

        return when {
            normalized.contains("analg") -> Triple(
                0xFFEAFBF3.toInt(),
                0xFFF3EAFE.toInt(),
                0xFF16A365.toInt()
            )

            normalized.contains("antibi") -> Triple(
                0xFFEAF4FF.toInt(),
                0xFFEAFBF3.toInt(),
                0xFF2F6FED.toInt()
            )

            normalized.contains("vitam") || normalized.contains("suplement") -> Triple(
                0xFFFFF7D6.toInt(),
                0xFFEAFBF3.toInt(),
                0xFFB7791F.toInt()
            )

            normalized.contains("cuidado") || normalized.contains("personal") || normalized.contains("cosm") -> Triple(
                0xFFFFEEF6.toInt(),
                0xFFF4EDFF.toInt(),
                0xFFC056A0.toInt()
            )

            else -> Triple(
                0xFFEAFBF3.toInt(),
                0xFFF1F2FF.toInt(),
                0xFF169B62.toInt()
            )
        }
    }
}

private class ProductAvatarDrawable(
    context: Context,
    private val initial: String,
    private val startColor: Int,
    private val endColor: Int,
    private val textColor: Int
) : Drawable() {

    private val density = context.resources.displayMetrics.density
    private val cornerRadius = 20f * density
    private val shadowOffset = 2f * density
    private val cardRect = RectF()
    private val shadowRect = RectF()

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ColorUtils.setAlphaComponent(0xFF0F172A.toInt(), 18)
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = ColorUtils.setAlphaComponent(0xFFFFFFFF.toInt(), 148)
        strokeWidth = 1f * density
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    override fun onBoundsChange(bounds: android.graphics.Rect) {
        super.onBoundsChange(bounds)
        cardRect.set(bounds.left.toFloat(), bounds.top.toFloat(), bounds.right.toFloat(), bounds.bottom.toFloat())
        shadowRect.set(
            bounds.left.toFloat(),
            bounds.top + shadowOffset,
            bounds.right.toFloat(),
            bounds.bottom + shadowOffset
        )
        backgroundPaint.shader = LinearGradient(
            cardRect.left,
            cardRect.top,
            cardRect.right,
            cardRect.bottom,
            startColor,
            endColor,
            Shader.TileMode.CLAMP
        )
        textPaint.textSize = min(cardRect.width(), cardRect.height()) * 0.44f
    }

    override fun draw(canvas: Canvas) {
        canvas.drawRoundRect(shadowRect, cornerRadius, cornerRadius, shadowPaint)
        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, backgroundPaint)
        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, borderPaint)
        val centerX = cardRect.centerX()
        val centerY = cardRect.centerY() - ((textPaint.descent() + textPaint.ascent()) / 2f)
        canvas.drawText(initial, centerX, centerY, textPaint)
    }

    override fun setAlpha(alpha: Int) {
        backgroundPaint.alpha = alpha
        textPaint.alpha = alpha
        borderPaint.alpha = alpha
        shadowPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        backgroundPaint.colorFilter = colorFilter
        textPaint.colorFilter = colorFilter
        borderPaint.colorFilter = colorFilter
        shadowPaint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
