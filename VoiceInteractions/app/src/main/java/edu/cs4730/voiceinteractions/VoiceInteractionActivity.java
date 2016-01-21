package edu.cs4730.voiceinteractions;

import android.app.VoiceInteractor;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.actions.NoteIntents;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

//https://developers.google.com/voice-actions/interaction/
//http://io2015codelabs.appspot.com/codelabs/voice-interaction#6

/*
 * This example doesn't work very well.  It attempts to recreate the google example of tuneIn
 *   Speak: Play music on tuneIn   , but I don't get any voiceinteraction
 *
 *   It also has the image intent.  So say Take a selfie  and it will show that voiceinteraction does work.
 */

public class VoiceInteractionActivity extends AppCompatActivity {
    ListView myList;
    String[] values = new String[]{"Blues", "Classical", "Country", "Folk", "Jazz", "Pop", "Rock",
            "Rap", "Heavy Metal"};
    String TAG = "VoiceActionAct";
   // private static final Uri APP_URI = Uri.parse("android-app://edu.cs4730.voiceinteractions/VoiceInteractionActivity");

    //private GoogleApiClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voiceinteraction);

        Intent intent = getIntent();
        if (intent == null) {
            Log.v("TAG", "No Intent!");
            finish();

            // } else if (intent.getAction().equals("com.google.android.gms.actions.CREATE_NOTE")) {  //direct action intent name.
        } else if (isVoiceInteraction()) {
            Log.v(TAG, "Intent is " + intent.getAction());
        //What the hell are the keys and all the data anyway?  There are a lot of them.
        Bundle ab = intent.getExtras();
        for (String key : ab.keySet()) {
            Object value = ab.get(key);
            Log.d(TAG, String.format("%s %s (%s)", key,
                    value.toString(), value.getClass().getName()));
        }

            Log.v(TAG, "It worked, onto Next step.");

            //setup the display
            myList = (ListView) findViewById(R.id.list1);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                    android.R.layout.simple_list_item_1, values);
            myList.setAdapter(adapter);
            //now setup and call the initial voice trigger.
            startVoiceTrigger();

        } else {
            Log.v(TAG, "Intent is " + intent.getAction());
            Log.v(TAG, "No Voice Interaction");
            finish();
        }


    }

    private void startVoiceTrigger() {
        Log.d(TAG, "startVoiceTrigger: ");
        if (isVoiceInteraction()) {
            String[] values = new String[]{"Blues", "Classical", "Country", "Folk", "Jazz", "Pop", "Rock",
                    "Rap", "Heavy Metal"};

            //Doing this the Hard way likely.
            VoiceInteractor.PickOptionRequest.Option[] mOptions = new VoiceInteractor.PickOptionRequest.Option[]{
                    new VoiceInteractor.PickOptionRequest.Option(values[0], 0),
                    new VoiceInteractor.PickOptionRequest.Option(values[1], 1),
                    new VoiceInteractor.PickOptionRequest.Option(values[2], 2),
                    new VoiceInteractor.PickOptionRequest.Option(values[3], 3),
                    new VoiceInteractor.PickOptionRequest.Option(values[4], 4),
                    new VoiceInteractor.PickOptionRequest.Option(values[5], 5),
                    new VoiceInteractor.PickOptionRequest.Option(values[6], 6),
                    new VoiceInteractor.PickOptionRequest.Option(values[7], 7),
                    new VoiceInteractor.PickOptionRequest.Option(values[8], 8).addSynonym("Angry White boy Music")
            };
//            VoiceInteractor.PickOptionRequest.Option option1 = new VoiceInteractor.PickOptionRequest.Option("work", 1);
//            option1.addSynonym("business");

            VoiceInteractor.Prompt prompt = new VoiceInteractor.Prompt("Pick a Category");
            getVoiceInteractor().submitRequest(new VoiceInteractor.PickOptionRequest(prompt, mOptions, null) {
                @Override
                public void onPickOptionResult(boolean finished, Option[] selections, Bundle result) {
                    if (finished && selections.length >= 1) {
                        //so doc's say it could be more then one selection, so just choose first one.
                        Log.v(TAG, "Option is " + selections[0].getLabel());
                        SendComplete(selections[0].getLabel().toString());
                    } else {
                        Log.v(TAG, "No Selection?");
                        finish();
                    }
                }

                @Override
                public void onCancel() {
                    Log.v(TAG, "User Canceled.");
                    finish();

                }
            });
        } else {
            Log.v(TAG, "IsVoiceInterACtion is false Now?");
            finish();
        }
    }


    public void SendComplete(String genre) {
        //
        VoiceInteractor.Prompt prompt = new VoiceInteractor.Prompt("Playing "+ genre);
        Bundle status = new Bundle();
        getVoiceInteractor().submitRequest(
                new VoiceInteractor.CompleteVoiceRequest(prompt, status) {
                    @Override
                    public void onCompleteResult(Bundle result) {
                        super.onCompleteResult(result);
                        Log.d(TAG, "OnCompleteResult:" + Log.getStackTraceString(new Exception()));
                    }
                });


        finish();
    }


}
