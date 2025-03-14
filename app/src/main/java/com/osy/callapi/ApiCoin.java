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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ApiCoin {
    final String TAG ="ApiCoin";
    protected Map<String, String> coinMap_BITHUMB;
    protected Map<String, String> coinCodeMap_BITHUMB;
    protected Map<String, String> coinMap_UPBIT;
    protected Map<String, String> coinCodeMap_UPBIT;

    public ApiCoin(){
        Log.i(TAG, "Class on - "+TAG);
        getBithumbList();
        getUpbitList();
    }

    protected void getBithumbList(){
        coinMap_BITHUMB = new HashMap<>();
        coinCodeMap_BITHUMB = new HashMap<>();
        try {
            URL url = new URL("https://api.bithumb.com/v1/market/all");
            InputStream is = url.openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is,"utf-8"));
            String response = br.readLine();

            JSONArray mainArray = new JSONArray(response);

            for(int i = 0 ; i < mainArray.length() ; i++) {
                String code = ((JSONObject)mainArray.get(i)).getString("market");
                String name= ((JSONObject)mainArray.get(i)).getString("korean_name");
                if(code==null & name==null) break;
                if(code.startsWith("KRW")){
//                    Log.i(TAG,"get - n/c : " + name + "/"+code);
                    coinMap_BITHUMB.put(name,code.substring(4));
                    coinCodeMap_BITHUMB.put(code.substring(4),name);
                }
            }
        }catch(Exception e) {e.printStackTrace();}
    }

    protected void getUpbitList(){
        coinMap_UPBIT = new HashMap<>();
        coinCodeMap_UPBIT = new HashMap<>();
        try {
            URL url = new URL("https://api.upbit.com/v1/market/all");
            InputStream is = url.openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is,"utf-8"));
            String response = br.readLine();

            JSONArray mainArray = new JSONArray(response);

            for(int i = 0 ; i < mainArray.length() ; i++) {
                String code = ((JSONObject)mainArray.get(i)).getString("market");
                String name= ((JSONObject)mainArray.get(i)).getString("korean_name");
                if(code==null & name==null) break;
                if(code.startsWith("KRW")){
//                    Log.i(TAG,"get - n/c : " + name + "/"+code);
                    coinMap_UPBIT.put(name,code.substring(4));
                    coinCodeMap_UPBIT.put(code.substring(4),name);
                }
            }
        }catch(Exception e) {e.printStackTrace();}
    }

    public String getPrice(String coinName){
        Log.i(TAG, "getPrice come in");
        String coinCode = coinMap_BITHUMB.get(coinName);
        if(coinCode==null) coinCode = coinMap_UPBIT.get(coinName);
        if(coinCode==null) {
            coinName = coinName.toUpperCase();
            for (int i = 0; i < coinName.length(); i++) {
                char c = coinName.charAt(i);
                if (c < 'A' || c > 'Z') return null;
            }
            coinCode = coinName;
            coinName = coinCodeMap_BITHUMB.get(coinCode);
        }
        if(coinName==null) coinName = coinCodeMap_UPBIT.get(coinCode);
        if(coinName==null) return null;

        StringBuilder sb = new StringBuilder(coinName+" 현황이에요\n");
        String bithumb = getPrice_BITHUMB(coinCode);
        String upbit = getPrice_UPBIT(coinCode);
        if(bithumb!=null) sb.append("(빗썸)"+bithumb);
        if(upbit!=null) sb.append( (bithumb!=null? "\n" :"")+ "(업비트)"+upbit);
        if(bithumb==null && upbit==null) return null;
        return sb.toString();
    }


    protected String getPrice_UPBIT(String coinCode){
        coinCode ="KRW-"+coinCode;
        try {
            String urlString = "https://api.upbit.com/v1/ticker?markets="+coinCode;
            URL url = new URL(urlString);
            URLConnection urlConnection = url.openConnection();
            InputStream is = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String response = br.readLine();

            JSONArray mainArray = new JSONArray(response);

            NumberFormat numberFormat = NumberFormat.getInstance();
            numberFormat.setMaximumFractionDigits(2);

            double sp = ((JSONObject)mainArray.get(0)).getDouble("opening_price");
            double cp = ((JSONObject)mainArray.get(0)).getDouble("trade_price");
            //double ripple24 = mainObject.getDouble("fluctate_rate_24H");
            double rippleRage = (cp-sp) / sp * 100;
            String curPrice =  numberFormat.format(cp);
            String t = curPrice+"원("+numberFormat.format(rippleRage)+"%)";
            return t;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    protected String getPrice_BITHUMB(String coinCode){
        coinCode +="_KRW";
        try {
            String urlString = "https://api.bithumb.com/public/ticker/"+coinCode;
            URL url = new URL(urlString);
            URLConnection urlConnection = url.openConnection();
            InputStream is = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String response = br.readLine();

            JSONObject mainObject = new JSONObject(response);
            JSONObject dataObject = mainObject.getJSONObject("data");

            NumberFormat numberFormat = NumberFormat.getInstance();
            numberFormat.setMaximumFractionDigits(2);

            double sp = dataObject.getDouble("opening_price");
            double cp = dataObject.getDouble("closing_price");
            double ripple24 = dataObject.getDouble("fluctate_rate_24H");
            double rippleRage = (cp-sp) / sp *100;

            String curPrice =  numberFormat.format(dataObject.getDouble("closing_price"));

            String t = curPrice+"원("+numberFormat.format(rippleRage)+"%)";
            return t;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public String getAllPrice_UPBIT(){
        StringBuilder allCoin = new StringBuilder("");

        coinMap_UPBIT.forEach((k,v)-> allCoin.append("KRW-"+v+","));
        allCoin.deleteCharAt(allCoin.length()-1);
        try {
            String urlString = "https://api.upbit.com/v1/ticker?markets="+allCoin;
            URL url = new URL(urlString);
            URLConnection urlConnection = url.openConnection();
            InputStream is = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String response = br.readLine();

            JSONArray mainArray = new JSONArray(response);

            NumberFormat numberFormat = NumberFormat.getInstance();
            numberFormat.setMaximumFractionDigits(2);
            StringBuilder sb = new StringBuilder("업비트 모든 코인입니다.");
            for(int i=0 ; i < mainArray.length() ; i++) {
                String name = ((JSONObject)mainArray.get(i)).getString("market").substring(4);
                double sp = ((JSONObject)mainArray.get(i)).getDouble("opening_price");
                double cp = ((JSONObject)mainArray.get(i)).getDouble("trade_price");
                //double ripple24 = mainObject.getDouble("fluctate_rate_24H");
                double rippleRage = (cp-sp) / sp *100;

                String curPrice =  numberFormat.format(cp);
                sb.append(coinCodeMap_UPBIT.get(name)+":"+curPrice+"원("+numberFormat.format(rippleRage)+"%)\n");
            }
            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public String getAllPrice_BITHUMB(){
        try {
            String urlString = "https://api.bithumb.com/public/ticker/all_KRW";
            URL url = new URL(urlString);
            URLConnection urlConnection = url.openConnection();
            InputStream is = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String response = br.readLine();

            JSONObject mainObject = new JSONObject(response);
            JSONObject allDataObject = mainObject.getJSONObject("data");

            NumberFormat numberFormat = NumberFormat.getInstance();
            numberFormat.setMaximumFractionDigits(2);

            Iterator<String> it = allDataObject.keys();
            StringBuilder sb = new StringBuilder("빗썸 모든코인입니다.");
            while(it.hasNext()){
                String coinCode = it.next();
                if(coinCode.matches("date")){
                    sb.insert(0, new SimpleDateFormat("yyyyMMdd hh:mm:ss").format(Long.parseLong(allDataObject.getString(coinCode)))+ "기준입니다.\n");
                    return sb.toString();
                }
                JSONObject dataObject = allDataObject.getJSONObject(coinCode);
                double sp = dataObject.getDouble("opening_price");
                double cp = dataObject.getDouble("closing_price");
                double rippleRage = (cp-sp) / sp * 100;
                String curPrice =  numberFormat.format(cp);
                sb.append(coinCodeMap_BITHUMB.get(coinCode)+":"+curPrice+"원("+numberFormat.format(rippleRage)+"%)\n");
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
