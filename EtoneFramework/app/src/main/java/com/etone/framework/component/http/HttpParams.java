package com.etone.framework.component.http;

import com.etone.framework.event.EventData;
import com.etone.framework.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class HttpParams implements EventData {
	private static final int DEFAULT_TIMEOUT = 1000 * 10;
	private static final int DEFAULT_BUFFER_SIZE = 4096;
	private static final String DEFAULT_CHARSET = "UTF-8";
	private static final String DEFAULT_COOKIE_KEY = "sid";
	private static final Accept DEFAULT_ACCEPT = Accept.JSON;
	private static final Content DEFAULT_CONTENT = Content.URLENCODED;

	private final String httpEvent;

	public String url;
	public HttpMethod method;
	public HttpAuthCookie cookieToGet;
	public HttpAuthCookie cookieToSend;
	public Accept accept;
	public Content contentType;
	public int timeOut;
	public String charset;
	public int bufferSize;
	public String cookieKey;

	private HashMap<String, String> header;
	private HashMap<String, String> params;
	private HashMap<String, Object> json;
	/*如果一组请求始终都需要发送一组参数的话，那么可以在这里进行设置*/
	public HashMap<String, String> alwaysSend;
	private HashMap<String, Object> userDef;

	/*这里和文件上传相关*/
	private ArrayList<FileUploader> files;
	private UploadListener uploadListener;

	public String content;
	public int responseCode;

	public HttpParams(String httpEvent, String url) {
		this.httpEvent = httpEvent;
		this.url = url;
		this.method = HttpMethod.GET;

		/* 这些默认值在以后可以进行设置，
		 * 如果有设置应用的全局变量，那么以全局变量为准,
		 * 用户可以在new过这个对象以后，再去进行自定义的设置
		 * */
		this.timeOut = HttpUtils.HTTP_TIME_OUT == -1 ? DEFAULT_TIMEOUT : HttpUtils.HTTP_TIME_OUT;
		this.bufferSize = HttpUtils.HTTP_BUFF_SIZE == -1 ? DEFAULT_BUFFER_SIZE : HttpUtils.HTTP_BUFF_SIZE;
		this.charset = StringUtils.isEmpty(HttpUtils.HTTP_CHARSET) ? DEFAULT_CHARSET : HttpUtils.HTTP_CHARSET;
		this.cookieKey = StringUtils.isEmpty(HttpUtils.HTTP_COOKIE_KEY) ? DEFAULT_COOKIE_KEY : HttpUtils.HTTP_COOKIE_KEY;
		this.accept = HttpUtils.HTTP_ACCEPT == null ? DEFAULT_ACCEPT : HttpUtils.HTTP_ACCEPT;
		this.contentType = HttpUtils.HTTP_CONTENT == null ? DEFAULT_CONTENT : HttpUtils.HTTP_CONTENT;

		/* 每一个请求可以设置不同,如果有一个请求不需要发送的话，那么可以在new出来之后把这个map清空
		   这里目前不支持json等格式 */
		this.alwaysSend = new HashMap<>(HttpUtils.alwaysSend);
	}

	public void setUploadListener(UploadListener uploadListener) {
		this.uploadListener = uploadListener;
	}

	public UploadListener getUploadListener() {
		return this.uploadListener;
	}

	public String getHttpEvent() {
		return httpEvent;
	}

	public void addParams(String key, String value) {
		if (params == null)
			params = new HashMap<>();

		params.put(key, value);
	}

	public void addJsonParams(String key, Object value) {
		if (json == null)
			json = new HashMap<>();

		json.put(key, value);
	}

	public void addHeader(String key, String value) {
		if (header == null)
			header = new HashMap<>();

		header.put(key, value);
	}

	public void putUserDefine(String key, Object value) {
		if (userDef == null)
			userDef = new HashMap<>();

		userDef.put(key, value);
	}

	public void addFile(String key, String fileName, File file) {
		if (StringUtils.isEmpty(key) || StringUtils.isEmpty(fileName) || file == null || !file.exists())
			return;

		if (files == null)
			files = new ArrayList<>();
		files.add(new FileUploader(key, fileName, file));
	}

	public ArrayList<FileUploader> getFiles()
	{
		return files;
	}
	
	public Object getUserDefine(String key)
	{
		if (userDef == null || !userDef.containsKey(key))
			return null;
		
		return userDef.get(key);
	}
	
	public HashMap<String, String> getHeader()
	{
		return this.header;
	}
	
	public HashMap<String, String> getParams()
	{
		return this.params;
	}

	public HashMap<String, Object> getJsonParams()
	{
		return this.json;
	}
	
	public HashMap<String, Object> getUserDef()
	{
		return this.userDef;
	}

	public enum Accept
	{
		XML,
		JSON,
		URLENCODED
	}

	public enum Content
	{
		HTML,
		JSON,
		URLENCODED
	}

	public static class FileUploader
	{
		public String key;
		public String fileName;
		public File file;

		public FileUploader(String key, String fileName, File file)
		{
			this.key = key;
			this.fileName = fileName;
			this.file = file;
		}
	}

	public interface UploadListener
	{
		void onProgress(FileUploader loader, long pro, double percent);
		void onFinish(FileUploader loader);
		void onAllFinish();
	}
}