package com.example.record.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class Waveform(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var paint = Paint()
    var amplitudes: ArrayList<Float> = ArrayList()
    var spikes: ArrayList<RectF> = ArrayList()
    private var radius = 6f
    private var w = 6f
    private var d = 6f
    private var sw = 0f
    private var sh = 400f
    private var maxSpikes = 0

    init {
        paint.color = Color.rgb(244, 81, 30)
        sw = resources.displayMetrics.widthPixels.toFloat()
        maxSpikes = (sw / (w + d)).toInt()
    }

    fun addAmplitude(amp: Float) {
        amplitudes.add(Math.min((amp.toInt() / 7), (200..400).random()).toFloat())
        spikes.clear()
        var amps = amplitudes.takeLast(maxSpikes)
        for (i: Int in amps.indices) {
            var l = sw - i * (w + d)
            var t = sh / 2 - amps[i] / 2
            var r = l + w
            var b = t + amps[i]
            spikes.add(RectF(l, t, r, b))
        }

        invalidate()
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        spikes.forEach {
            canvas?.drawRoundRect(it, radius, radius, paint)
        }
    }
}