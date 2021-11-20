package com.osy.notifyreply;

import android.app.Notification;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.os.Environment;
import android.renderscript.ScriptGroup;
import android.util.Log;

import com.osy.callapi.AnalysCGV;
import com.osy.callapi.ApiApplyhome;
import com.osy.callapi.ApiCoin;
import com.osy.callapi.ApiCorona;
import com.osy.callapi.ApiDict;
import com.osy.callapi.ApiKMA;
import com.osy.callapi.ApiSellApart;
import com.osy.callapi.ApiStock;
import com.osy.callapi.RssNews;
import com.osy.callapi.RssTopSearch;
import com.osy.roledb.RoleDB;
import com.osy.utility.DataRoom;
import com.osy.utility.LastTalk;

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

    public Map<String, LastTalk> lt = new HashMap<>();

    Map<String, String> topicChecker;
    Map<String, Map<String, Integer> > personAndScore;

    public static ReplyConstraint getInstance(){
        if(instance ==null) instance = new ReplyConstraint();
        return instance;
    }
    public void setContext(Context context){
        this.context = context;
    }


    ReplyConstraint() {
        isOperation = new HashMap<String, Boolean>();
        topicChecker = new HashMap<String,String>();
        personAndScore = new HashMap<String, Map<String, Integer> >();
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
            roomNodes = new ReplyFunction().setKeyList(roomNodes, room,"하이","하이하이^-^");
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
        if(!keyword.startsWith("뉴스")) return null;
        Calendar cal = Calendar.getInstance();

        if(keyword.matches("뉴스 구독")) {
            String day = cal.get(Calendar.MONTH)+ "" + cal.get(Calendar.DATE);
            topicChecker.put("subscriptionDailyNews"+room, day);
            return new String[]{"매일 아침 뉴스를 보내드려요!"};
        }
        if(keyword.endsWith("취소")){
            topicChecker.remove("subscriptionDailyNews"+room);
            return new String[]{"아침 뉴스 안할게요!"};
        }
        else if(keyword.contains("보기")){
            if(keyword.contains("연합")) return new RssNews().getNews(RssNews.YONHAP);
            if(keyword.contains("중앙")) return new RssNews().getNews(RssNews.JOONGANG);
            if(keyword.contains("JTBC") || keyword.contains("jtbc")) return new RssNews().getNews(RssNews.JTBC);
            if(keyword.contains("연합")) return new RssNews().getNews(RssNews.SBS);
            return new RssNews().getNews(new Random().nextInt(4));
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
        if(keyword.contains("코로나") && ( keyword.contains("확진") || keyword.contains("현재") || keyword.contains("현황") ||keyword.contains("명"))){
            String re = new ApiCorona(context).getNationalCorona();
            if(re !=null)
                return new String[]{new ApiCorona(context).getNationalCorona(),"다들 코로나 조심하세요."};
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
                if((coinName.contains("모든") || coinName.contains("전체")) && coinName.contains("업비트"))
                    t = new ApiCoin().getAllPrice_UPBIT();
                else if((coinName.contains("모든") || coinName.contains("전체"))&& ( coinName.contains("빗썸") ||coinName.contains("빗섬")) )
                    t = new ApiCoin().getAllPrice_BITHUMB();
                else
                    t = new ApiCoin().getPrice(coinName);

                if(t==null) return null;
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
        if(keyword.contains("청약") && (keyword.contains("정보")|| keyword.contains("접수"))){
            String[] t = new ApiApplyhome(context).getApplyhome(keyword);
            if(t != null) return t;
        }

        if(keyword.contains("주식") || keyword.contains("주가")){
            String t = new ApiStock(context).getStocks(keyword);
            if(t !=null) return new String[]{t};
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

        if(keyword.contains("11")){
//            Log.i(TAG, "@@@@@@@@@@"+roleDB.createConsonantQuiz("consonantGame_Drama",roleDB.consonantQuiz_Drama));
//            Log.i(TAG, "@@@@@@@@@@"+roleDB.createConsonantQuiz("consonantGame_Movie",roleDB.consonantQuiz_Movie));
            return null;
        }
        int seq = 1;
        String[] t = new String[]{
                seq+++">코로나 현황 확인\n" +
                        "ex) 코로나",
                seq+++">국어사전 검색\n" +
                        "-> [검색어]가 뭐야" +
                        "ex) 나무가 뭐야",
                seq+++">날씨 확인\n" +
                        "-> 오늘 날씨\n" +
                        "-> [구] [동] 날씨\n" +
                        "ex) 연수구 송도1동 날씨",
                seq+++">인기 검색어 확인\n" +
                        "ex) 실시간 검색어\n" +
                        "ex) 인기 검색어",
                seq+++">타임스페이스CGV 확인\n" +
                        "ex) CGV\n" +
                        "ex) 내일 CGV\n" +
                        "ex) 금요일 CGV\n" +
                        "-> CGV [제목] 시간표\n" +
                        "ex) CGV 인셉션 시간표\n" +
                        "ex) CGV 토요일 인셉션 시간표\n" +
                        "ex) CGV 내일 인셉션 시간표",
                seq+++">실시간 코인 가격\n" +
                        "-> [코인명] 시세\n" +
                        "ex) 이더리움 시세\n" +
                        "ex) ETH 시세\n" +
                        "ex) 업비트 전체 시세",
                seq+++">뉴스 확인\n" +
                        "ex) 데일리뉴스 보기\n" +
                        "ex) 데일리뉴스 구독\n" +
                        "ex) 데일리뉴스 구독 취소",
                seq+++">롤 초성퀴즈 풀기\n" +
                        "ex) 퀴즈 시작\n" +
                        "ex) 퀴즈 중지\n" +
                        "ex) 힌트",
                seq+++">부동산 실거래가 확인\n" +
                        "-> [시] [구] 실거래가\n" +
                        "ex) 인천광역시 연수구 실거래가",
                seq+++">청약 정보 확인\n" +
                        "ex) 청약정보\n" +
                        "ex) 청약정보 특별공급\n" +
                        "ex) 청약정보 2순위",
                seq+++">로또번호 추천\n" +
                        "ex) 로또 추천",
                seq+++">말 가르치기\n" +
                        "-> 학습하기+[키워드]+[대답]\n" +
                        "ex) 학습하기+배고파+밥먹어라",
                seq+++">학습한 내용보기/삭제\n" +
                        "-> 학습목록보기\n" +
                        "-> 학습목록삭제 39",
                seq+++">봇 멈추기/재시작\n" +
                        "-> 이제 그만\n" +
                        "-> 다시 작동"
        };

        return t;
    }

    public String[] ifConsonantGame(String sender, String room, String str){
        Log.i(TAG, "ifConsonantGame room/keyword : "+room+"/"+str);
        String[] quizName = new String[]{"consonantQuiz_lol", "consonantQuiz_lol_skin","consonantQuiz_Drama", "consonantQuiz_Nation","consonantQuiz_Movie"};

        String quizRoom = "beforeConsonantGame"+room;
        String quizRoomType = quizRoom+"type";

        String answar = topicChecker.get(quizRoom);

        if(answar!=null) {
            if (str.matches(answar)) { // SCORE +
                if (personAndScore.get(room) == null) {
                    personAndScore.put(room, new HashMap<>() );
                    personAndScore.get(room).put(sender, 1);
                }
                else if(personAndScore.get(room).get(sender) == null)
                    personAndScore.get(room).put(sender, 1);
                else
                    personAndScore.get(room).put(sender, personAndScore.get(room).get(sender) + 1);
                topicChecker.remove(quizRoom);
                String displySender = sender.contains("/") ?  sender.substring(0,sender.indexOf("/")) : sender;

                String question = roleDB.getConsonantQuestion(topicChecker.get(quizRoomType));
                topicChecker.put(quizRoom, question );
                ArrayList<String> reply = new ArrayList<>();
                Random r = new Random();
                r.setSeed(System.currentTimeMillis());
                reply.add(displySender + "님 " + personAndScore.get(room).get(sender) + "점!\n" + answar + " 정답이에요!");
                if (r.nextInt(10) < 2) reply.add("와 잘하세요!");
                if (r.nextInt(50) < 2) reply.add("점수가 궁금하면 [점수 확인]!");
                if (r.nextInt(50) < 2) reply.add("그만하시려면 [퀴즈중지]!");
                if (r.nextInt(200) < 2) reply.add("문제를 추가하고 싶으세요?\n다음과 같이 적어보세요!\n ex)문제추가 도레미파솔");
                reply.add("다음 문제에요!\n초성: " + new ReplyFunction().consonant(question));
                String[] t = new String[reply.size()];
                reply.toArray(t);
                return t;
            }
            else if (  str.contains("퀴즈") && (str.contains("포기") || str.contains("그만") || str.contains("중지"))) {
                topicChecker.remove(quizRoom);
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

            topicChecker.put(quizRoomType, quizName[new Random().nextInt(quizName.length)] );   // 퀴즈주제 랜덤지정
            String question = roleDB.getConsonantQuestion(topicChecker.get(quizRoomType));  //퀴즈 랜덤 추출
            topicChecker.put(quizRoom, question);   // 해당 채팅방에 퀴즈 등록
            StringBuilder sb = new StringBuilder("주제는 [");
            if(topicChecker.get(quizRoomType)=="consonantQuiz_lol") sb.append("롤 관련");
            else if(topicChecker.get(quizRoomType)=="consonantQuiz_lol_skin") sb.append("롤 스킨");
            else if(topicChecker.get(quizRoomType)=="consonantQuiz_Drama") sb.append("드라마 제목");
            else if(topicChecker.get(quizRoomType)=="consonantQuiz_Nation") sb.append("나라 이름");
            else if(topicChecker.get(quizRoomType)=="consonantQuiz_Movie") sb.append("영화 제목");
            sb.append("]! 맞춰보세요!\n");
            sb.append("초성"+ new ReplyFunction().consonant(question));
            return new String[]{sb.toString()};
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
                if(i==4) break;
            }
            sb.append("정답자는 총 "+list.size()+"명이에요.");
            return new String[]{sb.toString()};
        }

        if(str.startsWith("문제추가 ")) {
            try {
                roleDB.putConsonantQuestion(topicChecker.get(quizRoomType), str.substring(5));
                return new String[]{new ReplyFunction().consonant(str.substring(5)) + " 추가했어요\n" +
                        "문제는 총 "+roleDB.getTableSize(topicChecker.get(quizRoomType))+"개에요."};
            }catch (Exception e){e.printStackTrace(); }
        }

        return null;

    }


}
