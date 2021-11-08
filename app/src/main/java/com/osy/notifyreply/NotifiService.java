package com.osy.notifyreply;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.osy.callapi.ApiKMA;
import com.osy.utility.LastTalk;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static com.osy.notifyreply.MainActivity.globalOnOff;

public class NotifiService extends NotificationListenerService {
    ReplyConstraint rs;

    final String TAG = "NotifiService";
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        if(!globalOnOff){
            Log.i(TAG, "Global On Off -> OFF");
            stopSelf();
            return;
        }
        if(!sbn.getPackageName().contains("com.kakao.talk")){
            Log.i(TAG,"packName : "+sbn.getPackageName());
            return;
        }
        Log.i(TAG,"packName : "+sbn.getPackageName());
        new Thread( ()-> {
        Notification.WearableExtender wearableExtender =new Notification.WearableExtender(sbn.getNotification());
        List<Notification.Action> wearableAction = wearableExtender.getActions();

        for(Notification.Action act : wearableAction){
            if(act.getRemoteInputs() != null && act.getRemoteInputs().length>0){
                replyString(getApplicationContext(), act, sbn.getNotification());
                stopSelf();

            }
        }
        }).start();
    }

    public void replyString(Context context, Notification.Action act, Notification notification){
        rs = ReplyConstraint.getInstance();
        rs.setContext(this);


        //Share MainActivity
        String sender = notification.extras.getString("android.title");
        String roomName = notification.extras.getString("android.subText");
        if(roomName==null) roomName = sender;
        String message =  notification.extras.getString("android.text");
        Intent intent = new Intent("com.osy.notifyreply");
        intent.putExtra("sender", sender);
        intent.putExtra("message", message);
        sendBroadcast(intent);

        LastTalk lastTalk = new LastTalk(context,act);
        rs.lt.put(roomName, lastTalk);

        String lk = rs.topicChecker.get("subscriptionDailyNews"+roomName);
        if(lk!=null)
            try{
                Calendar cal = Calendar.getInstance();
                String day = cal.get(Calendar.MONTH)+""+(cal.get(Calendar.DATE));
                if(cal.get(Calendar.HOUR_OF_DAY) <= 8)
                if(cal.get(Calendar.HOUR_OF_DAY) >= 7)
                if(!lk.matches(day)) {
                    rs.topicChecker.replace("subscriptionDailyNews" + roomName, day);
                    sendReply(context, act, "오늘아침 주요뉴스!");
                    sendReply(context, act, rs.subscriptionDailyNews(roomName, "데일리뉴스 보기"));
                }
            }catch(Exception e){
                e.printStackTrace();
            };


        try {
            String[] replyMessage = rs.checkKeyword(sender, roomName, message);
            if(replyMessage==null) return;
            sendReply(context, act, replyMessage);

            }catch (Exception e){
                e.printStackTrace();
            }



    }


    public boolean sendReply(Context context, Notification.Action act, String replyMessage){
        if(replyMessage==null) return false;

        Intent sendIntent = new Intent();
        Bundle msg = new Bundle();
        for (RemoteInput inputable : act.getRemoteInputs())
            msg.putCharSequence(inputable.getResultKey(), replyMessage);
        RemoteInput.addResultsToIntent(act.getRemoteInputs(), sendIntent, msg);

        try {
            act.actionIntent.send(context, 0, sendIntent);
            Log.i(TAG,"send() to"+replyMessage);
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean sendReply(Context context, Notification.Action act, String[] replyMessage) throws Exception{
        if (replyMessage == null) return false;
        for (int i = 0; i < replyMessage.length; i++) {
            sendReply(context, act, replyMessage[i]);
            Thread.sleep(2000);
        }
        return true;
    }

}
