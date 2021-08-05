package edu.cs4730.voiceactionsdemo;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

/*
 * Nothing really to see here.  It just loads the data if there is any already created by
 * the voiceActionActivity.
 */
public class MainActivity extends AppCompatActivity {

    TextView title, text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences preferences = getSharedPreferences("example",MODE_PRIVATE);

        title = (TextView) findViewById(R.id.note_title);
        text = (TextView) findViewById(R.id.note_text);
        title.setText("Title is " + preferences.getString("title", ""));
        text.setText("Text is " + preferences.getString("text", ""));


    }
}
