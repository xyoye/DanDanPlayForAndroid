/******************************************************************
*
*	CyberHTTP for Java
*
*	Copyright (C) Satoshi Konno 2002
*
*	File: HTTPHeader.java
*
*	Revision;
*
*	11/19/02
*		- first revision.
*	05/26/04
*		- Jan Newmarch <jan.newmarch@infotech.monash.edu.au> (05/26/04)
*		- Fixed getValue() to compare using String::equals() instead of String::startWidth().
*	
******************************************************************/

package com.xyoye.dandanplay.utils.smb.cybergarage.http;
import com.xyoye.dandanplay.utils.smb.cybergarage.util.Debug;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;

public class HTTPHeader 
{
	private static int MAX_LENGTH = 1024;
	
	private String name;
	private String value;

	/** 创建一个 HTTPHeader对象
	 *	@param name 赋值给HTTPHeader的name字段
	 *	@param value 赋值给HTTPHeader的value字段
	 */
	public HTTPHeader(String name, String value)
	{
		setName(name);
		setValue(value);
	}

	/** 通过消息头设置消息头的名字，和消息头的值 */
	public HTTPHeader(String lineStr)
	{
		setName("");
		setValue("");
		if (lineStr == null){
			return;
		}
		int colonIdx = lineStr.indexOf(':');
		if (colonIdx < 0){
			return;
		}
		//获取消息头的名字
		String name = new String(lineStr.getBytes(), 0, colonIdx);
		//获取消息头的值
		String value = new String(lineStr.getBytes(), colonIdx+1, lineStr.length()-colonIdx-1);
		//设置消息头的名字
		setName(name.trim());
		//设置消息头的值
		setValue(value.trim());
	}

	////////////////////////////////////////////////
	//	Member
	////////////////////////////////////////////////
	
	/** 设置name */
	public void setName(String name)
	{
		this.name = name;
	}
	/** 设置value */
	public void setValue(String value)
	{
		this.value = value;
	}

	/** 获取name */
	public String getName()
	{
		return name;
	}

	/** 获取value */
	public String getValue()
	{
		return value;
	}

	/** 判断消息头的名字是否为null或空字符串，是返回false，否则返回true */
	public boolean hasName()
	{
        return name != null && name.length() > 0;
    }
	
	////////////////////////////////////////////////
	//	static methods
	////////////////////////////////////////////////
	//HOST: 239.255.255.250:1900
	/** 根据对应的name返回消息头的值 */
	public final static String getValue(LineNumberReader reader, String name)
	{
		//将name转换成大写
		String bigName = name.toUpperCase();
		try {
			//读取一行
			String lineStr = reader.readLine();
			while (lineStr != null && 0 < lineStr.length()) {
				//创建消息头
				HTTPHeader header = new HTTPHeader(lineStr);
				if (header.hasName() == false) {
					 lineStr = reader.readLine();
					continue;
				}
				String bigLineHeaderName = header.getName().toUpperCase();
				// Thanks for Jan Newmarch <jan.newmarch@infotech.monash.edu.au> (05/26/04)
				if (bigLineHeaderName.equals(bigName) == false) {
					 lineStr = reader.readLine();
					 continue;
				}
				return header.getValue();
			}
		}
		catch (IOException e) {
			Debug.warning(e);
			return "";
		}
		return "";
	}

	/** 根据对应的name返回消息头的值 */
	public final static String getValue(String data, String name)
	{
		/* Thanks for Stephan Mehlhase (2010-10-26) */
		//创建一个StringReader
		StringReader strReader = new StringReader(data);
		//创建LineNumberReader 对象
		LineNumberReader lineReader = new LineNumberReader(strReader, Math.min(data.length(), MAX_LENGTH));
		return getValue(lineReader, name);
	}

	/** 根据对应的name返回消息头的值 */
	public final static String getValue(byte[] data, String name)
	{
		return getValue(new String(data), name);
	}

	public final static int getIntegerValue(String data, String name)
	{
		try {
			return Integer.parseInt(getValue(data, name));
		}
		catch (Exception e) {
			return 0;
		}
	}

	/** 根据name获取data中的消息头，返回int */
	public final static int getIntegerValue(byte[] data, String name)
	{
		try {
			return Integer.parseInt(getValue(data, name));
		}
		catch (Exception e) {
			return 0;
		}
	}
}
