package edu.cs4730.speech2textdemo_kt

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import edu.cs4730.speech2textdemo_kt.databinding.ActivityMainBinding


/**
 * This is a simplified version of Google's voice recognition code.
 * It skips all the languages and just uses the default language.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var myActivityResultLauncher: ActivityResultLauncher<Intent>
    companion object {
        private const val TAG = "VoiceRecognition"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())
        binding.button1.setOnClickListener { startVoiceRecognitionActivity() }

        //using the new startActivityForResult method.
        registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                // Fill the list view with the strings the recognizer thought it could have heard, there should be 5, based on the call
                val matches = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

                //display results.
                logthis("results: " + matches!!.size.toString())
                for (i in matches.indices) {
                    logthis("result " + i + ":" + matches[i])
                }
            }
        }.also { myActivityResultLauncher = it }
    }

    /**
     * Fire an intent to start the speech recognition activity.
     */
    private fun startVoiceRecognitionActivity() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        // Specify the calling package to identify your application
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, javaClass.getPackage()?.name)

        // Display an hint to the user about what he should say.
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say Something!")

        // Given an hint to the recognizer about what the user is going to say
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        // Specify how many results you want to receive. The results will be sorted
        // where the first result is the one with higher confidence.
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)

        Log.i(TAG, "Calling the Voice Intenet")
        myActivityResultLauncher.launch(intent)
    }

    /**
     * simple method to add the log TextView and debug log.
     */
    private fun logthis(newinfo: String) {
        Log.d(TAG, newinfo)
        binding.logger.append(newinfo + "\n")
    }


}
