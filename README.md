# Smart Inventory & Supply Chain Application 📦

A comprehensive, enterprise-grade Java application designed to manage warehouse inventory, process Point of Sale (POS) transactions, track supply chain logistics, and visualize data analytics in real-time.

## 🎯 Objectives
* To create a robust system that prevents stockouts using automated low-stock alerts.
* To bridge the gap between warehouse management and customer-facing sales (POS).
* To provide business owners with real-time visual analytics of their inventory distribution and financial value.
* To implement secure, user-specific data management with authentication.

## 🚀 Key Outcomes
* **Functional POS System:** Instantly deducts stock upon checkout and generates official text-based receipts.
* **Smart Supply Chain:** AI-driven restock recommendations and order tracking.
* **Live Analytics:** Dynamic Pie, Bar, and Line charts that redraw instantly as data changes.
* **Secure Operations:** Password-protected account deletion, secure workspace notes, and email-based password recovery.

## 💻 Advanced Java Concepts Utilized
* **Java Swing & AWT:** Used extensively for creating a modern, multi-tabbed Graphical User Interface, including custom rendering for tables and dynamic dark mode toggling.
* **Multithreading & Concurrency:** Utilized `SwingUtilities.invokeLater()` and background `Thread` processes to handle email dispatching without freezing the main UI.
* **File I/O & Serialization:** Implemented `BufferedReader/Writer` for robust CSV database management, importing/exporting reports, and generating receipt text files.
* **Java Collections Framework:** Advanced use of `Map`, `HashMap`, and `List` to manage product distributions, track activity logs, and link user sessions.
* **Object-Oriented Design:** Encapsulated logic using dedicated classes (`POSModule`, `InventoryDatabase`, `Product`, `EmailService`).

## 👥 Team Contributions
* **[Student Name 1]:** [E.g., Designed the GUI, implemented Dark Mode, and built the Analytics Charts]
* **[Student Name 2]:** [E.g., Built the File I/O Database, Authentication system, and CSV import/export]
* **[Student Name 3]:** [E.g., Developed the POS Module, Email Service integration, and receipt generation]