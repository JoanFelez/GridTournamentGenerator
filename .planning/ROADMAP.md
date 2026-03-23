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
- [ ] 90%+ test coverage on domain layer
- [ ] All tests written before implementation (TDD)

**Covers:** REQ-01, REQ-02, REQ-08, REQ-10, REQ-11, NFR-03, NFR-04

---

## Phase 3: Bracket Logic & Application Services
**Goal:** Tournament bracket generation, BYE assignment, auto-advancement, consolation routing.

**Success Criteria:**
- [ ] Bracket generation algorithm for 1–32 pairs
- [ ] BYE slot selection by user
- [ ] Auto-advance winner when result entered
- [ ] Losers from round 1 routed to consolation bracket
- [ ] Second-loss elimination working
- [ ] Application services orchestrate domain operations
- [ ] Full TDD coverage

**Covers:** REQ-02, REQ-03, REQ-04, REQ-09, REQ-10, REQ-11

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

## Phase 5: UI — Match Management
**Goal:** User interactions for match details and result entry.

**Success Criteria:**
- [ ] Dialog to create/edit tournament (name, number of pairs)
- [ ] Dialog to enter/edit pair names
- [ ] Dialog to assign BYE positions
- [ ] Dialog to enter match details (location, date-time)
- [ ] Dialog to enter match results
- [ ] Result entry triggers auto-advancement in the grid

**Covers:** REQ-03, REQ-08, REQ-09

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
