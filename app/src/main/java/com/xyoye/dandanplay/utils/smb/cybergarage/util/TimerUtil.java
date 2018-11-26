/******************************************************************
*
*	CyberUtil for Java
*
*	Copyright (C) Satoshi Konno 2002-2003
*
*	File: TimerUtil.java
*
*	Revision:
*
*	01/15/03
*		- first revision.
*
******************************************************************/

package com.xyoye.dandanplay.utils.smb.cybergarage.util;

public final class TimerUtil
{
	/** 设置线程的睡眠
	 * @param waitTime 睡眠时间
	 *  
	 */
	public final static void wait(int waitTime)
	{
		try {
			Thread.sleep(waitTime);
		}
		catch (Exception e) {}
	}

	/** 线程随机时间睡眠
	 * @param  time 睡眠时间×随机数 等于线程的睡眠时间
	 */
	public final static void waitRandom(int time)
	{
		int waitTime = (int)(Math.random() * time);
		try {
			Thread.sleep(waitTime);
		}
		catch (Exception e) {}
	}
}

