<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>송금/결제 완료</title>
<style>
    body { font-family: 'Malgun Gothic', sans-serif; text-align: center; padding-top: 50px; }
    .container { border: 1px solid #ddd; display: inline-block; padding: 30px; border-radius: 10px; background-color: #fafafa; }
    .success-icon { font-size: 50px; color: #4CAF50; }
    .btn-home { margin-top: 20px; padding: 10px 20px; background-color: #FFEB00; border: none; border-radius: 5px; cursor: pointer; font-weight: bold; }
</style>
</head>
<body>

    <div class="container">
        <div class="success-icon">✓</div>
        <h2>카카오페이 테스트 완료</h2>
        <p>요청하신 간편 송금/결제 승인이 성공적으로 처리되었습니다.</p>
        <p style="color:gray; font-size:12px;">이클립스 콘솔 창에서 최종 승인 JSON 데이터를 확인하세요.</p>
        
        <button class="btn-home" onclick="location.href='/'">메인으로 돌아가기</button>
    </div>

</body>
</html>