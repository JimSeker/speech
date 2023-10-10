package edu.cs4730.text2speech;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import edu.cs4730.text2speech.databinding.ActivityMainBinding;

/**
 * A simple example of getting text input (via a EditText)
 * and using the text to speech engine to say the words.
 */

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private ActivityMainBinding binding;
    private static final String TAG = "TTS Demo";
    private TextToSpeech mTts;
    private final String myUtteranceId = "txt2spk";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.speak.setEnabled(false);
        binding.speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Speech is simple.  send the words to speech aloud via the
                //the text to speech end and add it to the end queue. (maybe others already in line.)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    //not sure what an utteranceId is supposed to be... we maybe able to setup a
                    //listener for "utterances" and check to see if they completed or something.
                    Log.d(TAG, "Android 11 or higher");
                    // mTts.speak(words.getText().toString(), TextToSpeech.QUEUE_ADD, null, myUtteranceId);
                    mTts.speak(binding.wordsToSpeak.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, myUtteranceId);
                } else {  //below 11/R use this.
                    Log.d(TAG, "Android 10 or lower");
                    mTts.speak(binding.wordsToSpeak.getText().toString(), TextToSpeech.QUEUE_ADD, null, myUtteranceId);
                }
            }
        });

        //using the new startActivityForResult method.
        ActivityResultLauncher<Intent> myActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        // if (result.getResultCode() == Activity.RESULT_OK) {
                        if (result.getResultCode() == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                            // TTS is up and running
                            mTts = new TextToSpeech(getApplicationContext(), MainActivity.this);
                            Log.v(TAG, "Pico is installed okay");
                        } else
                            Log.e(TAG, "Got a failure. TTS apparently not available");
                    }
                });
        // Check to be sure that TTS exists and is okay to use
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        myActivityResultLauncher.launch(checkIntent);
    }

    @Override
    public void onInit(int status) {
        // Now that the TTS engine is ready, we enable the button
        if (status == TextToSpeech.SUCCESS) {
            Log.wtf(TAG, "TextToSpeech.SUCCESS");
            binding.speak.setEnabled(true);
        } else if (status == TextToSpeech.ERROR) {
            Log.wtf(TAG, "TextToSpeech.ERROR");
        } else {
            Log.wtf(TAG, "status is " + status);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // if we're losing focus, stop talking
        if (mTts != null)
            mTts.stop();
    }

    @Override
    public void onDestroy() {
        //make sure and shutdown the text to speech engine.
        super.onDestroy();
        mTts.shutdown();
    }

}
