package com.osy.callapi;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

public class ApiApplyhome {
    public String getApplyhome(){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -2);
        int tempDate = cal.get(Calendar.MONTH)+1;
        String startDate = cal.get(Calendar.YEAR)+""+(tempDate <10 ? "0"+tempDate : tempDate);

        cal.add(Calendar.MONTH, 3);
        tempDate = cal.get(Calendar.MONTH)+1;
        String endDate = cal.get(Calendar.YEAR)+""+(tempDate <10 ? "0"+tempDate : tempDate);



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

            String[] nodeNameList = new String[] {"houseManageNo", "houseNm", "pblancNo", "rceptBgnde", "sido"};
            ArrayList<String[]> valueList = new ArrayList<String[]>();


            String[] values = new String[5];
            for(int i=0 ; i<nodeList.getLength() ; i++) {
                Node node = nodeList.item(i);
                String nodeName = node.getNodeName();

                if(nodeName.matches(nodeNameList[i%5]))
                    values[i%5] = node.getTextContent();

                if(i%5==4) valueList.add(values.clone());
            }

            int seq = 0;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for(String[] s : valueList) {
                long gap = sdf.parse(s[3]).getTime() - Calendar.getInstance().getTimeInMillis();
                if(gap> (long)86400000*5 ) continue;
                if(gap< -86400000*3) break;
                String[] t = detailInfo(s[0],s[2] );
                sb.append("["+s[4]+"]"+s[1]+"\n");
//                sb.append(""+t[1]+"\n");  //주소
                sb.append("1순위 접수 "+t[0]+"\n");
                sb.append("--------\n");
                seq++;
            }
            sb.append(seq+"개의 청약정보가 머지 않았어요!");
            return sb.toString();
            //System.out.println(sb.toString());

        }catch(Exception e) {e.printStackTrace();}
        return null;
    }
    String[] detailInfo(String houseManageNo, String pblancNo){

        String urlString = "http://openapi.reb.or.kr/OpenAPI_ToolInstallPackage/service/rest/ApplyhomeInfoSvc/getAPTLttotPblancDetail"
                + "?serviceKey=AnHCVCcJA4ryVT2mLGxn39ArocUJ4u24CjC48xSzKW5YEs1eQczGINhGX6rqt%2BCnZ9Z4hwcBYdFzcxeJWPMY5w%3D%3D"
                + "&houseManageNo=" + houseManageNo
                + "&pblancNo=" + pblancNo;

        try {
            URL url = new URL(urlString);
            InputStream is = url.openStream();

            Document document = new ApiParser().parseXML(is);
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xpath = xPathFactory.newXPath();

            XPathExpression expr = xpath
                    .compile("//gnrlrnk1crsparearceptpd | //hssplyadres");
            NodeList nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);

            String hssplyadres = nodeList.item(0).getTextContent();
            String gnrlrnk1crsparearceptpd = nodeList.item(1).getTextContent();
//			System.out.println(hssplyadres +" " + gnrlrnk1crsparearceptpd);
            return new String[] {hssplyadres,gnrlrnk1crsparearceptpd};

        }catch(Exception e) {e.printStackTrace();}
        return null;
    }

}
