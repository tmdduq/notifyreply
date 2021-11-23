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

    //빗썸은 전체목록을 보여주는 JSON이 따로 없네..
    final String[] coinList_BITHUMB = new String[]{"비트코인", "위믹스 ", "이더리움", "보라", "어셈블프로토콜", "리플 ", "샌드박스", "디센트럴랜드", "디비전 ", "파워렛저", "바이프로스트", "엔진코인 ", "싸이클럽", "이브이지 ", "제노토큰", "링크플로우", "왁스", "크로미아", "솔라나", "템코", "믹스마블", "클레이튼 ", "300피트 네트워크 ", "퀴즈톡 ", "페이코인", "콜라토큰", "루프링", "도지코인", "미스블록", "애니버스 ", "마이네이버앨리스", "에이다", "트론 ", "아르고", "라이트코인", "앵커뉴럴월드 ", "퀀텀", "하이브", "이더리움 클래식", "소다코인 ", "라이브피어", "이캐시", "이오스", "마일벌스", "폴카닷", "폴라리스 쉐어", "밀크", "알고랜드 ", "알파쿼크", "오미세고", "썸씽", "쿼크체인", "루나 ", "무비블록", "웨이키체인", "스텔라루멘", "비체인", "스와이프", "너보스", "라이파이낸스", "콘텐토스", "바이오패스포트", "체인링크", "게이머코인", "칠리즈", "바이낸스코인", "아로와나토큰 ", "크립토닷컴체인", "엘리시아 ", "비트토렌트", "베이직어텐션토큰", "룸네트워크", "엑시인피니티 ", "앵커", "오르빗 체인", "어댑터 토큰", "비트코인 캐시", "카르테시", "베라시티", "베이직", "코스모스", "아픽스", "테조스", "폴리곤 ", "밀리미터토큰", "골렘", "센트럴리티", "위드", "오키드", "누사이퍼", "왐토큰", "에이치닥", "아이콘 ", "오브스 ", "연파이낸스", "힙스 ", "울트라", "신세틱스", "아모코인", "엘프", "메타디움", "링엑스", "온톨로지", "스트라티스", "온톨로지가스", "유니스왑", "비트코인 골드", "프로톤", "더마이다스터치골드", "에이아이워크", "세럼", "넴", "코르텍스", "펀디엑스", "펑션엑스", "셀러네트워크", "아이온", "메탈", "보아", "비트코인에스브이", "코넌 ", "오션프로토콜", "스팀", "플레타", "리니어파이낸스", "트러스트버스", "블로서리", "렌", "썬", "버거스왑", "리저브라이트", "벨로프로토콜", "맵프로토콜", "카이버 네트워크", "이마이너 ", "쎄타토큰", "트루체인 ", "이오스트 ", "더그래프", "고머니2", "이포스", "우마", "저스트", "알파체인", "피르마체인", "옵저버", "스테이터스네트워크토큰", "비트코인 다이아몬드", "지엑스체인", "제로엑스", "심볼", "타키온프로토콜 ", "랠리", "밸러토큰", "에이피엠 코인", "다드", "미러 프로토콜", "밸런서", "다이", "쎄타퓨엘", "아이젝", "스시스왑", "질리카", "브이시스템즈 ", "어거", "쿠사마", "에이브", "팬케이크스왑 ", "네스트리", "월튼체인", "웨이브", "베이커리토큰", "비너스", "컴파운드", "뱅코르", "뉴메레르", "메이커", "써틱", "벨라프로토콜 ", "머신익스체인지코인"};
    final String[] coinCodeList_BITHUMB = new String[]{"BTC", "WEMIX", "ETH", "BORA", "ASM", "XRP", "SAND", "MANA", "DVI", "POWR", "BFC", "ENJ", "CYCLUB", "EVZ", "XNO", "LF", "WAXP", "CHR", "SOL", "TEMCO", "MIX", "KLAY", "FIT", "QTCON", "PCI", "COLA", "LRC", "DOGE", "MSB", "ANV", "ALICE", "ADA", "TRX", "AERGO", "LTC", "ANW", "QTUM", "HIVE", "ETC", "SOC", "LPT", "XEC", "EOS", "MVC", "DOT", "POLA", "MLK", "ALGO", "AQT", "OMG", "SSX", "QKC", "LUNA", "MBL", "WICC", "XLM", "VET", "SXP", "CKB", "RAI", "COS", "BIOT", "LINK", "GHX", "CHZ", "BNB", "ARW", "CRO", "EL", "BTT", "BAT", "LOOM", "AXS", "ANKR", "ORC", "ADP", "BCH", "CTSI", "VRA", "BASIC", "ATOM", "APIX", "XTZ", "MATIC", "MM", "GLM", "CENNZ", "WIKEN", "OXT", "NU", "WOM", "HDAC", "ICX", "ORBS", "YFI", "HIBS", "UOS", "SNX", "AMO", "ELF", "META", "RINGX", "ONT", "STRAX", "ONG", "UNI", "BTG", "XPR", "TMTG", "AWO", "SRM", "XEM", "CTXC", "PUNDIX", "FX", "CELR", "AION", "MTL", "BOA", "BSV", "CON", "OCEAN", "STEEM", "FLETA", "LINA", "TRV", "BLY", "REN", "SUN", "BURGER", "RSR", "VELO", "MAP", "KNC", "EM", "THETA", "TRUE", "IOST", "GRT", "GOM2", "WOZX", "UMA", "JST", "ARPA", "FCT", "OBSR", "SNT", "BCD", "GXC", "ZRX", "XYM", "IPX", "RLY", "VALOR", "APM", "DAD", "MIR", "BAL", "DAI", "TFUEL", "RLC", "SUSHI", "ZIL", "VSYS", "REP", "KSM", "AAVE", "CAKE", "EGG", "WTC", "WAVES", "BAKE", "XVS", "COMP", "BNT", "NMR", "MKR", "CTK", "BEL", "MXC"};

    public ApiCoin(){
        Log.i(TAG, "Class on - "+TAG);
        getBithumbList();
        getUpbitList();
    }
    protected void getBithumbList(){
        coinMap_BITHUMB = new HashMap<>();
        coinCodeMap_BITHUMB = new HashMap<>();

        for(int i= 0 ; i< coinList_BITHUMB.length ; i++) {
            coinMap_BITHUMB.put(coinList_BITHUMB[i], coinCodeList_BITHUMB[i]);
            coinCodeMap_BITHUMB.put(coinCodeList_BITHUMB[i], coinList_BITHUMB[i]);
        }
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
        if(bithumb!=null) sb.append("(빗썸)"+bithumb+"\n");
        if(upbit!=null) sb.append("(업비트)"+upbit);

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
