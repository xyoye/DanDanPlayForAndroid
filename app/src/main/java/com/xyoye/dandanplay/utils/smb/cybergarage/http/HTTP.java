/******************************************************************
 *
 *	CyberHTTP for Java
 *
 *	Copyright (C) Satoshi Konno 2002
 *
 *	File: HTTP.java
 *
 *	Revision:
 *
 *	11/18/02
 *		- first revision.
 *	08/30/03
 *		- Giordano Sassaroli <sassarol@cefriel.it>
 *		- Problem : the method getPort should return the default http port 80 when a port is not specified
 *		- Description : the method is used in ControlRequest.setRequestHost() and in SubscriptionRequest.setService(). maybe the default port check could be done in these methods.
 *	09/03/02
 *		- Added getRequestHostURL().
 *	03/11/04
 *		- Added the following methods to send big content stream.
 *		  post(HTTPResponse, byte[])
 *		  post(HTTPResponse, InputStream)
 *	05/26/04
 *		- Added NO_CATCH and MAX_AGE.
 *	10/20/04 
 *		- Brent Hills <bhills@openshores.com>
 *		- Added Range and MYNAME;
 *	
 ******************************************************************/

package com.xyoye.dandanplay.utils.smb.cybergarage.http;

import java.net.URL;

public class HTTP
{
	// //////////////////////////////////////////////
	// Constants
	// //////////////////////////////////////////////

	public static final String HOST = "HOST";

	public static final String VERSION = "1.1";
	public static final String VERSION_10 = "1.0";
	public static final String VERSION_11 = "1.1";

	/** CRLF 回车换行 \r\n */
	public static final String CRLF = "\r\n";
	/** CR 回车 \r */
	public static final byte CR = '\r';
	/** LF 换行 \n */
	public static final byte LF = '\n';
	/** TAB \t */
	public static final String TAB = "\t";

	public static final String SOAP_ACTION = "SOAPACTION";

	public static final String M_SEARCH = "M-SEARCH";
	public static final String NOTIFY = "NOTIFY";
	public static final String POST = "POST";
	public static final String GET = "GET";
	public static final String HEAD = "HEAD";
	public static final String SUBSCRIBE = "SUBSCRIBE";
	public static final String UNSUBSCRIBE = "UNSUBSCRIBE";

	public static final String DATE = "Date";
	public static final String CACHE_CONTROL = "Cache-Control";
	public static final String NO_CACHE = "no-cache";
	public static final String MAX_AGE = "max-age";
	public static final String CONNECTION = "Connection";
	public static final String CLOSE = "close";
	public static final String KEEP_ALIVE = "Keep-Alive";
	public static final String CONTENT_TYPE = "Content-Type";
	public static final String CHARSET = "charset";
	public static final String CONTENT_LENGTH = "Content-Length";
	public static final String CONTENT_RANGE = "Content-Range";
	public static final String CONTENT_RANGE_BYTES = "bytes";
	// Thanks for Brent Hills (10/20/04)
	public static final String RANGE = "Range";
	public static final String TRANSFER_ENCODING = "Transfer-Encoding";
	public static final String CHUNKED = "Chunked";
	public static final String LOCATION = "Location";
	public static final String SERVER = "Server";

	/** ST头为搜索目标 */
	public static final String ST = "ST";
	/** MX 最长等待时间 */
	public static final String MX = "MX";
	/** MAN标头的值采用双引号。值必须是"ssdp:discover" */
	public static final String MAN = "MAN";
	public static final String NT = "NT";
	/** Network Test System 网络测试系统系统 */
	public static final String NTS = "NTS";
	public static final String USN = "USN";
	public static final String EXT = "EXT";
	public static final String SID = "SID";
	public static final String SEQ = "SEQ";
	public final static String CALLBACK = "CALLBACK";
	public final static String TIMEOUT = "TIMEOUT";
	// Thanks for Brent Hills (10/20/04)
	public final static String MYNAME = "MYNAME";

	public static final String REQEST_LINE_DELIM = " ";
	public static final String HEADER_LINE_DELIM = " :";
	/** STATUS_LINE_DELIM 是一个空格 */
	public static final String STATUS_LINE_DELIM = " ";

	public static final int DEFAULT_PORT = 80;
	public static final int DEFAULT_CHUNK_SIZE = 512 * 1024;
	public static final int DEFAULT_TIMEOUT = 15;

	// //////////////////////////////////////////////
	// URL
	// //////////////////////////////////////////////

	/** 判断是不是有效的URL是返回true，否则返回false */
	public static final boolean isAbsoluteURL(String urlStr)
	{
		try
		{
			new URL(urlStr);
			return true;
		}
		catch (Exception e)
		{

			return false;
		}
	}

	/** 获取urlStr的地址 */
	public static final String getHost(String urlStr)
	{
		try
		{
			URL url = new URL(urlStr);
			return url.getHost();
		}
		catch (Exception e)
		{
			return "";
		}
	}

	/** 获取urlStr 的端口 */
	public static final int getPort(String urlStr)
	{
		try
		{
			URL url = new URL(urlStr);
			// Thanks for Giordano Sassaroli <sassarol@cefriel.it> (08/30/03)
			int port = url.getPort();
			if (port <= 0)
				port = DEFAULT_PORT;
			return port;
		}
		catch (Exception e)
		{
			return DEFAULT_PORT;
		}
	}

	/**
	 * 获取请求的主机地址
	 * 
	 * @param host
	 *            主机地址
	 * @param port
	 *            主机端口
	 **/
	public static final String getRequestHostURL(String host, int port)
	{
		String reqHost = "http://" + host + ":" + port;
		return reqHost;
	}

	/**
	 * urlStr url withParam uri后面是否带参数，是为true，否则为false 返回uri格式的字符串
	 * 例如:/service/ContentDirectory_event或
	 * /service/ContentDirectory_event?name=tom
	 */
	public static final String toRelativeURL(String urlStr, boolean withParam)
	{
		String uri = urlStr;
		System.out.println("33333---" + uri);
		// 如果不是正确的url就判断是否以/开头，如果不是就在URL前面加/，否则不操作
		if (isAbsoluteURL(urlStr) == false)
		{
			if (0 < urlStr.length() && urlStr.charAt(0) != '/')
			{
				uri = "/" + urlStr;
			}
			System.out.println("1---" + uri);
		}
		else
		{
			try
			{
				// 创建URL对象
				URL url = new URL(urlStr);
				uri = url.getPath();
				System.out.println("22222222---" + uri);
				if (withParam == true)
				{
					String queryStr = url.getQuery();
					if (!"".equals(queryStr))
					{
						uri += "?" + queryStr;
					}
				}
				System.out.println("22---" + uri);
				if (uri.endsWith("/"))
				{
					uri = uri.substring(0, uri.length() - 1);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			System.out.println("2---" + uri);
		}
		return uri;
	}

	public static final String toRelativeURL(String urlStr)
	{
		return toRelativeURL(urlStr, true);
	}

	/** 返回一个完整的URL */
	public static final String getAbsoluteURL(String baseURLStr,
                                              String relURlStr)
	{
		try
		{
			URL baseURL = new URL(baseURLStr);
			String url = baseURL.getProtocol() + "://" + baseURL.getHost()
					+ ":" + baseURL.getPort() + toRelativeURL(relURlStr);
			return url;
		}
		catch (Exception e)
		{
			return "";
		}
	}

	// //////////////////////////////////////////////
	// Chunk Size
	// //////////////////////////////////////////////

	/** 块大小 默认是 512 * 1024 */
	private static int chunkSize = DEFAULT_CHUNK_SIZE;

	/** 设置chunkSize */
	public static final void setChunkSize(int size)
	{
		chunkSize = size;
	}

	/** 获取chunkSize 默认是512 * 1024 */
	public static final int getChunkSize()
	{
		return chunkSize;
	}

}
