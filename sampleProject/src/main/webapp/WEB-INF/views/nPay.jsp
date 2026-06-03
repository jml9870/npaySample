<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>네이버페이 결제창으로 이동 중</title>
    <script type="text/javascript">
        // 화면이 로드되자마자 백엔드에서 받은 네이버페이 예약 URL로 자동 리다이렉트
        window.onload = function() {
            var targetUrl = "${paymentUrl}";
            
            if (targetUrl && targetUrl !== "") {
                window.location.href = targetUrl;
            } else {
                alert("결제창 주소가 유효하지 않습니다. 다시 시도해 주세요.");
                history.back();
            }
        };
    </script>
</head>
<body style="text-align: center; margin-top: 200px; font-family: sans-serif;">
    
    <div id="loading-zone">
        <h2>네이버페이 안전 결제창으로 연결 중입니다.</h2>
        <p>잠시만 기다려 주십시오...</p>
    </div>

</body>
</html>