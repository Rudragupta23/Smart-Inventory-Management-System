# 📦 Smart Inventory & Enterprise Supply Chain System

A comprehensive, enterprise-grade desktop application built in Java. This system seamlessly bridges the gap between backend warehouse management and customer-facing Point of Sale (POS) operations. It features real-time data analytics, automated low-stock email alerts, and a completely custom-built modern UI with Dark Mode support.

---

## 📑 Table of Contents
1. [Objectives & Outcomes](#-objectives--outcomes)
2. [Core Modules & Features](#-core-modules--features)
3. [Screenshots](#-system-screenshots)
4. [Advanced Technical Concepts](#-advanced-technical-concepts-used)
5. [Installation & Setup](#-installation--setup)
6. [Team & Contributors](#-team--contributors)

---

## 🎯 Objectives & Outcomes

### Objectives
* **Eliminate Stockouts:** Implement automated tracking and AI-recommended restock alerts to prevent inventory shortages.
* **Unified Operations:** Create a single, cohesive platform that handles both B2B supply chain logistics and B2C Point of Sale transactions.
* **Data-Driven Decisions:** Provide business owners with live, visual representations of their financial standing and category distributions.

### Project Outcomes
* **Functional POS System:** A digital cash register that instantly calculates totals, deducts physical stock upon checkout, and generates official `.txt` receipts.
* **Smart Supply Chain:** The system intelligently identifies low stock, recommends reorder quantities, and logs all physical receiving events.
* **Secure Authentication:** Features a robust login system with encrypted credential storage, user-specific profiles, and a secure "Forgot Password" flow utilizing secure email OTPs.

---

## 🚀 Core Modules & Features

### 1. 🛡️ Authentication & Security
* **Modern Split-Pane UI:** Sleek, web-style login and registration forms.
* **Live Validation:** Real-time RegEx validation for email formats and phone numbers.
* **Secure Recovery:** Automated 6-digit OTP codes sent directly to user emails via Java `SSLSocket` integration for password resets.

### 2. 📊 Inventory Dashboard
* **Full CRUD Operations:** Add, update, and delete products with specific lead times and supplier data.
* **Smart CSV Import/Export:** Bulk upload inventory data. The system automatically detects duplicate Product IDs and safely auto-increments them.
* **Barcode Generation:** Converts Product IDs into visually accurate, scannable PNG Barcode images.
* **Dynamic Filtering:** Instantly isolate low-stock items (< 20 units) with a single toggle pill.

### 3. 🛒 Point of Sale (POS) & Billing
* **Digital Register:** Split-screen interface for rapid catalog searching and cart building.
* **Safety Protocols:** Cashiers are hard-coded to be unable to sell more stock than physically available.
* **Automated Receipts:** Generates timestamped `Receipt_TXN-12345.txt` files directly to the local machine upon successful checkout.

### 4. 🚚 Supply Chain & Logging
* **Purchase Orders:** One-click restock ordering that calculates deficits to reach a 100-unit optimal stock level.
* **Audit Trails:** A non-editable, timestamped log of every action (sales, deletions, restocks) that can be exported as a secure Audit Log.
* **Manager Alerts:** Instantly dispatches a formatted "Low Stock Alert" email to the registered manager's inbox.

### 5. 📈 Live Analytics
* **Real-Time Rendering:** Custom-built Pie, Bar, and Line charts using `Graphics2D`.
* **Instant Sync:** The exact millisecond a POS sale is completed, the charts instantly redraw to reflect the new financial distributions.

### 6. 🎨 Personalization
* **Bulletproof Dark Mode:** A complete UI overhaul system that dynamically repaints tables, text fields, and panels without relying on external UI libraries.
* **Profile Avatars:** Automatically generates circular letter-avatars or allows users to upload custom PNG/JPG profile pictures.
* **Private Workspace:** Dedicated, auto-saving text area for users to keep personal shift notes.

---

## 📸 System Screenshots


| Login & Authentication | Inventory Dashboard |
| :---: | :---: |
| <img src="images/login.png" width="400" alt="Login Screen"> | <img src="images/dashboard.png" width="400" alt="Dashboard"> |

| Point of Sale (Dark Mode) | Live Analytics |
| :---: | :---: |
| <img src="images/pos_dark.png" width="400" alt="POS Dark Mode"> | <img src="images/analytics.png" width="400" alt="Analytics Charts"> |

| Supply Chain & Logs | Feedback & Team |
| :---: | :---: |
| <img src="images/logs.png" width="400" alt="Supply Chain Logs"> | <img src="images/feedback.png" width="400" alt="Feedback Screen"> |

---

## 🔄 System Workflow

1. User logs in / registers
2. Inventory is loaded from CSV files
3. User can:
   - Manage products
   - Perform sales via POS
4. System updates stock automatically
5. Analytics update in real-time
6. Low stock → Email alert sent


## 💻 Advanced Technical Concepts Used

This project was built entirely from scratch without relying on heavy external frameworks like Spring or JavaFX. It demonstrates a deep understanding of core Advanced Java concepts:

* **Java Swing & AWT Mastery:** Custom `DefaultTableCellRenderer` overrides to force table cells to dynamically adapt to dark mode and custom `Graphics2D` algorithms to draw interactive geometry (Charts, Barcodes, Circular Avatars).
* **Multithreading & Concurrency:** Implementation of background `Thread` processes to handle network requests (Email Dispatching) without freezing the main Event Dispatch Thread (EDT). UI updates are safely queued using `SwingUtilities.invokeLater()`.
* **File I/O & Serialization:** Utilization of `BufferedReader` and `BufferedWriter` to create a local, scalable CSV-based database. Implemented robust `try-catch` blocks to prevent data corruption during read/write cycles.
* **Java Collections Framework:** Advanced utilization of `HashMap<String, String[]>` for constant-time user authentication and `List<Product>` for dynamic table sorting and filtering.
* **Network Sockets:** Bypassed heavy external libraries (like JavaMail API) by utilizing low-level `SSLSocketFactory` to communicate directly with SMTP servers via Base64 encoded streams.

---

## 🛠️ Tech Stack

- **Language:** Java
- **UI:** Java Swing, AWT
- **Database:** CSV-based storage (File I/O)
- **Networking:** SMTP via SSLSocket
- **Graphics:** Graphics2D (Charts & UI)
- **Version Control:** Git & GitHub

```
## 📁 Project Structure
Smart-Inventory-Management-System/
│
├── .gitignore                  # Tells GitHub to ignore compiled .class files and 
├── README.md                   # Your detailed project documentation and report
│
├── images/                     # Folder for your GitHub README screenshots
│   ├── login.png               
│   ├── dashboard.png
│   ├── pos_dark.png
│   ├── analytics.png
│   ├── logs.png
│   └── feedback.png
│
├── src/                        # Your main Java Source Code folder
│   │
│   ├── MainApp.java            # The main application window, Auth system, and UI tabs
│   ├── POSModule.java          # Point of Sale (POS) and billing digital register logic
│   ├── InventoryDatabase.java  # Core backend, File I/O (CSV), and data management
│   ├── Product.java            # Product object blueprint (ID, Name, Qty, Price, etc.)
│   ├── EmailService.java       # Secure SSLSocket logic for sending OTPs and alerts
│   │
│   ├── AnalyticsChart.java     # Custom Graphics2D component for the Bar Chart
│   ├── LineChart.java          # Custom Graphics2D component for the Line Graph
│   └── PieChart.java           # Custom Graphics2D component for the Pie Chart
│
└── [Auto-Generated Files]      # These are created automatically when the app runs
    ├── users_data.csv          # Encrypted user accounts database
    ├── inventory_rudra.csv     # User-specific inventory database
    ├── Receipt_TXN-123.txt     # Generated POS checkout invoices
    ├── Audit_Log_rudra.txt     # Exported security logs
    └── Barcode_ITEM1.png       # Exported barcode images
```

## 🧰 Prerequisites

Make sure you have the following installed:

- Java JDK 8 or higher
- Git (for cloning repository)
- Any IDE (VS Code / IntelliJ / Eclipse)

Check Java version:
```bash
java -version
```

## ⚙️ Installation & Setup

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/Rudragupta23/Smart-Inventory-Management-System.git](https://github.com/Rudragupta23/Smart-Inventory-Management-System.git)

## 🚀 How to Run the Project

### 📂 Step 1: Navigate to the Project Directory
```bash
cd Smart-Inventory-Management-System/src
```

### ⚙️ Step 2: Compile the Java Files
```bash
javac *.java
```

### ▶️ Step 3: Run the Application
```bash
java MainApp
```
