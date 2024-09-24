package edu.cs4730.speech2text;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionSupport;
import android.speech.RecognitionSupportCallback;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executors;

import edu.cs4730.speech2text.databinding.ActivityMainBinding;

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

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private static final String TAG = "VoiceRecognition";
    private static final String myUtteranceId = "spk2txt";
    ActivityMainBinding binding;
    private Handler mHandler;
    private TextToSpeech mTts;
    ActivityResultLauncher<Intent> voiceActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mHandler = new Handler(Looper.getMainLooper());
        mTts = new TextToSpeech(this, this);

        // Check to see if a recognition activity is present, which needs the query in the manifest file in order to work.
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);

        if (!activities.isEmpty()) {
            binding.btnSpeak.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startVoiceRecognitionActivity();
                }
            });
        } else {
            binding.btnSpeak.setEnabled(false);
            binding.btnSpeak.setText("Recognizer not present");
        }
        Context context = this;  //for the launcher, so the adapter will show the theme correctly.  get base or get context don't return the right one.
        voiceActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        //Bundle extras = data.getExtras();  //dont' need this one here.

                        mTts.speak("Did you say?", TextToSpeech.QUEUE_ADD, null, myUtteranceId);

                        // Fill the list view with the strings the recognizer thought it could have heard
                        ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                        //Say it back, JW.
                        if (!matches.isEmpty())
                            mTts.speak(matches.get(0), TextToSpeech.QUEUE_ADD, null, myUtteranceId);


                        //list them to the screen.
                        binding.list.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, matches));

                    } else {
                        Toast.makeText(getApplicationContext(), "Recognation failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        // Most of the applications do not have to handle the voice settings. If the application
        // does not require a recognition in a specific language (i.e., different from the system
        // locale), the application does not need to read the voice settings.
        refreshVoiceSettings2();
    }


    /**
     * Fire an intent to start the speech recognition activity.
     */
    private void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // Specify the calling package to identify your application
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());

        // Display an hint to the user about what he should say.
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recognition demo");

        // Given an hint to the recognizer about what the user is going to say
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        // Specify how many results you want to receive. The results will be sorted
        // where the first result is the one with higher confidence.
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

        // Specify the recognition language. This parameter has to be specified only if the
        // recognition has to be done in a specific language and not the default one (i.e., the
        // system locale). Most of the applications do not have to set this parameter.
        if (binding.supportedLanguages.getSelectedItem() != null) {
            Log.d(TAG, binding.supportedLanguages.getSelectedItem().toString());

            if (!binding.supportedLanguages.getSelectedItem().toString().equals("Default")) {
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                        binding.supportedLanguages.getSelectedItem().toString());
            }
        }
        //startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
        //These the launchers for results.
        voiceActivityResultLauncher.launch(intent);

    }

    /**
     * uses newer API to find the extra languages, but only works in api 33+
     */

    private void refreshVoiceSettings2() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (SpeechRecognizer.isOnDeviceRecognitionAvailable(this) ) {
                SpeechRecognizer recognizer  = SpeechRecognizer.createOnDeviceSpeechRecognizer(this);
                Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                recognizer.checkRecognitionSupport(recognizerIntent, Executors.newSingleThreadExecutor(),
                    new RecognitionSupportCallback() {
                        @Override
                        public void onSupportResult(@NonNull RecognitionSupport recognitionSupport) {
                            mHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    updateSupportedLanguages(recognitionSupport.getSupportedOnDeviceLanguages());
                                }
                            });

                        }

                        @Override
                        public void onError(int error) {

                        }
                    }
                );
            }
        } else {
            refreshVoiceSettings(); //default to likely the broken one.

        }
    }

    /**
     * Should find the extra languages and pieces that can be recognized
     * but has been broken in Oreo, when the intent queries were required.
     *
     * likely will always return null.
     */

    private void refreshVoiceSettings() {
        Log.i(TAG, "Sending broadcast");

        /*
        Set<Locale> lang = mTts.getAvailableLanguages();  //this is always null
        for (Locale t: lang) {
            Log.d(TAG, t.toString()
            );
        }
        */

        //Intent i = new Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS);
        Intent i = new Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS);
        //intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        sendOrderedBroadcast(i,
            null,
            new SupportedLanguageBroadcastReceiver(),
            null,
            Activity.RESULT_OK,
            null,
            null);

        if (RecognizerIntent.getVoiceDetailsIntent(this) != null) {

            sendOrderedBroadcast(RecognizerIntent.getVoiceDetailsIntent(getApplicationContext()),
                null,
                new SupportedLanguageBroadcastReceiver(),
                null,
                Activity.RESULT_OK,
                null,
                null);
        } else {
            Log.wtf(TAG, "Google f**ing broke the intent visibly here and so this all fails.");
        }
    }

    private void updateSupportedLanguages(List<String> languages) {
        // We add "Default" at the beginning of the list to simulate default language.
        languages.add(0, "Default");

        SpinnerAdapter adapter = new ArrayAdapter<CharSequence>(this,
            android.R.layout.simple_spinner_item, languages.toArray(
            new String[languages.size()]));
        binding.supportedLanguages.setAdapter(adapter);
    }

    private void updateLanguagePreference(String language) {
        TextView textView = findViewById(R.id.language_preference);
        textView.setText(language);
    }

    /**
     * Handles the response of the broadcast request about the recognizer supported languages.
     * <p>
     * The receiver is required only if the application wants to do recognition in a specific
     * language.
     */
    private class SupportedLanguageBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, final Intent intent) {
            Log.wtf(TAG, "Receiving broadcast " + intent);

            final Bundle extra = getResultExtras(false);

            if (getResultCode() != Activity.RESULT_OK) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "Error code:" + getResultCode());
                    }
                });
            }

            if (extra == null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "No extra, so no supported languages included.");
                    }
                });
                return;
            }

            if (extra.containsKey(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES)) {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        updateSupportedLanguages(extra.getStringArrayList(
                            RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES));
                    }
                });
            }

            if (extra.containsKey(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE)) {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        updateLanguagePreference(
                            extra.getString(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE));
                    }
                });
            }
        }

        private void showToast(String text) {
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onInit(int status) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDestroy() {
        //make sure and shutdown the text to speech engine.
        super.onDestroy();
        mTts.shutdown();
    }
}
