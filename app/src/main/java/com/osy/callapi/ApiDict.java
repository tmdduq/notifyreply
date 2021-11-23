package com.osy.callapi;

import android.content.Context;
import android.util.Log;
import com.osy.notifyreply.R;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

public class ApiDict {
    final String TAG = "ApiDict";
    Context context;
    String ApiKey;

    public ApiDict(Context context){
        Log.i(TAG, "Class on - "+TAG);
        this.context = context;
        ApiKey = context.getResources().getString(R.string.dictKey);
    }

    public String searchDictionary(String keyword){
        String urlString = "https://stdict.korean.go.kr/api/search.do?key="+ApiKey+"&q="+ keyword;
        Log.i(TAG, "searchDictionary :"+urlString);
        try{
            URL url = new URL(urlString);
            URLConnection urlConnection = url.openConnection();

            Document document = new ApiParser().parseXML(urlConnection.getInputStream());
            NodeList nodeList = document.getElementsByTagName("item");
            StringBuilder sb = new StringBuilder("");
            if(nodeList.getLength()==0) return keyword+"..? 처음들어요.";
//          for(int i = 0; i< nodeList.getLength() ; i++)   // 1건만 나오도록 주석처리..
            int i = new Random().nextInt(nodeList.getLength()); // for 대신에 랜덤 1건만
                for(Node node = nodeList.item(i).getFirstChild(); node!=null; node=node.getNextSibling()){
                    String nodeName = node.getNodeName();
                    if(nodeName.matches("word"))
                        sb.append("'"+node.getTextContent()+"'");
                    else if(nodeName.matches("pos"))
                        sb.append(" ("+node.getTextContent()+")\n");
                    else if(nodeName.matches("sense")) {
                        sb.append("뜻: "+node.getChildNodes().item(1).getTextContent());
                    }
                }

            if(!sb.toString().matches(""))
                sb.insert(0,"국어사전을 찾아봤어요.\n");
            return sb.toString();


        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
