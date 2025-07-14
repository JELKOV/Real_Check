<p align="center">
  <img src="./src/main/resources/static/android-chrome-512x512.png" alt="RealCheck Banner" width="70%">
</p>

# 📍 RealCheck

> **실시간 현장 정보 공유 플랫폼**
> 사용자들이 주변의 대기 상태, 편의시설, 요청 응답 등을 실시간으로 주고받는 위치 기반 커뮤니티 서비스입니다.

---

<br>

<details>
<summary>🛠️<strong>기술 스택</strong></summary>

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
<summary>💡<strong>주요 기능 요약</strong></summary>

<br>

### 👤 사용자 기능

- 회원가입 / 로그인 (세션 기반 인증)
- 마이페이지 (포인트 내역, 내 활동 로그, 장소 관리)
- 요청 등록 (공식/비공식 장소 선택, 카테고리 기반 세부항목 포함)
- 응답(답변) 등록 및 채택 기능
- 자발 정보 공유(FREE_SHARE) 등록 및 지도 기반 공개
- 조회수 기반 포인트 자동 지급 (동일 사용자 중복 제외)
- 포인트 충전/사용 및 환전 신청
- 장소 등록 신청 (공식 장소, 위치, 카테고리 지정)
- 장소 등록 수정 및 반려 사유 확인
- 신고 기능 (신고 3회 시 자동 블라인드 처리 및 관리 기능 연동)

<br>

### 🗺️ 지도 기능

- 현재 위치 기반 요청/공유글 지도 보기
- 반경 3km 내 위치 기반 필터링 및 마커 표시
- 공식 장소별 커뮤니티 페이지 제공 (공지, 질문, 응답 통합)
- 장소별 응답 묶음 (질문 기준 그룹화, 최대 2개 표시 + 더보기 기능)
- 좌표 기반 사용자 입력 장소도 지원
- 지도 이동/줌에 따른 요청/공유 필터링

<br>

### 💬 응답 기능

- 요청별 답변 등록 (텍스트 + 이미지 + 카테고리 기반 세부 필드)
- 응답 수정/삭제 (작성자만 가능)
- 관리자 승인 없이 즉시 반영
- 채택 시 포인트 자동 지급, 미채택 시 반환 로직

<br>

### 🏢 장소 기능

- 공식 장소 등록 요청 (이름, 주소, 위치, 카테고리 포함)
- 장소 승인 후 커뮤니티 페이지 자동 생성
- 공지 등록 (REGISTER 유형 StatusLog)
- 커뮤니티 페이지 내 공지/답변/질문 통합 표시
- 장소별 카테고리 필터 지원

<br>

### 🛡️ 관리자 기능

- 사용자 전체 목록 조회 및 상태 관리 (정지/삭제/탈퇴 로그 포함)
- 요청/응답 목록 확인 및 블라인드 처리
- FREE_SHARE 응답 블라인드 처리 및 포인트 환급/재발급 기능
- 월별 통계 대시보드 (요청/응답/신고/포인트 변화량 등 시각화)
- 관리자 활동 로그 기록 (AdminActionLog, 필터링 지원)
- 장소 승인/반려 처리 및 반려 사유 저장

</details>

---

<br>

<details>
<summary>📁<strong>디렉토리 구조 & DB 설계</strong></summary>

<br>

#### 📁 폴더 구조

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
<br>

#### 🗂 ERD

<img src="https://github.com/user-attachments/assets/2ec9078b-eceb-4498-92c2-e88ffe83067c" width="800"/>

</details>

---

<br>

<details>
<summary>🚀<strong>실행 방법</strong></summary>

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
<summary>🌐<strong>배포 환경</strong></summary>

<br>

- HTTPS 지원: [https://real-check.store](https://real-check.store)
- Nginx reverse proxy + Certbot SSL
- EC2 + RDS(MySQL) + Redis 구성

</details>

---

<br>

<details>
<summary>📸<strong>프로젝트 시연</strong></summary>

<br>

> 아래는 주요 기능 시연 영상과 스크린샷입니다. 각 항목을 클릭해 내용을 펼쳐보세요.

<br>

<details>
<summary>🧭<strong>메인페이지</strong></summary>

---

#### 🔹 메인 화면

<video src="https://github.com/user-attachments/assets/3c0d1909-6c2c-4d2b-97ab-dbca8b95c760" controls width="600"></video>

#### 🔹 헤더 일반 사용자

<img src="https://github.com/user-attachments/assets/f79aaa6f-45b1-490f-8a4b-def8ef77b1c1" width="500"/>

#### 🔹 헤더 관리자

<img src="https://github.com/user-attachments/assets/8c59d782-706c-4824-bf11-0945a79a3421" width="500"/>

---
</details>

<br>

<details>
<summary>👤<strong>사용자 기능</strong></summary>

---

#### 🔹 회원 가입

<video src="https://github.com/user-attachments/assets/1b168e8e-5b12-4f09-ac64-a3a7b2d62260" controls width="600"></video>

#### 🔹 로그인

<video src="https://github.com/user-attachments/assets/15bb4f9d-132e-4a6b-a578-d22c6c2a2586" controls width="600"></video>

#### 🔹 포인트 충전

<video src="https://github.com/user-attachments/assets/693a5845-e80f-4fb5-b29b-f448f4141bc1" controls width="600"></video>

#### 🔹 정보 수정

<video src="https://github.com/user-attachments/assets/1283ad9f-9a57-4e78-b9cd-8dd047dc1251" controls width="600"></video>

#### 🔹 회원 탈퇴

<video src="https://github.com/user-attachments/assets/3d6e5bf9-1b9f-479d-8a46-ad42fd63df0b" controls width="600"></video>

---
</details>

<br>

<details>
<summary>📨<strong>요청 기능</strong></summary>

---

#### 🔹 공식장소 정보요청

<video src="https://github.com/user-attachments/assets/1ed542d9-ec40-4e35-aeec-b6ce1101b802" controls width="600"></video>

#### 🔹 일반장소 정보요청

<video src="https://github.com/user-attachments/assets/2708b923-e877-4fd9-b20a-2045c3d4494b" controls width="600"></video>

#### 🔹 3시간 경과 요청 보기

<video src="https://github.com/user-attachments/assets/daca81c9-83a2-4f63-9997-ffa816795625" controls width="600"></video>

#### 🔹 주변 요청 보기

<video src="https://github.com/user-attachments/assets/602e7cc9-7b9c-44e8-ac00-ac5ac9193c41" controls width="600"></video>

---
</details>

<br>

<details>
<summary>💬<strong>답변 기능</strong></summary>

---

#### 🔹 응답 답변 작성

<video src="https://github.com/user-attachments/assets/d48e5c68-8de1-4a51-b2f5-267b69b656b5" controls width="600"></video>

#### 🔹 자발적 정보 공유

<video src="https://github.com/user-attachments/assets/bc77b0b4-e914-4b4e-a3bd-1ee816be6480" controls width="600"></video>

#### 🔹 답변 채택하기

<video src="https://github.com/user-attachments/assets/76cfde2e-7be2-4f82-85dc-80ba3f782feb" controls width="600"></video>

#### 🔹 주변 응답 보기

<video src="https://github.com/user-attachments/assets/fca8179b-f820-4a23-b57e-a773c689f448" controls width="600"></video>

---
</details>

<br>

<details>
<summary>📍<strong>장소 기능</strong></summary>

---

#### 🔹 장소 등록

<video src="https://github.com/user-attachments/assets/f6954565-1fee-4dd5-80a6-a72b51e91d91" controls width="600"></video>

#### 🔹 공지 등록

<video src="https://github.com/user-attachments/assets/4706cb47-6760-4c54-9fe6-0a6f2f967538" controls width="600"></video>

#### 🔹 커뮤니티 페이지

<video src="https://github.com/user-attachments/assets/a119fe58-d997-426c-b3ba-68ea6a96aece" controls width="600"></video>

---
</details>

<br>

<details>
<summary>🛡️<strong>관리자 기능</strong></summary>

---

#### 🔹 통계 기능

<video src="https://github.com/user-attachments/assets/aac1aa85-dc3d-4bd3-a970-7b90583c7495" controls width="600"></video>

#### 🔹 사용자 관리

<video src="https://github.com/user-attachments/assets/8282c742-f2f7-4360-a2cc-2a3c3f62969b" controls width="600"></video>

#### 🔹 신고 관리

<video src="https://github.com/user-attachments/assets/504ca92a-fe83-4607-85b0-06113787c6a3" controls width="600"></video>

#### 🔹 로그 관리

<video src="https://github.com/user-attachments/assets/4b330ef0-fd41-4e95-b3a6-c6a0d908e94b" controls width="600"></video>

#### 🔹 장소 관리

<video src="https://github.com/user-attachments/assets/9bec2f7f-5b8f-4c34-b7a5-da971c7cbd8a" controls width="600"></video>

#### 🔹 자발적 공유 관리

<video src="https://github.com/user-attachments/assets/b5ff4e66-8802-4ff3-b14a-c61d33d9cbe0" controls width="600"></video>

---
</details>

</details>

---

<br>

<details>
<summary>🙋‍♂️<strong>개발자 정보</strong></summary>

<br>

| 이름   | 역할                                   | GitHub                                                 |
| ------ | -------------------------------------- | ------------------------------------------------------ |
| 안제호 | 전체 개발 (기획, 백엔드, 프론트, 배포) | [https://github.com/JELKOV](https://github.com/JELKOV) |

</details>

---

<br>

<details>
<summary>📄<strong>라이선스</strong></summary>

<br>

> 본 프로젝트는 포트폴리오용으로 제작되었으며, 상업적 사용을 금합니다.

</details>

---