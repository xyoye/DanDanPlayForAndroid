/******************************************************************
*
*	CyberUtil for Java
*
*	Copyright (C) Satoshi Konno 2002-2004
*
*	File: Mutex.java
*
*	Revision:
*
*	06/19/04
*		- first revision.
*
******************************************************************/

package com.xyoye.dandanplay.utils.smb.cybergarage.util;

/** Mutex 互斥 对象*/
public class Mutex
{
	/** syncLock 同步锁，默认为false */
	private boolean syncLock;
	
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////

	/** 创建一个Mutex对象，同步锁默认为false */
	public Mutex()
	{
		syncLock = false;
	}
	
	////////////////////////////////////////////////
	//	lock
	////////////////////////////////////////////////
	/**锁住线程的方法 让该线程进入等待队列*/
	public synchronized void lock()
	{
		while(syncLock == true) {
			try {
				wait();
			}
			catch (Exception e) {
				Debug.warning(e);
			}
        }
		syncLock = true;
	}

	/** 解锁所有线程的方法 */
	public synchronized void unlock()
	{
		syncLock = false;
		notifyAll();
	}

}