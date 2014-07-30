package me.drakeet.seashell.service;

import java.util.Date;
import java.util.Map;
import java.util.Random;

import com.google.gson.Gson;

import me.drakeet.seashell.ui.MainActivity;
import me.drakeet.seashell.utils.HttpDownloader;
import me.drakeet.seashell.utils.MySharedpreference;
import me.drakeet.seashell.R;
import me.drakeet.seashell.model.Word;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;


public class NotificatService extends Service {

    private Word mWord;

    public static boolean isRun = false;
    static long firstTime;
    Thread thread;
    private volatile boolean stopRequested;
    boolean isFirst2 = true;
    private int NOTIFY_ID = 524947901;
    private String mTodayGsonString;
    private String mYesterdayGsonString;
    private LocalBinder localBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent arg0) {
        return localBinder;
    }

    public class LocalBinder extends Binder {
        public NotificatService getService() {
            return NotificatService.this;
        }

        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply,
                                     int flags) throws RemoteException {
            //表示从activity中获取数值
            if (data.readInt() == 199) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startN();
                    }
                }).start();
                reply.writeInt(200);
            }
            return super.onTransact(code, data, reply, flags);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                Date date = new Date();

                while (stopRequested == false) {
                    if (isFirst2) {
                        firstTime = date.getDate();
                        startN();
                        isFirst2 = false;
                    }
                    date = new Date();
                    int currentTime = date.getDate();
                    System.out.println("currentTime:" + currentTime);
                    if (currentTime != firstTime) {
                        startN();
                        firstTime = currentTime;
                    }

                    try {
                        Log.i("Seashell-->", "runing");
                        Thread.sleep(120 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();

        return super.onStartCommand(intent, flags, startId);
    }

    public void changeNewAndOldWord() {

        Context context = getApplicationContext();
        MySharedpreference sharedpreference = new MySharedpreference(context);
        Map map = sharedpreference.getWordJson();
        if (((String) map.get("today_json")).equals(mTodayGsonString)) {
            return;
        }
        mYesterdayGsonString = (String) map.get("today_json");
        sharedpreference.saveYesterdayJson(mYesterdayGsonString);
        sharedpreference.saveTodayJson(mTodayGsonString);
    }

    public void startN() {
        HttpDownloader httpDownloader = new HttpDownloader();
        mTodayGsonString = httpDownloader
                .download("http://test.drakeet.me/?key=seashell2");
        if (mTodayGsonString == null || mTodayGsonString.isEmpty()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mTodayGsonString = httpDownloader
                    .download("http://test.drakeet.me/?key=seashell2");
        }
        mWord = new Word();
        Gson gson = new Gson();
        mWord = gson.fromJson(mTodayGsonString, Word.class);
        normalRegular();
        try {
            if (mWord != null) {
                Message message = Message.obtain();
                message.obj = mWord;
                MainActivity.handler.sendMessage(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Context context = getApplicationContext();
        MySharedpreference sharedpreference = new MySharedpreference(context);
        Map map = sharedpreference.getInfo();
        int honor = (Integer) map.get("honor");
        honor++;
        sharedpreference.saveHonor(honor);
        changeNewAndOldWord();
        sharedpreference.saveTodayJson(mTodayGsonString);
        MainActivity.mTodayWord = mWord;
    }

    private void normalRegular() {

        Random random = new Random();
        int i = random.nextInt((int) SystemClock.uptimeMillis());

        NotificationCompat.Builder notifyBuilder;
        notifyBuilder = new NotificationCompat.Builder(
                this);
        notifyBuilder.setSmallIcon(R.drawable.ic_launcher);
        // 初始化
        notifyBuilder.setContentTitle("未联网");
        notifyBuilder.setContentText("请尝试联网后重启程序...");
        if (mWord != null) {
            notifyBuilder.setContentTitle(mWord.getWord());
            notifyBuilder.setContentText(mWord.getSpeech() + " "
                    + mWord.getExplanation());
        }

        // 如果不设置LargeIcon，那么系统会默认将上面的SmallIcon显示在通知选项的最左侧，右下角的小图标将不再显示
        // Bitmap bitmap =
        // BitmapFactory.decodeResource(getResources(),R.drawable.bigicon);
        // notifyBuilder.setLargeIcon(bitmap);
        // 这里用来显示右下角的数字
        notifyBuilder.setWhen(System.currentTimeMillis());

        // Intent deleteIntent = new Intent(this, MainActivity.class);
        // int deleteCode = (int) SystemClock.uptimeMillis();
        // PendingIntent deletePendingIntent = PendingIntent.getActivity(this,
        // deleteCode, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // notifyBuilder.setDeleteIntent(deletePendingIntent);

        Intent notifyIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // stackBuilder.addParentStack(NilActivity.class);
        stackBuilder.addNextIntent(notifyIntent);
        // 当设置下面PendingIntent.FLAG_UPDATE_CURRENT这个参数的时候，常常使得点击通知栏没效果，你需要给notification设置一个独一无二的requestCode
        int requestCode = (int) SystemClock.uptimeMillis();
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                requestCode, PendingIntent.FLAG_UPDATE_CURRENT);
        notifyBuilder.setContentIntent(resultPendingIntent);

        // 将AutoCancel设为true后，当你点击通知栏的notification后，它会自动被取消消失
        // //notifyBuilder.setAutoCancel(true);
        // 从Android4.1开始，可以通过以下方法，设置notification的优先级，优先级越高的，通知排的越靠前，优先级低的，不会在手机最顶部的状态栏显示图标
        notifyBuilder.setPriority(NotificationCompat.PRIORITY_MIN);
        // notifyBuilder.setPriority(NotificationCompat.PRIORITY_MIN);

        // Uri uri =
        // Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.cat);
        // Uri uri = Uri.parse("file:///mnt/sdcard/cat.mp3");
        // notifyBuilder.setSound(uri);
        notifyBuilder.setOngoing(true);
        long[] vibrate = {0, 50, 0, 0};
        notifyBuilder.setVibrate(vibrate);

        Notification notification = notifyBuilder.build();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFY_ID, notification);
    }

    @Override
    public void onDestroy() {
        stopRequested = true;
        thread.interrupt();
        isRun = false;
        super.onDestroy();
    }
}
