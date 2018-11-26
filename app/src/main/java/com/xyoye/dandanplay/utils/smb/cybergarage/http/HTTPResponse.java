/******************************************************************
*
*	CyberHTTP for Java
*
*	Copyright (C) Satoshi Konno 2002-2003
*
*	File: HTTPResponse.java
*
*	Revision;
*
*	11/18/02
*		- first revision.
*	10/22/03
*		- Changed to initialize a content length header.
*	10/22/04
*		- Added isSuccessful().
*	
******************************************************************/

package com.xyoye.dandanplay.utils.smb.cybergarage.http;

import java.io.InputStream;

/** HTTPResponse 继承 HTTPPacket   */
public class HTTPResponse extends HTTPPacket
{
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////
	
	/** 创建一个默认的HTTPResponse 对象
	 * Version = 1.1
	 */
	public HTTPResponse()
	{
		setVersion(HTTP.VERSION_11);
		//设置内容类型为text/html; charset="utf-8"
		setContentType(HTML.CONTENT_TYPE);
		//设置服务的名字
		setServer(HTTPServer.getName());
		setContent("");
	}

	/** 创建HTTPResponse 对象 */
	public HTTPResponse(HTTPResponse httpRes)
	{
		//设置响应
		set(httpRes);
	}

	public HTTPResponse(InputStream in)
	{
		super(in);
	}

	public HTTPResponse(HTTPSocket httpSock)
	{
		this(httpSock.getInputStream());
	}

	////////////////////////////////////////////////
	//	Status Line
	////////////////////////////////////////////////

	/** 状态码 */
	private int statusCode = 0;
	
	/** 设置状态码 */
	public void setStatusCode(int code)
	{
		statusCode = code;
	}

	/** 获取状态码 */
	public int getStatusCode()
	{
		if (statusCode != 0)
			return statusCode;
		HTTPStatus httpStatus = new HTTPStatus(getFirstLine());
		return httpStatus.getStatusCode();
	}

	/** 判断状态码 是否在200-299之间 判断是否成功，成功返回true，否则返回false */
	public boolean isSuccessful()
	{
		return HTTPStatus.isSuccessful(getStatusCode());
	}
	
	/** 获取状态行 */
	public String getStatusLineString()
	{
		return "HTTP/" + getVersion() + " " + getStatusCode() + " " + HTTPStatus.code2String(statusCode) + HTTP.CRLF;
	}
	
	////////////////////////////////////////////////
	//	getHeader
	////////////////////////////////////////////////
	
	/** 获取状态行和多个消息头 */
	public String getHeader()
	{
		StringBuffer str = new StringBuffer();
	
		str.append(getStatusLineString());
		str.append(getHeaderString());
		
		return str.toString();
	}

	////////////////////////////////////////////////
	//	toString
	////////////////////////////////////////////////
	
	/** 返回一个完整的http响应 */
	@Override
	public String toString()
	{
		StringBuffer str = new StringBuffer();

		str.append(getStatusLineString());
		str.append(getHeaderString());
		str.append(HTTP.CRLF);
		str.append(getContentString());
		
		return str.toString();
	}

	public void print()
	{
		System.out.println(toString());
	}
}
