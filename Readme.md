# 슬기로운 숭실생활

숭실대학교 학생에게 필요한 학사 정보를 한곳에서 확인할 수 있도록 개발 중인 Android 애플리케이션입니다.

## 협업 규칙

### `main` 브랜치 보호

`main` 브랜치에는 직접 Push할 수 없습니다. 모든 변경 사항은 별도의 작업 브랜치에서 작성한 후 Pull Request를 통해 반영해야 합니다.

1. 최신 `main` 브랜치를 기준으로 작업 브랜치를 생성합니다.
2. 작업 범위에 적용되는 `AGENTS.md`를 확인합니다.
3. 코드 수정과 로컬 검증을 완료합니다.
4. 작업 브랜치를 원격 저장소에 Push합니다.
5. `main` 브랜치를 대상으로 Pull Request를 생성합니다.
6. 리뷰와 필수 CI를 통과한 후 병합합니다.

### `AGENTS.md` 활용

- 작업을 시작하기 전에 저장소 루트부터 현재 작업 디렉터리까지의 `AGENTS.md`를 확인하고 반드시 준수합니다.
- 루트 [AGENTS.md](AGENTS.md)는 프로젝트 전체에 적용되는 공통 규칙을 관리합니다.
- 특정 모듈이나 디렉터리에 별도 규칙이 필요하면 해당 위치에 `AGENTS.md`를 생성합니다.
- 개발 과정에서 규칙이나 구조가 변경되면 관련 `AGENTS.md`도 함께 수정합니다.
- 하위 디렉터리의 `AGENTS.md`는 루트 규칙을 기반으로 해당 영역에 필요한 세부 규칙을 추가합니다.

### 브랜치 컨벤션

브랜치 이름은 `<type>/<short-description>` 형식을 사용합니다. 설명은 영문 소문자와 하이픈으로 간결하게 작성합니다.

| Type | 용도 | 예시 |
| --- | --- | --- |
| `feature` | 새로운 기능 | `feature/dashboard-refresh` |
| `fix` | 버그 수정 | `fix/bottom-bar-inset` |
| `refactor` | 동작 변경 없는 구조 개선 | `refactor/lms-repository` |
| `docs` | 문서 수정 | `docs/contribution-guide` |
| `test` | 테스트 추가 및 수정 | `test/dashboard-view-model` |
| `chore` | 설정, 의존성, CI 등 기타 작업 | `chore/android-ci` |
| `hotfix` | 운영 중 긴급 수정 | `hotfix/login-crash` |

이슈 번호를 사용하는 경우 타입 뒤에 함께 표기할 수 있습니다.

```text
feature/123-dashboard-refresh
fix/245-bottom-bar-inset
```

### Pull Request

- 하나의 Pull Request에는 하나의 목적을 가진 변경만 포함합니다.
- 변경 내용과 검증 방법을 Pull Request 본문에 작성합니다.
- 비밀값, 계정 정보, `local.properties`는 커밋하지 않습니다.
- 필수 Status Check인 `프로젝트 검증`이 성공해야 병합할 수 있습니다.
- CI는 APK를 생성하지 않고 소스 컴파일, 단위 테스트, Android Lint를 수행합니다.

로컬에서는 다음 명령으로 CI와 동일하게 검증할 수 있습니다.

```bash
./gradlew :data:compileKotlin :app:compileDebugSources :app:testDebugUnitTest :app:lintDebug
```
