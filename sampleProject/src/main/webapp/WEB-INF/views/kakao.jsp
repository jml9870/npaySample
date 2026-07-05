<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>카카오페이 간편송금/결제 테스트</title>
<script src="/resources/js/jquery.min.js"></script>

<style>
    body { font-family: 'Malgun Gothic', sans-serif; padding: 40px; text-align: center; }
    .pay-box { border: 1px solid #e0e0e0; padding: 30px; display: inline-block; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
    .pay-btn { background-color: #FFEB00; color: #3C1E1E; font-weight: bold; font-size: 16px; border: none; padding: 12px 24px; border-radius: 4px; cursor: pointer; transition: background 0.2s; }
    .pay-btn:hover { background-color: #F7E100; }
    .info-txt { color: #666; font-size: 14px; margin-bottom: 20px; }
</style>

<script>
function requestKakaoPay() {
    // 버튼 중복 클릭 방지
    $('.pay-btn').prop('disabled', true).text('요청 중...');

    $.ajax({
        url: '/pay/ready',
        type: 'POST',
        dataType: 'json', // 서버에서 리턴하는 카카오페이 응답(JSON 문자열)을 오브젝트로 바로 파싱
        success: function(data) {
            // 카카오페이 신규 API 표준 PC 결제창 URL 존재 여부 확인
            if (data && data.next_redirect_pc_url) {
                // 현재 창에서 카카오페이 결제 준비(QR코드) 화면으로 이동
                location.href = data.next_redirect_pc_url;
            } else {
                alert('카카오페이 준비 요청 실패: URL을 전달받지 못했습니다.');
                resetButton();
            }
        },
        error: function(xhr, status, error) {
            console.error('에러 발생:', error);
            // 서버 에러 메시지 출력 (Controller의 getErrorStream 내용 확인용)
            alert('오류가 발생했습니다. 이클립스 콘솔 및 네트워크 탭을 확인하세요.\n' + xhr.responseText);
            resetButton();
        }
    });
}

function resetButton() {
    $('.pay-btn').prop('disabled', false).text('카카오페이로 테스트하기');
}
</script>
</head>
<body>

    <div class="pay-box">
        <h2>카카오페이 오픈 API 로컬 테스트</h2>
        <p class="info-txt">Spring 4 + Java 8 + HttpURLConnection 연동 샌드박스</p>
        
        <div style="margin: 20px 0; font-size: 18px; font-weight: bold;">
            테스트 송금 금액: <span style="color: #f5222d;">10,000원</span>
        </div>
        
        <button class="pay-btn" onclick="requestKakaoPay()">카카오페이로 테스트하기</button>
    </div>

</body>
</html>