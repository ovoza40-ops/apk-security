package com.example.securityguard

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity

class HackerActivity : AppCompatActivity() {

    private var vibrator: Vibrator? = null
    private var isAlarmPlaying = true
    private val handler = Handler(Looper.getMainLooper())

    private val unlockRunnable = Runnable {
        unlockAndExit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )

        hideSystemUI()

        try {
            startLockTask()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        setContentView(webView)

        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body { background: #000; color: #ff0000; font-family: monospace; text-align: center; padding-top: 25%; margin: 0; }
                .alert-card { border: 3px solid red; padding: 20px; margin: 15px; box-shadow: 0 0 30px red; animation: pulse 0.8s infinite; }
                @keyframes pulse { 50% { opacity: 0.3; } }
                h1 { font-size: 20px; letter-spacing: 1px; }
                p { font-size: 13px; color: #fff; }
                .locked { color: yellow; font-size: 13px; margin-top: 15px; font-weight: bold; }
                .code { color: #00ff00; font-size: 11px; text-align: left; margin-top: 15px; background: rgba(0,255,0,0.1); padding: 8px; }
            </style>
            </head>
            <body>
                <div class="alert-card">
                    <h1>⚠️ TIZIM TO'LIQ BLOKLANDI</h1>
                    <p>Ruxsatsiz begona dastur ochildi.</p>
                    <p class="locked">[ BARCHA TUGMALAR VA SENSIYA O'CHIRILDI ]</p>
                    <div class="code">
                        > Xavfsizlik protokoli: ACTIVE<br>
                        > Qurilma holati: QUFLLANGAN<br>
                        > IMEI va Yuz tasviri: Yuborildi<br>
                        > Begona ilova aniqlandi
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()

        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null)

        startVibration()
        startSiren()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (event.action == KeyEvent.ACTION_DOWN) {
                if (event.repeatCount == 0) {
                    handler.postDelayed(unlockRunnable, 2000)
                }
                return true
            } else if (event.action == KeyEvent.ACTION_UP) {
                handler.removeCallbacks(unlockRunnable)
                return true
            }
        }
        return true
    }

    private fun unlockAndExit() {
        isAlarmPlaying = false
        vibrator?.cancel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
        }

        try {
            stopLockTask()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        finish()
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        )
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    private fun startVibration() {
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(0, 500, 150, 500, 150)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun startSiren() {
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val targetVolume = (maxVolume * 0.80).toInt()
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0)

            Thread {
                val sampleRate = 44100
                val numSamples = sampleRate / 2
                val sample = DoubleArray(numSamples)
                val generatedSnd = ByteArray(2 * numSamples)

                for (i in 0 until numSamples) {
                    val freq = 700.0 + (i % 2000)
                    sample[i] = Math.sin(2 * Math.PI * i / (sampleRate / freq)) * 0.8
                }
                var idx = 0
                for (dVal in sample) {
                    val valShort = (dVal * 32767).toInt().toShort()
                    generatedSnd[idx++] = (valShort.toInt() and 0x00ff).toByte()
                    generatedSnd[idx++] = (valShort.toInt() shr 8 and 0x00ff).toByte()
                }

                val audioTrack = AudioTrack(
                    AudioManager.STREAM_MUSIC, sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                    generatedSnd.size, AudioTrack.MODE_STREAM
                )
                audioTrack.play()

                while (isAlarmPlaying) {
                    audioTrack.write(generatedSnd, 0, generatedSnd.size)
                }
                audioTrack.stop()
                audioTrack.release()
            }.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {}

    override fun onDestroy() {
        super.onDestroy()
        isAlarmPlaying = false
        vibrator?.cancel()
        handler.removeCallbacks(unlockRunnable)
    }
}
