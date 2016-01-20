package edu.cs4730.voiceinteractions;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

/*
 * Nothing really to see here, it is all done in VoiceInteractionActivity
 */

public class MainActivity extends AppCompatActivity {

    TextView title, text, category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = getSharedPreferences("example",MODE_PRIVATE);

        title = (TextView) findViewById(R.id.note_title);
        text = (TextView) findViewById(R.id.note_text);
        category = (TextView) findViewById(R.id.note_cat);
        title.setText("Title is " + preferences.getString("title", ""));
        text.setText("Text is " + preferences.getString("text", ""));
        category.setText("Cat is " + preferences.getString("category", ""));

    }
}
