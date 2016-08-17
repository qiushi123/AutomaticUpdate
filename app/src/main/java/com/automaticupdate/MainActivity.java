package com.automaticupdate;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.lzy.okhttpserver.download.DownloadInfo;
import com.lzy.okhttpserver.download.DownloadManager;
import com.lzy.okhttpserver.download.DownloadService;
import com.lzy.okhttpserver.listener.DownloadListener;

import java.io.File;

public class MainActivity extends Activity {
    //    下载相关
    @ViewInject(R.id.downloadSize)
    private TextView downloadSize;
    @ViewInject(R.id.tvProgress)
    private TextView tvProgress;
    @ViewInject(R.id.netSpeed)
    private TextView netSpeed;
    @ViewInject(R.id.pbProgress)
    private ProgressBar pbProgress;
    @ViewInject(R.id.start)
    private Button download;

    //    更新提示框相关
    @ViewInject(R.id.notifyLayout)
    private RelativeLayout notifyLayout;

    @ViewInject(R.id.downloadLayout)
    private RelativeLayout downloadLayout;


    private MyListener listener;
    private DownloadInfo downloadInfo;
    private String apkUrl = "http://download.apk8.com/d2/soft/meilijia.apk";//这里是你的apk下载地址
    private DownloadManager downloadManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        downloadManager = DownloadService.getDownloadManager(this);//创建下载文件的服务
        ViewUtils.inject(this);
        listener = new MyListener();
        downloadInfo = downloadManager.getTaskByUrl(apkUrl);
        if (downloadInfo != null) {
            //如果任务存在，把任务的监听换成当前页面需要的监听
            downloadInfo.setListener(listener);
            //需要第一次手动刷一次，因为任务可能处于下载完成，暂停，等待状态，此时是不会回调进度方法的

            refreshUi(downloadInfo);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (downloadInfo != null) {
            refreshUi(downloadInfo);

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (downloadInfo != null) {
            downloadInfo.removeListener();
        }
        if (manager != null) {
            manager.cancelAll();
        }
    }


    @OnClick({R.id.quit_text, R.id.sure_text, R.id.start})
    public void mOnClick(View v) {
        switch (v.getId()) {
            case R.id.quit_text:
                finish();
                break;
            case R.id.sure_text:
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
                break;
            case R.id.start:
                if (manager != null) {
                    manager.cancelAll();
                }
                initNotify();
                downloadInfo = downloadManager.getTaskByUrl(apkUrl);
                if (downloadInfo == null) {
                    downloadManager.addTask(apkUrl, listener);
                    return;
                }
                switch (downloadInfo.getState()) {
                    case DownloadManager.PAUSE:
                    case DownloadManager.NONE:
                        downloadManager.addTask(downloadInfo.getUrl(), listener);
                        break;
                    case DownloadManager.ERROR:
                        if (downloadInfo == null) {
                            downloadManager.addTask(apkUrl, listener);
                            return;
                        }
                        downloadManager.removeTask(downloadInfo.getUrl());
                        downloadSize.setText("--M/--M");
                        netSpeed.setText("---/s");
                        tvProgress.setText("--.--%");
                        pbProgress.setProgress(0);
                        download.setText("下载中");
                        downloadManager.addTask(apkUrl, listener);
                        break;
                    case DownloadManager.DOWNLOADING:
                        downloadManager.pauseTask(downloadInfo.getUrl());
                        break;
                    case DownloadManager.FINISH:
                        break;
                }
        }
    }

    private class MyListener extends DownloadListener {

        @Override
        public void onProgress(DownloadInfo downloadInfo) {
            refreshUi(downloadInfo);
        }

        @Override
        public void onFinish(DownloadInfo downloadInfo) {
            Toast.makeText(MainActivity.this, "下载完成", Toast.LENGTH_SHORT).show();
            //            if (!ApkUtils.isAvailable(ApkUpdataActivity.this, new File(downloadInfo.getTargetPath()))) {
            ApkUtils.install(MainActivity.this, new File(downloadInfo.getTargetPath()));
            finish();
            //            }
        }

        @Override
        public void onError(DownloadInfo downloadInfo, String errorMsg, Exception e) {
            if (errorMsg != null)
                Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshUi(DownloadInfo downloadInfo) {
        notifyLayout.setVisibility(View.GONE);
        downloadLayout.setVisibility(View.VISIBLE);
        String downloadLength = Formatter.formatFileSize(MainActivity.this, downloadInfo.getDownloadLength
                ());
        String totalLength = Formatter.formatFileSize(MainActivity.this, downloadInfo.getTotalLength());
        downloadSize.setText(downloadLength + "/" + totalLength);
        String networkSpeed = Formatter.formatFileSize(MainActivity.this, downloadInfo.getNetworkSpeed());
        netSpeed.setText(networkSpeed + "/s");
        tvProgress.setText((Math.round(downloadInfo.getProgress() * 10000) * 1.0f / 100) + "%");
        pbProgress.setMax((int) downloadInfo.getTotalLength());
        pbProgress.setProgress((int) downloadInfo.getDownloadLength());
        len = (int) (Math.round(downloadInfo.getProgress() * 10000) * 1.0f / 100);
        Message msg = new Message();
        msg.what = 0;
        msg.obj = len;
        handler.sendMessage(msg);
        if (len == 100) {
            handler.sendEmptyMessage(1);
        }
        switch (downloadInfo.getState()) {
            case DownloadManager.NONE:
                download.setText("点击下载");
                break;
            case DownloadManager.DOWNLOADING:
                download.setText("点击暂停");
                break;
            case DownloadManager.PAUSE:
                download.setText("点击继续");
                break;
            case DownloadManager.WAITING:
                download.setText("等待");
                break;
            case DownloadManager.ERROR:
                download.setText("下载出错,点击重新下载");
                break;
            case DownloadManager.FINISH:
                break;
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (notif != null && manager != null) {
                        notif.contentView.setTextViewText(R.id.content_view_text1, len + "%");
                        notif.contentView.setProgressBar(R.id.content_view_progress, 100, len, false);
                        manager.notify(0, notif);
                    }
                    break;
                case 1:
                    if (manager != null) {
                        manager.cancelAll();
                    }
                    break;
                default:
                    break;
            }
        }

    };

    private int len;
    private NotificationManager manager;
    private Notification notif;

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {

            //            new DownLoadThread().start();
            initNotify();
            finish();

        }
        return super.onKeyDown(keyCode, event);
    }

    private void initNotify() {
        if (notif != null) {
            return;
        }
        //点击通知栏后打开的activity
        //        Intent intent = new Intent(getApplicationContext(), ApkUpdataActivity.class);
        //        PendingIntent pIntent = PendingIntent.getActivity(ApkUpdataActivity.this, 0, intent, 0);
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notif = new Notification();
        notif.icon = R.mipmap.ic_launcher;
        notif.tickerText = "正在下载";
        //通知栏显示所用到的布局文件
        notif.contentView = new RemoteViews(getPackageName(), R.layout.notification_apk_updata);
        //        notif.contentIntent = pIntent;
        manager.notify(0, notif);


    }

}
