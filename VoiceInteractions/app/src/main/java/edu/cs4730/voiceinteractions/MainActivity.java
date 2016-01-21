package edu.cs4730.voiceinteractions;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/*
 * Nothing really to see here, it is all done in VoiceInteractionActivity
 */

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        finish();

    }
}
