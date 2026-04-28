# Repository Guide

## Core facts

- Root plugin build: `build.gradle.kts`
- Gradle settings: `settings.gradle.kts`
- Main source roots at the repo level: `src`, `resources`, `tests`
- Feature implementation modules: `modules/*`
- JPS support module: `jps-plugin`

## Module layout

The settings file auto-includes every `build.gradle.kts` found under `modules/` up to depth 4. Feature folders usually split responsibilities like this:

- `core`: shared feature logic, PSI, models, services
- `ui`: dialogs, tool windows, notifications, completion, forms
- `exec`: execution or remote-action integration
- `project`: project import and project-level integration
- `diagram`: diagram support where relevant

Commonly useful roots:

- `modules/shared`: cross-feature shared code
- `modules/core`: plugin-wide base functionality
- `modules/project`: project import, compile, UI, extension metadata
- `modules/logging`: logger preview and remote/custom logger UI

## Commands

Run from the repository root.

- Start the sandbox IDE: `./gradlew runIde`
- Run tests: `./gradlew test`
- List modules/tasks when scoping verification: `./gradlew projects`
- Build the plugin artifact: `./gradlew buildPlugin`

When exact module task paths matter, prefer discovering them from `./gradlew projects` or the nearest `build.gradle.kts` rather than assuming the path.

## Build caveats

- The project uses Java 21 and Kotlin JVM toolchain 21.
- `processResources` depends on a PR-fetch task that uses `GITHUB_TOKEN`.
- If that token is unavailable and the task is unrelated to PR metadata, set `GITHUB_SKIP_TASK_FETCH_PRS=true` for local verification.

## Working approach

- Find the owning feature under `modules/` first, then inspect sibling submodules for related behavior.
- Prefer narrow verification close to the changed feature before running repository-wide tasks.
- Reuse existing patterns in nearby files instead of introducing new conventions.
