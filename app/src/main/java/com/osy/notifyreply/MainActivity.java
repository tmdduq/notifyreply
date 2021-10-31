package com.osy.notifyreply;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.osy.utility.DataRoom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    static boolean globalOnOff = true;
    TextView tv;
    Button bt1, bt2;
    EditText key, values;
    Spinner room;
    private NotificationReceiver receiver;
    ReplyConstraint rs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = findViewById(R.id.textview);
        bt1 = findViewById(R.id.button1);
        bt2 = findViewById(R.id.button2);
        room = findViewById(R.id.edit_room);
        key = findViewById(R.id.edit_key);
        values = findViewById(R.id.edit_values);
        rs = ReplyConstraint.getInstance();
        rs.setInitialize(this);
        setSpinner();

        bt1.setOnClickListener(v -> {
            globalOnOff = !globalOnOff;
            bt1.setText(""+globalOnOff);
            if(globalOnOff) tv.setText("SYSTEM: 채팅을 시작합니다.\n"+tv.getText());
            if(!globalOnOff) tv.setText("SYSTEM: 채팅을 정지합니다.\n"+tv.getText());
        });
        bt2.setOnClickListener(v -> {
            rs.addContainsKeyword(room.getSelectedItem().toString(), key.getText().toString(), values.getText().toString() );
            tv.setText("SYSTEM: 학습하기"+room.getSelectedItem().toString()+"+"+key.getText().toString()+"+"+values.getText().toString()+  ".\n"+tv.getText());
            setSpinner();
        });

        ((Button)findViewById(R.id.button3)).setOnClickListener(
                view-> startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")));

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



    class NotificationReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            String sender = intent.getStringExtra("sender");
            Log.i("MainActivity", " Broad Receive sender/message: " +sender+"/"+message);
            tv.setText(sender+":"+message +"\n"+ tv.getText());
        }
    }
}