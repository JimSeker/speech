package edu.cs4730.speech2textdemo2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

/**
 * This example shows the speech recognition without a dialog box.
 * You should likely create your own, so people know when to speak and when it stops.
 */

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_PERM_ACCESS = 1;
    private TextView logger;
    private SpeechRecognizer sr;
    private static final String TAG = "MainActivity";
    ActivityResultLauncher<String[]> rpl;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.RECORD_AUDIO"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //set the listener for the button.
        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!allPermissionsGranted())
                    rpl.launch(REQUIRED_PERMISSIONS);
                else
                    RecordSpeak();
            }
        });
        //get the logger textview.
        logger = findViewById(R.id.log);

        //this allows us to check with multiple permissions, but in this case (currently) only need 1.
        rpl = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> isGranted) {
                    boolean granted = true;
                    for (Map.Entry<String, Boolean> x : isGranted.entrySet()) {
                        logthis(x.getKey() + " is " + x.getValue());
                        if (!x.getValue()) granted = false;
                    }
                    if (granted) RecordSpeak();
                }
            }
        );

        //get the SpeechRecognizer and set a listener for it.
        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new listener());
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


    /**
     * this will do the speech recording and then ask google for a result.
     * first check if we have permission.  If not, then mainactivity will call this again, once we have permissions.
     */
    public void RecordSpeak() {
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

    /**
     * simple method to add the log TextView and debug log
     */
    public void logthis(String newinfo) {
        Log.d(TAG, newinfo);
        logger.append(newinfo + "\n");
    }

    //ask for permissions when we start.
    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
