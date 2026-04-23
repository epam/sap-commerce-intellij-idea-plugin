/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package sap.commerce.toolset.logging.ui.textCompletion


import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PlainPrefixMatcher
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.CharFilter
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Iconable
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiPackage
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache
import com.intellij.util.indexing.IdFilter
import com.intellij.util.textCompletion.TextCompletionProvider
import sap.commerce.toolset.logging.ui.textCompletion.LoggerCompletionProvider.Companion.MAX_VISIBLE_SUGGESTIONS
import sap.commerce.toolset.logging.ui.textCompletion.LoggerCompletionProvider.Companion.OVERFLOW_PROBE_BUDGET
import javax.swing.Icon


/**
 * Completion provider for the logger name field. Streams matches from
 * [PsiShortNamesCache] on every popup refresh — no upfront enumeration,
 * no cached list. Resolution is per-query, prefix-filtered, and bounded:
 * typing `com.exam` iterates package names starting with that prefix plus
 * short class names whose last-segment prefix matches `exam`, resolves
 * only those, stops at [MAX_VISIBLE_SUGGESTIONS] matches, and probes
 * up to [OVERFLOW_PROBE_BUDGET] additional candidates to count overflow
 * for the `… more matches` hint row.
 *
 * Runs on the completion thread without a read action; PSI access must
 * therefore be wrapped in [com.intellij.openapi.application.Application.runReadAction] explicitly.
 *
 * The class scan runs in two phases to avoid a stub-index deadlock: the
 * platform refuses nested `processElements` calls, so `processClassesWithName`
 * (which goes through the stub index) must NOT be called from inside the
 * `processAllClassNames` processor lambda (which also goes through the
 * index). See the comment in [fillCompletionVariants] for details.
 *
 * Paging Search-Everywhere style (clickable `... more` that fetches the
 * next page) isn't supported by the lookup API — lookup elements are
 * terminal and there's no re-trigger hook. The hint row is a UX nudge
 * to narrow the prefix instead.
 */
internal class LoggerCompletionProvider(
    private val project: Project,
) : TextCompletionProvider {

    override fun getAdvertisement(): String? = null
    override fun acceptChar(c: Char): CharFilter.Result = CharFilter.Result.ADD_TO_PREFIX

    override fun applyPrefixMatcher(result: CompletionResultSet, prefix: String): CompletionResultSet =
        // caseInsensitive() makes the platform's post-filter of addAllElements
        // match regardless of letter case; without it, typing `serv` would
        // fail to match lookup strings starting with `Serv`.
        result.caseInsensitive().withPrefixMatcher(PlainPrefixMatcher(prefix))

    override fun getPrefix(text: String, offset: Int): String =
        text.substring(0, offset.coerceIn(0, text.length))

    override fun fillCompletionVariants(
        parameters: CompletionParameters,
        prefix: String,
        result: CompletionResultSet,
    ) {
        if (prefix.length < MIN_PREFIX_LENGTH) return
        if (DumbService.isDumb(project)) return  // indexes aren't ready — bail quietly.

        val scope = GlobalSearchScope.allScope(project)
        val idFilter = IdFilter.getProjectIdFilter(project, /* includeNonProjectItems = */ true)

        // The last dot-segment of the prefix is the short-name pattern. For a
        // dotted prefix (`com.example.MyServ`) it's just `MyServ`; for a bare
        // prefix (`MyServ`) it's the whole input. We use it to cheaply filter
        // the short-names index before paying to resolve any class PSI.
        val shortNamePrefix = prefix.substringAfterLast('.', prefix)

        // When the user is typing a bare (no-dot) prefix, they probably mean
        // a short class name — `Service` should match `com.example.Service`.
        // We add the short name as an alternate lookup string so the prefix
        // matcher matches it even though the FQN doesn't start with the
        // user's input. Accepting the element still inserts the FQN (the
        // primary lookup string).
        val matchOnShortName = !prefix.contains('.')

        // Shared collection state: the processors below write here.
        val matched = ArrayList<LookupElement>(MAX_VISIBLE_SUGGESTIONS)
        var overflow = 0
        var overflowCapped = false

        fun buildElement(fqn: String, shortName: String?, icon: Icon): LookupElement {
            val builder = LookupElementBuilder.create(fqn)
                .withIcon(icon)
            return if (matchOnShortName && shortName != null) builder.withLookupString(shortName)
            else builder
        }

        /**
         * [iconSupplier] is a lazy producer so that callers which had to
         * resolve PSI to determine the icon (e.g. the class phase) don't
         * pay that cost for items that turn out not to match the prefix
         * or that fall past the cap. Packages pass a constant supplier.
         */
        fun accept(fqn: String, shortName: String? = null, iconSupplier: () -> Icon): Boolean {
            // Matching is done with plain case-insensitive startsWith —
            // same semantics as the PlainPrefixMatcher we set in
            // applyPrefixMatcher with caseInsensitive(), but evaluated here
            // so we can cheaply filter before allocating a LookupElement.
            val matches = fqn.startsWith(prefix, ignoreCase = true) ||
                (matchOnShortName && shortName != null && shortName.startsWith(prefix, ignoreCase = true))
            if (!matches) return true

            if (matched.size < MAX_VISIBLE_SUGGESTIONS) {
                matched += buildElement(fqn, shortName, iconSupplier())
            } else {
                overflow++
                if (overflow >= OVERFLOW_PROBE_BUDGET) {
                    overflowCapped = true
                    return false  // stop — we have enough to show "N+".
                }
            }
            return true
        }

        ApplicationManager.getApplication().runReadAction {
            // Packages first: cheaper to iterate, users often want a package
            // FQN as a logger name. Walk from the root, pruning subtrees that
            // can't possibly match the prefix.
            //
            // All packages render with the same icon, so we pass a shared
            // constant — no per-package PsiPackage.getIcon() call.
            walkPackages(project, scope, prefix) { pkgFqn ->
                accept(pkgFqn) { AllIcons.Nodes.Package }
            }

            if (overflowCapped) return@runReadAction

            // Classes: two phases to avoid a stub-index deadlock.
            //
            // Phase 1: collect the short names that prefix-match. This runs
            //   inside processAllClassNames's processor lambda — which itself
            //   walks the stub index — so we MUST NOT make other stub-index
            //   calls from in here. No processClassesWithName, no PSI resolve.
            //
            // Phase 2: after processAllClassNames returns, iterate the
            //   collected short names and resolve each via processClassesWithName.
            //   Now that we're outside the outer index operation, nesting is
            //   allowed again.
            //
            // Violating this is how we hit "Nesting processElements call
            // under other stub index operation can lead to a deadlock".
            //
            // Skip entirely when the short-name prefix is empty (e.g. user
            // typed `com.sap.`). An empty prefix matches every short name,
            // so phase 1 would either blow up or, bounded by a budget,
            // arbitrarily truncate — giving unpredictable results. The
            // package walk handles this case; the user's next keystroke
            // will re-query with a concrete short-name prefix.
            if (shortNamePrefix.isEmpty()) return@runReadAction

            val shortNames = PsiShortNamesCache.getInstance(project)

            // Phase 1. Bound at (cap + overflow-probe) short names so a
            // single-letter prefix doesn't materialise the entire symbol
            // table before we've even started resolving. Each short name
            // typically resolves to 1–2 classes, so a few hundred collected
            // names is more than enough to fill the cap and measure overflow.
            val matchingShortNames = ArrayList<String>()
            val phase1Budget = (MAX_VISIBLE_SUGGESTIONS + OVERFLOW_PROBE_BUDGET) * 2
            shortNames.processAllClassNames({ shortName ->
                ProgressManager.checkCanceled()
                if (shortName.startsWith(shortNamePrefix, ignoreCase = true)) {
                    matchingShortNames += shortName
                    // Budget-cut so a huge prefix-match set doesn't blow memory.
                    matchingShortNames.size < phase1Budget
                } else {
                    true
                }
            }, scope, idFilter)

            // Phase 2. Resolve collected names. accept() returns false once
            // the probe budget is spent, and we propagate that to stop the
            // outer loop.
            //
            // Icons are resolved lazily — only if the FQN prefix-matches and
            // we're still below the visible cap. That avoids paying for a
            // PSI icon lookup on classes whose short name matched but whose
            // FQN didn't (e.g. short name `Service` resolves to both
            // `com.example.Service` and `com.unrelated.Service`; only one
            // matches a user prefix like `com.example.S`).
            //
            // Resolution happens here (while we hold PSI and the read lock)
            // not later during rendering — so painting the popup is pure
            // Swing, no PSI work on the EDT. ICON_FLAG_VISIBILITY gives us
            // the class-kind icon (C / I / E / @) plus the public/private
            // marker dot; we skip ICON_FLAG_READ_STATUS, which triggers a
            // VFS writability check per class.
            outer@ for (shortName in matchingShortNames) {
                ProgressManager.checkCanceled()
                var keepGoing = true
                shortNames.processClassesWithName(shortName, { psiClass ->
                    ProgressManager.checkCanceled()
                    val fqn = psiClass.qualifiedName
                    if (fqn != null) {
                        keepGoing = accept(fqn, shortName) { psiClass.safeIcon() }
                    }
                    keepGoing
                }, scope, idFilter)
                if (!keepGoing) break@outer
            }
        }

        // Batch-add so the platform treats these as one sort group — the
        // "freeze top results" behaviour doesn't fragment our display.
        result.addAllElements(matched)

        if (overflow > 0) {
            // Bypass the prefix matcher for the hint so it's visible
            // regardless of what the user typed: the hint itself doesn't
            // look anything like a logger FQN, so a normal matcher would
            // filter it out.
            val hintResult = result.withPrefixMatcher(PlainPrefixMatcher(""))
            hintResult.addElement(buildMoreHintElement(overflow, overflowCapped))
        }
    }

    /**
     * Walk packages depth-first from the root, pruning subtrees whose FQN
     * cannot possibly extend to a prefix match. Each match is offered to
     * [accept]; when `accept` returns false we stop the whole walk.
     */
    private fun walkPackages(
        project: Project,
        scope: GlobalSearchScope,
        prefix: String,
        accept: (String) -> Boolean,
    ) {
        val root = JavaPsiFacade.getInstance(project).findPackage("") ?: return
        val stack = ArrayDeque<PsiPackage>().apply { addLast(root) }
        while (stack.isNotEmpty()) {
            ProgressManager.checkCanceled()
            val pkg = stack.removeLast()
            val fqn = pkg.qualifiedName

            // Prune: if we're past the root, the package FQN must be either
            // a prefix of the user's input (so its children might match) or
            // start with the user's input (so it itself might match). Neither
            // means the whole subtree is unreachable.
            if (fqn.isNotEmpty() &&
                !prefix.startsWith(fqn, ignoreCase = true) &&
                !fqn.startsWith(prefix, ignoreCase = true)
            ) {
                continue
            }

            if (fqn.isNotEmpty() && !accept(fqn)) return

            pkg.getSubPackages(scope).forEach { stack.addLast(it) }
        }
    }

    private fun buildMoreHintElement(overflow: Int, capped: Boolean): LookupElement {
        val label = if (capped) "$overflow+" else "$overflow"
        val text = "… $label more match${if (overflow == 1 && !capped) "" else "es"} — refine search"
        val element = LookupElementBuilder.create(text)
            .withTypeText("hint", /* grayed = */ true)
            // Accepting the hint would insert its label text into the field,
            // which is nonsense. Cancel the insertion so the user's original
            // prefix stays untouched whether they Enter, Tab, or click it.
            .withInsertHandler { ctx, _ ->
                ctx.document.deleteString(ctx.startOffset, ctx.tailOffset)
            }
        // Pin to the bottom regardless of the platform's relevance scoring.
        return PrioritizedLookupElement.withPriority(element, Double.NEGATIVE_INFINITY)
    }

    companion object {
        private const val MIN_PREFIX_LENGTH = 2
        private const val MAX_VISIBLE_SUGGESTIONS = 50
        private const val OVERFLOW_PROBE_BUDGET = 50
    }
}

/**
 * Resolve the presentation icon for a class without letting an IconProvider
 * failure take down the completion popup. A third-party IconProvider that
 * throws for a specific PSI shape is a real (if rare) hazard — we'd rather
 * render a generic class icon than surface the exception and lose the whole
 * result set. ProcessCanceledException must still propagate or the completion
 * thread's cancellation is broken.
 *
 * Uses ICON_FLAG_VISIBILITY for the public/private marker dot; skips
 * ICON_FLAG_READ_STATUS which triggers VFS writability lookups we don't
 * want to pay for on every keystroke.
 */
private fun PsiClass.safeIcon(): Icon = try {
    getIcon(Iconable.ICON_FLAG_VISIBILITY) ?: AllIcons.Nodes.Class
} catch (e: ProcessCanceledException) {
    throw e
} catch (_: Throwable) {
    AllIcons.Nodes.Class
}

