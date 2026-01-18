# AgroPlanner: Intelligent Cultivation Layout System

**AgroPlanner** is a Java-based application designed to optimize agricultural land usage through evolutionary computation. The system generates collision-free cultivation plans by employing **Genetic Algorithms (GA)** to solve spatial packing problems within geometric constraints.

Designed with a strict **Model-View-Controller (MVC)** architecture, the application offers both a high-performance **Command Line Interface (CLI)** and a responsive **JavaFX Graphical User Interface (GUI)**.

---

## 🚀 Key Features

* **Geometric Domain Modeling:** Support for various terrain shapes (Rectangular, Circular) via Factory Pattern instantiation.
* **Genetic Optimization Engine:** An evolutionary solver (`TerrainOptimizer`) that maximizes land usage while strictly respecting biological constraints and physical collisions.
* **Dynamic Inventory Composer:** Allows users to define plant varieties and quantities (`PlantInventory`), creating complex input constraints for the algorithm.
* **Dual Interface Implementation:**
    * **CLI:** Streamlined execution for batch processing and headless environments.
    * **JavaFX GUI:** Interactive dashboard with real-time visualization of the field and dynamic parameter adaptation.
* **Persistence:** Data access layer (DAO) for managing plant profiles and varieties.

---

## 🏗 Software Architecture

The project adheres to rigorous software engineering principles, featuring a decoupled design to ensure maintainability and scalability.

### Design Patterns
* **MVC (Model-View-Controller):** Strict separation between business logic, data, and presentation layers.
* **Factory Method:** Used in `TerrainFactory` to abstract the instantiation of specific domain topologies (`CircleDomain`, `RectangleDomain`).
* **Strategy Pattern:** (Implicit in Optimizer) To handle different fitness calculation strategies and collision detection algorithms.
* **DAO (Data Access Object):** For abstracting the persistence layer and retrieving `PlantProfile` data.
* **Composite/Aggregation:** Structured modeling of `PlantInventory` and `InventoryEntry`.

### Architectural Note
The system is orchestrated by a central `SystemOrchestrator` which delegates execution to either `CLIOrchestrator` or the JavaFX runtime based on the startup configuration.
*Note: While the CLI implementation strictly follows the `CultivationPlanInterface` for Dependency Inversion, the JavaFX View layer maintains a direct dependency on the Controller due to FXML reflection constraints.*

---

## 🛠 Tech Stack

* **Language:** Java (JDK 23+)
* **GUI Framework:** JavaFX (Modular implementation with FXML)
* **Build Tool:** Maven
* **Versioning:** Git

---

## 📦 Installation & execution

### Prerequisites
Ensure you have **Java JDK 23** (or higher) and **Maven** installed on your machine.

### Build the Project
Clone the repository and build the application using Maven:

```bash
git clone [https://github.com/yourusername/agroplanner.git](https://github.com/yourusername/agroplanner.git)
cd agroplanner
mvn clean install
