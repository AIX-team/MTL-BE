package com.example.mytravellink.domain.url.service;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

@Service
public class YoutubeApiService {
    /**
     * 주어진 유튜브 videoId를 통해 실제 자막이 존재하는지 확인합니다.
     * 내부적으로 http://video.google.com/timedtext?type=list&v=<videoId> 엔드포인트를 호출하여
     * XML 응답에 <track> 태그가 존재하는지, 그리고 그 중 한국어("ko")나 영어("en") 자막이 있는지를 체크합니다.
     *
     * @param videoId 유튜브 영상 ID
     * @return 자막이 존재하면 true, 자막이 없으면 false
     */
    public boolean hasSubtitles(String videoId) {
        try {
            // YouTube 자막 목록을 가져오는 URL 생성 (timedtext API는 인증 없이 공개적으로 사용 가능)
            String urlStr = "http://video.google.com/timedtext?type=list&v=" + videoId;
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            
            int responseCode = connection.getResponseCode();
            if(responseCode != HttpURLConnection.HTTP_OK) {
                System.err.println("자막 정보를 가져오는 데 실패했습니다. HTTP 응답 코드: " + responseCode);
                return false;
            }
            
            // XML 파싱을 위한 DocumentBuilder 준비
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            InputStream inputStream = connection.getInputStream();
            Document document = builder.parse(inputStream);
            inputStream.close();
            
            // XML 문서에서 <track> 태그들을 검색
            NodeList tracks = document.getElementsByTagName("track");
            if (tracks.getLength() == 0) {
                System.out.println("자막이 존재하지 않습니다.");
                return false;
            }
            
            // <track> 태그들을 순회하면서 선호 언어(예: 한국어 또는 영어) 자막이 있는지 확인합니다.
            for (int i = 0; i < tracks.getLength(); i++) {
                Node node = tracks.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String lang = element.getAttribute("lang_code");
                    if ("ko".equalsIgnoreCase(lang) || "en".equalsIgnoreCase(lang)) {
                        System.out.println("자막이 있습니다. (lang: " + lang + ")");
                        return true;
                    }
                }
            }
            System.out.println("선호하는 언어의 자막이 없습니다.");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
} 