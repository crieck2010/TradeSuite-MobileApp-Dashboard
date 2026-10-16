# Natural Language 0DTE Strategy Studio & Autonomous openD Execution Desk

Comprehensive architectural blueprint for dictating, parsing, validating, and autonomously executing 0DTE options strategies via moomoo openD and streaming market telemetry, featuring interactive pairwise cross-strategy correlation heatmaps with shrinkage.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural parameters were confirmed through the interactive clarification interview and will directly govern the execution pipeline:

- **Autonomous Execution Gate**: Confirmed **Fully autonomous execution directly to openD upon signal trigger**. When an active strategy's entry or exit condition is satisfied by incoming moomoo DataStream ticks, the order payload is automatically dispatched to the openD gateway socket (`127.0.0.1:11111`) without requiring modal tap-to-confirm, protected by strict pre-flight parameter checklists and the existing global Emergency Kill-Switch.
- **Priority 0DTE Options Structures**: Confirmed complete coverage across all 4 core options families:
  1. *0DTE Delta-targeted Credit Spreads & Iron Condors* (wing-width rules, credit floor, delta targeting).
  2. *0DTE Momentum Debit Verticals* (VWAP breakout indicators, intraday trend filters, debit caps).
  3. *Delta-Neutral Credit Strangles* (expected move decay thresholds from ThetaData).
  4. *Volatility Calendar & Diagonal Spreads* (event-driven skew, earnings/FOMC term structure shifts).
- **Correlation & Risk Engine**: Confirmed **Interactive Pairwise Pearson Correlation Heatmap with Ledoit-Wolf Shrinkage**. Displays an $N \times N$ matrix correlating new candidate strategies with running quantitative engines (`donchian_breakout`, `swing_trend`, `pairs_cointegration`, `options_em_decay`, `factor_ff5_long`, and `sentiment_event_study`) to protect against factor crowding.

---

## 1. Overview & Core Concept

### What It Delivers
The Natural Language Strategy Studio enables quantitative options traders to articulate complex 0DTE (zero days to expiration) credit and debit strategies in plain English or voice dictation. The system:
1. **Transcribes & Parses**: Converts natural language into formal, typed algorithmic structures (`StrictStrategyParams`) with mathematical delta targets, wing widths, credit/debit floors, time windows, and profit/loss bounds.
2. **Conducts Pre-Flight Correlation Analysis**: Calculates pairwise empirical return correlation against all running strategies in the `trade-suite` monorepo using Ledoit-Wolf covariance shrinkage (`trade-eda`), highlighting non-correlated alpha and diversification scores.
3. **Automates via moomoo openD**: Establishes continuous telemetry with the moomoo DataStream. When real-time price, Greek, or VWAP conditions align with strict parameters, orders are autonomously dispatched to the local openD gateway (`127.0.0.1:11111`).
4. **Enforces Strict Parameter Adherence**: Continuously inspects real-time slippage, delta drift, and hard time cutoffs (e.g., 3:45 PM EST liquidation guard), with real-time audit logs and instant kill-switch veto clearance.

### Target Audience & Persona
Algorithmic options traders and quantitative researchers using the `trade-suite` framework who require mobile freedom to deploy and oversee intraday 0DTE strategies with institutional discipline and zero-click execution responsiveness.

---

## 2. User Experience & Visual Design

### Key User Flows

```
[ Natural Language Input / Voice Dictation ]
                   │
                   ▼
[ Parameter Extraction & Strict Checklist Validation ]
                   │
                   ▼
[ Pairwise Correlation Heatmap & Diversification Scoring ]
                   │
                   ▼
[ Live moomoo DataStream Monitoring (VWAP, Delta, EM Decay) ]
                   │
                   ▼
[ Autonomous openD Dispatch (127.0.0.1:11111) ]
                   │
                   ▼
[ Real-Time PnL Tracking & Hard 3:45 PM Auto-Liquidation Guard ]
```

1. **Strategy Dictation & Preset Selection**:
   - The user selects a 0DTE preset chip or inputs custom strategy parameters via voice/text (e.g., *"0DTE SPX Iron Condor: Sell 12-delta put and call wings with 50-pt wide wings at 10:00 AM EST, profit target 50%, stop loss 2x credit, hard close at 3:45 PM EST"*).
   - Instant visual token parsing highlights detected underlying, options structure, delta, wing width, profit target, and time stops.

2. **Strict Parameter Adherence Checklist**:
   - A dedicated card displays interactive verification badges: Delta verification ($\pm 0.02$ tolerance), credit floor threshold, wing-width spread validation, hard time stop liquidation guard, and pre-flight risk clearance.
   - All parameters must pass green checkmarks before autonomous routing activates.

3. **Interactive Pearson Correlation Heatmap**:
   - An interactive $N \times N$ matrix rendered on Canvas displays correlation coefficients between the candidate strategy and existing active engines (`donchian_breakout`, `swing_trend`, `pairs_cointegration`, `options_em_decay`, `factor_ff5_long`, `sentiment_event_study`).
   - Color grading: Deep Emerald (strong negative correlation / high diversification), Neutral Slate (zero correlation), Amber/Rose (moderate/high correlation).
   - Tapping any cell reveals detailed pairwise scatter statistics and diversification benefit analysis.

4. **Autonomous openD Execution & DataStream Telemetry**:
   - One-tap toggle: **Arm Autonomous openD Execution**.
   - Live stream ticker display shows incoming tick timestamps, delta tracking, and trigger thresholds.
   - Automatic status transition: `STANDBY` $\rightarrow$ `MONITORING_STREAM` $\rightarrow$ `ENTRY_TRIGGERED` $\rightarrow$ `OPEN_POSITION` $\rightarrow$ `EXIT_TRIGGERED` $\rightarrow$ `CLOSED_COMPLETE`.
   - Real-time PnL chip with percentage gain and one-tap emergency manual override.

### Visual Identity & Theme
- **Theme**: Premium High-Frequency Quant Terminal (Dark Mode default).
- **Color Tokens**:
  - Background: Obsidian Black (`#080D1A`) with subtle radial gridlines.
  - Surface: Deep Slate (`#0F172A`) and Elevated Card (`#1E293B`).
  - Accents: Neon Cyan (`#00F2FE`) for telemetry and streams; Emerald Green (`#00E676`) for profit/diversification; Electric Amber (`#FFD600`) for monitoring triggers; Crimson Red (`#FF5252`) for vetoes and risk halts.
- **Typography**: Monospace and tabular numbers (`FontFamily.Monospace`) for financial figures, deltas, and openD socket payloads; bold sans-serif display headers for screen orientation.

---

## 3. Key Product Decisions & Trade-Offs

### Decision 1: Autonomous openD Execution Pipeline
- **Chosen Approach**: Direct event-driven dispatch to the openD API context upon parameter condition satisfaction, backed by automated pre-flight checks and the global emergency kill-switch.
- **Why**: 0DTE options decay and momentum spikes require sub-second execution windows where manual prompt-confirmation would introduce unacceptable slippage.
- **Alternatives Considered**: Two-step manual confirmation was evaluated but discarded per user direction to ensure true algorithmic speed.

### Decision 2: Pairwise Correlation Heatmap with Ledoit-Wolf Shrinkage
- **Chosen Approach**: Full $N \times N$ correlation matrix computed using regularized covariance shrinkage (matching the `trade-eda` engine in `trade-suite`).
- **Why**: Standard empirical sample covariance on short intraday time horizons is notoriously noisy and prone to inversion error. Shrinkage towards an identity or constant-correlation target provides robust diversification estimates.
- **Alternatives Considered**: Simple rolling 15-minute return sparklines, which show directionality but fail to communicate cross-portfolio risk concentration.

### Decision 3: Parameter Formalization via Hybrid Parsing
- **Chosen Approach**: Deterministic regex and token extractors paired with structured rule-based parameter synthesis, plus optional Gemini AI copilot for natural language nuance.
- **Why**: Trading algorithms require 100% deterministic parameter bounds (exact delta, exact dollar stops, exact cutoff times) where hallucinated numbers cannot be tolerated.

---

## 4. Technical Architecture & Data Strategy

### Component & Data Flow Architecture

```
┌────────────────────────────────────────────────────────────────────────┐
│                        TradeSuite Compose UI Layer                     │
├────────────────────────────────────────────────────────────────────────┤
│  ┌───────────────────────┐  ┌────────────────────────────────────────┐ │
│  │ Natural Language Input│  │  Strict Parameter Adherence Checklist  │ │
│  │ (Presets + Dictation) │  │  (Delta, Wings, Time Stop, Slippage)   │ │
│  └───────────┬───────────┘  └───────────────────┬────────────────────┘ │
│              │                                  │                      │
│              ▼                                  ▼                      │
│  ┌───────────────────────┐  ┌────────────────────────────────────────┐ │
│  │ Pairwise Correlation  │  │  Autonomous openD Telemetry Runner     │ │
│  │ Heatmap Canvas (M3)   │  │  (DataStream Ticks, Entry/Exit Engine) │ │
│  └───────────────────────┘  └───────────────────┬────────────────────┘ │
└─────────────────────────────────────────────────┼──────────────────────┘
                                                  │
                                                  ▼
┌────────────────────────────────────────────────────────────────────────┐
│                   TradeSuiteViewModel & StateFlow                      │
│   • nlStrategies: StateFlow<List<NaturalLanguageStrategy>>             │
│   • activeDataStreamTick: StateFlow<String>                            │
│   • correlationMatrix: StateFlow<CorrelationMatrixData>                │
└─────────────────────────────────────────────────┬──────────────────────┘
                                                  │
                                                  ▼
┌────────────────────────────────────────────────────────────────────────┐
│                   TradeSuiteRepository & OpenD Gateway                 │
│   • parseNaturalLanguageStrategy(dictatedText)                         │
│   • computePairwiseCorrelationMatrix(newStrategy, activeStrategies)    │
│   • dispatchAutonomousOpenDOrder(strategyId, tickData)                 │
│   • monitorDataStreamTicks() -> Flow<MoomooTick>                       │
└────────────────────────────────────────────────────────────────────────┘
```

### Data Model Enhancements
- **`CorrelationMatrixData`**: Matrix holding strategy labels, raw correlation coefficients, shrunk Ledoit-Wolf values, and pairwise diversification ratings.
- **`StrictStrategyParams`**: Extended to cover debit verticals, calendar/diagonal parameters, and expected move decay triggers.
- **`OpenDExecutionLog`**: Structured log of autonomous order dispatches, socket response codes, filled prices, execution latency, and slippage calculations.

### Execution Safeguards
- Pre-execution validation verifies:
  1. Connection to moomoo openD socket (`127.0.0.1:11111`) is active.
  2. Delta is within tolerance ($\pm 0.02$).
  3. Minimum credit/debit threshold is satisfied.
  4. System time is prior to the 3:45 PM EST hard cutoff.
  5. Emergency Kill-Switch is in `ARMED` state (not halted).
