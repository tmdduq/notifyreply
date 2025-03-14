package com.osy.notifyreply;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import static com.osy.notifyreply.MainActivity.globalOnOff;

import com.osy.utility.LastTalk;

public class NotifiService extends NotificationListenerService {
    ReplyConstraint replyConstraint;

    final String TAG = "NotifiService";
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        if(!globalOnOff){
            Log.i(TAG, "Global On Off -> OFF");
            stopSelf();
            return;
        }
        Log.i(TAG,"packName : "+sbn.getPackageName());

        if(!sbn.getPackageName().contains("com.kakao.talk")) return;
        printLogSbn(sbn);
        new Thread(()-> generatingReplyString(sbn)).start();
    }

    public void generatingReplyString(StatusBarNotification sbn){
        replyConstraint = ReplyConstraint.getInstance();
        replyConstraint.setContext(this);
        Notification notification = sbn.getNotification();

        //Share MainActivity
        String sender = notification.extras.getString("android.title");
        if(sender==null) return;
        String roomName = notification.extras.getString("android.subText");
        if(roomName==null) roomName = sender;
        replyConstraint.lastTalkMap.put(roomName, new LastTalk(this, notification.actions ));

        String message =  notification.extras.getString("android.text");
        Intent intent = new Intent("com.osy.notifyreply.newMessage");

        intent.putExtra("sender", sender);
        intent.putExtra("message", message);

        Icon largeIcon = notification.getLargeIcon();      //@
        intent.putExtra("largeIcon", largeIcon); //@
        intent.putExtra("detailImageUri", getDetailImageUri(notification));

        Log.i(TAG,"//\t"+sender+": "+message);

        sendBroadcast(intent);

        if(roomName.contains("미래기술과")) return;
        else if(roomName.contains("청공지")) return;
        else if(roomName.contains("청서무")) return;

        try {
            String[] replyMessage = replyConstraint.checkKeyword(sender, roomName, message);
            if(replyMessage==null) return;
            sendReply(replyMessage, sbn);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private Uri getDetailImageUri(Notification notification){
        Parcelable[] messages = notification.extras.getParcelableArray(Notification.EXTRA_MESSAGES);
        if (messages != null) {
            for (Parcelable p : messages)
                if (p instanceof Bundle) {
                    Bundle msgBundle = (Bundle) p; // msgBundle.keySet() == Bundle extras, sender_person, String sender, String text, long time, [String uri,String type]
                    if(msgBundle!=null && msgBundle.getString("type")!=null && msgBundle.get("uri") !=null) {
                        String ImageType = msgBundle.getString("type");
                        if(ImageType.startsWith("image"))
                            return (Uri)msgBundle.get("uri");
                    }
                }
        }
        return null;
    }

    public boolean sendReply(String replyMessage, StatusBarNotification sbn){
        if(replyMessage==null) return false;

        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        Notification.Action[] actions = sbn.getNotification().actions;  // 알림(Notification)에 포함된 액션 버튼들을 가져옴
        if(actions == null){   // null == 버튼이 없는 알림
            Log.i("sendReply", "(actions button) null");
            return false;
        }
        Log.i("sendReply actions", "(actions button len)"+actions.length); // len == 2 (답장버튼, 읽기처리버튼)

        for(Notification.Action a : actions) {
            RemoteInput[] arrayOfRemoteInput = a.getRemoteInputs();  // RemoteInput == 답장 입력창
            if (arrayOfRemoteInput == null) continue; // 입력창이 없다? PASS

            try {
                for (int i = 0; i < arrayOfRemoteInput.length; i++){ // arrayOfRemoteInput[i].getResultKey() : 답장입력창 아이디  :: 카카오톡에서는 "reply_message"
                    bundle.putCharSequence(arrayOfRemoteInput[i].getResultKey(), replyMessage);
                    Log.i("sendReply BundleData" , "(key)"+arrayOfRemoteInput[i].getResultKey() + "(value)"+replyMessage );
                }
                RemoteInput.addResultsToIntent(a.getRemoteInputs(), intent, bundle); // bundle 데이터를 intent에 추가
                a.actionIntent.send((Context) this, 0, intent); // 답장버튼 클릭!

            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    public boolean sendReply(String[] replyMessage, StatusBarNotification sbn) throws Exception{
        if (replyMessage == null) return false;
        for (int i = 0; i < replyMessage.length; i++) {
            sendReply(replyMessage[i], sbn);
            Thread.sleep(2000);
        }
        return true;
    }

    public void printLogSbn(StatusBarNotification sbn){
        Notification notification = sbn.getNotification();

        Log.i("printLogSbn","(packageName)"+ sbn.getPackageName());
        notification.extras.keySet().forEach( k -> Log.i("printLogSbn", "(key)"+k + " (value)"+notification.extras.get(k)));

        Parcelable[] messages = notification.extras.getParcelableArray(Notification.EXTRA_MESSAGES);
        if (messages != null) {
            for (Parcelable message : messages)
                if (message instanceof Bundle) {
                    // msgBundle.keySet() == Bundle extras, sender_person, String sender, String text, long time, [String uri,String type]
                    Bundle msgBundle = (Bundle) message;
                    if(msgBundle!=null)
                        msgBundle.keySet().forEach(k-> Log.d("printLogSbn: ", "(key)"+k+" (value)"+msgBundle.get(k) ));
                }

        }
    }

}
