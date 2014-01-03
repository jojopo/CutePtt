package com.example.cuteptt;

import java.net.Socket;

import android.app.Application;

public class MyGlobal extends Application {
	private SocketClient socketclient;
	private String account;
	private boolean isExit = false;
	
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
	
	public boolean isExit()
	{
		return isExit;
	}
	
	public void exit()
	{
		isExit = true;
	}
}
