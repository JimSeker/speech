package edu.cs4730.voiceinteractions;

import android.app.VoiceInteractor;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;



/*   Voice interactions are problematic and my guess are going to be replaced with the google actions anyway.
 *   many of the intents don't support voice (even though you can take it anyway, like all the music intents left in this example)
 *
 *   this example show the command "Take a selfie" or  "take a picture."
 *   speak one of the two commands.  selfie impies the front camera, so it will only confirm you action
 *   while take a picture will ask front or back camera.
 *
 *   btw, te taxi example they show on the main page, only works from android wear....
 *   https://developers.google.com/voice-actions/interaction/voice-interactions
 *
 *   The base of this code is from here http://io2015codelabs.appspot.com/codelabs/voice-interaction#6  but has been heavy cleaned up and changed.
 */

public class VoiceInteractionActivity extends AppCompatActivity {
    ListView myList;
    String[] values = new String[]{"Front", "Back", "Other"};
    String TAG = "VoiceActionAct";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voiceinteraction);

    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if (intent == null) {  //not sure how this would happen, so just exit.
            Log.v("TAG", "No Intent!");
            finish();

        } else if (isVoiceInteraction()) {
            Log.v(TAG, "Intent is " + intent.getAction());
            printkeysAnd(intent);

        } else if (isVoiceInteractionRoot()) {  //started by voice only?  I think. doc's are not clear here.
            Log.v(TAG, "Intent is " + intent.getAction());
            printkeysAnd(intent);
            Log.v(TAG, "it's working!?");
        } else {
            Log.v(TAG, "Intent is " + intent.getAction());
            //print the keys anyway, to see that is there.
            Bundle ab = intent.getExtras();
            for (String key : ab.keySet()) {
                Object value = ab.get(key);
                Log.d("VAA Key", String.format("%s %s (%s)", key,
                        value.toString(), value.getClass().getName()));
            }
            Log.v(TAG, "No Voice Interaction");
            finish();
        }
    }

    /*
     *  a helper function to print out all the keys in the intent check if android.intent.extra.USE_FRONT_CAMERA is true.
     */
    private void printkeysAnd(Intent intent) {
        boolean frontcamera = false;
        //first print out are the keys and all the data anyway?  There are a lot of them.
        Bundle ab = intent.getExtras();
        frontcamera = ab.getBoolean("android.intent.extra.USE_FRONT_CAMERA", false);
        for (String key : ab.keySet()) {
            Object value = ab.get(key);
            Log.d("VAA Key", String.format("%s %s (%s)", key,
                    value.toString(), value.getClass().getName()));
        }
        Log.v(TAG, "It worked, onto Next step.");

        if (frontcamera) { //said take a selfie, so default to front camera, just confirm.
            //so now confirm it, then fake the picture and say success I guess.
            String ttsPrompt = "Use the front camera";
            String visualPrompt = "Using the front camera?";
            getVoiceInteractor().submitRequest(new Confirm(ttsPrompt, visualPrompt));
        } else {
            //draw the list so the user can see what the options are, using a simple listview.
            myList = (ListView) findViewById(R.id.list1);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                    android.R.layout.simple_list_item_1, values);
            myList.setAdapter(adapter);
            //now setup and call the initial voice trigger.
            startVoiceTrigger();
        }
    }

    /*
     * extend the request class, so the the work for Take a selfie.
     */
    class Confirm extends VoiceInteractor.ConfirmationRequest {
        public Confirm(String ttsPrompt, String visualPrompt) {
            //super must come first, so their code is well wrong... shocker...
            super(new VoiceInteractor.Prompt(
                            new String[]{ttsPrompt}, visualPrompt
                    )
                    , null);
        }

        @Override
        public void onConfirmationResult(boolean confirmed, Bundle result) {
            Bundle status = new Bundle();  //the picture should be in the bundle.
            VoiceInteractor.Request request = null;

            if (confirmed) {
                //here is where we would take the picture.  except I'm faking it.
                request = new VoiceInteractor.CompleteVoiceRequest(new VoiceInteractor.Prompt("Success"), status);
            } else {
                request = new VoiceInteractor.AbortVoiceRequest(new VoiceInteractor.Prompt("Too Complex"), status);
            }
            getVoiceInteractor().submitRequest(request);

            finish();
        }
    }

    /*
     * instead of extending the class, we are just using the submitRequest and options in the method.
     */
    private void startVoiceTrigger() {

        Log.d(TAG, "startVoiceTrigger: ");

        //Doing this the Hard way likely.
        VoiceInteractor.PickOptionRequest.Option[] mOptions = new VoiceInteractor.PickOptionRequest.Option[]{
                new VoiceInteractor.PickOptionRequest.Option(values[0], 0).addSynonym("Forward").addSynonym("selfie"),
                new VoiceInteractor.PickOptionRequest.Option(values[1], 1).addSynonym("default").addSynonym("Rear").addSynonym("normal"),
                new VoiceInteractor.PickOptionRequest.Option(values[2], 2).addSynonym("whatever")
        };

        //instead of extending the class, just use it here.
        VoiceInteractor.Prompt prompt = new VoiceInteractor.Prompt("Which camera would you like to use?");
        getVoiceInteractor().submitRequest(new VoiceInteractor.PickOptionRequest(prompt, mOptions, null) {
            @Override
            public void onPickOptionResult(boolean finished, Option[] selections, Bundle result) {
                if (finished && selections.length >= 1) {
                    //so doc's say it could be more then one selection, so just choose first one.
                    Log.v(TAG, "Option is " + selections[0].getLabel());

                    //here we would use the camera

                    //then send the complete
                    getVoiceInteractor().submitRequest(
                            new VoiceInteractor.CompleteVoiceRequest(
                                    new VoiceInteractor.Prompt("Took picture with " + selections[0].getLabel()),
                                    new Bundle()
                            ) {  //anonymous class to see the complete result message.  Which should be uninteresting.
                                @Override
                                public void onCompleteResult(Bundle result) {
                                    super.onCompleteResult(result);
                                    Log.d(TAG, "OnCompleteResult:");  //dig out the result in bundle if we really cared.
                                }
                            }
                    );
                    finish();
                } else {  //nothing was selected, so...
                    getVoiceInteractor().submitRequest(
                            new VoiceInteractor.AbortVoiceRequest(
                                    new VoiceInteractor.Prompt("Too Complex"),
                                    new Bundle()
                            )
                    );
                    finish();
                }
            }

            @Override
            public void onCancel() {
                Log.v(TAG, "User Canceled.");
                getVoiceInteractor().submitRequest(
                        new VoiceInteractor.AbortVoiceRequest(
                                new VoiceInteractor.Prompt("User Canceled"),
                                new Bundle()
                        )
                );
                finish();

            }
        });
    }
}
