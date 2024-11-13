package edu.cs4730.speech2textdemo2_kt

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import edu.cs4730.speech2textdemo2_kt.databinding.ActivityMainBinding

/**
 * This example shows the speech recognition without a dialog box.
 * You should likely create your own, so people know when to speak and when it stops.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sr: SpeechRecognizer
    private lateinit var rpl: ActivityResultLauncher<Array<String>>
    private val REQUIRED_PERMISSIONS = arrayOf("android.permission.RECORD_AUDIO")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }


        //set the listener for the button.
        binding.button1.setOnClickListener {
            if (!allPermissionsGranted()) rpl.launch(REQUIRED_PERMISSIONS)
            else RecordSpeak()
        }

        //this allows us to check with multiple permissions, but in this case (currently) only need 1.
        rpl = registerForActivityResult<Array<String>, Map<String, Boolean>>(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { isGranted ->
            var granted = true
            for ((key, value) in isGranted) {
                logthis("$key is $value")
                if (!value) granted = false
            }
            if (granted) RecordSpeak()
        }

        //get the SpeechRecognizer and set a listener for it.
        sr = SpeechRecognizer.createSpeechRecognizer(this)
        sr.setRecognitionListener(listener())
    }


    public override fun onDestroy() {
        sr.destroy()
        super.onDestroy()
    }

    /**
     * The Recognitionlistener for the SpeechRecognizer.
     */
    internal inner class listener : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle) {
            logthis("onReadyForSpeech")
        }

        override fun onBeginningOfSpeech() {
            logthis("onBeginningOfSpeech")
        }

        override fun onRmsChanged(rmsdB: Float) {
            Log.d(TAG, "onRmsChanged") //called way to much for the textview
        }

        override fun onBufferReceived(buffer: ByteArray) {
            logthis("onBufferReceived")
        }

        override fun onEndOfSpeech() {
            logthis("onEndofSpeech")
        }

        override fun onError(error: Int) {
            logthis("error $error")
        }

        override fun onResults(results: Bundle) {
            Log.d(TAG, "onResults $results")
            // Fill the list view with the strings the recognizer thought it could have heard, there should be 5, based on the call
            val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            //display results.
            logthis("results: " + matches!!.size.toString())
            for (i in matches.indices) {
                logthis("result " + i + ":" + matches[i])
            }
        }

        override fun onPartialResults(partialResults: Bundle) {
            logthis("onPartialResults")
        }

        override fun onEvent(eventType: Int, params: Bundle) {
            logthis("onEvent $eventType")
        }
    }


    /**
     * this will do the speech recording and then ask google for a result.
     * first check if we have permission.  If not, then mainactivity will call this again, once we have permissions.
     */
    private fun RecordSpeak() {
        //get the recognize intent
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        //Specify the calling package to identify your application
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, javaClass.getPackage().name)
        //Given an hint to the recognizer about what the user is going to say
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        //specify the max number of results
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        //User of SpeechRecognizer to "send" the intent.
        Log.i(TAG, "before start listening")
        sr.startListening(intent)
        Log.i(TAG, "Intent sent")
    }

    /**
     * simple method to add the log TextView and debug log
     */
    fun logthis(newinfo: String) {
        Log.d(TAG, newinfo)
        binding.log.append(newinfo + "\n")
    }

    //ask for permissions when we start.
    private fun allPermissionsGranted(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    this, permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
