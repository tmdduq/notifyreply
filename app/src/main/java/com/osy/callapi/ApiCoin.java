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
    final String[] coinList_BITHUMB = new String[]{"비트코인","리플","이더리움","폴카닷","스텔라루멘","도지코인","클레이튼","유니스왑","라이트코인","위믹스","마일벌스","알파체인","링크플로우","퀴즈톡","어셈블프로토콜","바이낸스코인","콘텐토스","오미세고","펑션엑스","이오스","에이다","이캐시","제노토큰","디비전","솔라나","이더리움 클래식","쿼크체인","앵커뉴럴월드","루나","트론","미스블록","프로톤","퀀텀","소다코인","체인링크","비체인","비트코인 캐시","왐토큰","하이브","엑시인피니티","테조스","코스모스","비트토렌트","벨라프로토콜","웨이브","폴라리스 쉐어","어댑터 토큰","알파쿼크","블로서리","아이젝","코르텍스","연파이낸스","메탈","벨로프로토콜","베라시티","비트코인 골드","아픽스","밀크","알고랜드","쿠사마","이브이지","스와이프","오르빗 체인","샌드박스","아르고","아이온","버거스왑","비트코인에스브이","무비블록","세럼","팬케이크스왑","바이프로스트","베이직","에이치닥","룸네트워크","믹스마블","쎄타토큰","메타디움","왁스","고머니2","너보스","마이네이버앨리스","칠리즈","비트코인 다이아몬드","페이코인","밀리미터토큰","온톨로지가스","넴","디센트럴랜드","아이콘","보라","리니어파이낸스","지엑스체인","라이브피어","심볼","스팀","애니버스","바이오패스포트","펀디엑스","셀러네트워크","누사이퍼","썸씽","질리카","크로미아","맵프로토콜","엘리시아","렌","신세틱스","엔진코인","파워렛저","옵저버","피르마체인","위드","플레타","비너스","카이버 네트워크","엘프","트루체인","웨이키체인","오브스","크립토닷컴체인","스시스왑","스트라티스","컴파운드","온톨로지","폴리곤","네스트리","머신익스체인지코인","이오스트","카르테시","저스트","싸이클럽","이마이너","링엑스","썬","코넌","베이커리토큰","골렘","게이머코인","스테이터스네트워크토큰","쎄타퓨엘","더마이다스터치골드","리저브라이트","우마","뉴메레르","어거","베이직어텐션토큰","다이","아모코인","제로엑스","오키드","에이브","콜라토큰","300피트 네트워크","밸런서","보아","타키온프로토콜","힙스","템코","더그래프","써틱","앵커","메이커","뱅코르","라이파이낸스","울트라","미러 프로토콜","브이시스템즈","루프링","다드","센트럴리티","아로와나토큰","월튼체인","오션프로토콜","트러스트버스","에이아이워크","이포스","랠리","밸러토큰","에이피엠 코인"};
    final String[] coinCodeList_BITHUMB = new String[]{"BTC","XRP","ETH","DOT","XLM","DOGE","KLAY","UNI","LTC","WEMIX","MVC","ARPA","LF","QTCON","ASM","BNB","COS","OMG","FX","EOS","ADA","XEC","XNO","DVI","SOL","ETC","QKC","ANW","LUNA","TRX","MSB","XPR","QTUM","SOC","LINK","VET","BCH","WOM","HIVE","AXS","XTZ","ATOM","BTT","BEL","WAVES","POLA","ADP","AQT","BLY","RLC","CTXC","YFI","MTL","VELO","VRA","BTG","APIX","MLK","ALGO","KSM","EVZ","SXP","ORC","SAND","AERGO","AION","BURGER","BSV","MBL","SRM","CAKE","BFC","BASIC","HDAC","LOOM","MIX","THETA","META","WAXP","GOM2","CKB","ALICE","CHZ","BCD","PCI","MM","ONG","XEM","MANA","ICX","BORA","LINA","GXC","LPT","XYM","STEEM","ANV","BIOT","PUNDIX","CELR","NU","SSX","ZIL","CHR","MAP","EL","REN","SNX","ENJ","POWR","OBSR","FCT","WIKEN","FLETA","XVS","KNC","ELF","TRUE","WICC","ORBS","CRO","SUSHI","STRAX","COMP","ONT","MATIC","EGG","MXC","IOST","CTSI","JST","CYCLUB","EM","RINGX","SUN","CON","BAKE","GLM","GHX","SNT","TFUEL","TMTG","RSR","UMA","NMR","REP","BAT","DAI","AMO","ZRX","OXT","AAVE","COLA","FIT","BAL","BOA","IPX","HIBS","TEMCO","GRT","CTK","ANKR","MKR","BNT","RAI","UOS","MIR","VSYS","LRC","DAD","CENNZ","ARW","WTC","OCEAN","TRV","AWO","WOZX","RLY","VALOR","APM"};

    public ApiCoin(){
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
            double rippleRage = 0;
            if(sp<cp)
                rippleRage = cp/sp;
            else
                rippleRage = sp/cp *(-1);

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
            double rippleRage = 0;
            if(sp<cp)
                rippleRage = cp/sp;
            else
                rippleRage = sp/cp *(-1);

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
                double rippleRage = 0;
                if(sp<cp) rippleRage = cp/sp;
                else rippleRage = sp/cp *(-1);

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
                double rippleRage = 0;
                if(sp<cp) rippleRage = cp/sp;
                else rippleRage = sp/cp *(-1);
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
