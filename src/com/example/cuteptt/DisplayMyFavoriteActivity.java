package com.example.cuteptt;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class DisplayMyFavoriteActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_my_favorite);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.display_my_favorite, menu);
		return true;
	}

}
