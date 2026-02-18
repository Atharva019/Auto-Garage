# AutoNexa (Auto Garage Master)

AutoNexa is a comprehensive Android application designed for garage owners and mechanics to streamline their workshop operations. From managing job cards to generating professional PDF invoices, AutoNexa provides a complete digital solution for modern auto garages.

## 🚀 Features

- **Dashboard**: Real-time overview of pending jobs, revenue stats, and low-stock alerts.
- **Job Card Management**: Create and track vehicle service progress from entry to completion.
- **Customer Directory**: Maintain a detailed database of customers and their vehicle history.
- **Inventory Tracking**: Manage spare parts and consumables with automated low-stock warnings.
- **Invoice Generation**: Generate professional PDF invoices using iText7 integration.
- **Staff/Worker Management**: Track workshop staff and assign tasks.
- **Reports & Analytics**: Gain insights into revenue growth and workshop performance.
- **Data Persistence**: Offline-first approach using Room database.

## 🛠 Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Dependency Injection**: [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- **Database**: [Room](https://developer.android.com/training/data-storage/room)
- **Navigation**: Compose Navigation
- **Asynchronous Programming**: Coroutines & Flow
- **Architecture**: MVVM with Clean Architecture principles
- **PDF Generation**: iText7
- **Settings Storage**: DataStore Preferences

## 📁 Project Structure

```text
app/src/main/java/com/autogarage/
├── data/           # Data Layer: Repositories, DAOs, and Room Database
├── domain/         # Domain Layer: Entities and Use Cases
├── presentation/   # UI Layer: ViewModels, Composables, and Theme
│   ├── ui/         # Screen-specific UI components
│   └── navigation/ # App navigation logic
├── di/             # Hilt modules for Dependency Injection
├── util/           # Helper classes and extensions
└── worker/         # Background tasks (WorkManager)
```

## ⚙️ Installation Guide

1. **Prerequisites**: 
   - Android Studio Koala or newer.
   - JDK 17.
   - Android Device/Emulator with API level 26 (Oreo) or higher.

2. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/AutoGarage.git
   ```

3. **Open in Android Studio**:
   - Select `File > Open` and navigate to the project folder.
   - Wait for Gradle sync to complete.

4. **Run the App**:
   - Select your device/emulator and click the **Run** button.

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.
