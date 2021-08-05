package edu.cs4730.speech2textdemo2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * This example shows the speech recognition without a dialog box.
 * You should likely create your own, so people know when to speak and when it stops.
 */
public class MainFragment extends Fragment implements OnClickListener {
    private TextView logger;
    private SpeechRecognizer sr;
    private static final String TAG = "MainFragment";

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_main, container, false);
        //set the listener for the button.
        myView.findViewById(R.id.button1).setOnClickListener(this);
        //get the logger textview.
        logger = myView.findViewById(R.id.log);

        //get the SpeechRecognizer and set a listener for it.
        sr = SpeechRecognizer.createSpeechRecognizer(getContext());
        sr.setRecognitionListener(new listener());

        return myView;
    }

    @Override
    public void onDestroy() {
        sr.destroy();
        sr = null;
        super.onDestroy();
    }

    /**
     * The Recognitionlistener for the SpeechRecognizer.
     */
    class listener implements RecognitionListener {
        public void onReadyForSpeech(Bundle params) {
            logthis("onReadyForSpeech");
        }

        public void onBeginningOfSpeech() {
            logthis("onBeginningOfSpeech");
        }

        public void onRmsChanged(float rmsdB) {
            Log.d(TAG, "onRmsChanged");  //called way to much for the textview
        }

        public void onBufferReceived(byte[] buffer) {
            logthis("onBufferReceived");
        }

        public void onEndOfSpeech() {
            logthis("onEndofSpeech");
        }

        public void onError(int error) {
            logthis("error " + error);
        }

        public void onResults(Bundle results) {

            Log.d(TAG, "onResults " + results);
            // Fill the list view with the strings the recognizer thought it could have heard, there should be 5, based on the call
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            //display results.
            logthis("results: " + String.valueOf(matches.size()));
            for (int i = 0; i < matches.size(); i++) {
                logthis("result " + i + ":" + matches.get(i));
            }
        }

        public void onPartialResults(Bundle partialResults) {
            logthis("onPartialResults");
        }

        public void onEvent(int eventType, Bundle params) {
            logthis("onEvent " + eventType);
        }
    }

    //button listener.  nothing really to see here.
    public void onClick(View v) {
        if (v.getId() == R.id.button1) {
            RecordSpeak();
        }
    }

    /**
     * this will do the speech recording and then ask google for a result.
     * first check if we have permission.  If not, then mainactivity will call this again, once we have permissions.
     */
    public void RecordSpeak() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            //I'm on not explaining why, just asking for permission.
            Log.v(TAG, "asking for permissions");
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO},
                MainActivity.REQUEST_PERM_ACCESS);
        } else {
            //get the recognize intent
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            //Specify the calling package to identify your application
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
            //Given an hint to the recognizer about what the user is going to say
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            //specify the max number of results
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
            //User of SpeechRecognizer to "send" the intent.
            Log.i(TAG, "before start listening");
            sr.startListening(intent);
            Log.i(TAG, "Intent sent");
        }
    }

    /**
     * simple method to add the log TextView and debug log
     */
    public void logthis(String newinfo) {
        Log.d(TAG, newinfo);
        logger.append(newinfo + "\n");
    }
}
