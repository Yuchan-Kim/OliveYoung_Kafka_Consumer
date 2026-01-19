# Kafka → REST 전달 처리 정책 

---
#### History

**2026-01-19** (1.0.0) 김유찬 프로 작성 

---
### 0. 범위
- Kafka Consumer가 메시지를 수신하여 고정 REST URL로 전달한다.

### 1. 입력/출력
- Input: Kafka message (JSON)
- Output: REST POST (JSON)

### 2. 필드 매핑 규칙
- CALLID <- callId
- TXRX <- txrx
- EXT <- ext
- ENDPOINT <- endpoint (boolean)
- SEQ <- seq
- EVENT <- event

- RESULT <- result

### 3. 전송 스킵 규칙
- RESULT가 null/empty/blank이면 REST 전송하지 않고 성공 처리(commit)한다.

### 4. 성공 기준
- REST 응답이 HTTP 200이면 성공으로 간주한다.

### 5. 실패 및 재시도 정책
- HTTP 200이 아니면 실패로 간주하고 재시도한다.
- 네트워크 오류/타임아웃도 재시도 대상이다.
- 재시도는 무한 반복한다.
- Backoff: {여기에 수치 기입: 예 1s → 2s → 5s → 10s(상한)}
- Timeout:
    - connect timeout: {예 2s}
    - response timeout: {예 10s}

### 6. Kafka Offset Commit 정책
- REST 성공(HTTP 200) 또는 전송 스킵(RESULT empty) 시에만 commit 한다.
- 실패(200 아님/예외) 시 commit 하지 않는다.

### 7. 중복 처리 정책
- Dedup 미구현.
- 재시도/커밋 실패로 인해 중복 REST 요청이 발생할 수 있음을 허용한다.

### 8. 오류 처리/관측성
- 로그 필수 키: callId, ext, seq, event, endpoint
- 실패 로그에 HTTP status / exception / retryCount 포함
