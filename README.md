<p align="center">
  <img src="https://github.com/user-attachments/assets/e85dd935-2b9c-4f6b-bdca-6ff79ff140c6" alt="Sealog. 말하는 순간 기록되는 AI 회의록" />
</p>

# Sealog

화자별 실시간 기록과 AI 요약, 피드백으로 회의 진행을 돕는 앱

## 서비스 소개

회의에서 발언을 받아 적다 보면 다음 이야기를 놓치기 쉽습니다. 대화가 길어질수록 지금까지 논의한 내용을 정리하고, 참가자들이 얼마나 발언했는지 살피기도 어렵습니다.

Sealog는 회의 중 발화를 실시간으로 기록해 화자별로 보여주고, 참가자별 발화 비중과 안건 진행 상태를 표시합니다. AI가 제공하는 중간 요약과 피드백을 보며 지금까지 논의한 내용을 확인할 수 있습니다. 회의가 끝나면 기록과 녹음을 회의록에서 다시 확인할 수 있습니다.

## 시연 화면

| 실시간 발화 기록 | 발화 비중과 중간 요약 | 회의록과 녹음 재생 |
| :---: | :---: | :---: |
| <a href="https://github.com/user-attachments/assets/3c04c29c-e61c-4437-9415-946beec60626"><img src="https://github.com/user-attachments/assets/3c04c29c-e61c-4437-9415-946beec60626" width="216" alt="회의 중 화자 이름과 발화 시각이 붙은 말풍선이 차례로 추가되는 시연" /></a> | <a href="https://github.com/user-attachments/assets/521b3b49-327b-4c9c-976a-2c42018a8614"><img src="https://github.com/user-attachments/assets/521b3b49-327b-4c9c-976a-2c42018a8614" width="216" alt="회의 중 화면을 넘겨 참가자별 발화 비중과 중간 요약을 확인하는 시연" /></a> | <a href="https://github.com/user-attachments/assets/31490a77-7a27-4ab9-b6d6-290cf629b736"><img src="https://github.com/user-attachments/assets/31490a77-7a27-4ab9-b6d6-290cf629b736" width="216" alt="회의록에서 녹음을 재생하고 재생 막대로 다른 시점으로 이동하는 시연" /></a> |
| 누가 언제 말했는지 확인합니다. | 누가 얼마나 말했는지, 지금까지 어떤 이야기가 나왔는지 봅니다. | 재생 중인 발언이 강조되고, 재생 막대로 원하는 구간을 찾을 수 있습니다. |

## 주요 기능

| 단계 | 할 수 있는 일 |
| --- | --- |
| 회의 준비 | 회의 일정과 안건을 등록하고 이메일 주소로 참가자를 추가합니다. 등록된 팀에서 참석할 멤버를 골라 한 번에 추가할 수도 있습니다. |
| 회의 진행 | 화자별 발화 기록과 안건 진행 상태를 확인합니다. 중간 요약, 피드백, 참가자별 발화 비중을 보며 회의를 진행할 수 있습니다. |
| 회의 다시 보기 | 회의 분석, 전체 기록, 피드백 기록 탭에서 회의 내용을 확인합니다. 발화나 시각을 누르면 해당 녹음으로 이동하고, 재생 중인 발화가 강조됩니다. |
| 회의록 정리 | 완료된 회의를 폴더에 모아 봅니다. 회의 제목이나 팀 이름, 폴더 이름으로 원하는 항목을 검색할 수 있습니다. |

## 기술 스택

| 영역 | 기술 |
| --- | --- |
| 언어 및 UI | ![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white) ![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?logo=jetpackcompose&logoColor=white), Material 3, Navigation Compose |
| 상태 관리 및 의존성 주입 | ViewModel, Kotlin Coroutines, Flow, Hilt |
| API 및 실시간 연결 | Retrofit, OkHttp, WebSocket, Server-Sent Events(SSE) |
| 로컬 저장 및 목록 | Jetpack DataStore, Paging 3 |
| 오디오 | Android `AudioRecord`, Media3 ExoPlayer |
| 빌드 및 오류 수집 | Gradle Kotlin DSL, KSP, Firebase Crashlytics |

## 프로젝트 아키텍처

Android 앱은 단일 `:app` 모듈 안에서 Presentation, Domain, Data 계층으로 구성됩니다. 아래 그림은 MVVM의 주요 흐름을 요약합니다. View는 사용자 입력을 ViewModel에 전달하고, StateFlow를 통해 UI State를 관찰합니다. 점선 화살표는 Repository 인터페이스의 구현 관계를 나타냅니다.

![Sealog 계층형 MVVM 아키텍처. View와 ViewModel 사이의 입력 및 화면 상태 전달, Domain과 Data 계층의 관계](https://github.com/user-attachments/assets/53328533-ed57-4c1c-90db-fba9c7dbe430)

## 프로젝트 구조

주요 Kotlin 코드는 `app/src/main/java/com/imhungry/sillok/` 아래에 있습니다.

| 위치 | 역할 |
| --- | --- |
| [`presentation/`](app/src/main/java/com/imhungry/sillok/presentation/) | 화면, ViewModel과 화면 상태, 내비게이션, 회의 진행 서비스 |
| [`domain/`](app/src/main/java/com/imhungry/sillok/domain/) | 회의, 사용자 등 도메인 모델, 유스케이스, Repository 인터페이스 |
| [`data/`](app/src/main/java/com/imhungry/sillok/data/) | Retrofit API, DTO 및 Mapper, Repository 구현, DataStore, PagingSource |
| [`di/`](app/src/main/java/com/imhungry/sillok/di/) | Hilt 공통 모듈 |
| [`ui/`](app/src/main/java/com/imhungry/sillok/ui/), [`res/`](app/src/main/res/) | Compose 테마, 공통 UI 컴포넌트, 이미지, 문자열, 폰트 |
| [`src/debug/`](app/src/debug/), [`src/release/`](app/src/release/) | 빌드 유형에 맞는 Repository와 실시간 이벤트 소스 연결 |
| [`gradle/`](gradle/) | Gradle Wrapper와 버전 카탈로그 |

## 실행 준비

| 항목 | 기준 |
| --- | --- |
| IDE | Android Studio |
| Gradle JDK | 17 |
| Android SDK | SDK Platform 35, minSdk 24 |
| 빌드 도구 | Gradle Wrapper 8.12.1, Android Gradle Plugin 8.8.1 |

SDK 버전과 플러그인 설정은 [앱 빌드 설정](app/build.gradle.kts)과 [버전 카탈로그](gradle/libs.versions.toml)에서 확인할 수 있습니다. Gradle JDK 기준은 [AGP 8.8 호환성 표](https://developer.android.com/build/releases/agp-8-8-0-release-notes#compatibility)와 같습니다.

### `local.properties`

저장소 루트의 `local.properties`에 팀에서 사용하는 서버 주소와 OAuth 설정을 입력합니다. Android Studio가 생성한 `sdk.dir`은 유지합니다.

```properties
base_url=https://api.example.invalid/
oauth_client_id=
redirect-uris.app=
```

`base_url`은 Retrofit과 회의 실시간 연결에서 함께 사용합니다. 끝에 `/`가 붙은 실제 서버 주소를 입력합니다. `oauth_client_id`와 `redirect-uris.app`에는 Google OAuth 클라이언트 ID와 등록된 리디렉션 URI를 입력합니다. 회의 장소 검색을 사용하려면 같은 파일에 `naver_client_id`와 `naver_client_secret`도 설정합니다.

### Firebase

Firebase에서 애플리케이션 ID `com.imhungry.sillok`에 맞는 `google-services.json`을 받아 `app/google-services.json`에 둡니다. 이 파일과 `local.properties`는 Git에서 제외됩니다. [Git 제외 경로](.gitignore)

## 실행 방법

1. Android Studio에서 저장소 루트를 열고 Gradle JDK를 17로 설정합니다.
2. 위의 설정 파일을 준비한 뒤 Gradle을 동기화합니다.
3. `app` 실행 구성과 Android 기기를 선택하고 **Run**을 실행합니다.

Debug 빌드는 테스트용 이벤트와 회의 데이터를 사용합니다. 로그인과 일부 API에는 서버 설정이 필요하며, 실제 실시간 연결은 Release 빌드에서 사용합니다.

저장소 루트에서 Debug APK를 만들거나 코드를 검사할 수 있습니다.

```bash
bash ./gradlew :app:assembleDebug
bash ./gradlew :app:testDebugUnitTest :app:lintDebug
```

Windows에서는 `bash ./gradlew` 대신 `.\gradlew.bat`을 사용합니다. Debug APK는 `app/build/outputs/apk/debug/`에 생성됩니다.

현재 Kotlin 2.0.0과 KSP 2.0.0-Beta4-1.0.18을 사용하며, 빌드 시 버전 호환성 경고가 나타날 수 있습니다. [버전 카탈로그](gradle/libs.versions.toml)

## 팀원

| Android | Android | Backend | AI | PM |
| :---: | :---: | :---: | :---: | :---: |
| <img src="https://avatars.githubusercontent.com/u/108224432?s=160" width="72" alt="" /> | <img src="https://avatars.githubusercontent.com/u/86427017?s=160" width="72" alt="" /> | <img src="https://avatars.githubusercontent.com/u/85011588?s=160" width="72" alt="" /> | <img src="https://avatars.githubusercontent.com/u/124423175?s=160" width="72" alt="" /> | <img src="https://avatars.githubusercontent.com/u/122345597?s=160" width="72" alt="" /> |
| [조은경](https://github.com/eungyeong12) | [yujin](https://github.com/JeongYuu) | [정원영](https://github.com/gardenzeeero) | [SangJung](https://github.com/sangjung0) | [jianjianjianjianjianjianjian](https://github.com/jianjianjianjianjianjianjian) |
