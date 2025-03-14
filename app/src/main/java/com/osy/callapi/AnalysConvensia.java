package com.osy.callapi;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class AnalysConvensia {
    final String TAG = "AnalysConvensia";

    public AnalysConvensia(){
        Log.i(TAG, "Class on - "+TAG);
    }
    public String getConvensia(String keyword){

        Calendar c = Calendar.getInstance();
        String targetDay = null;
        if(keyword.contains("내일")){
            c.add(Calendar.DAY_OF_MONTH,1);
            targetDay = new SimpleDateFormat("yyyyMMdd").format(c.getTime());
        }
        else{
            keyword = keyword.replaceAll("[^0-9 ]","");
            String[] d = keyword.split(" ");
            int today= c.get(Calendar.DAY_OF_MONTH);
            for(String dd : d){
                try {
                    int t = Integer.parseInt(dd);
                    if (t < today)
                        c.add(Calendar.MONTH, 1);
                    targetDay = new SimpleDateFormat("yyyyMM").format(c.getTime()) + (t > 9 ? t : ("0" + t));
                    break;
                }catch(Exception e){}
            }
        }
        if(targetDay == null) targetDay = new SimpleDateFormat("yyyyMMdd").format(c.getTime());

        try {
            String urlString = "https://songdoconvensia.visitincheon.or.kr/schedule/list.do?startDt="+targetDay+"&endDt="+targetDay+"&type=1%2C2%2C3%2C4";
            Log.i("UrlString",""+urlString);
            URL url = new URL(urlString);
            URLConnection urlConnection = url.openConnection();
            InputStream is = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String response = br.readLine();

            JSONArray mainArray = new JSONArray(response);
            int arrayLength = mainArray.length();
            StringBuilder sb = new StringBuilder(""+ targetDay.substring(4,6)+"월"+ targetDay.substring(6)+"일 컨벤시아는\n");
           if(arrayLength == 0)
               return sb.append("행사가 없어요.").toString();
            else
                sb.append(arrayLength+"개 행사가 있어요!");

            Log.i("mainArray length",""+arrayLength);
            for(int i = 0 ; i < arrayLength ; i ++){
                JSONObject item = (JSONObject) mainArray.get(i);
                String title = item.getString("title");
                String company = item.getString("hostCompHome");
                Log.i("data", title+" // " + company);
                sb.append(String.format("\n%d.%s(%s)", i+1, title, company) );
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
