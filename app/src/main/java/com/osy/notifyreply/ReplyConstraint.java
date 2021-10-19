package com.osy.notifyreply;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.os.Environment;
import android.renderscript.ScriptGroup;
import android.util.Log;

import com.osy.callapi.ApiCoin;
import com.osy.callapi.ApiCorona;
import com.osy.callapi.ApiDict;
import com.osy.callapi.ApiKMA;
import com.osy.callapi.ApiSellApart;
import com.osy.callapi.RssTopSearch;
import com.osy.roledb.RoleDB;
import com.osy.utility.DataRoom;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
    DataRoom<DataRoom<String>> commonKeyNodes;
    Map<String, String> beforeConsonantGame = new HashMap<String,String>();
    Map<String, Map<String, Integer> > personAndScore = new HashMap<String, Map<String, Integer> >();
    ArrayList<String> allQuestion =null;

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
    }

    public String checkKeyword(String sender, String room, String keyword){
        if(room.length() > 27) room = room.substring(0,27);
        try {
            if (keyword.length() > 120) keyword = keyword.substring(0, 120);
        }catch (Exception e){return null;}

        String reply;

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

    public String ifShowReplyList(String room,String keyword){
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
            return sb.toString();
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

    public String ifOnOff(String room, String keyword){
        if(isOperation.get(room)==null){
            roomNodes = new ReplyFunction().setKeyList(roomNodes, room,"안녕","안녕하세요");
        }
        if(keyword.matches("이제그만") && isOperation.get(room)) {
            isOperation.replace(room,false);
            return "채팅을 중지할게요.";
        }
        else if(keyword.matches("다시작동") && !isOperation.get(room)) {
            isOperation.replace(room,true);
            return "채팅을 시작할게요.";
        }
        return null;
    }

    public String ifDeleteKey(String room, String keyword){
        if(keyword.startsWith("학습목록삭제") || keyword.startsWith("ㅎㅅㅁㄹㅅㅈ") || keyword.startsWith("ㅎㅅㅁㄽㅈ")) {
            try {
                String[] del = keyword.split(" ");
                int i = Integer.parseInt(del[1]);
                boolean b = roleDB.deleteContainKey(room, i);
                if (b) {
                    setInitialize(context);
                    return "제거했어요!";
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }


    public String ifContainsKeyword(String room, String keyword){
        Log.i(TAG, "ifContainsKeyword room/keyword : "+room+"/"+keyword);

        for(DataRoom<DataRoom<String>> roomNode : roomNodes)
            if(roomNode.label.matches(room))
                for(DataRoom<String> keyNode : roomNode.dataList)
                    if(keyword.contains(keyNode.label))
                        return keyNode.dataList.get(new Random().nextInt(keyNode.dataList.size()));
        return null;

    }
    public String ifApiQuestion(String room, String keyword, Context context){
        Log.i(TAG, "ifApiQuestion room/keyword : "+room+"/"+keyword);
        if(keyword.contains(" 날씨")) {
            if(keyword.contains("오늘 날씨")) keyword = "연수구 송도1동 날씨";
            String[] addr = keyword.split(" ");
            try {
                if (addr[2].contains("날씨"))
                    return new ApiKMA(context).getWeather(addr[0], addr[1]);
            } catch (Exception e) {
                e.printStackTrace();
                String t = "날씨가 궁금하면 아래와 같이 검색해보세요!\n" +
                        "[구] [동] 날씨\n " +
                        "예시)부평구 삼산1동 날씨";
                return t;
            }
            return null;
        }
        if(keyword.contains("코로나 현황")){
            return new ApiCorona(context).getNationalCorona();
        }
        if(keyword.contains(" 실거래가")){
            try {
                String addr = keyword.substring(0, keyword.indexOf("실거래가")-1);
                return new ApiSellApart(context).getPrice(addr);
            } catch (Exception e) {
                e.printStackTrace();
                String t = "부동산 실거래가기 궁금하면 아래와 같이 검색해보세요!\n" +
                        "[법정동] 실거래가\n" +
                        "예시1)인천광역시 연수구 실거래가\n" +
                        "예시2)경기도 성남시 분당구 실거래가\n" +
                        "예시3)서울특별시 송파구 실거래가";
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
                return t;
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
            return new ApiDict(context).searchDictionary(keyword);
        }
        if(keyword.contains("검색어") && (keyword.contains("실시간") || keyword.contains("인기")) )
            return new RssTopSearch().getTopSearchKeyword();

        return null;
    }
    public String ifMatchExpletiveKeyword(String room, String keyword){
//        for(String s : expletiveKeyword)
//         if (keyword.contains(s)) return "욕하지마세요ㅜ_ㅜ";
        return null;
    }

    public String ifEducateKeyword(String room, String str){
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
                return t;
            }catch(Exception e){
                return t;
            }
        if(str.contains("학습하기"))
            return t;
        return null;
    }


    public String ifConsonantGame(String sender, String room, String str){
        Log.i(TAG, "ifConsonantGame");
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
                return displySender + "님 " + personAndScore.get(room).get(sender) + "점!\n" + answar + " 정답이에요!";
            }
            else if (  str.contains("퀴즈") && (str.contains("포기") || str.contains("그만") || str.contains("중지"))) {
                beforeConsonantGame.remove(room);
                return "퀴즈를 포기했어요.ㅠ\n정답은 " + answar+"이였어요..\n계속하려면 [퀴즈시작]를 외쳐주세요!";
            }
            else if(str.contains("힌트")){
                String question= new ReplyFunction().consonant(answar);
                String result = "힌트! ";
                for(int i =0 ; i< answar.length() ; i++)
                    result += (new Random().nextBoolean() ? question.charAt(i) : answar.charAt(i) );
                return result;
            }
        }

        if(str.contains("퀴즈") && str.contains("시작")) {
            if(answar != null)
                return "지난퀴즈 정답자가 없어요.\n주제는 롤 스킨! 맞춰보세요!\n초성: " + new ReplyFunction().consonant(answar);

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
                    return "다음 문제에요!\n초성: " + new ReplyFunction().consonant(question);
                else
                    return " 주제 롤스킨! 맞춰보세요!\n초성: " + new ReplyFunction().consonant(question);
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
            return sb.toString();
        }

        if(str.contains("문제추가 ")) {
            try {
                allQuestion.add(str.substring(5));
                return str.substring(5) + new ReplyFunction().consonant(str.substring(5)) + " 추가했어요\n" +
                        "문제는 총 "+allQuestion.size()+"개에요.";
            }catch (Exception e){ }
        }


        return null;
    }


}
