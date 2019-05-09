/******************************************************************
*
*	CyberHTTP for Java
*
*	Copyright (C) Satoshi Konno 2002-2004
*
*	File: HTTPConnection.java
*
*	Revision;
*
*	11/18/02
*		- first revision.
*	09/02/03
*		- Giordano Sassaroli <sassarol@cefriel.it>
*		- Problem : The API is unable to receive responses from the Microsoft UPnP stack
*		- Error : the Microsoft UPnP stack is based on ISAPI on IIS, and whenever IIS
*                 receives a post request, it answers with two responses: the first one has no 
*		          body and it is a code 100 (continue) response, which has to be ignored. The
*		          second response is the actual one and should be parsed as the response.
*	02/09/04
*		- Ralf G. R. Bergs" <Ralf@Ber.gs>
*		- Why do you strip leading and trailing white space from the response body?
*		- Disabled to trim the content string.
*	03/11/04
*		- Added some methods about InputStream content.
*		  setContentInputStream(), getContentInputStream() and hasContentInputStream().
*	03/16/04
*		- Thanks for Darrell Young
*		- Added setVersion() and getVersion();
*	03/17/04
*		- Added hasFirstLine();
*	05/26/04
*		- Jan Newmarch <jan.newmarch@infotech.monash.edu.au> (05/26/04)
*		- Changed setCacheControl() and getChcheControl();
*	08/25/04
*		- Added the following methods.
*		  hasContentRange(), setContentRange(), getContentRange(), 
*		  getContentRangeFirstPosition(), getContentRangeLastPosition() and getContentRangeInstanceLength()
*	08/26/04
*		- Added the following methods.
*		  hasConnection(), setConnection(), getConnection(), 
*		  isCloseConnection() and isKeepAliveConnection()
*	08/27/04
*		- Added a updateWithContentLength paramger to setContent().
*		- Changed to HTTPPacket::set() not to change the header of Content-Length.
*	08/28/04
*		- Added init() and read().
*	09/19/04
*		- Added a onlyHeaders parameter to set().
*	10/20/04 
*		- Brent Hills <bhills@openshores.com>
*		- Changed hasContentRange() to check Content-Range and Range header.
*		- Added support for Range header to getContentRange().
*	02/02/05
*		- Mark Retallack <mretallack@users.sourceforge.net>
*		- Fixed set() not to read over the content length when the stream is keep alive.
*	02/28/05
*		- Added the following methods for chunked stream support.
*		  hasTransferEncoding(), setTransferEncoding(), getTransferEncoding(), isChunked().
*	03/02/05
*		- Changed post() to suppot chunked stream.
*	06/11/05
*		- Added setHost().
*	07/07/05
*		- Lee Peik Feng <pflee@users.sourceforge.net>
*		- Andrey Ovchar <AOvchar@consultitnow.com>
*		- Fixed set() to parse the chunk size as a hex string.
*	11/02/05
*		- Changed set() to use BufferedInputStream instead of BufferedReader to
*		  get the content as a byte stream.
*	11/06/05
*		- Added getCharSet().
*		- Changed getContentString() to return the content string using the charset.
*
*******************************************************************/

package com.xyoye.dandanplay.utils.smb.cybergarage.http;

import com.xyoye.dandanplay.utils.smb.cybergarage.net.HostInterface;
import com.xyoye.dandanplay.utils.smb.cybergarage.util.Debug;
import com.xyoye.dandanplay.utils.smb.cybergarage.util.StringUtil;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.Vector;


/** HTTPPacket 三大属性 1:请求行, 2:消息头, 3内容 */
public class HTTPPacket 
{
	private final String tag = "HTTPPacket";
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////
	/** 创建一个HTTPPacket 对象 设置version的值为1.1 */
	public HTTPPacket()
	{
		setVersion(HTTP.VERSION);
		setContentInputStream(null);
	}

	public HTTPPacket(HTTPPacket httpPacket)
	{
		setVersion(HTTP.VERSION);
		set(httpPacket);
		setContentInputStream(null);
	}

	public HTTPPacket(InputStream in)
	{
		setVersion(HTTP.VERSION);
		set(in);
		setContentInputStream(null);
	}

	////////////////////////////////////////////////
	//	init
	////////////////////////////////////////////////
	
	/** 读取时的初始化 */
	public void init()
	{
		//设置第一行的值为空字符串
		setFirstLine("");
		//清空httpHeaderList集合
		clearHeaders();
		//清空内容的节子数组
		setContent(new byte[0], false);
		setContentInputStream(null);
	}

	////////////////////////////////////////////////
	//	Version
	////////////////////////////////////////////////
	/** version http协议版本 */
	private String version;
	
	/** 设置version */
	public void setVersion(String ver)
	{
		version = ver;
	}
	
	/** 获取version */
	public String getVersion()
	{
		return version;
	}
		
	////////////////////////////////////////////////
	//	set
	////////////////////////////////////////////////
	
	/** 读取一行，返回该行的字符转 */
	private String readLine(BufferedInputStream in)
	{
		//创建一个ByteArrayOutputStream
		ByteArrayOutputStream lineBuf = new ByteArrayOutputStream();
		//创建一个byte 数组 
		byte readBuf[] = new byte[1];
		
 		try {
 			int	readLen = in.read(readBuf);
 			while (0 < readLen) {
 				//如果是换行符就结束循环
 				if (readBuf[0] == HTTP.LF){
 					break;
 				}
 				//如果不等于回车就写出
 				if (readBuf[0] != HTTP.CR){ 
 					lineBuf.write(readBuf[0]);
 				}
 	 			readLen = in.read(readBuf);
			}
 		}
 		catch (InterruptedIOException e) {
 			//Ignoring warning because it's a way to break the HTTP connecttion
 			//TODO Create a new level of Logging and log the event
		}
		catch (IOException e) {
			System.out.println("readLine Exception");
			Debug.warning(e);
		}

		return lineBuf.toString();
	}
	
	/** 读取数据的方法 */
	protected boolean set(InputStream in, boolean onlyHeaders)
	{
 		try {
 			//创建一个BufferedInputStream
 			BufferedInputStream reader = new BufferedInputStream(in);
 			Debug.message("setsetsetsetset = ");
 			//if(in.available() == 0)return false;
 			//读取第一行
			String firstLine = readLine(reader);
			if (firstLine == null || firstLine.length() <= 0){
				return false;
			}
			//设置第一行的值
			setFirstLine(firstLine);
			
			// Thanks for Giordano Sassaroli <sassarol@cefriel.it> (09/03/03)
			//创建一个HTTPStatus
	 
 
			HTTPStatus httpStatus = new HTTPStatus(firstLine);
			//获取状态码
			int statCode = httpStatus.getStatusCode();
			//状态码为100的操作 ,请求者应当继续提出请求。 服务器返回此代码表示已收到请求的第一部分，正在等待其余部分。
			if (statCode == HTTPStatus.CONTINUE){
				//ad hoc code for managing iis non-standard behaviour
				//iis sends 100 code response and a 200 code response in the same
				//stream, so the code should check the presence of the actual
				//response in the stream.
				//skip all header lines
				//专案管理的：IIS不规范行为的非法入境者的代码发送100码的响应和200代码在同一个流的反应，这样的代码应该检查是否存在实际的响应流中的。
				//跳过所有标题行
				//读取没个消息头
				String headerLine = readLine(reader);
				while ((headerLine != null) && (0 < headerLine.length()) ) {
					//创建消息头对象
					HTTPHeader header = new HTTPHeader(headerLine);
					if (header.hasName() == true){
						//设置httpHeaderList中的header的值
						setHeader(header);
					}
					headerLine = readLine(reader);
				}
				//look forward another first line
				//期待着另一个第一行
				String actualFirstLine = readLine(reader);
				if ((actualFirstLine != null) && (0 < actualFirstLine.length()) ) {
					//this is the actual first line
					//这是实际的第一行
					setFirstLine(actualFirstLine);
				}else{
					return true;
				}
			}
				
			//读取头
			String headerLine = readLine(reader);
			while ((headerLine != null) && (0 < headerLine.length()) ) {
				//创建一个HTTPHeader
				HTTPHeader header = new HTTPHeader(headerLine);
				if (header.hasName() == true){
					setHeader(header);
				}
				headerLine = readLine(reader);
			}
				
			if (onlyHeaders == true) {
				setContent("", false);
				return true;
			}
				
			boolean isChunkedRequest = isChunked();
				
			long contentLen = 0;
			if (isChunkedRequest == true) {
				try {
					String chunkSizeLine = readLine(reader);
					// Thanks for Lee Peik Feng <pflee@users.sourceforge.net> (07/07/05)
					//contentLen = Long.parseLong(new String(chunkSizeLine.getBytes(), 0, chunkSizeLine.length()-2), 16);
					contentLen = (chunkSizeLine != null) ? Long.parseLong(chunkSizeLine.trim(), 16) : 0;
				}
				catch (Exception e) {}
			}
			else{
				//获取文件的长度
				contentLen = getContentLength();
			}
			ByteArrayOutputStream contentBuf = new ByteArrayOutputStream();
			
			//这里是读取内容
			while (0 < contentLen) {
				//获取块大小
				int chunkSize = HTTP.getChunkSize();
				
				/* Thanks for Stephan Mehlhase (2010-10-26) */
				//判断byte数组的长度 内容长度大于块大小，则为块大小，否则为内容长度
				byte readBuf[] = new byte[(int) (contentLen > chunkSize ? chunkSize : contentLen)];
				
				long readCnt = 0;
				//读取内容写出到内存
				while (readCnt < contentLen) {
					try {
						// Thanks for Mark Retallack (02/02/05)
						long bufReadLen = contentLen - readCnt;
						if (chunkSize < bufReadLen){
							bufReadLen = chunkSize;
						}
						int readLen = reader.read(readBuf, 0, (int)bufReadLen);
						if (readLen < 0){
							break;
						}
						//写出到内存
						contentBuf.write(readBuf, 0, readLen);
						readCnt += readLen;
					}
					catch (Exception e)
					{
						Debug.warning(e);
						break;
					}
				}
				if (isChunkedRequest == true) {
					// skip CRLF
					// 跳过回车换行
					long skipLen = 0;
					do {
						long skipCnt = reader.skip(HTTP.CRLF.length() - skipLen);
						if (skipCnt < 0){
							break;
						}
						skipLen += skipCnt;
					} while (skipLen < HTTP.CRLF.length());
					// read next chunk size
					// 读取下一个数据块大小
					try {
						String chunkSizeLine = readLine(reader);
						// Thanks for Lee Peik Feng <pflee@users.sourceforge.net> (07/07/05)
						contentLen = Long.parseLong(new String(chunkSizeLine.getBytes(), 0, chunkSizeLine.length()-2), 16);
					}
					catch (Exception e) {
						contentLen = 0;
					}
				}
				else{
					contentLen = 0;
				}
			}

			setContent(contentBuf.toByteArray(), false);
 		}
		catch (Exception e) {
			Debug.warning(e);
			return false;
		}
		
		return true;
	}

	/** 调用set(InputStream in, boolean onlyHeaders) */
	protected boolean set(InputStream in)
	{
		return set(in, false);
	}
	
	/** 调用set(InputStream in) */
	protected boolean set(HTTPSocket httpSock)
	{
		return set(httpSock.getInputStream());
	}

	/** 设置本类的内容，根据参数的内容赋值给本类的内容 */
	protected void set(HTTPPacket httpPacket)
	{
		setFirstLine(httpPacket.getFirstLine());
		
		clearHeaders();
		int nHeaders = httpPacket.getNHeaders();
		for (int n=0; n<nHeaders; n++) {
			HTTPHeader header = httpPacket.getHeader(n);
			addHeader(header);
		}
		setContent(httpPacket.getContent());
	}

	////////////////////////////////////////////////
	//	read
	////////////////////////////////////////////////
	
	public boolean read(HTTPSocket httpSock)
	{
		init();
		return set(httpSock);
	}
	
	////////////////////////////////////////////////
	//	String
	////////////////////////////////////////////////

	/** firstLine 第一行  */
	private String firstLine = "";
 	
	/** 设置firstLine 的值 */
	private void setFirstLine(String value)
	{
			firstLine = value;
	}
	
	/** 获取firstLine 的值 */
	protected String getFirstLine()
	{
		return firstLine;
	}

	protected String getFirstLineToken(int num)
	{
		StringTokenizer st = new StringTokenizer(firstLine, HTTP.REQEST_LINE_DELIM);
		String lastToken = "";
		for (int n=0; n<=num; n++) {
			if (st.hasMoreTokens() == false){
				return "";
			}
			lastToken = st.nextToken();
		}
		return lastToken;
     }
	
	/** 判断firstLine的长度，如果大于0就返回true，否则返回false */
	public boolean hasFirstLine()
	{
		return 0 < firstLine.length();
	}
	
	////////////////////////////////////////////////
	//	Header
	////////////////////////////////////////////////

	/** httpHeaderList 集合保存HTTPHeader对象 */
	private Vector httpHeaderList = new Vector();
	
	/** 获取httpHeaderList 的 httpHeader的总数*/
	public int getNHeaders()
	{
		return httpHeaderList.size();
	}

	/** httpHeaderList中 添加 HTTPHeader头对象*/
	public void addHeader(HTTPHeader header)
	{
		httpHeaderList.add(header);
	}

	/** 创建HTTPHeader对象添加到httpHeaderList集合 */
	public void addHeader(String name, String value)
	{
		HTTPHeader header = new HTTPHeader(name, value);
		httpHeaderList.add(header);
	}

	/** 根据索引获取httpHeaderList的 元素，返回HTTPHeader*/
	public HTTPHeader getHeader(int n)
	{
		return (HTTPHeader)httpHeaderList.get(n);
	}
	
	/** 根据名字获取httpHeaderList的元素，如果与 httpHeaderList元素中的HTTPHeader的name字段相同,不区分大小写则返回HTTPHeader
	 *  否则返回false
	 */
	public HTTPHeader getHeader(String name)
	{
		int nHeaders = getNHeaders();
		for (int n=0; n<nHeaders; n++) {
			HTTPHeader header = getHeader(n);
			String headerName = header.getName();
			if (headerName.equalsIgnoreCase(name) == true){
				return header;			
			}
		}
		return null;
	}

	/** 清空httpHeaderList集合 */
	public void clearHeaders()
	{
		httpHeaderList.clear();
		httpHeaderList = new Vector();
	}
	
	/** 判断名字为name的值的消息头是否存在 有此消息头返回true，否则返回false */
	public boolean hasHeader(String name)
	{
		return getHeader(name) != null;
	}

	/** 如果httpHeaderList中有HTTPHeader的名字与name相同则设置value的值
	 *否则创建一个新的HTTPHeader 添加到httpHeaderList中
	 * 
	 * @param name 获取HTTPHeader，或设置HTTPHeader的name字段的值
	 * @param value 设置HTTPHeader 的值
	 *
	 **/
	public void setHeader(String name, String value)
	{
		HTTPHeader header = getHeader(name);
		if (header != null) {
			header.setValue(value);
			return;
		}
		addHeader(name, value);
	}

	public void setHeader(String name, int value)
	{
		setHeader(name, Integer.toString(value));
	}

	public void setHeader(String name, long value)
	{
		setHeader(name, Long.toString(value));
	}
	
	/** 如果httpHeaderList集合中包含 header 此消息头，则修改该值，否则添加到httpHeaderList中*/
	public void setHeader(HTTPHeader header)
	{
		setHeader(header.getName(), header.getValue());
	}

	/** 根据name获取HTTPHeader ，如果HTTPHeader为null返回空字符串，否则返回HTTPHeader的value的值 */
	public String getHeaderValue(String name)
	{
		HTTPHeader header = getHeader(name);
		if (header == null){
			return "";
		}
		return header.getValue();
	}

	////////////////////////////////////////////////
	// set*Value
	////////////////////////////////////////////////

	/** 设置头的值，判断值是否有前缀或后缀，没有就添加前缀或后缀 */
	public void setStringHeader(String name, String value, String startWidth, String endWidth)
	{
		String headerValue = value;
		if (headerValue.startsWith(startWidth) == false){
			headerValue = startWidth + headerValue;
		}
		if (headerValue.endsWith(endWidth) == false){
			headerValue = headerValue + endWidth;
		}
		setHeader(name, headerValue);
	}

	/** 设置消息头,消息头的值前缀是\后缀也是\
	 * @param name 消息头的名字
	 * @param value 消息头的值
	 *  
	 *  */
	public void setStringHeader(String name, String value)
	{
		setStringHeader(name, value, "\"", "\"");
	}
	
	/** 获取头的值，返回是没有前缀和后缀的 */
	public String getStringHeaderValue(String name, String startWidth, String endWidth)
	{
		String headerValue = getHeaderValue(name);
		if (headerValue.startsWith(startWidth) == true){
			headerValue = headerValue.substring(1);
		}
		if (headerValue.endsWith(endWidth) == true){
			headerValue = headerValue.substring(0, headerValue.length()-1);
		}
		return headerValue;
	}
	
	/** 获取头的值，返回是没有前缀 \ 和 后缀 \ 的  */
	public String getStringHeaderValue(String name)
	{
		return getStringHeaderValue(name, "\"", "\"");
	}

	public void setIntegerHeader(String name, int value)
	{
		setHeader(name, Integer.toString(value));
	}
	
	/** 设置 */
	public void setLongHeader(String name, long value)
	{
		setHeader(name, Long.toString(value));
	}
	
	public int getIntegerHeaderValue(String name)
	{
		HTTPHeader header = getHeader(name);
		if (header == null)
			return 0;
		return StringUtil.toInteger(header.getValue());
	}

	/** 根据名字获取HTTPHeader ,将HTTPHeader的value的值转换为long型 */
	public long getLongHeaderValue(String name)
	{
		//获取HTTPHeader
		HTTPHeader header = getHeader(name);
		if (header == null){
			return 0;
		}
		return StringUtil.toLong(header.getValue());
	}

	////////////////////////////////////////////////
	//	getHeader
	////////////////////////////////////////////////
	
	/** 获取消息头 */
	public String getHeaderString()
	{
		StringBuffer str = new StringBuffer();
	
		int nHeaders = getNHeaders();
		for (int n=0; n<nHeaders; n++) {
			HTTPHeader header = getHeader(n);
			str.append(header.getName() + ": " + header.getValue() + HTTP.CRLF);
		}
		
		return str.toString();
	}

	////////////////////////////////////////////////
	//	Contents
	////////////////////////////////////////////////

	/** 内容的字节 */
	private byte content[] = new byte[0];
	
	/** 设置 content的值 如果updateWithContentLength 为true添加Content-Length 消息头到httpHeaderList集合中 */
	public void setContent(byte data[], boolean updateWithContentLength)
	{
		content = data;
		if (updateWithContentLength == true){
			setContentLength(data.length);
		}
	}

	public void setContent(byte data[])
	{
		setContent(data, true);
	}
	
	/** 调用setContent(byte data[], boolean updateWithContentLength)
	 * data 为data.getBytes()
	 * updateWithContentLength为updateWithContentLength
	 */
	public void setContent(String data, boolean updateWithContentLength)
	{
		setContent(data.getBytes(), updateWithContentLength);
	}

	/** 
	 * 设置 Content-Length 头的值
	 * 调用 setContent(String data, boolean updateWithContentLength)
	 * updateWithContentLength 为true
	 */
	public void setContent(String data)
	{
		setContent(data, true);
	}
	
	/** 获取内容的字节 */
	public  byte []getContent()
	{
		return content;
	}

	/** 获取内容字符串 */
	public String getContentString()
	{
		//获取字符集
		String charSet = getCharSet();
		if (charSet == null || charSet.length() <= 0){
			return new String(content);
		}
		try {
			return new String(content, charSet);
		}
		catch (Exception e) {
			Debug.warning(e);
		}
		return new String(content);
	}
	
	public boolean hasContent()
	{
		return content.length > 0;
	}

	////////////////////////////////////////////////
	//	Contents (InputStream)
	////////////////////////////////////////////////

	/** 内容的输入流 */
	private InputStream contentInput = null;
	
	/** 设置内容的输入流 InputStream contentInput */
	public void setContentInputStream(InputStream in)
	{
		contentInput = in;
	}

	/** 获取 内容的输入流 InputStream contentInput*/
	public InputStream getContentInputStream()
	{
		return contentInput;
	}

	/** 判断 contentInput 是否为null，不为null返回true，为null返回false*/
	public boolean hasContentInputStream()
	{
		return contentInput != null;
	}

	////////////////////////////////////////////////
	//	ContentType
	////////////////////////////////////////////////

	/** 设置HTTPHeader 的name为 Content-Type 值为type的值*/
	public void setContentType(String type)
	{
		setHeader(HTTP.CONTENT_TYPE, type);
	}

	/** 获取消息头  Content-Type 的值 */
	public String getContentType()
	{
		return getHeaderValue(HTTP.CONTENT_TYPE);
	}

	////////////////////////////////////////////////
	//	Charset
	////////////////////////////////////////////////

	/** 获取字符集 */
	public String getCharSet()
	{
		//获取Content-Type 的值
		String contentType = getContentType();
		if (contentType == null){
			return "";
		}
		//改成小写
		contentType = contentType.toLowerCase();
		int charSetIdx = contentType.indexOf(HTTP.CHARSET);
		if (charSetIdx < 0){
			return "";
		}
		int charSetEndIdx = charSetIdx + HTTP.CHARSET.length() + 1; 
		//获取charSet的值
		String charSet = new String(contentType.getBytes(), charSetEndIdx, (contentType.length() - charSetEndIdx));
		if (charSet.length() < 0){
			return "";
		}
		if (charSet.charAt(0) == '\"'){
			charSet = charSet.substring(1, (charSet.length() - 1));
		}
		if (charSet.length() < 0){
			return "";
		}
		if (charSet.charAt((charSet.length()-1)) == '\"'){
			charSet = charSet.substring(0, (charSet.length() - 1));
		}
		return charSet;
	}

	////////////////////////////////////////////////
	//	ContentLength
	////////////////////////////////////////////////

	
	/** 设置HTTPHeader 的name为 Content-Length 值为len的值*/
	public void setContentLength(long len)
	{
		setLongHeader(HTTP.CONTENT_LENGTH, len);
	}

	/**  获取消息头Content-Length的值，返回一个long型 */
	public long getContentLength()
	{
		return getLongHeaderValue(HTTP.CONTENT_LENGTH);
	}

	////////////////////////////////////////////////
	//	Connection
	////////////////////////////////////////////////

	/** 如果有Connection头返回true，否则返回false */
	public boolean hasConnection()
	{
		return hasHeader(HTTP.CONNECTION);
	}

	/** 设置HTTPHeader 的name为Connection value为value的值
	 * 	设置是否保存连接 
	 */
	public void setConnection(String value)
	{
		setHeader(HTTP.CONNECTION, value);
	}

	/** 获取Connection头的值 */
	public String getConnection()
	{
		return getHeaderValue(HTTP.CONNECTION);
	}

	public boolean isCloseConnection()
	{	
		if (hasConnection() == false)
			return false;
		String connection = getConnection();
		if (connection == null)
			return false;
		return connection.equalsIgnoreCase(HTTP.CLOSE);
	}

	public boolean isKeepAliveConnection()
	{	
		if (hasConnection() == false)
			return false;
		String connection = getConnection();
		if (connection == null)
			return false;
		return connection.equalsIgnoreCase(HTTP.KEEP_ALIVE);
	}
	
	////////////////////////////////////////////////
	//	ContentRange
	////////////////////////////////////////////////

	/** Content-Range || Range 存在就返回true */
	public boolean hasContentRange()
	{
		return (hasHeader(HTTP.CONTENT_RANGE) || hasHeader(HTTP.RANGE));
	}
	
	/** 
	 * 设置ContentRange 例如这种格式:Content-Range: bytes 0-800/801
	 * @param firstPos 首位置例如0
	 * @param lastPos  最后的位置例如800
	 * @param length   内容的总长度 例如801
	 */ 
	public void setContentRange(long firstPos, long lastPos, long length)
	{
		String rangeStr = "";
		rangeStr += HTTP.CONTENT_RANGE_BYTES + " ";
		rangeStr += Long.toString(firstPos) + "-";
		rangeStr += Long.toString(lastPos) + "/";
		rangeStr += ((0 < length) ? Long.toString(length) : "*");
		setHeader(HTTP.CONTENT_RANGE, rangeStr);
	}

	/** 获取Range 返回  long[]*/
	public long[] getContentRange()
	{
		long range[] = new long[3];
		range[0] = range[1] = range[2] = 0;
		if (hasContentRange() == false){
			return range;
		}
		//获取Content-Range的值
		String rangeLine = getHeaderValue(HTTP.CONTENT_RANGE);
		// Thanks for Brent Hills (10/20/04)
		if (rangeLine.length() <= 0){
			//获取Range的值
			rangeLine = getHeaderValue(HTTP.RANGE);
		}
		if (rangeLine.length() <= 0){
			return range;
		}
		
		try
		{
			String str[] = rangeLine.split("[ =\\-/]");
			
			if(2 <= str.length){
				range[0] = Long.parseLong(str[1]);
			}
			
			if(3 <= str.length){
				range[1] = Long.parseLong(str[2]);
			}
			
			if(4 <= str.length){
				range[2] = Long.parseLong(str[3]);
			}
		}
		catch (NumberFormatException e)
		{
			e.printStackTrace();
		}
		return range;
//		// Thanks for Brent Hills (10/20/04)
//		StringTokenizer strToken = new StringTokenizer(rangeLine, " =");
//		Debug.message("strToken");
//		// Skip bytes
//		if (strToken.hasMoreTokens() == false){
//			return range;
//		}
//		String bytesStr = strToken.nextToken(" ");
//		// Get first-byte-pos
//		if (strToken.hasMoreTokens() == false)
//			return range;
//		String firstPosStr = strToken.nextToken(" -");
//		Debug.message("firstPosStr"+firstPosStr);
//		try {
//			range[0] = Long.parseLong(firstPosStr);
//		}
//		catch (NumberFormatException e) {};
//		if (strToken.hasMoreTokens() == false)
//			return range;
//		String lastPosStr = strToken.nextToken("-/");
//		Debug.message("lastPosStr"+lastPosStr);
//		try {
//			range[1] = Long.parseLong(lastPosStr);
//		}
//		catch (NumberFormatException e) {};
//		if (strToken.hasMoreTokens() == false)
//			return range;
//		String lengthStr = strToken.nextToken("/");
//		try {
//			range[2] = Long.parseLong(lengthStr);
//		}
//		catch (NumberFormatException e) {};
//		return range;
	}
	
	/** 获取首位置 */
	public long getContentRangeFirstPosition()
	{
		long range[] = getContentRange();
		return range[0];
	}

	/** 获取最后位置 */
	public long getContentRangeLastPosition()
	{
		long range[] = getContentRange();
		return range[1];
	}

	public long getContentRangeInstanceLength()
	{
		long range[] = getContentRange();
		return range[2];
	}
	
	////////////////////////////////////////////////
	//	CacheControl
	////////////////////////////////////////////////

	public void setCacheControl(String directive)
	{
		setHeader(HTTP.CACHE_CONTROL, directive);
	}
	
	public void setCacheControl(String directive, int value)
	{
		String strVal = directive + "=" + Integer.toString(value);
		setHeader(HTTP.CACHE_CONTROL, strVal);
	}
	
	public void setCacheControl(int value)
	{
		setCacheControl(HTTP.MAX_AGE, value);
	}

	public String getCacheControl()
	{
		return getHeaderValue(HTTP.CACHE_CONTROL);
	}

	////////////////////////////////////////////////
	//	Server
	////////////////////////////////////////////////

	/** 设置HTTPHeader 的name为 Server 值为name的值*/
	public void setServer(String name)
	{
		setHeader(HTTP.SERVER, name);
	}

	public String getServer()
	{
		return getHeaderValue(HTTP.SERVER);
	}

	////////////////////////////////////////////////
	//	Host
	////////////////////////////////////////////////

	/**
	 * 设置HOST头的值
	 * 设置HTTPHeader的name和value的值 name 的值为HOST ，value的值为 hostAddr + ":" + Integer.toString(port)
	 * @param host 主机的地址
	 * @param port 端口
	 * 
	 */
	public void setHost(String host, int port)
	{
		String hostAddr = host;
		if (HostInterface.isIPv6Address(host) == true){
			hostAddr = "[" + host + "]";
		}
		setHeader(HTTP.HOST, hostAddr + ":" + Integer.toString(port));
	}

	/** 设置主机的Host */
	public void setHost(String host)
	{
		String hostAddr = host;
		if (HostInterface.isIPv6Address(host) == true){
			hostAddr = "[" + host + "]";
		}
		setHeader(HTTP.HOST, hostAddr);
	}
	
	public String getHost()
	{
		return getHeaderValue(HTTP.HOST);
	}


	////////////////////////////////////////////////
	//	Date
	////////////////////////////////////////////////

	
	/** 设置Date 的消息头*/
	public void setDate(Calendar cal)
	{
		Date date = new Date(cal);
		setHeader(HTTP.DATE, date.getDateString());
	}

	/** 获取日期消息头的值 */
	public String getDate()
	{
		return getHeaderValue(HTTP.DATE);
	}

	////////////////////////////////////////////////
	//	Connection
	////////////////////////////////////////////////
	/** Transfer-Encoding 判断是否有此消息头，有返回true，没有返回false */
	public boolean hasTransferEncoding()
	{
		return hasHeader(HTTP.TRANSFER_ENCODING);
	}

	public void setTransferEncoding(String value)
	{
		setHeader(HTTP.TRANSFER_ENCODING, value);
	}

	/** 获取Transfer-Encoding 对应的值 */
	public String getTransferEncoding()
	{
		return getHeaderValue(HTTP.TRANSFER_ENCODING);
	}

	/** 如果没有 Transfer-Encoding 消息头返回false
	 * 如果有获取Transfer-Encoding 消息头对象的值，如果与Chunked相同,不区分大小写，就返回true，否则返回false
	 */
	public boolean isChunked()
	{	
		if (hasTransferEncoding() == false){
			return false;
		}
		String transEnc = getTransferEncoding();
		if (transEnc == null){
			return false;
		}
		return transEnc.equalsIgnoreCase(HTTP.CHUNKED);
	}
	
	////////////////////////////////////////////////
	//	set
	////////////////////////////////////////////////

/*
	public final static boolean parse(HTTPPacket httpPacket, InputStream in)
	{
 		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			return parse(httpPacket, reader);
		}
		catch (Exception e) {
			Debug.warning(e);
		}
		return false;
	}
*/
}

