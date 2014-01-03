package com.example.cuteptt;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DisplayMyFavoriteActivity extends Activity {
	private SocketClient S;
	private MyGlobal G;
	private HandlerThread mThread;
	private Handler mThreadHandler;
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			ProgressBar pBar = (ProgressBar)findViewById(R.id.progressBar_loginStatus);
			TextView pLoginStatus = (TextView)findViewById(R.id.textView_loginstatus);
			
				System.out.printf("message %d\n", msg.what);
			switch(msg.what) {
				case 0x00:
					pBar.setVisibility(View.VISIBLE);
					break;
				case 0x01:
					
					break;
				case 0x05:
					pBar.setVisibility(View.INVISIBLE);
					pLoginStatus.setText("登入成功");
					break;
				case -1:
					pBar.setVisibility(View.INVISIBLE);
					pLoginStatus.setText("密碼錯誤");
					logout(null);
					break;
				case -2:
					pBar.setVisibility(View.INVISIBLE);
					pLoginStatus.setText("登入太頻繁");
					logout(null);
					break;
				default:
					System.out.printf("unknown message\n");
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		System.out.printf("[onCreate]\n");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_my_favorite);
		
		G = ((MyGlobal)getApplicationContext());
		
		restoreLoginInfo();
		
		
		
		
		
		if(G.getAccount().compareTo("") == 0)
		{
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			return;
		}
		
		init_sock_thread();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.display_my_favorite, menu);
		return true;
	}

	Runnable loginJob = new Runnable() {
		public void run() {
			try {
			Message msg;
			String query1;
			int state = 0;
			
if(true){
			while(S.isParsingData() || !S.isInLogin())
			{
				Thread.sleep(10);
			}
			System.out.printf("login status\n");
			msg = new Message();
			msg.what = 0x00;
			mHandler.sendMessage(msg);

			query1 = String.format("%s\r0713\r", G.getAccount());
			S.send(query1);
			//S.send("frog3\r0713\r");

			while(S.isParsingData() || (!S.isInWelcome() && !S.isInLoginRepeat() && !S.isInKickOtherAccount() && !S.isInLoginRepeat2() && !S.isWrongPassword()))
			{
				Thread.sleep(10);
			}

			if(S.isInKickOtherAccount())
			{
				state = 1;
			}
			else if(S.isInLoginRepeat())
			{
				state = 2;
			}
			else if(S.isInWelcome())
			{
				state = 3;
			}
			else if(S.isInLoginRepeat2())
			{
				state = -1;
				System.out.printf("login failed\n");
				msg = new Message();
				msg.what = -2;
				mHandler.sendMessage(msg);
			}
			else if(S.isWrongPassword())
			{
				msg = new Message();
				msg.what = -1;
				mHandler.sendMessage(msg);
			}
			System.out.printf("state:%d\n", state);

			if(state == 1)
			{
				S.send("n\r");
				while(S.isParsingData() || (!S.isInWelcome() && !S.isInLoginRepeat()))
				{
					Thread.sleep(10);
				}
				if(S.isInLoginRepeat())
					state = 2;
				else if(S.isInWelcome())
					state = 3;
			}

			if(state == 2)
			{
				query1 = String.format("%s", Constant.ANSI_ETX);
				S.send(query1);
				while(S.isParsingData() || !S.isInWelcome())
				{
					Thread.sleep(10);
				}
				state = 3;
			}

			if(state == 3)
			{
				query1 = String.format("%s", Constant.ANSI_ETX);
				S.send(query1);
				while(S.isParsingData() || (!S.isInClearIpRecord() && !S.isInMain()))
				{
					Thread.sleep(10);
				}

				if(S.isInClearIpRecord())
					state = 4;
				else if(S.isInMain())
					state = 5;
			}

			if(state == 4)
			{
				S.send("n\r");
				while(S.isParsingData() || !S.isInMain())
				{
					Thread.sleep(10);
				}
				state = 5;
			}

			if(state == 5)
			{
				//System.out.printf("In Main\n");
				msg = new Message();
				msg.what = 0x05;
				mHandler.sendMessage(msg);
			}
			else
			{
				msg = new Message();
				msg.what = 0x05;
				//msg.what = -1;
				//mHandler.sendMessage(msg);
				System.out.printf("Error\n");
			}
			
			
}
			} catch(java.lang.InterruptedException e)
			{
				System.out.printf("Exception\n");
			}
		}
	};
	
	@Override
	public void onResume() {
		super.onResume();
		System.out.printf("[onResume]\n");
		
		restoreLoginInfo();
		
		if(G.isExit())
		{
			System.out.printf("G.isExit()\n");
			uninit_sock_thread();
			finish();
			System.exit(0);
			return;
		}
		
		if(G.getSocketClient() == null)
		{
			init_sock_thread();
		}
	}
	
	public void init_sock_thread()
	{
		if(G.getAccount().compareTo("") == 0)
			return;
		
		S = new SocketClient();
		G.setSocketClient(S);
		S.start();
		
		mThread = new HandlerThread("name");
		mThread.start();
		
		mThreadHandler = new Handler(mThread.getLooper());
		mThreadHandler.post(loginJob);
	}
	
	@Override
	public void onDestroy() {
		System.out.printf("onDestroy\n");
	    super.onDestroy();
	    uninit_sock_thread();
	}
	
	public void uninit_sock_thread()
	{
		if (mThread != null) {
	        mThread.quit();
	    }
		if(S != null)
	    {
			S.terminal();
			S.closeSocket();
			G.setSocketClient(null);
			S = null;
			if(S == null)
				System.out.printf("S == NULL\n");
			else
				System.out.printf("S != NULL\n");
	    }
	}

	public void logout(View view)
	{
		uninit_sock_thread();
		
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}
	
	private void restoreLoginInfo()
	{
		SharedPreferences setting = getSharedPreferences("cuteptt_login_info", 0);
		if(setting != null) {
			
			String account = setting.getString("account", "");
			System.out.printf("read SharedPreferences account:%s\n", account);
			G.setAccount(account);
		}
		else {
			System.out.printf("can't read SharedPreferences\n");
		}
	}
	
}
