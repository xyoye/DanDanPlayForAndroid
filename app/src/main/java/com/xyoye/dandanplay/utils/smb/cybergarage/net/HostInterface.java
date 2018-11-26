/******************************************************************
 *
 *	CyberHTTP for Java
 *
 *	Copyright (C) Satoshi Konno 2002-2003
 *
 *	File: HostInterface.java
 *
 *	Revision;
 *
 *	05/12/03
 *		- first revision.
 *	05/13/03
 *		- Added support for IPv6 and loopback address.
 *	02/15/04
 *		- Added the following methods to set only a interface.
 *		- setInterface(), getInterfaces(), hasAssignedInterface()
 *	06/30/04
 *		- Moved the package from org.cybergarage.http to org.cybergarage.net.
 *	06/30/04
 *		- Theo Beisch <theo.beisch@gmx.de>
 *		- Changed isUseAddress() to isUsableAddress().
 *	
 ******************************************************************/

package com.xyoye.dandanplay.utils.smb.cybergarage.net;

import com.xyoye.dandanplay.utils.smb.cybergarage.util.Debug;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;


/** 主机接口 */
public class HostInterface
{
	private static final String TAG  = "org.cybergarage.net.HostInterface";
	// //////////////////////////////////////////////
	// Constants
	// //////////////////////////////////////////////

	public static boolean USE_LOOPBACK_ADDR = false;
	/** 设置使用ipv4地址 */
	public static boolean USE_ONLY_IPV4_ADDR = false;
	public static boolean USE_ONLY_IPV6_ADDR = false;

	// //////////////////////////////////////////////
	// Network Interfaces
	// //////////////////////////////////////////////

	/** 接口地址 */
	private static String ifAddress = "";
	public final static int IPV4_BITMASK = 0x0001;
	public final static int IPV6_BITMASK = 0x0010;
	public final static int LOCAL_BITMASK = 0x0100;

	/** 设置接口地址 */
	public final static void setInterface(String ifaddr)
	{
		System.out.println("设置=======================================");
		ifAddress = ifaddr;
	}

	/** 获取地址 */
	public final static String getInterface()
	{
		return ifAddress;
	}

	/** 判断是否 已分配的接口 */
	private final static boolean hasAssignedInterface()
	{
		return (0 < ifAddress.length()) ? true : false;
	}

	// //////////////////////////////////////////////
	// Network Interfaces
	// //////////////////////////////////////////////

	// Thanks for Theo Beisch (10/27/04)
	/** 判断是否可用的地址 */
	private final static boolean isUsableAddress(InetAddress addr)
	{
		if (USE_LOOPBACK_ADDR == false)
		{
			// 检查 InetAddress 是否是回送地址的实用例行程序。
			if (addr.isLoopbackAddress() == true || addr.isLinkLocalAddress() == true)
			{
				return false;
			}
		}
		
		
		if (USE_ONLY_IPV4_ADDR == true)
		{
			if (addr instanceof Inet6Address)
			{
				return false;
			}
		}
		if (USE_ONLY_IPV6_ADDR == true)
		{
			if (addr instanceof Inet4Address)
			{
				return false;
			}
		}
		return true;
	}

	/** 获取主机地址的数量 */
	public final static int getNHostAddresses()
	{

		// System.out.println("===============================测试静态变量");
		// System.out.println("USE_LOOPBACK_ADDR="+USE_LOOPBACK_ADDR);
		// System.out.println("USE_ONLY_IPV4_ADDR="+USE_ONLY_IPV4_ADDR);
		// System.out.println("USE_ONLY_IPV6_ADDR="+USE_ONLY_IPV6_ADDR);
		// System.out.println("===============================");
		// System.out.println("getNHostAddresses");
		if (hasAssignedInterface() == true)
		{
			System.out.println("已经分配接口");
			return 1;
		}

		int nHostAddrs = 0;
		try
		{
			// 返回此机器上的所有接口。
			Enumeration nis = NetworkInterface.getNetworkInterfaces();
			while (nis.hasMoreElements())
			{
				NetworkInterface ni = (NetworkInterface) nis.nextElement();
				// 返回一个具有绑定到此网络接口全部或部分 InetAddress 的 Enumeration。
				Enumeration<InetAddress> addrs = ni.getInetAddresses();
				while (addrs.hasMoreElements())
				{
					InetAddress addr = addrs.nextElement();
					 
					
					if (isUsableAddress(addr) == false)
					{ 
						continue;
					}
					nHostAddrs++;
				}
			}
		}
		catch (Exception e)
		{
			Debug.warning(e);
		}
		;
		return nHostAddrs;
	}

	/**
	 * 
	 * @param ipfilter
	 * @param interfaces
	 * @return
	 * @since 1.8.0
	 * @author Stefano "Kismet" Lenzi &lt;kismet.sl@gmail.com&gt;
	 */
	public final static InetAddress[] getInetAddress(int ipfilter,
                                                     String[] interfaces)
	{
		Enumeration nis;
		if (interfaces != null)
		{
			Vector iflist = new Vector();
			for (int i = 0; i < interfaces.length; i++)
			{
				NetworkInterface ni;
				try
				{
					ni = NetworkInterface.getByName(interfaces[i]);
				}
				catch (SocketException e)
				{
					continue;
				}
				if (ni != null)
					iflist.add(ni);

			}
			nis = iflist.elements();
		}
		else
		{
			try
			{
				nis = NetworkInterface.getNetworkInterfaces();
			}
			catch (SocketException e)
			{
				return null;
			}
		}
		ArrayList addresses = new ArrayList();
		while (nis.hasMoreElements())
		{
			NetworkInterface ni = (NetworkInterface) nis.nextElement();
			Enumeration addrs = ni.getInetAddresses();
			while (addrs.hasMoreElements())
			{
				InetAddress addr = (InetAddress) addrs.nextElement();
				if (((ipfilter & LOCAL_BITMASK) == 0)
						&& addr.isLoopbackAddress())
					continue;

				if (((ipfilter & IPV4_BITMASK) != 0)
						&& addr instanceof Inet4Address)
				{
					addresses.add(addr);
				}
				else if (((ipfilter & IPV6_BITMASK) != 0)
						&& addr instanceof InetAddress)
				{
					addresses.add(addr);
				}
			}
		}
		return (InetAddress[]) addresses.toArray(new InetAddress[] {});
	}

	/** 获取主机地址 */
	public final static String getHostAddress(int n)
	{
		if (hasAssignedInterface() == true)
			return getInterface();

		int hostAddrCnt = 0;
		try
		{
			// 返回此机器上的所有接口。
			Enumeration<NetworkInterface> nis = NetworkInterface
					.getNetworkInterfaces();
			while (nis.hasMoreElements())
			{
				NetworkInterface ni = nis.nextElement();
				// 一个便捷方法，返回一个具有绑定到此网络接口全部或部分 InetAddress 的 Enumeration。
				Enumeration<InetAddress> addrs = ni.getInetAddresses();
				while (addrs.hasMoreElements())
				{
					InetAddress addr = addrs.nextElement();
					if (isUsableAddress(addr) == false)
						continue;
					if (hostAddrCnt < n)
					{
						hostAddrCnt++;
						continue;
					}
					String host = addr.getHostAddress();
					// System.out.println("========================================");
					// System.out.println("host="+host);
					// if (addr instanceof Inet6Address)
					// host = "[" + host + "]";
					return host;
				}
			}
		}
		catch (Exception e)
		{
		}
		;
		return "";
	}

	// //////////////////////////////////////////////
	// isIPv?Address
	// //////////////////////////////////////////////

	/** 判断host如果是IPv6 地址返回true，否则返回false */
	public final static boolean isIPv6Address(String host)
	{
		try
		{
			InetAddress addr = InetAddress.getByName(host);
			if (addr instanceof Inet6Address)
			{
				return true;
			}
			return false;
		}
		catch (Exception e)
		{
		}
		return false;
	}

	/** 判断host如果是IPv4 地址返回true，否则返回false */
	public final static boolean isIPv4Address(String host)
	{
		try
		{
			InetAddress addr = InetAddress.getByName(host);
			if (addr instanceof Inet4Address)
			{
				return true;
			}
			return false;
		}
		catch (Exception e)
		{
		}
		return false;
	}

	// //////////////////////////////////////////////
	// hasIPv?Interfaces
	// //////////////////////////////////////////////

	public final static boolean hasIPv4Addresses()
	{
		int addrCnt = getNHostAddresses();
		for (int n = 0; n < addrCnt; n++)
		{
			String addr = getHostAddress(n);
			if (isIPv4Address(addr) == true)
				return true;
		}
		return false;
	}

	public final static boolean hasIPv6Addresses()
	{
		int addrCnt = getNHostAddresses();
		for (int n = 0; n < addrCnt; n++)
		{
			String addr = getHostAddress(n);
			if (isIPv6Address(addr) == true)
				return true;
		}
		return false;
	}

	// //////////////////////////////////////////////
	// hasIPv?Interfaces
	// //////////////////////////////////////////////

	public final static String getIPv4Address()
	{
		int addrCnt = getNHostAddresses();
		for (int n = 0; n < addrCnt; n++)
		{
			String addr = getHostAddress(n);
			if (isIPv4Address(addr) == true)
				return addr;
		}
		return "";
	}

	public final static String getIPv6Address()
	{
		int addrCnt = getNHostAddresses();
		for (int n = 0; n < addrCnt; n++)
		{
			String addr = getHostAddress(n);
			if (isIPv6Address(addr) == true)
				return addr;
		}
		return "";
	}

	// //////////////////////////////////////////////
	// getHostURL
	// //////////////////////////////////////////////

	/** 获取URL */
	public final static String getHostURL(String host, int port, String uri)
	{
		String hostAddr = host;
		if (isIPv6Address(host) == true)
		{
			hostAddr = "[" + host + "]";
		}
		return "http://" + hostAddr + ":" + Integer.toString(port) + uri;
	}

}
