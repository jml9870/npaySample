package com.sample.spring.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/npayTest")
public class NPayTestController {

    private final RestTemplate restTemplate = new RestTemplate();

    // 네이버페이 개발 가맹점 환경설정 값 (테스트용 발급 키 입력)
    private final String CLIENT_ID = "YOUR_TEST_CLIENT_ID";
    private final String CLIENT_SECRET = "YOUR_TEST_CLIENT_SECRET";
    private final String CHAIN_ID = "YOUR_TEST_CHAIN_ID";

    /**
     * [1단계] 결제 준비 API
     * 브라우저에서 http://localhost:8080/test/naver-ready 호출 시 실행
     */
    @RequestMapping(value = "/naver-ready", method = RequestMethod.GET)
    public String naverPayReady(Model model) {
        try {
        	
        	System.out.println("naver 결졔 예약 생성 요청.....");
        	
            // 네이버페이 공식 최신 결제 예약 개발 도메인 URL
            String reserveUrl = "https://dev-pay.paygate.naver.com/naverpay-partner/naverpay/payments/v2/reserve";

            // 헤더 설정 (JSON 방식)
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Naver-Client-Id", CLIENT_ID);
            headers.add("X-Naver-Client-Secret", CLIENT_SECRET);
            headers.add("X-NaverPay-Chain-Id", CHAIN_ID);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 바디 설정 (주문 데이터 하드코딩)
            Map<String, Object> body = new HashMap<String, Object>();
            body.put("merchantUserKey", "test_user_01");
            body.put("merchantOrderId", "TEST_ORD_" + System.currentTimeMillis()); // 주문번호 중복 방지
            body.put("productName", "100원 테스트 상품");
            body.put("totalPayAmount", 100);
            
            // 로컬 웹뷰에서 감지할 임시 성공 리다이렉트 주소 세팅
            body.put("returnUrl", "http://localhost:8080/test/naver-success"); 

            HttpEntity<Map<String, Object>> entity = new HttpEntity<Map<String, Object>>(body, headers);
            
            // 네이버페이 서버로 예약 요청
            Map response = restTemplate.postForObject(reserveUrl, entity, Map.class);
            Map result = (Map) response.get("result");
            
            // 리턴받은 결제 예약 ID(paymentId) 추출
            String paymentId = result.get("paymentId").toString(); 

            // 공식 개발용 서비스 도메인과 조합하여 최종 결제창 주소 생성
            String finalPaymentUrl = "https://test-m.pay.naver.com/payments/" + paymentId;

            // JSP 화면으로 이동할 주소 전달
            model.addAttribute("paymentUrl", finalPaymentUrl);

        } catch (Exception e) {
            e.printStackTrace();
            return "pay_error";
        }

        return "nPay"; // nPay.jsp 호출
    }

    /**
     * [3단계] 결제 검증 및 승인 API
     * 앱(웹뷰 가로채기)이 최종적으로 칩 정보와 결제 ID를 실어서 백엔드로 쏘는 주소
     */
    @RequestMapping(value = "/naver-approve", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> naverPayApprove(
            @RequestParam("paymentId") String paymentId,
            @RequestParam("chipInfo") String chipInfo) {
        
        Map<String, Object> resultMap = new HashMap<String, Object>();

        try {
            // ① [검증 단계] 기존 토스페이처럼 앱이 실어 보낸 칩 정보 유효성 검증
            // (로컬 테스트 시에는 무조건 통과하도록 처리 혹은 로그만 적재)
            System.out.println("앱으로부터 전달받은 기기 칩 정보: " + chipInfo);
            boolean isSecure = true; // 대조 검증 로직 들어갈 자리
            
            if (!isSecure) {
                resultMap.put("success", false);
                resultMap.put("message", "보안 검증(칩 정보 불일치) 실패");
                return resultMap;
            }

            // ② [승인 단계] 문서에 명시된 최신 v2.2 개발 승인 URL 주소
            String applyUrl = "https://dev-pay.paygate.naver.com/naverpay-partner/naverpay/payments/v2.2/apply/payment";

            // 헤더 세팅 (★ API 멱등성 키 헤더 필수 추가)
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Naver-Client-Id", CLIENT_ID);
            headers.add("X-Naver-Client-Secret", CLIENT_SECRET);
            headers.add("X-NaverPay-Chain-Id", CHAIN_ID);
            headers.add("X-NaverPay-Idempotency-Key", UUID.randomUUID().toString()); // 매 요청마다 고유 키 생성
            
            // ★ 중요: 명세에 따른 Form URL ENCODED 설정
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // 바디 세팅 (Form 전송을 위해 MultiValueMap 사용)
            MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
            body.add("paymentId", paymentId);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<MultiValueMap<String, String>>(body, headers);

            // 네이버페이 서버로 최종 돈 인출(승인) 요청
            Map response = restTemplate.postForObject(applyUrl, entity, Map.class);
            System.out.println("네이버페이 최종 승인 응답 결과: " + response.toString());

            // ③ [DB 처리 단계] 여기에 주문 완료(결제 성공) DB Update 로직 작성

            resultMap.put("success", true);
            resultMap.put("message", "결제 승인 완료");

        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("success", false);
            resultMap.put("message", e.getMessage());
        }

        return resultMap; // 앱(네이티브)에게 JSON으로 결과 반환
    }
}