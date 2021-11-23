package com.osy.callapi;

import android.content.Context;
import android.util.Log;

import com.osy.notifyreply.R;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

public class ApiApplyhome {
    final String TAG = "ApiApplyhome";
    Context context;
    String ApiKey;
    String[] allSido = {"강원", "광주", "부산", "인천", "충남", "경기", "기타", "서울", "전남", "충복",
            "경남", "대구", "세종", "전북", "경북", "대전", "울산", "제주"};

    public ApiApplyhome(Context context){
        Log.i(TAG, "Class on - "+TAG);
        this.context = context;
        ApiKey = context.getResources().getString(R.string.apllyhomeKey);
    }

    public String[] getApplyhome(String keyword){
        int rank = 1;
        if(keyword.contains("특")) rank = 0;
        else if(keyword.contains("2")) rank = 2;
        // 0 -특별 // 1-1순위 // 2-2순위

        String selectSido = null;
        for(String s : allSido)
            if(selectSido==null && keyword.contains(s)) selectSido = s;

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -2);
        int tempDate = cal.get(Calendar.MONTH)+1;
        String startDate = cal.get(Calendar.YEAR)+""+(tempDate <10 ? "0"+tempDate : tempDate);

        cal.add(Calendar.MONTH, 3);
        tempDate = cal.get(Calendar.MONTH)+1;
        String endDate = cal.get(Calendar.YEAR)+""+(tempDate <10 ? "0"+tempDate : tempDate);

        String rankString = rank==0 ? "특별공급" : rank==2 ? "2순위" : "1순위";

        String urlString = "http://openapi.reb.or.kr/OpenAPI_ToolInstallPackage/service/rest/ApplyhomeInfoSvc/getLttotPblancList"
                + "?serviceKey="+ApiKey
                + "&startmonth="+startDate
                + "&endmonth=" + endDate
                + "&numOfRows=100";
        System.out.println(urlString);
        try {
            URL url = new URL(urlString);
            InputStream is = url.openStream();

            Document document = new ApiParser().parseXML(is);
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xpath = xPathFactory.newXPath();

            XPathExpression expr = xpath
                    .compile("//houseManageNo | //houseNm | //pblancNo | //rceptEndde | //sido");
            NodeList nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);

            StringBuilder sb = new StringBuilder("");

            String[] nodeNameList = new String[] {"houseManageNo", "houseNm", "pblancNo", "rceptEndde", "sido", "gnrlrnk1crsparearceptpd", "hssplyadres"};
            ArrayList<String[]> valueList = new ArrayList<String[]>();


            String[] values = new String[7];
            for(int i=0 ; i<nodeList.getLength() ; i++) {
                Node node = nodeList.item(i);
                String nodeName = node.getNodeName();

                if(nodeName.matches(nodeNameList[i%5]))
                    values[i%5] = node.getTextContent();

                if(i%5==4)
                    if(selectSido!=null)
                        if(values[4].matches(selectSido)) valueList.add(values.clone());else;
                    else valueList.add(values.clone());

            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            for(int i=0 ; i<valueList.size() ; i++) {
                long gap = sdf.parse(valueList.get(i)[3]).getTime() +(3600000*17) - Calendar.getInstance().getTimeInMillis();
                if(gap< 0) {
                    valueList.remove(i--);
                    continue;
                }
                String[] t = detailInfo(valueList.get(i)[0],valueList.get(i)[2], rank);
                if(t==null) {
                    valueList.remove(i--);
                    continue;
                }
                valueList.get(i)[5] = t[0];
                valueList.get(i)[6] = t[1];
            }
            if(valueList.size()==0){
                if(selectSido==null) return new String[]{"청약정보가 없어요."};
                else return new String[]{ "다가오는 일주일동안 "+selectSido+"지역 청약정보가 없어요."};
            }
            valueList.sort(new ApplyhomeComparator());
            for(int i=0 ; i<valueList.size() ; i++){
                sb.append((i+1)+".("+valueList.get(i)[4]+")"+valueList.get(i)[1]+"\n");
                sb.append(" -"+valueList.get(i)[5]+" 접수\n");
//              sb.append(""+s[6]+"\n");  //주소
            }
            sb.append(rankString+" 청약접수가 머지 않았어요!");
            if(selectSido ==null) return new String[]{sb.toString(), "원하는 지역만 검색할 수 있어요!\nex)인천 청약정보 2순위"};
            else return new String[]{sb.toString()};

        }catch(Exception e) {e.printStackTrace();}
        return null;
    }

    String[] detailInfo(String houseManageNo, String pblancNo, int rank){

        String urlString = "http://openapi.reb.or.kr/OpenAPI_ToolInstallPackage/service/rest/ApplyhomeInfoSvc/getAPTLttotPblancDetail"
                + "?serviceKey="+ApiKey
                + "&houseManageNo=" + houseManageNo
                + "&pblancNo=" + pblancNo;
        String rankString = rank == 0 ? "spsplyrceptbgnde" : rank==2 ? "gnrlrnk2crsparearceptpd" : "gnrlrnk1crsparearceptpd";
        try {
            URL url = new URL(urlString);
            InputStream is = url.openStream();

            Document document = new ApiParser().parseXML(is);
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xpath = xPathFactory.newXPath();

            XPathExpression expr;

            expr = xpath.compile("//"+rankString+" | //hssplyadres");
            NodeList nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);

            String gnrlrnk1crsparearceptpd = nodeList.item(0).getTextContent();
            String hssplyadres = nodeList.item(1).getTextContent();
            if(rank==0) {
                String t = hssplyadres;
                hssplyadres = gnrlrnk1crsparearceptpd;
                gnrlrnk1crsparearceptpd = t;
            }
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(gnrlrnk1crsparearceptpd);
            long gap = date.getTime()+(3600000*17) - System.currentTimeMillis();
            if(gap >0 && gap < (long)86400000*7) {
                System.out.println(gnrlrnk1crsparearceptpd +" " +  hssplyadres);
                return new String[] {gnrlrnk1crsparearceptpd, hssplyadres};
            }
            else return null;

        }catch(Exception e) {e.printStackTrace();}
        return null;
    }

    class ApplyhomeComparator implements Comparator<String[]> {
        @Override
        public int compare(String[] f1, String[] f2) {
            long d1, d2;
            try {
                d1 = new SimpleDateFormat("yyyy-MM-dd").parse(f1[5]).getTime();
            }catch(Exception e) { d1 = Long.MAX_VALUE;}
            try {
                d2 = new SimpleDateFormat("yyyy-MM-dd").parse(f2[5]).getTime();
            }catch(Exception e) { d2 = Long.MAX_VALUE;}
            if (d1 > d2) return 1;
            else if (d1 < d2) return -1;
            return 0;

        }
    }

}
