/******************************************************************
 *
 *	CyberHTTP for Java
 *
 *	Copyright (C) Satoshi Konno 2002-2004
 *
 *	File: HTTPSocket.java
 *
 *	Revision;
 *
 *	12/12/02
 *		- first revision.
 *	03/11/04
 *		- Added the following methods about chunk size.
 *		  setChunkSize(), getChunkSize().
 *	08/26/04
 *		- Added a isOnlyHeader to post().
 *	03/02/05
 *		- Changed post() to suppot chunked stream.
 *	06/10/05
 *		- Changed post() to add a Date headedr to the HTTPResponse before the posting.
 *	07/07/05
 *		- Lee Peik Feng <pflee@users.sourceforge.net>
 *		- Fixed post() to output the chunk size as a hex string.
 *	
 ******************************************************************/

package com.xyoye.dandanplay.utils.smb.cybergarage.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Calendar;


/** HTTPSocket 负责将内容写出 */
public class HTTPSocket
{

	private static final String TAG = "org.cybergarage.http.HTTPSocket";

	// //////////////////////////////////////////////
	// Constructor
	// //////////////////////////////////////////////

	/**
	 * 创建一个 HTTPSocket 对象 赋值Socket ， InputStream，OutputStream
	 * 
	 * @param socket
	 *            赋值给本类的Socket对象
	 **/
	public HTTPSocket(Socket socket)
	{
		setSocket(socket);
		open();
	}

	/**
	 * 创建一个 HTTPSocket 对象赋值Socket ， InputStream，OutputStream
	 * 
	 * @param socket
	 *            获自己的Socket ， InputStream，OutputStream赋值给HTTPSocket
	 * 
	 */
	public HTTPSocket(HTTPSocket socket)
	{
		setSocket(socket.getSocket());
		setInputStream(socket.getInputStream());
		setOutputStream(socket.getOutputStream());
	}

	@Override
	public void finalize()
	{
		close();
	}

	// //////////////////////////////////////////////
	// Socket
	// //////////////////////////////////////////////

	/** Socket socket */
	private Socket socket = null;

	/** 设置 socket */
	private void setSocket(Socket socket)
	{
		this.socket = socket;
	}

	/** 获取 socket */
	public Socket getSocket()
	{
		return socket;
	}

	// //////////////////////////////////////////////
	// local address/port
	// //////////////////////////////////////////////

	/** 获取本地地址 */
	public String getLocalAddress()
	{
		return getSocket().getLocalAddress().getHostAddress();
	}

	/** 获取本地端口 */
	public int getLocalPort()
	{
		return getSocket().getLocalPort();
	}

	// //////////////////////////////////////////////
	// in/out
	// //////////////////////////////////////////////

	/** InputStream sockIn 输入流 */
	private InputStream sockIn = null;
	/** OutputStream sockOut 输出流 */
	private OutputStream sockOut = null;

	/** 设置 sockIn */
	private void setInputStream(InputStream in)
	{
		sockIn = in;
	}

	/** 获取sockIn */
	public InputStream getInputStream()
	{
		return sockIn;
	}

	/** 设置 sockOut */
	private void setOutputStream(OutputStream out)
	{
		sockOut = out;
	}

	/** 获取sockOut */
	private OutputStream getOutputStream()
	{
		return sockOut;
	}

	// //////////////////////////////////////////////
	// open/close
	// //////////////////////////////////////////////

	/** 获取Socket中的InputStream 和OutputStream ,出现异常返回false，没有异常返回true */
	public boolean open()
	{
		// 获取socket对象
		Socket sock = getSocket();
		try
		{
			// 获取InputStream
			sockIn = sock.getInputStream();
			// 获取OutputStream
			sockOut = sock.getOutputStream();
		}
		catch (Exception e)
		{
			// TODO Add blacklistening of the UPnP Device
			return false;
		}
		return true;
	}

	/** 关闭sockIn,sockOut,socket的方法，没有异常返回true，有异常返回false */
	public boolean close()
	{
		try
		{
			if (sockIn != null)
			{
				sockIn.close();
			}
			if (sockOut != null)
			{
				sockOut.close();
			}
			getSocket().close();
		}
		catch (Exception e)
		{
			// Debug.warning(e);
			return false;
		}
		return true;
	}

	// //////////////////////////////////////////////
	// post
	// //////////////////////////////////////////////

	/**
	 * 将内容写出
	 * 
	 * @param httpRes
	 * @param content
	 *            写出的内容
	 * @param contentOffset
	 *            内容数组的偏移量
	 * @param contentLength
	 *            内容的长度
	 * @param isOnlyHeader
	 *            请求方式是HEAD 为true，否则false
	 * 
	 **/
	private boolean post(HTTPResponse httpRes, byte content[],
                         long contentOffset, long contentLength, boolean isOnlyHeader)
	{
		// TODO Check for bad HTTP agents, this method may be list for
		// IOInteruptedException and for blacklistening
		// 检查不好的HTTP代理，这种方法可能是列表IOInteruptedException和blacklistening
		httpRes.setDate(Calendar.getInstance());

		// 获取OutputStream
		OutputStream out = getOutputStream();

		try
		{
			// 设置内容长度
			httpRes.setContentLength(contentLength);

			// 写出状态行和多个消息头
			out.write(httpRes.getHeader().getBytes());
			// 写出CRLF
			out.write(HTTP.CRLF.getBytes());

			// 如果请求是HEAD 执行
			if (isOnlyHeader == true)
			{
				out.flush();
				return true;
			}

			// 判断是否有Transfer-Encoding: chunked
			boolean isChunkedResponse = httpRes.isChunked();

			if (isChunkedResponse == true)
			{
				// Thanks for Lee Peik Feng <pflee@users.sourceforge.net>
				// (07/07/05)
				// 将内容长度转换成16进制
				String chunSizeBuf = Long.toHexString(contentLength);
				// 写出内容长度
				out.write(chunSizeBuf.getBytes());
				// 写出CRLF
				out.write(HTTP.CRLF.getBytes());
			}

			// 写出内容
			out.write(content, (int) contentOffset, (int) contentLength);

			if (isChunkedResponse == true)
			{
				// 写出CRLF
				out.write(HTTP.CRLF.getBytes());
				// 写出结束标记
				out.write("0".getBytes());
				// 写出CRLF
				out.write(HTTP.CRLF.getBytes());
			}
			// 刷新缓冲区
			out.flush();
		}
		catch (Exception e)
		{
			// Debug.warning(e);
			return false;
		}

		return true;
	}

	/**
	 * 将内容边读边写出
	 * 
	 * @param httpRes
	 * @param in
	 *            输入流
	 * @param contentOffset
	 *            内容的起始位置
	 * @param contentLength
	 *            内容的长度
	 * @param isOnlyHeader
	 *            请求方式是HEAD 为true，否则false
	 * @return
	 */
	private boolean post(HTTPResponse httpRes, InputStream in,
                         long contentOffset, long contentLength, boolean isOnlyHeader)
	{
		// TODO Check for bad HTTP agents, this method may be list for
		// IOInteruptedException and for blacklistening
		// 检查不好的HTTP代理，这种方法可能是列表IOInteruptedException和blacklistening
		try
		{
			httpRes.setDate(Calendar.getInstance());

			// 获取outputStream
			OutputStream out = getOutputStream();

			// 设置内容长度
			httpRes.setContentLength(contentLength);

			// 写出响应的状态行和多个消息头

			out.write(httpRes.getHeader().getBytes());

			// 写出CRLF
			out.write(HTTP.CRLF.getBytes());

			// 如果请求是HEAD 执行
			if (isOnlyHeader == true)
			{
				out.flush();
				return true;
			}

			// 判断是否有Transfer-Encoding: chunked
			boolean isChunkedResponse = httpRes.isChunked();

			if (0 < contentOffset)
			{
				// 设置流的起始位置
				in.skip(contentOffset);
			}

			// 获取块大小
			int chunkSize = HTTP.getChunkSize();
			// 创建一个读取的缓冲数组
			byte readBuf[] = new byte[chunkSize];
			long readCnt = 0;
			long readSize = (chunkSize < contentLength) ? chunkSize
					: contentLength;
			int readLen = in.read(readBuf, 0, (int) readSize);

			while (0 < readLen && readCnt < contentLength)
			{
				if (isChunkedResponse == true)
				{
					// Thanks for Lee Peik Feng <pflee@users.sourceforge.net>
					// (07/07/05)
					// 转换成16进制
					String chunSizeBuf = Long.toHexString(readLen);
					// 发送长度
					out.write(chunSizeBuf.getBytes());
					// 发送CRLF

					out.write(HTTP.CRLF.getBytes());
				}
				// 写出已读的数据
				out.write(readBuf, 0, readLen);

				if (isChunkedResponse == true)
				{
					// 发送CRLF
					out.write(HTTP.CRLF.getBytes());
				}
				readCnt += readLen;
				readSize = (chunkSize < (contentLength - readCnt)) ? chunkSize
						: (contentLength - readCnt);
				readLen = in.read(readBuf, 0, (int) readSize);
			}

			// 结束标记
			if (isChunkedResponse == true)
			{
				// 发送0
				out.write("0".getBytes());
				// 发送CRLF
				out.write(HTTP.CRLF.getBytes());
			}

			out.flush();


		}
		catch (IOException e)
		{ 
		}
		
		return true;
	}

	/**
	 * 发送响应的消息
	 * 
	 * @param httpRes
	 *            HTTPResponse对象
	 * @param contentOffset
	 *            内容的起始位置
	 * @param contentLength
	 *            内容的长度
	 * @param isOnlyHeader
	 * @return
	 */
	public boolean post(HTTPResponse httpRes, long contentOffset,
                        long contentLength, boolean isOnlyHeader)
	{
		// TODO Close if Connection != keep-alive
		if (httpRes.hasContentInputStream() == true)
		{
			return post(httpRes, httpRes.getContentInputStream(),
					contentOffset, contentLength, isOnlyHeader);
		}
		return post(httpRes, httpRes.getContent(), contentOffset,
				contentLength, isOnlyHeader);
	}
}
