package edu.cs4730.speech2text;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements OnClickListener, OnInitListener {


    private static final String TAG = "VoiceRecognition";
    private static final String myUtteranceId = "spk2txt";
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

    private ListView mList;

    private Handler mHandler;

    private Spinner mSupportedLanguageView;
    private TextToSpeech mTts;

    View myView;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mHandler = new Handler();
        mTts = new TextToSpeech(getActivity(), this);
        // Inflate our UI from its XML layout description.
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get display items for later interaction
        Button speakButton = myView.findViewById(R.id.btn_speak);

        mList = myView.findViewById(R.id.list);

        mSupportedLanguageView = myView.findViewById(R.id.supported_languages);

        // Check to see if a recognition activity is present
        PackageManager pm = getActivity().getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
            new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);

        if (activities.size() != 0) {
            speakButton.setOnClickListener(this);
            // Most of the applications do not have to handle the voice settings. If the application
            // does not require a recognition in a specific language (i.e., different from the system
            // locale), the application does not need to read the voice settings.
            refreshVoiceSettings();

        } else {
            speakButton.setEnabled(false);
            speakButton.setText("Recognizer not present");
        }


        return myView;
    }


    /**
     * Handle the click on the start recognition button.
     */
    public void onClick(View v) {
        if (v.getId() == R.id.btn_speak) {
            startVoiceRecognitionActivity();
        }
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
        if (!mSupportedLanguageView.getSelectedItem().toString().equals("Default")) {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                mSupportedLanguageView.getSelectedItem().toString());
        }

        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

    /**
     * Handle the results from the recognition activity.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            mTts.speak("Did you say?", TextToSpeech.QUEUE_ADD, null, myUtteranceId);

            // Fill the list view with the strings the recognizer thought it could have heard
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            //Say it back, JW.
            if (!matches.isEmpty())
                mTts.speak(matches.get(0), TextToSpeech.QUEUE_ADD, null, myUtteranceId);


            //list them to the screen.
            mList.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, matches));
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void refreshVoiceSettings() {
        Log.i(TAG, "Sending broadcast");
        getActivity().sendOrderedBroadcast(RecognizerIntent.getVoiceDetailsIntent(getActivity()), null,
            new SupportedLanguageBroadcastReceiver(), null, Activity.RESULT_OK, null, null);
    }

    private void updateSupportedLanguages(List<String> languages) {
        // We add "Default" at the beginning of the list to simulate default language.
        languages.add(0, "Default");

        SpinnerAdapter adapter = new ArrayAdapter<CharSequence>(getActivity(),
            android.R.layout.simple_spinner_item, languages.toArray(
            new String[languages.size()]));
        mSupportedLanguageView.setAdapter(adapter);
    }

    private void updateLanguagePreference(String language) {
        TextView textView = (TextView) myView.findViewById(R.id.language_preference);
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
            Log.i(TAG, "Receiving broadcast " + intent);

            final Bundle extra = getResultExtras(false);

            if (getResultCode() != Activity.RESULT_OK) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        showToast("Error code:" + getResultCode());
                    }
                });
            }

            if (extra == null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        showToast("No extra");
                    }
                });
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
            Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
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
