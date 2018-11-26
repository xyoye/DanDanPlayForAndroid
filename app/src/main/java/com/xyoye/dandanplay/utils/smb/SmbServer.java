package com.xyoye.dandanplay.utils.smb;

import com.xyoye.dandanplay.utils.FileUtils;
import com.xyoye.dandanplay.utils.smb.cybergarage.http.HTTPRequest;
import com.xyoye.dandanplay.utils.smb.cybergarage.http.HTTPRequestListener;
import com.xyoye.dandanplay.utils.smb.cybergarage.http.HTTPResponse;
import com.xyoye.dandanplay.utils.smb.cybergarage.http.HTTPServerList;
import com.xyoye.dandanplay.utils.smb.cybergarage.http.HTTPStatus;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import jcifs.smb.SmbFile;


public class SmbServer extends Thread implements HTTPRequestListener
{

	public static final String CONTENT_EXPORT_URI = "/smb";
	public static final String tag = "dlna.certus.iptv.sharefile.server.ShareFileManager";
	private HTTPServerList httpServerList = new HTTPServerList();
	// 默认的共享端口
	private int HTTPPort = 2222;
	// 绑定的ip
	private String bindIP = null;


	public String getBindIP()
	{
		return bindIP;
	}

	public void setBindIP(String bindIP)
	{
		this.bindIP = bindIP;
	}

	public HTTPServerList getHttpServerList()
	{
		return httpServerList;
	}

	public void setHttpServerList(HTTPServerList httpServerList)
	{
		this.httpServerList = httpServerList;
	}

	public int getHTTPPort()
	{
		return HTTPPort;
	}

	public void setHTTPPort(int hTTPPort)
	{
		HTTPPort = hTTPPort;
	}

	@Override
	public void run()
	{
		super.run();

		/**************************************************
		 *
		 * 创建http服务器，接收共享请求
		 *
		 *************************************************/
		// 重试次数
		int retryCnt = 0;
		// 获取端口 2222
		int bindPort = getHTTPPort();

		HTTPServerList hsl = getHttpServerList();
		while (!hsl.open(bindPort))
		{
			retryCnt++;
			// 重试次数大于服务器重试次数时返回
			if (100 < retryCnt)
			{
				return;
			}
			setHTTPPort(bindPort + 1);
			bindPort = getHTTPPort();
		}
		// 给集合中的每个HTTPServer对象添加HTTPRequestListener对象
		hsl.addRequestListener(this);
		// 调用集合中所有HTTPServer的start方法
		hsl.start();

		LocalIPUtil.IP = hsl.getHTTPServer(0).getBindAddress();
		LocalIPUtil.PORT = hsl.getHTTPServer(0).getBindPort();

	}

	@Override
	public void httpRequestRecieved(HTTPRequest httpReq)
	{

		String uri = httpReq.getURI();

		if (!uri.startsWith(CONTENT_EXPORT_URI))
		{
			httpReq.returnBadRequest();
			return;
		}

		try
		{
			uri = URLDecoder.decode(uri, "UTF-8");
		}
		catch (UnsupportedEncodingException e1)
		{
			e1.printStackTrace();
		}
		//截取文件的信息
		String filePaths = "smb://" + uri.substring(5);

		//判断uri中是否包含参数
		int indexOf = filePaths.indexOf("&");

		if (indexOf != -1)
		{
			filePaths = filePaths.substring(0, indexOf);
		}


		try
		{
			SmbFile file = new SmbFile(filePaths);

			// 获取文件的大小
			long contentLen = file.length();
			// 获取文件类型
			String contentType = FileUtils.getFileExt(filePaths);
			// 获取文文件流
			InputStream contentIn = file.getInputStream();

			if (contentLen <= 0 || contentType.length() <= 0
					|| contentIn == null)
			{
				httpReq.returnBadRequest();
				return;
			}


			HTTPResponse httpRes = new HTTPResponse();
			httpRes.setContentType(contentType);
			httpRes.setStatusCode(HTTPStatus.OK);
			httpRes.setContentLength(contentLen);
			httpRes.setContentInputStream(contentIn);

			httpReq.post(httpRes);

			contentIn.close();
		} catch (IOException e){
			httpReq.returnBadRequest();
		}

	}

}
