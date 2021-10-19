package com.osy.notifyreply;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.osy.callapi.ApiKMA;

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


        Log.i(TAG, "replyString roomName/message : " + roomName+"/"+message);
        String replyMessage =  rs.checkKeyword(sender,roomName,message);

        sendReply(context, act, replyMessage);

        if(replyMessage!=null && replyMessage.endsWith(" 정답이에요!"))
            try {
                Thread.sleep(2000);
                Random r = new Random();
                r.setSeed(System.currentTimeMillis());
                if(r.nextInt(10)<2) sendReply(context, act, "와 잘하세요!");
                if(r.nextInt(50)<2) sendReply(context, act, "점수가 궁금하면 [점수 확인]!");
                if(r.nextInt(50)<2) sendReply(context, act, "그만하시려면 [퀴즈중지]!");
                if(r.nextInt(100)<2)sendReply(context, act, "[명예의전당] ★시즌1★\n" +
                        "1위 짜와오(+100)\n2위 아리는냐옹냐옹(+95)\n3위 새 글(+45)\n4위 초운보전(+25)\n5위 에펜팡(+22)\n등 30명이 참가하였습니다.");
                Thread.sleep(2000);
                replyMessage =  rs.checkKeyword(sender,roomName,"퀴즈시작이어가기");
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


}
