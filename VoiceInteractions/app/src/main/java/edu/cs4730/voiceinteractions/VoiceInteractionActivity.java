package edu.cs4730.voiceinteractions;

import android.app.VoiceInteractor;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.actions.NoteIntents;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

//https://developers.google.com/voice-actions/interaction/
//http://io2015codelabs.appspot.com/codelabs/voice-interaction#6

public class VoiceInteractionActivity extends AppCompatActivity {
    String title, text, category;
    String TAG = "VoiceActionAct";
    private static final Uri APP_URI = Uri.parse("android-app://edu.cs4730.voiceinteractions/VoiceInteractionActivity");

    private GoogleApiClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent == null) {
            finish();

            //} else if (isVoiceInteraction()) {  //yes called by voice, not some other way  API 23+
            //This was called by a voice interatction.  Should work I hope.


            // } else if (intent.getAction().equals("com.google.android.gms.actions.CREATE_NOTE")) {  //direct action intent name.
        } else {//if (NoteIntents.ACTION_CREATE_NOTE.equals(intent.getAction())) {
            // text = intent.getExtras().getString(NoteIntents.EXTRA_TEXT, "nothing?");   //This one doesn't work?!
            //these work...
            title = intent.getExtras().getString("android.intent.extra.SUBJECT", ""); //title, optional and likely not there.
            text = intent.getExtras().getString("android.intent.extra.TEXT", "nothing?");

            Log.v(TAG, "It worked: " + text);
            startVoiceTrigger();

        }



    }


    public void finishup() {
        //store the note.
        SharedPreferences preferences = getSharedPreferences("example", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("title", title);
        editor.putString("text", text);
        editor.putString("category", category);
        editor.commit();

/*
        //What the hell are the keys and all the data anyway?  There are a lot of them.
        Bundle ab = intent.getExtras();

        for (String key : ab.keySet()) {
            Object value = ab.get(key);
            Log.d(TAG, String.format("%s %s (%s)", key,
                    value.toString(), value.getClass().getName()));
        }
*/
        //tell google we have completed the action.
        mClient = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        Thing note = new Thing.Builder()
                .setName(title)
                .setDescription(text)
                .setUrl(APP_URI)
                .build();

        Action createNoteAction = new Action.Builder(Action.TYPE_ADD)
                .setObject(note)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();

        AppIndex.AppIndexApi.end(mClient, createNoteAction);


        VoiceInteractor.Prompt prompt = new VoiceInteractor.Prompt("Note Added.");
        getVoiceInteractor().submitRequest(
                new VoiceInteractor.CompleteVoiceRequest(prompt, null) {
                    @Override
                    public void onCompleteResult(Bundle result) {
                        super.onCompleteResult(result);
                        Log.d(TAG, "OnCompleteResult:" + Log.getStackTraceString(new Exception()));

                    }
                });



        finish();
    }


    private void startVoiceTrigger() {
        Log.d(TAG, "startVoiceTrigger: ");
        if (isVoiceInteraction()) {
            VoiceInteractor.PickOptionRequest.Option option1 = new VoiceInteractor.PickOptionRequest.Option("work", 1);
            option1.addSynonym("business");
            VoiceInteractor.PickOptionRequest.Option option2 = new VoiceInteractor.PickOptionRequest.Option("home", 2);
            option2.addSynonym("personal");

            VoiceInteractor.Prompt prompt = new VoiceInteractor.Prompt("Pick a Category");
            getVoiceInteractor()
                    .submitRequest(new VoiceInteractor.PickOptionRequest(prompt, new VoiceInteractor.PickOptionRequest.Option[]{option1, option2}, null) {
                        @Override
                        public void onPickOptionResult(boolean finished, Option[] selections, Bundle result) {
                            if (finished && selections.length >= 1) {
                                Log.v(TAG, "Option is " + selections[0].getLabel());
                                category = selections[0].getLabel().toString();
                                finishup();
                            } else {
                                finish();
                            }
                        }

                        @Override
                        public void onCancel() {
                            finish();

                        }
                    });
        } else {
            Log.v(TAG, "IsVoiceInterACtion is false?");
            finish();
        }
    }

}
