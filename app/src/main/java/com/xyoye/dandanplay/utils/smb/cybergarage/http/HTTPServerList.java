/******************************************************************
 *
 *	CyberUPnP for Java
 *
 *	Copyright (C) Satoshi Konno 2002-2003
 *
 *	File: HTTPServerList.java
 *
 *	Revision;
 *
 *	05/08/03
 *		- first revision.
 *	24/03/06
 *		- Stefano Lenzi:added debug information as request by Stephen More
 *
 ******************************************************************/

package com.xyoye.dandanplay.utils.smb.cybergarage.http;

import com.xyoye.dandanplay.utils.smb.cybergarage.net.HostInterface;

import java.net.InetAddress;
import java.util.Vector;

/** HTTPServerList 继承 Vector 保存的是HTTPServer 类型 */
public class HTTPServerList extends Vector
{
	
	
	// //////////////////////////////////////////////
	// Constructor
	// //////////////////////////////////////////////

	/** 地址 */
	private InetAddress[] binds = null;
	/** 端口 */
	private int port = 4004;

	/** 创建一个默认的HTTPServerList */
	public HTTPServerList()
	{
	}

	/**
	 * 创建一个 HTTPServerList
	 * 
	 * @param list
	 *            地址
	 * @param port
	 *            端口
	 * 
	 * */
	public HTTPServerList(InetAddress[] list, int port)
	{
		this.binds = list;
		this.port = port;
	}

	// //////////////////////////////////////////////
	// Methods
	// //////////////////////////////////////////////

	/** 给集合中的每个HTTPServer对象添加HTTPRequestListener对象 */
	public void addRequestListener(HTTPRequestListener listener)
	{
		int nServers = size();
		for (int n = 0; n < nServers; n++)
		{
			HTTPServer server = getHTTPServer(n);
			server.addRequestListener(listener);
		}
	}

	/** 根据索引获取HTTPServer 对象 */
	public HTTPServer getHTTPServer(int n)
	{
		return (HTTPServer) get(n);
	}

	// //////////////////////////////////////////////
	// open/close
	// //////////////////////////////////////////////

	/** 调用集合中所有HTTPServer的close方法,关闭HTTPServer的ServerSocket */
	public void close()
	{
		int nServers = size();
		for (int n = 0; n < nServers; n++)
		{
			HTTPServer server = getHTTPServer(n);
			server.close();
		}
	}

	/** 创建HTTPServer对象添加到集合中，返回添加的数量 */
	public int open()
	{
		InetAddress[] binds = this.binds;
		String[] bindAddresses;
		
		if (binds != null)
		{
			bindAddresses = new String[binds.length];
			for (int i = 0; i < binds.length; i++)
			{
				bindAddresses[i] = binds[i].getHostAddress(); 
			}
		}
		else
		{
			int nHostAddrs = HostInterface.getNHostAddresses();

			bindAddresses = new String[nHostAddrs];
			for (int n = 0; n < nHostAddrs; n++)
			{
				bindAddresses[n] = HostInterface.getHostAddress(n);
			}
		}
		// System.out.println("=======================================");
		// System.out.println("bindAddresses的长度="+bindAddresses.length);
		//
		// for(int i=0;i<bindAddresses.length;i++){
		// System.out.println("bindAddresses["+i+"]="+bindAddresses[i]);
		// }

		int j = 0;
		for (int i = 0; i < bindAddresses.length; i++)
		{
			// 创建一个HTTPServer
			HTTPServer httpServer = new HTTPServer();
			if ((bindAddresses[i] == null)
					|| (httpServer.open(bindAddresses[i], port) == false))
			{
				close();
				clear();
			}
			else
			{
				add(httpServer);
				j++;
			}
		}
		return j;
	}

	/** 赋值给本类的端口 */
	public boolean open(int port)
	{
		this.port = port;
		return open() != 0;
	}

	// //////////////////////////////////////////////
	// start/stop
	// //////////////////////////////////////////////

	/** 调用集合中所有HTTPServer的start方法 */
	public void start()
	{
		int nServers = size();
		for (int n = 0; n < nServers; n++)
		{
			HTTPServer server = getHTTPServer(n);
			server.start();
		}
	}

	/** 调用集合中所有HTTPServer的stop方法 设置线程为null 停止循环 */
	public void stop()
	{
		int nServers = size();
		for (int n = 0; n < nServers; n++)
		{
			HTTPServer server = getHTTPServer(n);
			server.stop();
		}
	}

}
