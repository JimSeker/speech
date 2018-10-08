package edu.cs4730.speech2textdemo2;

import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 *  Nothing to see here, look at MainFragment.
 *  The request premissions is handled here and then calls back to the fragment.
 */

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_PERM_ACCESS = 1;
    MainFragment myMainFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myMainFragment = new MainFragment();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .add(R.id.container, myMainFragment).commit();
        }
    }

    //handle the response.
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERM_ACCESS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    myMainFragment.logthis("Contact Read Access: Granted");
                    myMainFragment.RecordSpeak();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    myMainFragment.logthis("Contact Read Access: Not Granted");
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

}
