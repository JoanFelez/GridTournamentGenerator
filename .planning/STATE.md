# Project State

## Current Version: 0.0.1
## Current Phase: 7 — Windows Packaging
## Status: IN PROGRESS

## Completed Phases
- **Phase 1:** Project Skeleton & Infrastructure ✅
- **Phase 2:** Domain Model (DDD + TDD) ✅
- **Phase 3:** Bracket Logic & Application Services ✅
- **Phase 4:** UI Grid Rendering ✅
- **Phase 5:** UI Match Management & Tournament History ✅
- **Phase 6:** PDF Export ✅ (pending)

## Phase 7 Progress
- **Windows Packaging** 🔧
  - `package-win.bat` — Creates standalone app-image (.exe launcher)
  - `package-win-installer.bat` — Creates .exe installer (requires WiX Toolset)
  - `package-linux.sh` — Linux app-image (validated working)
  - jpackage approach: Spring Boot fat JAR → jpackage app-image with bundled JRE
  - App-image size: ~184MB (includes JRE + all dependencies)
  - Validated on Linux: app starts, Spring Boot context loads, JavaFX window appears

## Git Workflow
- **Main branch:** main (production)
- **Development branch:** develop
- **Task branches:** feature/<task-name>, created from develop
- **PRs:** Each task branch → PR to develop
- **Commits:** Conventional Commits format
