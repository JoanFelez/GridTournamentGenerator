# Requirements — Grid Padel Tournament Generator

## Functional Requirements

| ID | Requirement | Priority |
|---|---|---|
| REQ-01 | Support up to 32 pairs of players | Must |
| REQ-02 | Support odd number of pairs with BYE slots | Must |
| REQ-03 | User selects which matches get BYE | Must |
| REQ-04 | Double-elimination format: Main Way + Consolation Way | Must |
| REQ-05 | First-round matches rendered in center column | Must |
| REQ-06 | Main Way bracket flows to the right | Must |
| REQ-07 | Consolation Way bracket flows to the left | Must |
| REQ-08 | Each match has: 2 pairs, location, date-time | Must |
| REQ-09 | Entering match result auto-advances winner to next round | Must |
| REQ-10 | Every pair guaranteed 2 matches (losers go to consolation) | Must |
| REQ-11 | After second loss, pair is eliminated | Must |
| REQ-12 | Grid rendered visually on screen | Must |
| REQ-13 | Export grid to PDF | Must |
| REQ-14 | Auto-executable on Windows 11 (.exe) | Must |
| REQ-15 | Location and date-time can be set in advance (before match is played) | Must |
| REQ-16 | All tournament data is editable at any time (pairs, results, locations, dates) | Must |
| REQ-17 | Tournament history: save, reopen, and modify past tournaments | Must |
| REQ-18 | Persist tournaments to local storage (file-based) | Must |
| REQ-19 | Pairs can be assigned a seed number (1, 2, 3...) | Must |
| REQ-20 | Seeded pairs get favorable bracket placement (avoid meeting early) | Must |
| REQ-21 | Top seeds may receive BYEs when pair count is not a power of 2 | Must |
| REQ-22 | Match stores full set-by-set result with padel scoring rules (6-0..6-4, 7-5, 7-6; best of 3 sets) | Must |
| REQ-23 | Input validation errors show informative messages without stopping execution; user can correct and retry | Must |

## Non-Functional Requirements

| ID | Requirement | Priority |
|---|---|---|
| NFR-01 | Clean Architecture (layered: domain → application → infrastructure → UI) | Must |
| NFR-02 | SOLID principles | Must |
| NFR-03 | Domain-Driven Design (DDD) | Must |
| NFR-04 | Test-Driven Development (TDD) | Must |
| NFR-05 | Conventional Commits | Must |
| NFR-06 | Branch per task, PR to develop | Must |
| NFR-07 | Spring Boot 3.x + JavaFX + Maven | Must |
