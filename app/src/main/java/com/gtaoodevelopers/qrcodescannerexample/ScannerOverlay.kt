package com.gtaoodevelopers.qrcodescannerexample

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

class ScannerOverlay(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    // Red square border paint
    private val squarePaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
    }

    // Dark mask outside square
    private val maskPaint = Paint().apply {
        color = Color.parseColor("#AA000000") // semi-transparent black
    }

    private val square = Rect()
    private var size: Int = 0

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Make square 70% of screen width
        size = (w * 0.7).toInt()

        val left = (w - size) / 2
        val top = (h - size) / 2
        val right = left + size
        val bottom = top + size

        square.set(left, top, right, bottom)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Darken outside of square
        canvas.drawRect(0f, 0f, width.toFloat(), square.top.toFloat(), maskPaint)
        canvas.drawRect(0f, square.bottom.toFloat(), width.toFloat(), height.toFloat(), maskPaint)
        canvas.drawRect(0f, square.top.toFloat(), square.left.toFloat(), square.bottom.toFloat(), maskPaint)
        canvas.drawRect(square.right.toFloat(), square.top.toFloat(), width.toFloat(), square.bottom.toFloat(), maskPaint)

        // Draw the red scanning square
        canvas.drawRect(square, squarePaint)
    }
}
