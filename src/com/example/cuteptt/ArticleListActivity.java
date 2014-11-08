package com.example.cuteptt;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Parcelable;
import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Interpolator;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AbsoluteLayout;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Scroller;
import android.widget.Toast;
import com.jojopo.cuteptt.R;
import android.app.ActionBar;
//import android.support.v7.app.ActionBar;
//import android.support.v7.app.ActionBarActivity;


public class ArticleListActivity extends Activity {
//public class ArticleListActivity extends ActionBarActivity {
	
	private SocketClient S;
	private MyGlobal G;
	private PostList mPostList;
	private String mBoard;
	private boolean loadingBoard = false;
	private PostListAdapter adapter;
	private int mNumber;
	private List<PostInfo> loadedlist;
	
	private List<View> listViews;
	private ViewPager viewPager;
	private View main;
	
	boolean viewPagerQuit = false;
	
	private HandlerThread mThread;
	private Handler mThreadHandler;
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			ProgressBar pBar = (ProgressBar)findViewById(R.id.progressBar_BoardStatus);
			//TextView pLoginStatus = (TextView)findViewById(R.id.textView_loginstatus);
			
			//System.out.printf("message %d\n", msg.what);
			switch(msg.what) {
				case -1:
					Toast.makeText(getApplicationContext(), "not in article list now..", Toast.LENGTH_SHORT).show();
					break;
				case 0x00:
					break;
				case 0x08:
					adapter = new PostListAdapter(getApplicationContext(), R.layout.postlistview, mPostList.list);
					//ListView listView_board = (ListView)findViewById(R.id.listView1);
					ListView listView_board = (ListView)main.findViewById(R.id.listView1);
	                listView_board.setAdapter(adapter);
	                listView_board.setOnScrollListener(new AbsListView.OnScrollListener() {
	                	@Override
	                	public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
	                		final int lastItem = firstVisibleItem + visibleItemCount;
	                		if (!loadingBoard && lastItem >= totalItemCount - 8)
	                		{
	                			//System.out.printf("lastItem=%d totalItemCount=%d\n", lastItem, totalItemCount);
	                			loadingBoard = true;
	                			mThreadHandler.post(lodingBoardJob);
	                		}
	                	}

						@Override
						public void onScrollStateChanged(AbsListView view,
								int scrollState) {
							// TODO Auto-generated method stub
							
						}
	                });
	                
	                listView_board.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	                	@Override
	                	public void onItemClick(AdapterView arg0, View arg1, int arg2, long arg3) {
	                		if(loadingBoard)
	                		{
	                			Toast.makeText(getApplicationContext(), "loading article list..", Toast.LENGTH_SHORT).show();
	                			return;
	                		}
	                		ListView listView = (ListView) arg0;

	                		mNumber = ((PostInfo)(listView.getItemAtPosition(arg2))).number;
	                		Context context = getApplicationContext();
	                		//Toast.makeText(context, String.valueOf(mNumber), Toast.LENGTH_SHORT).show();
	                		
	                		mThreadHandler.post(enterArticleJob);
	                		
	                		//Intent intent = new Intent(context, ArticlActivity.class);
	                		//intent.putExtra("number", mNumber);
	                		//startActivity(intent);
	                	}
					});
					break;
				case 0x01:
					if(true)
					//for(int i=S.FIRST_LIST_ROW+S.getItemsNumber()-1; i>=S.FIRST_LIST_ROW; i--)
					for(int i=0; i<loadedlist.size(); i++)
		            {
		                    //System.out.printf("%s\n", new String(S.data[i], 0, S.COL, "big5"));
		                    //PostInfo pInfo = new PostInfo();
		                    //pInfo.fill(S.data[i]);
		                    mPostList.list.add(loadedlist.get(i));
		                    
		                    
		            }
					adapter.notifyDataSetChanged();
					loadedlist.clear();
					//ListView listView_board2 = (ListView)findViewById(R.id.listView1);
	                //listView_board2.setVisibility(View.GONE);
					//adapter.notifyDataSetChanged();
					//listView_board2.setVisibility(View.VISIBLE);
					loadingBoard = false;
					break;
				case 11:
					mPostList.list.add(loadedlist.get(0));
                    adapter.notifyDataSetChanged();
                    loadedlist.remove(0);
					break;
				case 0x02:
					loadingBoard = false;
					break;
				case 0x09:
					Intent intent = new Intent(getApplicationContext(), ArticlActivity.class);
            		intent.putExtra("number", mNumber);
            		startActivity(intent);
					break;
				default:
					//System.out.printf("unknown message\n");
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//System.out.printf("onCreate\n");
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_article_list);
		
		/*
		G = ((MyGlobal)getApplicationContext());
		S = G.getSocketClient();
		
		mPostList = new PostList();
		loadedlist = new ArrayList<PostInfo>();
		
		Intent intent = getIntent();
		mBoard = intent.getStringExtra("board");
		
		setTitle(mBoard);
		*/
		
		/**/
		//
		//main = (ViewGroup) this.findViewById(R.id.main);
		main = View.inflate(getApplicationContext(), R.layout.activity_article_list, null);
		listViews = new ArrayList<View>();
		//listViews.add(View.inflate(getApplicationContext(), R.layout.item01, null));
		//listViews.add(View.inflate(getApplicationContext(), R.layout.item01, null));
		listViews.add(View.inflate(getApplicationContext(), R.layout.item01, null));
		listViews.add(main);
		//listViews.add(this.findViewById(R.layout.activity_article_list));
		//listViews.add(View.inflate(getApplicationContext(), R.layout.favorite_divide_line_view, null));
		
		//viewPager = (ViewPager) this.findViewById(R.id.v_Pager);
		//viewPager = (ViewPager) main.findViewById(R.id.v_Pager);
		viewPager = new ViewPager(this) {
			@Override
		    public boolean onTouchEvent(MotionEvent event) {
		        // Never allow swiping to switch between pages
		        if(viewPagerQuit)
		        	return false;
		        return super.onTouchEvent(event);
		    }
			
			@Override
		    public boolean onInterceptTouchEvent(MotionEvent event) {
		        // Never allow swiping to switch between pages
				if(viewPagerQuit)
		        	return false;
		        return super.onInterceptTouchEvent(event);
		    }
		};
		viewPager.setAdapter(new GuidePageAdapter());
		viewPager.setCurrentItem(1);
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			//boolean close = false;
			
			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub
				//System.out.printf("onPageSelected:%d\n", arg0);
				if(arg0 == 0)
				{
					leave_article();
					viewPagerQuit = true;
					//finish();
				}
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				//System.out.printf("onPageScrolled:%d %f %d\n", arg0, arg1, arg2);
				if(viewPagerQuit && arg0 == 0 && arg1 == 0)
					finish();
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				// TODO Auto-generated method stub
				//if(viewPagerQuit && state == ViewPager.SCROLL_STATE_IDLE)
					//finish();
			}
		});
		
		try{
		Field mScroller;
	    mScroller = ViewPager.class.getDeclaredField("mScroller");
	    mScroller.setAccessible(true); 
	    //Interpolator sInterpolator = (Interpolator) new AccelerateInterpolator();
	    FixedSpeedScroller scroller = new FixedSpeedScroller(viewPager.getContext());
	    mScroller.set(viewPager, scroller);
		} catch (Exception e) {
	    }
		
		
		setContentView(viewPager);
		

		//setContentView(main);
		/**/
		
		G = ((MyGlobal)getApplicationContext());
		S = G.getSocketClient();
		
		mPostList = new PostList();
		loadedlist = new ArrayList<PostInfo>();
		
		Intent intent = getIntent();
		mBoard = intent.getStringExtra("board");
		
		setTitle(String.format("看板 %s", mBoard));
		
		if(init_thread())
			mThreadHandler.post(enterBoardJob);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.article_list, menu);
		return true;
	}
	
	@Override
	public void onResume() {
		if(adapter != null)
			adapter.notifyDataSetChanged();
		super.onResume();
	}

	@Override
	public void onDestroy() {
		//System.out.printf("onDestroy\n");
	    super.onDestroy();
	    uninit_thread();
	    //leave_article();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if(keyCode == KeyEvent.KEYCODE_BACK){
			//finish();
			leave_article();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		//Toast.makeText(getApplicationContext(), "onTouchEvent", Toast.LENGTH_SHORT).show();
		//System.out.printf("onTouchEvent (%f,%f)\n", event.getX(), event.getY());
		super.onTouchEvent(event);
		return true;
	}
	
	void onSearch()
	{
		
	}
	
	private boolean init_thread()
	{
		mThread = new HandlerThread("board");
		mThread.start();
		
		mThreadHandler = new Handler(mThread.getLooper());

		return true;
	}
	
	public void uninit_thread()
	{
		if (mThread != null) {
	        mThread.quit();
	        mThread.interrupt();
	        //System.out.printf("mThread.interrupt()\n");
	    }
	}
	
	public void leave_article()
	{
		S.send("q");
		//System.out.printf("q\n");
	}

	Runnable enterBoardJob = new Runnable() {

		Message msg;
		
		private void sendMessage(int type)
		{
			msg = new Message();
			msg.what = type;
			mHandler.sendMessage(msg);
		}
		
		public void run() {
			String query;
			String boardName = ""; 
			
			//System.out.printf("on enterBoardJob\n");
			if(S.isInSubList())
			{
				//System.out.printf("[w] in SubList\n");
				return;
			}
			
			try{
			query = String.format("%s%s", Constant.ANSI_ETX, Constant.ANSI_SUB);
			S.send(query);
			
			while(S.isParsingData() || !S.isInSubList())
			{
				//System.out.printf("d0\n");
				Thread.sleep(10);
			}
			//System.out.printf("d1\n");

			query = String.format("fs%s\r\nr%s%s%s", mBoard, Constant.ANSI_ETX, Constant.ANSI_ETX, Constant.ANSI_END);
			S.send(query);
			
			while(S.isParsingData() || !S.isInPostList())
            {
                    Thread.sleep(10);
            }
			//System.out.printf("d2\n");
			
			String topString = S.getSpecifyData(S.TOP_ROW, 0, S.COL);
			int boardNameStart = topString.lastIndexOf('《') + 1;
			
			if (boardNameStart > 0)
			{
				String[] board = topString.substring(boardNameStart).split("[》]");
				boardName = board[0];
				//System.out.printf("%s\n", boardName);
			}
			else
			{
				//System.out.printf("can't get board name\n");
			}
			
			//System.out.printf("d3\n");
			
			if(boardName.compareTo(mBoard) == 0 || boardNameStart == 0)
			{
				
				//System.out.printf("mBoard:%s", mBoard);
				//int firstNumber = S.getFirstItemNumber();
				
				int lastItemNumber = S.getLastItemNumber();
	            int bottomItemNumber = S.getBottomItemsNumberThisPage();
	            //System.out.printf("bottomItemNumber:%d\n", bottomItemNumber);
				
				for(int i=S.FIRST_LIST_ROW+S.getItemsNumber()-1; i>=S.FIRST_LIST_ROW; i--)
                {
                        //System.out.printf("%s\n", new String(S.data[i], 0, S.COL, "big5"));
                        PostInfo pInfo = new PostInfo();
                        int fakeBottomItemNumber = 0;
                        
                        if(bottomItemNumber > 0)
                        {
                        	fakeBottomItemNumber = lastItemNumber+bottomItemNumber;
                        	bottomItemNumber--;
                        	pInfo.fill(S.data[i], fakeBottomItemNumber);
                        }
                        else
                        	pInfo.fill(S.data[i]);
                        mPostList.list.add(pInfo);
                }
				sendMessage(8);
			}

			} catch(java.lang.InterruptedException e)
			{
				//System.out.printf("InterruptedException e\n");
			}
		}
	};
	
	Runnable lodingBoardJob = new Runnable() {

		Message msg;
		
		private void sendMessage(int type)
		{
			msg = new Message();
			msg.what = type;
			mHandler.sendMessage(msg);
		}
		
		public void run() {
			
			try{
				
			String query;
			
			if(S.isInSubList())
			{
				//System.out.printf("[w] in SubList\n");
				sendMessage(2);
				return;
			}
			
			if(!S.isInPostList())
			{
				//System.out.printf("[w] not in PostList\n");
				sendMessage(2);
				return;
			}
			
			int targetNumber;
			
			if(mPostList.latestLoadingNumber == 0)
			{
				int firstNumber = S.getFirstItemNumber();
				//System.out.printf("top1:%d\n", firstNumber);
				if(firstNumber == 1)
					return;
				
				if(S.getSecondItemNumber() - firstNumber > 100000)
					firstNumber += 100000;

				targetNumber = (firstNumber - 10 < 1) ? 1 : firstNumber - 10;
			}
			else
			{
				if(mPostList.latestLoadingNumber == 1)
					return;
				
				if(S.getSecondItemNumber() - mPostList.latestLoadingNumber > 100000)
					mPostList.latestLoadingNumber += 100000;
				targetNumber = (mPostList.latestLoadingNumber - 10 < 1) ? 1 : mPostList.latestLoadingNumber - 10;
			}

			query = String.format("%d", targetNumber);
            S.send(query);
            while(S.isParsingData() || !S.isInInputNumberState())
            {
            	//System.out.printf("[waiting to inupt number state]\n");
            	Thread.sleep(10);
            }

            query = String.format("\r\n");
            S.send(query);
            while(S.isParsingData() || !S.isInPostList())
            {
            	//System.out.printf("[waiting to post state]\n");
                    Thread.sleep(10);
            }
            //System.out.printf("top2:%d\n",  S.getFirstItemNumber());

            if (true)
            for(int i=S.FIRST_LIST_ROW+S.getItemsNumber()-1; i>=S.FIRST_LIST_ROW; i--)
            {
                    //System.out.printf("%s\n", new String(S.data[i], 0, S.COL, "big5"));
                    PostInfo pInfo = new PostInfo();
                    
                    pInfo.fill(S.data[i]);
                    loadedlist.add(pInfo);
                    
                    //mPostList.list.add(pInfo);
                    //sendMessage(11);
            }
            //loadingBoard = false;
            
            mPostList.latestLoadingNumber = S.getFirstItemNumber();
            
            sendMessage(1);
            //Toast.makeText(getApplicationContext(), "讀取下一頁文章完成", Toast.LENGTH_SHORT).show();

			} catch(java.lang.InterruptedException e)
			{}
		}
	};
	
	Runnable enterArticleJob = new Runnable() {

		Message msg;
		
		private void sendMessage(int type)
		{
			msg = new Message();
			msg.what = type;
			mHandler.sendMessage(msg);
		}
		
		public void run() {
			String query;
			
			if(!S.isInPostList())
			{
				sendMessage(-1);
				return;
			}
			
			try{

			sendMessage(9);
			
			query = String.format("%s%s", Constant.ANSI_ETX, Constant.ANSI_SUB);
			S.send(query);
			
			while(S.isParsingData() || !S.isInSubList())
			{
				Thread.sleep(10);
			}
			
			query = String.format("%s%d\r\n\r\ni", Constant.ANSI_ETX, mNumber);
			S.send(query);
			

			} catch(java.lang.InterruptedException e)
			{}
		}
	};
	
	
	class GuidePageAdapter extends PagerAdapter {
		 
        @Override
        public int getCount() {
            return listViews.size();
        }
 
        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }
 
        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }
 
        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView(listViews.get(arg1));
        }
 
        @Override
        public Object instantiateItem(View arg0, int arg1) {
            ((ViewPager) arg0).addView(listViews.get(arg1));
            return listViews.get(arg1);
        }
 
        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
 
        }
 
        @Override
        public Parcelable saveState() {
            return null;
        }
 
        @Override
        public void startUpdate(View arg0) {
        }
 
        @Override
        public void finishUpdate(View arg0) {
        }
    }
	
	public class FixedSpeedScroller extends Scroller {

	    private int mDuration = 100;

	    public FixedSpeedScroller(Context context) {
	        super(context);
	    }

	    public FixedSpeedScroller(Context context, Interpolator interpolator) {
	        super(context);
	    }

	    public FixedSpeedScroller(Context context, Interpolator interpolator, boolean flywheel) {
	        super(context);
	    }


	    @Override
	    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
	        // Ignore received duration, use fixed one instead
	        super.startScroll(startX, startY, dx, dy, mDuration);
	    }

	    @Override
	    public void startScroll(int startX, int startY, int dx, int dy) {
	        // Ignore received duration, use fixed one instead
	        super.startScroll(startX, startY, dx, dy, mDuration);
	    }
	}
}
