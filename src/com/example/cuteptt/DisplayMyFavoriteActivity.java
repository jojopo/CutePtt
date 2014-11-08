package com.example.cuteptt;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.jojopo.cuteptt.R;

public class DisplayMyFavoriteActivity extends Activity {
	TextView pLoginStatus;
	int state = 0;
	MyFavorite mFavorite;
	private SocketClient S;
	private MyGlobal G;
	private HandlerThread mThread;
	private Handler mThreadHandler;
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			ProgressBar pBar = (ProgressBar)findViewById(R.id.progressBar_loginStatus);
			//TextView pLoginStatus = (TextView)findViewById(R.id.textView_loginstatus);
			
			//System.out.printf("message %d\n", msg.what);
			switch(msg.what) {
				case 0x00:
					pBar.setVisibility(View.VISIBLE);
					pLoginStatus.setVisibility(View.VISIBLE);
					pLoginStatus.setText("連上批踢踢");
					break;
				case 0x01:
					pLoginStatus.setText("重複登入帳號");
					break;
				case 0x02:
					pLoginStatus.setText("登入頻繁訊息");
					break;
				case 0x04:
					pLoginStatus.setText("發現登入記錄");
					break;
				case 0x05:
					pBar.setVisibility(View.INVISIBLE);
					pLoginStatus.setText("登入成功，讀取我的最愛..");
					state = 1;
					mThreadHandler.post(loadMyFavoriteJob);
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
				case -3:
					pBar.setVisibility(View.INVISIBLE);
					pLoginStatus.setText("登入失敗");
					//logout(null);
					break;
					
				case 8:
					pLoginStatus.setVisibility(View.INVISIBLE);
					FavoriteListAdapter adapter = new FavoriteListAdapter(getApplicationContext(), R.layout.favoritelistview, mFavorite.favoriteList);
					ListView listView_favorite = (ListView)findViewById(R.id.listView1);
	                listView_favorite.setAdapter(adapter);
	                listView_favorite.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	                	@Override
	                	public void onItemClick(AdapterView arg0, View arg1, int arg2, long arg3) {
	                		ListView listView = (ListView) arg0;
	                		Context context = getApplicationContext();
	                		if(((BoardInfo)(listView.getItemAtPosition(arg2))).forward.compareTo("Σ") == 0)
	                		{
	                			Toast.makeText(context, "還不支援進入目錄唷", Toast.LENGTH_SHORT).show();
	                			return;
	                		}
	                		String board = ((BoardInfo)(listView.getItemAtPosition(arg2))).board;
	                		//Context context = getApplicationContext();
	                		Toast.makeText(context, board, Toast.LENGTH_SHORT).show();
	                		Intent intent = new Intent(context, ArticleListActivity.class);
	                		
	                		intent.putExtra("board", board);
	                		startActivity(intent);
	                	}
					});

					break;
				default:
					//System.out.printf("unknown message\n");
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//System.out.printf("[onCreate]\n");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_my_favorite);
		
		pLoginStatus = (TextView)findViewById(R.id.textView_loginstatus);
		G = ((MyGlobal)getApplicationContext());
		
		mFavorite = new MyFavorite();
		
		restoreLoginInfo();
		
		
		if(G.getAccount().compareTo("") == 0 || G.getPassword().compareTo("") == 0)
		{
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			return;
		}
		
		init_sock_thread();
		mThreadHandler.post(loginJob);

		//String[] str = {"ac", "bc", "cc"};
		//ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, str);
        //ListView listView1 = (ListView)findViewById(R.id.listView1);
        //listView1.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.display_my_favorite, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	    // action with ID action_settings was selected
	    case R.id.action_settings:
	      logout(null);
	      break;
	    default:
	      break;
	    }

	    return true;
	} 
	
	Runnable loadMyFavoriteJob = new Runnable() {
		
		Message msg;
		private void sendMessage(int type)
		{
			msg = new Message();
			msg.what = type;
			mHandler.sendMessage(msg);
		}
		
		@Override
		public void run() {
			String query;
			int lastFavoriteListItemNumber;
			int firstItemNumber;
			int lastItemNumber;
			int cnt = 0;
			
			if(S.isInSubList())
			{
				//System.out.printf("[w] in SubList\n");
				return;
			}
			
			mFavorite.favoriteList.clear();
			
			try{
			//mFavorite
			query = String.format("%sqqqq%s", Constant.ANSI_ETX, Constant.ANSI_SUB);
			S.send(query);
			while(S.isParsingData() || !S.isInSubList())
			{
				Thread.sleep(10);
			}
			
			query = String.format("%s%sf%s", Constant.ANSI_ETX, Constant.ANSI_SUB, Constant.ANSI_END);
			S.send(query);
			while(S.isParsingData() || !S.isInBoardList())
			{
				Thread.sleep(10);
			}
			
			lastFavoriteListItemNumber = S.getLastItemNumber();
			lastItemNumber = lastFavoriteListItemNumber;
			firstItemNumber = S.getFirstItemNumber();
			
			//System.out.printf("lastFavoriteItemNumber:%d  firstItemNumber:%d lastItemNumber:%d\n", lastFavoriteListItemNumber, firstItemNumber, lastItemNumber);
			
			if(lastFavoriteListItemNumber > 0)
			{
				int page = 0;
				outerLoop:
				do {
					int cnt2 = 0;
					if(lastFavoriteListItemNumber > 20) {
						S.send(String.format("%s", Constant.ANSI_PAGEDOWN));
						while(S.isParsingData() || S.getFirstItemNumber() == firstItemNumber || S.getLastItemNumber() == lastItemNumber)
						{
							cnt2++;
							if(cnt2>500)
                            {
								//System.out.printf("BREAK\n");
								S.displayData();
								break outerLoop;
                            }
							Thread.sleep(10);
						}
					}
						
						firstItemNumber = S.getFirstItemNumber();
						lastItemNumber = S.getLastItemNumber();
						
						page++;
						//System.out.printf("getLastItemNumber:%d firstItemNumber:%d page:%d\n", S.getLastItemNumber(), firstItemNumber, page);
					
						for(int i=S.FIRST_LIST_ROW; i<S.FIRST_LIST_ROW+S.getItemsNumber(); i++)
						{
							BoardInfo bInfo = new BoardInfo();
							bInfo.fill(S.data[i]);
							mFavorite.favoriteList.add(bInfo);
						}
						//System.out.printf("Get page %d, %d_%d\n", page, firstItemNumber, S.getLastItemNumber());
				} while(S.getLastItemNumber() < lastFavoriteListItemNumber);
				
				
				//System.out.printf("%d items\n", mFavorite.favoriteList.size());
				sendMessage(8);
			}
			
			} catch(java.lang.InterruptedException e)
			{}
		}
	};

	Runnable loginJob = new Runnable() {
		
		Message msg;
		private void sendMessage(int type)
		{
			msg = new Message();
			msg.what = type;
			mHandler.sendMessage(msg);
		}
		
		@Override
		public void run() {
			try {
			//Message msg;
			String query;
			int state = 0;
			
			while(S.isParsingData() || !S.isInLogin())
			{
				Thread.sleep(10);
			}
			//System.out.printf("login status\n");
			sendMessage(0);

			query = String.format("%s\r%s\r", G.getAccount(), G.getPassword());
			S.send(query);
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
				//System.out.printf("login failed\n");
				sendMessage(-2);
			}
			else if(S.isWrongPassword())
			{
				sendMessage(-1);
			}
			//System.out.printf("state:%d\n", state);

			if(state == 1)
			{
				sendMessage(1);
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
				sendMessage(2);
				query = String.format("%s", Constant.ANSI_ETX);
				S.send(query);
				while(S.isParsingData() || !S.isInWelcome())
				{
					Thread.sleep(10);
				}
				state = 3;
			}

			if(state == 3)
			{
				query = String.format("%s", Constant.ANSI_ETX);
				S.send(query);
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
				sendMessage(4);
				while(S.isParsingData() || !S.isInMain())
				{
					Thread.sleep(10);
				}
				state = 5;
			}

			if(state == 5)
			{
				//System.out.printf("In Main\n");
				sendMessage(5);
			}
			else
			{
				//System.out.printf("Error\n");				
				sendMessage(-3);
			}
			
			} catch(java.lang.InterruptedException e)
			{
				//System.out.printf("loginJob InterruptedException\n");
			}
		}
	};
	
	@Override
	public void onResume() {
		super.onResume();
		//System.out.printf("[onResume]\n");
		
		restoreLoginInfo();
		
		if(G.isExit())
		{
			//System.out.printf("G.isExit()\n");
			uninit_sock_thread();
			finish();
			System.exit(0);
			return;
		}
		
		if(G.getSocketClient() == null)
		{
			if(init_sock_thread())
				mThreadHandler.post(loginJob);
		}
	}
	
	public boolean init_sock_thread()
	{
		if ( (G.getAccount().compareTo("") == 0) || (G.getPassword().compareTo("") == 0) )
			return false;
		
		S = new SocketClient();
		G.setSocketClient(S);
		S.start();
		
		mThread = new HandlerThread("name");
		mThread.start();
		
		mThreadHandler = new Handler(mThread.getLooper());
		//mThreadHandler.post(loginJob);
		
		return true;
	}
	
	@Override
	public void onDestroy() {
		//System.out.printf("onDestroy\n");
	    super.onDestroy();
	    uninit_sock_thread();
	}
	
	public void uninit_sock_thread()
	{
		if (mThread != null) {
	        mThread.quit();
	        mThread.interrupt();
	    }

		if(S != null)
	    {
			S.terminal();
			S.closeSocket();
			G.setSocketClient(null);
			S = null;
			if(S == null)
				;//System.out.printf("S == NULL\n");
			else
				;//System.out.printf("S != NULL\n");
	    }
	}

	public void logout(View view)
	{
		uninit_sock_thread();
		pLoginStatus.setText("");
		mFavorite.favoriteList.clear();
		
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}
	
	private void restoreLoginInfo()
	{
		SharedPreferences setting = getSharedPreferences("cuteptt_login_info", 0);
		if(setting != null) {
			String account = setting.getString("account", "");
			//System.out.printf("read SharedPreferences account:%s\n", account);
			String password = setting.getString("password", "");
			//System.out.printf("read SharedPreferences password:%s\n", password);
			G.setAccount(account);
			G.setPassword(password);
		}
		else {
			//System.out.printf("can't read SharedPreferences\n");
		}
	}
	
}
