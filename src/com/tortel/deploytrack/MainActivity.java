package com.tortel.deploytrack;

import android.os.Bundle;
import android.view.KeyEvent;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;

public class MainActivity extends SherlockFragmentActivity {
	private Menu settingsMenu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.main, menu);
		settingsMenu = menu;
		return true;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent e){
		switch(keyCode){
		case KeyEvent.KEYCODE_MENU:
			settingsMenu.performIdentifierAction(R.id.full_menu_settings, 0);
			return true; 
		}
		
		return super.onKeyUp(keyCode, e);
	}
}
