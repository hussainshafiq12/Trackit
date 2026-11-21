# Trackit — Track Every Spend

A comprehensive desktop personal finance and shared expense management system developed for SE423 (Software Construction and Development) and SE431 (Software Quality Assurance).

## Team Members
* **Shafiq Hussain** (Reg: 2023643) - Presentation Layer, Domain Entities, Command Pattern (Undo/Redo), Reporting Factory, Concurrency
* **Niaz Ali** (Reg: 2023573) - Controller Business Logic, Persistence (DAO), Strategy Pattern (Splitting), Concurrency (RWLock)

## Architecture Overview
* **Layered 3-Tier MVC + DAO Architecture**
* **Design Patterns:** Singleton (DatabaseManager), Strategy (SplitStrategy), Command (UndoManager), Factory Method (ReportFactory: CSV, JSON, HTML), Facade (ExpenseManager, GroupController), Builder (ExpenseSearchCriteria).
* **Multi-Format Reporting:** Dynamic export to TXT, CSV, JSON, and styled browser-ready HTML via `ReportFactory`.
* **Interactive Query & Filtering:** In-memory multi-criteria transaction search across date ranges, categories, amount bounds, and keywords via `ExpenseFilterPredicate` and `ModernFilterDialog`.
* **Concurrency:** Asynchronous non-blocking report generation via `CompletableFuture` & thread-safe disk persistence via `ReentrantReadWriteLock`.
* **Testing:** Test-Driven Development (TDD) using JUnit 5 and JaCoCo coverage tracking.

## Running the Application
```bash
mvn clean compile
mvn test
mvn compile exec:java -Dexec.mainClass="com.trackit.Main"
```
