package com.osy.notifyreply;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    static boolean globalOnOff = true;
    TextView tv;
    Button bt;
    EditText room, key, values;
    private NotificationReceiver receiver;
    ReplyConstraint rs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = findViewById(R.id.textview);
        bt = findViewById(R.id.button1);
        room = findViewById(R.id.edit_room);
        key = findViewById(R.id.edit_key);
        values = findViewById(R.id.edit_values);
        rs = ReplyConstraint.getInstance();
        rs.setInitialize(this);

        bt.setOnClickListener(v -> {
            globalOnOff = !globalOnOff;
            bt.setText(""+globalOnOff);
        });
        ((Button)findViewById(R.id.button2)).setOnClickListener(view->{
                 Intent intentnew = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                   startActivity(intentnew);
        });

        receiver = new NotificationReceiver( );
        IntentFilter filter = new IntentFilter("com.osy.notifyreply");
        registerReceiver(receiver,filter);

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