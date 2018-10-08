package edu.cs4730.text2speech;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

//Nothing to see here.  See the MainFragment for actual code.

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .add(R.id.container, new MainFragment()).commit();
        }
    }
}
