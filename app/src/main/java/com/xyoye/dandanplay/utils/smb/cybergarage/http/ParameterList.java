/******************************************************************
*
*	CyberHTTP for Java
*
*	Copyright (C) Satoshi Konno 2002-2004
*
*	File: ParameterList.java
*
*	Revision;
*
*	02/01/04
*		- first revision.
*
******************************************************************/

package com.xyoye.dandanplay.utils.smb.cybergarage.http;

import java.util.Vector;

/** ParameterList 继承Vector  保存的是Parameter对象 */
public class ParameterList extends Vector
{
	/** 创建ParameterList对象 */
	public ParameterList() 
	{
	}
	
	/** 根据索引获取Parameter 对象 */
	public Parameter at(int n)
	{
		return (Parameter)get(n);
	}

	/** 根据索引获取Parameter 对象 */
	public Parameter getParameter(int n)
	{
		return (Parameter)get(n);
	}
	
	/** 根据名字获取Parameter，如果集合中有返回该Parameter否则返回null  */
	public Parameter getParameter(String name)
	{
		if (name == null)
			return null;
		
		int nLists = size(); 
		for (int n=0; n<nLists; n++) {
			Parameter param = at(n);
			if (name.compareTo(param.getName()) == 0)
				return param;
		}
		return null;
	}

	/** 根据名字获取对应额值 */
	public String getValue(String name)
	{
		Parameter param = getParameter(name);
		if (param == null)
			return "";
		return param.getValue();
	}
}

