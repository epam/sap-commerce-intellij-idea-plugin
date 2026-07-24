# Commit & PR conventions

## Remote topology

- `origin` — contributor's fork. Push branches here.
- `upstream` — `epam/sap-commerce-intellij-idea-plugin`. PR target; merges into `epam:main`.
- Never commit to `main` directly. Branch: `git checkout -b feature/<short-topic>`.
- Push: `git push -u origin feature/<short-topic>`.

## Commit messages

```
<Area> | <Imperative description>
```

- `<Area>` from PR labels → changed functionality → existing commits; defined in `.github/labels.md`. Do not invent new areas unless introducing a new long-lived feature domain.
- Description: release-note-friendly, user-visible capability — not implementation details.
- No `#NNNN` prefix — added by maintainer's squash-merge.
- DCO required: `Signed-off-by: Real Name <email>` (contributor's own name/email).
- No `Co-Authored-By` for AI assistants.
- Commit iterative progress to the feature branch as you work — don't accumulate all changes into a single uncommitted diff.
- Multiple small commits on a branch are fine (squash-merged). Prefer follow-up commits over `--amend` + force-push on already-pushed branches.

Examples:
- `ImpEx | Resolve external macros from included files`
- `Project Import | Detect extension source availability`
- `AI | Expose Bean System enums as MCP tool`

## Opening the PR

Target `epam/…:main` from `<fork>:<branch>` via the GitHub compare page:

```
https://github.com/epam/sap-commerce-intellij-idea-plugin/compare/main...<fork-owner>:sap-commerce-intellij-idea-plugin:<branch>?expand=1
```

- Title — `<Area> | <description>` (no `#NNNN`)
- Labels — cross-cutting + feature label(s); add impact labels if required (see below); check a recent analogous PR
- Assignee — self; Reviewer — `mlytvyn`; Milestone — current open
- Create as ready-for-review unless WIP.

### Impact labels

- `Requires - Project Refresh` — user must refresh after update; bump `ProjectImportConstants.MIN_REFRESH_API_VERSION`
- `Requires - Project Reimport` — user must reimport (stronger); bump `MIN_IMPORT_API_VERSION`

## CHANGELOG

Add bullet under current unreleased version's matching `### \`<Area>\`` section in `CHANGELOG.md`:

```
- <Imperative description> [#NNNN](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/NNNN)
```

Flow: commit without link → push → open PR → add `[#NNNN](…)` → follow-up commit → push.

Keep `<!-- Plugin description -->` / `<!-- Plugin description end -->` markers in README intact.
