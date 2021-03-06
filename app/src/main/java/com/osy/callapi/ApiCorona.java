package com.osy.callapi;

import android.content.Context;
import android.util.Log;
import com.osy.notifyreply.R;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.io.InputStream;
import java.net.URL;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

public class ApiCorona {
    final String TAG = "ApiCorona";
    String ApiKey = null;

    public ApiCorona(Context context){
        Log.i(TAG, "Class on - "+TAG);
        ApiKey =  context.getResources().getString(R.string.coronaKey);
    }

    public String getNationalCorona(){
        Calendar cal = Calendar.getInstance();
        int tempDate = cal.get(Calendar.MONTH)+1;
        String endDate = ""+(tempDate <10 ? "0"+tempDate : tempDate);
        tempDate = cal.get(Calendar.DATE);
        endDate += (tempDate <10 ? "0"+tempDate : tempDate);
        endDate = cal.get(Calendar.YEAR)+endDate;

        cal.add(Calendar.DATE,-5);
        tempDate = cal.get(Calendar.MONTH)+1;
        String stDate = ""+(tempDate <10 ? "0"+tempDate : tempDate);
        tempDate = cal.get(Calendar.DATE);
        stDate += (tempDate <10 ? "0"+tempDate : tempDate);
        stDate = cal.get(Calendar.YEAR)+stDate;


        String urlString = "http://openapi.data.go.kr/openapi/service/rest/Covid19/getCovid19InfStateJson?" +
                "serviceKey=" + ApiKey +
                "&startCreateDt=" +stDate +
                "&endCreateDt=" + endDate ;
        Log.i(TAG, "url : " + urlString);
        try {
            URL url = new URL(urlString);
            InputStream is = url.openStream();

            Document document = new ApiParser().parseXML(is);
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xpath = xPathFactory.newXPath();

            XPathExpression expr = xpath.compile("//decideCnt | //deathCnt | //createDt");
            NodeList nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            //NodeList nodeList= document.getElementsByTagName("item");

            StringBuilder sb = new StringBuilder("");


            int decideCnt=0, clearCnt=0, deathCnt=0, careCnt=0;
            int raise_decideCnt=0, raise_clearCnt=0, raise_deathCnt=0, raise_careCnt=0;
            Date createDt = null;

            for(int i = 0 ; i < 3 ; i++) {
                Node node = nodeList.item(i);
                String name = node.getNodeName();
                String value = node.getTextContent();

                if(name.matches("createDt")) // ??????
                    createDt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS").parse(value);
                else if(name.matches("deathCnt")) // ??????
                    deathCnt = Integer.parseInt(value);
                else if(name.matches("decideCnt")) // ?????????
                    decideCnt = Integer.parseInt(value);
            }

            for(int i = 3 ; i < 6 ; i++) {
                Node node = nodeList.item(i);
                String name = node.getNodeName();
                String value = node.getTextContent();

                if(name.matches("deathCnt"))  // ?????? ??????
                    raise_deathCnt = deathCnt-Integer.parseInt(value);
                else if(name.matches("decideCnt")) // ?????? ?????????
                    raise_decideCnt = decideCnt-Integer.parseInt(value);
            }

            NumberFormat nf = NumberFormat.getInstance();
            sb.append(new SimpleDateFormat("MM??? dd??? ????????? ??????!\n").format(createDt));
            sb.append(String.format("?????? ?????? +%s???\n",nf.format(raise_decideCnt)));
            sb.append(String.format("?????? ?????? +%s???\n",nf.format(raise_deathCnt)));
            sb.append(String.format("?????? ?????? %s???", nf.format(decideCnt)));

            return sb.toString();

        }catch(Exception e) {e.printStackTrace();}
        return null;
    }
}
