package com.example.cuteptt;

import java.net.Socket;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.lang.NumberFormatException;

public class SocketClient extends java.lang.Thread{

	Socket client;
	boolean loop = true;
	DataOutputStream out;
	final int ROW = 24;
	final int COL = 80;
	final int TOP_ROW = 0;
	final int LAST_ROW = ROW-1;
	final int LAST_LIST_ROW = ROW-2;
	final int FIRST_LIST_ROW = 3;
	boolean parsingData;
	String lastTag;
	int debug;
	
	byte[][] data;
	int cursorRow, cursorColumn;
	
	String hostName;
	int port;
	InetAddress address;

	public SocketClient()
	{
		//client = socket;

		data = new byte[ROW][COL];
		cursorRow = 0;
		cursorColumn = 0;
		parsingData = false;
		debug = 0;
		lastTag = "";
		
		hostName = "ptt.cc";
		port = 23;
	}

	public void terminal()
	{
		loop = false;
	}

	public void send(String s)
	{
		try {
		out.write(s.getBytes(), 0, s.length());
		}
		catch(java.io.IOException e)
		{
		}
	}

	public void send(byte[] b)
        {
                try {
                out.write(b);
                }
                catch(java.io.IOException e)
                {
                }
        }

	public void writeData(byte[] writeByte, int length)
	{
		for(int i=0; i<length; i++)
		{
			data[cursorRow][cursorColumn] = writeByte[i];
		}
		cursorColumn += length;
	}

	public void writeData(byte writeByte)
        {
		try{
		//if(debug == 0) System.out.printf("<row:%d col:%d> ", cursorRow, cursorColumn);
		data[cursorRow][cursorColumn] = writeByte;
		cursorColumn++;
		//System.out.printf("col:%d\n", cursorColumn);
		}catch(java.lang.ArrayIndexOutOfBoundsException e)
		{
		//	System.out.printf("<row:%d col:%d %c> ", cursorRow, cursorColumn, writeByte);
		}
	}

	public void clearData()
	{
		for(int i=0; i<ROW; i++)
		{
			for(int j=0; j<COL; j++)
			{
				data[i][j] = ' ';
			}
		}
	}

	public void clearLineFromCursorRight()
	{
		if(cursorRow>=ROW || cursorColumn>=COL) {
			System.out.printf("<over row:%d col:%d> ", cursorRow, cursorColumn);
			debug = 1;
			return;
		}
		for(int i=cursorColumn; i<COL; i++)
		{
			data[cursorRow][i] = ' ';
		}
	}

	public void displayData()
	{
		for(int i=0; i<COL; i++)
		{
			if(i == 0)
				System.out.printf("[--]%d", i%10);
			else
				System.out.printf("%d", i%10);
			if(i == COL-1) System.out.printf("\n");
		}
		for(int i=0; i<ROW; i++)
		{
			try{
			String str = new String(data[i], 0, COL, "big5");
			System.out.printf("[%02d]%s\n", i, str);
			}catch(java.io.UnsupportedEncodingException e)
			{}
		}
	}

	public void displayDataN(int n)
	{
		try{
			String str = new String(data[n], 0, COL, "big5");
			//System.out.printf("[%d]%s\n", n, str);

			for(int i=0; i<COL; i++)
				System.out.printf("[%02X] ", data[n][i]);
		}catch(java.io.UnsupportedEncodingException e)
		{}
	}

	public String getSpecifyData(int row, int col, int length)
	{
		if( (row > ROW-1) || (col > COL-1) )
		{
			System.out.printf("incorrect row or col (%d,d)\n", row, col);
			return "";
		}
		byte[] bytes = new byte[length];
		String str = "";

		for(int i=0; i<length; i++)
		{
			bytes[i] = data[row][col+i];
		}

		try{
		str = new String(bytes, 0, length, "big5");
		} catch (java.io.UnsupportedEncodingException e)
		{
		}
		
		return str;
	}

	public boolean isInLogin()
	{
		String s1 = getSpecifyData(20, 31, 11);
		if (s1.compareTo("以 new 註冊") == 0)
		{
			s1 = getSpecifyData(LAST_ROW, 0, 10);
			if (s1.compareTo("          ") == 0)
				return true;
		}
		return false;
	}
	
	public boolean isWrongPassword()
	{
		String s1 = getSpecifyData(21, 0, 18);
		if (s1.compareTo("密碼不對或無此帳號") == 0)
			return true;
		return false;
	}

	public boolean isInKickOtherAccount()
	{
		String s1 = getSpecifyData(22, 0, 28);
		if (s1.compareTo("您想刪除其他重複登入的連線嗎") == 0)
			return true;
		return false;
	}

	public boolean isInWelcome()
	{
		String s1 = getSpecifyData(20, 6, 18);
		if (s1.compareTo("歡迎您再度拜訪本站") == 0)
		{
			s1 = getSpecifyData(LAST_ROW, 47, 2);
			if (s1.compareTo("▄") == 0)
				return true;
		}
		return false;
	}

	public boolean isInLoginRepeat()
	{
		String s1 = getSpecifyData(LAST_ROW, 0, 16);
		if (s1.compareTo(" ◆ 請勿頻繁登入") == 0)
			return true;
		return false;
	}

	public boolean isInLoginRepeat2()
	{
		String s1 = getSpecifyData(LAST_ROW, 0, 10);
		if (s1.compareTo("登入太頻繁") == 0)
			return true;
		return false;
	}

	public boolean isInClearIpRecord()
	{
		String s1 = getSpecifyData(LAST_ROW, 0, 28);
		if (s1.compareTo("您要刪除以上錯誤嘗試的記錄嗎") == 0)
			return true;
		return false;
	}

	public boolean isInMain()
	{
		String s1 = getSpecifyData(TOP_ROW, 0, 12);
		if (s1.compareTo("【主功能表】") == 0)
		{
			s1 = getSpecifyData(LAST_ROW, 66, 8);
			if (s1.compareTo("[呼叫器]") == 0)
				return true;
		}
		return false;
	}

	public boolean isInBoardList()
	{
		String s1 = getSpecifyData(LAST_ROW, 2, 8);
		String s2 = getSpecifyData(TOP_ROW, 2, 8);
		if ( (s1.compareTo("選擇看板") == 0) && (s2.compareTo("看板列表") == 0) )
			return true;
		else
			return false;
	}

	public boolean isInPostList()
	{
		String s1 = getSpecifyData(LAST_ROW, 1, 8);
		if (s1.compareTo("文章選讀") == 0)
			return true;
	 	else if(s1.compareTo("        ") == 0)
		{
			String s2 = getSpecifyData(TOP_ROW, COL-1-2, 2);
			String s3 = getSpecifyData(TOP_ROW, 0, 6);
			if((s2.compareTo("》") == 0) && (s3.compareTo("【板主") == 0))
				return true;
			else return false;
		}
		else
			return false;
	}

	public boolean isInSubList()
	{
		String s1 = getSpecifyData(LAST_ROW, 1, 10);
		if (s1.compareTo("★快速切換") ==  0)
			return true;
		else
			return false;
	}

	public boolean isInInputNumberState()
	{
		String s1 = getSpecifyData(LAST_ROW, 1, 10);
		if (s1.compareTo("跳至第幾項") ==  0)
			return true;
		else
			return false;
	}

	public boolean isInArticle()
	{
		String s1 = getSpecifyData(LAST_ROW, 2, 4);
		if (s1.compareTo("瀏覽") ==  0)
			return true;
		else
			return false;
	}

	public boolean isInArticleOptionSetting()
	{
		String s1 = getSpecifyData(LAST_ROW, 1, 13);
		if (s1.compareTo("◆ 請調整設定") == 0)
			return true;
		else
			return false;
	}

	public String getCurrentBoard()
	{
		String ret;

		String s1 = getSpecifyData(TOP_ROW, COL-1-2, 2);
		if(s1.compareTo("》") != 0)
		{
			ret = "unknow";
		}
		else
		{
			String topString = getSpecifyData(TOP_ROW, 0, COL);
			int boardNameStart = topString.lastIndexOf('《') + 1;	
			String[] board = topString.substring(boardNameStart).split("[》]");
			ret = board[0];
		}
		return ret;
	}

	public int getCursorRow()
	{
		System.out.printf("cursorRow:%d\n", cursorRow);
		return cursorRow;
	}

	public int getItemNumber(int row)
	{
	//	int number;
		try{
		String sNumber = getSpecifyData(row, 2, 5).trim();
		int number;
		//System.out.printf("sNumber:%s\n", sNumber);
		if(Tool.isNumber(sNumber))
		{
			number = Integer.parseInt(sNumber);
		}
		else
		{
			String s1 = getSpecifyData(row, 14, 6);
			if(s1.compareTo("空目錄") == 0)
				System.out.printf("我的最愛為空目錄\n");
			number = -1;
		}
		return number;
		} catch(java.lang.NumberFormatException e)
		{

			return -1;
		}
	}

	public int getFirstItemNumber()
	{
		int number = -1;
		String sNumber = getSpecifyData(FIRST_LIST_ROW, 2, 5).trim();
		if(Tool.isNumber(sNumber))
		{
			number = Integer.parseInt(sNumber);
		}
		return number;
	}

	public int getLastItemNumber() //get lowerst item number
	{
		int number = -1;
		try{
		for(int i=LAST_LIST_ROW; i>=FIRST_LIST_ROW; i--)
		{
			String sNumber = getSpecifyData(i, 2, 5).trim();
			if(Tool.isNumber(sNumber))
			{
				number = Integer.parseInt(sNumber);
				break;
			}
			else ;
			
		}
		} catch(java.lang.NumberFormatException e)
		{
			number = -1;
		}
		return number;
	}

	public int getItemsNumber()
	{
		int number = -1;
		try{
		for(int i=LAST_LIST_ROW; i>=FIRST_LIST_ROW; i--)
		{
			String sNumber = getSpecifyData(i, 2, 5).trim();
			if(Tool.isNumber(sNumber))
			{
				number = i - FIRST_LIST_ROW + 1;
				break;
			}
			else if(sNumber.matches("[^ ]"))
			{
				if(sNumber.compareTo("★") == 0)
				{
					number = i - FIRST_LIST_ROW + 1;
					break;
				}
				else System.out.printf("%s not blank\n", sNumber);
			}
		}
		} catch(java.lang.NumberFormatException e)
		{
			number = -1;
		}
		return number;
	}

	public int getLineNumberShown(int first)
	{
		int ret;
		String bottomString = getSpecifyData(LAST_ROW, 0, COL);
		int offset = bottomString.lastIndexOf("第 ") + 2;
		int offset2 = bottomString.lastIndexOf(" 行");
		//System.out.printf("offset:%d offset2:%d\n", offset, offset2);
		String r = bottomString.substring(offset, offset2);
		//System.out.printf("%s\n", r);
		String afterSplit[] = r.split("~");
		if(first == 1)
			ret = Integer.parseInt(afterSplit[0]);
		else
			ret = Integer.parseInt(afterSplit[1]);
		return ret;
	}

	public int getFirstLineNumberShown()
	{
		return getLineNumberShown(1);
	}

	public int getLastLineNumberShown()
	{
		return getLineNumberShown(0);
	}

	public int getArticleReadPersent()
	{
		String bottomString = getSpecifyData(LAST_ROW, 0, COL);
		int offset = bottomString.indexOf("(") + 1;
		int offset2 = bottomString.indexOf("%)");
		String r = bottomString.substring(offset, offset2).trim();
		return Integer.parseInt(r);
	}

	public boolean isShowCrOptionEnable()
	{
		boolean isEnable = false;
		if(isInArticle())
		{
			try{
			String query;
			query = String.format("%so", Constant.ANSI_ETX);
			send(query);
			while(isParsingData() || !isInArticleOptionSetting())
			{
				Thread.sleep(10);
			}

			String s1 = getSpecifyData(19, 34, 1);
			if(s1.compareTo("*") == 0)
			{
				isEnable = true;
			}

			query = String.format("%s", Constant.ANSI_ETX);
			send(query);
			while(isParsingData() || !isInArticle())
			{
				Thread.sleep(10);
			}


			return isEnable;

			} catch(java.lang.InterruptedException e)
			{}
		}
		else
		{
			System.out.printf("not in article, not expected case\n");
			return false;
		}
		return isEnable;
	}

	public void setShowCrOption(boolean enable)
	{
		boolean change = false;
		if(isInArticle())
		{
			try{
			String query;
			query = String.format("%so", Constant.ANSI_ETX);
			send(query);
			while(isParsingData() || !isInArticleOptionSetting())
			{
				Thread.sleep(10);
			}

			String s1 = getSpecifyData(19, 34, 1);
			if(s1.compareTo("*") == 0)
			{
				if(!enable)
					change = true;
			}
			else
			{
				if(enable)
					change = true;
			}

			if(change)
				query = String.format("m%s", Constant.ANSI_ETX);
			else
				query = String.format("%s", Constant.ANSI_ETX);

			send(query);
			while(isParsingData() || !isInArticle())
			{
				Thread.sleep(10);
			}

			} catch(java.lang.InterruptedException e)
			{}
		}
	}

	public boolean isParsingData()
	{
		return parsingData;
	}

	public int ClearControlCode(byte[] oriBuf, int length)
	{
		int ptr = 0;
		//byte[] f = Arrays.copyOf(oriBuf, length*3);
		byte[] f = new byte[length*3];
		System.out.printf("len:%d\n", length);

		int state = 0;
		String tag = "";
		int line = 0;

		if(lastTag.length() > 0)
		{
			tag = lastTag;
			lastTag = "";
			state = 1;
			//System.out.printf("lastTag:%s\n", lastTag);
		}

		parsingData = true;

		//System.out.printf("-------------------------------------------------\n");

		for(int i=0; i<length; i++)
		{
			byte ch = oriBuf[i];
			switch(state)
			{
				case 0:
					//System.out.printf("(%02X)", ch);
					/* escape */
					if(ch == (byte)0x1b)
					{
						//System.out.printf("case0(%c)", ch);
						//System.out.printf("e");
						tag = "#";
						state = 1;
						break;
					}


					/* CR */
					//if(ch == '\n')
					if(ch == (byte)0x0a)
					{
						//System.out.printf("[%02X %c]", ch, ch);
						line++;
						f[ptr++] = '[';

						String sLine = String.valueOf(line);
						//System.out.printf("line:%d ", line);
						for(int k=0; k<sLine.length(); k++)
						{
							//System.out.printf("(%d)%c", sLine.length(), sLine.charAt(k));
							f[ptr++] = (byte)sLine.charAt(k);
						}
						if(cursorRow < ROW-1)
							cursorRow++;
						else
						{
							//System.out.printf("strange CR while (%d,%d)\n", cursorRow, cursorColumn);
							// I found it seems be move screen up one line
							for(int k=0; k<ROW-1; k++)
							{
								for(int m=0; m<COL-1; m++)
								{
									if(k != ROW-1)
										data[k][m] = data[k+1][m];
									else
										data[k][m] = ' ';
								}
							}
						}
						//System.out.printf("CR goto (%d,%d)\n", cursorRow, cursorColumn);
						f[ptr++] = ']';
						break;
					}
					//else if(ch == '\r')
					else if(ch == (byte)0x0d)
					{
						cursorColumn = 0;
						//System.out.printf("\\r (%d,%d)\n", cursorRow, cursorColumn);
						break;
					}
					else if(ch == '\b')
					{
						cursorColumn--;
						break;
					}

					f[ptr++] = ch;
					writeData(ch);
					break;

				case 1:
					/* celloct control code */

					//System.out.printf("(%02X)", ch);

					tag += (char)ch;

					if (ch == 'm')
					{
						state = 0;
						//System.out.printf("%s", tag);
						//System.out.printf("\u001b[1;31m");
						//break;
						tag = "";
					}
					else if (ch == 'H')
					{
						state = 0;
						//System.out.printf("(%s)", tag);
						//f[ptr++] = '\n';
						if(tag.length() > 3)
						{
							String[] afterSplit = (String[])tag.substring(2).split("[;|H]");
							//System.out.printf("(%s)", afterSplit[0]);
							f[ptr++] = 'L';
							for(int k=0; k<afterSplit[0].length(); k++)
							{
								f[ptr++] = (byte)afterSplit[0].charAt(k);
							}
							if(line != Integer.parseInt(afterSplit[0])){
								f[ptr++] = '\n';
								line = Integer.parseInt(afterSplit[0]);
							}
							cursorColumn = Integer.parseInt(afterSplit[1])-1;
							cursorRow = line-1;
							//System.out.printf("goto (%d,%d)\n", cursorRow, cursorColumn);
						}
						else if(tag.compareTo("#[H") == 0)
						{
							// home
							cursorRow = 0;
							cursorColumn = 0;
						}

						//break;
						tag = "";
					}
					else if ( ch == 'K' || ch == 'J' )
					{
						//System.out.printf("%s", tag);

						if(tag.compareTo("#[2J") == 0)
						{
							//System.out.printf("clear all\n");
							clearData();
						}
						else if(tag.compareTo("#[K") == 0)
						{
							//System.out.printf("clear right (%d,%d)\n", cursorRow, cursorColumn);
							clearLineFromCursorRight();
						}
                                                state = 0;
						//break;
						tag = "";
					}
					else if ( ch == 'M')
					{
						state = 0;
						tag = "";
					}
					break;
			}
		}
		if(false)
		{
			try{
			String data2;
			data2 = new String(f, 0, ptr, "big5");
			System.out.printf("%s", data2);
			} catch (java.io.IOException e)
			{}
		}

		if(tag.length() > 0)
		{
			lastTag = tag;
			//System.out.printf("remaing tag:%s\n", tag);
		}

		if(isInBoardList())
			System.out.printf("InBoardList\n");
		else if(isInPostList())
			System.out.printf("InPostList\n");
		else if(isInSubList())
			System.out.printf("InSubList\n");

		parsingData = false;
		return ptr;
	}

	public void detectBig5(byte[] b, int length)
	{
		String big5Str = "";
		for(int i=0; i<length; i++)
		{

			if ( (b[i] >= (byte)0x80 && b[i] <= (byte)0xFE) && 
				( (b[i+1] >= (byte)0x40 && b[i+1] <= (byte)0x7E) || (b[i+1] >= (byte)0xA1 && b[i+1] <= (byte)0xFE) ) )
			{
				byte[] c = new byte[2];
				c[0] = b[i];
				c[1] = b[i+1];

				try{
				//big5Str.concat(new String(c, "big5"));
				//big5Str = big5Str.concat(new String(c, "big5"));
				
				big5Str = new String(c, "big5");
				i++;
				//System.out.printf("(%s %02X %02X)", big5Str, c[0], c[1]);
				} catch(java.io.UnsupportedEncodingException e)
				{}
			}
		}
		//System.out.printf("%s", big5Str);

	}
	
	public void closeSocket()
	{
		try {
		client.close();
		} catch(java.io.IOException e)
		{}
	}

	public void run()
	{
		try {
		//client = new Socket();
		//InetAddress addr = InetAddress.getByName("ptt.cc");
		//InetSocketAddress isa = new InetSocketAddress(addr , 23);
		//client.connect(isa, 10000);
		//BufferedInputStream in = new BufferedInputStream(client.getInputStream());
		
		address = InetAddress.getByName(hostName);
		client = new Socket(address, port);
		
		DataInputStream in = new DataInputStream(client.getInputStream());
		out = new DataOutputStream(client.getOutputStream());
		byte[] b = new byte[5120];
		String data = "";
		int length;
		int ptr;
		while (loop && (length = in.read(b)) > 0)
		{
			ptr = ClearControlCode(b, length);
			//detectBig5(b, length);

if(1 == 0)
			for(int i=0; i<length; i++)
			{
				if(b[i] == 0x1b)
					b[i] = '#';
			}
			data = new String(b, 0, length, "big5");
			//displayData();
if(1 == 0)
{
			if(length > 5000) System.out.printf("%d\n", length);
			else System.out.printf("%s", data);
}
		}

		System.out.println("end loop");
		in.close();
		client.close();

		} catch (java.io.IOException e)
		{
			System.out.println("socket problem!");
		}

		
	}

}


class Constant{
	public static final String ANSI_ARROW_LEFT = "\u001b[D";
	public static final String ANSI_ARROW_DOWN = "\u001b[B";

	public static final String ANSI_PAGEDOWN = "\u001b[6~";

	public static final String ANSI_HOME = "\u001b[1~";
	public static final String ANSI_END = "\u001b[4~";

	public static final String ANSI_ETX = "\u0003"; //ctrl+c
	public static final String ANSI_SUB = "\u001a"; //ctrl+z
}

class Tool{
	public static boolean isNumber(String s)
	{
		return s.matches("\\d+\\.?\\d*");
	}

}

class PostList{
	List<PostInfo> list;
	String boardName;
	public PostList()
	{
		list = new ArrayList<PostInfo>();
		boardName = "";
	}
}

class PostInfo{
	public int number;
	public String date;
	public String author;
	public String title;

	public String getSpecifyBytes(byte[] data, int offset, int length)
	{
		byte[] bytes = new byte[length];
		String str = "";
		for(int i=0; i<length; i++)
			bytes[i] = data[offset+i];
		try{
		str = new String(bytes, 0, length, "big5");
                } catch(java.io.UnsupportedEncodingException e)
		{}
		return str.trim();
	}

	public int fill(byte[] data)
	{
		if(Tool.isNumber(getSpecifyBytes(data, 2, 5)))
			number = Integer.parseInt(getSpecifyBytes(data, 2, 5));
		else if(getSpecifyBytes(data, 2, 5).compareTo("★") == 0)
			number = 99999;

		date = getSpecifyBytes(data, 11, 5);
		author = getSpecifyBytes(data, 17, 13);
		title = getSpecifyBytes(data, 30, 50);


		return 0;
	}

			
}

class MyFavorite{
	List<BoardInfo> favoriteList;
	public MyFavorite()
	{
		favoriteList = new ArrayList<BoardInfo>();
	}
}

class BoardInfo{
	public String board;
	public int number;
	public String category;
	public String forward;
	public String description;
	public String popularity;

	public String getSpecifyBytes(byte[] data, int offset, int length)
	{
		byte[] bytes = new byte[length];
		String str = "";
		for(int i=0; i<length; i++)
			bytes[i] = data[offset+i];
		try{
		str = new String(bytes, 0, length, "big5");
		} catch(java.io.UnsupportedEncodingException e)
		{}
		return str.trim();
	}

	public int fill(byte[] data)
	{
		if(Tool.isNumber(getSpecifyBytes(data, 2, 5)))
			number = Integer.parseInt(getSpecifyBytes(data, 2, 5));
		else
			number = 0;
		board = getSpecifyBytes(data, 10, 13);
		category = getSpecifyBytes(data, 23, 4);
		forward = getSpecifyBytes(data, 28, 2);
		description = getSpecifyBytes(data, 30, 34);
		popularity = getSpecifyBytes(data, 64, 3);
		
		return 0;
	}
}