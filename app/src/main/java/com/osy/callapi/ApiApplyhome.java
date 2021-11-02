package com.osy.callapi;

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

    public String getApplyhome(int rank){
        // 0 -특별 // 1-1순위 // 2-2순위
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -2);
        int tempDate = cal.get(Calendar.MONTH)+1;
        String startDate = cal.get(Calendar.YEAR)+""+(tempDate <10 ? "0"+tempDate : tempDate);

        cal.add(Calendar.MONTH, 3);
        tempDate = cal.get(Calendar.MONTH)+1;
        String endDate = cal.get(Calendar.YEAR)+""+(tempDate <10 ? "0"+tempDate : tempDate);

        String rankString = rank==0 ? "특별공급" : rank==2 ? "2순위" : "1순위";

        String urlString = "http://openapi.reb.or.kr/OpenAPI_ToolInstallPackage/service/rest/ApplyhomeInfoSvc/getLttotPblancList"
                + "?serviceKey=AnHCVCcJA4ryVT2mLGxn39ArocUJ4u24CjC48xSzKW5YEs1eQczGINhGX6rqt%2BCnZ9Z4hwcBYdFzcxeJWPMY5w%3D%3D"
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
                    .compile("//houseManageNo | //houseNm | //pblancNo | //rceptBgnde | //sido");
            NodeList nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);

            StringBuilder sb = new StringBuilder("");

            String[] nodeNameList = new String[] {"houseManageNo", "houseNm", "pblancNo", "rceptBgnde", "sido", "gnrlrnk1crsparearceptpd", "hssplyadres"};
            ArrayList<String[]> valueList = new ArrayList<String[]>();


            String[] values = new String[7];
            for(int i=0 ; i<nodeList.getLength() ; i++) {
                Node node = nodeList.item(i);
                String nodeName = node.getNodeName();

                if(nodeName.matches(nodeNameList[i%5]))
                    values[i%5] = node.getTextContent();

                if(i%5==4) valueList.add(values.clone());
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            for(int i=0 ; i<valueList.size() ; i++) {
                long gap = sdf.parse(valueList.get(i)[3]).getTime() - Calendar.getInstance().getTimeInMillis();
                if(gap<0) {
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
            valueList.sort(new ApplyhomeComparator());
            for(int i=0 ; i<valueList.size() ; i++){
                sb.append((i+1)+".("+valueList.get(i)[4]+")"+valueList.get(i)[1]+"\n");
                sb.append(" -"+valueList.get(i)[5]+" 접수\n");
//              sb.append(""+s[6]+"\n");  //주소
            }
            sb.append(rankString+" 청약접수가 머지 않았어요!");
//            System.out.println(sb.toString());
            return sb.toString();

        }catch(Exception e) {e.printStackTrace();}
        return null;
    }

    String[] detailInfo(String houseManageNo, String pblancNo, int rank){

        String urlString = "http://openapi.reb.or.kr/OpenAPI_ToolInstallPackage/service/rest/ApplyhomeInfoSvc/getAPTLttotPblancDetail"
                + "?serviceKey=AnHCVCcJA4ryVT2mLGxn39ArocUJ4u24CjC48xSzKW5YEs1eQczGINhGX6rqt%2BCnZ9Z4hwcBYdFzcxeJWPMY5w%3D%3D"
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
            long gap = date.getTime() - System.currentTimeMillis();
            if(gap < (long)86400000*7) {
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
