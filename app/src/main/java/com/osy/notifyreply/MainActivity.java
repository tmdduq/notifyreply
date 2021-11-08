package com.osy.notifyreply;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.osy.utility.DataRoom;
import com.osy.utility.LastTalk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    static boolean globalOnOff = true;
    TextView tv;
    EditText key, values;
    Spinner room;
    ScrollView sv;
    private NotificationReceiver receiver;
    ReplyConstraint rs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = findViewById(R.id.textview);
        room = findViewById(R.id.edit_room);
        key = findViewById(R.id.edit_key);
        values = findViewById(R.id.edit_values);
        sv = findViewById(R.id.scrollView);
        rs = ReplyConstraint.getInstance();
        rs.setInitialize(this);
        setSpinner();

        ((Button)findViewById(R.id.button1_on)).setOnClickListener(v -> {
            globalOnOff = !globalOnOff;
            ((Button)v).setText(""+globalOnOff);
            if(globalOnOff) tv.append("SYSTEM: 채팅을 시작합니다.\n");
            if(!globalOnOff) tv.append("SYSTEM: 채팅을 정지합니다.\n");
            sv.post(()->sv.fullScroll(ScrollView.FOCUS_DOWN));
        });
        ((Button)findViewById(R.id.button2_add)).setOnClickListener(v -> {
            rs.addContainsKeyword(room.getSelectedItem().toString(), key.getText().toString(), values.getText().toString() );
            tv.append("SYSTEM: 학습하기"+room.getSelectedItem().toString()+"+"+key.getText().toString()+"+"+values.getText().toString()+  ".\n");
            sv.post(()->sv.fullScroll(ScrollView.FOCUS_DOWN));
            setSpinner();
        });

        ((Button)findViewById(R.id.button3_set)).setOnClickListener(view->
                startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")));
        ((Button)findViewById(R.id.button4_send)).setOnClickListener(view->{
            if(key.getText().toString()=="1") {
                tv.append("SYSTEM: 채팅방에 1 매크로를 가동합니다.\n");
                sv.post(() -> sv.fullScroll(ScrollView.FOCUS_DOWN));
                handlar.sendEmptyMessage(0);
            }
            else replyMessage(values.getText().toString());

                });


        receiver = new NotificationReceiver( );
        IntentFilter filter = new IntentFilter("com.osy.notifyreply");
        registerReceiver(receiver,filter);

    }

    public void setSpinner(){
        ArrayList<String> roomList = new ArrayList<String>();
        roomList.add("채팅방 선택");
        for(DataRoom r : rs.roomNodes) roomList.add(r.label);
        ArrayAdapter<String> roomAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, roomList);
        room.setAdapter(roomAdapter);
    }

    Handler handlar = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msge) {
            super.handleMessage(msge);
            LastTalk lastTalk = rs.lt.get(room.getSelectedItem().toString());
            if(lastTalk==null){
                tv.append("SYSTEM: 수신이력이 없습니다..\n");
                sv.post(()->sv.fullScroll(ScrollView.FOCUS_DOWN));
                return;
            }
            Calendar c = Calendar.getInstance();
            if( c.get(Calendar.MINUTE)!=0 || c.get(Calendar.HOUR_OF_DAY)!=0)  {
                sendEmptyMessageDelayed(0,5000);
                return;
            }
            tv.append("SYSTEM: check time.."+ c.get(Calendar.HOUR)+":00\n");

            Context context = lastTalk.getContext();
            Notification.Action act = lastTalk.getAct();
            Intent sendIntent = new Intent();
            Bundle msg = new Bundle();
            for (RemoteInput inputable : act.getRemoteInputs())
                msg.putCharSequence(inputable.getResultKey(), "1");
            RemoteInput.addResultsToIntent(act.getRemoteInputs(), sendIntent, msg);
            try {
                act.actionIntent.send(context, 0, sendIntent);
                tv.append("SYSTEM: 1 매크로 동작 완료.\n");
                sv.post(()->sv.fullScroll(ScrollView.FOCUS_DOWN));
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }

            int i = 0;
            try{
                i = Integer.parseInt(key.getText().toString());
            }catch(Exception e){i = 0;}
            if(i==0) sendEmptyMessageDelayed(0,60000);
            else  tv.append("SYSTEM: 1 매크로 중지.\n");
        }
    };

    public void replyMessage(String s){
        LastTalk lastTalk = rs.lt.get(room.getSelectedItem().toString());
        Context context = lastTalk.getContext();
        Notification.Action act = lastTalk.getAct();
        Intent sendIntent = new Intent();
        Bundle msg = new Bundle();
        for (RemoteInput inputable : act.getRemoteInputs())
            msg.putCharSequence(inputable.getResultKey(), s);
        RemoteInput.addResultsToIntent(act.getRemoteInputs(), sendIntent, msg);
        try {
            act.actionIntent.send(context, 0, sendIntent);
            sv.post(()->sv.fullScroll(ScrollView.FOCUS_DOWN));
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    class NotificationReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            String sender = intent.getStringExtra("sender");
            Log.i("MainActivity", " Broad Receive sender/message: " +sender+"/"+message);
            tv.setText(sender+":"+message +"\n"+ tv.getText());
            sv.post(()->sv.fullScroll(ScrollView.FOCUS_DOWN));
        }
    }
}