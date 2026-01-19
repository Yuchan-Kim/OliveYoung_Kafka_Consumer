# Change / Decision Log

## 2026-01-19
- [DECISION] REST 200 수신 후에만 Kafka offset commit 한다.
- [DECISION] HTTP 200이 아니면 무한 재시도한다. (중복 가능 수용)
- [DECISION] RESULT empty면 전송 스킵 후 commit 한다.
- [CHANGE] WebClient 기반 REST Sender 추가 (timeout/backoff 설정 예정)
- [NOTE] BACKOFF -> 실패 후 다음 요청을 보내기 전까지 기다리는 시간
- [NOTE] TIMEOUT -> REST 요청이 응답을 기다리는 최대 시간


