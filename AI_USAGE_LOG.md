# Official AI Usage Log
**Project:** Trackit — Track Every Spend  
**Course:** SE423 – Software Construction and Development (Fall 2026)  
**Standard:** Joint Semester Project Guidelines & Rubric (Section 9: Academic Integrity & AI Use)  
**Authors:** Shafiq Hussain (Reg: 2023643) & Niaz Ali (Reg: 2023573)  
**Instructor:** Dr. Mian Muaz Razaq, GIKI  

---

## 1. Compliance Statement & Academic Integrity

In accordance with **Section 9 of the Joint Project Guidelines**, the use of AI-assisted engineering tools is disclosed transparently. Assessment is based on the team's complete understanding, verification, and ability to defend all submitted architectural decisions, implementations, test suites, and documentation.

Both team members independently verify that no AI-generated code or text was incorporated without critical manual review, rigorous test validation, and architectural adaptation to Trackit's specific domain requirements.

---

## 2. Milestone 2 Continuous AI Usage Log

| Tool Used | Purpose | Relevant Prompt Summary | AI-Generated Material Adopted | Modifications Made by Team | Verification Performed by Team |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Gemini 3.8 Flash** | Coding assistance (pattern scaffolding) | *"Draft structural boilerplate for Command undo/redo stacks and Report Factory exporters in Java 17."* | Initial interface signatures for `Command`, `UndoManager` deque operations, and `ReportFactory`. | Refactored to utilize custom `TrackitException`; bound stack operations to Swing UI action listeners; added rollback isolation and memory bounds. | Authored 12 JUnit 5 tests in `UndoManagerTest.java`; verified zero stack corruption across 100 random mutation cycles. |
| **Gemini 3.8 Flash** | Coding assistance (algorithm logic) | *"Suggest greedy settlement debt minimization logic and Java 8 Predicate chaining for expense searches."* | Core greedy cash-flow pairing logic and boolean expression chaining in `ExpenseFilterPredicate`. | Fixed start-date inclusivity boundary bug caught in peer review; added defensive copy wrappers and decimal tolerance checks. | Validated against 18 unit tests in `SplitStrategyTest.java` and `SettlementOptimizerTest.java`; achieved 96.8% line coverage. |
| **Gemini 3.8 Flash** | Documentation assistance (report drafting) | *"Format SE423 Milestone 2 academic report in LaTeX conforming to GIKI style and rubric requirements."* | Structural LaTeX document skeleton, package configuration, and table layouts matching Milestone 2 sections. | Inserted verified Git commit SHAs, team contribution roles, JaCoCo coverage metrics, and tailored custom column types to eliminate text clipping. | Compiled on Overleaf with zero errors, zero warnings; cross-checked against Milestone 2 rubric requirements. |

---

## 3. Team Defense Commitment

Shafiq Hussain and Niaz Ali are fully prepared to demonstrate and defend every aspect of Trackit during the milestone evaluation:
* **Shafiq Hussain** claims primary responsibility for and can defend Modules 1, 2, 4, and 6 (Swing Presentation Tier, Command Pattern Undo/Redo Engine, Criteria Query Builder/Predicate, and Report Factory Architecture).
* **Niaz Ali** claims primary responsibility for and can defend Modules 3 and 5 (Split Strategy Family, Greedy Min-Cash-Flow Settlement Optimizer, Thread-Safe Persistence with `ReentrantReadWriteLock`, and DAO Layer).
