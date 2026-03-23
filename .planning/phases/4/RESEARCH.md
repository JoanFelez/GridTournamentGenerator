# Phase 4 Research: UI Grid Rendering (JavaFX)

## Layout Architecture

### Bracket Rendering Strategy
The bracket uses a **center-out** layout:
- **Center column**: Round 1 matches (all first-round matches)
- **Right side**: Main bracket (winners advance right through rounds)
- **Left side**: Consolation bracket (first-round losers flow left)

### JavaFX Approach
- Use **Pane** (absolute positioning) for bracket layout — gives full control over match box placement and connector lines
- Wrap in **ScrollPane** for scrolling, add **Scale** transform for zoom
- Match boxes are custom **VBox** components showing pair names, location, date-time, result
- Connector lines drawn with **Line** or **Path** elements between rounds

### Component Hierarchy
```
MainView (BorderPane)
└── ScrollPane (center)
    └── Group (scalable container)
        └── BracketPane (Pane - absolute positioning)
            ├── MatchBox[] (VBox per match)
            └── Line[] (connectors between rounds)
```

### Match Box Design
Each match rendered as a styled VBox:
```
┌─────────────────────────┐
│ Pair 1 Name        [S1] │
│─────────────────────────│
│ Pair 2 Name        [S2] │
│─────────────────────────│
│ 📍 Location  📅 DateTime│
└─────────────────────────┘
```

### Layout Calculation
- Match box width: fixed (e.g., 220px)
- Match box height: fixed (e.g., 80px)
- Column gap: ~100px (for connector lines)
- Vertical spacing: matches spread vertically, doubling gap per round
- Round N has `totalMatches / 2^N` matches
- Vertical position: centered relative to the two feeder matches

### Connector Lines
- Horizontal line from match box to midpoint
- Vertical line connecting two feeder matches
- Horizontal line from midpoint to next round match box

### Zoom/Scroll
- ScrollPane wraps entire bracket
- Zoom via Scale transform on inner Group, controlled by scroll wheel + Ctrl

## Key Decisions
1. **Pane vs Canvas**: Pane with Nodes — easier hit detection, styling, interaction for Phase 5
2. **Custom Component**: MatchBoxView as reusable VBox
3. **Separation**: BracketRenderer computes layout, BracketPane renders it
4. **Spring Integration**: Controllers are Spring beans via @Component, injected by Spring context
