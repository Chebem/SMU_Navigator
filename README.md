# 📍 SMU Navigator

A **bilingual Android mobile application** designed to help students and visitors navigate **Semyung University** and explore **Jecheon City**.  
Supports **English 🇬🇧** and **Korean 🇰🇷**, with real-time campus information, local integration, and a modern map-based UI.

---

## 🚀 Features

- **Interactive Campus Map** – View buildings, dorms, restaurants, cafes, and stores.
- **Custom Marker Overlays** – Detailed info for each location (name, address, opening hours, contact).
- **Walking, Biking, and Scooter Routes** – Powered by Google Directions API.
- **Profile System** – Create and update your profile with name, bio, department, and profile picture.
- **Post & Social Features** – Upload photos, view others’ posts, follow/unfollow users.
- **Favorites** – Save and quickly access your favorite locations.
- **Bilingual Support** – Automatic language switching based on device language.
- **External Map Integration** – Open routes in Kakao Map or Naver Map.

---

## 📸 Screenshots

| Home Screen | Map with Markers | Profile Page |
|-------------|------------------|--------------|


|![FF5BB12A-ADBF-44F4-A6AE-5ECBBBFE1F0A_1_105_c](https://github.com/user-attachments/assets/e215269b-48f0-41fa-8042-ca1728b717fc) | ![A8A0D244-8CB3-43FD-B879-21B3B1D49557](https://github.com/user-attachments/assets/a18a16a9-516a-428a-8396-95903870566a)
 | ![Profile](assets/screenshots/profile.png) |

---

## 🛠 Tech Stack

- **Language:** Java
- **Framework:** Android SDK
- **Map Services:** Google Maps API, Kakao Local API
- **Backend:** Firebase Authentication, Realtime Database, Firebase Storage
- **UI:** ConstraintLayout, Custom Views, ChipNavigationBar
- **Image Loading:** Glide

---

## 📂 Project Structureapp/
├── java/com/example/smunavigator2/
│   ├── Activity/        # All Activities
│   ├── Adapter/         # RecyclerView adapters
│   ├── Domain/          # Data models
│   ├── ViewModel/       # MVVM ViewModels
│   └── utils/           # Helper classes
└── res/
├── layout/          # XML UI layouts
├── values/          # Strings, colors, styles
└── drawable/        # Icons, shapes, images---

## ⚙️ Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/YourUsername/SMU_Navigator.git

   2.	Open the project in Android Studio.
	3.	Add your Google Maps API key to:.	Configure Firebase:
	•	Add google-services.json to app/ directory.
	•	Enable Authentication, Realtime Database, and Storage in Firebase Console.
	5.	Build & Run the app on an emulator or physical device.

⸻

📅 Roadmap
	•	Offline Map Mode
	•	Dark Mode UI
	•	Campus Event Calendar
	•	Multi-language expansion (Chinese, Japanese)
	•	Push Notifications for announcements
⸻

📜 License

This project is licensed under the MIT License – see the LICENSE file for details.

⸻

🙌 Acknowledgements
	•	Semyung University
	•	Google Maps API
	•	Firebase
	•	Kakao Developers
