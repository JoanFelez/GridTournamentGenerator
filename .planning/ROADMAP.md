# Roadmap — Grid Padel Tournament Generator

## Stack Decision
- **Option A:** Spring Boot 3.x + JavaFX + Maven
- **Architecture:** Clean Architecture + DDD + SOLID
- **Testing:** TDD (JUnit 5 + Mockito + TestFX)
- **Packaging:** jpackage → Windows .exe
- **PDF:** OpenPDF / PDFBox
- **Version:** 0.0.1

---

## Phase 1: Project Skeleton & Infrastructure
**Goal:** Maven multi-module project, Spring Boot + JavaFX bootstrap, CI-ready structure.

**Success Criteria:**
- [ ] Maven project compiles with `mvn clean compile`
- [ ] Spring Boot context starts
- [ ] JavaFX window launches with a blank stage
- [ ] Clean Architecture layers defined (domain, application, infrastructure, ui)
- [ ] .gitignore, README, develop branch set up
- [ ] Unit test infrastructure works (`mvn test` passes)

**Covers:** NFR-01, NFR-02, NFR-03, NFR-07

---

## Phase 2: Domain Model (DDD + TDD)
**Goal:** Core domain entities, value objects, and domain services.

**Success Criteria:**
- [ ] Pair, Match, Round, Bracket, Tournament entities with proper DDD modeling
- [ ] Value Objects for MatchResult, Location, MatchDateTime
- [ ] Domain rules enforced (max 32 pairs, BYE logic, double-elimination invariants)
- [ ] Location and date-time can exist on a match before it is played (pre-set)
- [ ] All entities support mutation (edit pair names, change results, update locations/dates)
- [ ] Tournament aggregate root with full state (supports save/restore)
- [ ] Repository interface for tournament persistence (port)
- [ ] 90%+ test coverage on domain layer
- [ ] All tests written before implementation (TDD)

**Covers:** REQ-01, REQ-02, REQ-08, REQ-10, REQ-11, REQ-15, REQ-16, NFR-03, NFR-04

---

## Phase 3: Bracket Logic & Application Services
**Goal:** Tournament bracket generation, BYE assignment, auto-advancement, consolation routing, persistence.

**Success Criteria:**
- [ ] Bracket generation algorithm for 1–32 pairs
- [ ] BYE slot selection by user
- [ ] Auto-advance winner when result entered
- [ ] Losers from round 1 routed to consolation bracket
- [ ] Second-loss elimination working
- [ ] Application services orchestrate domain operations
- [ ] Save/load tournament use cases (serialize to JSON/file)
- [ ] Edit tournament use case (modify any field, recompute bracket state)
- [ ] Tournament history listing (list saved tournaments)
- [ ] Full TDD coverage

**Covers:** REQ-02, REQ-03, REQ-04, REQ-09, REQ-10, REQ-11, REQ-16, REQ-17, REQ-18

---

## Phase 4: UI — Grid Rendering (JavaFX)
**Goal:** Visual bracket rendering with center-column first round, main→right, consolation→left.

**Success Criteria:**
- [ ] First-round matches in center column
- [ ] Main bracket flows right with connecting lines
- [ ] Consolation bracket flows left with connecting lines
- [ ] Scrollable/zoomable canvas for large brackets
- [ ] Match boxes show pair names, location, date-time, result

**Covers:** REQ-05, REQ-06, REQ-07, REQ-12

---

## Phase 5: UI — Match Management & Tournament History
**Goal:** User interactions for match details, result entry, editing, and tournament history.

**Success Criteria:**
- [ ] Dialog to create/edit tournament (name, number of pairs)
- [ ] Dialog to enter/edit pair names
- [ ] Dialog to assign BYE positions
- [ ] Dialog to enter/edit match details (location, date-time) — including in advance
- [ ] Dialog to enter/edit match results
- [ ] Result entry triggers auto-advancement in the grid
- [ ] All fields editable after initial entry (pairs, results, locations, dates)
- [ ] Tournament history screen: list saved tournaments, open, delete
- [ ] Save/auto-save current tournament
- [ ] Reopen a saved tournament and resume editing

**Covers:** REQ-03, REQ-08, REQ-09, REQ-15, REQ-16, REQ-17, REQ-18

---

## Phase 6: PDF Export
**Goal:** Export the rendered grid to a downloadable PDF file.

**Success Criteria:**
- [ ] PDF contains full bracket (main + consolation)
- [ ] PDF is correctly scaled/paginated for A3/A4
- [ ] Match details visible in PDF
- [ ] File save dialog for export location

**Covers:** REQ-13

---

## Phase 7: Windows Packaging
**Goal:** Produce a self-contained Windows 11 .exe installer.

**Success Criteria:**
- [ ] jpackage produces working .exe / .msi
- [ ] Application starts without JRE pre-installed
- [ ] Version 0.0.1 embedded

**Covers:** REQ-14

---

## Phase Dependencies
```
Phase 1 → Phase 2 → Phase 3 → Phase 4 → Phase 5 → Phase 6 → Phase 7
                                  ↓
                            Phase 5 (parallel possible with Phase 4 for non-overlapping UI)
```
