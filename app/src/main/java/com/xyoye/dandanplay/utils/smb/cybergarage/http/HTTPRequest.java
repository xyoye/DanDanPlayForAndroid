/******************************************************************
 *
 *	CyberHTTP for Java
 *
 *	Copyright (C) Satoshi Konno 2002-2004
 *
 *	File: HTTPRequest.java
 *
 *	Revision;
 *
 *	11/18/02
 *		- first revision.
 *	05/23/03
 *		- Giordano Sassaroli <sassarol@cefriel.it>
 *		- Add a relative URL check to setURI().
 *	09/02/03
 *		- Giordano Sassaroli <sassarol@cefriel.it>
 *		- Problem : Devices whose description use absolute urls receive wrong http requests
 *		- Error : the presence of a base url is not mandatory, the API code makes the assumption that control and event subscription urls are relative
 *		- Description: The method setURI should be changed as follows
 *	02/01/04
 *		- Added URI parameter methods.
 *	03/16/04
 *		- Removed setVersion() because the method is added to the super class.
 *		- Changed getVersion() to return the version when the first line string has the length.
 *	05/19/04
 *		- Changed post(HTTPResponse *) to close the socket stream from the server.
 *	08/19/04
 *		- Fixed getFirstLineString() and getHTTPVersion() no to return "HTTP/HTTP/version".
 *	08/25/04
 *		- Added isHeadRequest().
 *	08/26/04
 *		- Changed post(HTTPResponse) not to close the connection.
 *		- Changed post(String, int) to add a connection header to close.
 *	08/27/04
 *		- Changed post(String, int) to support the persistent connection.
 *	08/28/04
 *		- Added isKeepAlive().
 *	10/26/04
 *		- Brent Hills <bhills@openshores.com>
 *		- Added a fix to post() when the last position of Content-Range header is 0.
 *		- Added a Content-Range header to the response in post().
 *		- Changed the status code for the Content-Range request in post().
 *		- Added to check the range of Content-Range request in post().
 *	03/02/05
 *		- Changed post() to suppot chunked stream.
 *	06/10/05
 *		- Changed post() to add a HOST headedr before the posting.
 *	07/07/05
 *		- Lee Peik Feng <pflee@users.sourceforge.net>
 *		- Fixed post() to output the chunk size as a hex string.
 *
 ******************************************************************/

package com.xyoye.dandanplay.utils.smb.cybergarage.http;

import com.xyoye.dandanplay.utils.smb.cybergarage.util.Debug;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.StringTokenizer;


/**
 * HTTPRequest 继承 HTTPPacket 保存http请求的消息
 * 
 * This class rappresnet an HTTP <b>request</b>, and act as HTTP client when it
 * sends the request<br>
 * 
 * @author Satoshi "skonno" Konno
 * @author Stefano "Kismet" Lenzi
 * @version 1.8
 * 
 */
public class HTTPRequest extends HTTPPacket
{
    private static final String TAG = "org.cybergarage.http.HTTPRequest";
	
	// //////////////////////////////////////////////
	// Constructor
	// //////////////////////////////////////////////

	/** 创建一个HTTPRequest 设置version值为1.0 */
	public HTTPRequest()
	{
		setVersion(HTTP.VERSION_10);
	}

	public HTTPRequest(InputStream in)
	{
		super(in);
	}

	public HTTPRequest(HTTPSocket httpSock)
	{
		this(httpSock.getInputStream());
		setSocket(httpSock);
	}

	// //////////////////////////////////////////////
	// Method
	// //////////////////////////////////////////////

	/** 私有的String变量 method 是请求的方法，常见有POST、GET、HEAD、OPTIONS、DELETE、TRACE、PUT */
	private String method = null;

	/**
	 * 设置method的值. method 是请求的方法，常见有POST、GET、HEAD、OPTIONS、DELETE、TRACE、PUT
	 **/
	public void setMethod(String value)
	{
		method = value;
	}

	/**
	 * 获取Method method 是请求的方法，常见有POST、GET、HEAD、OPTIONS、DELETE、TRACE、PUT
	 **/
	public String getMethod()
	{
		if (method != null)
		{
			return method;
		}
		return getFirstLineToken(0);
	}

	/**
	 * 判断method的值是否与参数method的值相同，不区分大小写 相同返回true，否则返回false
	 */
	public boolean isMethod(String method)
	{
		String headerMethod = getMethod();
		if (headerMethod == null)
		{
			return false;
		}
		return headerMethod.equalsIgnoreCase(method);
	}

	/**
	 * 判断是不是GET的请求
	 * 
	 * @return 是返回true，否则返回false
	 */
	public boolean isGetRequest()
	{
		return isMethod(HTTP.GET);
	}

	/**
	 * 判断是不是POST的请求
	 * 
	 * @return 是返回true，否则返回false
	 */
	public boolean isPostRequest()
	{
		return isMethod(HTTP.POST);
	}

	/**
	 * 判断是不是HEAD的请求
	 * 
	 * @return 是返回true，否则返回false
	 */
	public boolean isHeadRequest()
	{
		return isMethod(HTTP.HEAD);
	}

	/**
	 * 判断是不是SUBSCRIBE的请求
	 * 
	 * @return 是返回true，否则返回false
	 */
	public boolean isSubscribeRequest()
	{
		return isMethod(HTTP.SUBSCRIBE);
	}

	/**
	 * 判断是不是UNSUBSCRIBE的请求
	 * 
	 * @return 是返回true，否则返回false
	 */
	public boolean isUnsubscribeRequest()
	{
		return isMethod(HTTP.UNSUBSCRIBE);
	}

	/**
	 * 判断是不是NOTIFY的请求
	 * 
	 * @return 是返回true，否则返回false
	 */
	public boolean isNotifyRequest()
	{
		return isMethod(HTTP.NOTIFY);
	}

	// //////////////////////////////////////////////
	// URI
	// //////////////////////////////////////////////

	/** String uri */
	private String uri = null;

	/**
	 * 设置 uri value uri格式的字符串 isCheckRelativeURL true设置 http相对的uri ，false不设置
	 */
	public void setURI(String value, boolean isCheckRelativeURL)
	{
		uri = value;
		if (isCheckRelativeURL == false)
		{
			return;
		}
		// Thanks for Giordano Sassaroli <sassarol@cefriel.it> (09/02/03)
		uri = HTTP.toRelativeURL(uri);
	}

	/** 设置uri */
	public void setURI(String value)
	{
		setURI(value, false);
	}

	/** 获取uri */
	public String getURI()
	{
		if (uri != null)
		{
			return uri;
		}
		return getFirstLineToken(1);
	}

	// //////////////////////////////////////////////
	// URI Parameter
	// //////////////////////////////////////////////

	/** 获取URI中的参数 例如 /ExportContent?id=2 */
	public ParameterList getParameterList()
	{
		// 创建ParameterList
		ParameterList paramList = new ParameterList();
		// 获取uri
		String uri = getURI();
		if (uri == null)
		{
			return paramList;
		}
		int paramIdx = uri.indexOf('?');
		if (paramIdx < 0)
		{
			return paramList;
		}
		while (0 < paramIdx)
		{
			int eqIdx = uri.indexOf('=', (paramIdx + 1));
			String name = uri.substring(paramIdx + 1, eqIdx);
			int nextParamIdx = uri.indexOf('&', (eqIdx + 1));
			String value = uri.substring(eqIdx + 1,
					(0 < nextParamIdx) ? nextParamIdx : uri.length());
			// 创建Parameter对象
			Parameter param = new Parameter(name, value);
			// 添加到集合中
			paramList.add(param);
			paramIdx = nextParamIdx;
		}
		return paramList;
	}

	public String getParameterValue(String name)
	{
		ParameterList paramList = getParameterList();
		return paramList.getValue(name);
	}

	// //////////////////////////////////////////////
	// SOAPAction
	// //////////////////////////////////////////////

	/** 判断有没有SOAPACTION头 有返回true，否则返回false */
	public boolean isSOAPAction()
	{
		return hasHeader(HTTP.SOAP_ACTION);
	}

	// //////////////////////////////////////////////
	// Host / Port
	// //////////////////////////////////////////////

	/** 请求地址 */
	private String requestHost = "";

	/** 设置请求地址 */
	public void setRequestHost(String host)
	{
		requestHost = host;
	}

	/** 获取请求地址 */
	public String getRequestHost()
	{
		return requestHost;
	}

	/** 请求端口 */
	private int requestPort = -1;

	/** 设置请求端口 */
	public void setRequestPort(int host)
	{
		requestPort = host;
	}

	/** 获取请求端口 */
	public int getRequestPort()
	{
		return requestPort;
	}

	// //////////////////////////////////////////////
	// Socket
	// //////////////////////////////////////////////

	/** HTTPSocket httpSocket */
	private HTTPSocket httpSocket = null;

	/** 设置httpSocket */
	public void setSocket(HTTPSocket value)
	{
		httpSocket = value;
	}

	/** 获取httpSocket */
	public HTTPSocket getSocket()
	{
		return httpSocket;
	}

	// ///////////////////////// /////////////////////
	// local address/port
	// //////////////////////////////////////////////

	/** 获取本机地址 */
	public String getLocalAddress()
	{
		return getSocket().getLocalAddress();
	}

	/** 获取本机端口 */
	public int getLocalPort()
	{
		return getSocket().getLocalPort();
	}

	// //////////////////////////////////////////////
	// parseRequest
	// //////////////////////////////////////////////

	public boolean parseRequestLine(String lineStr)
	{
		StringTokenizer st = new StringTokenizer(lineStr,
				HTTP.REQEST_LINE_DELIM);
		if (st.hasMoreTokens() == false)
			return false;
		setMethod(st.nextToken());
		if (st.hasMoreTokens() == false)
			return false;
		setURI(st.nextToken());
		if (st.hasMoreTokens() == false)
			return false;
		setVersion(st.nextToken());
		return true;
	}

	// //////////////////////////////////////////////
	// First Line
	// //////////////////////////////////////////////

	public String getHTTPVersion()
	{
		if (hasFirstLine() == true)
			return getFirstLineToken(2);
		return "HTTP/" + super.getVersion();
	}

	/** 获取请求行字符串 */
	public String getFirstLineString()
	{
		return getMethod() + " " + getURI() + " " + getHTTPVersion()
				+ HTTP.CRLF;
	}

	// //////////////////////////////////////////////
	// getHeader
	// //////////////////////////////////////////////

	/** 获取一个完整的http请求，包括请求行和多个消息头 */
	public String getHeader()
	{
		StringBuffer str = new StringBuffer();

		str.append(getFirstLineString());

		String headerString = getHeaderString();
		str.append(headerString);

		return str.toString();
	}

	// //////////////////////////////////////////////
	// isKeepAlive
	// //////////////////////////////////////////////

	public boolean isKeepAlive()
	{
		if (isCloseConnection() == true)
			return false;
		if (isKeepAliveConnection() == true)
			return true;
		String httpVer = getHTTPVersion();
		boolean isHTTP10 = 0 < httpVer.indexOf("1.0");
		return isHTTP10 != true;
	}

	// //////////////////////////////////////////////
	// read
	// //////////////////////////////////////////////

	/** 读取内容的方法 */
	public boolean read()
	{
		return super.read(getSocket());
	}

	// //////////////////////////////////////////////
	// POST (Response)
	// //////////////////////////////////////////////

	/**
	 * 发送响应消息
	 * 
	 * @param httpRes
	 * @return
	 */
	public boolean post(HTTPResponse httpRes)
	{
		// 获取HTTPSocket
		HTTPSocket httpSock = getSocket();
		long offset = 0;
		// 获取内容的长度
		long length = httpRes.getContentLength();

		if (hasContentRange() == true)
		{
			// 获取首位值
			long firstPos = getContentRangeFirstPosition();
			// 获取结束位置
			long lastPos = getContentRangeLastPosition();

			// Thanks for Brent Hills (10/26/04)
			if (lastPos <= 0)
			{
				lastPos = length - 1;
			}
			if ((firstPos > length) || (lastPos > length))
			{
				return returnResponse(HTTPStatus.INVALID_RANGE);
			}
			// 设置ContentRange头
			httpRes.setContentRange(firstPos, lastPos, length);
			// 设置状态码
			httpRes.setStatusCode(HTTPStatus.PARTIAL_CONTENT);

			offset = firstPos;
			length = lastPos - firstPos + 1;
		}
		return httpSock.post(httpRes, offset, length, isHeadRequest());
		// httpSock.close();
	}

	// //////////////////////////////////////////////
	// POST (Request)
	// //////////////////////////////////////////////

	/** POST 提交的Socket */
	private Socket postSocket = null;

	/** 发送请求 */
	public HTTPResponse post(String host, int port, boolean isKeepAlive)
	{
		// 创建一个HTTPResponse对象
		HTTPResponse httpRes = new HTTPResponse();

		// 设置主机的host
		setHost(host);

		// 设置保存连接
		setConnection((isKeepAlive == true) ? HTTP.KEEP_ALIVE : HTTP.CLOSE);

		// 判断method是不是HEAD
		boolean isHeaderRequest = isHeadRequest();

		OutputStream out = null;
		InputStream in = null;

		try
		{
			if (postSocket == null)
			{
				// Thanks for Hao Hu
				// 创建一个socket
				postSocket = new Socket();
				// 连接，并指定连接超时为80000毫秒
				postSocket.connect(new InetSocketAddress(host, port),
						HTTPServer.DEFAULT_TIMEOUT);
			}

			// 获取输出流
			out = postSocket.getOutputStream();
			PrintStream pout = new PrintStream(out);
			// 写出请求头信息
			pout.print(getHeader());
			// 写出一个回车换行
			pout.print(HTTP.CRLF);

			boolean isChunkedRequest = isChunked();
			// 获取内容字符串
			String content = getContentString();
			int contentLength = 0;
			if (content != null)
			{
				// 设置内容的长度
				contentLength = content.length();
			}

			if (0 < contentLength)
			{
				if (isChunkedRequest == true)
				{
					// Thanks for Lee Peik Feng <pflee@users.sourceforge.net>
					// (07/07/05)
					// 将内容的长度转换为16进制的字符串
					String chunSizeBuf = Long.toHexString(contentLength);
					// 写出内容长度
					pout.print(chunSizeBuf);
					// 写出回车换行
					pout.print(HTTP.CRLF);
				}
				// 写出内容
				pout.print(content);
				if (isChunkedRequest == true)
				{
					pout.print(HTTP.CRLF);
				}
			}

			if (isChunkedRequest == true)
			{
				pout.print("0");
				pout.print(HTTP.CRLF);
			}
			// 刷新缓冲区
			pout.flush(); 

			// 获取InputStream
			in = postSocket.getInputStream();
			httpRes.set(in, isHeaderRequest); 
			
 
		}
		catch (SocketException e)
		{
			// 设置状态码为500
			httpRes.setStatusCode(HTTPStatus.INTERNAL_SERVER_ERROR);
			Debug.warning(e);
		}
		catch (IOException e)
		{
			// Socket create but without connection
			// TODO Blacklistening the device
			// 设置状态码为500
			httpRes.setStatusCode(HTTPStatus.INTERNAL_SERVER_ERROR);
			Debug.warning(e);
		}
		finally
		{
			// 如果不保存连接就关闭资源
			if (isKeepAlive == false)
			{
				try
				{
					in.close();
				}
				catch (Exception e)
				{
				}

				if (in != null)
				{
					try
					{
						out.close();
					}
					catch (Exception e)
					{
					}
				}
				if (out != null)
				{
					try
					{
						postSocket.close();
					}
					catch (Exception e)
					{
					}
				}
				postSocket = null;
			}
		}

		return httpRes;
	}

	/**
	 * post提交
	 * 
	 * @param host
	 *            提交的主机地址
	 * @param port
	 *            提交的主机端口
	 * @return 返回一个 HTTPResponse
	 */
	public HTTPResponse post(String host, int port)
	{
		return post(host, port, false);
	}

	// //////////////////////////////////////////////
	// set
	// //////////////////////////////////////////////

	/**
	 * 将httpReq的属性值赋值给本类对应的属性值,并把socket赋值给本类的socket
	 * 
	 * @param httpReq
	 */
	public void set(HTTPRequest httpReq)
	{
		set((HTTPPacket) httpReq);
		setSocket(httpReq.getSocket());
	}

	// //////////////////////////////////////////////
	// OK/BAD_REQUEST
	// //////////////////////////////////////////////

	/** 设置状态码响应 */
	public boolean returnResponse(int statusCode)
	{
		// 创建HTTPResponse对象
		HTTPResponse httpRes = new HTTPResponse();
		// 设置状态码
		httpRes.setStatusCode(statusCode);
		// 设置内容长度
		httpRes.setContentLength(0);
		return post(httpRes);
	}

	/** 返回200的状态码 */
	public boolean returnOK()
	{
		return returnResponse(HTTPStatus.OK);
	}

	/** 返回400的状态码 指出客户端请求中的语法错误 */
	public boolean returnBadRequest()
	{
		return returnResponse(HTTPStatus.BAD_REQUEST);
	}

	// //////////////////////////////////////////////
	// toString
	// //////////////////////////////////////////////

	/** 返回一个完整的http请求 */
	@Override
	public String toString()
	{
		StringBuffer str = new StringBuffer();

		str.append(getHeader());
		str.append(HTTP.CRLF);
		str.append(getContentString());

		return str.toString();
	}

	/** 打印一个完整的http请求 */
	public void print()
	{
		System.out.println(toString());
	}
}
