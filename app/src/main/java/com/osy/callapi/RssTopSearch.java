package com.osy.callapi;

import android.app.Notification;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RssTopSearch {

    final String TAG = "RssTopSearch";

    public RssTopSearch(){
        Log.i(TAG, "Class on - "+TAG);
    }
    public String getTopSearchKeyword(){

        String urlString = "https://trends.google.co.kr/trends/trendingsearches/daily/rss?geo=KR";
        try{
            URL url = new URL(urlString);
            URLConnection urlConnection = url.openConnection();
            Log.i(TAG, "getTopSearchKeyword: "+urlString);
            Document document  = new ApiParser().parseXML(urlConnection.getInputStream());
            NodeList nodeList = document.getElementsByTagName("item");

            StringBuilder sb = new StringBuilder("");
            for(int i = 0 ; i< nodeList.getLength() ; i++){
                Node node = nodeList.item(i).getFirstChild();
                String t = null;
                for( ; node!=null ; node = node.getNextSibling()){
                    String nodeName = node.getNodeName();
                    if(nodeName.equals("title"))
                        t = (i + 1) + "위: " + node.getChildNodes().item(0).getTextContent();
                    if(nodeName.matches("ht:approx_traffic"))
                        t += " ("+node.getTextContent()+")";
                    else if(nodeName.equals("pubDate")){
                        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy", new Locale("en","US"));
                        String curTime = sdf.format(Calendar.getInstance().getTime());
                        String nodeValue = node.getTextContent().substring(0,16);
                        if(!curTime.matches(nodeValue))
                            return sb.toString()+"구글에서 가져왔어요!";

                        sb.append(t+"\n");
                        t = null;
                    }
                }
            }
        return sb.toString();

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
