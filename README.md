# 🔥 Torch Browser

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)
![JavaFX](https://img.shields.io/badge/JavaFX-WebKit-blue?style=flat-square)
![Maven](https://img.shields.io/badge/Build-Maven-C71A36?style=flat-square&logo=apachemaven)
![Status](https://img.shields.io/badge/Status-Completed-success?style=flat-square)

Torch Browser is a custom-built, lightweight web browser developed entirely in Java. It bridges the classic **Java Swing** UI framework with the modern **JavaFX WebKit** rendering engine to create a fully functional, multi-tabbed internet experience. 

Designed with a sleek "Charcoal & Ember" dark mode UI, Torch provides an optimized browsing experience tailored for low-resource hardware (like integrated graphics) while implementing core Object-Oriented Programming (OOP) architectures.

## ✨ Key Features

* **Multi-Tab Architecture:** Dynamic tab spawning, active memory management, and explicit Garbage Collection to keep RAM usage low.
* **Smart Omnibox:** Automatically detects whether input is a URL or a search query, routing non-URLs directly to Google Search.
* **👻 Ghost Mode:** A localized incognito mode that instantly suspends all File I/O Byte/Character streams to prevent history and sync logging.
* **Intelligent Auto-Fallback:** Automatically detects heavy, modern AI sites (ChatGPT, Gemini) or Cloudflare-blocked domains and seamlessly routes them to the host's native system browser to prevent engine crashes.
* **Unified Control Center:** Manage localized user profiles, clear browsing data, and view history logs via Java Character Streams.
* **Persistent Bookmarks:** A functional UI bookmarks bar driven by standard local File I/O.
* **Hardware Accelerated:** Injects `d3d` and `forceGPU` system properties at boot for smoother rendering on integrated graphics.

## 🛠️ Tech Stack

* **Language:** Java 17
* **UI Framework:** Java Swing & AWT
* **Web Engine:** JavaFX WebKit (`javafx-web`, `javafx-swing`)
* **Build Tool:** Apache Maven (w/ Shade Plugin for Fat JAR deployment)
* **Wrapper:** Launch4j (for `.exe` deployment)

## 🚀 Installation & Deployment

### Prerequisites
* Java Development Kit (JDK) 17 or higher
* Apache Maven

### 1. Build from Source
Clone the repository and build the Fat JAR using Maven:
#### Install Maven and Java
```bash
choco install maven openjdk -y
```
#### Refresh the environment (so PowerShell recognizes 'mvn')
```bash
refreshenv
```
#### Clone and Build
```bash 
git clone https://github.com/Donuts-procodes/Torch-Browser.git
cd Torch-Browser
mvn clean package
```
### 2. Run the Application
Once compiled, you can run the executable JAR directly from the target directory:


```Bash
java -jar target/TorchBrowser-1.0.jar
```
Note: For Windows users, a native TorchBrowser.exe can be generated using Launch4j by targeting the compiled Fat JAR and utilizing the Boot.java dummy class to bypass JavaFX module path restrictions.

## 🎓 Academic Fulfillment (MCA OOP Lab)

This project, **Torch Browser**, was specifically engineered to fulfill and exceed the requirements of the Master of Computer Applications (MCA) Object-Oriented Programming Lab. It actively demonstrates the following core Java and OOP concepts:

* **Inheritance & Abstraction**: The project utilizes an abstract class `CoreBrowserWindow` to define a blueprint, which the primary class `App.java` inherits from to implement mandatory architectural methods.
* **Method Overloading**: URL routing utilizes overloaded `MapsTo(String)` and `MapsTo(URL)` methods, demonstrating compile-time polymorphism.
* **Multithreading**: Features a sophisticated architecture including a **Logic Thread** for networking, a **Background Daemon Thread** for profile syncing, and the main **Event Dispatch Thread (EDT)** for UI rendering.
* **File I/O Streams**:
    * **Byte Streams**: Used for simulating cloud profile syncing via `.dat` files.
    * **Character Streams**: Used for appending and reading human-readable History and Bookmark logs in `.txt` format.
* **Exception Handling**: Robust implementation of `try-catch` blocks to gracefully handle malformed URLs, missing files, and unresolvable domains.
* **Advanced GUI & Event Handling**: Deep implementation of Swing components, complex Layout Managers, `MouseAdapters` for hover effects, and `KeyListeners` for "Enter-to-Search" functionality.

