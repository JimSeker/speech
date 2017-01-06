package edu.cs4730.voiceinteractions;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/*
 * Nothing really to see here, it is all done in VoiceInteractionActivity
 */

public class MainActivity extends AppCompatActivity {
    String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//        finish();

    }
    @Override
    public void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if (intent == null) {
            Log.v(TAG, "No Intent!");
        } else if (isVoiceInteraction()) {
            Log.v(TAG, "Intent is " + intent.getAction());
            Log.v(TAG, "it's working!");
        } else if (isVoiceInteractionRoot()) {  //started by voice only?  I think. doc's are not clear here.
            Log.v(TAG, "Intent is " + intent.getAction());
            Log.v(TAG, "it's working?");
        } else {
            Log.v(TAG, "Intent is " + intent.getAction());
            Log.v(TAG, "No Voice Interaction");

        }
       // finish();
    }
}
