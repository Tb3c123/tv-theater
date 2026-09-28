# 📺 TV Theater - Rạp Phim Gia Đình Trên Android TV

Ứng dụng xem phim giải trí gia đình chuẩn trải nghiệm **10-foot UI**, phong cách Netflix trên nền tảng **Android TV / Google TV**, phát triển hoàn toàn bằng **Kotlin** và **Jetpack Compose for TV**.

![Android TV](https://img.shields.io/badge/Platform-Android%20TV%20%7C%20Google%20TV-3DDC84?logo=android)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.23-7F52FF?logo=kotlin)
![Compose for TV](https://img.shields.io/badge/Compose_for_TV-1.0.0--alpha10-4285F4?logo=jetpackcompose)
![Media3 ExoPlayer](https://img.shields.io/badge/Player-AndroidX_Media3_ExoPlayer-FF0000?logo=youtube)
![CI/CD](https://img.shields.io/badge/CI%2FCD-GitHub_Actions-2088FF?logo=githubactions)
![Tests](https://img.shields.io/badge/Tests-26%2F26_Passing_(100%25)-brightgreen)

---

## 🌟 Tính Năng Nổi Bật

1. **Giao Diện 10-foot TV & Điều Khiển D-Pad**:
   - Thiết kế tối ưu cho khoảng cách xem từ 3 mét trên màn hình lớn.
   - Hiệu ứng Remote Focus mượt mà: phóng to `1.08x`, viền sáng phát quang màu **Ice Blue** (`#38BDF8`) nổi bật trên nền **Cinema Dark Navy** (`#0B1120`).
   - Tích hợp **Bàn phím ảo trực tiếp trên TV (On-Screen Virtual Keyboard)** giúp gõ tìm kiếm phim dễ dàng chỉ bằng 4 phím mũi tên và nút OK.
2. **Thuật Toán Khám Phá Ngẫu Nhiên (Random Discovery Engine)**:
   - Tự động xáo trộn và làm mới 3 thể loại ngẫu nhiên kèm banner nổi bật ở mỗi lần khởi động hoặc khi bấm nút `🎲 Khám Phá Mới`.
3. **Trình Phát Video Lai (Hybrid Video Player Engine)**:
   - **Primary**: Trình phát gốc **AndroidX Media3 (ExoPlayer)** với luồng giải mã HLS trực tiếp.
   - **Fallback**: Tự động chuyển đổi sang **WebView Player** tối ưu phần cứng (Hardware Accelerated) khi gặp liên kết nhúng bảo mật hoặc CDN hạn chế.
   - Hỗ trợ phím tắt điều khiển từ xa: tua nhanh `+10s` (D-Pad Phải), tua lùi `-10s` (D-Pad Trái), Tạm dừng/Phát (D-Pad Center).
4. **Lưu Trữ Lịch Sử & Tiếp Tục Xem**:
   - Tự động lưu tiến độ phát vào **Room Database** mỗi 5 giây.
   - Hiển thị hàng **"Tiếp Tục Xem"** ngay đầu trang chủ để gia đình xem tiếp phim bất kỳ lúc nào.
5. **Không Bộ Lọc Nội Dung (Unfiltered Viewing)**:
   - Giữ nguyên toàn bộ kho phim phong phú từ hệ thống API `phim.nguonc.com`.

---

## 🏗️ Kiến Trúc Ứng Dụng (Clean Architecture & MVVM)

```
app/src/main/java/com/tvtheater/app/
├── data/
│   ├── api/          # Retrofit Service & DTOs (NguonC API)
│   ├── local/        # Room Database, Entities, Reactive Flow DAOs
│   └── repository/   # Repository Implementations
├── di/               # AppContainer (Dependency Injection Provider)
├── domain/
│   ├── model/        # Domain Entities (Movie, MovieDetail, EpisodeItem)
│   ├── repository/   # Domain Repository Interfaces
│   └── usecase/      # Business UseCases (RandomCatalog, History, StreamExtractor)
└── presentation/
    ├── components/   # TV 10-foot UI: MovieCard, MovieRow, HeroBanner, TVTopBar
    ├── home/         # HomeScreen & HomeViewModel
    ├── detail/       # DetailScreen & DetailViewModel
    ├── search/       # SearchScreen & SearchViewModel (Virtual TV Keyboard)
    ├── player/       # PlayerScreen, NativePlayerView, FallbackWebViewPlayer, OSD
    ├── navigation/   # TVNavGraph (Safe UTF-8 URL encoding)
    └── theme/        # Soft Blue & Cinema Dark Navy Color Tokens
```

---

## 🚀 Hướng Dẫn Cài Đặt & Chạy Ứng Dụng

### 1. Yêu cầu môi trường
- **Java**: OpenJDK 17 trở lên (`JAVA_HOME`)
- **Android SDK**: API 34, Build-tools 34.0.0
- **Thiết bị**: Android TV thật (Sony, TCL, Xiaomi, Chromecast with Google TV) hoặc Android TV Emulator (1080p).

### 2. Chạy Kiểm Thử Tự Động (Unit Tests)
```bash
./gradlew testDebugUnitTest
```
> Kết quả: **26/26 tests PASS (100%)** trong ~1.16 giây.

### 3. Build File Cài Đặt (APK)
```bash
./gradlew assembleDebug
```
File APK sẽ được tạo tại: `app/build/outputs/apk/debug/app-debug.apk`.

### 4. Cài Đặt Lên Android TV Qua ADB
```bash
# Kết nối với TV qua mạng Wi-Fi nội bộ
adb connect <IP_CUA_TV>:5555

# Cài đặt APK
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 🐳 Docker & CI/CD

### Build APK Bằng Docker (Không Cần Cài Đặt Android SDK Cục Bộ)
```bash
# Sử dụng Docker Compose để tự động build và xuất APK ra thư mục ./dist/
docker compose up
```

### GitHub Actions CI/CD
Repository đã được cấu hình tự động:
- Kiểm tra mã nguồn và chạy toàn bộ unit tests trên mỗi Pull Request và commit vào `main`.
- Chạy Android Lint kiểm toán bảo mật.
- Biên dịch và tự động tải file `tv-theater-debug.apk` lên tab **Artifacts** của GitHub Actions để tải về trực tiếp.

---

## 📋 Quản Lý Task Dự Án (Jira Cloud)
Toàn bộ danh mục 34 Epics và Tasks được đồng bộ hóa với Jira Cloud Board tại:
- **Tài liệu theo dõi**: [`docs/JIRA_TASKS.md`](docs/JIRA_TASKS.md)
- **Jira Cloud Board**: [Home Lab (SCRUM)](https://khongdung165.atlassian.net/jira/software/projects/SCRUM/boards)

---
*Phát triển bởi Principal Software Engineer theo quy chuẩn Agent-Pack SDLC System.*
