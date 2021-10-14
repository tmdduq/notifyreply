package com.osy.callapi;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import com.osy.notifyreply.R;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class ApiKMA {
    final String APIKey = Resources.getSystem().getString(R.string.dataPotalKey);
    final String TAG = "ApiKMA";
    public class Code{

        final int BASEDATE = 0;
        final int CATEGORY = 1;
        final int FCSTTIME = 2;
        final int FCSTVALUE = 3;

        String category=null;
        String fcstValue=null;
        String fcstTime=null;

        String[] useCode = new String[]{"POP","REH","SKY","TMP","VEC","WSD"};
        final int POP = 0; // 강수확률 %
        final int REH = 1; // 습도 %
        final int SKY = 2; // 하늘상태 code
        final int TMP = 3; //
        final int VEC = 4; // 풍향 deg
        final int WSD = 5; // 풍속 m/s

        final int TMN = 6; // 일 최저기온 ˚C
        final int TMX = 7; // 일 최고기온 ˚C
        final int PTY = 8; // 강수형태코드값
        final int PCP = 9; // 1시간 강수량 mm
        final int SNO = 10; // 1시간 신적설 cm
        final int UUU = 11; // 풍속(동서성분) m/s
        final int VVV = 12; // 풍속(남북성분) m/s
        final int WAV = 13; // 파고 M

    }

    List<CSVRecord> records;
    ApiParser apiParser;

    public ApiKMA(Context context){
        apiParser =new ApiParser();
        try {
            records = CSVFormat.EXCEL.parse(new InputStreamReader(context.getAssets().open("KMA.csv", AssetManager.ACCESS_BUFFER))).getRecords();
            int columnsSize = records.get(0).size();
            while (records.get(records.size() - 1).size() != columnsSize)
                records.remove(records.size() - 1);
        }catch(Exception e){
            Log.i(TAG, "Fail phasing csv");
            e.printStackTrace();
        }
    }

    String[] addrToXy(String L1addr, String L2addr){
        Log.i(TAG, "addrToXy L1/L2: "+L1addr+"/"+L2addr);
        String[] xy = null;
        try {
            for (int i = 0; i < records.size(); i++) {
                if (records.get(i).get(3).matches(L1addr) && records.get(i).get(4).matches(L2addr)) {
                    xy = new String[2];
                    xy[0] = records.get(i).get(5);
                    xy[1] = records.get(i).get(6);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "address search exception: " + e);
            return null;
        }
        return xy;
    }

    public String getWeather(String L1addr, String L2addr) {
        Log.i(TAG, "getWeather L1/L2: "+L1addr+"/"+L2addr);
        String[] addr = addrToXy(L1addr, L2addr);
        if(addr==null) return "주소를 찾을수 없어요.";

        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        if(hour <3){
            c.add(Calendar.DAY_OF_WEEK,-1);
            hour = 23;
            minute = 50;
        }

        if ( hour % 3 != 2 || minute<10 )
            for( hour-- ; hour % 3 != 2 ; hour--);

        StringBuilder urlString = new StringBuilder("");
        urlString.append("http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst")
                .append("?serviceKey="+APIKey)
                .append("&pageNo=1")
                .append("&numOfRows=30")
                .append("&dataType=XML")
                .append("&base_date=" + new SimpleDateFormat("yyyyMMdd").format(c.getTime()) )
                .append("&base_time=" + (hour < 10 ? "0"+hour+"00" : hour+"00" ) )
                .append("&nx=" + addr[0])
                .append("&ny=" + addr[1]);

        try {
            Log.i(TAG, "getWeather2 call URL: " + urlString.toString());
            URL url = new URL(urlString.toString());
            URLConnection connection = url.openConnection();

            Document doc = apiParser.parseXML(connection.getInputStream());
            NodeList descNodes = doc.getElementsByTagName("item");

            StringBuilder reply = null;

            Code code = new Code();
            String curWeather[][] = new String[4][code.useCode.length+1];
            Log.i(TAG, "getWeather3 parseAPI(start) ");
            for(int i=0; i<descNodes.getLength() ;i++) {
                if(i==0) reply = new StringBuilder("");
                int check=0;
                String tmp=null;
                for(Node node = descNodes.item(i).getFirstChild(); node!=null; node=node.getNextSibling()) {
                    String nodeName = node.getNodeName();
                    if(nodeName.matches("baseDate")) {
                        tmp = node.getTextContent();
                    }
                    else if(nodeName.matches("category")) {
                        String category  = node.getTextContent();
                        for( ; check < code.useCode.length ; check++)
                            if(code.useCode[check].matches(category)) break;
                        if( (check==code.useCode.length) || (curWeather[code.CATEGORY][check]!=null) ) break;
                        curWeather[code.BASEDATE][check] = tmp;
                        curWeather[code.CATEGORY][check] = category;
                    }
                    else if(nodeName.matches("fcstTime"))
                        curWeather[code.FCSTTIME][check] = node.getTextContent();
                    else if(nodeName.matches("fcstValue"))
                        curWeather[code.FCSTVALUE][check] = node.getTextContent();
                }
                if(curWeather[code.FCSTTIME][check]==null) continue;
            }
            Log.i(TAG, "getWeather3 parseAPI(end point)");

            if (reply!=null) {
                reply.append(L1addr + " " + L2addr + " 지역 날씨정보입니다.\n");
                reply.append(curWeather[code.FCSTTIME][code.POP] + "기준,\n");
                reply.append("강수확률 " + curWeather[code.FCSTVALUE][code.POP] + "%\n");
                reply.append("습도 " + curWeather[code.FCSTVALUE][code.REH] + "%\n");
                reply.append("구름량 " + curWeather[code.FCSTVALUE][code.SKY] + "0%\n");
                reply.append("현재기온 " + curWeather[code.FCSTVALUE][code.TMP] + "˚C\n");
                reply.append("풍향 " + curWeather[code.FCSTVALUE][code.VEC] + "˚\n");
                reply.append("풍속 " + curWeather[code.FCSTVALUE][code.WSD] + "m/s\n");
                reply.append("입니다.^^\n");
                //System.out.println(reply.toString());
                return reply.toString();
            }
            return "잠시 후에 다시 시도해주세요.";

        }catch(Exception e) {
            e.printStackTrace();
            return "잠시 후에 다시 시도해주세요.";
        }

    }
}
