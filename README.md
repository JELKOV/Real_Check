<p align="center">
  <img src="./src/main/resources/static/android-chrome-512x512.png" alt="RealCheck Banner" width="70%">
</p>

# 📍 RealCheck

> **실시간 현장 정보 공유 플랫폼**
> 사용자들이 주변의 대기 상태, 편의시설, 요청 응답 등을 실시간으로 주고받는 위치 기반 커뮤니티 서비스입니다.

---

<br>

<details>
<summary> <span style="font-size: 1.2rem;"><strong>🛠️ 기술 스택</strong></summary>

<br>

| 구분         | 기술                                             |
| ------------ | ------------------------------------------------ |
| **Backend**  | Java 17, Spring Boot 3.4.4, Spring Security, JPA |
| **Frontend** | JSP, jQuery, JavaScript, Bootstrap 5, CSS        |
| **Database** | MySQL 8, Redis (세션/캐시)                       |
| **Infra**    | AWS EC2, RDS, Nginx, Certbot (HTTPS)             |
| **API**      | RESTful API, Naver Maps API                      |
| **Others**   | GitHub, Maven, Linux shell 배포                  |

</details>

---

<br>

<details>
<summary> <span style="font-size: 1.2rem;"><strong>💡 주요 기능 요약</strong></summary>

### 👤 사용자 기능

- 회원가입 / 로그인 (세션 기반)
- 마이페이지 (활동 로그, 포인트 이력, 장소 관리)
- 요청 등록 및 응답 작성, 채택
- 자발 공유(FREE_SHARE) 및 조회수 기반 포인트 지급
- 장소 등록, 승인 대기/반려/수정
- 포인트 충전/사용/환전 요청
- 신고 기능 (자동 블라인드 처리)

<br>

### 🗺️ 지도 기능

- 현재 위치 기반 요청/공유글 지도 보기
- 반경 3km 내 공식 장소별 커뮤니티 페이지
- 장소별 응답 묶음 및 질문 더보기 기능

<br>

### 🛡️ 관리자 기능

- 사용자, 요청, 공유글, 신고, 장소 전체 관리
- 통계 조회: 월별 사용자/요청/응답/신고/포인트
- 관리자 활동 로그(AdminActionLog)
- FREE_SHARE 블라인드 및 포인트 환급/재발급

</details>

---

<br>

<details>
<summary> <span style="font-size: 1.2rem;"><strong>📁 프로젝트 구조</strong></summary>

<br>

```bash
realcheck/
└── src/
    └── main/
        ├── java/com/realcheck/
        │   ├── admin/ common/ config/ deletionlog/ file/
        │   ├── naver/ page/ place/ point/ report/ request/
        │   ├── scheduler/ status/ user/ util/
        ├── resources/
        │   ├── static/ (css/js/images)
        │   └── templates/
        └── webapp/WEB_INF/views (jsp)
            ├── admin/ map/ place/ request/ status/ user/
```

</details>

---

<br>

<details>
<summary> <span style="font-size: 1.2rem;"><strong>🚀 실행 방법</strong></summary>

<br>

```bash
# 1. 의존성 설치
./mvnw clean install

# 2. 빌드
./mvnw package

# 3. 실행 (EC2 등 서버 환경에서)
java -jar target/realcheck-0.0.1-SNAPSHOT.jar
```

</details>

---

<br>

<details>
<summary> <span style="font-size: 1.2rem;"><strong>🌐 배포 환경</strong></summary>

<br>

- HTTPS 지원: [https://real-check.store](https://real-check.store)
- Nginx reverse proxy + Certbot SSL
- EC2 + RDS(MySQL) + Redis 구성

</details>

---

<br>

<details>
<summary> <span style="font-size: 1.2rem;"><strong>📸 프로젝트 시연</strong></summary>
<div style="padding-left: 1rem; border-left: 2px solid #555; margin-top: 0.5rem;">

<details style="margin-left: 0.5rem;">
<summary><span style="font-size: 1.05rem;">🧭 메인페이지</summary>

<div style="margin: 0.5rem 0 1rem 1rem;">

#### 🔹 메인 화면

<img src="./src/main/resources/static/images/index.gif" alt="메인화면" style="max-width: 100%; margin-bottom: 1rem;" />

#### 🔹 헤더 일반 사용자

<img src="./src/main/resources/static/images/Header_User.png" alt="헤더 사용자" style="max-width: 100%; margin-bottom: 1rem;" />

#### 🔹 헤더 관리자

<img src="./src/main/resources/static/images/Header_Admin.png" alt="헤더 관리자" style="max-width: 100%; margin-bottom: 1rem;" />

</div>
</details>

<details style="margin-left: 0.5rem;">
<summary><span style="font-size: 1.05rem;">👤 사용자 기능</summary>

<div style="margin: 0.5rem 0 1rem 1rem;">

#### 🔹 회원 가입

<img src="./src/main/resources/static/images/Register_User.gif" style="max-width: 100%; margin-bottom: 1rem;" />

#### 🔹 로그인

<img src="./src/main/resources/static/images/Login_User.gif" style="max-width: 100%; margin-bottom: 1rem;" />

#### 🔹 포인트 충전

<img src="./src/main/resources/static/images/Point_Charge_Cash.gif" style="max-width: 100%; margin-bottom: 1rem;" />

#### 🔹 정보 수정

<img src="./src/main/resources/static/images/Edit_User.gif" style="max-width: 100%; margin-bottom: 1rem;" />

#### 🔹 회원 탈퇴

<img src="./src/main/resources/static/images/Delete_User.gif" style="max-width: 100%; margin-bottom: 1rem;" />

</div>
</details>

<details style="margin-left: 0.5rem;">
<summary><span style="font-size: 1.05rem;">📨 요청 기능</summary>

<div style="margin: 0.5rem 0 1rem 1rem;">

#### 🔹 공식장소 정보요청

<img src="./src/main/resources/static/images/PublicPlace_Request.gif" style="max-width: 100%; margin-bottom: 1rem;" />

#### 🔹 일반장소 정보요청

<img src="./src/main/resources/static/images/GeneralPlace_Request.gif" style="max-width: 100%; margin-bottom: 1rem;" />

#### 🔹 3시간 경과 요청 보기

<img src="./src/main/resources/static/images/OverTime_Request.gif" style="max-width: 100%; margin-bottom: 1rem;" />

#### 🔹 주변 요청 보기

<img src="./src/main/resources/static/images/Nearby_Request.gif" style="max-width: 100%; margin-bottom: 1rem;" />

</div>
</details>

<details style="margin-left: 0.5rem;">
<summary><span style="font-size: 1.05rem;">💬 답변 기능</summary>

<div style="margin: 0.5rem 0 1rem 1rem;">

#### 🔹 응답 답변 작성

<img src="./src/main/resources/static/images/Request_Answer.gif" style="max-width: 100%; margin-bottom: 1rem;" />

#### 🔹 자발적 정보 공유

<img src="./src/main/resources/static/images/Free_Share.gif" style="max-width: 100%; margin-bottom: 1rem;" />

#### 🔹 답변 채택하기

<img src="./src/main/resources/static/images/Select_Answer.gif" style="max-width: 100%; margin-bottom: 1rem;" />

#### 🔹 주변 응답 보기

<img src="./src/main/resources/static/images/Nearby_Answer.gif" style="max-width: 100%; margin-bottom: 1rem;" />

</div>
</details>

<details style="margin-left: 0.5rem;">
<summary><span style="font-size: 1.05rem;">📍 장소 기능</summary>

<div style="margin: 0.5rem 0 1rem 1rem;">

#### 🔹 장소 등록

<img src="./src/main/resources/static/images/Register_Place.gif" style="max-width: 100%; margin-bottom: 1rem;" />

#### 🔹 공지 등록

<img src="./src/main/resources/static/images/Register_Notice.gif" style="max-width: 100%; margin-bottom: 1rem;" />

#### 🔹 커뮤니티 페이지

<img src="./src/main/resources/static/images/Community.gif" style="max-width: 100%; margin-bottom: 1rem;" />

</div>
</details>

<details style="margin-left: 0.5rem;">
<summary><span style="font-size: 1.05rem;">🛡️ 관리자 기능</summary>

<div style="margin: 0.5rem 0 1rem 1rem;">

#### 🔹 통계 기능

<img src="./src/main/resources/static/images/Admin_Statistics.gif" style="max-width: 100%; margin-bottom: 1rem;" />

#### 🔹 사용자 관리

<img src="./src/main/resources/static/images/Admin_Users.gif" style="max-width: 100%; margin-bottom: 1rem;" />

#### 🔹 신고 관리

<img src="./src/main/resources/static/images/Admin_Reports.gif" style="max-width: 100%; margin-bottom: 1rem;" />

#### 🔹 로그 관리

<img src="./src/main/resources/static/images/Admin_Logs.gif" style="max-width: 100%; margin-bottom: 1rem;" />

#### 🔹 장소 관리

<img src="./src/main/resources/static/images/Admin_Place.gif" style="max-width: 100%; margin-bottom: 1rem;" />

#### 🔹 자발적 공유 관리

<img src="./src/main/resources/static/images/Admin_FreeShare.gif" style="max-width: 100%; margin-bottom: 1rem;" />

</div>
</details>

</div>
</details>

---

<br>

<details>
<summary><span style="font-size: 1.2rem;"><strong>🙋‍♂️ 개발자 정보</strong></summary>

| 이름   | 역할                                   | GitHub                                                 |
| ------ | -------------------------------------- | ------------------------------------------------------ |
| 안제호 | 전체 개발 (기획, 백엔드, 프론트, 배포) | [https://github.com/JELKOV](https://github.com/JELKOV) |

</details>

---

<br>

<details>
<summary><span style="font-size: 1.2rem;"><strong>📄 라이선스</strong></summary>

> 본 프로젝트는 포트폴리오용으로 제작되었으며, 상업적 사용을 금합니다.

</details>
