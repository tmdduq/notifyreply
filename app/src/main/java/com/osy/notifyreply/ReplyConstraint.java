package com.osy.notifyreply;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.os.Environment;
import android.renderscript.ScriptGroup;
import android.util.Log;

import com.osy.callapi.AnalysCGV;
import com.osy.callapi.ApiCoin;
import com.osy.callapi.ApiCorona;
import com.osy.callapi.ApiDict;
import com.osy.callapi.ApiKMA;
import com.osy.callapi.ApiSellApart;
import com.osy.callapi.RssNews;
import com.osy.callapi.RssTopSearch;
import com.osy.roledb.RoleDB;
import com.osy.utility.DataRoom;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ReplyConstraint {
    final String TAG = "ReplyConstraint";
    Context context = null;
    private static ReplyConstraint instance = null;
    private RoleDB roleDB=null;
    protected Map<String, Boolean> isOperation;
    ArrayList<DataRoom<DataRoom<String>>> roomNodes;

    Map<String, String> dailyNews;
    Map<String, String> beforeConsonantGame;
    Map<String, Map<String, Integer> > personAndScore;
    ArrayList<String> allQuestion;

    public static ReplyConstraint getInstance(){
        if(instance ==null)
            instance = new ReplyConstraint();
        return instance;
    }
    public void setContext(Context context){
        this.context = context;
    }


    ReplyConstraint() {
        isOperation = new HashMap<String, Boolean>();
        dailyNews = new HashMap<String,String>();
        beforeConsonantGame = new HashMap<String,String>();
        personAndScore = new HashMap<String, Map<String, Integer> >();
        allQuestion =null;
    }

    public String[] checkKeyword(String sender, String room, String keyword){
        if(room.length() > 27) room = room.substring(0,27);
        try {
            if (keyword.length() > 120) keyword = keyword.substring(0, 120);
        }catch (Exception e){return null;}

        String[] reply  ;

        reply = ifOnOff(room, keyword);
        if(reply != null) return reply;
        if(!isOperation.get(room)) return null;

        reply = ifMatchExpletiveKeyword(room,keyword);
        if(reply != null) return reply;

        reply = ifDeleteKey(room,keyword);
        if(reply != null) return reply;

        reply = ifShowReplyList(room, keyword);
        if(reply != null) return reply;

        reply = ifEducateKeyword(room,keyword);
        if(reply != null) return reply;

        reply = ifApiQuestion(room,keyword,context);
        if(reply != null) return reply;

        reply = ifConsonantGame(sender, room,keyword);
        if(reply != null) return reply;

        reply = ifContainsKeyword(room,keyword);
        if(reply != null) return reply;

        reply = subscriptionDailyNews(room,keyword);
        if(reply != null) return reply;

        reply = ifLotto(room,keyword);
        if(reply != null) return reply;

        reply = specialKeyword(keyword);
        if(reply != null) return reply;

        return null;
    }

    public void showNodes(){
        Log.i(TAG,"SHOW all Node");
        for( DataRoom<DataRoom<String>> s :roomNodes) {
            for (DataRoom<String> t : s.dataList){
                StringBuilder dataList= new StringBuilder("\n");
                dataList.append("room("+s.label+")"+"-key("+t.label+") - ");
                dataList.append("value ("+t.dataList.size()+"set) - ");
                for(String c : t.dataList){
                    dataList.append(c+" / ");
                }
                Log.i(TAG,dataList.toString());
            }
        }
    }

    public void setInitialize(Context context){
        if(roleDB==null)
            roleDB = new RoleDB(context,"roleList",null,4);
        Cursor cursor = roleDB.getContainsKeyList(null); // contains
        roomNodes = new ArrayList<DataRoom<DataRoom<String>>>();
        while(cursor.moveToNext()) {
            String room = cursor.getString(0);
            String key = cursor.getString(1);
            String value = cursor.getString(2);
            Log.i(TAG, "setInstance readRole(r/k/v): "+room+"/"+key+"/"+value);
            roomNodes = new ReplyFunction().setKeyList(roomNodes, room , key, value);
        }

        //        showNodes();
    }

    public String[] ifShowReplyList(String room,String keyword){
        Log.i(TAG, "ifShowReplyList room/keyword : " + room +"/"+keyword);
        if(keyword.startsWith("학습목록보기") ||keyword.startsWith("ㅎㅅㅁㄹㅂㄱ")|| keyword.startsWith("ㅎㅅㅁㄼㄱ")   ) {
            Cursor cursor;
            StringBuilder sb = new StringBuilder("");
            if(keyword.matches("학습목록보기 전체다")) {
                cursor = roleDB.getContainsKeyList(null);
                while (cursor.moveToNext()) {
                    String key = cursor.getString(1);
                    room = cursor.getString(0);
                    String value = cursor.getString(2);
                    int num = cursor.getInt(3);
                    sb.append(num + "/" +room +"/"+ key + "/" + value + "\n");
                }
            }
            else {
                cursor = roleDB.getContainsKeyList(room);
                while (cursor.moveToNext()) {
                    String key = cursor.getString(1);
                    String value = cursor.getString(2);
                    int num = cursor.getInt(3);
                    sb.append(num + "/" + key + "/" + value + "\n");
                }
            }
            return new String[]{sb.toString()};
        }

        return null;
    }

    public boolean addContainsKeyword(String room, String key, String value){
        if(room.length() > 27) room = room.substring(0,27);
        if(key.length() > 18) key = key.substring(0,18);
        if(value.length() > 90) value= value.substring(0,90);

        roomNodes = new ReplyFunction().setKeyList(roomNodes, room, key, value);
        roleDB.insertContainKeyword(room, key, value);
        setInitialize(context);
        return true;
    }
    public String[] ifLotto(String room, String keyword) {
        Log.i(TAG, "ifLotto room/keyword : " + room +"/"+keyword);
        if(!keyword.contains("로또") || !keyword.contains("추천")) return null;
        Random r = new Random();
        r.setSeed(System.currentTimeMillis());
        int[] number = new int[7];
        for(int i = 0 ; i < 7 ; i++){
            number[i] = r.nextInt(45)+1;
            for(int j = 0 ; j < i ; j++)
                if(number[i] == number[j]) i--;
        }
        String[] ment = new String[]{
                "APQ8096,4GB LPDDR SDRAM,32GB EMMC MEM 제가 가진 모든 성능을 동원해서 분석해봤어요\n",
                "하늘과 우주의 기운 33,899,148,592가지 모아 종합분석해봤어요.\n",
                "아..그냥 이걸로 사세요\n",
                "지난 회차의 모든 로또번호를 분석해봤어요. 이번 회차는 이 번호가 확실해요.\n"};
        String t = ment[r.nextInt(ment.length)];
        for(int i =0 ; i<6 ; i++) t += number[i]+", " ;
        t += "보너스 "+ number[6];
        Log.i(TAG,"LOTTO : "+t) ;
        return new String[]{t};
    }

    public String[] ifOnOff(String room, String keyword){
        Log.i(TAG, "ifOnOff room/keyword : " + room +"/"+keyword);
        if(isOperation.get(room)==null){
            roomNodes = new ReplyFunction().setKeyList(roomNodes, room,"안녕","안녕하세요");
        }
        if(keyword.matches("이제그만") && isOperation.get(room)) {
            isOperation.replace(room,false);
            return new String[]{"채팅을 중지할게요."};
        }
        else if(keyword.matches("다시작동") && !isOperation.get(room)) {
            isOperation.replace(room,true);
            return new String[]{"채팅을 시작할게요."};
        }
        return null;
    }
    public String[] subscriptionDailyNews(String room, String keyword) {
        Log.i(TAG, "subscriptionDailyNews room/keyword : " + room +"/"+keyword);
        if(!keyword.startsWith("데일리뉴스")) return null;
        String s = dailyNews.get(room);
        Calendar cal = Calendar.getInstance();

        if(s==null || keyword.matches("데일리뉴스 구독")) {
            String day = cal.get(Calendar.MONTH)+ "" + cal.get(Calendar.DATE);
            dailyNews.put(room, day);
            return new String[]{"매일 아침 뉴스를 보내드려요!"};
        }
        if(keyword.endsWith("취소")){
            dailyNews.remove(room);
            return new String[]{"아침 뉴스 안할게요!"};
        }
        else if(keyword.matches("데일리뉴스 구독@!#") || keyword.contains("데일리뉴스 보기")){
            return new RssNews().getNews();
        }

        return null;
    }

    public String[] ifDeleteKey(String room, String keyword){
        if(keyword.startsWith("학습목록삭제") || keyword.startsWith("ㅎㅅㅁㄹㅅㅈ") || keyword.startsWith("ㅎㅅㅁㄽㅈ")) {
            try {
                String[] del = keyword.split(" ");
                int i = Integer.parseInt(del[1]);
                boolean b = roleDB.deleteContainKey(room, i);
                if (b) {
                    setInitialize(context);
                    return new String[]{"제거했어요!"};
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }


    public String[] ifContainsKeyword(String room, String keyword){
        Log.i(TAG, "ifContainsKeyword room/keyword : "+room+"/"+keyword);

        for(DataRoom<DataRoom<String>> roomNode : roomNodes)
            if(roomNode.label.matches(room))
                for(DataRoom<String> keyNode : roomNode.dataList) {

                    String[] t = new String[]{keyNode.label};
                    if(keyNode.label.contains(" ")) t = keyNode.label.split(" ");

                    for(int i  = 0 ; i< t.length ; i++) {
                        if (!keyword.contains(t[i])) break;
                        if( i+1 == t.length) return new String[]{keyNode.dataList.get(new Random().nextInt(keyNode.dataList.size()))};
                    }

                }
        return null;
    }

    public String[] ifApiQuestion(String room, String keyword, Context context){
        Log.i(TAG, "ifApiQuestion room/keyword : "+room+"/"+keyword);
        if(keyword.contains(" 날씨")) {
            if(keyword.contains("오늘 날씨")) keyword = "연수구 송도1동 날씨";
            String[] addr = keyword.split(" ");
            try {
                if (addr[2].contains("날씨"))
                    return new String[]{new ApiKMA(context).getWeather(addr[0], addr[1])};
            } catch (Exception e) {
                e.printStackTrace();
                String[] t =  new String[1];
                t[0] = "날씨가 궁금하면 아래와 같이 검색해보세요!\n" +
                        "[구] [동] 날씨\n " +
                        "예시)부평구 삼산1동 날씨";
                return t;
            }
            return null;
        }
        if(keyword.contains("코로나")){
            return new String[]{new ApiCorona(context).getNationalCorona()};
        }
        if(keyword.contains(" 실거래가")){
            try {
                String addr = keyword.substring(0, keyword.indexOf("실거래가")-1);
                return new String[]{new ApiSellApart(context).getPrice(addr)};
            } catch (Exception e) {
                e.printStackTrace();
                String[] t = new String[]{"부동산 실거래가기 궁금하면 아래와 같이 검색해보세요!\n" +
                        "[법정동] 실거래가\n" +
                        "예시1)인천광역시 연수구 실거래가\n" +
                        "예시2)경기도 성남시 분당구 실거래가\n" +
                        "예시3)서울특별시 송파구 실거래가"};
                return t;
            }
        }
        if(keyword.contains("시세")){
            try{
                String coinName = keyword.substring(0, keyword.indexOf("시세")).trim();
                String t;
                if(coinName.matches("모든코인"))
                    t = new ApiCoin().getAllPrice();
                else
                    t = new ApiCoin().getPrice(coinName);

                if(t==null && keyword.contains("코인")){
                    t = "코인 시세가 궁금하면 아래와 같이 검색해보세요!\n" +
                            "[코인이름] 시세\n" +
                            "예시1)이더리움 클래식 시세\n" +
                            "예시2)ETH 시세\n" +
                            "예시3)모든코인 시세";
                }
                return new String[]{t};
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        /*Stream API 연습
        IntStream.range(1, 11 ).filter(i-> i%2==0).forEach(System.out::println);
        String keyword2[] = new String[]{"asdad"};
        IntStream.range(0,words.length).filter(i -> keyword2[0].contains(words[i])).findFirst();
        람다식 API 연습
        String[] ss = new String[]{"가 뭐야","이 뭐야","은 뭐야","는 뭐야","가 뭐냐","이 뭐냐","은 뭐냐","는 뭐냐"};
        Arrays.asList(ss).forEach(s -> {
            if(k.contains(s))
            k.substring(0, k.lastIndexOf("뭐")-2);
        });*/
        for( String w : new String[]{"가 뭐야","이 뭐야","은 뭐야","는 뭐야","가 뭐냐","이 뭐냐","은 뭐냐","는 뭐냐"})
            if(keyword.contains(w)){
            keyword = keyword.substring(0, keyword.lastIndexOf("뭐")-2);
            return new String[]{new ApiDict(context).searchDictionary(keyword)};
        }
        if(keyword.contains("검색어") && (keyword.contains("실시간") || keyword.contains("인기")) )
            return new String[]{new RssTopSearch().getTopSearchKeyword()};


        if( (keyword.contains("CGV") ||  keyword.contains("cgv"))){
            Calendar cal = Calendar.getInstance();
            if(keyword.contains("내일"))
                cal.add(Calendar.DATE,1);
            else if(keyword.contains("토요일"))
                cal.add(Calendar.DATE,7- cal.get(Calendar.DAY_OF_WEEK));
            else if(keyword.contains("일요일"))
                cal.add(Calendar.DATE,(8-cal.get(Calendar.DAY_OF_WEEK))%7 );
            else if(keyword.contains("월요일"))
                cal.add(Calendar.DATE,(9-cal.get(Calendar.DAY_OF_WEEK)) %7 );
            else if(keyword.contains("화요일"))
                cal.add(Calendar.DATE,(10-cal.get(Calendar.DAY_OF_WEEK)) %7 );
            else if(keyword.contains("수요일"))
                cal.add(Calendar.DATE,(11-cal.get(Calendar.DAY_OF_WEEK)) %7 );
            else if(keyword.contains("목요일"))
                cal.add(Calendar.DATE,(12-cal.get(Calendar.DAY_OF_WEEK)) %7 );
            else if(keyword.contains("금요일"))
                cal.add(Calendar.DATE,(13-cal.get(Calendar.DAY_OF_WEEK)) %7 );

            String month = ""+ (cal.get(Calendar.MONTH)+1 <10 ? "0"+(cal.get(Calendar.MONTH)+1) : cal.get(Calendar.MONTH)+1);
            String date = ""+ (cal.get(Calendar.DATE) <10 ? "0"+cal.get(Calendar.DATE) : cal.get(Calendar.DATE));
            if(keyword.contains("시간표"))
                return new String[]{new AnalysCGV().cgvTimeTable(cal.get(Calendar.YEAR)+month+date,keyword)};
            else {
                String[] t = new String[2];
                t[0] = new AnalysCGV().cgvRunningMoive(cal.get(Calendar.YEAR) + month + date);
                t[1] = "상영시간표도 알려드려요!\nCGV 시간표 [영화제목]";
                return t;
            }
        }


        return null;
    }
    public String[] ifMatchExpletiveKeyword(String room, String keyword){
//        for(String s : expletiveKeyword)
//         if (keyword.contains(s)) return "욕하지마세요ㅜ_ㅜ";
        return null;
    }

    public String[] ifEducateKeyword(String room, String str){
        String t = "말을 가르치고 싶으세요?\n" +
                "학습하기+키워드+대답 을 입력해보세요!\n" +
                "예시) 학습하기+배고파+밥먹어";
        if(str.startsWith("학습하기") ||str.startsWith("ㅎㅅㅎㄱ"))
            try {
                String[] s = str.split("\\+");
                String keyword = s[1];
                String value = s[2];
                boolean b = addContainsKeyword(room, keyword, value);
                if(b) t = "["+keyword+"]" +"를 ["+value+"]으로 배웠어요";
                return new String[]{t};
            }catch(Exception e){
                return new String[]{t};
            }
        if(str.contains("학습하기"))
            return new String[]{t};
        return null;
    }
    public String[] specialKeyword(String keyword){
        Log.i(TAG, "specialKeyword keyword : " +"/"+keyword);
        if( !keyword.contains("도움말") || !keyword.contains("봇")) return null;
        String[] t = new String[]{
                "[1]코로나 현황 확인\n" +
                        "-> 코로나",
                "[2]국어사전 검색\n" +
                        "-> [검색어]가 뭐야",
                "[3]날씨 확인\n" +
                        "-> 오늘 날씨\n" +
                        "-> [구] [동] 날씨\n" +
                        "ex) 연수구 송도1동 날씨",
                "[4]인기 검색어 확인\n" +
                        "-> 실시간 검색어\n" +
                        "-> 인기 검색어",
                "[5]타임스페이스CGV 확인\n" +
                        "-> CGV\n" +
                        "-> CGV [제목] 시간표\n" +
                        "ex) CGV 인셉션 시간표\n" +
                        "ex) CGV 내일 인셉션 시간표",
                "[6]실시간 코인 가격\n" +
                        "-> [코인명] 시세\n" +
                        "ex) 이더리움 시세\n" +
                        "-> [코인약어] 시세\n" +
                        "ex) ETH 시세\n" +
                        "-> 모든코인 시세",
                "[6]매일 아침 뉴스 받기\n" +
                        "-> 데일리뉴스 구독\n" +
                        "-> 데일리뉴스 구독 취소",
                "[7]롤 초성퀴즈 풀기\n" +
                        "-> 퀴즈 시작\n" +
                        "-> 퀴즈 중지\n" +
                        "-> 힌트",
                "[8]부동산 실거래가 확인\n" +
                        "-> [시] [구] 실거래가\n" +
                        "ex) 인천광역시 연수구 실거래가",
                "[9]로또번호 추천\n" +
                        "-> 로또 추천",
                "[10]말 가르치기\n" +
                        "-> 학습하기+[키워드]+[대답]\n" +
                        "ex) 학습하기+배고파+밥먹어라",
                "[11]학습한 내용보기/삭제\n" +
                        "-> 학습목록보기\n" +
                        "-> 학습목록삭제 39"
        };

        return t;
    }

    public String[] ifConsonantGame(String sender, String room, String str){
        Log.i(TAG, "ifConsonantGame room/keyword : "+room+"/"+str);
        String answar = beforeConsonantGame.get(room);

        if(answar!=null) {
            if (str.matches(answar)) { // SCORE +
                if (personAndScore.get(room) == null) {
                    personAndScore.put(room, new HashMap<String, Integer>() );
                    personAndScore.get(room).put(sender, 1);
                }
                else if(personAndScore.get(room).get(sender) == null)
                    personAndScore.get(room).put(sender, 1);
                else
                    personAndScore.get(room).put(sender, personAndScore.get(room).get(sender) + 1);
                beforeConsonantGame.remove(room);
                String displySender = sender.contains("/") ?  sender.substring(0,sender.indexOf("/")) : sender;
                return new String[]{displySender + "님 " + personAndScore.get(room).get(sender) + "점!\n" + answar + " 정답이에요!"};
            }
            else if (  str.contains("퀴즈") && (str.contains("포기") || str.contains("그만") || str.contains("중지"))) {
                beforeConsonantGame.remove(room);
                return new String[]{"퀴즈를 포기했어요.ㅠ\n정답은 " + answar+"이였어요..\n계속하려면 [퀴즈시작]를 외쳐주세요!"};
            }
            else if(str.contains("힌트")){
                String question= new ReplyFunction().consonant(answar);
                String result = "힌트! ";
                for(int i =0 ; i< answar.length() ; i++)
                    result += (new Random().nextBoolean() ? question.charAt(i) : answar.charAt(i) );
                return new String[]{result};
            }
        }

        if(str.contains("퀴즈") && str.contains("시작")) {
            if(answar != null)
                return new String[]{"지난퀴즈 정답자가 없어요.\n주제는 롤 스킨! 맞춰보세요!\n초성: " + new ReplyFunction().consonant(answar)};

            try {
                String question;
                if(allQuestion==null) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(context.getAssets().open("consonantGame_LoL2", AssetManager.ACCESS_BUFFER)));
                    allQuestion = new ArrayList<String>();
                    while (null != (question = br.readLine())) allQuestion.add(question);
                }
                question = allQuestion.get(new Random().nextInt(allQuestion.size()));
                beforeConsonantGame.put(room, question);
                if(str.matches("퀴즈시작이어가기"))
                    return new String[]{"다음 문제에요!\n초성: " + new ReplyFunction().consonant(question)};
                else
                    return new String[]{" 주제 롤스킨! 맞춰보세요!\n초성: " + new ReplyFunction().consonant(question)};
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(str.contains("점수") && (str.contains("확인") || str.contains("보기")) ){
            StringBuilder sb = new StringBuilder("");
            Map<String, Integer> m = personAndScore.get(room);
            if(m==null) return null;
            List<Map.Entry<String, Integer>> list = new ArrayList<>(m.entrySet());
            list.sort(Map.Entry.comparingByValue());
            for(int i = 0 ; i < list.size() ; i++) {
                Map.Entry<String, Integer> v = list.get( list.size()-1-i );
                String person = v.getKey().length()>4 ? (v.getKey().substring(0,4)+"..") : v.getKey();
                sb.append((i+1)+"위 "+person + "님 (+" + v.getValue() + ")\n");
                if(i==4){ break;}
            }
            sb.append("정답자는 총 "+list.size()+"명이에요.");
            return new String[]{sb.toString()};
        }

        if(str.contains("문제추가 ")) {
            try {
                allQuestion.add(str.substring(5));
                return new String[]{str.substring(5) + new ReplyFunction().consonant(str.substring(5)) + " 추가했어요\n" +
                        "문제는 총 "+allQuestion.size()+"개에요."};
            }catch (Exception e){ }
        }


        return null;
    }


}
