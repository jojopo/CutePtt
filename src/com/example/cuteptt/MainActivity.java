package com.example.cuteptt;

import java.net.InetAddress;
import java.net.Socket;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View; //view
import android.view.Window;
import android.widget.EditText;
import com.jojopo.cuteptt.R;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void login_submit(View view)
	{
		MyGlobal G = ((MyGlobal)getApplicationContext());
		if(G.getSocketClient() != null)
		{
			SocketClient S = G.getSocketClient(); 
			//System.out.printf("check G.socketclient is not NULL\n");
			S.terminal();
			S.closeSocket();
			G.setSocketClient(null);
		}

		EditText accountEdit = (EditText) findViewById(R.id.accountEditText);
		EditText passwordEdit = (EditText) findViewById(R.id.passwordEditText);
		String account = accountEdit.getText().toString();
		String password = passwordEdit.getText().toString();
		saveLoginInfo(account, password);
		finish();
	}
	
	private void saveLoginInfo(String account, String password)
	{
		SharedPreferences setting = getSharedPreferences("cuteptt_login_info", 0);
		setting.edit().putString("account", account)
		.commit();
		setting.edit().putString("password", password)
		.commit();
		//System.out.printf("save account:%s\n", account);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if(keyCode == KeyEvent.KEYCODE_BACK){
			MyGlobal G = ((MyGlobal)getApplicationContext());
			G.exit();
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
}
