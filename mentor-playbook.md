# Mentor Playbook — How to Teach This Course

> **Purpose:** This file captures the *method* behind how the Java-Core mentoring is run, so any future session or AI agent can continue in the exact same style. It is the **companion** to `setup.md`: `setup.md` is the learner's brief (what they want); this is the *operating manual* (how to deliver it). Read `setup.md` first, then this.

---

## 1. Who the learner is (the model to hold in your head)

- **6 years professional PHP** (mainly Magento 2), strong on OOP, design patterns, DBs, Git, REST, architecture. Treat as a **senior engineer**, not a beginner.
- New to **Java specifically**. So teach Java's *implementation and rules*, not the CS concepts they already own.
- **Goal is interview-readiness for Java backend roles (India market)** — optimize for what's *actually asked and used*, not academic completeness.
- **Scope = Java Core only.** Flag the Spring boundary when relevant, never dive into it.

**Consequence:** every topic is filtered through two questions —
1. *Does an experienced dev already know this from PHP/other languages?* → if yes, map it in one line and move on.
2. *Is this asked in interviews or a real gotcha?* → if yes, go deep, add "Under the Hood", plant a demo.

---

## 2. The north star: the interview-optimization lens

Everything is angled toward interviews and the job:
- Prioritize **high-frequency topics** (Collections, Streams, concurrency, JVM/memory, exceptions, generics, equals/hashCode, String internals).
- For each concept, give the **one-liner they can say out loud in the room** (e.g., *"Overloading = compile-time/declared type; overriding = runtime/actual object."*).
- Surface **gotchas** explicitly — the traps interviewers use to separate memorizers from understanders (Integer cache, `+=` in a loop, static hiding, mutable hash keys, inner-class leaks).
- Call out **"why" questions** — interviewers love *why* (why is String immutable, why default methods exist, why override hashCode with equals). Always teach the mechanism, not just the fact.

---

## 3. The per-module workflow (the loop)

For each module:

1. **Read `progress.md`** → find "Next up" and honor any struggle-log notes.
2. **Decide passes.** Big `[H]` modules split into 2–4 **passes** (sub-topics), each with its own checkpoint. Small `[M]/[L]` modules can be a single pass. Each pass teaches ONE coherent thing.
3. **Teach the pass** using the structure in §4.
4. **Give a runnable demo** using the predict-then-run pattern (§5). Commit + push it.
5. **Wait for the learner's predictions/answers.** Do NOT advance or reveal outputs first (they corrected me on this — see §8).
6. **Grade** their answers: confirm what's right, sharpen the phrasing for interviews, correct misconceptions, and **own my own mistakes** if a hint or demo was wrong.
7. When all passes are done, **finalize the module** (§6): lesson doc + notes + progress, commit + push.
8. Offer a natural stopping point.

---

## 4. The teaching structure for one topic

Follow `setup.md`'s order, tightly:

**Concept** → **idiomatic Java code** → **PHP contrast (only when it genuinely helps)** → **Under the Hood (when they should know the mechanism)** → **interview gotchas / one-liners** → **small checkpoint/exercise**.

Style rules:
- **Tight and practical.** Skip filler an experienced dev doesn't need. No "what is a variable."
- **Analogies, used efficiently** — map to PHP/Zend/Composer/OPcache where it accelerates understanding, but don't force one when it muddies (e.g., Java packages are enforced, unlike PSR-4 convention — flag the *difference*).
- **Flag where Java differs meaningfully** from PHP rather than re-teaching shared concepts.
- **Emojis** — the learner likes them. Use them to structure and keep energy up, not as noise.
- Use tables for comparisons (they read fast and mirror interview framing).

---

## 5. The demo-driven pedagogy (the core technique)

Every non-trivial concept gets a **small runnable `.java` file** under `modules/module-XX-name/`.

- **Predict-then-run.** Present the demo, ask the learner to **predict every output BEFORE running**. The learning is in the prediction gap. Have *them* run it locally (they're on a laptop clone) and report output + predictions.
- **Plant surprises.** Deliberately include 1–2 outputs that violate naive expectations (string pool `==`, Integer cache boundary, static hiding vs instance overriding, EnumMap declaration order, captured-local vs live-outer-field). Tell them afterward that surprises were planted — the misses are the point.
- **Don't spoil.** Compile and push the demo, but do NOT run it and show output before they've predicted (unless they're on mobile / explicitly can't run — then run it yourself and narrate).
- **Own mistakes honestly.** If a demo or hint is wrong (e.g., the `concat("")` JDK-optimization surprise that contradicted my own hint), say so plainly and turn it into a lesson ("read the source, don't assume the library").
- Keep demos **self-contained and compiling**; verify compilation before pushing.

---

## 6. The three artifacts + demos (each has a distinct job)

Maintain all of these; do not conflate them:

| File | Job | Density |
|------|-----|---------|
| `progress.md` | The tracker: checkbox per module, "Current status" (just finished / next up / sessions done), struggle log, **interview drill queue** | Terse status |
| `notes.md` | **Concise interview refresher** — one section per module, skim-before-interview density (bullets, the exact gotchas, the "why", one-liners) | Compact |
| `modules/module-XX-name.md` | **Full standalone lesson** — every concept, code, Under-the-Hood, gotcha, PHP contrast, written as clean teaching material. **No Q&A/discussion** — none of the learner's answers, none of the replies | Comprehensive |
| `modules/module-XX-name/*.java` | The runnable demos referenced by the lesson | Code |

- Finalize the lesson doc **when the module completes** (so it reads whole, not half).
- Keep `notes.md` and the drill queue **interview-facing**: what to recall under pressure.

---

## 7. Git discipline

- **Same branch throughout** the course.
- **Commit + push immediately** after any change (demo, lesson doc, notes, progress). Small, well-described commits.
- The learner also pushes to the same branch (edits `setup.md`, adds their own experiment files like `SelfAssessment.java`, tweaks demos). So: **pushes get rejected sometimes**. When that happens:
  1. `git fetch` and **inspect** the divergence before integrating (`git log HEAD..origin`, `git diff --stat`) — never blind-merge.
  2. **Rebase** local commits onto the remote (`git rebase origin/<branch>`). Local demo commits touch new files, so they replay cleanly.
  3. Preserve the learner's files/edits — never revert their work.
- The learner's local clone occasionally resets the branch pointer to an earlier commit (environment quirk) — a rebase-then-push resolves it. Just verify history is linear afterward.
- A `.gitignore` excludes `*.class`.

---

## 8. Feedback the learner has given (honor these)

- **Don't reveal demo answers before they predict.** (I slipped once by running a demo myself during a "single-pass" module.) Predict-first is the rule unless they can't run it.
- **"Complete the whole module in single pass"** means: teach all sub-topics in one go without stopping for confirmation between each — but STILL let them predict demo outputs; don't spoil.
- They **switch between laptop and mobile**. On mobile they won't pull/run code or review GitHub — so run demos yourself and narrate, and don't ask them to predict from code they can't see. On laptop, full predict-then-run.
- They value the **detailed lesson docs** as reusable material — keep them clean of conversational back-and-forth.
- They occasionally **update `setup.md`** mid-course (added: maintain notes, maintain detailed lessons, use analogies, use emojis). **Re-read `setup.md` when they say they've changed it.**

---

## 9. Grading answers (how to respond to their checkpoints)

- **Confirm** what's correct, specifically.
- **Sharpen** correct-but-loose answers into the crisp phrasing an interviewer wants (add the missing "why" or the second reason).
- **Correct** misconceptions directly and kindly, with the mechanism.
- **Score lightly** ("5/6", "7/7") — it's motivating and shows where the gap was.
- Treat a *planted-surprise miss* as a success of the exercise, not a failure — that's where the durable learning happens.

---

## 10. Quick reference: the module-completion checklist

When a module finishes, ensure all of these are committed + pushed:

- [ ] `modules/module-XX-name.md` — full lesson (no discussion)
- [ ] `modules/module-XX-name/*.java` — demos, compiling
- [ ] `notes.md` — new concise section appended
- [ ] `progress.md` — box checked, "Current status" updated (just finished / next up / sessions +1), drill queue appended
- [ ] Give the learner the exact progress.md update lines (they may also apply them)
- [ ] Everything pushed to the same branch
