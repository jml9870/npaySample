package com.sample.spring.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sample.spring.dto.NaverReserveRequest;
import com.sample.spring.dto.ProductItem;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class NPayTestController {

    private final RestTemplate restTemplate = new RestTemplate();

    // 네이버페이 개발 가맹점 환경설정 값 (테스트용 발급 키 입력)
    private final String CLIENT_ID = "";
    private final String CLIENT_SECRET = "";
    private final String CHAIN_ID = "";
    
    //가매정 아이디 : np_spvkz506717
    
    private final String APPLY_URL = "https://dev-pay.paygate.naver.com/naverpay-partner/naverpay/payments/v2.2/apply/payment";

    /**
     * [1단계] 결제 준비 API
     * 브라우저에서 http://localhost:8080/test/naver-ready 호출 시 실행
     */
    @RequestMapping(value = "/npayTest/naver-ready", method = RequestMethod.GET)
    public String naverPayReady(Model model) {
        try {
        	
        	System.out.println("naver 결졔 예약 생성 요청.....");
        	
        	String apiDomain = "dev-pay.paygate.naver.com"; //운영 : pay.paygate.naver.com
        	String apiVersion = "v2";
        	
            // 네이버페이 결제 예약 개발 도메인 URL
            String reserveUrl = "https://"+apiDomain+"/naverpay-partner/naverpay/payments/"+apiVersion+"/reserve";

            // 헤더 설정 (JSON 방식)
            RestTemplate restTemplate = new RestTemplate();

            // 1. 헤더 세팅 (Content-Type: application/json 및 네이버 필수 키)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Naver-Client-Id", CLIENT_ID);
            headers.set("X-Naver-Client-Secret", CLIENT_SECRET);
            headers.set("X-NaverPay-Chain-Id", CHAIN_ID);
            
            // 멱등성 키: 중복 요청 방지를 위해 랜덤 UUID 생성하여 세팅
            headers.set("X-NaverPay-Idempotency-Key", UUID.randomUUID().toString());

            // 2. 바디(데이터) 세팅
            NaverReserveRequest requestBody = new NaverReserveRequest();
            requestBody.setModelVersion("2");
            requestBody.setMerchantUserKey("muserkey_" + System.currentTimeMillis()); // 테스트용 고유값 생성
            requestBody.setMerchantPayKey("mpaykey_" + System.currentTimeMillis());
            requestBody.setProductName("한국사 외 1건");
            requestBody.setProductCount(2);
            requestBody.setTotalPayAmount(1000); // 만약 1000원이면
            
            String returlUrl = "http://localhost:8080";
            
            // 주말에 테스트할 ngrok 고정 도메인의 성공 리다이렉트 주소 매핑
            requestBody.setReturnUrl(returlUrl + "/npayTest/naver-success"); 
            
            requestBody.setTaxScopeAmount(0);
            requestBody.setTaxExScopeAmount(1000);
          //  requestBody.setEnvironmentDepositAmount(0);
            requestBody.setPurchaserName("홍길동");
            requestBody.setPurchaserBirthday("20000101");

            // 내부 상품 리스트(List) 조립
            List<ProductItem> items = new ArrayList<>();
            items.add(new ProductItem("BOOK", "GENERAL", "107922211", "한국사", "NAVER_BOOK", 1));
            items.add(new ProductItem("MUSIC", "CD", "299911002", "Loves", "NAVER_BOOK", 1));
            requestBody.setProductItems(items);
            
            String reserveId = "";

            // 3. 헤더와 바디 결합
            HttpEntity<NaverReserveRequest> entity = new HttpEntity<>(requestBody, headers);
            
            String respoonseData = "";
            
            try {
                // 4. 네이버페이 서버로 POST 요청 발송 (결과는 String 또는 필요한 DTO로 수신)
                ResponseEntity<String> response = restTemplate.exchange(reserveUrl, HttpMethod.POST, entity, String.class);
                
                String jsonResponse = response.getBody(); // 네이버가 준 JSON 덩어리
                
                // 2. Jackson ObjectMapper 생성
                ObjectMapper objectMapper = new ObjectMapper();
                
                // 3. JSON 문자열을 트리 구조(JsonNode)로 읽어들이기
                JsonNode rootNode = objectMapper.readTree(jsonResponse);
                
                // 4. 응답 코드가 "Success" 인지 먼저 확인
                String code = rootNode.path("code").asText();
                
                if ("Success".equals(code)) {
                    // ⭐ 핵심: body 안으로 들어가서 reserveId 가져오기
                    reserveId = rootNode.path("body").path("reserveId").asText();
                    
                    System.out.println("발급된 결제예약 ID: " + reserveId);
                 //   return reserveId; // reserveId만 최종 반환!
                } else {
                    // 실패했을 경우 네이버가 준 에러 메시지 추출
                    String message = rootNode.path("message").asText();
                    System.out.println("네이버페이 예약 실패: " + message);
                  //  return "FAIL";
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                return "{\"code\":\"Fail\", \"message\":\"" + e.getMessage() + "\"}";
            }
            
            // 리턴받은 결제 예약 ID(paymentId) 추출
          //  String paymentId = result.get("paymentId").toString(); 

            // 공식 개발용 서비스 도메인과 조합하여 최종 결제창 주소 생성
            String finalPaymentUrl = "https://test-m.pay.naver.com/payments/" + reserveId;

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
    @RequestMapping(value = "/npayTest/naver-success", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> naverSuccess(
            @RequestParam(required=false) String paymentId,
            @RequestParam String resultCode,
            @RequestParam(required=false) String resultMessage,
            @RequestParam(required=false) String reserveId) {
    	
    	// ex : http://localhost:8080/npayTest/naver-success?resultCode=Success&reserveId=20260607L0h0czByOUpFMnh1RVZrZ2VQb0hIZzQ1cE1rPQ==&paymentId=20260607NP1213836102
        
        Map<String, Object> resultMap = new HashMap<String, Object>();

        try {
            // ① [검증 단계] 기존 토스페이처럼 앱이 실어 보낸 칩 정보 유효성 검증
            // (로컬 테스트 시에는 무조건 통과하도록 처리 혹은 로그만 적재)
        	System.out.println("paymentId " + paymentId);
        	
        	if ("Success".equalsIgnoreCase(resultCode)) {
        		System.out.println("결제 성공");
        	} else {
        		System.out.println("결제 실패");
        		System.out.println("resultCode :: " + resultCode);
        		
        	}
        	
        	callNaverApprove(paymentId);
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("success", false);
            resultMap.put("message", e.getMessage());
        }

        return resultMap; // 앱(네이티브)에게 JSON으로 결과 반환
    }

    
    //최종 결제 승인
    public boolean callNaverApprove(String paymentId) {
        // 1. [중요] 명세서 주의사항에 따른 60초 타임아웃 세팅 (단위: 밀리세컨드)
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);  // 연결 타임아웃 5초
        requestFactory.setReadTimeout(60000);    // 읽기(응답) 타임아웃 60초 설정 필수!
        
        RestTemplate restTemplate = new RestTemplate(requestFactory);

        // 2. 헤더 세팅 (Content-Type: application/x-www-form-urlencoded)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); // Form 방식 지정
        headers.set("X-Naver-Client-Id", CLIENT_ID);
        headers.set("X-Naver-Client-Secret", CLIENT_SECRET);
        headers.set("X-NaverPay-Chain-Id", CHAIN_ID);
        
        // 멱등성 키: 승인 오류 시 중복 결제 방지를 위해 랜덤 UUID 필수 세팅
        headers.set("X-NaverPay-Idempotency-Key", UUID.randomUUID().toString());

        // 3. 바디 데이터 세팅 (Form 방식이므로 MultiValueMap을 사용합니다)
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("paymentId", paymentId); // 네이버가 준 결제번호 세팅

        // 4. 헤더와 바디 결합
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // 5. 네이버페이 서버로 승인 POST 요청 발송
            ResponseEntity<String> response = restTemplate.exchange(APPLY_URL, HttpMethod.POST, entity, String.class);
            String jsonResponse = response.getBody();
            
            // 6. 결과 파싱 (ObjectMapper 이용)
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            String code = rootNode.path("code").asText();
            
            System.out.println("rootNode :: " + rootNode);
            System.out.println("rootNode.toString :: " + rootNode.path("body").path("paymentId"));
            
            if ("Success".equals(code)) {
                System.out.println("네이버페이 최종 결제승인 성공! 주문 완료 처리 진행");
                return true;
            } else {
                String message = rootNode.path("message").asText();
                System.out.println("네이버페이 승인 실패 사유: " + message);
                return false;
            }
            
        } catch (Exception e) {
            // 60초 타임아웃이 터지거나 망 장애가 났을 때 예외 처리
            System.out.println("승인 API 호출 중 에러 발생 (타임아웃 등)");
            e.printStackTrace();
            return false;
        }
    }
}