package com.osy.callapi;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class AnalysPlace {
    final String TAG = "SongdoHelper";
    public AnalysPlace(){
        Log.i(TAG, "Class on - "+TAG);
    }



    public String[] getPlace(String keyword) {
        if(keyword.contains("전화번호") )
            keyword = keyword.substring(0, keyword.indexOf("전화번호"));
        try {
            keyword = URLEncoder.encode(keyword,"UTF-8");
            String urlString = "https://m.map.naver.com/search2/searchMore.naver?query="+keyword+"&page=1&displayCount=75&type=SITE_1&sm=clk";
            Log.i(TAG, urlString);
            URL ulr = new URL(urlString);
            InputStream is = ulr.openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is,"utf-8"));
            String t = null;

            StringBuilder response = new StringBuilder();
            while(null!= (t = br.readLine()) )
                response.append(t+"\n");

            JSONArray jsonArray = new JSONObject(response.toString())
                    .getJSONObject("result")
                    .getJSONObject("site")
                    .getJSONArray("list");

            JSONObject jsonObject2 = jsonArray.getJSONObject(0);

            String name = jsonObject2.getString("name");
            String tel = jsonObject2.getString("tel");
            String addr = jsonObject2.getString("roadAddress");
            String id= jsonObject2.getString("id");
            String category= jsonObject2.getJSONArray("category").getString(0);
            String homePage= jsonObject2.getString("homePage");


            StringBuilder result = new StringBuilder();

            result.append(name+"("+category+")\n");
            result.append(addr+"\n");
            result.append("전화: "+tel+"\n");
            result.append(homePage+"\n");
            String detail = ("https://m.map.naver.com/search2/search.naver?query="+keyword+"\n자세히 보려면 클릭하세요!");
            if(new Random().nextInt(10)<1) return new String[]{detail, result.toString()};
            else return new String[]{result.toString()};
        }catch(Exception e){e.printStackTrace();}
        return null;
    }


    public String[] recommandPlace(String keyword){

        try {
            String urlString = "https://m.map.naver.com/search2/searchMore.naver?query="+URLEncoder.encode(keyword,"UTF-8")+"&displayCount=300";
            Log.i(TAG, urlString);
            URL ulr = new URL(urlString);
            InputStream is = ulr.openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is,"utf-8"));

            String t=null;
            StringBuilder response = new StringBuilder();
            while(null!= (t = br.readLine()) )
                response.append(t+"\n");

            JSONArray jsonArray = new JSONObject(response.toString())
                    .getJSONObject("result")
                    .getJSONObject("site")
                    .getJSONArray("list");

            ArrayList<String> list = new ArrayList<>();
            for(int i=0 ; i<jsonArray.length() ; i++) {
                JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                String name = jsonObject2.getString("name");
                JSONArray jsonArray2 = jsonObject2.getJSONArray("category");
                String category= jsonArray2.getString( jsonArray2.length()-1);
                list.add(name+"("+category+") ");
            }

            StringBuilder resultMessage = new StringBuilder(keyword+"!!\n");
            Collections.shuffle(list);
            for(int i=0 ; i < list.size() ; i++){
                resultMessage.append((i+1) +"."+list.get(i)+"\n");
                if(i>1) break;
            }
            resultMessage.append("추천해요!^^");
            return new String[]{resultMessage.toString()};

        }catch(Exception e){e.printStackTrace();}
        return null;
    }



}
