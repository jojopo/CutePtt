package com.example.cuteptt;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils.TruncateAt;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.jojopo.cuteptt.R;

public class PostListAdapter extends ArrayAdapter {

	private Context mContext;
    private int id;
    private List <PostInfo>items ;

	public PostListAdapter(Context context, int textViewResourceId , List<PostInfo> list )
	{
		super(context, textViewResourceId, list);           
		mContext = context;
		id = textViewResourceId;
		items = list ;
	}
	
	@Override
    public View getView(int position, View v, ViewGroup parent)
	{
		View mView = v ;
		if(mView == null) {
            LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
           	mView = vi.inflate(id, null);
        }
		
		if(items.get(position) != null )
		{
			TextView textTitle = (TextView) mView.findViewById(R.id.textView_title);
			TextView textTitleClass = (TextView) mView.findViewById(R.id.textView_titleClass);
			TextView textLike = (TextView) mView.findViewById(R.id.textView_like);
			TextView textNumber = (TextView) mView.findViewById(R.id.textView_number);
			
			String titleText;
			if (items.get(position).title != null)
				titleText = items.get(position).title;
			else
				titleText = items.get(position).titleSimple;
			textTitle.setText(titleText);
			textTitle.setEllipsize(TruncateAt.END);
			
			char readFlag = items.get(position).readFlag;
			
			MyGlobal G = (MyGlobal)mContext;
			if(G.getLastArticleNumber() == items.get(position).number)
				textTitle.setTextColor(Color.GREEN);
			else if(readFlag == '+' || readFlag == 'M')
				textTitle.setTextColor(Color.WHITE);
			else
				textTitle.setTextColor(Color.GRAY);
			
			//android:background="#228B22"
			if(items.get(position).titleClass == null)
			{
				textTitleClass.setVisibility(View.GONE);
			}
			else
			{
				textTitleClass.setVisibility(View.VISIBLE);
				textTitleClass.setText(items.get(position).titleClass);
				textTitleClass.setTextColor(Color.WHITE);
			}
			
			int color = 0;
			int like = items.get(position).like;
			String sLike = String.valueOf(like);
			if(like == 100)
				sLike = "Ãz";
			else if(like == 0)
				sLike = "";
			
			if(like < 10)
				color = Color.GREEN;
			else if(like > 10 && like < 100)
				color = Color.YELLOW;
			else if(like == 100)
				color = Color.RED;
			
			//android:background="#333388"
			textLike.setText(sLike);
			textLike.setTextColor(color);
			
			String sNumber = String.valueOf(items.get(position).number);
			//String s_Number_author = sNumber + " " + items.get(position).author;
			SpannableStringBuilder s_Number_author = new SpannableStringBuilder(new String(sNumber + " " + items.get(position).author));
			int offset = s_Number_author.length();
			if(readFlag == '~')
			{
				s_Number_author.append(" ~");
				s_Number_author.setSpan(new ForegroundColorSpan(Color.GREEN), offset+1, offset+2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			textNumber.setText(s_Number_author);
			textNumber.setTextColor(Color.GRAY);
			
		}
		return mView;
	}
}
