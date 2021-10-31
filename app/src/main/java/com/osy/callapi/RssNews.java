package com.osy.callapi;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.URL;

public class RssNews {
    String[] newsURL = new String[4];
    public final int YONHAP = 0;
    public final int JOONGANG = 1;
    public final int JTBC = 2;
    public final int SBS = 3;

    public RssNews(){
        newsURL[0] = "https://www.yonhapnewstv.co.kr/category/news/headline/feed/";
        newsURL[1] = "https://rss.joins.com/joins_homenews_list.xml";
        newsURL[2] = "https://fs.jtbc.joins.com//RSS/newsflash.xml";
        newsURL[3] = "https://news.sbs.co.kr/news/SectionRssFeed.do?sectionId=03&plink=RSSREADER";
    }


    public String[] getNews(){
        String resunMessage[] = new String[4];
        for(int k = 0 ; k< newsURL.length ; k++) {
            try {
                URL url = new URL(newsURL[k].toString());
                Document document = new ApiParser().parseXML(url.openStream());
                NodeList nodeList = document.getElementsByTagName("title");
                StringBuilder sb = new StringBuilder("");
                int i = 1;
                switch (k) {
                    case 0:
                        sb.append("연합뉴스입니다." + "\n");
                        break;
                    case 1:
                        sb.append("중알일보입니다." + "\n");
                        i = 2;
                        break;
                    case 2:
                        sb.append("JTBC뉴스입니다." + "\n");
                        break;
                    case 3:
                        sb.append("SBS뉴스입니다." + "\n");
                        i = 2;
                        break;
                }
                for (Node n = nodeList.item(i); n != null; n = nodeList.item(i + 1)) {
                    sb.append("＊" + n.getTextContent() + "\n");
                    if (i++ > 4) break;
                }
                resunMessage[k] = sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return resunMessage;
    }
}
