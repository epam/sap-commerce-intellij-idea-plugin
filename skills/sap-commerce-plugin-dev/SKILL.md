---
name: sap-commerce-plugin-dev
description: Use when working in the sap-commerce-intellij-idea-plugin repository on IntelliJ Platform plugin code, Gradle builds, Kotlin or Java modules under modules/, or repo-specific UI and contribution conventions.
---

# SAP Commerce Plugin Dev

## Overview

Use this skill when the current workspace is the SAP Commerce Developers Toolset repository or when the task clearly targets this plugin.

This repository is a large IntelliJ Platform plugin composed from many Gradle modules under `modules/`. Work from the owning module outward, keep verification scoped, and prefer existing project conventions over generic IntelliJ plugin advice.

## Workflow

1. Confirm the task is for this repository by checking for `settings.gradle.kts`, `build.gradle.kts`, and the `modules/` directory at the repo root.
2. Identify the owning module first. Most features live in a feature folder such as `modules/logging`, `modules/impex`, `modules/project`, or `modules/typeSystem`, often split into `core`, `ui`, `exec`, `project`, or `diagram`.
3. Read the closest files before making assumptions. Shared behavior often lives in `modules/shared`, `modules/core`, or sibling feature submodules.
4. Keep changes narrow. Match the existing IntelliJ UI DSL, Swing, Kotlin, and Gradle patterns already used nearby.
5. Verify with the smallest useful command first. Only run broad root tasks when the local module-level check is not enough.

## Working Rules

- Prefer targeted searches in the relevant feature module before scanning the whole repository.
- Preserve existing JetBrains UI patterns when editing plugin UI.
- When changing user-facing onboarding or hints, check `README.md` for the note about using Got It tooltips.
- Treat Gradle task names as discoverable. If an exact module task path is unclear, inspect with `./gradlew projects` before guessing.
- Read [references/repo-guide.md](references/repo-guide.md) when you need commands, module layout, or build-specific caveats.

## When To Read References

- Read [references/repo-guide.md](references/repo-guide.md) for common Gradle commands, module structure, and repository-specific build notes.
