package com.osy.notifyreply;

import android.app.Notification;
import android.content.Context;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class KeywordMap<K extends String,V extends String> {
    final String TAG = "KeywordMap";

    Map<String, Room> map = new HashMap<String,Room>();
    protected class Room{
        ArrayList<K> keyList = new ArrayList<K>();
        ArrayList<ArrayList<V>> valuesList = new ArrayList< ArrayList<V> >();
    }


    KeywordMap put(String room, K key, V values){
        Room curRoom = map.get(room);
        if(curRoom == null) curRoom = new Room();

        boolean isNewItem = false;
        int index = 0;
        for(  ; index < curRoom.keyList.size(); index++)
            if(curRoom.keyList.get(index) == key)
                break;
        if (index == curRoom.keyList.size()) isNewItem = true;

        ArrayList<V> arrayList;
        if (isNewItem) {
            arrayList = new ArrayList<V>();
            arrayList.add(values);
            curRoom.keyList.add(index, key);
            curRoom.valuesList.add(index, arrayList);
        }
        else{
            arrayList = curRoom.valuesList.get(index);
            arrayList.add(values);
            curRoom.keyList.set(index, key);
            curRoom.valuesList.set(index, arrayList);
        }
        map.put(room, curRoom);
        return this;
    }

    ArrayList<V> get(String room, K key){
        Log.i(TAG, "get method in.");
        Room curRoom = map.get(room);
        if(curRoom==null) return null;

        for(int i = 0 ; i < curRoom.keyList.size() ; i++)
            if( key.contains(curRoom.keyList.get(i)) )
                return curRoom.valuesList.get(i);

        return null;
    }
}
