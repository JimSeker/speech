package edu.cs4730.spk2txtDemo;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

/*
 * Nothing to see here, see the MainFragment.
 */

public class MainActivity extends ActionBarActivity {

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
