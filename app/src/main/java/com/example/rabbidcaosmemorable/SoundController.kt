package com.example.rabbidcaosmemorable

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.widget.ImageView

class SoundController(private val context: Context) {

    private var mediaPlayerClick: MediaPlayer = MediaPlayer.create(context, R.raw.click2)
    private var mediaPlayerGoodJob: MediaPlayer = MediaPlayer.create(context, R.raw.good)
    private var mediaPlayerFallo: MediaPlayer = MediaPlayer.create(context, R.raw.error)

    val SOUND_STATE_PREFERENCE_KEY = "SoundState"

    private var soundOn: Boolean = true

    fun getSoundOn(): Boolean {
        return soundOn
    }

    fun setSoundOn(isSoundOn: Boolean) {
        soundOn = isSoundOn
    }

    fun saveSoundState(preferences: SharedPreferences) {
        preferences.edit()
            .putBoolean(SOUND_STATE_PREFERENCE_KEY, soundOn)
            .apply()
    }

    fun stopAllSounds() {
        mediaPlayerClick.pause()
        mediaPlayerClick.seekTo(0)
        mediaPlayerGoodJob.pause()
        mediaPlayerGoodJob.seekTo(0)
        mediaPlayerFallo.pause()
        mediaPlayerFallo.seekTo(0)

        // Liberar recursos de los MediaPlayers
        mediaPlayerClick.release()
        mediaPlayerGoodJob.release()
        mediaPlayerFallo.release()

        // Volver a crear los MediaPlayers para evitar problemas de estado
        mediaPlayerClick = MediaPlayer.create(context, R.raw.click2)
        mediaPlayerGoodJob = MediaPlayer.create(context, R.raw.good)
        mediaPlayerFallo = MediaPlayer.create(context, R.raw.error)
    }

    fun updateSoundButtonState(btnSound: ImageView, soundOn: Boolean) {
        if (soundOn) {
            btnSound.setImageResource(R.drawable.sound_on)
        } else {
            btnSound.setImageResource(R.drawable.sound_off)
            stopAllSounds()
        }
    }

    fun toggleSound(btnSound: ImageView, preferences: SharedPreferences) {
        soundOn = !soundOn
        updateSoundButtonState(btnSound, soundOn)

        // Guarda el estado actual del sonido en SharedPreferences
        preferences.edit().putBoolean(SOUND_STATE_PREFERENCE_KEY, soundOn).apply()
    }

    fun isSoundOn(): Boolean {
        return soundOn
    }

    fun playClickSound() {
        if (soundOn) {
            mediaPlayerClick.release()
            mediaPlayerClick = MediaPlayer.create(context, R.raw.click2)
            mediaPlayerClick.start()
        }
    }

    fun playGoodJobSound() {
        if (soundOn) {
            mediaPlayerGoodJob.release()
            mediaPlayerGoodJob = MediaPlayer.create(context, R.raw.good)
            mediaPlayerGoodJob.start()
        }
    }

    fun playFalloSound() {
        if (soundOn) {
            mediaPlayerFallo.release()
            mediaPlayerFallo = MediaPlayer.create(context, R.raw.error)
            mediaPlayerFallo.start()
        }
    }
}