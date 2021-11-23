package com.osy.callapi;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class AnalysConvensia {
    final String TAG = "AnalysConvensia";

    public AnalysConvensia(){
        Log.i(TAG, "Class on - "+TAG);
    }
    public String getConvensia(String keyword){

        ArrayList<Integer> targetDays = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        int today = c.get(Calendar.DAY_OF_MONTH);
        String yyyyMM;
        try{
            if(keyword.contains("내일")){
                c.add(Calendar.DAY_OF_MONTH,1);
                targetDays.add(c.get(Calendar.DAY_OF_MONTH));
            }
            keyword = keyword.replaceAll("[^0-9 ]","");
            String[] d = keyword.split(" ");
            for(String dd : d)
                targetDays.add(Integer.parseInt(dd));
        }catch(Exception e){ e.printStackTrace(); }

        if(targetDays.size()==0) targetDays.add(today);

        try {
            StringBuilder sb = new StringBuilder();
            for(int n = 0 ; n < targetDays.size() ; n++) {
                if(n>0) break; ///// 너무 길어서 보기 싫음..
                int targetDay = targetDays.get(n);
                c = Calendar.getInstance();
                if(targetDay < today) c.add(Calendar.MONTH,1);
                yyyyMM = new SimpleDateFormat("yyyyMM").format(c.getTime());;

                String urlString = "http://songdoconvensia.visitincheon.or.kr/sch/organizer/scinfo/event/UI-SC-0101-001Q.do?eventDivH=ALL&menuDiv=10&YYYYMM=" + yyyyMM;
                Document document = Jsoup.connect(urlString).get();
                Elements elements = document.select("div.contents div.con_calendar td.txt_event");
                elements.add(Calendar.getInstance().get(Calendar.DAY_OF_MONTH) - 1, document.select("div.contents div.con_calendar td.txt_today").first());
//			select문법(예시) : tag (div) // #id (div#wrap, #logo) // .class(div.left, .result) // [attr] (a[href], [title])

                Element element = elements.get(targetDay - 1);
                Elements dayElements = element.getElementsByTag("a");
                sb.append((c.get(Calendar.MONTH)+ 1) + "월" + element.select("strong").first().text() + "일은 ");
                if (dayElements.isEmpty()) sb.append("컨벤시아 행사가 없어요.\n");
                else sb.append(dayElements.size() + "개 행사가 있어요.\n");

                for (int i = 0; i < dayElements.size(); i++)
                    sb.append("＊ " + dayElements.get(i).text() + "\n");
            }
            if(sb.length()>30) sb.append("즐거운 컨벤시아 관람되세요!");
            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
