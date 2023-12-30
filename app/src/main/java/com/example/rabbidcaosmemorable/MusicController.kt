
package com.example.rabbidcaosmemorable

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.widget.ImageView

class MusicController(context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var musicOn: Boolean = true

    val MY_APP_PREFERENCES = "MyAppPreferences"
    val MUSIC_STATE_PREFERENCE_KEY = "music_state"
    val MUSIC_BUTTON_STATE_KEY = "music_button_state"

    init {
        mediaPlayer = MediaPlayer.create(context, R.raw.musica_fondo)
        mediaPlayer?.isLooping = true

        // Ajuste el volumen al 50%
        mediaPlayer?.setVolume(0.3f, 0.3f)

        val pref = context.getSharedPreferences(MY_APP_PREFERENCES, Context.MODE_PRIVATE)
        musicOn = pref.getBoolean(MUSIC_STATE_PREFERENCE_KEY, false)
    }

    fun startMusic() {
        if (musicOn && !mediaPlayer?.isPlaying!!) {
            mediaPlayer?.start()
        }
    }

    fun pauseMusic() {
        mediaPlayer?.pause()
    }

    fun toggleMusic(btnMusic: ImageView, pref: SharedPreferences) {
        if (musicOn) {
            mediaPlayer?.pause()
            mediaPlayer?.seekTo(0)
            updateMusicButtonState(btnMusic)
            pref.edit().putBoolean(MUSIC_BUTTON_STATE_KEY, false).apply()
        } else {
            mediaPlayer?.start()
            updateMusicButtonState(btnMusic)
            pref.edit().putBoolean(MUSIC_BUTTON_STATE_KEY, true).apply()
        }

        musicOn = !musicOn
        updateMusicButtonState(btnMusic)
        pref.edit().putBoolean(MUSIC_BUTTON_STATE_KEY, musicOn).apply()
        pref.edit().putBoolean(MUSIC_STATE_PREFERENCE_KEY, musicOn).apply()
    }

    fun updateMusicButtonState(btnMusic: ImageView) {
        if (musicOn) {
            btnMusic.setImageResource(R.drawable.music_on)
        } else {
            btnMusic.setImageResource(R.drawable.music_off)
        }
    }

    fun saveMusicState(pref: SharedPreferences) {
        pref.edit().putBoolean(MUSIC_STATE_PREFERENCE_KEY, musicOn).apply()
        pref.edit().putBoolean(MUSIC_BUTTON_STATE_KEY, musicOn).apply()
    }

    fun releaseMediaPlayer() {
        mediaPlayer?.release()
    }

    fun isMusicOn(): Boolean {
        return musicOn
    }

}
