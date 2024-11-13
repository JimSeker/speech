package edu.cs4730.text2speech_kt

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import edu.cs4730.text2speech_kt.databinding.ActivityMainBinding


/**
 * A simple example of getting text input (via a EditText)
 * and using the text to speech engine to say the words.
 */
class MainActivity : AppCompatActivity(), OnInitListener {
    private lateinit var binding: ActivityMainBinding
    private var mTts: TextToSpeech? = null
    private val myUtteranceId = "txt2spk"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }
        binding.speak.setEnabled(false)
        binding.speak.setOnClickListener {
            //Speech is simple.  send the words to speech aloud via the
            //the text to speech end and add it to the end queue. (maybe others already in line.)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                //not sure what an utteranceId is supposed to be... we maybe able to setup a
                //listener for "utterances" and check to see if they completed or something.
                Log.d(TAG, "Android 11 or higher")
                // mTts.speak(words.getText().toString(), TextToSpeech.QUEUE_ADD, null, myUtteranceId);
                mTts!!.speak(
                    binding.wordsToSpeak.getText().toString(),
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    myUtteranceId
                )
            } else {  //below 11/R use this.
                Log.d(TAG, "Android 10 or lower")
                mTts!!.speak(
                    binding.wordsToSpeak.getText().toString(),
                    TextToSpeech.QUEUE_ADD,
                    null,
                    myUtteranceId
                )
            }
        }

        //using the new startActivityForResult method.
        val myActivityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            // if (result.getResultCode() == Activity.RESULT_OK) {
            if (result.resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // TTS is up and running
                mTts = TextToSpeech(applicationContext, this@MainActivity)
                Log.v(TAG, "Pico is installed okay")
            } else Log.e(TAG, "Got a failure. TTS apparently not available")
        }
        // Check to be sure that TTS exists and is okay to use
        val checkIntent = Intent()
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA)
        myActivityResultLauncher.launch(checkIntent)
    }

    override fun onInit(status: Int) {
        // Now that the TTS engine is ready, we enable the button
        if (status == TextToSpeech.SUCCESS) {
            Log.wtf(TAG, "TextToSpeech.SUCCESS")
            binding.speak.setEnabled(true)
        } else if (status == TextToSpeech.ERROR) {
            Log.wtf(TAG, "TextToSpeech.ERROR")
        } else {
            Log.wtf(TAG, "status is $status")
        }
    }

    public override fun onPause() {
        super.onPause()
        // if we're losing focus, stop talking
        if (mTts != null) mTts!!.stop()
    }

    public override fun onDestroy() {
        //make sure and shutdown the text to speech engine.
        super.onDestroy()
        mTts!!.shutdown()
    }

    companion object {
        private const val TAG = "TTS Demo"
    }
}
