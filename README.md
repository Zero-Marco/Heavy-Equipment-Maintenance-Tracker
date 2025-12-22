# Heavy Equipment Maintenance Tracker 🚜

A robust Android application built to manage heavy machinery fleets and track complex maintenance logs. This project demonstrates modern Android development practices, including offline-first architecture, dynamic UI components, and automated PDF reporting.

## 🚀 Key Features
* **Fleet Management:** Add, edit, and track various heavy equipment with photo support.
* **Dynamic Maintenance Logs:** Track service records with dynamic input rows for parts, mechanics, and miscellaneous expenses.
* **Automated PDF Reporting:**
    * Generate individual repair reports.
    * Export machine service history based on custom date ranges using `MaterialDatePicker`.
* **Image Management:** Efficient high-resolution image handling using Glide and Room URI persistence.
* **Advanced Search:** Real-time filtering of maintenance records using Kotlin Flow and Room's reactive queries.

## 🛠 Tech Stack & Architecture
This project follows **Clean Architecture** principles and the **MVVM** design pattern.

* **Language:** Kotlin
* **Dependency Injection:** Hilt (Dagger)
* **Database:** Room (Handling complex 1-to-Many and 1-to-1 relationships with `@Relation` and `@Embedded`).
* **Asynchronous Logic:** Kotlin Coroutines & Flow (StateFlow for UI state management).
* **Navigation:** Jetpack Navigation Component with SafeArgs.
* **UI Components:** Material Design 3, ViewBinding, and ListAdapter + DiffUtil for high-performance RecyclerViews.
* **Image Loading:** Glide (Optimized downsampling to prevent OOM errors).
* **Reporting:** Native Android `PdfDocument` API.

## 🏗 Project Structure & Clean Code
The project emphasizes maintainability through:
* **Utility Objects:** Centralized date formatting (`DateUtils`) and global values (`Constants`).
* **Extension Functions:** Simplified UI logic (e.g., `View.visible()`, `View.gone()`).
* **Validation:** Input validation logic moved to ViewModels to ensure business rules are unit-testable.
* **POJO Encapsulation:** Calculated properties (like `totalCost`) kept within Relationship POJOs to keep the UI layer "dumb."

## 🧠 Challenges Overcome
* **Memory Management:** Handled high-resolution camera images by implementing Glide downsampling and `largeHeap` configuration to prevent `Canvas: trying to draw too large bitmap` errors.
* **Relational Data:** Designed a complex Room database schema to link maintenance logs with multiple sub-tables (Parts, Mechanics, Expenses, Images) while maintaining a single source of truth.
* **Permission Security:** Implemented `FileProvider` with temporary URI grants to securely share PDF reports with external applications.