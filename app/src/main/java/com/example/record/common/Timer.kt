package com.example.record.common

import android.os.Handler
import android.os.Looper

class Timer(listener:onTimerClickListener) {

    interface onTimerClickListener{
        fun onTimeClick(duration: String)
    }

    private var handler:Handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    private var duration = 0L
    var delay = 100L

    init{
        runnable = Runnable {
            duration += delay
            handler.postDelayed(runnable, delay)
            listener.onTimeClick(format())
        }
    }

    fun start(){
        handler.postDelayed(runnable, delay)
    }

    fun pause(){
        handler.removeCallbacks(runnable)
    }

    fun stop(){
        handler.removeCallbacks(runnable)
        duration = 0L
    }

    private fun format(): String {
        val millis = (duration % 1000)
        val seconds = (duration / 1000) % 60
        val minutes = (duration / (1000 * 60)) % 60
        val hours = (duration / (1000 * 60 * 60))

        return if(hours == 0L) {
            "%02d:%02d.%02d".format(minutes, seconds, millis / 10)
        } else {
            "%02d:%02d:%02d".format(hours, minutes, seconds)
        }
    }
}