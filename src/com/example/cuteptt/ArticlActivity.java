package com.example.cuteptt;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.jojopo.cuteptt.R;

public class ArticlActivity extends Activity {
	
	private SocketClient S;
	private MyGlobal G;
	int mNumber;
	String mContent = "";
	String mUrl = "";
	SpannableStringBuilder mSP = new SpannableStringBuilder("");
	TextView addview;
	boolean addingView = false;
	boolean addingShowPicView = false;
	
	
	float downX, downY;
	boolean decideDirection = false;
	boolean horizontal = false;
	
	private HandlerThread mThread;
	private Handler mThreadHandler;
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			ProgressBar pBar = (ProgressBar)findViewById(R.id.progressBar_BoardStatus);
			//TextView pLoginStatus = (TextView)findViewById(R.id.textView_loginstatus);
			//TextView view = (TextView)findViewById(R.id.textView_content);
			LinearLayout layout = (LinearLayout)findViewById(R.id.linearViewGroup_content);
			
			Log.d("ArticlActivity", "message "+Integer.toString(msg.what));
			switch(msg.what) {
				case -1:
					finish();
					break;
				case 0x06:
					//String tmp = (view.getText().toString()) + mContent;
					//view.setText(tmp);
					break;
				case 0x07:
					//view.setText(mSP);
					break;
				case 0x08:
					//view.setText(mContent);
					break;
				case 0x09:
					
					layout.removeView(addview);
					layout.addView(addview);
					//mContent = "";
					addingView = false;
					break;
				case 0x0a:
					//LinearLayout layout = (LinearLayout)findViewById(R.id.linearViewGroup_content);
					View vi = View.inflate(getApplicationContext(), R.layout.download_pic_view, null);
					//ImageView iv = new ImageView(getApplicationContext());
					layout.addView(vi);
					DownloadImageView dlView = new DownloadImageView(vi, mUrl);
					//DownloadImageTask task = (DownloadImageTask) new DownloadImageTask(iv).execute("http://java.sogeti.nl/JavaBlog/wp-content/uploads/2009/04/android_icon_256.png");
					mUrl = "";
					addingShowPicView = false;
					break;
				default:
					Log.d("ArticlActivity", "unknown message");
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_articl);
		
		G = ((MyGlobal)getApplicationContext());
		S = G.getSocketClient();
		
		Intent intent = getIntent();
		mNumber = intent.getIntExtra("number", -1);
		//System.out.printf("mNumber:%d\n", mNumber);
		Log.d("ArticlActivity", "mNumber:"+Integer.toString(mNumber));
		
		if(init_thread())
			mThreadHandler.post(enterArticleJob);
		
		/*
		LinearLayout layout = (LinearLayout)findViewById(R.id.linearViewGroup_content);
		View vi = View.inflate(getApplicationContext(), R.layout.download_pic_view, null);
		//ImageView iv = new ImageView(getApplicationContext());
		layout.addView(vi);
		DownloadImageView dlView = new DownloadImageView(vi);
		//DownloadImageTask task = (DownloadImageTask) new DownloadImageTask(iv).execute("http://java.sogeti.nl/JavaBlog/wp-content/uploads/2009/04/android_icon_256.png");
		*/
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.articl, menu);
		return true;
	}
	
	@Override
	public void onDestroy() {
		Log.d("ArticlActivity", "onDestroy");
		uninit_thread();
	    //leave_article();
	    super.onDestroy();
	    
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
	public boolean dispatchTouchEvent(MotionEvent event) {
		float x;
		float y;
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
			downX = event.getX();
			downY = event.getY();
			//System.out.printf("ACTION_DOWN (%f,%f)\n", downX, downY);
        	//return false;
			decideDirection = false;
			horizontal = false;
			break;
        case MotionEvent.ACTION_MOVE:
        	
        	x=event.getX();
        	y=event.getY();
        	if(!decideDirection)
        	{
        		if(Math.abs(x - downX) > 50)
        		{
        			decideDirection = true;
        			horizontal = true;
        		}
        		else if(Math.abs(y - downY) > 50)
        		{
        			decideDirection = true;
        			horizontal = false;
        		}
        	}
        	
        	//System.out.printf("ACTION_MOVE (%f,%f)\n", event.getX(), event.getY());
        	break;
        case MotionEvent.ACTION_UP:
        	
        	float upX = event.getX();
            float upY = event.getY();
            //System.out.printf("ACTION_UP (%f,%f)\n", upX, upY);
            
			x=Math.abs(upX-downX);
			y=Math.abs(upY-downY);
			double z=Math.sqrt(x*x+y*y);
			int angle = Math.round((float)(Math.asin(y/z)/Math.PI*180));
            
            if(horizontal && upX > downX && angle < 30)
            {
            	//Toast.makeText(getApplicationContext(), "right", Toast.LENGTH_SHORT).show();
            	leave_article();
            	//uninit_thread();
            	finish();
            	//return false;
            }
            
            if(upX < downX)
            {
            	//Toast.makeText(getApplicationContext(), "left", Toast.LENGTH_SHORT).show();
            	//return false;
            }
        	break;
        }
 
        if(decideDirection)
        {
        	super.dispatchTouchEvent(event);
        	return false;
        }
        else
        	return super.dispatchTouchEvent(event);
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
	    }
	}
	
	public void leave_article()
	{
		S.send("q");
		Log.d("ArticlActivity", "q");
	}

	Runnable enterArticleJob = new Runnable() {

		Message msg;
		Context context;
		
		private void update()
		{
			if(context == null)
				context = getApplicationContext();
			
			if(addingView == true)
			{
				Log.d("ArticlActivity", "override addview !!!");
				while(addingView)
				{
					try {
					Thread.sleep(1);
					} catch(java.lang.InterruptedException e)
					{}
				}
			}
			
			//System.out.printf("update:%s\n", mSP);
			
			if(mSP.length() == 0)
				return;

			if(mSP.charAt(mSP.length()-1) == '\n')
				mSP.delete(mSP.length()-1, mSP.length());
			
			addview = new TextView(context);
			//addview.setText(mContent);
			addview.setBackgroundColor(Color.BLACK);
			addview.setTextColor(Color.WHITE);
			addview.setText(mSP);
			addview.setTextSize(16);//size
			addingView = true;
			sendMessage(0x09);
			mSP = new SpannableStringBuilder("");
			mContent = "";
		}
		
		private void updateTitle()
		{
			if(context == null)
				context = getApplicationContext();
			
			if(addingView == true)
			{
				Log.d("ArticlActivity", "override addview !!!");
				while(addingView)
				{
					try {
					Thread.sleep(1);
					} catch(java.lang.InterruptedException e)
					{}
				}
			}
			
			int colorBlue = Color.rgb(0x00, 0x00, 0xA0);
			
			addview = new TextView(context);
			//addview.setText(mContent);
			addview.setBackgroundColor(colorBlue);
			addview.setTextColor(Color.WHITE);
			//addview.setSingleLine();
			//addview.setLines(3);
			//addview.setEllipsize(TruncateAt.END);
			addview.setText(mSP);
			addingView = true;
			sendMessage(0x09);
			mSP = new SpannableStringBuilder("");
			mContent = "";
		}
		
		private void updateShowPic(String url)
		{
			if(addingShowPicView == true)
			{
				Log.d("ArticlActivity", "override adding ShowPicView !!!");
				while(addingShowPicView)
				{
					try {
					Thread.sleep(1);
					} catch(java.lang.InterruptedException e)
					{}
				}
			}
			mUrl = url;
			addingShowPicView = true;
			sendMessage(0x0A);
			//mUrl = "";
		}
		
		private void sendMessage(int type)
		{
			msg = new Message();
			msg.what = type;
			mHandler.sendMessage(msg);
		}
		
		public void run() {
			String query;
			
			try{
			
			while(S.isParsingData() || S.isInSubList() || S.isInPostList())
			{
				Thread.sleep(10);
			}
			
			if(S.isInBoardSetting())
			{
				Log.d("ArticlActivity", "article deleted");
				leave_article();
				sendMessage(-1);
				return;
			}
			else if(!S.isInArticle())
			{
				Log.d("ArticlActivity", "unknown enable read article");
				S.displayData();
				leave_article();
				sendMessage(-1);
				return;
			}

			G.setLastArticleNumber(mNumber);

			int n1, n2;
			int lineCnt = 1;
			//String line;
			int flushColorCnt = 0;
			boolean isStandardTitle = true;

			Log.d("ArticlActivity", "S.getArticleReadPersent:"+Integer.toString(S.getArticleReadPersent()));
			//while(S.getArticleReadPersent() <= 4)
			//while(lineCnt <= 25)
			while(true)
			{
				int cntCr = 0;
				int crRecord = 0;
				int breakLine = 0;
				int localLineCnt = 0;
				String[] infoString = {"", "", ""};

				Log.d("ArticlActivity", "S.getArticleReadPersent:%d\n"+Integer.toString(S.getArticleReadPersent()));
				n1 = S.getFirstLineNumberShown();
				n2 = S.getLastLineNumberShown();
				Log.d("ArticlActivity", Integer.toString(n1)+"-"+Integer.toString(n2));

				if( (n2 - n1 < 22) && (n1 >= 5) )
				{
					cntCr = 22 - (n2-n1);
					Log.d("ArticlActivity", "[1] Detect "+Integer.toString(22 - (n2-n1))+" CR");
				}
				else if( (n2 - n1 < 21) && (n1 <= 4) && (n1 >= 2) )
				{
					cntCr = 21 - (n2-n1);
					Log.d("ArticlActivity", "[2] Detect "+Integer.toString(21 - (n2-n1))+" CR");
				}
				else if( (n2 - n1 < 21) && (n1 == 1) && (S.getArticleReadPersent() != 100) )
				{
					cntCr = 21 - (n2-n1);
					Log.d("ArticlActivity", "[3] Detect "+Integer.toString(21 - (n2-n1))+" CR");
				}
				else if( (n2 - n1 < 21) && (n1 == 1) && (S.getArticleReadPersent() == 100) )
				{
					//CR = 1;
				}

				if(cntCr > 0)
				{
					int CrOffRecord = 0, CrOnRecord = 0;
					String field;
					//S.setShowCrOption(false);
					for(int i=0; i<S.ROW-1; i++)
					{
						field = S.getSpecifyData(i, 78, 1);
						if(field.compareTo("\\") == 0)
						{
							CrOffRecord |= 1 << i;
						}
					}

					S.setShowCrOption(true);
					for(int i=0; i<S.ROW-1; i++)
					{
						field = S.getSpecifyData(i, 78, 1);
						if(field.compareTo("\\") == 0)
						{
							CrOnRecord |= 1 << i;
						}
					}

					crRecord = CrOffRecord^CrOnRecord;
					//System.out.printf("%08X  %08X  %08X\n", CrOffRecord, CrOnRecord, crRecord);


					if( (crRecord & 1 << 22) != 0 )
					{
						for(int i=22; i>=0; i--)
						{
							if ( (crRecord & 1 << i) != 0 )
								continue;
							else
							{
								breakLine = i;
								break;
							}
						}
						Log.d("ArticlActivity", "detect last row is CR, breakLine is "+Integer.toString(breakLine));
					}
					S.setShowCrOption(false);
				}

				localLineCnt = n1;

				for(int i=0; i<S.ROW-1; i++)
				{
					int k = 0, len = 0;
					boolean skipSP = false;
					boolean isPushLine = false;
					ArrayList array;

					//System.out.printf("i:%d localLineCnt:%d lineCnt:%d\n", i, localLineCnt, lineCnt);
					if(localLineCnt < lineCnt)
					{
						// following condition NOT increase line number
                        // 1.) this line == 4 && isStandardTitle
                        // 2.) this line is CR
						if ( (n1 + i != 4 || !isStandardTitle) && (crRecord & 1 << i) == 0 )
							localLineCnt++;
						continue;
					}

					 
					String line = S.getSpecifyData(i, 0, S.COL);
					isPushLine = S.isPushLine(i);
					//System.out.printf("[i:%d][localLineCnt:%d][lineCnt:%d]%s\n", i, localLineCnt, lineCnt, line);
					//System.out.printf("[%d]%s\n", i, line);
					
					line = line.replaceAll("\\s*$" , "");
				
					/* Title */
					if (n1 + i >= 1 && n1 + i <= 4)
					{
						if (n1 + i == 1)
						{
							if(line.length() > 5 && line.substring(0, 5).compareTo(" 作者  ") == 0)
							{
								int offset = line.lastIndexOf(')')+1;
								if(offset == 0)
								{
									offset = line.lastIndexOf('看')-1;
								}
									
								infoString[0] = line.substring( 5, offset ).trim();

								//System.out.printf("authorString:%s\n", infoString[0]);
							}
							else
								isStandardTitle = false;
						}
						else if (n1 + i == 2)
						{
							if(line.length() > 5 && line.substring(0, 5).compareTo(" 標題  ") == 0)
							{
								infoString[1] = line.substring(5);
								//System.out.printf("titleString:%s\n", infoString[1]);
							}
							else
								isStandardTitle = false;
						}
						else if (n1 + i == 3)
						{
							if(line.length() > 5 && line.substring(0, 5).compareTo(" 時間  ") == 0)
							{
								infoString[2] = line.substring(5);
								//System.out.printf("timeString:%s\n", infoString[2]);
							}
							else
								isStandardTitle = false;
						}
						
						if (n1 + i == 4 && isStandardTitle)
						{
							int offset;
							String[] tag = {"作者", "標題", "時間"};
							mSP = new SpannableStringBuilder("");
							
							
							for(int j=0; j<3; j++)
							{
								//mSP = new SpannableStringBuilder("");
								offset = mSP.length();
								
								mSP.append(String.format(" %s  %s%s", tag[j], infoString[j], j!=2?"\n":""));
								mSP.setSpan(new ForegroundColorSpan(Color.BLUE), offset, offset+4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
								mSP.setSpan(new BackgroundColorSpan(Color.WHITE), offset, offset+4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
								//mSP.setSpan(new AbsoluteSizeSpan(50), offset, offset+4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
								//updateTitle();
							}
							skipSP = true;
							updateTitle();
						}
					}
					
					/* push */
					if(isPushLine)
					{
						int colorGold = Color.rgb(0xFF, 0xB9, 0x0F);
						//mSP.append("isPushLine");
						String author = S.getPushAuthor(i);
						String content = S.getPushContent(i);
						String type = S.getPushTypeStr(i);
						int pushType = S.getPushType(i);
						int offset = mSP.length();
						
						//mSP.append(String.format("%s %s:%s%s", type, author, content,  (i != S.ROW-1-1)?"\n":""  ));
						mSP.append(String.format("%s %s:%s\n", type, author, content));
						
						if(pushType == 0 || pushType == 2)
							mSP.setSpan(new ForegroundColorSpan(Color.RED), offset, offset+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						
						mSP.setSpan(new ForegroundColorSpan(colorGold), offset + 2 + author.length() + 1, offset + 2 + author.length() + 1 + content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						
						//System.out.printf("[push %d]%s\n", i, line);
						
						skipSP = true;
					}

					/* default show */
					if(!skipSP)
					{
					mContent = String.format("%s%s", mContent, line);
					//System.out.printf("[%d]%s\n", i, line);
					len = mSP.length();
					mSP.append(line);
					
					int continusStartSpan = -1;
					int continusEndSpan = -1;
					int continusColor = -1;
					int continusColorValue = -1;

					if(true)
					for(int j=0; j<line.length(); j++)
					{
						//System.out.printf("len:%d j:%d len+j:%d\n", len, j, len+j);
						int color = S.colorMap[i][j+k] & 0x00f;
						
						//if(true)
						for(int m=0; m<6; m++)
						{
							if(color == TColor.e[m].code)
							{
								if(continusColor == -1)
								{
									continusStartSpan = len+j;
									continusEndSpan = continusStartSpan;
									continusColor = color;
									continusColorValue = TColor.e[m].value;
								}
								else if(color == continusColor)
								{
									continusEndSpan++;
								}
								else if(color != continusColor)
								{
									flushColorCnt++;
									mSP.setSpan(new ForegroundColorSpan(continusColorValue), continusStartSpan, continusEndSpan+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
									continusStartSpan = len+j;
									continusEndSpan = continusStartSpan;
									continusColor = color;
									continusColorValue = TColor.e[m].value;
								}

								if(j == line.length()-1 && continusStartSpan != -1)
								{
									flushColorCnt++;
									mSP.setSpan(new ForegroundColorSpan(continusColorValue), continusStartSpan, continusEndSpan+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
								}
								
								break;
							}
							else if(m == 5)
							{
								if(continusColor != -1)
								{
									flushColorCnt++;
									mSP.setSpan(new ForegroundColorSpan(continusColorValue), continusStartSpan, continusEndSpan+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

									continusStartSpan = -1;
									continusEndSpan = -1;
									continusColor = -1;
									continusColorValue = -1;
								}
							}
						}
						if(line.charAt(j) > 256)
							k++;
					}
					//System.out.printf("mSP length:%d\n", mSP.length());
					//System.out.printf("mContent length:%d\n", mContent.length());
					
					//if (i != S.ROW-1-1)
					{
						mContent = mContent.format("%s\n", mContent);
						mSP.append("\n");
					}
					
					
					
					} /* default show */
					
					//if (mContent == null) mContent = String.format("%s\n", wordtoSpan);
					//else mContent += String.format("%s\n", wordtoSpan);
					
					array = retrieveLinks(line);
					if(array.size() > 0)
					{
						Log.d("ArticlActivity", "retrieveLinks get size "+Integer.toString(array.size()));
						update();
						updateShowPic((String)array.get(0));
					}
					
				
					if (n1 + i == 1)
					{
						if(line.length() < 5 || line.substring(0, 5).compareTo(" 作者  ") != 0)
							isStandardTitle = false;
					}
					else if (n1 + i == 2)
					{
						if(line.length() < 5 || line.substring(0, 5).compareTo(" 標題  ") != 0)
							isStandardTitle = false;
					}
					else if (n1 + i == 3)
					{
						if(line.length() < 5 || line.substring(0, 5).compareTo(" 時間  ") != 0)
							isStandardTitle = false;
					}

					if ( (n1 + i != 4 || !isStandardTitle) && (crRecord & 1 << i) == 0 )
					{
						localLineCnt++;
						lineCnt++;
						//System.out.printf("->%d ->%d\n", localLineCnt, lineCnt);
					}


					if(breakLine > 0 && i == breakLine)
					{
						//System.out.printf("breakLine %d\n", breakLine);
						break;
					}

				}

				if(S.getArticleReadPersent() == 100)
				{
					update();
					break;
				}
				
				//sendMessage(0x09);
				
				if(true)
				if(n1 >= 1 && n1 <= 20)
				{
					update();
				}	
				else if(n1 >= 20 && n1 <= 40)
				{	
					update();
				}
				else if(n1 >= 90 && n1 <= 110)
				{
					update();
				}
				else if(n1 >= 150 && n1 <= 170)
				{
					update();
					//break;
				}
				

				if(breakLine == 0)
				{
					query = String.format("%s", Constant.ANSI_PAGEDOWN);
					S.send(query);

					while( S.isParsingData() || (n1 == S.getFirstLineNumberShown() && n2 == S.getLastLineNumberShown() && n1 != -1 && n2 != -1) )
					{
						Thread.sleep(1);
					}
				}
				else
				{
					S.setShowCrOption(true);
					while(true){
						query = String.format("%s", Constant.ANSI_ARROW_DOWN);
						S.send(query);
						while( S.isParsingData() || n1 == S.getFirstLineNumberShown() )
						{
							Thread.sleep(1);
						}
						n1 = S.getFirstLineNumberShown();
						if(S.getSpecifyData(S.LAST_LIST_ROW, 78, 1).compareTo(" ") == 0)
						{
							S.setShowCrOption(false);
							break;
						}
					}
				}
			}
			//System.out.printf("flushColorCnt:%d\n", flushColorCnt);
			//sendMessage(0x06);
			//sendMessage(0x07);
			//sendMessage(0x08);

			} catch(java.lang.InterruptedException e)
			{}
		}
	};
	
	
	private class DownloadImageView {
		View v;
		ImageView bmImage;
		TextView bmText;
		DownloadImageTask task;
		String url;
		boolean touched = false;
		public DownloadImageView(View view, String _url) {
			v = view;
			bmImage = (ImageView) v.findViewById(R.id.imageView1);
			bmText = (TextView) v.findViewById(R.id.textView_showPic);
			//task = new DownloadImageTask(bmImage, bmText, _url);
			url = _url;
			
			DetectLink detect = new DetectLink(bmImage, bmText, v);
	        detect.execute(url);

	        if(false)
			bmText.setOnTouchListener(new View.OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					if(touched)
						return false;

					touched = true;
					//System.out.printf("DownloadImageView:onTouch\n");
					bmText.setText("Downloading..");
					//task.execute("http://java.sogeti.nl/JavaBlog/wp-content/uploads/2009/04/android_icon_256.png");
					//task.execute("http://ppt.cc/r4qU@.jpg");
					//task.execute("http://ppt.cc/r4qU");
					//task.execute(url);
					
					return false;
				}
			});
		}
	}
	
	//private class DetectLink extends AsyncTask<String, Void, Boolean> {
	private class DetectLink extends AsyncTask<String, Void, Bitmap> {
		boolean isLinkPptcc = false;
		boolean isLinkImgur = false;
		boolean isPicture = false;
		ImageView bmImage;
		Bitmap bmBitmap;
		TextView bmText;
		View v;
		DownloadImageTask task;
		String mUrl;
		boolean touched = false;
			
		public DetectLink(ImageView bmImage, TextView bmText, View v) {
			this.bmText = bmText;
			this.v = v;
			this.bmImage = bmImage;
			//this.touched = touched;
		}	
		//protected Boolean doInBackground(String... urls) {
		protected Bitmap doInBackground(String... urls) {
			Bitmap mIcon11 = null;
			String urldisplay = urls[0];
			mUrl = urldisplay;
			//isLinkPptcc = isLinkPptcc(urldisplay);

			if(true)
	        {
		        String referer = urldisplay;
		        
		        try {
			        URL url = new URL(urldisplay);
			        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
	        		InputStream stream = connection.getInputStream();
		        	BufferedInputStream in = new BufferedInputStream(stream);
		            mIcon11 = BitmapFactory.decodeStream(in);
		            in.close();
		            if(mIcon11 == null)
		            {
		            	//System.out.printf("detect not picture\n");
		            	
		            	if(isLinkPptcc(urldisplay))
		            	{
		            		isLinkPptcc = true;
		            		//System.out.printf("isLinkPptcc true\n");
		            	}
		            	else if(isLinkImgur(urldisplay))
		            	{
		            		isLinkImgur = true;
		            		//System.out.printf("isLinkImgur true\n");
		            	}
		            	else
		            		//System.out.printf("all false\n");
		            		
		            	
		            	if(isLinkPptcc || isLinkImgur)
		            	{
		            		if(isLinkPptcc)
		            			urldisplay = String.format("%s@.jpg", urldisplay);
		            		else if(isLinkImgur)
		            		{
		            			urldisplay = String.format("http://i.%s.jpg", urldisplay.substring(urldisplay.indexOf("://")+3));
		            			//System.out.printf("urldisplay:%s\n", urldisplay);
		            		}
		            		url = new URL(urldisplay);
					        connection = (HttpURLConnection)url.openConnection();
					        
				        	if(isLinkPptcc)
				        	{
				        		connection.setInstanceFollowRedirects(false);
				        		connection.setRequestProperty("Referer", referer);
				        	}
			        		stream = connection.getInputStream();
				        	in = new BufferedInputStream(stream);
				            mIcon11 = BitmapFactory.decodeStream(in);
				            //if(mIcon11 != null)
				            	//System.out.printf("detect ppt.cc picture\n");
				            
				            //System.out.printf("getResponseCode:%d %s\n", connection.getResponseCode(), connection.getHeaderField("location"));
				            in.close();
				        
				            /*
				            if(mIcon11 != null)
				            {
				            	mIcon11 = null;
								return true;
				            }
							else return false;
							*/
		            	}
		            }
		            else
		            {
		            	isPicture = true;
		            	//System.out.printf("detect picture\n");
		            }
		        } catch (Exception e) {
		        	//System.out.printf("Exception:%s\n", e.toString());
		        }
		        
	        }
			
			return mIcon11;
			/*
			if(mIcon11 != null)
				return true;
			else return false;
			*/
		}
		
		//protected void onPostExecute(Boolean result) {
		protected void onPostExecute(Bitmap result) {
			
			//System.out.printf("FAonPostExecuteonPostExecuteonPostExecuteLSE\n");
	    	if(result == null)
			//if(result == false)
	    	{
	    		//System.out.printf("FALSE\n");
	    		bmText.setVisibility(View.GONE);
	    	}
	    	else
	    	{
	    		//System.out.printf("TRUE\n");
	    		bmBitmap = result;
	    		//bmBitmap = null;
	    		bmText.setVisibility(View.VISIBLE);
	    		bmText.setOnTouchListener(new View.OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						// TODO Auto-generated method stub
						if(touched)
							return false;

						touched = true;
						//System.out.printf("DownloadImageView:onTouch\n");
						bmText.setText("Downloading..");
						//task.execute("http://java.sogeti.nl/JavaBlog/wp-content/uploads/2009/04/android_icon_256.png");
						//task.execute("http://ppt.cc/r4qU@.jpg");
						//task.execute("http://ppt.cc/r4qU");
						
						//task = new DownloadImageTask(bmImage, bmText, mUrl);
						//task.execute(mUrl);
						
						
						bmImage.setImageBitmap(bmBitmap);
			    		bmText.setVisibility(View.GONE);
						
						return false;
					}
				});
	    	}
	        
	    }
	}
	
	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
	    ImageView bmImage;
	    TextView bmText;
	    boolean isLinkPptcc = false;
	    boolean isLinkImgur = false;
	    boolean notPicture = false;

	    public DownloadImageTask(ImageView bmImage, TextView bmText) {
	        this.bmImage = bmImage;
	        this.bmText = bmText;
	        //System.out.printf("DownloadImageTask no override~~~~~~~~~~~\n");
	        
	        //isLinkPptcc = isLinkPptcc();
	    }
	    
	    public DownloadImageTask(ImageView bmImage, TextView bmText, String urls) {
	        this.bmImage = bmImage;
	        this.bmText = bmText;
	        
	        //System.out.printf("DownloadImageTask override~~~~~~~~~~~\n");
	        
	        
	    }

	    protected Bitmap doInBackground(String... urls) {
	        String urldisplay = urls[0];
	        Bitmap mIcon11 = null;
	        String referer = "";
	        
	        isLinkPptcc = isLinkPptcc(urldisplay);
	        isLinkImgur = isLinkImgur(urldisplay);
	        if(isLinkPptcc)
	        {
	        	referer = urldisplay;
	        	urldisplay = String.format("%s@.jpg", urldisplay);
	        	//System.out.printf("isLinkPptcc\n");
	        }
	        else if(isLinkImgur)
	        {
	        	urldisplay = String.format("http://i.%s.jpg", urldisplay.substring(urldisplay.indexOf("://")+3));
	        }
	        try {
	        	URL url = new URL(urldisplay);
	        	//URL url = new URL("http://java.sogeti.nl/JavaBlog/wp-content/uploads/2009/04/android_icon_256.png");
	        	
	        	//System.out.printf("urldisplay:%s\n", urldisplay);
	        	//URLConnection connection = url.openConnection();
	        	HttpURLConnection connection = (HttpURLConnection)url.openConnection();
	        	
	        	if(isLinkPptcc)
	        	{
	        		connection.setInstanceFollowRedirects(false);
	        		connection.setRequestProperty("Referer", referer);
	        	}
	        	InputStream stream = connection.getInputStream();
	        	BufferedInputStream in = new BufferedInputStream(stream);
	            //InputStream in = new java.net.URL(urldisplay).openStream();
	            mIcon11 = BitmapFactory.decodeStream(in);
	            in.close();
	            
	            int status = connection.getResponseCode();
	            //System.out.printf("status:%d\n", connection.getResponseCode());

	        } catch (Exception e) {
	            //Log.e("Error", e.getMessage());
	            //e.printStackTrace();
	        }
	        return mIcon11;
	    }

	    protected void onPostExecute(Bitmap result) {
	    	if(result == null)
	    	{
	    		if(isLinkPptcc)
	    			bmText.setText("this is link");
	    		else
	    			bmText.setText("fail");
	    		//System.out.printf("bmImage NULL!!!!!!!!!\n");
	    	}
	    	else
	    	{
	    		bmImage.setImageBitmap(result);
	    		bmText.setVisibility(View.GONE);
	    	}
	        
	    }
	}
	
	public ArrayList retrieveLinks(String text) {
        ArrayList links = new ArrayList();

        String regex = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]";
        //String regex = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*.jpg";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        while(m.find()) {
	        String urlStr = m.group();
	        char[] stringArray1 = urlStr.toCharArray();
	
	        if (urlStr.startsWith("(") && urlStr.endsWith(")"))
	        {
	
	            char[] stringArray = urlStr.toCharArray(); 
	
	            char[] newArray = new char[stringArray.length-2];
	            System.arraycopy(stringArray, 1, newArray, 0, stringArray.length-2);
	            urlStr = new String(newArray);
	            //System.out.println("Finally Url ="+newArray.toString());
	
	        }
	        //System.out.println("...Url..."+urlStr);
	        links.add(urlStr);
        }
        return links;
    }
	
	
	public boolean isLinkPptcc(String text) {
        String regex = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]";
        //String regex = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*.jpg";
        
        return text.matches("http://ppt\\.cc/[-A-Za-z0-9][-A-Za-z0-9][-A-Za-z0-9][-A-Za-z0-9]");
        
    }
	
	public boolean isLinkImgur(String text) {
        String regex = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]";
        //String regex = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*.jpg";
        
        return text.matches("http://imgur\\.com/[-A-Za-z0-9][-A-Za-z0-9][-A-Za-z0-9][-A-Za-z0-9][-A-Za-z0-9][-A-Za-z0-9][-A-Za-z0-9]");
        
    }
}
