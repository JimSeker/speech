package edu.cs4730.speech2text_kt

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionSupport
import android.speech.RecognitionSupportCallback
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import edu.cs4730.speech2text_kt.databinding.ActivityMainBinding
import java.util.concurrent.Executors


/**
 * One of google's older examples of speech recognition.  with some fixes here and there it
 * still works mostly.
 *
 * 10/14/22 The multilingual part is now broken.  at at android 11, it stop being able to get the supported languages
 * and  the intent queries is pretty much broken this app for the addition languages.  mulit searches over 2 years
 * have turned up no good fix.
 * 9/24/24 but api 33 does have a fix in it, so refreshVoiceSettings2 uses those and fails back to the broken
 * ones for below 33.
 *
 * It will do multilingual recognition as well.  It will also speak back to
 * you the top result.
 */
class MainActivity : AppCompatActivity(), OnInitListener {

    private val TAG = "VoiceRecognition"
    private val myUtteranceId = "spk2txt"
    private lateinit var binding: ActivityMainBinding
    private lateinit var mHandler: Handler
    private lateinit var mTts: TextToSpeech
    private lateinit var voiceActivityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }
        mHandler = Handler(Looper.getMainLooper())
        mTts = TextToSpeech(this, this)

        // Check to see if a recognition activity is present, which needs the query in the manifest file in order to work.
        val pm = packageManager
        val activities =
            pm.queryIntentActivities(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0)

        if (activities.isNotEmpty()) {
            binding.btnSpeak.setOnClickListener(View.OnClickListener { startVoiceRecognitionActivity() })
        } else {
            binding.btnSpeak.setEnabled(false)
            binding.btnSpeak.text = "Recognizer not present"
        }
        val context: Context =
            this //for the launcher, so the adapter will show the theme correctly.  get base or get context don't return the right one.
        voiceActivityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                // There are no request codes
                val data = result.data

                //Bundle extras = data.getExtras();  //dont' need this one here.
                mTts.speak("Did you say?", TextToSpeech.QUEUE_ADD, null, myUtteranceId)

                // Fill the list view with the strings the recognizer thought it could have heard
                val matches = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

                //Say it back, JW.
                if (matches!!.isNotEmpty()) mTts.speak(
                    matches[0], TextToSpeech.QUEUE_ADD, null, myUtteranceId
                )


                //list them to the screen.
                binding.list.setAdapter(
                    ArrayAdapter(
                        context, android.R.layout.simple_list_item_1, matches
                    )
                )
            } else {
                Toast.makeText(applicationContext, "Recognation failed", Toast.LENGTH_SHORT).show()
            }
        }

        // Most of the applications do not have to handle the voice settings. If the application
        // does not require a recognition in a specific language (i.e., different from the system
        // locale), the application does not need to read the voice settings.
        refreshVoiceSettings2()
    }


    /**
     * Fire an intent to start the speech recognition activity.
     */
    private fun startVoiceRecognitionActivity() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        // Specify the calling package to identify your application
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, javaClass.getPackage().name)

        // Display an hint to the user about what he should say.
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recognition demo")

        // Given an hint to the recognizer about what the user is going to say
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        // Specify how many results you want to receive. The results will be sorted
        // where the first result is the one with higher confidence.
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)

        // Specify the recognition language. This parameter has to be specified only if the
        // recognition has to be done in a specific language and not the default one (i.e., the
        // system locale). Most of the applications do not have to set this parameter.
        if (binding.supportedLanguages.getSelectedItem() != null) {
            Log.d(TAG, binding.supportedLanguages.getSelectedItem().toString())

            if (!binding.supportedLanguages.getSelectedItem().toString().equals("Default")) {
                intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    binding.supportedLanguages.getSelectedItem().toString()
                )
            }
        }
        //startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
        //These the launchers for results.
        voiceActivityResultLauncher.launch(intent)
    }

    /**
     * uses newer API to find the extra languages, but only works in api 33+
     */
    private fun refreshVoiceSettings2() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (SpeechRecognizer.isOnDeviceRecognitionAvailable(this)) {
                val recognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(this)
                val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                recognizer.checkRecognitionSupport(recognizerIntent,
                    Executors.newSingleThreadExecutor(),
                    object : RecognitionSupportCallback {
                        override fun onSupportResult(recognitionSupport: RecognitionSupport) {
                            mHandler.post { updateSupportedLanguages(recognitionSupport.supportedOnDeviceLanguages) }
                        }

                        override fun onError(error: Int) {
                        }
                    })
            }
        } else {
            refreshVoiceSettings() //default to likely the broken one.
        }
    }

    /**
     * Should find the extra languages and pieces that can be recognized
     * but has been broken in Oreo, when the intent queries were required.
     *
     * likely will always return null.
     */
    private fun refreshVoiceSettings() {
        Log.i(TAG, "Sending broadcast")

        /*
        Set<Locale> lang = mTts.getAvailableLanguages();  //this is always null
        for (Locale t: lang) {
            Log.d(TAG, t.toString()
            );
        }
        */

        //Intent i = new Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS);
        val i = Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS)
        //intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        sendOrderedBroadcast(
            i, null, SupportedLanguageBroadcastReceiver(), null, RESULT_OK, null, null
        )

        if (RecognizerIntent.getVoiceDetailsIntent(this) != null) {
            sendOrderedBroadcast(
                RecognizerIntent.getVoiceDetailsIntent(applicationContext),
                null,
                SupportedLanguageBroadcastReceiver(),
                null,
                RESULT_OK,
                null,
                null
            )
        } else {
            Log.wtf(TAG, "Google f**ing broke the intent visibly here and so this all fails.")
        }
    }

    private fun updateSupportedLanguages(initlanguages: List<String>?) {
        // We add "Default" at the beginning of the list to simulate default language.
        val languages = mutableListOf("Default") + initlanguages!!.toTypedArray<String>()

        val adapter: SpinnerAdapter = ArrayAdapter<CharSequence>(
            this, android.R.layout.simple_spinner_item, languages.toTypedArray<String>()
        )
        binding.supportedLanguages.setAdapter(adapter)
    }

    private fun updateLanguagePreference(language: String?) {
        val textView = findViewById<TextView>(R.id.language_preference)
        textView.text = language
    }

    /**
     * Handles the response of the broadcast request about the recognizer supported languages.
     *
     *
     * The receiver is required only if the application wants to do recognition in a specific
     * language.
     */
    private inner class SupportedLanguageBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.wtf(TAG, "Receiving broadcast $intent")

            val extra = getResultExtras(false)

            if (resultCode != RESULT_OK) {
                mHandler.post {
                    Log.e(TAG, "Error code:$resultCode")
                }
            }

            if (extra == null) {
                mHandler.post {
                    Log.e(TAG, "No extra, so no supported languages included.")
                }
                return
            }

            if (extra.containsKey(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES)) {
                mHandler.post {
                    updateSupportedLanguages(
                        extra.getStringArrayList(
                            RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES
                        )
                    )
                }
            }

            if (extra.containsKey(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE)) {
                mHandler.post {
                    updateLanguagePreference(
                        extra.getString(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE)
                    )
                }
            }
        }

        private fun showToast(text: String) {
            Toast.makeText(applicationContext, text, Toast.LENGTH_LONG).show()
        }
    }

    override fun onInit(status: Int) {
        // TODO Auto-generated method stub
    }

    public override fun onDestroy() {
        //make sure and shutdown the text to speech engine.
        super.onDestroy()
        mTts.shutdown()
    }
}
