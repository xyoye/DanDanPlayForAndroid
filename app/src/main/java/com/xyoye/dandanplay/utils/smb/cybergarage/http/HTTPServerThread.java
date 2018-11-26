/******************************************************************
 *
 *	CyberHTTP for Java
 *
 *	Copyright (C) Satoshi Konno 2002-2003
 *
 *	File: HTTPServerThread.java
 *
 *	Revision;
 *
 *	10/10/03
 *		- first revision.
 *	
 ******************************************************************/

package com.xyoye.dandanplay.utils.smb.cybergarage.http;

import java.net.Socket;


/** HTTPServerThread 继承 Thread */
public class HTTPServerThread extends Thread
{
	private static final String tag = "HTTPServerThread";
	private HTTPServer httpServer;
	private Socket sock;

	// //////////////////////////////////////////////
	// Constructor
	// //////////////////////////////////////////////

	/**
	 * 创建一个HTTPServerThread 对象
	 * 
	 * @param httpServer
	 *            赋值给本类的HTTPServer对象
	 * @param sock
	 *            赋值给本类的Socket对象
	 **/
	public HTTPServerThread(HTTPServer httpServer, Socket sock)
	{
		super("Cyber.HTTPServerThread");
		this.httpServer = httpServer;
		this.sock = sock;
	}

	// //////////////////////////////////////////////
	// run
	// //////////////////////////////////////////////

	@Override
	public void run()
	{
		// 创建一个HTTPSocket对象
		HTTPSocket httpSock = new HTTPSocket(sock);
		if (httpSock.open() == false)
		{
			return;
		}
		
		
		// 创建HTTPRequest对象
		HTTPRequest httpReq = new HTTPRequest();
		httpReq.setSocket(httpSock);
 
		// 读取
		while (httpReq.read() == true)
		{
			 
			httpServer.performRequestListener(httpReq);
			 
		 
			if (httpReq.isKeepAlive() == false)
			{
				break;
			}
		}
		httpSock.close();
	}
}
