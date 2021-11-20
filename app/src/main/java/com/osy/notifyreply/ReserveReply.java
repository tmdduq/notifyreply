package com.osy.notifyreply;

import com.osy.utility.LastTalk;

import java.util.Map;

public class ReserveReply {

    ReplyConstraint rs;
    LastTalk lastTalk;
    public ReserveReply(){
        rs = ReplyConstraint.getInstance();
    }

    public boolean callReserveReply(String roomName, String keyword){
        lastTalk = rs.lt.get(roomName);
        String topic;

        topic = rs.topicChecker.get(roomName+"dailyApplyhome");
        if(topic != null);


        return false;
    }


    public String dailyApplyhome(String keyword){

        return null;
    }

    public String customMacro(String keyword, String reserveTime, boolean preserve){

        return null;
    }
}
