package com.osy.notifyreply;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import com.osy.utility.DataRoom;
import com.osy.utility.LastTalk;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    static boolean globalOnOff = true;
    TextView tv;
    EditText key, values;
    Spinner room;
    ScrollView sv;
    ImageView iconView1;
    ImageView iconView2;
    ReplyConstraint replyConstraint;
    @Override
    protected void onResume() {
        super.onResume();
        setSpinner();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = findViewById(R.id.textview);
        room = findViewById(R.id.edit_room);
        key = findViewById(R.id.edit_key);
        values = findViewById(R.id.edit_values);
        iconView1 = findViewById(R.id.iconView1);
        iconView2 = findViewById(R.id.iconView2);
        sv = findViewById(R.id.scrollView);
        replyConstraint = ReplyConstraint.getInstance();
        replyConstraint.setInitialize(this);
        setSpinner();


        iconView1.setBackgroundColor(0x77ff0000);
        iconView2.setBackgroundColor(0x77ff00ff);
        iconView1.requestFocus();
        ((Button)findViewById(R.id.button1_on)).setOnClickListener(v -> {
            globalOnOff = !globalOnOff;
            ((Button)v).setText(""+globalOnOff);
            if(globalOnOff) logAppend("SYSTEM: 채팅을 시작합니다.\n");
            if(!globalOnOff) logAppend("SYSTEM: 채팅을 정지합니다.\n");
        });
        ((Button)findViewById(R.id.button2_add)).setOnClickListener(v -> {
            replyConstraint.addContainsKeyword(room.getSelectedItem().toString(), key.getText().toString(), values.getText().toString() );
            logAppend("SYSTEM: 학습하기"+room.getSelectedItem().toString()+"+"+key.getText().toString()+"+"+values.getText().toString()+  ".\n");
        });
        room.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position==1) setSpinner();
                if(position==1) logAppend(replyConstraint.showNodes()+"\n");
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        ((Button)findViewById(R.id.button3_set)).setOnClickListener(view->
                startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")));

        ((Button)findViewById(R.id.button4_send)).setOnClickListener(view->{
                replyMessage(values.getText().toString());
        });
        ((Button)findViewById(R.id.button5_macro)).setOnClickListener(view->{
            String roomName = "macro"+room.getSelectedItem().toString();
            String valueText = values.getText().toString();
            if(replyConstraint.topicChecker.get(roomName)!=null) {
                replyConstraint.topicChecker.remove("macro"+room.getSelectedItem().toString());
                logAppend("SYSTEM: 매크로 중단.\n");
            }
            else {
                replyConstraint.topicChecker.put(roomName, valueText);
                logAppend("SYSTEM: 채팅방에 [" + valueText + "]매크로 시작합니다.\n");
                reverseWork();
            }
            sv.post(() -> sv.fullScroll(ScrollView.FOCUS_DOWN));
        });

        NotificationReceiver receiver = new NotificationReceiver();
        IntentFilter filter = new IntentFilter("com.osy.notifyreply.preverseWork");
        filter.addAction("com.osy.notifyreply.newMessage");
        registerReceiver(receiver,filter);
    }

    public void reverseWork(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0); // 원하는 시간 설정
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1); // 내일 자정으로 설정
        }
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent("com.osy.notifyreply.preverseWork");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        //alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+10000, 10000, pendingIntent);
    }

    public void setSpinner(){
        ArrayList<String> roomList = new ArrayList<String>();
        roomList.add("채팅방 선택");
        roomList.add("★목록 업데이트★");
        for(DataRoom r : replyConstraint.roomNodes) roomList.add(r.label);
        ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roomList);
        room.setAdapter(roomAdapter);

    }

    public void sendMessage(){

    }

    public void replyMessage(String s){
        LastTalk lastTalk = replyConstraint.lastTalkMap.get(room.getSelectedItem().toString());
        Context context = lastTalk.getContext();
        Notification.Action act = lastTalk.getAct();
        Intent sendIntent = new Intent();
        Bundle msg = new Bundle();
        for (RemoteInput inputable : act.getRemoteInputs())
            msg.putCharSequence(inputable.getResultKey(), s);
        RemoteInput.addResultsToIntent(act.getRemoteInputs(), sendIntent, msg);
        try {
            act.actionIntent.send(context, 0, sendIntent);
            logAppend("SYSTEM: SEND TO : "+s+"\n");
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }


    public void logAppend(String text){
        String[] lines = tv.getText().toString().split("\n");
        int linesToKeep = Math.min(10, lines.length);  // 5줄을 넘지 않도록 처리
        StringBuilder result = new StringBuilder();
        for (int i = lines.length - linesToKeep; i < lines.length; i++) {
            result.append(lines[i]).append("\n");  // 줄바꿈을 포함하여 추가
        }
        tv.setText(result +text+"\n");
        sv.post(()->sv.fullScroll(ScrollView.FOCUS_DOWN));
    }


    class NotificationReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("NotificationReceiver", "(action)"+action);
            switch(action){
                case "com.osy.notifyreply.newMessage" :
                    printNewMessage(intent);
                    break;
                case "com.osy.notifyreply.preverseWork" :
                    macroReply();
                    break;
            }

        }
        public void printNewMessage(Intent intent){
            String sender = intent.getStringExtra("sender");
            String message = intent.getStringExtra("message");
            Uri detailImageUri = intent.getParcelableExtra("detailImageUri");
            Icon largeIcon = intent.getParcelableExtra("largeIcon");

            Map<String, Object> msgMap = new HashMap<>();
            msgMap.put("message", message);
            msgMap.put("sender", sender);
            msgMap.put("detailImageUri", detailImageUri);
            msgMap.put("largeIcon", largeIcon);
            Message msg = Message.obtain();
            msg.obj = msgMap;
            printImage.sendMessage(msg); // Handler로 전송

            Log.i("MainActivity", " Broad Receive(sender/message) ");
            logAppend(sender+":"+message +"\n");
        }

        public void macroReply(){
            LastTalk lastTalk = replyConstraint.lastTalkMap.get(room.getSelectedItem().toString());
            if(lastTalk==null){
                logAppend("SYSTEM: 수신이력이 없습니다..\n");
                return;
            }
            Context context = lastTalk.getContext();
            Notification.Action act = lastTalk.getAct();
            Intent sendIntent = new Intent();
            Bundle msg = new Bundle();
            for (RemoteInput inputable : act.getRemoteInputs())
                msg.putCharSequence(inputable.getResultKey(), "1?");
            RemoteInput.addResultsToIntent(act.getRemoteInputs(), sendIntent, msg);
            try {
                act.actionIntent.send(context, 0, sendIntent);
                logAppend("SYSTEM: ["+values.getText().toString()+"] 매크로 전송.\n");
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }

        }

    }

    Handler printImage = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            Map<String, Object> msgMap = (Map<String, Object>) msg.obj;
            Uri detailImageUri = (Uri) msgMap.get("detailImageUri");
            Icon largeIocn = (Icon) msgMap.get("largeIcon");

            Log.i("printImage"," 프사: "+largeIocn+"/ 사진첨부: "+ detailImageUri);

            iconView2.setImageBitmap(iconToBitmap(largeIocn)); // 사용자 프사
            if(detailImageUri != null) iconView1.setImageBitmap(getBitmapFromUri( detailImageUri)); // 사진이미지 첨부
            else iconView1.setImageBitmap( Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888) );
        }
    };

    private Bitmap getBitmapFromUri(Uri uri) {
        Context context = this;
        try {
            return MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private Bitmap iconToBitmap(Icon icon) {
        if(icon==null) return Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
        Drawable drawable = icon.loadDrawable(this);
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        // 일반 Drawable을 Bitmap으로 변환
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }


}