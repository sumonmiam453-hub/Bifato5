package com.example.util

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper

object SoundManager {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        } catch (e: Exception) {
            toneGenerator = null
        }
    }

    // Facebook-like Like / Reaction Sound (Bright Double Beep)
    fun playLikeSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Facebook-like Reaction Pop-up Sound (When holding like button)
    fun playReactionPopupSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_PIP, 40)
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_CDMA_PIP, 40)
                } catch (_: Exception) {}
            }, 50)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Facebook-like Comment Sound (Crisp Chime)
    fun playCommentSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 100)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Facebook-like Upload Sound (Ascending Melody Tone)
    fun playUploadSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 120)
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 100)
                } catch (_: Exception) {}
            }, 100)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Facebook-like Story Reaction / Heart Sound
    fun playStoryReactionSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 70)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Subtle Button / Menu Click Sound
    fun playClickSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 40)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Facebook-like Refresh Sound (crisp pop tone)
    fun playRefreshSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 80)
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 70)
                } catch (_: Exception) {}
            }, 80)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Message sent / post success sound
    fun playPostSuccessSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 90)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
