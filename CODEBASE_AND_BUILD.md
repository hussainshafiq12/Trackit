# Codebase and Build Artifact Report
**Project:** Trackit — Track Every Spend  
**Course:** SE423 – Software Construction and Development (Fall 2026)  
**Deliverable Category:** Codebase & Build Artifacts (Milestone 2)  
**Authors:** Shafiq Hussain (Reg: 2023643) & Niaz Ali (Reg: 2023573)  
**Instructor:** Dr. Mian Muaz Razaq, GIKI  

---

## 1. System Topology & 4-Tier Desktop Container

Trackit is an offline-first, event-driven financial desktop application built on Java 17 and Swing. The codebase enforces strict separation of concerns through an acyclic, 4-tier layered architecture:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                     1. PRESENTATION TIER (Swing GUI)                    │
│ ModernMainDashboard, ChartsDashboardPanel, BudgetProgressPanel,         │
│ ModernFilterDialog, SplitExpenseDialog, SettlementDialog, AddExpenseForm│
└────────────────────────────────────┬────────────────────────────────────┘
                                     │ (Dispatches via Facade)
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                  2. CONTROLLER & ORCHESTRATION TIER                     │
│ ExpenseManager (Facade), UndoManager (Command Stacks),                  │
│ ReportFactory (Factory Method), GroupController, SettlementOptimizer,   │
│ ExpenseFilterPredicate, AuthenticationController, BudgetManager         │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │ (Applies Domain Invariants)
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                        3. DOMAIN MODEL TIER                             │
│ Expense (Immutable Entity), Budget, User, ExpenseCategory,              │
│ ExpenseSearchCriteria (Builder), SplitDetail, SharedExpense,            │
│ SplitStrategy Family (EqualSplitStrategy, PercentageSplitStrategy,      │
│ WeightedSplitStrategy), Receipt                                         │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │ (Guarded by ReentrantReadWriteLock)
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                     4. FILE PERSISTENCE & DAO TIER                      │
│ DatabaseManager (Singleton), ExpenseDAO, BudgetDAO, UserDAO, GroupDAO,  │
│ RecurringExpenseDAO (Protected Binary Object Stream Serialization)      │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Package Organization & 61 Source Classes Inventory

The production codebase comprises **61 source files** organized into cohesive packages:

### `com.trackit` (Root)
* `Main.java` — Desktop application entry point; initializes look-and-feel and launches authentication container on Swing EDT.

### `com.trackit.model` (Domain Kernel)
* `Expense.java` — Core transaction entity (UUID, amount, category, date, description, note).
* `Budget.java` — Category spending threshold model with limit and month tracking.
* `User.java` — User authentication profile with salted SHA-256 password hash.
* `ExpenseCategory.java` — Extensible domain enumeration for financial categories.
* `ExpenseSearchCriteria.java` — Fluent query object with static nested `Builder` for multi-criteria searches.
* `Group.java` — Communal living / project expense group entity.
* `SharedExpense.java` — Multi-participant expense container referencing split strategies.
* `SplitDetail.java` — Individual participant debt/credit share calculation.
* `RecurringExpense.java` — Scheduled subscription / recurring payment entity.
* `Receipt.java` — Optical file receipt attachment model.

### `com.trackit.controller` (Business Logic & Orchestration)
* `ExpenseManager.java` — High-level Application Facade coordinating personal transactions.
* `GroupController.java` — Application Facade managing communal expenses, participant shares, and settlements.
* `SettlementOptimizer.java` — Greedy Min-Cash-Flow algorithm minimizing total debt transfer transactions.
* `ExpenseFilterPredicate.java` — Compound Java 8 `Predicate` builder evaluating multi-criteria queries.
* `AuthenticationController.java` — Manages offline login, password salt verification, and session state.
* `BudgetManager.java` — Computes category spending variances and generates 80%/100% threshold alerts.
* `RecurringExpenseManager.java` — Evaluates recurring expense triggers against current timestamp.
* `Command.java` — Behavioral Command interface declaring `execute()` and `undo()`.
* `AddExpenseCommand.java` — Transactional command adding an expense with full rollback capability.
* `DeleteExpenseCommand.java` — Transactional command deleting an expense with reversible restore.
* `EditExpenseCommand.java` — Transactional command modifying an expense, capturing prior/updated snapshots.
* `UndoManager.java` — Transaction manager maintaining dual stacks (`undoStack`, `redoStack`).

### `com.trackit.controller.strategy` (Strategy Pattern Family)
* `SplitStrategy.java` — Common behavioral interface for cost-splitting algorithms.
* `EqualSplitStrategy.java` — Divides costs equally with exact round-off deficit preservation.
* `PercentageSplitStrategy.java` — Apportions costs according to user-defined percentage shares.
* `WeightedSplitStrategy.java` — Allocates costs based on ratio weights (e.g., room size, income ratio).

### `com.trackit.controller.report` (Factory Method Reporting Engine)
* `ReportGenerator.java` — Abstract interface declaring synchronous `generate()` and non-blocking `generateAsync()`.
* `ReportFactory.java` — Factory Method creating concrete report exporters.
* `CsvReportGenerator.java` — Exports structured comma-separated values format.
* `JsonReportGenerator.java` — Serializes expenses into formatted JSON array documents.
* `HtmlReportGenerator.java` — Generates styled, modern HTML financial dashboard reports with entity escaping.
* `PersonalReportGenerator.java` — Formats text-based personal spending metrics.
* `GroupReportGenerator.java` — Formats text-based group settlement debt distribution statements.

### `com.trackit.dao` (Thread-Safe Persistence)
* `DatabaseManager.java` — Thread-safe Singleton managing disk serialization and `ReentrantReadWriteLock`.
* `ExpenseDAO.java` — Data Access Object handling persistence for personal expenses.
* `BudgetDAO.java` — Data Access Object managing category budget limits.
* `UserDAO.java` — Data Access Object managing user authentication profiles.
* `GroupDAO.java` — Data Access Object persisting communal expense groups.
* `RecurringExpenseDAO.java` — Data Access Object managing scheduled transactions.

### `com.trackit.view` (Presentation Tier - Swing GUI)
* `ModernMainDashboard.java` — Primary dashboard container featuring tab navigation and responsive sidebar.
* `ChartsDashboardPanel.java` — Custom vector rendering of spending breakdown bar charts and category distribution.
* `BudgetProgressPanel.java` — Real-time visual progress bars with dynamic color warnings (Normal, 80% Warning, 100% Breach).
* `PersonalPanel.java` — Tabular view of personal transactions with quick-action buttons.
* `GroupPanel.java` — Group expense management grid with real-time settlement debt display.
* `ModernFilterDialog.java` — Interactive dialog for multi-criteria search (keywords, date range, categories, amount bounds).
* `AddExpenseForm.java` — Transaction creation dialog with live input validation.
* `SplitExpenseDialog.java` — Shared expense configuration dialog allowing dynamic strategy switching.
* `SettlementDialog.java` — Displays optimal debt minimization payment paths.
* `SplitDetailsDialog.java` — Modal dialog breaking down participant liability distributions.
* `ReceiptViewerDialog.java` — Modal preview dialog for attached expense receipts.
* `BudgetSettingsDialog.java` — Category monthly budget threshold configuration dialog.
* `LoginView.java` — User authentication dialog.
* `SignupView.java` — Offline user registration dialog with password confirmation.
* `MainDashboard.java` — Legacy container maintained for backward compatibility.
* `UITheme.java` — Centralized color palette, typographic hierarchy, and border styling.

### `com.trackit.exception` (Custom Structured Exception Tree)
* `TrackitException.java` — Base unchecked runtime exception for all system anomalies.
* `ValidationException.java` — Thrown when user inputs violate domain boundaries or constraints.
* `InvalidSplitException.java` — Thrown when group expense calculations fail mathematical invariants.
* `DataPersistenceException.java` — Thrown when physical storage stream I/O fails.

---

## 3. Technical Ownership Matrix

| Mod # | Module Scope | Primary Owner | Reviewer | Key Packages / Classes |
| :---: | :--- | :---: | :---: | :--- |
| **1** | Core Domain & Criteria Filtering | Shafiq Hussain | Niaz Ali | `com.trackit.model`, `ExpenseSearchCriteria.Builder` |
| **2** | Transaction Engine & Undo Stack | Shafiq Hussain | Niaz Ali | `com.trackit.controller.command`, `UndoManager` |
| **3** | Group Expense & Settlement Strategy | Niaz Ali | Shafiq Hussain | `com.trackit.controller.strategy`, `SettlementOptimizer` |
| **4** | Export Engine & Factory Method | Shafiq Hussain | Niaz Ali | `com.trackit.controller.report`, `ReportFactory` |
| **5** | Thread-Safe Persistence & Locking | Niaz Ali | Shafiq Hussain | `com.trackit.dao`, `DatabaseManager` |
| **6** | Presentation Tier & Asynchronous UI | Shafiq Hussain | Niaz Ali | `com.trackit.view`, `ModernMainDashboard` |

---

## 4. Build, Compilation, & Execution Instructions

### Prerequisites
* Java JDK 17+ (Tested on OpenJDK 17 and Oracle JDK 17)
* Apache Maven 3.8.0+

### Compilation & Build Verification
```bash
# Clean previous build artifacts and compile all 61 production files
mvn clean compile
```

### Packaging into Executable JAR
```bash
mvn clean package -DskipTests=false
```
*Build output: Generates `target/trackit-1.0.0.jar` and compiled `.class` files in `target/classes/`.*

### Application Launch
```bash
mvn compile exec:java -Dexec.mainClass="com.trackit.Main"
```

---

## 5. Defensive Programming & Security Defenses

1. **Boundary Input Validation:**
   - Numerical spinners enforce strict positive bounds: amounts must be $> 0.0$, finite, not NaN, and $\le \$1,000,000.0$.
   - Transaction descriptions reject blank or whitespace-only strings.
2. **Path Traversal Protection (`getCanonicalFile()`):**
   - File exports sanitize user-specified target filenames. The canonical destination path is asserted to reside within the intended output directory, preventing directory traversal attacks (`../../etc/`).
3. **HTML Entity Sanitization:**
   - `HtmlReportGenerator` passes all string fields through an entity sanitizer (`&amp;`, `&lt;`, `&gt;`, `&quot;`) preventing cross-site scripting or layout hijacking in exported HTML reports.
4. **Structured Logging:**
   - Unexpected anomalies are systematically logged using `java.util.logging.Logger` before user-friendly modal alerts are surfaced to the UI. Zero unhandled silent crashes.
