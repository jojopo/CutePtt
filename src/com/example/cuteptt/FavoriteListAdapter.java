package com.example.cuteptt;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.jojopo.cuteptt.R;

public class FavoriteListAdapter extends ArrayAdapter {
	private Context mContext;
    private int id;
    private List <BoardInfo>items ;
    

    public FavoriteListAdapter(Context context, int textViewResourceId , List<BoardInfo> list ) 
    {
        super(context, textViewResourceId, list);           
        mContext = context;
        id = textViewResourceId;
        items = list ;
    }
    
    @Override
    public int getViewTypeCount() {
        return 2;
    }
    
    @Override
    public int getItemViewType(int position) {
        if(items.get(position).board.compareTo("------------") != 0) {
            return 0; // matches R.layout.date_separator
        }
        else {
            return 1; // matches R.layout.match_layout
        }
    }

    @Override
    public View getView(int position, View v, ViewGroup parent)
    {
        View mView = v ;
        if(mView == null){
        	//System.out.printf("mView == null \n");
            LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(items.get(position).board.compareTo("------------") != 0)
            	mView = vi.inflate(id, null);
            else
            	mView = vi.inflate(R.layout.favorite_divide_line_view, null);
        }

        

        if(items.get(position) != null )
        {
        	
        	if(items.get(position).board.compareTo("------------") != 0)
        	//if(true)
        	{
        		
    		TextView textBoard = (TextView) mView.findViewById(R.id.textView_board);
            TextView textDescription = (TextView) mView.findViewById(R.id.textView_description);

        	textBoard.setTextColor(Color.WHITE);
        	textBoard.setText(items.get(position).board);
        	//textBoard.setBackgroundColor(Color.RED); 
            //int color = Color.argb( 200, 255, 64, 64 );
            //textBoard.setBackgroundColor( color );

            textDescription.setText(items.get(position).description);
        	}
        	else
        	{
        		//LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        		//mView = vi.inflate(R.layout.favorite_divide_line_view, null);
        	}
        }

        return mView;
    }
}
