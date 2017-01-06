package edu.cs4730.voiceactionsdemo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.actions.NoteIntents;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

/*
 * To test.  Say "Ok Google, Create Note"
 *       May need to tell it VoiceActivionActivity  (see the screen)
 *    She will say, "What's the Note?"
 *      Then something like Get groceries.  You can then open the app to see if it saved.
 *
 *   this is google Voice Actions https://developers.google.com/voice-actions/system/#system_actions_reference
 *   not to be confused with Voice Interactions or "Actions on Google"
 *
 *   https://developers.google.com/android/reference/com/google/android/gms/appindexing/package-summary
 *     for the Thing and Thing.builder used at the end.
 *
 */

public class VoiceActionActivity extends AppCompatActivity {
    String title, text;
    String TAG = "VoiceActionAct";
    private static final Uri APP_URI = Uri.parse("android-app://edu.cs4730.voiceactionsdemo/VoiceActionActivity");

    private GoogleApiClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent == null) {
            finish();

        //} else if (isVoiceInteraction()) {  //yes called by voice, not some other way  API 23+

        // } else if (intent.getAction().equals("com.google.android.gms.actions.CREATE_NOTE")) {  //direct action intent name.
        } else if (NoteIntents.ACTION_CREATE_NOTE.equals(intent.getAction())) {
            // text = intent.getExtras().getString(NoteIntents.EXTRA_TEXT, "nothing?");   //This one doesn't work?!
            //these work...
            title = intent.getExtras().getString("android.intent.extra.SUBJECT", ""); //title, optional and likely not there.
            text = intent.getExtras().getString("android.intent.extra.TEXT", "nothing?");

            Log.v(TAG, "It worked: " + text);

            //store the note.
            SharedPreferences preferences = getSharedPreferences("example", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("title", title);
            editor.putString("text", text);
            editor.commit();


            //What the hell are the keys and all the data anyway?  There are a lot of them.
            Bundle ab = intent.getExtras();

            for (String key : ab.keySet()) {
                Object value = ab.get(key);
                Log.d(TAG, String.format("%s %s (%s)", key,
                        value.toString(), value.getClass().getName()));
            }

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
            finish();
        }
    }
}
