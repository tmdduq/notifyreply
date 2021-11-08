package com.osy.utility;

import android.app.Notification;
import android.content.Context;

public class LastTalk {
        private Context context;
        private Notification.Action act;

    public LastTalk(Context context, Notification.Action act) {
        this.context = context;
        this.act = act;
    }

    public Context getContext() {
        return context;
    }
    public Notification.Action getAct() {
        return act;
    }





}
