# Contributing to SeyMeteo

Thank you for your interest in contributing! This guide covers the workflow for adding features, fixing bugs, and submitting pull requests.

---

## Code of Conduct

Be respectful, constructive, and inclusive. This is a community project serving the Seychellois people.

---

## Branch Strategy

| Branch | Purpose |
|--------|---------|
| `main` | Stable, production-ready code |
| `develop` | Integration branch for upcoming release |
| `feature/<name>` | New features, branched from `develop` |
| `fix/<name>` | Bug fixes |
| `chore/<name>` | Non-functional changes (refactor, deps, docs) |

```bash
# Start a new feature
git checkout develop
git pull origin develop
git checkout -b feature/tide-chart
```

---

## Commit Message Style

We follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <short summary>

[optional body]
[optional footer]
```

| Type | Use for |
|------|---------|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation only |
| `style` | Formatting, whitespace (no logic change) |
| `refactor` | Code restructure, no feature change |
| `test` | Adding or fixing tests |
| `chore` | Build system, dependency updates |
| `perf` | Performance improvement |

**Examples:**
```
feat(forecast): add 7-day rain chance bar chart
fix(room): prevent duplicate forecast inserts on rapid refresh
docs(readme): update API endpoint table
chore(deps): bump Compose BOM to 2024.09.00
```

---

## Development Setup

1. **Clone:** `git clone https://github.com/YOUR_USERNAME/SeyMeteo.git`
2. **Open** in Android Studio (Hedgehog+) or IntelliJ with Android plugin
3. **Sync Gradle** — it will download all dependencies automatically
4. **Run** on an emulator (API 26+) or physical device via ADB

### Recommended Emulator Config
- API Level: 34 (Android 14)
- RAM: 4GB
- Display: 1080×2400 (Phone)

---

## Architecture Rules

- **No business logic in Composables.** All state lives in `WeatherViewModel`.
- **Repository is the single source of truth.** Never call Retrofit directly from a ViewModel.
- **Offline-first.** Always check Room cache before making a network call.
- **Error states must be handled.** Every `Result.failure` must surface a UI message.
- **Use `StateFlow`** in ViewModels, not `LiveData`.
- **Room DAOs return `Flow<T>`** for UI-observed data, `suspend fun` for one-shot operations.

---

## Pull Request Checklist

Before submitting a PR, ensure:

- [ ] Code builds without warnings (`./gradlew assembleDebug`)
- [ ] No hardcoded strings — all UI text in `strings.xml`
- [ ] New features tested on API 26 (minSdk) and API 34
- [ ] Dark mode checked (toggle in Settings → Theme → Dark)
- [ ] Offline behaviour tested (enable airplane mode after first load)
- [ ] PR description explains *why*, not just *what*

---

## Reporting Issues

Open a GitHub Issue with:
1. **Device / Android version**
2. **Steps to reproduce**
3. **Expected vs actual behaviour**
4. **Logcat output** (filter: `SeyMeteo`)

---

## Questions?

Open a Discussion on GitHub or raise in the PR comments.
