package com.raas.kakaotalkbot;

import android.app.Notification;
import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class KakaoTalkNotificationListenerService extends NotificationListenerService {

    boolean isRunning;

    @Override
    public void onCreate() {
        super.onCreate();

        isRunning = true;
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("control-message"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        isRunning = false;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        // 작동 제어
        if(!isRunning)
            return;

        // 카카오톡 메시지 아니면 무시
        if(!sbn.getPackageName().equals("com.kakao.talk"))
            return;

        Notification notification = sbn.getNotification();
        Notification.WearableExtender wExt = new Notification.WearableExtender(notification);

        // 카톡 데이터 받기
        Bundle extras = notification.extras;

        String title = extras.getString(Notification.EXTRA_TITLE);
        CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);
//        CharSequence subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);

        if(text == null)
            return;

        Log.i("NotificationListener", "[snowdeer] Title:" + title);
        Log.i("NotificationListener", "[snowdeer] Text:" + text);
//        Log.i("NotificationListener", "[snowdeer] Sub Text:" + subText);

        if(title == null || !title.contains("test"))
            return;

        //답장하기
        for(Notification.Action act : wExt.getActions()) {
            if(act.getRemoteInputs() != null && act.getRemoteInputs().length > 0) {
                if (act.title.toString().toLowerCase().contains("reply") || act.title.toString().contains("답장")) {
                    Bundle msg = new Bundle();
                    Intent sendIntent = new Intent();
                    CharSequence value = text;
                    for (RemoteInput inputTable : act.getRemoteInputs()) msg.putCharSequence(inputTable.getResultKey(), value);
                    RemoteInput.addResultsToIntent(act.getRemoteInputs(), sendIntent, msg);

                    android.content.Context execContext = getApplicationContext();
                    try {
                        act.actionIntent.send(execContext, 0, sendIntent);
                    } catch (Throwable e) {
                        Log.e("parser", "?", e);
                    }
                }
            }
        }

    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent)
        {
            isRunning = intent.getBooleanExtra("isRunning", false);
            Log.d("isRunning", String.valueOf(isRunning));
        }
    };

}