/******************************************************************
 *
 *	CyberUtil for Java
 *
 *	Copyright (C) Satoshi Konno 2002
 *
 *	File: Debug.java
 *
 *	Revision;
 *
 *	11/18/02
 *		- first revision.
 *
 ******************************************************************/

package com.xyoye.dandanplay.utils.smb.cybergarage.util;

import java.io.PrintStream;

/** 调试类 */
public final class Debug
{

	/** 静态的Debug 对象 */
	public static Debug debug = new Debug();

	/** 系统的 PrintStream */
	private PrintStream out = System.out;

	/** 默认的构造方法 */
	public Debug()
	{

	}

	/** 获取 PrintStream 对象，带同步锁，线程安全的 */
	public synchronized PrintStream getOut()
	{
		return out;
	}

	/** 设置PrintStream 对象，带同步锁，线程安全的 */
	public synchronized void setOut(PrintStream out)
	{
		this.out = out;
	}

	/** Debug是否开启，true为开启，false为关闭 */
	public static boolean enabled = false;

	/** 获取Debug 对象 */
	public static Debug getDebug()
	{
		return Debug.debug;
	}

	/** 开启Debug 可以打印信息 */
	public static final void on()
	{
		enabled = true;
	}

	/** 关闭Debug 不能打印信息 */
	public static final void off()
	{
		enabled = false;
	}

	/** 判断Debug 是否开启 */
	public static boolean isOn()
	{
		return enabled;
	}

	/** 打印信息 */
	public static final void message(String s)
	{
		if (enabled == true)
			Debug.debug.getOut().println("CyberGarage message : " + s);
	}

	/** 打印信息 */
	public static final void message(String m1, String m2)
	{
		if (enabled == true)
			Debug.debug.getOut().println("CyberGarage message : ");
		Debug.debug.getOut().println(m1);
		Debug.debug.getOut().println(m2);
	}

	/** 打印警告信息 */
	public static final void warning(String s)
	{
		Debug.debug.getOut().println("CyberGarage warning : " + s);
	}

	/** 打印警告信息 */
	public static final void warning(String m, Exception e)
	{
		if (e.getMessage() == null)
		{
			Debug.debug.getOut().println(
					"CyberGarage warning : " + m + " START");
			e.printStackTrace(Debug.debug.getOut());
			Debug.debug.getOut().println("CyberGarage warning : " + m + " END");
		}
		else
		{
			Debug.debug.getOut().println(
					"CyberGarage warning : " + m + " (" + e.getMessage() + ")");
			e.printStackTrace(Debug.debug.getOut());
		}
	}

	/** 打印异常 */
	public static final void warning(Exception e)
	{
		warning(e.getMessage());
		e.printStackTrace(Debug.debug.getOut());
	}
}
