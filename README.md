# AutomaticUpdate
安卓自动更新，静默更新，可以在通知栏里显示更新下载进度，几行代码快速实现Android下载更新

之前用的友盟更新，但是友盟最近自动跟新业务要停止服务了，所以就自己写了这套自动更新的代码
使用起来特别简单，几行代码就可以快速集成

##先看效果图


1，进入应用更新弹窗，这个可以自定义（详看代码）

![image](https://github.com/qiushi123/AutomaticUpdate/blob/master/images/1.png?raw=true)

2，可以在应用中查看下载进度，也可以暂停下载

![image](https://github.com/qiushi123/AutomaticUpdate/blob/master/images/2.png?raw=true)
![image](https://github.com/qiushi123/AutomaticUpdate/blob/master/images/3.png?raw=true)

3，下载的通知也会在通知栏里显示

![image](https://github.com/qiushi123/AutomaticUpdate/blob/master/images/4.png?raw=true)

4，下载完成以后可以自动安装

![image](https://github.com/qiushi123/AutomaticUpdate/blob/master/images/5.png?raw=true)


#使用步骤

##一，添加类库

	compile 'com.lzy.net:okhttputils:1.6.7'
	compile 'com.lzy.net:okhttpserver:0.1.7' //扩展了下载管理和上传管理，根据需要添加
  
##二，全局配置
一般在 Aplication，或者基类中，只需要调用一次即可，可以配置调试开关，全局的超时时间，公共的请求头和请求参数等信息

	public class GApp extends Application {
	    @Override
	    public void onCreate() {
	        super.onCreate();
	        HttpHeaders headers = new HttpHeaders();
	        headers.put("commonHeaderKey1", "commonHeaderValue1");    //所有的 header 都 不支持 中文
	        headers.put("commonHeaderKey2", "commonHeaderValue2");
	        HttpParams params = new HttpParams();
	        params.put("commonParamsKey1", "commonParamsValue1");     //所有的 params 都 支持 中文
	        params.put("commonParamsKey2", "这里支持中文参数");
	
	        //必须调用初始化
	        OkHttpUtils.init(this);
	        //以下都不是必须的，根据需要自行选择
	        OkHttpUtils.getInstance()//
	                .debug("OkHttpUtils")                                              //是否打开调试
	                .setConnectTimeout(OkHttpUtils.DEFAULT_MILLISECONDS)               //全局的连接超时时间
	                .setReadTimeOut(OkHttpUtils.DEFAULT_MILLISECONDS)                  //全局的读取超时时间
	                .setWriteTimeOut(OkHttpUtils.DEFAULT_MILLISECONDS)                 //全局的写入超时时间
			//.setCookieStore(new MemoryCookieStore()) //cookie使用内存缓存（app退出后，cookie消失）
	                .setCookieStore(new PersistentCookieStore())                       //cookie持久化存储，如果cookie不过期，则一直有效
	                .addCommonHeaders(headers)                                         //设置全局公共头
	                .addCommonParams(params);                                          //设置全局公共参数
	    }
	} 
	  
  
##三，实现下载更新

这里只贴出部分代码，详细代码可以查看项目里的MainActivity类


	initNotify();
	downloadInfo = downloadManager.getTaskByUrl(apkUrl);
	notifyLayout.setVisibility(View.GONE);
	downloadLayout.setVisibility(View.VISIBLE);
	if (downloadInfo == null) {
		downloadManager.addTask(apkUrl, listener);
	} else {
		downloadManager.removeTask(downloadInfo.getUrl());
		downloadSize.setText("--M/--M");
		netSpeed.setText("---/s");
		tvProgress.setText("--.--%");
		pbProgress.setProgress(0);
		download.setText("下载中");
		downloadManager.addTask(apkUrl, listener);
	}

  
博客地址： 
  
  
  
  
  
  
  
  
  
  
  
