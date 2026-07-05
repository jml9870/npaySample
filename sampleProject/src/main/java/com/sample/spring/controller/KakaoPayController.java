package com.sample.spring.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

// JSON 단순 파싱을 위해 억지로 라이브러리를 쓰지 않고, 
// 디버깅 및 테스트 편의를 위해 문자열에서 추출하는 방식을 예시로 넣었습니다.
@Controller
@RequestMapping("/pay")
public class KakaoPayController {
	
	

    private static final String SECRET_KEY = "SECRET_KEY DEV84C3FFA575A6FFC68D24CFAE0B7D016D68CA4"; 
    
    //private static final String cid = "TC0ONETIME"; //단기결제용
    private static final String cid = "TCSUBSCRIP"; //장기결제용
    /**
     * [1단계] 결제 준비 API
     * 브라우저에서 http://localhost:8080/test/naver-ready 호출 시 실행
     */
    @RequestMapping(value = "/kakao", method = RequestMethod.GET)
    public String naverPayReady(Model model) {
        	
        return "kakao"; // nPay.jsp 호출
    }
    
    // [1단계] 결제 준비 API
    @RequestMapping("/ready")
    @ResponseBody
    public String payReady(HttpSession session) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL("https://open-api.kakaopay.com/online/v1/payment/ready");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", SECRET_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonPayload = "{"
                    + "\"cid\":\""+cid+"\","
                    + "\"partner_order_id\":\"12345678\","
                    + "\"partner_user_id\":\"user01\","
                    + "\"item_name\":\"간편송금테스트\","
                    + "\"quantity\":1,"
                    + "\"total_amount\":1000,"
                    + "\"tax_free_amount\":0,"
                    + "\"approval_url\":\"http://localhost:8080/pay/success\","
                    + "\"cancel_url\":\"http://localhost:8080/pay/cancel\","
                    + "\"fail_url\":\"http://localhost:8080/pay/fail\""
                    + "}";
            
            System.out.println("여기 오는건지?????");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));			
            }

            int responseCode = conn.getResponseCode();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                (responseCode == 200) ? conn.getInputStream() : conn.getErrorStream(), StandardCharsets.UTF_8));
            
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) { sb.append(line); }
            
            String responseBody = sb.toString();

            // 로컬 테스트용 TID 추출 유틸 (Jackson/Gson이 없다면 임시로 사용)
            if (responseCode == 200 && responseBody.contains("\"tid\":\"")) {
                String tid = responseBody.split("\"tid\":\"")[1].split("\"")[0];
                // 중요: 승인 API에서 써야 하므로 세션에 저장
                session.setAttribute("tid", tid); 
            }

            return responseBody;

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"result\":\"FAIL\"}";
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    // [2단계] 카카오페이 인증 완료 후 리다이렉트 되는 승인 요청 API
    @RequestMapping("/success")
    public String paySuccess(@RequestParam("pg_token") String pgToken, HttpSession session) {
        HttpURLConnection conn = null;
        try {
            // 세션에서 준비 단계 때 저장한 거래번호(tid)를 꺼냄
            String tid = (String) session.getAttribute("tid");
            
            URL url = new URL("https://open-api.kakaopay.com/online/v1/payment/approve");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", SECRET_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // 승인 요청에 필요한 5가지 필수 파라미터 조립
            String jsonPayload = "{"
            		 + "\"cid\":\""+cid+"\","
                    + "\"tid\":\"" + tid + "\","
                    + "\"partner_order_id\":\"12345678\","
                    + "\"partner_user_id\":\"user01\","
                    + "\"pg_token\":\"" + pgToken + "\""
                    + "}";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));			
            }

            int responseCode = conn.getResponseCode();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                (responseCode == 200) ? conn.getInputStream() : conn.getErrorStream(), StandardCharsets.UTF_8));
            
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) { sb.append(line); }
            
            System.out.println("최종 승인 응답 결과: " + sb.toString());
            
            if (responseCode == 200) {
                // 승인이 정상 완료되면 세션의 TID 제거 후 성공 페이지로 이동
                session.removeAttribute("tid");
                return "kakaoSuccess"; 
            } else {
                return "kakaoFail"; // 실패 페이지 (필요 시 생성)
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "payFailJsp";
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
    
    @RequestMapping("/automatic")
    @ResponseBody
    public String payAutomatic(HttpSession session) {
        HttpURLConnection conn = null;
        try {
            // 세션(또는 DB)에서 저장해 둔 sid를 꺼냅니다.
            String sid = (String) session.getAttribute("saved_sid");
            if (sid == null) {
                return "{\"result\":\"FAIL\", \"message\":\"저장된 sid가 없습니다. 최초 결제를 먼저 진행하세요.\"}";
            }

            // 정기결제(Subscription) 전용 신규 API 주소
            URL url = new URL("https://open-api.kakaopay.com/online/v1/payment/subscription");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", SECRET_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            String formatedNow = now.format(formatter);

            // 2회차 결제 요청 바디 조립 (pg_token 필요 없음, 대신 sid 대입)
            String jsonPayload = "{"
            		+ "\"cid\":\""+cid+"\"," // 정기결제 코드 고정
                    + "\"sid\":\"" + sid + "\"," // 1회차 때 발급받아 저장한 빌링키
                    + "\"partner_order_id\":\""+formatedNow+"\"," // 주문번호는 매번 새로 생성해야 함
                    + "\"partner_user_id\":\"user01\","
                    + "\"item_name\":\"정기구독 결제\","
                    + "\"quantity\":1,"
                    + "\"total_amount\":1000," // 2회차 실 결제 금액
                    + "\"tax_free_amount\":0"
                    + "}";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));			
            }

            int responseCode = conn.getResponseCode();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                (responseCode == 200) ? conn.getInputStream() : conn.getErrorStream(), StandardCharsets.UTF_8));
            
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) { sb.append(line); }
            
            System.out.println("2회차 자동결제 요청 결과: " + sb.toString());
            return sb.toString(); // 성공 시 결제 상세 내역 JSON 리턴

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"result\":\"FAIL\"}";
        } finally {
            if (conn != null) conn.disconnect();
        }
        
        
    }
    
    @RequestMapping("/cancel-action")
    @ResponseBody
    public String payCancel(@RequestParam("tid") String tid, HttpSession session) {
        HttpURLConnection conn = null;
        try {
            // 1. 카카오페이 결제 취소 API 엔드포인트
            URL url = new URL("https://open-api.kakaopay.com/online/v1/payment/cancel");
            conn = (HttpURLConnection) url.openConnection();
            
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", SECRET_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // 2. 취소 요청 파라미터 조립
            // 테스트 가맹점 ID는 단건결제(TC0ONETIME)든 정기결제(TCSUBSCRIP)든 본인이 테스트한 cid를 일치시켜야 합니다.
            String jsonPayload = "{"
            		+ "\"cid\":\""+cid+"\","            // [필수] 테스트 가맹점 코드 (본인이 테스트한 코드와 일치시킬 것)
                    + "\"tid\":\"" + tid + "\","           // [필수] 결제 승인 후 발급받았던 거래 고유 번호
                    + "\"cancel_amount\":1000,"           // [필수] 취소할 총 금액
                    + "\"cancel_tax_free_amount\":0"       // [필수] 취소할 비과세 금액
                    + "}";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));			
            }

            // 3. 응답 처리
            int responseCode = conn.getResponseCode();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                (responseCode == 200) ? conn.getInputStream() : conn.getErrorStream(), StandardCharsets.UTF_8));
            
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            
            System.out.println("카카오페이 취소 응답 결과 (" + responseCode + "): " + sb.toString());
            return sb.toString(); // 성공 시 취소 상세 정보 JSON 반환

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"result\":\"FAIL\", \"message\":\"" + e.getMessage() + "\"}";
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
    
    // [1] 정기결제 키(sid) 상태 조회 API
    @RequestMapping("/sid-status")
    @ResponseBody
    public String getSidStatus(@RequestParam("sid") String sid) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL("https://open-api.kakaopay.com/online/v1/payment/manage/subscription/status");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", SECRET_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // 조회에 필요한 데이터 조립 (cid는 정기결제용 TCSUBSCRIP 고정)
            String jsonPayload = "{"
            		+ "\"cid\":\""+cid+"\"," 
                    + "\"sid\":\"" + sid + "\""
                    + "}";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));			
            }

            int responseCode = conn.getResponseCode();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                (responseCode == 200) ? conn.getInputStream() : conn.getErrorStream(), StandardCharsets.UTF_8));
            
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) { sb.append(line); }
            
            System.out.println("★ sid 상태 조회 결과: " + sb.toString());
            return sb.toString(); // 성공 시 status("ACTIVE", "INACTIVE" 등) 정보가 담긴 JSON 반환

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"result\":\"FAIL\"}";
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    // [2] 정기결제 키(sid) 비활성화(해지) API
    @RequestMapping("/sid-inactive")
    @ResponseBody
    public String inactiveSid(@RequestParam("sid") String sid) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL("https://open-api.kakaopay.com/online/v1/payment/manage/subscription/inactive");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", SECRET_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // 비활성화에 필요한 데이터 조립
            String jsonPayload = "{"
            		+ "\"cid\":\""+cid+"\"," 
                    + "\"sid\":\"" + sid + "\""
                    + "}";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));			
            }

            int responseCode = conn.getResponseCode();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                (responseCode == 200) ? conn.getInputStream() : conn.getErrorStream(), StandardCharsets.UTF_8));
            
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) { sb.append(line); }
            
            System.out.println("★ sid 비활성화 결과: " + sb.toString());
            return sb.toString(); // 성공 시 비활성화된 시간 정보 등이 담긴 JSON 반환

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"result\":\"FAIL\"}";
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}