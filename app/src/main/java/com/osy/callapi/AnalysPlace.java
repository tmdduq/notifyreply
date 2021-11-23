package com.osy.callapi;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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
        if(keyword.contains("가게정보"))
            keyword = keyword.substring(0, keyword.indexOf("위치정보"));
        else if(keyword.contains("영업시간") )
            keyword = keyword.substring(0, keyword.indexOf("영업시간"));
        else if(keyword.contains("전화번호") )
            keyword = keyword.substring(0, keyword.indexOf("전화번호"));
        keyword = "송도 "+keyword;
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

            StringBuilder result = new StringBuilder();

            result.append(name+"("+category+")\n");
            result.append("주소: "+addr+"\n");
            result.append("전화: "+tel+"\n");
            result.append("영업: "+getWorkTime(id));
            String detail = ("https://m.map.naver.com/search2/search.naver?query="+keyword+"\n자세히 보려면 클릭하세요!");
            if(new Random().nextInt(10)<1) return new String[]{detail, result.toString()};
            else return new String[]{result.toString()};
        }catch(Exception e){e.printStackTrace();}
        return null;
    }

    public String getWorkTime(String id){
        id = id.replaceAll("[^0-9]","");
        String urlString = "https://m.place.naver.com/place/"+id+"/home?entry=pll";
        Log.i(TAG, urlString);
        try {
            Document document = Jsoup.connect(urlString).get();
            Element element = document.select("div.vfjlt").first();
            String resultMessage = element.text().replaceAll(" 펼쳐보기", "");
            return resultMessage;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String[] recommandPlace(String keyword){
        String[] referencePlaces = new String[]{"해양경찰청 근처 ", "롯데몰 송도캐슬파크 근처 ", "송도 컨벤시아 근처 ",
                "송도 오라카이 근처 ", "캠퍼스타운역 근처 ","커넬워크 근처 ","현대프리미엄아울렛 송도점 근처 ",
                "송도5동 행정복지센터 근처 ", "송도 트리플스트리트 근처 "};
        String referencePlace = referencePlaces[ new Random().nextInt(referencePlaces.length)];
        if(keyword.contains("해양경찰청")) referencePlace = referencePlaces[0];
        else if(keyword.contains("롯데몰") || keyword.contains("캐슬")) referencePlace = referencePlaces[1];
        else if(keyword.contains("컨벤시아")) referencePlace = referencePlaces[2];
        else if(keyword.contains("오라카이")) referencePlace = referencePlaces[3];
        else if(keyword.contains("캠")) referencePlace = referencePlaces[4];
        else if(keyword.contains("커넬")) referencePlace = referencePlaces[5];
        else if(keyword.contains("송현아")) referencePlace = referencePlaces[6];
        else if(keyword.contains("8공구")) referencePlace = referencePlaces[7];
        else if(keyword.contains("트리플")||keyword.contains("스트리트")) referencePlace = referencePlaces[8];
        else if(keyword.contains(" 근처")) referencePlace = "송도 "+keyword.substring(0, keyword.indexOf(" 근처"))+" 근처 ";

        if(keyword.contains("식당")) keyword = "식당";
        else if(keyword.contains("음식점")) keyword = "음식점";
        else if(keyword.contains("한식")) keyword = "한식";
        else if(keyword.contains("일식")) keyword = "일식";
        else if(keyword.contains("중식")) keyword = "중국집";
        else if(keyword.contains("고기")) keyword = "고기";
        else if(keyword.contains("한정식")) keyword = "한정식";
        else if(keyword.contains("레스토랑")) keyword = "레스토랑";
        else if(keyword.contains("파스타")) keyword = "파스타";
        else if(keyword.contains("술집")) keyword = "술집";
        else if(keyword.contains("카페")) keyword = "카페";
        else if(keyword.contains("고기집")) keyword = "고기집";
        else if(keyword.contains("삼겹살")) keyword = "삼겹살";
        else if(keyword.contains("한우")) keyword = "한우";
        else if(keyword.contains("중국")) keyword = "중국집";
        else if(keyword.contains("해장국")) keyword = "해장국";
        else keyword ="맛집";
        keyword = referencePlace + keyword;
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

            StringBuilder resultMessage = new StringBuilder(keyword.replace("현대프리미엄아울렛 송도점","송현아")+"!!\n");
            Collections.shuffle(list);
            for(int i=0 ; i < list.size() ; i++){
                resultMessage.append((i+1) +"."+list.get(i)+"\n");
                if(i>1) break;
            }
            resultMessage.append("도우미가 추천해요!^^");
            if(new Random().nextInt(10)<1) {
                String t2 = "[종류]음식점, 한식, 일식, 고기, 한정식, 레스토랑, 파스타, 술집, 카페, 고기집, 삼겹살, 한우, 중국집, 해장국\n------\n";
                t2 += "[종류] 맛집 추천\n으로 물어보시면 더 상세히 추천드릴게요!";
                return new String[]{resultMessage.toString(), t2};
            }
            else return new String[]{resultMessage.toString()};

        }catch(Exception e){e.printStackTrace();}
        return null;
    }



}
