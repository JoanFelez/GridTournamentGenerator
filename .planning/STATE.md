# Project State

## Current Version: 0.0.1
## Current Phase: 3 — Bracket Logic & Application Services
## Status: IN REVIEW (PRs #7, #8)

## Completed Phases
- **Phase 1:** Project Skeleton & Infrastructure ✅
- **Phase 2:** Domain Model (DDD + TDD) ✅
  - Value Objects: PairId, MatchId, TournamentId, PlayerName, Location, MatchDateTime, MatchResult, BracketType, SetResult
  - Entities: Pair, Match, Round, Bracket
  - Aggregate Root: Tournament (max 32 pairs, dual brackets, timestamps)
  - Repository Port: TournamentRepository
  - PRs: #1–#6 merged

## Phase 3 Progress
- **Domain Services** ✅ (PR #7 — feature/bracket-generation)
  - BracketGenerationService: BPP seeding, BYE placement, auto-resolve
  - MatchAdvancementService: winner advancement, consolation routing, result clearing
- **Application & Infrastructure** ✅ (PR #8 — feature/application-services)
  - TournamentService: all use cases (create, manage pairs, generate, record results, etc.)
  - JsonTournamentRepository: file-based JSON persistence
  - TournamentDtoMapper: full round-trip domain ↔ DTO
  - InfrastructureConfig: Spring bean registration for domain services
- **Tests:** 218 total (188 domain + 15 application + 15 infrastructure)
- **Remaining:** BYE slot user override (REQ-03) — deferred to UI phase

## Git Workflow
- **Main branch:** main (production)
- **Development branch:** develop
- **Task branches:** feature/<task-name>, created from develop
- **PRs:** Each task branch → PR to develop
- **Commits:** Conventional Commits format
