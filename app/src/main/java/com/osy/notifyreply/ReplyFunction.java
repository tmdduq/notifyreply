package com.osy.notifyreply;

import android.database.Cursor;

import com.osy.utility.DataRoom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ReplyFunction {

    ReplyConstraint rs;
    ReplyFunction(){
        rs = ReplyConstraint.getInstance();
    }

    public ArrayList<DataRoom<DataRoom<String>>> setKeyList(ArrayList<DataRoom<DataRoom<String>>> roomNodes,
                                                            String insertRoom, String insertKey, String insertValue) {
        int RoomIndex=0;
        for( ; RoomIndex <roomNodes.size() ; RoomIndex++) {

            if (roomNodes.get(RoomIndex).label.matches(insertRoom)) {//방이 이미 있으면,
                DataRoom<DataRoom<String>> room = roomNodes.get(RoomIndex);
                int keyIndex = 0;

                for (; keyIndex < room.dataList.size(); keyIndex++) { // 키가 있으면,

                    if (room.dataList.get(keyIndex).label.matches(insertKey)) {
                        DataRoom<String> key = room.dataList.get(keyIndex);
                        key.dataList.add(insertValue);
                        keyIndex=room.dataList.size()+1;
                        return roomNodes;
                    }
                }
                if (keyIndex == room.dataList.size()) { //키가 없으면, 생성
                    ArrayList<String> values = new ArrayList<String>();
                    values.add(insertValue);
                    DataRoom<String> key = new DataRoom<String>();
                    key.label = insertKey;
                    key.dataList = values;
                    room.dataList.add(key);
                    return roomNodes;
                }
            }
        }
        if(RoomIndex==roomNodes.size()){ // 방이 없으면, 생성
            ArrayList<String> values = new ArrayList<String>();
            values.add(insertValue);
            DataRoom<String> key = new DataRoom<String>();
            key.label = insertKey;
            key.dataList = values;
            DataRoom<DataRoom<String>> createRoom = new DataRoom<DataRoom<String>>();
            createRoom.label = insertRoom;
            createRoom.dataList = new ArrayList<DataRoom<String>>();
            createRoom.dataList.add(key);
            roomNodes.add(createRoom);
            rs.isOperation.put(insertRoom,true);
        }

        return roomNodes;

    }



    public String consonant(String keyword){
        String[] chs = new String[] { "ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ", "ㅃ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ" };

        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < keyword.length(); i++) {
            char chName = keyword.charAt(i);
            if (chName >= 0xAC00) {
                int uniVal = chName - 0xAC00;
                int cho = ((uniVal - (uniVal % 28)) / 28) / 21;
                sb.append(chs[cho]);
            } else
                sb.append(chName);
        }
        return sb.toString();
    }



}
