package edu.cs4730.speech2textdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

/**
 * This is a simplified version of Google's voice recognition code.
 * It skips all the languages and just uses the default language.
 */

public class MainFragment extends Fragment {

    private static final String TAG = "VoiceRecognition";

    TextView log;
    Button spk;
    ActivityResultLauncher<Intent> myActivityResultLauncher;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_main, container, false);
        spk = myView.findViewById(R.id.button1);
        spk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceRecognitionActivity();
            }
        });
        log = myView.findViewById(R.id.log);

        //using the new startActivityForResult method.
        myActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        // Fill the list view with the strings the recognizer thought it could have heard, there should be 5, based on the call
                        ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                        //display results.
                        logthis("results: " + String.valueOf(matches.size()));
                        for (int i = 0; i < matches.size(); i++) {
                            logthis("result " + i + ":" + matches.get(i));
                        }
                    }
                }
            });

        return myView;
    }

    /**
     * Fire an intent to start the speech recognition activity.
     */
    private void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // Specify the calling package to identify your application
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());

        // Display an hint to the user about what he should say.
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say Something!");

        // Given an hint to the recognizer about what the user is going to say
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        // Specify how many results you want to receive. The results will be sorted
        // where the first result is the one with higher confidence.
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

        Log.i(TAG, "Calling the Voice Intenet");
        myActivityResultLauncher.launch(intent);
    }

    /**
     * simple method to add the log TextView and debug log.
     */
    public void logthis(String newinfo) {
        Log.d(TAG, newinfo);
        log.append(newinfo + "\n");
    }
}
