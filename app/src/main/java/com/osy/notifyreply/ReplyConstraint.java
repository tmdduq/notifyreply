package com.osy.notifyreply;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.osy.callapi.AnalysConvensia;
import com.osy.callapi.ApiChatGPT;
import com.osy.callapi.ApiCoin;
import com.osy.callapi.ApiDict;
import com.osy.callapi.ApiStock;
import com.osy.callapi.RssNews;
import com.osy.callapi.RssTopSearch;
import com.osy.callapi.AnalysPlace;
import com.osy.roledb.RoleDB;
import com.osy.utility.DataRoom;
import com.osy.utility.LastTalk;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class ReplyConstraint {
    final String TAG = "ReplyConstraint";
    Context context = null;
    private static ReplyConstraint instance = null;
    private RoleDB roleDB=null;
    protected Map<String, Boolean> isOperation;
    ArrayList<DataRoom<DataRoom<String>>> roomNodes;

    public Map<String, LastTalk> lastTalkMap;

    Map<String, String> topicChecker;
    Map<String, Map<String, Integer> > personAndScore;
    ArrayList<String> gptTalkList;

    public static ReplyConstraint getInstance(){
        if(instance ==null) instance = new ReplyConstraint();
        return instance;
    }
    public void setContext(Context context){
        this.context = context;
    }

    ReplyConstraint() {
        isOperation = new HashMap<String, Boolean>();   // key: roomName, value:작동/정지
        topicChecker = new HashMap<String,String>(); // key : roomName, value: 쪽지내용
        personAndScore = new HashMap<String, Map<String, Integer> >(); // key: roomName, value: Map<사람, 점수>
        lastTalkMap = new HashMap<>(); // key: roomName, value: LastTalk
        gptTalkList = new ArrayList<String>();  // 최근 5개 질문을 저장해서 연계질문이 가능하도록 하기 위함

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

        reply = ifMatchGPTKeyword(room,keyword);
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

    public String showNodes(){
        Log.i(TAG,"SHOW all Node");
        StringBuilder sb = new StringBuilder();
        for( DataRoom<DataRoom<String>> s :roomNodes) {
            for (DataRoom<String> t : s.dataList){
                StringBuilder dataList= new StringBuilder("\n");
                dataList.append(s.label+" / "+""+t.label+" / ");
                dataList.append("value ("+t.dataList.size()+"set) - ");
                for(String c : t.dataList){
                    dataList.append(c+" / ");
                }
                Log.i(TAG,dataList.toString());
                sb.append(dataList);
            }
        }
        return sb.toString();
    }
    // DB 재구성
    public void setInitialize(Context context){
        Log.i(TAG, "method on - setInitialize");
        if(roleDB==null)
            roleDB = new RoleDB(context,"roleList",null,4);
        Cursor cursor = roleDB.getContainsKeyList(null, null); // contains
        roomNodes = new ArrayList<DataRoom<DataRoom<String>>>();
        while(cursor.moveToNext()) {
            String room = cursor.getString(0);
            String key = cursor.getString(1);
            String value = cursor.getString(2);
//            Log.i(TAG, "setInstance readRole(r/k/v): "+room+"/"+key+"/"+value);
            roomNodes = new ReplyFunction().setKeyList(roomNodes, room , key, value);
        }
        //        showNodes();
    }

    // 학습목록보기
    public String[] ifShowReplyList(String room,String keyword){
        Log.i(TAG, "method on - ifShowReplyList");
        if(keyword.startsWith("학습목록보기") ||keyword.startsWith("ㅎㅅㅁㄹㅂㄱ")|| keyword.startsWith("ㅎㅅㅁㄼㄱ")   ) {
            Cursor cursor;
            StringBuilder sb = new StringBuilder("");
            if(keyword.matches("학습목록보기 전체다")) {
                cursor = roleDB.getContainsKeyList(null, null);
                while (cursor.moveToNext()) {
                    String key = cursor.getString(1);
                    room = cursor.getString(0);
                    String value = cursor.getString(2);
                    int num = cursor.getInt(3);
                    sb.append(num + "/" +room +"/"+ key + "/" + value + "\n");
                }
            }
            else {
                cursor = roleDB.getContainsKeyList(room,null);
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

    // reply 키워드 추가
    public boolean addContainsKeyword(String room, String key, String value){
        Log.i(TAG, "method on - addContainsKeyword");
        if(room.length() > 27) room = room.substring(0,27);
        if(key.length() > 18) key = key.substring(0,18);
        if(value.length() > 90) value= value.substring(0,90);

        roomNodes = new ReplyFunction().setKeyList(roomNodes, room, key, value);
        roleDB.insertContainKeyword(room, key, value, null);
        setInitialize(context);
        return true;
    }

    // 로또
    public String[] ifLotto(String room, String keyword) {
        Log.i(TAG, "method on - ifLotto");
        if(!keyword.contains("로또") || !keyword.contains("추천")) return null;
        Random r = new Random();
        r.setSeed(System.currentTimeMillis());
        int[] number = new int[7];
        for(int i = 0 ; i < 7 ; i++){
            number[i] = r.nextInt(45)+1;
            for(int j = 0 ; j < i ; j++)
                if(number[i] == number[j]) i--;
        }
        Arrays.sort(number);
        String[] ment = new String[]{
                "APQ8096,4GB,LPDDR,SDRAM,32GB,EMMC,MEM 제가 가진 모든 성능을 동원해서 분석해봤어요.\n",
                "하늘과 우주의 기운 33,899,148,592가지를 모아 분석해봤어요.\n",
                "아.. 귀찮은데 그냥 이걸로 사세요.\n",
                "지난 회차의 모든 로또번호를 분석해봤어요. 이번 회차는 이 번호가 확실해요.\n"};
        String t = ment[r.nextInt(ment.length)];
        for(int i =0 ; i<6 ; i++) t += number[i]+", " ;
        t += "보너스 "+ number[6];
        Log.i(TAG,"LOTTO : "+t) ;
        return new String[]{t};
    }

    // 멈추기/재시작하기
    public String[] ifOnOff(String room, String keyword){
        Log.i(TAG, "method on - ifOnOff");
        String[] rst = null;
        if(isOperation.get(room)==null)
            addContainsKeyword(room,"안녕","안녕하세요.^^");
        else if(keyword.matches("이제그만") && isOperation.get(room)) {
            isOperation.replace(room,false);
            rst =  new String[]{"채팅을 중지할게요."};
        }
        else if(keyword.matches("다시작동") && !isOperation.get(room)) {
            isOperation.replace(room,true);
            rst =  new String[]{"채팅을 시작할게요."};
        }
        return rst;
    }

    public String[] subscriptionDailyNews(String room, String keyword) {
        Log.i(TAG, "method on - subscriptionDailyNews");
        if(!keyword.contains("뉴스")) return null;
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
        else if(keyword.contains("보기") || keyword.contains("오늘")||keyword.contains("지금") ||keyword.contains("인기")){
            if(keyword.contains("연합")) return new RssNews().getNews(RssNews.YONHAP);
            if(keyword.contains("동아")) return new RssNews().getNews(RssNews.DONGA);
            if(keyword.contains("JTBC")) return new RssNews().getNews(RssNews.JTBC);
            if(keyword.contains("SBS")) return new RssNews().getNews(RssNews.SBS);
            return new RssNews().getNews(new Random().nextInt(4));
        }
        return null;
    }

    //학습목록 삭제
    public String[] ifDeleteKey(String room, String keyword){
        Log.i(TAG, "method on - ifDeleteKey");
        if(keyword.startsWith("학습목록삭제") || keyword.startsWith("ㅎㅅㅁㄹㅅㅈ") || keyword.startsWith("ㅎㅅㅁㄽㅈ")) {
            try {
                String[] del = keyword.split(" ");
                int i = Integer.parseInt(del[1]);
                boolean b = roleDB.deleteContainKey(room, i, null);
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

    //등록한 키워드(DB)에 있는가?
    public String[] ifContainsKeyword(String room, String keyword){
        Log.i(TAG, "method on - ifContainsKeyword");
        for(DataRoom<DataRoom<String>> roomNode : roomNodes)
            if(roomNode.label.matches(room)) {
                for (DataRoom<String> keyNode : roomNode.dataList) {

                    String[] t = new String[]{keyNode.label};
                    if (keyNode.label.contains(" ")) t = keyNode.label.split(" ");

                    for (int i = 0; i < t.length; i++) {
                        if (!keyword.contains(t[i])) break;
                        if (i + 1 == t.length)
                            return new String[]{keyNode.dataList.get(new Random().nextInt(keyNode.dataList.size()))};
                    }
                }
            }
        return null;
    }


    public String[] ifApiQuestion(String room, String keyword, Context context){
        Log.i(TAG, "method on - ifApiQuestion");
        ArrayList<String> rst = new ArrayList<>();
        if(keyword.contains("시세")){
            String coinName = keyword.substring(0, keyword.indexOf("시세")).trim();
            try{
                String t;
                if((coinName.contains("모든") || coinName.contains("전체")) && coinName.contains("업비트"))
                    rst.add( new ApiCoin().getAllPrice_UPBIT() );
                else if((coinName.contains("모든") || coinName.contains("전체"))&& ( coinName.contains("빗썸") ||coinName.contains("빗섬")) )
                    rst.add(new ApiCoin().getAllPrice_BITHUMB());
                else {
                    rst.add( new ApiCoin().getPrice(coinName)); // 코인
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            Cursor cursor = roleDB.getStockList(coinName, null);
            ArrayList<String[]> nameCodeList = new ArrayList<>();
            while(cursor.moveToNext()) {
                String stockType = cursor.getString(0);
                String stockCode = cursor.getString(1);
                String stockName = cursor.getString(2);
                nameCodeList.add( new String[]{stockType, stockCode, stockName});
            }
            rst.addAll( new ApiStock(context).getStocks(nameCodeList) );
            rst.removeIf(Objects::isNull);
            if(rst.size()<1) return null;
            return (String[]) rst.toArray(new String[0]);
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

        if(keyword.contains("컨벤시아")){
            String t = new AnalysConvensia().getConvensia(keyword);
            if(t !=null) return new String[]{t};
        }
        if( (keyword.contains("전화번호") ) || keyword.contains("가보신") ){
            String[] t = new AnalysPlace().getPlace(keyword);
            if(t !=null) return t;
        }
        if(keyword.contains("맛집 추천") || keyword.contains("메뉴 추천") || keyword.contains("뭐 먹지")
        || keyword.contains("맛집추천") ||keyword.contains("메뉴추천")||keyword.contains("뭐먹지")){
            String[] t = new AnalysPlace().recommandPlace(keyword);
            if(t !=null) return t;
        }
        return null;
    }
    public String[] ifMatchExpletiveKeyword(String room, String keyword){
        Log.i(TAG, "method on - ifMatchExpletiveKeyword");
//        for(String s : expletiveKeyword)
//         if (keyword.contains(s)) return "욕하지마세요ㅜ_ㅜ";
        return null;
    }

    public String[] ifMatchGPTKeyword(String room, String keyword){
        Log.i(TAG, "method on - ifMatchGPTKeyword");
        if(keyword.length()>10)
            if(keyword.startsWith("ai야 ") || keyword.startsWith("AI야 ")) {
                keyword = keyword.substring(4);
                if (gptTalkList.size() >= 5)
                    gptTalkList.remove(0);
                else gptTalkList.add(keyword);
                return new String[]{new ApiChatGPT(context).getCompletions(gptTalkList)};
            }
        return null;
    }

    public String[] ifEducateKeyword(String room, String str){
        Log.i(TAG, "method on - ifEducateKeyword");
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
        Log.i(TAG, "method on - specialKeyword");
        if( !keyword.contains("도움말") || !keyword.contains("봇")) return null;

        int seq = 1;
        String[] t = new String[]{
                seq+++">국어사전 검색\n" +
                        "-> [검색어]가 뭐야\n" +
                        "ex) 나무가 뭐야",
                seq+++">인기 검색어 확인\n" +
                        "ex) 실시간 검색어\n" +
                        "ex) 인기 검색어",
                seq+++">맛집 추천\n" +
                        "ex) 한식 맛집 추천\n" +
                        "ex) 커넬워크 맛집 추천",
                seq+++">컨벤시아 전시 일정\n" +
                        "-> [날짜] 컨벤시아\n" +
                        "ex) 28일 컨벤시아",
                seq+++">실시간 코인 가격\n" +
                        "-> [코인명] 시세\n" +
                        "ex) 이더리움 시세\n" +
                        "ex) ETH 시세\n" +
                        "ex) 업비트 전체 시세",
                seq+++">실시간 주가\n" +
                        "-> [종목명] 주가\n" +
                        "ex) 삼성전자 주가",
                seq+++">뉴스 확인\n" +
                        "ex) 뉴스 보기\n" +
                        "ex) 뉴스 구독",
                seq+++">초성퀴즈 풀기\n" +
                        "ex) 퀴즈 시작\n" +
                        "ex) 퀴즈 중지\n" +
                        "ex) 힌트",
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

    // 2025. topicChecker라는 맵 하나를 너무 우려먹는듯... 좀 분리해야할 필요가 있어보인다.
    public String[] ifConsonantGame(String sender, String room, String str){
        Log.i(TAG, "method on - ifConsonantGame");

        String quizRoom = "beforeConsonantGame"+room;
        String quizRoomType = quizRoom+"type";

        String answar = topicChecker.get(quizRoom);

        if(answar!=null) {  //퀴즈가 진행중인가?
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

                String question = roleDB.getConsonantQuestion(topicChecker.get(quizRoomType), null);
                topicChecker.put(quizRoom, question );
                ArrayList<String> reply = new ArrayList<>();
                Random r = new Random();
                r.setSeed(System.currentTimeMillis());
                reply.add(displySender + "님 " + personAndScore.get(room).get(sender) + "점!\n" + answar + " 정답이에요!");
                if (r.nextInt(20) < 2) reply.add("와 잘하세요!");
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
                    result += (new Random().nextBoolean() ? question.charAt(i) : answar.charAt(i));

                return new String[]{result};
            }
        }




        if(str.contains("퀴즈") && str.contains("시작")) {
//            if(answar != null)
//                return new String[]{"지난퀴즈 정답자가 없어요.\n주제는 롤 스킨! 맞춰보세요!\n초성: " + new ReplyFunction().consonant(answar)};
            int quizIndex = new Random().nextInt(roleDB.getGameTableNameMap().size() ); // 퀴즈주제 랜덤지정
            String quizName = (String) roleDB.getGameTableNameMap().keySet().toArray()[quizIndex]; // 퀴즈주제 랜덤지정
            topicChecker.put(quizRoomType, quizName );   // 채팅방에 퀴즈 "주제" 등록
            String question = roleDB.getConsonantQuestion(quizName, null);  //퀴즈 랜덤 추출
            topicChecker.put(quizRoom, question);   // 해당 채팅방에 "주제"에 "문제" 등록
            StringBuilder sb = new StringBuilder("주제는 ["+ roleDB.getGameTableNameMap().get(quizName) + "]! 맞춰보세요!\n");
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
                roleDB.putConsonantQuestion(topicChecker.get(quizRoomType), str.substring(5), null);
                return new String[]{new ReplyFunction().consonant(str.substring(5)) + " 추가했어요\n" +
                        "문제는 총 "+roleDB.getTableSize(topicChecker.get(quizRoomType), null)+"개에요."};
            }catch (Exception e){e.printStackTrace(); }
        }

        return null;

    }


}
