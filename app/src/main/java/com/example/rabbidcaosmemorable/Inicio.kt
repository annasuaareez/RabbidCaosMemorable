package com.example.rabbidcaosmemorable

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton

class Inicio : AppCompatActivity() {

    private lateinit var musicController: MusicController
    private lateinit var soundController: SoundController

    private val MY_APP_PREFERENCES = "MyAppPreferences"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio)

        musicController = MusicController(this)
        soundController = SoundController(this)

        val pref = getSharedPreferences(MY_APP_PREFERENCES, Context.MODE_PRIVATE)

        val playButton: AppCompatButton = findViewById(R.id.btnPlay)
        val configuracionButton: ImageButton = findViewById(R.id.btnConfiguracion)

        playButton.setOnClickListener {
            val intent = Intent(this@Inicio, MainActivity::class.java)
            intent.putExtra(musicController.MUSIC_STATE_PREFERENCE_KEY, musicController.isMusicOn())
            intent.putExtra(soundController.SOUND_STATE_PREFERENCE_KEY, soundController.isSoundOn())
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }

        configuracionButton.setOnClickListener {
            soundController.playClickSound()
            mostrarDialogoConfiguracion(pref)
        }

        // Cargar y aplicar el estado del sonido almacenado en las preferencias compartidas
        soundController.setSoundOn(pref.getBoolean(soundController.SOUND_STATE_PREFERENCE_KEY, true))
    }

    override fun onPause() {
        super.onPause()
        musicController.pauseMusic()
        musicController.saveMusicState(getSharedPreferences(MY_APP_PREFERENCES, Context.MODE_PRIVATE))
        soundController.saveSoundState(getSharedPreferences(MY_APP_PREFERENCES, Context.MODE_PRIVATE))
        soundController.stopAllSounds()
    }

    override fun onResume() {
        super.onResume()
        musicController.startMusic()
        if (!soundController.isSoundOn()) {
            soundController.stopAllSounds()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        musicController.saveMusicState(getSharedPreferences(MY_APP_PREFERENCES, Context.MODE_PRIVATE))
        musicController.releaseMediaPlayer()
    }

    private fun mostrarDialogoConfiguracion(pref: SharedPreferences) {
        val width = resources.getDimensionPixelSize(R.dimen.dialog_width)
        val height = resources.getDimensionPixelSize(R.dimen.dialog_height)

        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_configuracion, null)

        val btnMusicDialog: ImageView = dialogView.findViewById(R.id.btnMusic)
        val btnSoundDialog: ImageView = dialogView.findViewById(R.id.btnSound)

        val dialog = builder.create().apply {
            window?.setLayout(width, height)
            window?.setBackgroundDrawableResource(R.color.colorFondo)
            setView(dialogView)
            setCancelable(false)
            show()
        }

        val btnCerrar: ImageView = dialogView.findViewById(R.id.btnCerrar)

        updateMusicButtonState(btnMusicDialog)

        updateSoundButtonState(btnSoundDialog)

        btnSoundDialog.setOnClickListener {
            soundController.toggleSound(btnSoundDialog, pref)
        }

        btnMusicDialog.setOnClickListener {
            musicController.toggleMusic(btnMusicDialog, pref)
            soundController.playClickSound()
        }

        btnCerrar.setOnClickListener {
            soundController.playClickSound()
            // Cerrar el diálogo al hacer clic en la cruz
            dialog?.dismiss()
        }
    }

    private fun updateMusicButtonState(btnMusic: ImageView) {
        musicController.updateMusicButtonState(btnMusic)
    }

    private fun updateSoundButtonState(btnSound: ImageView) {
        soundController.updateSoundButtonState(btnSound, soundController.getSoundOn())
    }

}