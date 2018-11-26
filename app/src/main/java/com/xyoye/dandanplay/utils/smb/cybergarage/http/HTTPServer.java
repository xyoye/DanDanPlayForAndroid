/******************************************************************
 *
 *	CyberHTTP for Java
 *
 *	Copyright (C) Satoshi Konno 2002-2003
 *
 *	File: HTTPServer.java
 *
 *	Revision;
 *
 *	12/12/02
 *		- first revision.
 *	10/20/03
 *		- Improved the HTTP server using multithreading.
 *	08/27/04
 *		- Changed accept() to set a default timeout, HTTP.DEFAULT_TIMEOUT, to the socket.
 *	
 ******************************************************************/

package com.xyoye.dandanplay.utils.smb.cybergarage.http;

import android.util.Log;

import com.xyoye.dandanplay.utils.smb.cybergarage.util.Debug;
import com.xyoye.dandanplay.utils.smb.cybergarage.util.ListenerList;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * HTTPServer 实现 Runnable,是一个HTTP服务器 This class identifies an HTTP over TCP
 * server<br>
 * The server must be initialized iether by the
 * {@link HTTPServer#open(InetAddress, int)} or the
 * {@link HTTPServer#open(String, int)} method.<br>
 * Optionally a set of {@link HTTPRequestListener} may be set<br>
 * The server then can be started or stopped by the method
 * {@link HTTPServer#start()} and {@link HTTPServer#stop()}
 * 
 * @author Satoshi "skonno" Konno
 * @author Stefano "Kismet" Lenzi
 * @version 1.8
 * 
 */
public class HTTPServer implements Runnable
{
	private final static String tag = "HTTPServer";
	// //////////////////////////////////////////////
	// Constants
	// //////////////////////////////////////////////

	public final static String NAME = "CyberHTTP";
	public final static String VERSION = "1.0";

	/** 默认的端口为80 */
	public final static int DEFAULT_PORT = 80;

	/**
	 * 默认连接的超时时间 Default timeout connection for HTTP comunication
	 * 默认超时设置80*1000
	 * 
	 * @since 1.8
	 */
	public final static int DEFAULT_TIMEOUT = 15 * 1000;

	/** 获取HTTPServer的名字 */
	public static String getName()
	{
		// 获取系统的名字
		String osName = System.getProperty("os.name");
		// 获取系统的版本
		String osVer = System.getProperty("os.version");
		return osName + "/" + osVer + " " + NAME + "/" + VERSION;
	}

	// //////////////////////////////////////////////
	// Constructor
	// //////////////////////////////////////////////

	public HTTPServer()
	{
		serverSock = null;

	}

	// //////////////////////////////////////////////
	// ServerSocket
	// //////////////////////////////////////////////

	/** ServerSocket serverSock */
	private ServerSocket serverSock = null;
	/** InetAddress bindAddr 地址 */
	private InetAddress bindAddr = null;
	/** 端口 */
	private int bindPort = 0;
	/**
	 * timeout tcp的超时时间 默认值为80×1000 Store the current TCP timeout value The
	 * variable should be accessed by getter and setter metho
	 */
	protected int timeout = DEFAULT_TIMEOUT;

	/** 获取serverSock */
	public ServerSocket getServerSock()
	{
		return serverSock;
	}

	/** 返回ip地址的字符转如果bindAddr为null则返回空字符串 */
	public String getBindAddress()
	{
		if (bindAddr == null)
		{
			return "";
		}
		return bindAddr.getHostAddress();
	}

	/** 获取端口 */
	public int getBindPort()
	{
		return bindPort;
	}

	// //////////////////////////////////////////////
	// open/close
	// //////////////////////////////////////////////

	/**
	 * 获取超时时间，该方法被同步锁锁住 Get the current socket timeout
	 * 
	 * @since 1.8
	 */
	public synchronized int getTimeout()
	{
		return timeout;
	}

	/**
	 * 设置超时时间，该方法被同步锁锁住 Set the current socket timeout
	 * 
	 * @param longout
	 *            new timeout
	 * @since 1.8
	 */
	public synchronized void setTimeout(int timeout)
	{
		this.timeout = timeout;
	}

	/** serverSock不等于null返回true ，否则创建ServerSocket 对象，没有异常返回true，发生异常返回false */
	public boolean open(InetAddress addr, int port)
	{
		if (serverSock != null)
			return true;
		try
		{
			serverSock = new ServerSocket(bindPort, 0, bindAddr);


		}
		catch (IOException e)
		{
			return false;
		}
		return true;
	}

	/** serverSock不等于null返回true ，否则创建ServerSocket 对象，没有异常返回true，发生异常返回false */
	public boolean open(String addr, int port)
	{
		if (serverSock != null)
		{
			return true;
		}
		try
		{
			bindAddr = InetAddress.getByName(addr);
			bindPort = port;
			serverSock = new ServerSocket(bindPort, 0, bindAddr);
	 


		}
		catch (IOException e)
		{
			return false;
		}
		return true;
	}

	/** 关闭socket的方法如果没有异常返回true，有异常就返回false */
	public boolean close()
	{
		if (serverSock == null)
		{
			return true;
		}
		try
		{
			serverSock.close();
			serverSock = null;
			bindAddr = null;
			bindPort = 0;
			
			Log.e(tag, "关闭http服务器： "+serverSock.getInetAddress().getHostAddress());
		}
		catch (Exception e)
		{
			Debug.warning(e);
			return false;
		}
		return true;
	}

	/** 开始监听并设置超时时间，如果没发生异常返回Socket对象，否则返回null */
	public Socket accept()
	{
		if (serverSock == null)
			return null;
		try
		{
			// 开始监听
			Socket sock = serverSock.accept();
			// 设置超时时间
			sock.setSoTimeout(getTimeout());
			return sock;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/** 判断是否创建 ServerSocket 对象 */
	public boolean isOpened()
	{
		return (serverSock != null) ? true : false;
	}

	// //////////////////////////////////////////////
	// httpRequest
	// //////////////////////////////////////////////

	/** ListenerList httpRequestListenerList 保存 HTTPRequestListener 对象 */
	private ListenerList httpRequestListenerList = new ListenerList();

	/** ListenerList 中添加 HTTPRequestListener 对象 */
	public void addRequestListener(HTTPRequestListener listener)
	{
		httpRequestListenerList.add(listener);
	}

	/** ListenerList 中删除 HTTPRequestListener 对象 */
	public void removeRequestListener(HTTPRequestListener listener)
	{
		httpRequestListenerList.remove(listener);
	}

	/** 为集合中的每个HTTPRequestListener 对象执行httpRequestRecieved方法 */
	public void performRequestListener(HTTPRequest httpReq)
	{
		int listenerSize = httpRequestListenerList.size();
		for (int n = 0; n < listenerSize; n++)
		{
			HTTPRequestListener listener = (HTTPRequestListener) httpRequestListenerList
					.get(n);
		 
			
			listener.httpRequestRecieved(httpReq);
		}
	}

	// //////////////////////////////////////////////
	// run
	// //////////////////////////////////////////////

	private Thread httpServerThread = null;

	@Override
	public void run()
	{
		// 如果没有创建 ServerSocket 对象return
		if (isOpened() == false)
		{
			return;
		}

		// 获取当前线程
		Thread thisThread = Thread.currentThread();

		while (httpServerThread == thisThread)
		{
			// 线程让步，回到准备运行状态
			Thread.yield();
			Socket sock;
			try
			{
				Debug.message("accept ...");
				
				sock = accept();
				if (sock != null)
				{
					Debug.message("sock = " + sock.getRemoteSocketAddress());
				}
			}
			catch (Exception e)
			{ 
				break;
			}
			 
			// 创建一个HTTPServerThread 对象
			HTTPServerThread httpServThread = new HTTPServerThread(this, sock);
			httpServThread.start();
			Debug.message("httpServThread ...");
		}
	}

	public boolean start()
	{
		StringBuffer name = new StringBuffer("Cyber.HTTPServer/");
		name.append(serverSock.getLocalSocketAddress());
		httpServerThread = new Thread(this, name.toString());
		httpServerThread.start();
		return true;
	}

	/** httpServerThread 设置为null */
	public boolean stop()
	{
		httpServerThread = null;
		return true;
	}
}
