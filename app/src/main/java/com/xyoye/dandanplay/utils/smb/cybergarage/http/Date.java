/******************************************************************
 *
 *	CyberHTTP for Java
 *
 *	Copyright (C) Satoshi Konno 2002-2003
 *
 *	File : Date.java
 *
 *	Revision;
 *
 *	01/05/03
 *		- first revision
 *	10/20/04
 *		- Theo Beisch <theo.beisch@gmx.de>
 *		- Fixed the following methods to use HOUR_OF_DAY instead of HOUR.
 *			getHour(), getDateString() getTimeString()
 *		- Fixed getInstance() to return GMT instance.
 *
 ******************************************************************/

package com.xyoye.dandanplay.utils.smb.cybergarage.http;

import java.util.Calendar;
import java.util.TimeZone;

/** 日期时间类 */
public class Date
{
	/** Calendar 日历 */
	private Calendar cal;

	/** 创建一个Date
	 * @param cal Calendar对象
	 **/
	public Date(Calendar cal)
	{
		this.cal = cal;
	}

	/** 获取 Calendar 日历对象*/
	public Calendar getCalendar()
	{
		return cal;
	}

	////////////////////////////////////////////////
	//	Time
	////////////////////////////////////////////////

	/** 获取时 */
	public int getHour()
	{
		// Thanks for Theo Beisch (10/20/04)
		//Calendar.HOUR_OF_DAY 一天中的小时
		return getCalendar().get(Calendar.HOUR_OF_DAY);
	}

	/** 获取分 */
	public int getMinute()
	{
		//Calendar.MINUTE 指示一小时中的分钟
		return getCalendar().get(Calendar.MINUTE);
	}

	/** 获取秒 */
	public int getSecond()
	{
		// Calendar.SECOND 指示一分钟中的秒
		return getCalendar().get(Calendar.SECOND);
	}

	////////////////////////////////////////////////
	//	paint
	////////////////////////////////////////////////

	/** 获取本地Date的实例 */
	public final static Date getLocalInstance()
	{
		return new Date(Calendar.getInstance());
	}

	/** 指定GMT格式的Date */
	public final static Date getInstance()
	{
		// Thanks for Theo Beisch (10/20/04)
		return new Date(Calendar.getInstance(TimeZone.getTimeZone("GMT")));
	}

	////////////////////////////////////////////////
	//	getDateString
	////////////////////////////////////////////////

	public final static String toDateString(int value)
	{
		if (value < 10)
			return "0" + Integer.toString(value);
		return Integer.toString(value);
	}

	/** 月的数组 */
	private final static String MONTH_STRING[] = {
			"Jan",
			"Feb",
			"Mar",
			"Apr",
			"May",
			"Jun",
			"Jul",
			"Aug",
			"Sep",
			"Oct",
			"Nov",
			"Dec",
	};

	/** 获取月份 */
	public final static String toMonthString(int value)
	{
		value -= Calendar.JANUARY;
		if (0 <= value && value < 12)
			return MONTH_STRING[value];
		return "";
	}

	/** 周的数组 */
	private final static String WEEK_STRING[] = {
			"Sun",
			"Mon",
			"Tue",
			"Wed",
			"Thu",
			"Fri",
			"Sat",
	};

	/** 获取星期中的第几天 */
	public final static String toWeekString(int value)
	{
		value -= Calendar.SUNDAY;
		if (0 <= value && value < 7)
			return WEEK_STRING[value];
		return "";
	}

	/** 如果传入的int小于10，则在前面加上0 */
	public final static String toTimeString(int value)
	{
		String str  = "";
		if (value < 10)
			str += "0";
		str += Integer.toString(value);
		return str;
	}

	/** 获取要发出日期消息头的值 */
	public String getDateString()
	{
		// Thanks for Theo Beisch (10/20/04)
		Calendar cal = getCalendar();
		return
				toWeekString(cal.get(Calendar.DAY_OF_WEEK)) +", " +
						toTimeString(cal.get(Calendar.DATE)) + " " +
						toMonthString(cal.get(Calendar.MONTH)) + " " +
						Integer.toString(cal.get(Calendar.YEAR)) + " " +
						toTimeString(cal.get(Calendar.HOUR_OF_DAY)) + ":" +
						toTimeString(cal.get(Calendar.MINUTE)) + ":" +
						toTimeString(cal.get(Calendar.SECOND)) + " GMT";
	}

	////////////////////////////////////////////////
	//	getTimeString
	////////////////////////////////////////////////

	public String getTimeString()
	{
		// Thanks for Theo Beisch (10/20/04)
		Calendar cal = getCalendar();
		return
				toDateString(cal.get(Calendar.HOUR_OF_DAY)) +
						(((cal.get(Calendar.SECOND) % 2) == 0) ? ":" : " ") +
						toDateString(cal.get(Calendar.MINUTE));
	}

}

