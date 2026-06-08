# Formulator

**A unit-aware mathematical expression engine and calculator for the JVM.**

Formulator parses mathematical expressions involving physical quantities (values
with units), tracks dimensions through arithmetic, infers unit types from
context, and can convert between compatible units — including currencies.

Built in pure Kotlin with zero runtime dependencies.

---

## Features

- **Expression parsing** — full arithmetic (`+`, `-`, `*`, `/`, `^`) with
  correct BODMAS precedence and right-associative exponentiation
- **Physical quantities** — numbers carry units (`10 m`, `5 kg`, `2.5 hr`)
  through every operation
- **Unit conversion** — convert between any two units of the same dimension
  (e.g., `cm → m`, `min → s`)
- **Dimension inference** — when a constant has no explicit unit, the solver
  infers its dimension from surrounding context
- **Currency support** — multi-currency arithmetic with a pluggable exchange
  rate provider; automatic conversion on `+`/`-`
- **Auto-scaling output** — display values in human-readable form
  (e.g., `1500 m → 1.5 km`)
- **Layered API** — use the low-level parser/evaluator directly or the
  top-level `FormulaApi` for a polished interface

---

## Architecture

```
String expression
    │
    ▼
Tokenizer ────→ List<Token>
    │
    ▼
Parser ───────→ Expr (AST)
    │
    ├──→ DimBuilder + Solver ──→ inferred dimensions
    │
    ▼
Evaluator ────→ Quantity
    │
    ▼
Formatter ────→ String / FormattedQuantity
```

The pipeline is fully composable — each stage can be used independently.

---

## Quick Start

### Prerequisites

- [IntelliJ IDEA](https://www.jetbrains.com/idea/) (Community or Ultimate)
- JDK 17+

### Run

1. Open the project folder in IntelliJ IDEA.
2. Run `src/Main.kt` — the `main()` function registers a few units and
   demonstrates basic quantity arithmetic.

### Test

All 80+ tests live under `test/`. Right-click the `test/` directory in
IntelliJ and select **Run All Tests**, or run individual test classes.

---

## Usage Examples

```kotlin
// Register some units
UnitRegistry.register(Unit("km", 1000.0, Dimension(length = 1)))
UnitRegistry.register(Unit("m",   1.0,   Dimension(length = 1)))
UnitRegistry.register(Unit("cm",  0.01,  Dimension(length = 1)))

// Arithmetic with automatic unit conversion
val total = Quantity(1.0, UnitRegistry.get("km"))
          + Quantity(2.0, UnitRegistry.get("m"))
          + Quantity(120.0, UnitRegistry.get("cm"))
println(total)   // → 2.0 m
```

```kotlin
// Using the top-level API
val api = FormulaApi(FormulaService())
val result = api.evaluate("10 m / 2 s")
println(result)  // → Success(5.0 m/s)
```

```kotlin
// Currency conversion
val usd = Quantity(100.0, UnitRegistry.get("USD"))
val pkr = api.convert(usd, "PKR")
println(pkr)     // → 27950.0 PKR  (using static exchange rates)
```

---

## Project Structure

```
src/
├── Main.kt                  # Demo entry point
├── api/                     # Public API layer
│   ├── FormulaApi.kt        #   UI-facing entry point
│   ├── FormulaEngine.kt     #   Orchestrates parsing + solving + evaluation
│   ├── FormulaError.kt      #   Error types (sealed class)
│   ├── FormulaResult.kt     #   Result wrapper (Success / Error)
│   └── FormulaService.kt    #   Service layer with error handling
├── currency/                # Currency support
│   ├── CurrencyBootstrap.kt
│   ├── CurrencyConverter.kt
│   ├── ExchangeRateProvider.kt
│   └── StaticExchangeRateProvider.kt
├── dimensions/              # Dimension inference
│   ├── Constraint.kt
│   ├── DimBuilder.kt
│   ├── DimExpr.kt
│   ├── DimVar.kt
│   ├── DimensionVector.kt
│   ├── LinearExpr.kt
│   └── Solver.kt
├── evaluator/
│   └── Evaluator.kt
├── parser/
│   ├── Expr.kt              # Expression AST nodes
│   ├── Parser.kt            # Recursive-descent parser
│   ├── Token.kt             # Token types
│   └── Tokenizer.kt         # Lexer
└── units/
    ├── Dimension.kt
    ├── Quantity.kt
    ├── Unit.kt
    ├── UnitOps.kt
    ├── UnitParser.kt
    ├── UnitRegistry.kt
    └── format/
        ├── FormattedQuantity.kt
        ├── UnitConverter.kt
        ├── UnitExpr.kt
        ├── UnitExprParser.kt
        └── UnitFormatter.kt
```

---

## Dependencies

| Dependency | Version | Scope |
|---|---|---|
| Kotlin (JVM) | — | Compile |
| JUnit Jupiter | 5.8.1 | Test |

There are **no third-party runtime dependencies**. A proper build system
(Gradle / Maven) is planned.

---

## License

Formulator is licensed under the **Apache License, Version 2.0**. See
[LICENSE](LICENSE) for the full text.
