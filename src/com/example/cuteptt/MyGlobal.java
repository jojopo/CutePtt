package com.example.cuteptt;

import java.net.Socket;

import android.app.Application;

public class MyGlobal extends Application {
	private SocketClient socketclient;
	private String account;
	private String password;
	private boolean isExit = false;
	private int lastArticleNumber = 0;
	
	public SocketClient getSocketClient()
	{
		return socketclient;
	}
	
	public void setSocketClient(SocketClient sockclt)
	{
		socketclient = sockclt;
	}
	
	public String getAccount()
	{
		return account;
	}
	public void setAccount(String s)
	{
		account = s;
	}
	
	public int getLastArticleNumber()
	{
		return lastArticleNumber;
	}
	public void setLastArticleNumber(int n)
	{
		lastArticleNumber = n;
	}
	
	public String getPassword()
	{
		return password;
	}
	public void setPassword(String s)
	{
		password = s;
	}
	
	
	
	public boolean isExit()
	{
		return isExit;
	}
	
	public void exit()
	{
		isExit = true;
	}
}
