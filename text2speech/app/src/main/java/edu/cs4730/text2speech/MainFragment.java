package edu.cs4730.text2speech;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * A simple example of getting text input (via a EditText)
 * and using the text to speech engine to say the words.
 */
public class MainFragment extends Fragment implements TextToSpeech.OnInitListener {
    private EditText words = null;
    private Button speakBtn = null;
    private static final String TAG = "TTS Demo";
    private TextToSpeech mTts;
    private final String myUtteranceId = "txt2spk";


    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.main_fragment, container, false);

        words = myView.findViewById(R.id.wordsToSpeak);
        speakBtn = myView.findViewById(R.id.speak);
        speakBtn.setEnabled(false);
        speakBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //Speech is simple.  send the words to speech aloud via the
                //the text to speech end and add it to the end queue. (maybe others already in line.)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    //not sure what an utteranceId is supposed to be... we maybe able to setup a
                    //listener for "utterances" and check to see if they completed or something.
                    Log.d(TAG, "Android 11 or higher");
                    // mTts.speak(words.getText().toString(), TextToSpeech.QUEUE_ADD, null, myUtteranceId);
                    mTts.speak(words.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, myUtteranceId);
                } else {  //below 11/R use this.
                    Log.d(TAG, "Android 10 or lower");
                    mTts.speak(words.getText().toString(), TextToSpeech.QUEUE_ADD, null, myUtteranceId);
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
                        mTts = new TextToSpeech(getActivity(), MainFragment.this);
                        Log.v(TAG, "Pico is installed okay");
                    } else
                        Log.e(TAG, "Got a failure. TTS apparently not available");
                }
            });
        // Check to be sure that TTS exists and is okay to use
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        myActivityResultLauncher.launch(checkIntent);
        return myView;
    }

    @Override
    public void onInit(int status) {
        // Now that the TTS engine is ready, we enable the button
        if (status == TextToSpeech.SUCCESS) {
            Log.wtf(TAG, "TextToSpeech.SUCCESS");
            speakBtn.setEnabled(true);
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
