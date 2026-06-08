# Contributing

Thank you for your interest in Formulator! We welcome contributions of all
kinds — bug fixes, features, documentation, and ideas.

This project is governed by the [Contributor Covenant][covenant] code of
conduct. By participating, you agree to uphold its standards.

[covenant]: https://www.contributor-covenant.org/version/2/1/code_of_conduct/

---

## Getting Started

1. **Fork** the repository.
2. **Clone** your fork:
   ```bash
   git clone https://github.com/<your-username>/formulator.git
   ```
3. **Open** the project in IntelliJ IDEA — the `.iml` file and `.idea/`
   directory configure the module structure automatically.
4. **Verify** that all tests pass by running the test suite in IntelliJ or
   via your configured test runner.

---

## Code Style

- Follow standard **Kotlin** conventions (see
  [kotlinlang.org/docs/coding-conventions](https://kotlinlang.org/docs/coding-conventions.html)).
- Do **not** add extraneous comments — the existing codebase favours
  self-documenting code and clear naming.
- Use IntelliJ's built-in Kotlin formatter (`Ctrl+Alt+L` / `Cmd+Option+L`).

---

## Testing

- All tests live under `test/` and mirror the `src/` package layout.
- We use **JUnit 5** (`junit-jupiter`). Parameterised tests are encouraged
  where appropriate.
- **Run the full suite** before submitting a pull request. Every new feature
  or bug fix must include tests.
- Maintain or improve the existing test coverage.

---

## Pull Request Process

1. Create a **feature branch** from `main`:
   ```bash
   git checkout -b feat/my-feature
   ```
2. Make your changes, keeping commits small and focused.
3. Write or update tests to cover your changes.
4. Ensure the entire test suite passes.
5. Push your branch and open a pull request.
6. In the PR description, explain **what** the change does and **why** it is
   needed. Link any related issues.

Guidelines:

- One feature / fix per PR — keep reviews manageable.
- Clearly label the PR title (e.g., `feat:`, `fix:`, `docs:`, `refactor:`).
- Be responsive to review feedback.

---

## Reporting Issues

- **Bug reports**: include the input expression, expected result, actual
  result, and the relevant error message (if any).
- **Feature requests**: describe the use case and what the desired behaviour
  would look like.
- Search existing issues before opening a new one.

---

## License

By contributing, you agree that your contributions will be licensed under the
[Apache License 2.0](LICENSE).
