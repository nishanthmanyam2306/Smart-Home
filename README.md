# 🏠 Smart Home — AI-Powered Android App

<div align="center">

An enterprise-grade Smart Home control and monitoring Android application powered by Google Gemini AI.

</div>

---

## 📱 About

**Smart Home** is a fully featured Android application for controlling and monitoring smart home devices. It leverages the power of **Google Gemini AI** via a server-side API to deliver an intelligent, responsive, and automated home management experience.

Built using **Google AI Studio**, this app demonstrates the integration of modern Android development practices with cutting-edge AI capabilities.

---

## ✨ Features

- 🤖 **AI-Powered Automation** — Powered by Google Gemini AI for smart decision-making
- 🗄️ **Room Database** — Persistent local storage for device states and schedules
- ⏰ **Scheduling Engine** — Set timers and schedules for home devices
- ⚡ **Automation Engine** — Create custom automation rules and triggers
- 🎨 **Custom Styles** — Clean, modern UI built with Jetpack Compose
- 📡 **Real-Time Monitoring** — Live status updates for all connected devices

---

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| **Kotlin** | Primary programming language |
| **Jetpack Compose** | Modern declarative UI toolkit |
| **Room Database** | Local data persistence |
| **Google Gemini AI** | Server-side AI capabilities |
| **Google AI Studio** | App generation and prototyping |
| **Gradle (KTS)** | Build system |

---

## 🚀 Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (latest stable version)
- A **Gemini API Key** — get one free at [Google AI Studio](https://aistudio.google.com)
- Android device or emulator (API level 26+)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/smart-home.git
   cd smart-home
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select **Open** and choose the project directory
   - Let Android Studio sync and resolve dependencies

3. **Set up your API Key**

   Create a `.env` file in the root project directory:
   ```
   GEMINI_API_KEY=your_api_key_here
   ```
   *(See `.env.example` for reference)*

4. **Fix signing config**

   Open `app/build.gradle.kts` and remove this line:
   ```kotlin
   signingConfig = signingConfigs.getByName("debugConfig")
   ```

5. **Run the app**
   - Connect your Android device via USB (enable **Developer Mode** and **USB Debugging**)
   - Or use an Android Emulator
   - Hit the ▶️ **Run** button in Android Studio

---

## 📂 Project Structure

```
smart-home/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/         # Kotlin source files
│   │   │   ├── res/          # Resources (layouts, drawables, etc.)
│   │   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 🔑 API Key Setup

This app requires a **Google Gemini API Key** for AI features.

1. Visit [Google AI Studio](https://aistudio.google.com)
2. Sign in with your Google account
3. Click **Get API Key** → **Create API Key**
4. Copy the key and paste it into your `.env` file

> ⚠️ **Never commit your API key to GitHub.** The `.env` file is already in `.gitignore`.

---

## 📸 Screenshots

> *(Add your app screenshots here)*

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!

1. Fork the repository
2. Create a new branch (`git checkout -b feature/your-feature`)
3. Commit your changes (`git commit -m 'Add some feature'`)
4. Push to the branch (`git push origin feature/your-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgements

- [Google AI Studio](https://ai.studio) — For app generation
- [Google Gemini API](https://ai.google.dev) — For AI capabilities
- [Jetpack Compose](https://developer.android.com/jetpack/compose) — For modern Android UI

---

<div align="center">
Made with ❤️ using Google AI Studio
</div># 🏠 Smart Home — AI-Powered Android App

<div align="center">

![Android](https://img.shields.io/badge/Platform-Android-green?style=for-the-badge&logo=android)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple?style=for-the-badge&logo=kotlin)
![Gemini AI](https://img.shields.io/badge/AI-Gemini-blue?style=for-the-badge&logo=google)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-orange?style=for-the-badge&logo=jetpackcompose)
![Google AI Studio](https://img.shields.io/badge/Built%20with-Google%20AI%20Studio-yellow?style=for-the-badge&logo=google)

An enterprise-grade Smart Home control and monitoring Android application powered by Google Gemini AI.

</div>

---

## 📱 About

**Smart Home** is a fully featured Android application for controlling and monitoring smart home devices. It leverages the power of **Google Gemini AI** via a server-side API to deliver an intelligent, responsive, and automated home management experience.

Built using **Google AI Studio**, this app demonstrates the integration of modern Android development practices with cutting-edge AI capabilities.

---

## ✨ Features

- 🤖 **AI-Powered Automation** — Powered by Google Gemini AI for smart decision-making
- 🗄️ **Room Database** — Persistent local storage for device states and schedules
- ⏰ **Scheduling Engine** — Set timers and schedules for home devices
- ⚡ **Automation Engine** — Create custom automation rules and triggers
- 🎨 **Custom Styles** — Clean, modern UI built with Jetpack Compose
- 📡 **Real-Time Monitoring** — Live status updates for all connected devices

---

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| **Kotlin** | Primary programming language |
| **Jetpack Compose** | Modern declarative UI toolkit |
| **Room Database** | Local data persistence |
| **Google Gemini AI** | Server-side AI capabilities |
| **Google AI Studio** | App generation and prototyping |
| **Gradle (KTS)** | Build system |

---

## 🚀 Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (latest stable version)
- A **Gemini API Key** — get one free at [Google AI Studio](https://aistudio.google.com)
- Android device or emulator (API level 26+)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/smart-home.git
   cd smart-home
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select **Open** and choose the project directory
   - Let Android Studio sync and resolve dependencies

3. **Set up your API Key**

   Create a `.env` file in the root project directory:
   ```
   GEMINI_API_KEY=your_api_key_here
   ```
   *(See `.env.example` for reference)*

4. **Fix signing config**

   Open `app/build.gradle.kts` and remove this line:
   ```kotlin
   signingConfig = signingConfigs.getByName("debugConfig")
   ```

5. **Run the app**
   - Connect your Android device via USB (enable **Developer Mode** and **USB Debugging**)
   - Or use an Android Emulator
   - Hit the ▶️ **Run** button in Android Studio

---

## 📂 Project Structure

```
smart-home/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/         # Kotlin source files
│   │   │   ├── res/          # Resources (layouts, drawables, etc.)
│   │   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 🔑 API Key Setup

This app requires a **Google Gemini API Key** for AI features.

1. Visit [Google AI Studio](https://aistudio.google.com)
2. Sign in with your Google account
3. Click **Get API Key** → **Create API Key**
4. Copy the key and paste it into your `.env` file

> ⚠️ **Never commit your API key to GitHub.** The `.env` file is already in `.gitignore`.

---

## 📸 Screenshots

> *(Add your app screenshots here)*

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!

1. Fork the repository
2. Create a new branch (`git checkout -b feature/your-feature`)
3. Commit your changes (`git commit -m 'Add some feature'`)
4. Push to the branch (`git push origin feature/your-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgements

- [Google AI Studio](https://ai.studio) — For app generation
- [Google Gemini API](https://ai.google.dev) — For AI capabilities
- [Jetpack Compose](https://developer.android.com/jetpack/compose) — For modern Android UI

---

<div align="center">
Made with ❤️ using Google AI Studio
</div>
