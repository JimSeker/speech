package edu.cs4730.txt2spk;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * A simple example of getting text input (via a EditText)
 * and using the text to spech engine to say the words.
 * 
 */
public class MainFragment extends Fragment implements OnInitListener {
	  private EditText words = null;
	  private Button speakBtn = null;
	  private static final int REQ_TTS_STATUS_CHECK = 0;
	  private static final String TAG = "TTS Demo";
	  private TextToSpeech mTts;

	
	
	public MainFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View myView = inflater.inflate(R.layout.main_fragment, container, false);
		
        words = (EditText) myView.findViewById(R.id.wordsToSpeak);
        speakBtn = (Button) myView.findViewById(R.id.speak);
        speakBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
            	//Speech is simple.  send the words to speech aloud via the 
            	//the text to speech end and add it to the end queue. (maybe others already in line.)
                mTts.speak(words.getText().toString(), TextToSpeech.QUEUE_ADD, null);
            }});

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
        }
        else {
            // Got something else
        }
    }

    @Override
    public void onInit(int status) {
        // Now that the TTS engine is ready, we enable the button
        if( status == TextToSpeech.SUCCESS) {
            speakBtn.setEnabled(true);
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        // if we're losing focus, stop talking
        if( mTts != null)
            mTts.stop();
    }

    @Override
    public void onDestroy()
    {
    	//make sure and shutdown the text to speech engine.
        super.onDestroy();
        mTts.shutdown();
    }
}
