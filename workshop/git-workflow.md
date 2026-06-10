# Workshop Git Workflow

## Branch Structure

```
main                     ← Production state (all quality gates active)
workshop/base            ← Workshop branch with 3 commits:
  ├── A (workshop/start)        — Starting point: gates disabled, no skills
  ├── B (workshop/enablement-2) — All skills + requirements workflow
  └── C (workshop/enablement-3) — Quality gates activated
workshop/final-reference ← Snapshot of main before workshop preparation
```

## How the Workshop Works

The workshop has 3 iterations. Between iterations, prepared enablement commits are applied via `git cherry-pick`:

```
workshop/start → Iteration 1 (live) → cherry-pick enablement-2 → Iteration 2 (live) → cherry-pick enablement-3 → Iteration 3 (live)
```

| Phase | What happens | Agentic Engineering Aspect |
|---|---|---|
| Start | No skills, no quality gates | — |
| Iteration 1 | Implement claim creation | Plan & Implement (CLAUDE.md only) |
| Cherry-pick enablement-2 | Skills + issue template appear | — |
| Iteration 2 | Implement reserve management | Requirements-Driven (Issue + Skills) |
| Cherry-pick enablement-3 | PMD, SpotBugs, Spotless, ArchUnit, Coverage active | — |
| Iteration 3 | Implement payment management | Governed Engineering (Quality Gates) |

## Starting the Workshop

```bash
git checkout workshop/start
git checkout -b workshop/live
```

## After Iteration 1 (Claim done)

```bash
git add .
git commit -m "Add claim creation"
git cherry-pick workshop/enablement-2
```

## After Iteration 2 (Reserve done)

```bash
git add .
git commit -m "Add reserve management"
git cherry-pick workshop/enablement-3
```

## After Iteration 3 (Payment done)

```bash
git add .
git commit -m "Add payment management"
```

## Emergency: Checkpoints

If a dry-run was performed beforehand (see below), checkpoint tags exist:

```bash
git tag -l "workshop/checkpoint/*"
```

Jump to any checkpoint:

```bash
git reset --hard workshop/checkpoint/iteration-2-done
```

This moves the current branch to the saved state — regardless of where you are.

## Applying Later Changes from `main`

If commits are made on `main` after the workshop preparation (e.g. UI improvements):

```bash
git checkout workshop/base
git rebase main
# Re-apply tags (rebase changes commit hashes):
git tag -f workshop/start <hash-A>
git tag -f workshop/enablement-2 <hash-B>
git tag -f workshop/enablement-3 <hash-C>
```

(Read hashes from `git log --oneline workshop/base`.)
