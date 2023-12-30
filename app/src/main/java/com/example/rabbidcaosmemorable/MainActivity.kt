package com.example.rabbidcaosmemorable

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    private lateinit var musicController: MusicController
    private lateinit var soundController: SoundController
    private lateinit var mediaPlayerGoodJob: MediaPlayer
    private lateinit var mediaPlayerFallo: MediaPlayer
    private lateinit var sharedPreferencesInicio: SharedPreferences

    private val MUSIC_STATE_PREFERENCE_KEY = "music_state"

    private var selectedCards: MutableList<ImageButton> = mutableListOf()
    private var cardImages: MutableList<Int> = mutableListOf(
        R.drawable.rabbid_ahorrador, R.drawable.rabbid_ahorrador,
        R.drawable.rabbid_atleta, R.drawable.rabbid_atleta,
        R.drawable.rabbid_cavernicola, R.drawable.rabbid_cavernicola,
        R.drawable.rabbid_cocinero, R.drawable.rabbid_cocinero,
        R.drawable.rabbid_detective, R.drawable.rabbid_detective,
        R.drawable.rabbid_informatico, R.drawable.rabbid_informatico,
        R.drawable.rabbid_musico, R.drawable.rabbid_musico,
        R.drawable.rabbid_unicornio, R.drawable.rabbid_unicornio,
    )

    private val buttons: MutableList<ImageButton> by lazy {
        mutableListOf(
            findViewById(R.id.imageButton1), findViewById(R.id.imageButton2),
            findViewById(R.id.imageButton3), findViewById(R.id.imageButton4),
            findViewById(R.id.imageButton5), findViewById(R.id.imageButton6),
            findViewById(R.id.imageButton7), findViewById(R.id.imageButton8),
            findViewById(R.id.imageButton9), findViewById(R.id.imageButton10),
            findViewById(R.id.imageButton11), findViewById(R.id.imageButton12),
            findViewById(R.id.imageButton13), findViewById(R.id.imageButton14),
            findViewById(R.id.imageButton15), findViewById(R.id.imageButton16)
        )
    }

    private var currentPlayer = 1
    private var player1Score = 0
    private var player2Score = 0

    private var isDelayInProgress = false
    private var isSoundPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        musicController = MusicController(this)
        soundController = SoundController(this)
        mediaPlayerGoodJob = MediaPlayer.create(this, R.raw.good)
        mediaPlayerFallo = MediaPlayer.create(this, R.raw.error)
        sharedPreferencesInicio = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)

        val pauseButton: ImageButton = findViewById(R.id.pauseButton)

        val isMusicPlaying = sharedPreferencesInicio.getBoolean(MUSIC_STATE_PREFERENCE_KEY, false)

        // Obtén el estado del sonido del intent
        val intent = intent
        val soundState = intent.getBooleanExtra(soundController.SOUND_STATE_PREFERENCE_KEY, true)

        soundController.setSoundOn(soundState)

        pauseButton.setOnClickListener {
            soundController.playClickSound()
            showPauseConfigurationDialog()
        }

        if (isMusicPlaying) {
            musicController.startMusic()
        }

        cardImages.shuffle()

        for ((index, button) in buttons.withIndex()) {
            button.setOnClickListener {
                onCardClick(index, button)
            }
        }

        // Escala normal y ampliada para los jugadores
        val normalScale = 1.0f
        val enlargedScale = 1.11f

        // Configurar la escala inicial del jugador 1
        val imagePlayer1: ImageView = findViewById(R.id.imagePlayer1)
        imagePlayer1.scaleX = enlargedScale
        imagePlayer1.scaleY = enlargedScale

        // Configurar la escala inicial del jugador 2
        val imagePlayer2: ImageView = findViewById(R.id.imagePlayer2)
        imagePlayer2.scaleX = normalScale
        imagePlayer2.scaleY = normalScale
    }

    override fun onPause() {
        super.onPause()
        musicController.pauseMusic()
        sharedPreferencesInicio.edit()
            .putBoolean(MUSIC_STATE_PREFERENCE_KEY, musicController.isMusicOn())
            .putBoolean(soundController.SOUND_STATE_PREFERENCE_KEY, soundController.isSoundOn())
            .apply()
        soundController.stopAllSounds()
    }

    override fun onResume() {
        super.onResume()
        musicController.startMusic()
        val storedSoundState = sharedPreferencesInicio.getBoolean(soundController.SOUND_STATE_PREFERENCE_KEY, true)
        soundController.setSoundOn(storedSoundState)
        if (!soundController.isSoundOn()) {
            soundController.stopAllSounds()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        musicController.releaseMediaPlayer()
        mediaPlayerGoodJob.release()
        mediaPlayerFallo.release()
    }

    private fun showPauseConfigurationDialog() {
        val width = resources.getDimensionPixelSize(R.dimen.dialog_width)
        val height = resources.getDimensionPixelSize(R.dimen.dialog_height)

        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.diolog_pausa, null)

        val btnMusic: ImageView = dialogView.findViewById(R.id.btnMusic)
        val btnSound: ImageView = dialogView.findViewById(R.id.btnSound)
        val btnPlay: ImageView = dialogView.findViewById(R.id.btnPlay)
        val btnReplay: ImageView = dialogView.findViewById(R.id.btnReplay)

        // Obtén el estado actual del sonido desde Inicio
        val isSoundOn = sharedPreferencesInicio.getBoolean(soundController.SOUND_STATE_PREFERENCE_KEY, true)

        val dialog = builder.create().apply {
            window?.setLayout(width, height)
            window?.setBackgroundDrawableResource(R.color.colorFondo)
            setView(dialogView)
            setCancelable(false)
            show()
        }

        // Inicializa y configura btnSound aquí
        updateSoundButtonState(btnSound, isSoundOn)
        updateMusicButtonState(btnMusic)

        btnMusic.setOnClickListener {
            soundController.playClickSound()
            musicController.toggleMusic(btnMusic, sharedPreferencesInicio)
            updateMusicButtonState(btnMusic)
        }

        btnSound.setOnClickListener {
            soundController.toggleSound(btnSound, sharedPreferencesInicio)
            updateSoundButtonState(btnSound, soundController.isSoundOn())

            if (!soundController.isSoundOn()) {
                soundController.stopAllSounds()
            }
        }

        btnPlay.setOnClickListener {
            soundController.playClickSound()
            // Cerrar el diálogo al hacer clic en la cruz
            dialog?.dismiss()
        }

        btnReplay.setOnClickListener {
            soundController.playClickSound()
            // Cerrar el diálogo y reiniciar el tablero al hacer clic en Replay
            dialog?.dismiss()
            restartBoard()
        }
    }

    private fun updateMusicButtonState(btnMusic: ImageView,) {
        musicController.updateMusicButtonState(btnMusic)
    }

    private fun updateSoundButtonState(btnSound: ImageView, isSoundOn: Boolean) {
        soundController.updateSoundButtonState(btnSound, soundController.getSoundOn())
    }

    private fun onCardClick(index: Int, button: ImageButton) {
        if (isDelayInProgress || selectedCards.size >= 2 || button.tag == "flipped") {
            return
        }

        button.setImageResource(cardImages[index])
        selectedCards.add(button)
        button.tag = "flipped"

        if (selectedCards.size == 2) {
            val firstCard = selectedCards[0]
            val secondCard = selectedCards[1]

            if (cardImages[buttons.indexOf(firstCard)] == cardImages[buttons.indexOf(secondCard)]) {
                // Las cartas son iguales, suma puntos y actualiza el marcador
                if (currentPlayer == 1) {
                    player1Score++
                    findViewById<TextView>(R.id.player1Score).text = player1Score.toString()
                } else {
                    player2Score++
                    findViewById<TextView>(R.id.player2Score).text = player2Score.toString()
                }

                // Cambiar de jugador
                currentPlayer = if (currentPlayer == 1) 2 else 1

                // Actualizar la imagen del jugador actual
                updatePlayerImage()

                // Reproducir sonido de acierto
                soundController.playGoodJobSound()
            } else {
                isDelayInProgress = true

                Handler().postDelayed({
                    firstCard.setImageResource(R.drawable.back_card)
                    secondCard.setImageResource(R.drawable.back_card)
                    firstCard.tag = null
                    secondCard.tag = null

                    isDelayInProgress = false

                    for (btn in buttons) {
                        btn.isClickable = true
                        btn.isEnabled = true
                    }

                    // Cambiar de jugador
                    currentPlayer = if (currentPlayer == 1) 2 else 1

                    // Actualizar la imagen del jugador actual
                    updatePlayerImage()

                    checkGameStatus()

                    // Reproducir sonido de fallo
                    soundController.playFalloSound()
                }, 1500)

                for (btn in buttons) {
                    btn.isClickable = false
                    btn.isEnabled = false
                }
            }

            // Verificar si todas las cartas están emparejadas después de procesar el par actual
            checkGameStatus()

            selectedCards.clear()
        }
    }

    private fun updatePlayerImage() {
        val imagePlayer1 = findViewById<ImageView>(R.id.imagePlayer1)
        val imagePlayer2 = findViewById<ImageView>(R.id.imagePlayer2)

        val normalScale = 1.0f  // Tamaño normal
        val enlargedScale = 1.2f  // Tamaño ampliado

        when (currentPlayer) {
            1 -> {
                imagePlayer1.setImageResource(R.drawable.player1_playing)
                imagePlayer1.scaleX = enlargedScale
                imagePlayer1.scaleY = enlargedScale

                imagePlayer2.setImageResource(R.drawable.player2)
                imagePlayer2.scaleX = normalScale
                imagePlayer2.scaleY = normalScale
            }

            2 -> {
                imagePlayer1.setImageResource(R.drawable.player1)
                imagePlayer1.scaleX = normalScale
                imagePlayer1.scaleY = normalScale

                imagePlayer2.setImageResource(R.drawable.player2_playing)
                imagePlayer2.scaleX = enlargedScale
                imagePlayer2.scaleY = enlargedScale
            }
        }
    }

    private fun restartBoard() {
        // Reinicia las variables del juego y actualiza la interfaz de usuario según sea necesario
        player1Score = 0
        player2Score = 0
        currentPlayer = 1
        isDelayInProgress = false

        // Reinicia las imágenes de los jugadores
        updatePlayerImage()

        // Reinicia las imágenes de las cartas en el tablero
        for (btn in buttons) {
            btn.setImageResource(R.drawable.back_card)
            btn.tag = null
            btn.isClickable = true
            btn.isEnabled = true
        }

        // Vuelve a barajar las cartas
        cardImages.shuffle()

        // Actualiza los contadores a 0
        findViewById<TextView>(R.id.player1Score).text = "0"
        findViewById<TextView>(R.id.player2Score).text = "0"
    }

    private fun showWinnerDialog() {
        val width = resources.getDimensionPixelSize(R.dimen.dialog_width)
        val height = resources.getDimensionPixelSize(R.dimen.dialog_height)

        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_ganador, null)

        val ganadorTextView: TextView = dialogView.findViewById(R.id.ganador)
        val imageViewPlayer1: ImageView = dialogView.findViewById(R.id.player1)
        val imageViewPlayer2: ImageView = dialogView.findViewById(R.id.player2)
        val btnYes: Button = dialogView.findViewById(R.id.btnYes)
        val btnNo: Button = dialogView.findViewById(R.id.btnNo)

        // Determinar quién es el ganador o si hay un empate
        val winnerMessage: String
        val winnerImage: Int
        when {
            player1Score > player2Score -> {
                winnerMessage = "¡Jugador 1 gana!"
                winnerImage = R.drawable.player1
                imageViewPlayer1.visibility = View.VISIBLE
                imageViewPlayer2.visibility = View.INVISIBLE

            }
            player2Score > player1Score -> {
                winnerMessage = "¡Jugador 2 gana!"
                winnerImage = R.drawable.player2
                imageViewPlayer1.visibility = View.INVISIBLE
                imageViewPlayer2.visibility = View.VISIBLE
            }
            else -> {
                winnerMessage = "¡Empate!"
                winnerImage = 0
                imageViewPlayer1.visibility = View.VISIBLE
                imageViewPlayer2.visibility = View.VISIBLE
            }
        }

        ganadorTextView.text = winnerMessage

        if (winnerImage != 0) {
            imageViewPlayer1.setImageResource(winnerImage)
            imageViewPlayer2.setImageResource(winnerImage)
        }

        val dialog = builder.create().apply {
            window?.setLayout(width, height)
            window?.setBackgroundDrawableResource(R.color.colorFondo)
            setView(dialogView)
            setCancelable(false)
            show()
        }

        btnYes.setOnClickListener {
            restartBoard()
            dialog.dismiss()
        }

        btnNo.setOnClickListener {
            // Cierra la aplicación y elimínala de la memoria
            finishAffinity()
            exitProcess(0)
        }
    }

    private fun checkGameStatus() {
        // Verificar si todas las cartas están emparejadas
        if (isGameFinished()) {
            // Mostrar el diálogo ganador
            //println("Todas las cartas han sido emparejadas. Mostrando diálogo ganador.")
            showWinnerDialog()
        }
    }

    private fun isGameFinished(): Boolean {
        for (btn in buttons) {
            // Verifica si alguna carta no está emparejada
            if (btn.tag != "flipped") {
                return false
            }
        }
        return true
    }

}