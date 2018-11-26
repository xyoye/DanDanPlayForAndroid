/******************************************************************
 *
 *	CyberUtil for Java
 *
 *	Copyright (C) Satoshi Konno 2002-2004
 *
 *	File: Thread.java
 *
 *	Revision:
 *
 *	01/05/04
 *		- first revision.
 *	08/23/07
 *		- Thanks for Kazuyuki Shudo
 *		- Changed stop() to stop more safety using Thread::interrupt().
 *
 ******************************************************************/

package com.xyoye.dandanplay.utils.smb.cybergarage.util;

/** ThreadCore 继承 Runnable */
public class ThreadCore implements Runnable
{
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////

	public ThreadCore()
	{
	}

	////////////////////////////////////////////////
	//	Thread
	////////////////////////////////////////////////

	private java.lang.Thread mThreadObject = null;

	/** 设置线程对象 */
	public void setThreadObject(java.lang.Thread obj) {
		mThreadObject = obj;
	}

	/** 获取线程对象 */
	public java.lang.Thread getThreadObject() {
		return mThreadObject;
	}

	/** 启动线程 */
	public void start()
	{
		java.lang.Thread threadObject = getThreadObject();
		if (threadObject == null) {
			threadObject = new java.lang.Thread(this,"Cyber.ThreadCore");
			setThreadObject(threadObject);
			threadObject.start();
		}
	}

	@Override
	public void run()
	{
	}

	/** 判断当前运行的线程是否 mThreadObject 对象  是返回true 否则返回false*/
	public boolean isRunnable()
	{
		return (Thread.currentThread() == getThreadObject()) ? true : false;
	}

	/** 停止线程并设置 mThreadObject 为null*/
	public void stop()
	{
		java.lang.Thread threadObject = getThreadObject();
		if (threadObject != null) {
			//threadObject.destroy();
			//threadObject.stop();

			// Thanks for Kazuyuki Shudo (08/23/07)
			threadObject.interrupt();

			setThreadObject(null);
		}
	}

	/** 重启线程的方法 */
	public void restart()
	{
		stop();
		start();
	}
}
