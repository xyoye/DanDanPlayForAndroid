/******************************************************************
*
*	CyberHTTP for Java
*
*	Copyright (C) Satoshi Konno 2002
*
*	File: HTTPStatus.java
*
*	Revision;
*
*	12/17/02
*		- first revision.
*	09/03/03
*		- Added CONTINUE_STATUS.
*	10/20/04 
*		- Brent Hills <bhills@openshores.com>
*		- Added PARTIAL_CONTENT and INVALID_RANGE;
*	10/22/04
*		- Added isSuccessful().
*	10/29/04
*		- Fixed set() to set the version and the response code when the mothod is null.
*		- Fixed set() to read multi words of the response sring such as Not Found.
*	
******************************************************************/

package com.xyoye.dandanplay.utils.smb.cybergarage.http;
import com.xyoye.dandanplay.utils.smb.cybergarage.util.Debug;

import java.util.StringTokenizer;

/*** HTTPStatus http状态 */
public class HTTPStatus 
{
	////////////////////////////////////////////////
	//	Code
	////////////////////////////////////////////////
	
	/**
	 * 请求者应当继续提出请求。服务器返回此代码则意味着，服务器已收到了请求的第一部分，现正在等待接收其余部分。
	 */
	public static final int CONTINUE = 100;
	
	
	/**
	 *  服务器已成功处理了请求 
	 */
	public static final int OK = 200;
	//	Thanks for Brent Hills (10/20/04)
	/**
	 * 服务器成功处理了部分 GET 请求
	 */
	public static final int PARTIAL_CONTENT = 206;
	
	/**
	 * 服务器不理解请求的语法 
	 */
	public static final int BAD_REQUEST = 400;
	
	
	/**
	 * 服务器找不到请求的网页 
	 */
	public static final int NOT_FOUND = 404;
	
	/**
	 *服务器未满足请求者在请求中设置的其中一个前提条件。 
	 */
	public static final int PRECONDITION_FAILED = 412;
	//	Thanks for Brent Hills (10/20/04)
	
	/**
	 * 所请求的范围无法满足 
	 */
	public static final int INVALID_RANGE = 416;
	
	
	/**
	 * 服务器遇到错误，无法完成请求 
	 */
	public static final int INTERNAL_SERVER_ERROR = 500;

	/** 将状态码转换成提示的字符串 ,如果没有此状态码返回一个空字符串*/
	public static final String code2String(int code)
	{
		switch (code) {
		case CONTINUE: return "Continue";
		case OK: return "OK";
		case PARTIAL_CONTENT: return "Partial Content";
		case BAD_REQUEST: return "Bad Request";
		case NOT_FOUND: return "Not Found";
		case PRECONDITION_FAILED: return "Precondition Failed";
		case INVALID_RANGE: return "Invalid Range";
		case INTERNAL_SERVER_ERROR: return "Internal Server Error";
		}
		 return "";
	}
 	
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////

	public HTTPStatus()
	{
		setVersion("");
		setStatusCode(0);
		setReasonPhrase("");
	}
	
	public HTTPStatus(String ver, int code, String reason)
	{
		setVersion(ver);
		setStatusCode(code);
		setReasonPhrase(reason);
	}

	/** 创建一个 HTTPStatus
	 * @param
	 * 传入一个状态行 设置 状态 */
	public HTTPStatus(String lineStr)
	{
		set(lineStr);
	}
	
	////////////////////////////////////////////////
	//	Member
	////////////////////////////////////////////////

	/** http协议的版本 */
	private String version = "";
	/** response的状态码 */
	private int statusCode = 0;
	/** reasonPhrase 原因短语 */
	private String reasonPhrase = "";

	/** 设置http版本 */
	public void setVersion(String value)
	{
		version = value;
	}
	
	/** 设置http状态码 */
	public void setStatusCode(int value)
	{
		statusCode = value;
	}
	
	/** 设置原因短语 */
	public void setReasonPhrase(String value)
	{
		reasonPhrase = value;
	}
	
	/** 获取http版本 */
	public String getVersion()
	{
		return version;
	}
	
	/** 获取http状态码 */
	public int getStatusCode()
	{
		return statusCode;
	}
	
	/** 获取原因短语 */
	public String getReasonPhrase()
	{
		return reasonPhrase;
	}

	////////////////////////////////////////////////
	//	Status
	////////////////////////////////////////////////

	/** 如果状态码是200-299之间返回true 否则返回false
	 * 200-299 用于表示请求成功。 
	 */
	final public static boolean isSuccessful(int statCode)
	{
		if (200 <= statCode && statCode < 300){
			return true;
		}
		return false;
	}
	
	public boolean isSuccessful()
	{
		return isSuccessful(getStatusCode());
	}

	////////////////////////////////////////////////
	//	set
	////////////////////////////////////////////////
	
	/**
	 *	设置http协议版本，状态码，原因短语
	 *	@param lineStr 状态行
	 */
	public void set(String lineStr)
	{
		if (lineStr == null) {
			//设置协议版本
			setVersion(HTTP.VERSION);
			//设置状态码
			setStatusCode(INTERNAL_SERVER_ERROR);
			//设置原因短语
			setReasonPhrase(code2String(INTERNAL_SERVER_ERROR));
			return;
		}

		try {
			StringTokenizer st = new StringTokenizer(lineStr, HTTP.STATUS_LINE_DELIM);
		
			if (st.hasMoreTokens() == false){
				return;
			}
			String ver = st.nextToken();
			//设置http版本
			setVersion(ver.trim());
			
			if (st.hasMoreTokens() == false){
				return;
			}
			String codeStr = st.nextToken();
			int code = 0;
			try {
				code = Integer.parseInt(codeStr);
			}
			catch (Exception e1) {}
			//设置状态码
			setStatusCode(code);
			
			String reason = "";
			while (st.hasMoreTokens() == true) {
				if (0 <= reason.length()){
					reason += " ";
				}
				reason += st.nextToken();
			}
			//设置状态短语
			setReasonPhrase(reason.trim());
		}
		catch (Exception e) {
			Debug.warning(e);
		}

	}	
}
