/******************************************************************
*
*	CyberUtil for Java
*
*	Copyright (C) Satoshi Konno 2002
*
*	File: ListenerList.java
*
*	Revision;
*
*	12/30/02
*		- first revision.
*
******************************************************************/

package com.xyoye.dandanplay.utils.smb.cybergarage.util;

import java.util.Vector;

/** ListenerList 继承  Vector */
public class ListenerList extends Vector
{
	/** 添加对象到  ListenerList 集合中，如果集合中包含此对象返回false，否则集合中添加对象返回true*/
	@Override
	public boolean add(Object obj)
	{
		if (0 <= indexOf(obj))
			return false;
		return super.add(obj);
	}
}

