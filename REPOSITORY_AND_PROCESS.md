# Repository and Process Artifact Report
**Project:** Trackit — Track Every Spend  
**Course:** SE423 – Software Construction and Development (Fall 2026)  
**Deliverable Category:** Repository & Process Artifacts (Milestone 2)  
**Authors:** Shafiq Hussain (Reg: 2023643) & Niaz Ali (Reg: 2023573)  
**Instructor:** Dr. Mian Muaz Razaq, GIKI  
**Repository:** [github.com/hussainshafiq12/Trackit](https://github.com/hussainshafiq12/Trackit)  

---

## 1. GitFlow Branching Strategy

The Trackit engineering team enforced a strict **GitFlow branching model** throughout Milestone 2 to maintain code stability, prevent merge anomalies, and isolate experimental features:

```
[main] ─────────────────────────────────────────● (Release Tag: v0.2.0-m2)
                                                ▲
                                                │ (Production Release)
[develop] ───●──────────●──────────●────────────● (Integrated Baseline)
             ▲          ▲          ▲            ▲
             │          │          │            │
[features] ──┴─[PR #1]──┴─[PR #2]──┴─[PR #3, #4]┴─[PR #5]
```

* **`main` Branch:** Protected production branch containing stable, tagged release milestones (`v0.1.0-m1`, `v0.2.0-m2`). Direct commits to `main` are strictly forbidden.
* **`develop` Branch:** Primary integration branch where verified features are aggregated following successful peer review and automated build validation.
* **`feature/*` Branches:** Short-lived branches dedicated to individual modules or capabilities, authored by specific owners (`feature/shafiq-m2-trackit`, `feature/niaz-m2-trackit`, `feature/shafiq-m2-analytics-ui`, `feature/niaz-backend-optimization`, `feature/shafiq-m2-advanced-filtering-export`).

---

## 2. Commit Conventions & Discipline

Every commit follows the **Conventional Commits** specification (`<type>(<scope>): <subject>`), ensuring an attributable and semantically traceable history:

* `feat(<module>):` Introduction of a new functional capability or design pattern.
* `fix(<module>):` Correction of a defect or edge-case failure.
* `test(<module>):` Introduction or modification of automated unit/integration tests.
* `refactor(<module>):` Code structural reorganization without altering observable behavior.
* `docs(<module>):` Documentation updates, markdown reports, or architectural diagrams.

---

## 3. Milestone 2 Pull Request Lifecycle Summary

All cross-module integrations during Milestone 2 were executed via formal, peer-reviewed Pull Requests:

| PR # | Title / Scope | Author | Reviewer | Commits | Status | Merge Target |
| :---: | :--- | :---: | :---: | :---: | :---: | :---: |
| **#1** | Core Architecture, Command Pattern, Report Factory | Shafiq | Niaz Ali | 15 | **Merged** | `develop` |
| **#2** | Backend Logic, Split Strategies, Settlement Optimizer | Niaz Ali | Shafiq | 12 | **Merged** | `develop` |
| **#3** | Advanced Financial Analytics, Budget Alerts | Shafiq | Niaz Ali | 9 | **Merged** | `develop` |
| **#4** | Min-Cash-Flow Optimization, Path Traversal Security | Niaz Ali | Shafiq | 8 | **Merged** | `develop` |
| **#5** | Multi-Criteria Search, HTML Export, Filter Dialog | Shafiq | Niaz Ali | 12 | **Merged** | `develop` |

---

## 4. Peer Review Inspection Logs & Code Fixes

Peer reviews were conducted with substantive technical rigor rather than superficial approvals. Two notable examples evidence genuine defect discovery and remediation:

### Example A: Path Traversal Security Vulnerability Catch (PR #4)
* **Reviewer Finding (Shafiq Hussain):**  
  > *"In `DatabaseManager.exportToFile()`, the filename provided by the user is directly appended to the output folder path. An attacker or malicious path string (`../../etc/`) could write outside the application sandbox."*
* **Remediation Implemented (Niaz Ali):**  
  Refactored path resolution using canonical file checks:
  ```java
  File target = new File(baseDir, sanitizedFileName).getCanonicalFile();
  if (!target.getPath().startsWith(baseDir.getCanonicalPath())) {
      throw new SecurityException("Directory traversal attempt detected.");
  }
  ```
* **Status:** Verified and re-approved prior to merging.

### Example B: Start Date Inclusivity & HTML Entity Sanitization (PR #5)
* **Reviewer Finding (Niaz Ali):**  
  > *"In `ExpenseFilterPredicate`, expenses occurring on the exact start date are rejected because of a strict `isAfter()` comparison. Furthermore, titles containing `<` or `>` in `HtmlReportGenerator` cause layout rendering defects."*
* **Remediation Implemented (Shafiq Hussain):**  
  Updated comparison to include boundary dates (`!expenseDate.isBefore(startDate)`) and introduced character entity replacement (`&amp;`, `&lt;`, `&gt;`). Added 3 new regression tests in `ExpenseFilterPredicateTest.java` and `HtmlReportTest.java`.
* **Status:** Re-tested with 100% pass rate and merged into `develop`.

---

## 5. Branch Protection & Quality Gates

To protect the integrity of the `develop` and `main` branches, the GitHub repository enforces:
1. **Required Pull Request Reviews:** Direct pushes to `develop` and `main` are blocked. Every change requires an approving review from the peer collaborator.
2. **Passing Automated CI Checks:** Automated test suites must execute with 0 failures before merge buttons are enabled.
3. **Linear History / Squash or Merge Commits:** Preserves a clean Git timeline documenting milestone boundaries.

---

## 6. Future Pull Request Lifecycle Scaling Note

The five pull requests documented in Table 1 establish the foundational architectural baseline for Milestone 2. As the project advances into Milestone 3, pull request volume will scale to incorporate:
* Enhanced optical receipt scanning and OCR integration.
* Automated budget forecasting algorithms.
* Final IEEE 730 SQAP compliance verification and security audits.

All subsequent pull requests will adhere to the identical peer-review, branch protection, and commit attribution standards.
