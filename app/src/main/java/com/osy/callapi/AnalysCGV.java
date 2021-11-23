package com.osy.callapi;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class AnalysCGV {
    final String TAG = "AnalysCGV";
    public ArrayList<String> movieName= new ArrayList<String>();

    public AnalysCGV(){
        Log.i(TAG, "Class on - "+TAG);
    }

    public String cgvRunningMoive(String targetDate){
        String urlString = "http://www.cgv.co.kr/common/showtimes/iframeTheater.aspx?areacode=202&theatercode=0325&date="+targetDate;
        try {
            // URL url = new URL(urlString);
            // InputStream is = url.openStream();
            HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393");
            conn.connect();
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            //  BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));
            StringBuilder sb = new StringBuilder("타임스페이스 CGV 상영영화!\n");
            String s= br.readLine();
            while(!s.contains("sect-showtimes")) s = br.readLine();

            String subject = null;
            String genre = null;
            String runningTime = null;
            String releaseDay = null;
            int count=1;
            while(!s.contains("info-noti")) {
                if(s.contains("</strong>"))
                    subject = s.split("</strong>")[0].trim();
                else if(s.contains("</span><i>")) {
                    genre = br.readLine().split("</i>")[0].trim().replaceAll("&nbsp;", "");
                    sb.append("["+count+++"]"+subject+  " ("+genre+")"+ "\n");
                }
                s = br.readLine();
            }
            sb.append(targetDate.substring(4,6)+"월"+targetDate.substring(6,8)+"일 "+(count-1)+"개 상영중!");
            return sb.toString();

        }catch(Exception e) {e.printStackTrace();}
        return null;
    }


    public String cgvTimeTable(String targetDate, String movie){
        if(movie ==null) movie= "";
        String urlString = "http://www.cgv.co.kr/common/showtimes/iframeTheater.aspx?areacode=202&theatercode=0325&date="+targetDate;
        int idx_st = -1;
        try {
           // URL url = new URL(urlString);
           // InputStream is = url.openStream();
            HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393");
            conn.connect();
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
          //  BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));
            StringBuilder sb = new StringBuilder("");
            String s= br.readLine();
            while(!s.contains("sect-showtimes")) s = br.readLine();

            String subject = null;
            String genre = null;
            String runningTime = null;
            String releaseDay = null;
            String onTime = null;
            String room = null;
            String allSeat = null;
            String remainSeat = null;
            int count = 0;
            boolean isSame= false;
            while(!s.contains("info-noti")) {
                if(s.contains("</strong>")) {
                    subject = s.split("</strong>")[0].trim();
                }
                else if(s.contains("</span><i>")) {
                    genre = br.readLine().split("</i>")[0].trim().replaceAll("&nbsp;", "");
                    runningTime = br.readLine().split("</i>")[0].trim();
                    releaseDay = br.readLine().trim();
                    isSame=true;
                }
                else if(s.contains("<div class=\"info-hall\">")){
                    br.readLine();br.readLine();br.readLine();br.readLine();
                    s = br.readLine();
                    room = s.split("<")[0].trim();
                    br.readLine();
                    s = br.readLine();
                    allSeat = s.split("<")[0].trim();
                }

                else if(s.contains("잔여좌석")) {
                    if(idx_st < 0) idx_st = s.indexOf("data-playstarttime=")+20;
                    onTime = s.substring(idx_st,idx_st+4) +"~"+s.substring(idx_st+24,idx_st+28);
                    if(room.contains("리클")) room="리클";
                    else room="";
                    remainSeat = s.split("석</span>")[1].trim();
                    if(isSame) {
                        try{
                            String[] key = movie.split(" ",-1);
                            boolean t = false;
                            for(int i=0 ; i<key.length ; i++)
                                if (subject.contains(key[i])){
                                    t = true;
                                    count++;
                                }
                            if(!t){
                                s = br.readLine();
                                continue;
                            }
                        }catch(Exception e){e.printStackTrace();}
                        sb.append("-------------"+"\n");
                        sb.append(subject+  " ("+genre+")"+ "\n");
                        sb.append(releaseDay+" 개봉\n");
                        isSame = false;
                    }
                    sb.append(""+onTime+" "+room +"("+remainSeat+"/"+allSeat+")"+"\n");
                }
                s = br.readLine();
            }
            if(count==0) return null;
            sb.insert(0, targetDate.substring(4,6)+"월"+targetDate.substring(6,8)+"일\n"+"타임스페이스 CGV 상영정보에요!\n");
            //System.out.println(sb.toString());
            return sb.toString();

        }catch(Exception e) {e.printStackTrace();}
        return null;
    }

}
