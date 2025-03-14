package com.osy.callapi;

import android.content.Context;
import android.util.Log;

import com.osy.notifyreply.R;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

public class ApiStock {
    final String TAG = "ApiStock";
    String apiKey;
    Context context;

    public ApiStock(Context context){
        Log.i(TAG, "Class on - "+TAG);
        this.context = context;
        apiKey = context.getResources().getString(R.string.stockKey);
    }

    public ArrayList<String> getStocks(ArrayList<String[]> allList){
        StringBuilder sb = new StringBuilder();

        ArrayList<String> nasList = new ArrayList<>();
        ArrayList<String> kosList = new ArrayList<>();

        allList.forEach(it->{
            if(it[0].startsWith("nasdaq")) nasList.add( it[1]);
            else if(it[0].startsWith("kospi")) kosList.add( it[1]);
        });
        while (nasList.size() > 5) nasList.remove(nasList.size() - 1);
        while (kosList.size() > 5) kosList.remove(kosList.size() - 1);

        ArrayList<String> rst = new ArrayList<>();

        nasList.forEach( it-> sb.append(getNasdaqStock(it)+"\n") );
        if(sb.length()>0){
            sb.insert(0,"나스닥 현황이에요.\n");
            sb.deleteCharAt(sb.length()-1);
            rst.add(sb.toString());
        }
        sb.setLength(0);
        kosList.forEach( it-> sb.append(getDomesticStock(it)+"\n") );
        if(sb.length()>0){
            sb.deleteCharAt(sb.length()-1);
            sb.insert(0,"코스피 현황이에요.\n");
            rst.add(sb.toString());
        }
        return rst;
    }

    protected String getDomesticStock(String code){

        String ulrString = "https://polling.finance.naver.com/api/realtime/domestic/stock/"+code;
        try {
            URL ulr = new URL(ulrString);
            InputStream is = ulr.openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is,"utf-8"));
            String response = br.readLine();

            JSONObject jsonObject = new JSONObject(response);
//          jsonObject.keySet().forEach(s-> System.out.println(s+":\t"+ jsonObject.get(s)));

            JSONArray jsonArray = jsonObject.getJSONArray("datas");
            JSONObject jsonObject2 = jsonArray.getJSONObject(0);
//          jsonObject2.keySet().forEach(s-> System.out.println("2-"+s+":\t"+ jsonObject2.get(s)));

            String referenceTime = jsonObject.getString("time");
            String symbolCode = jsonObject2.getString("symbolCode");
            String stockName = jsonObject2.getString("stockName");
            String curPrice = jsonObject2.getString("closePrice");
            String changePrice = jsonObject2.getString("compareToPreviousClosePrice");;
            String changePriceRate = jsonObject2.getString("fluctuationsRatio");
            if(!changePrice.startsWith("-")) {
                changePrice = "+"+changePrice;
                changePriceRate = "+"+changePriceRate;
            }

            StringBuilder sb = new StringBuilder("");
            sb.append(stockName+" "+ curPrice+"원(" +changePriceRate+"%)");
            return sb.toString();
        }catch(Exception e){e.printStackTrace();}
        return null;
    }

    protected String getNasdaqStock(String code){

        String ulrString = "https://polling.finance.naver.com/api/realtime/worldstock/stock/"+code;
        try {
            URL ulr = new URL(ulrString);
            InputStream is = ulr.openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is,"utf-8"));
            String response = br.readLine();

            JSONObject jsonObject = new JSONObject(response);
//          jsonObject.keySet().forEach(s-> System.out.println(s+":\t"+ jsonObject.get(s)));

            JSONArray jsonArray = jsonObject.getJSONArray("datas");
            JSONObject jsonObject2 = jsonArray.getJSONObject(0);
//          jsonObject2.keySet().forEach(s-> System.out.println("2-"+s+":\t"+ jsonObject2.get(s)));

            String referenceTime = jsonObject.getString("time");
            String symbolCode = jsonObject2.getString("symbolCode");
            String stockName = jsonObject2.getString("stockName");
            String curPrice = jsonObject2.getString("closePrice");
            String changePrice = jsonObject2.getString("compareToPreviousClosePrice");;
            String changePriceRate = jsonObject2.getString("fluctuationsRatio");
            if(!changePrice.startsWith("-")) {
                changePrice = "+"+changePrice;
                changePriceRate = "+"+changePriceRate;
            }

            StringBuilder sb = new StringBuilder("");
            sb.append(stockName+" $"+ curPrice+"(" +changePriceRate+"%)");
            //sb.append("("+changePrice+"원 / "+changePriceRate+"%)");
            return sb.toString();
        }catch(Exception e){e.printStackTrace();}
        return null;
    }




    protected void getAllCode(int martTpcd){
        // martTpcd = 11.유가증권시장, 12.코스닥, 13.K-OTC, 14.코넥스, 50.기타시장
        String urlString = "http://api.seibro.or.kr/openapi/service/StockSvc/getShotnByMartN1" +
                "?serviceKey=" + apiKey +
                "&pageNo=1" +
                "&numOfRows=5000" +
                "&martTpcd=11" ;
        try{
            URL url = new URL(urlString);
            InputStream is = url.openStream();

        }catch(Exception e){
            e.printStackTrace();
            return;
        }
    }
    protected String[] getCode(String stockName){
        StringBuilder urlString = new StringBuilder("");
        urlString.append("http://api.seibro.or.kr/openapi/service/StockSvc/getStkIsinByNmN1");
        urlString.append("?secnNm=" + stockName);
        urlString.append("&numOfRows=10");
        urlString.append("&pageNo=1");
        urlString.append("&ServiceKey="+apiKey);
        try{
            URL url = new URL(urlString.toString());
            InputStream is = url.openStream();

            Document document = new ApiParser().parseXML(is);
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xpath = xPathFactory.newXPath();

            XPathExpression expr = xpath.compile("//shotnIsin ");
            NodeList nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            int length = nodeList.getLength();
            if(length==0) return null;

            String[] resultCode = new String[length];
            for(int i=0 ; i<length ; i++)
                resultCode[i] = nodeList.item(i).getTextContent();
            return resultCode;
        }catch (Exception e){
            e.printStackTrace();
        return null;}
    }


}
