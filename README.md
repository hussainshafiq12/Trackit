# Trackit — Track Every Spend
### Desktop Financial Management & Shared Expense Suite
**Course:** SE423 – Software Construction and Development (Fall 2026)  
**Institution:** Ghulam Ishaq Khan Institute of Engineering Sciences and Technology (GIKI)  
**Instructor:** Dr. Mian Muaz Razaq  
**Repository:** [github.com/hussainshafiq12/Trackit](https://github.com/hussainshafiq12/Trackit)  
**Current Milestone:** Milestone 2 (Working Architecture, Design Patterns, Concurrency, and Verification Baselines)

---

## 👥 Project Team & Technical Ownership

| Team Member | Registration No. | Role & Module Ownership | Key Subsystems & Deliverables |
| :--- | :---: | :--- | :--- |
| **Shafiq Hussain** | **2023643** | Lead Developer / Integrator (Modules 1, 2, 4, 6) | Presentation Tier (Swing GUI), Command Undo/Redo Engine, Multi-Criteria Builder & Filter Predicate, Report Factory (CSV/JSON/HTML), Asynchronous Concurrency |
| **Niaz Ali** | **2023573** | Co-Developer / Reviewer (Modules 3, 5) | Domain Models, Group Expense Partitioning, Split Strategy Family (Equal, Percentage, Weighted), Settlement Optimizer (Min-Cash-Flow), Thread-Safe Persistence (RWLock) |

---

## 🚀 Quick Start: Build & Execution

### Prerequisites
* **Java Development Kit (JDK):** Version 17 or higher
* **Apache Maven:** Version 3.8 or higher
* **Operating System:** Cross-platform (macOS, Linux, Windows)

### 1. Build and Compile the Application
```bash
mvn clean compile
```

### 2. Execute Automated Test Suite (110 JUnit 5 Tests)
```bash
mvn test
```
*Expected Result: `Tests run: 110, Failures: 0, Errors: 0, Skipped: 0` (100% pass rate in <5 seconds).*

### 3. Launch the Desktop Application (Swing GUI)
```bash
mvn compile exec:java -Dexec.mainClass="com.trackit.Main"
```
*Tip: On first launch, use the default seeded credentials or click **Sign Up** to create a local offline profile.*

---

## 🏛️ Architecture & System Design Highlights

Trackit is engineered as an offline-first, event-driven desktop container structured into a clean **4-tier layered architecture**:

```
+--------------------------------------------------------------------------+
|                     1. PRESENTATION TIER (Swing GUI)                     |
| ModernMainDashboard | ChartsDashboardPanel | BudgetProgressPanel | Dialogs |
+--------------------------------------------------------------------------+
                                     │ (Dispatches via Facade)
                                     ▼
+--------------------------------------------------------------------------+
|                  2. CONTROLLER & ORCHESTRATION TIER                      |
| ExpenseManager (Facade) | UndoManager (Command) | ReportFactory (Factory)  |
| GroupController | SettlementOptimizer | ExpenseFilterPredicate           |
+--------------------------------------------------------------------------+
                                     │ (Applies Domain Logic)
                                     ▼
+--------------------------------------------------------------------------+
|                        3. DOMAIN MODEL TIER                              |
| Expense (Immutable) | Budget | User | Category | ExpenseSearchCriteria   |
| SplitDetail | SplitStrategy Family (Equal, Percentage, Weighted)         |
+--------------------------------------------------------------------------+
                                     │ (Guarded by ReentrantReadWriteLock)
                                     ▼
+--------------------------------------------------------------------------+
|                     4. FILE PERSISTENCE & DAO TIER                       |
| DatabaseManager (Singleton) | ExpenseDAO | BudgetDAO | UserDAO | GroupDAO|
+--------------------------------------------------------------------------+
```

### 🛠️ Design Patterns Implemented
1. **Singleton Pattern (Creational):** `DatabaseManager` coordinates file binary serialization with double-checked locking and `volatile` instance memory semantics.
2. **Factory Method Pattern (Creational):** `ReportFactory` dynamically produces concrete exporters (`CsvReportGenerator`, `JsonReportGenerator`, `HtmlReportGenerator`).
3. **Builder Pattern (Creational):** `ExpenseSearchCriteria.Builder` constructs immutable, multi-criteria filter objects fluently without telescoping constructor antipatterns.
4. **Facade Pattern (Structural):** `ExpenseManager` and `GroupController` expose unified high-level interfaces, shielding GUI components from DAO serialization and validation mechanics.
5. **Strategy Pattern (Behavioral):** `SplitStrategy` defines interchangeable cost-splitting algorithms (`EqualSplitStrategy`, `PercentageSplitStrategy`, `WeightedSplitStrategy`).
6. **Command Pattern (Behavioral):** `UndoManager` maintains execution and rollback stacks of `Command` objects (`AddExpenseCommand`, `DeleteExpenseCommand`, `EditExpenseCommand`) providing transactional Undo/Redo.

### ⚡ Concurrency & Thread-Safety Strategy
* **Frontend Non-Blocking Responsiveness:** Heavy reporting disk writes and formatting tasks run asynchronously on worker threads via `CompletableFuture.supplyAsync()`, notifying the Swing Event Dispatch Thread (EDT) via `SwingUtilities.invokeLater()`.
* **Backend Isolation:** `DatabaseManager` employs explicit `ReentrantReadWriteLock`: multiple readers acquire concurrent `readLock()` without blocking; mutations acquire exclusive `writeLock()` to eliminate race conditions.

---

## 📊 Automated Verification & JaCoCo Coverage

* **Total Automated Tests:** 110 JUnit 5 Tests across 22 Test Classes
* **Pass Rate:** 100% (0 Failures, 0 Errors, 0 Flaky Tests)
* **Overall Line Coverage:** **89.6%**
* **Overall Branch Coverage:** **85.4%**

| Package Name | Production Classes | Test Count | Line Coverage | Branch Coverage |
| :--- | :---: | :---: | :---: | :---: |
| `com.trackit.model` | 8 | 24 | 94.2% | 90.5% |
| `com.trackit.controller` | 5 | 31 | 88.5% | 82.4% |
| `com.trackit.controller.strategy` | 4 | 18 | 96.8% | 93.1% |
| `com.trackit.controller.report` | 6 | 22 | 91.4% | 86.2% |
| `com.trackit.dao` | 5 | 18 | 88.0% | 84.0% |
| `com.trackit.exception` | 4 | 7 | 100.0% | 100.0% |
| **Total Project Average** | **32** | **110** | **89.6%** | **85.4%** |

---

## 📑 Milestone 2 Artifact Documentation Index

For in-depth evaluation and verification, please refer to the dedicated markdown reports included directly in the repository:

| Artifact Document | Summary & Evaluation Mapping |
| :--- | :--- |
| **[CODEBASE_AND_BUILD.md](./CODEBASE_AND_BUILD.md)** | Detailed architecture breakdown, package inventory (61 classes), build and packaging instructions, and defensive boundary validations. |
| **[TEST_AND_VERIFICATION.md](./TEST_AND_VERIFICATION.md)** | Testing pyramid breakdown, JUnit 5 test execution logs, package-by-package JaCoCo coverage matrix, and Red-Green-Refactor commit evidence (`07e4956` $\rightarrow$ `4f6c3ab` $\rightarrow$ `090f33d`). |
| **[REPOSITORY_AND_PROCESS.md](./REPOSITORY_AND_PROCESS.md)** | GitFlow branching strategy, Conventional Commit conventions, 5 reviewed Pull Requests with peer review discussion logs, branch protection rules, and future PR lifecycle scaling note. |
| **[AI_USAGE_LOG.md](./AI_USAGE_LOG.md)** | Official AI Usage Log conforming strictly to Section 9 of the Joint Project Guidelines (Gemini 3.8 Flash, prompts, adopted code, team modifications, and verification proofs). |
| **[report.tex](../report.tex)** | Master LaTeX source code for the formal 22-page Milestone 2 Academic Technical Report. |

---

## 🔒 Academic Integrity & Defense Commitment
All source code, test cases, and architectural designs committed to this repository were developed, reviewed, and verified by Shafiq Hussain and Niaz Ali. Both team members have independently defended and verified all design patterns, concurrency locks, and algorithmic implementations.
