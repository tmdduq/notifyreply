package com.osy.utility;

import android.app.Notification;
import android.content.Context;

public class LastTalk {
        private Context context;
        private Notification.Action act;

    public LastTalk(Context context, Notification.Action[] acts) {
        this.context = context;
        for(Notification.Action a : acts)
            if( a.getRemoteInputs() !=null ) this.act = a;
    }

    public Context getContext() {
        return context;
    }
    public Notification.Action getAct() {
        return act;
    }





}
