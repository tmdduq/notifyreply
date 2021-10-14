package com.osy.callapi;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.osy.notifyreply.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;

public class ApiCorona {
    final String TAG = "ApiCorona";
    String ApiKey = null;

    public ApiCorona(Context context){
        ApiKey = context.getResources().getString(R.string.coronaKey);
    }

    public String getNationalCorona(){
        String urlString = "https://api.corona-19.kr/korea/?serviceKey="+ApiKey;
        try {
            Log.i(TAG, "getNationalCorona call URL: " + urlString.toString());
            URL url = new URL(urlString.toString());
            URLConnection connection = url.openConnection();
            InputStream is = connection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));


            int totalCase=0;
            int totalRecovered=0;
            int totalDeath=0;
            int nowCase=0;

            while(totalCase==0 || totalDeath==0 || totalRecovered ==0 || nowCase==0) {
                String t = br.readLine();
                Log.i(TAG,t);
                try {
                    String[] result = t.split(":");
                    result[0] =result[0].trim().replace("\"","");
                    if(result[0].matches("TotalCase"))
                        totalCase = Integer.parseInt(result[1].trim().replace("\"","").replace(",",""));
                    else if(result[0].matches("TotalRecovered"))
                        totalRecovered = Integer.parseInt(result[1].trim().replace("\"","").replace(",",""));
                    else if(result[0].matches("TotalDeath"))
                        totalDeath = Integer.parseInt(result[1].trim().replace("\"","").replace(",",""));
                    else if(result[0].matches("NowCase"))
                        nowCase = Integer.parseInt(result[1].trim().replace("\"","").replace(",",""));
                }catch (Exception e){e.printStackTrace();}

            }
            if(totalCase==0 || totalDeath==0 || totalRecovered ==0 || nowCase==0) return null;
            DecimalFormat df = new DecimalFormat("###,###");
            String t = "코로나 현황입니다.\n" +
                    "누적 "+df.format(totalCase) +"명\n" +
                    "완치 "+df.format(totalRecovered) +"명\n" +
                    "사망 "+df.format(totalDeath) +"명\n" +
                    "현재 "+df.format(nowCase) +"명\n입니다.";
            return t;

        }catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }
}
