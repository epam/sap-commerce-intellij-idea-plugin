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
import com.intellij.util.application
import com.intellij.util.indexing.IdFilter
import com.intellij.util.textCompletion.TextCompletionProvider
import javax.swing.Icon


/**
 * Provides bounded completion for logger names by combining matching packages and classes on demand.
 *
 * Each refresh resolves only the current prefix, limits visible results, and adds a non-selectable hint
 * when more matches exist. PSI access is wrapped in a read action, and class lookup is split into a
 * short-name collection phase followed by resolution to avoid nested stub-index calls.
 */
internal class LoggerCompletionProvider(
    private val project: Project,
) : TextCompletionProvider {

    override fun getAdvertisement(): String? = null
    override fun acceptChar(c: Char): CharFilter.Result = CharFilter.Result.ADD_TO_PREFIX

    override fun applyPrefixMatcher(result: CompletionResultSet, prefix: String): CompletionResultSet =
        result
            .caseInsensitive()
            .withPrefixMatcher(PlainPrefixMatcher(prefix))

    override fun getPrefix(text: String, offset: Int): String = text.substring(0, offset.coerceIn(0, text.length))

    override fun fillCompletionVariants(
        parameters: CompletionParameters,
        prefix: String,
        result: CompletionResultSet,
    ) {
        if (prefix.length < MIN_PREFIX_LENGTH) return
        if (DumbService.isDumb(project)) return

        val scope = GlobalSearchScope.allScope(project)
        val idFilter = IdFilter.getProjectIdFilter(project, true)

        val shortNamePrefix = prefix.substringAfterLast('.', prefix)

        val matchOnShortName = !prefix.contains('.')

        val state = CompletionState()

        application.runReadAction {
            walkPackages(project, scope, prefix) { pkgFqn ->
                accept(
                    fqn = pkgFqn,
                    prefix = prefix,
                    matchOnShortName = matchOnShortName,
                    state = state,
                    iconSupplier = { AllIcons.Nodes.Package },
                )
            }

            if (state.overflowCapped) return@runReadAction

            if (shortNamePrefix.isEmpty()) return@runReadAction

            val shortNames = PsiShortNamesCache.getInstance(project)

            val matchingShortNames = mutableListOf<String>()
            val phase1Budget = (MAX_VISIBLE_SUGGESTIONS + OVERFLOW_PROBE_BUDGET) * 2
            shortNames.processAllClassNames({ shortName ->
                ProgressManager.checkCanceled()
                if (shortName.startsWith(shortNamePrefix, ignoreCase = true)) {
                    matchingShortNames += shortName
                    matchingShortNames.size < phase1Budget
                } else {
                    true
                }
            }, scope, idFilter)

            for (shortName in matchingShortNames) {
                ProgressManager.checkCanceled()
                if (!processClassesWithShortName(
                        shortNames = shortNames,
                        shortName = shortName,
                        scope = scope,
                        idFilter = idFilter,
                        prefix = prefix,
                        matchOnShortName = matchOnShortName,
                        state = state,
                    )
                ) {
                    break
                }
            }
        }

        result.addAllElements(state.matched)

        if (state.overflow > 0) {
            val hintResult = result.withPrefixMatcher(PlainPrefixMatcher(""))
            hintResult.addElement(buildMoreHintElement(state.overflow, state.overflowCapped))
        }
    }

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

    private fun buildElement(
        fqn: String,
        shortName: String?,
        matchOnShortName: Boolean,
        icon: Icon,
    ): LookupElement {
        val builder = LookupElementBuilder.create(fqn)
            .withIcon(icon)
        return if (matchOnShortName && shortName != null) builder.withLookupString(shortName)
        else builder
    }

    private fun accept(
        fqn: String,
        prefix: String,
        matchOnShortName: Boolean,
        state: CompletionState,
        shortName: String? = null,
        iconSupplier: () -> Icon,
    ): Boolean {
        val matches = fqn.startsWith(prefix, ignoreCase = true) ||
            (matchOnShortName && shortName != null && shortName.startsWith(prefix, ignoreCase = true))
        if (!matches) return true

        if (state.matched.size < MAX_VISIBLE_SUGGESTIONS) {
            state.matched += buildElement(fqn, shortName, matchOnShortName, iconSupplier())
            return true
        }

        state.overflow++
        if (state.overflow >= OVERFLOW_PROBE_BUDGET) {
            state.overflowCapped = true
            return false
        }
        return true
    }

    private fun processClassesWithShortName(
        shortNames: PsiShortNamesCache,
        shortName: String,
        scope: GlobalSearchScope,
        idFilter: IdFilter?,
        prefix: String,
        matchOnShortName: Boolean,
        state: CompletionState,
    ): Boolean = shortNames.processClassesWithName(shortName, { psiClass ->
        ProgressManager.checkCanceled()
        val fqn = psiClass.qualifiedName ?: return@processClassesWithName true
        accept(
            fqn = fqn,
            prefix = prefix,
            matchOnShortName = matchOnShortName,
            state = state,
            shortName = shortName,
            iconSupplier = { psiClass.safeIcon() },
        )
    }, scope, idFilter)

    private fun buildMoreHintElement(overflow: Int, capped: Boolean): LookupElement {
        val label = if (capped) "$overflow+" else "$overflow"
        val text = "… $label more match${if (overflow == 1 && !capped) "" else "es"} — refine search"
        val element = LookupElementBuilder.create(text)
            .withTypeText("hint", true)
            .withInsertHandler { ctx, _ ->
                ctx.document.deleteString(ctx.startOffset, ctx.tailOffset)
            }
        return PrioritizedLookupElement.withPriority(element, Double.NEGATIVE_INFINITY)
    }

    companion object {
        private const val MIN_PREFIX_LENGTH = 2
        private const val MAX_VISIBLE_SUGGESTIONS = 50
        private const val OVERFLOW_PROBE_BUDGET = 50
    }
}

private class CompletionState(
    val matched: MutableList<LookupElement> = mutableListOf(),
    var overflow: Int = 0,
    var overflowCapped: Boolean = false,
)

private fun PsiClass.safeIcon(): Icon = try {
    getIcon(Iconable.ICON_FLAG_VISIBILITY) ?: AllIcons.Nodes.Class
} catch (e: ProcessCanceledException) {
    throw e
} catch (_: Throwable) {
    AllIcons.Nodes.Class
}
