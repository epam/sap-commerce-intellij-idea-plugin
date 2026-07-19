# Commit & PR conventions

## Remote topology

- `origin` — contributor's fork. Push branches here.
- `upstream` — `epam/sap-commerce-intellij-idea-plugin`. PR target; merges into `epam:main`.
- Never commit to `main` directly. Branch: `git checkout -b feature/<short-topic>`.
- Push: `git push -u origin feature/<short-topic>`.

## Commit messages

```
<Category> | <Imperative description>
```

- `<Category>` matches the CHANGELOG section: `AI`, `Project Import`, `Spring`, `Type System`, `ImpEx`, `Bean System`, `Other`.
- No `#NNNN` prefix — added by maintainer's squash-merge.
- DCO required: `Signed-off-by: Real Name <email>` (contributor's own name/email).
- No `Co-Authored-By` for AI assistants.
- Commit iterative progress to the feature branch as you work — don't accumulate all changes into a single uncommitted diff.
- Multiple small commits on a branch are fine (squash-merged). Prefer follow-up commits over `--amend` + force-push on already-pushed branches.

## Opening the PR

Target `epam/…:main` from `<fork>:<branch>` via the GitHub compare page:

```
https://github.com/epam/sap-commerce-intellij-idea-plugin/compare/main...<fork-owner>:sap-commerce-intellij-idea-plugin:<branch>?expand=1
```

- **Title** — `<Category> | <description>` (no `#NNNN`)
- **Labels** — cross-cutting + feature label(s); check a recent analogous PR
- **Assignee** — self; **Reviewer** — `mlytvyn`; **Milestone** — current open
- Create as ready-for-review unless WIP.

## CHANGELOG

Add bullet under current unreleased version's matching `### \`<Category>\`` section in `CHANGELOG.md`:

```
- <Imperative description> [#NNNN](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/NNNN)
```

Flow: commit without link → push → open PR → add `[#NNNN](…)` → follow-up commit → push.
