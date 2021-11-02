package com.osy.callapi;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;

import com.osy.notifyreply.R;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class ApiSellApart {
    final String TAG = "ApiSellApart";
    String ApiKey = null;
    Context context;

    public ApiSellApart(Context context){
        this.context = context;
        ApiKey = context.getResources().getString(R.string.sellApartKey);
    }

    public String getPrice(String addr){
        String landCode = null;
        try {
            InputStream is = context.getAssets().open("realSellApart.txt", AssetManager.ACCESS_BUFFER);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            for(String s= br.readLine();s!=null ; s=br.readLine()){
                if(s.startsWith(addr)){
                    String t[] = s.split("\t");
                    landCode = t[t.length-1].trim();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "법정동 "+landCode);
        if(landCode==null) return "법정동을 찾을 수 없어요";

        try {
            String urlString = "http://openapi.molit.go.kr:8081/OpenAPI_ToolInstallPackage/service/rest/RTMSOBJSvc/getRTMSDataSvcAptTrade" +
                    "?serviceKey=" +ApiKey+
                    "&LAWD_CD=" + landCode+
                    "&DEAL_YMD=" +new SimpleDateFormat("yyyyMM").format(Calendar.getInstance().getTime());
            URL url = null;

            url = new URL(urlString);
            Log.i(TAG,"Api Url :"+ urlString);

            URLConnection connection = url.openConnection();
            InputStream is = connection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String response = br.readLine();
            response = response.replace("<item>","\n")
                    .replace("</거래금액>","\n")
                    .replace("</거래유형>","\n")
                    .replace("</건축년도>", "\n")
                    .replace("</년>", "\n")
                    .replace("</법정동>", "\n")
                    .replace("</아파트>", "\n")
                    .replace("</월>", "\n")
                    .replace("</일>", "\n")
                    .replace("</전용면적>", "\n")
                    .replace("</지번>", "\n")
                    .replace("</지역코드>", "\n")
                    .replace("</층>", "\n")
                    .replace("</해제사유발생일>", "\n")
                    .replace("</해제여부>", "\n")
                    .replace("</item>", "\n");
            StringReader sr = new StringReader(response);
            br.close();
            br = new BufferedReader(sr);
            StringBuilder sb = new StringBuilder("");
            int i = 0;
            for(String readLine=br.readLine() ; readLine != null ; readLine=br.readLine()){
                if(readLine.contains("<거래금액"))
                    sb.append("거래금액: "+readLine.substring(readLine.indexOf(">")+1).trim()+ "만원\n");
                else if(readLine.contains("<건축년도"))
                    sb.append("건축년도: "+readLine.substring(readLine.indexOf(">")+1).trim()+ "\n");
                else if(readLine.contains("<법정동"))
                    sb.append("아파트명: "+readLine.substring(readLine.indexOf(">")+1).trim()+ " ");
                else if(readLine.contains("<아파트"))
                    sb.append(readLine.substring(readLine.indexOf(">")+1).trim()+ "\n");
                else if(readLine.contains("<월"))
                    sb.append("거래날짜: "+readLine.substring(readLine.indexOf(">")+1).trim()+ "월");
                else if(readLine.contains("<일"))
                    sb.append(readLine.substring(readLine.indexOf(">")+1).trim()+ "일\n");
                else if(readLine.contains("<전용면적"))
                    sb.append("전용면적: "+readLine.substring(readLine.indexOf(">")+1).trim()+ "m² (");
                else if(readLine.contains("<층")) {
                    sb.append(readLine.substring(readLine.indexOf(">")+1).trim()+ "층)\n");
                    sb.append("----------\n");
                    i++;
                }


                if(i>2){
                    sb.append("등이 실거래되었어요.");
                    return sb.toString();
                }
            }
            if(sb.toString().matches("")) return "검색결과가 없어요.";
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "잠시 후 다시시도해주세요";
        }
    }
}
