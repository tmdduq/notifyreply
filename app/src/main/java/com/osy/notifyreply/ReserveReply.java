package com.osy.notifyreply;

import com.osy.utility.LastTalk;

import java.util.Map;

public class ReserveReply {

    ReplyConstraint replyConstraint;
    LastTalk lastTalk;
    public ReserveReply(){
        replyConstraint = ReplyConstraint.getInstance();
    }

    public boolean callReserveReply(String roomName, String keyword){
        lastTalk = replyConstraint.lastTalkMap.get(roomName);
        String topic;

        topic = replyConstraint.topicChecker.get(roomName+"dailyApplyhome");
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
