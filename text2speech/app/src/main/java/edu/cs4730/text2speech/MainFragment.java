package edu.cs4730.text2speech;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;

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
    private static final int REQ_TTS_STATUS_CHECK = 0;
    private static final String TAG = "TTS Demo";
    private TextToSpeech mTts;
    private String myUtteranceId = "txt2spk";


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

        // Check to be sure that TTS exists and is okay to use
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        //The result will come back in onActivityResult with our REQ_TTS_STATUS_CHECK number
        startActivityForResult(checkIntent, REQ_TTS_STATUS_CHECK);

        return myView;
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_TTS_STATUS_CHECK) {
            switch (resultCode) {
                case TextToSpeech.Engine.CHECK_VOICE_DATA_PASS:
                    // TTS is up and running
                    mTts = new TextToSpeech(getActivity(), this);
                    Log.v(TAG, "Pico is installed okay");
                    break;
                case TextToSpeech.Engine.CHECK_VOICE_DATA_FAIL:
                default:
                    Log.e(TAG, "Got a failure. TTS apparently not available");
            }
        } else {
            // Got something else
        }
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
