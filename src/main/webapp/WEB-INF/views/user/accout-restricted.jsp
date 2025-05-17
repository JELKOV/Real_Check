<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>서비스 접근 제한 - RealCheck</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
    <style>
        .restricted-container {
            max-width: 500px;
            margin: 0 auto;
            margin-top: 100px;
            padding: 30px;
            border: 1px solid #ddd;
            border-radius: 10px;
            background-color: #f8f9fa;
        }
        .restricted-container h2 {
            margin-bottom: 20px;
        }
        .restricted-container p {
            font-size: 16px;
            color: #6c757d;
        }
    </style>
</head>
<body>
    <div class="container restricted-container text-center">
        <h2>🔒 서비스 접근 제한</h2>
        <p class="mt-3">현재 회원님은 탈퇴 예약 상태로 서비스 이용이 제한되었습니다.</p>
        <p class="text-muted">탈퇴 예약은 취소할 수 있으며, 계정을 유지하려면 아래 버튼을 클릭하세요.</p>
        <div class="mt-4">
            <a href="/cancel-account-deletion" class="btn btn-primary me-2">탈퇴 예약 취소</a>
            <a href="/logout" class="btn btn-secondary">로그아웃</a>
        </div>
    </div>
</body>
</html>
