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

package sap.commerce.toolset.logging.ui

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PlainPrefixMatcher
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.CharFilter
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.ide.projectView.ProjectView
import com.intellij.ide.util.PsiNavigationSupport
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Iconable
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiPackage
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache
import com.intellij.psi.util.startOffset
import com.intellij.ui.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.builder.Cell
import com.intellij.util.indexing.IdFilter
import com.intellij.util.textCompletion.TextCompletionProvider
import com.intellij.util.textCompletion.TextFieldWithCompletion
import kotlinx.coroutines.*
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.logging.CxLogLevel
import sap.commerce.toolset.logging.presentation.CxLoggerPresentation
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.util.concurrent.ConcurrentHashMap
import javax.swing.JComponent
import com.intellij.openapi.editor.event.DocumentEvent as EditorDocumentEvent
import com.intellij.openapi.editor.event.DocumentListener as EditorDocumentListener

data class LazyLoggerRow(
    val cxLogger: CxLoggerPresentation,
    val placeholderIcon: Placeholder,
    val placeholderHelp: Placeholder,
    val placeholderName: Placeholder,
)

/**
 * Holds per-row visibility state used for client-side filtering of the rendered
 * logger list. Each entry maps a logger name to the [AtomicBooleanProperty] that
 * the row was built with via `row { ... }.visibleIf(prop)`. Flipping the property
 * hides or reveals the row without rebuilding the panel, so scroll position,
 * lazy PSI resolution, and validation state are preserved.
 *
 * [hasMatches] is set to `true` whenever at least one row matches the current
 * filter; [showNoMatches] is its inverse, convenient for binding to a
 * "no results" row via `.visibleIf(showNoMatches)` without pulling in the
 * observable-negation extension.
 *
 * Thread-safe without external locking: the backing map is a [ConcurrentHashMap]
 * (weakly consistent iteration, never throws CME) and the boolean properties
 * are themselves atomic. Writes happen on the background render coroutine
 * while [apply] can be invoked concurrently from the EDT document listener.
 */
internal class LoggerFilterState {
    private val rowVisibility = ConcurrentHashMap<String, AtomicBooleanProperty>()

    val hasMatches: AtomicBooleanProperty = AtomicBooleanProperty(true)
    val showNoMatches: AtomicBooleanProperty = AtomicBooleanProperty(false)

    /**
     * Register a row's visibility toggle under the given logger name. Called
     * while building the panel on the background coroutine.
     */
    fun track(loggerName: String, visibility: AtomicBooleanProperty) {
        rowVisibility[loggerName] = visibility
    }

    fun clear() {
        rowVisibility.clear()
        hasMatches.set(true)
        showNoMatches.set(false)
    }

    fun apply(filterText: String) {
        val needle = filterText.trim()

        if (needle.isEmpty()) {
            rowVisibility.values.forEach { it.set(true) }
            hasMatches.set(rowVisibility.isNotEmpty())
            // Empty filter never shows the "no matches" banner; the outer
            // view already handles the "no loggers at all" case.
            showNoMatches.set(false)
            return
        }

        var anyMatch = false
        // ConcurrentHashMap's iteration is weakly consistent: safe against
        // concurrent puts/clears without CME. Entries added mid-iteration may
        // or may not be seen — that's fine, they start visible=true and the
        // next apply() call from render() or another keystroke will catch up.
        rowVisibility.forEach { (name, visible) ->
            val matches = name.contains(needle, ignoreCase = true)
            if (matches) anyMatch = true
            visible.set(matches)
        }
        hasMatches.set(anyMatch)
        showNoMatches.set(rowVisibility.isNotEmpty() && !anyMatch)
    }
}

internal fun Row.loggerDetailsPlaceholders(cxLogger: CxLoggerPresentation): LazyLoggerRow {
    val placeholderIcon = placeholder().gap(RightGap.SMALL)
    val placeholderHelp = placeholder()
    val placeholderName = placeholder().resizableColumn().gap(RightGap.SMALL)

    placeholderIcon.component = icon(AnimatedIcon.Default.INSTANCE).component
    placeholderName.component = label(cxLogger.name)
        .align(AlignX.FILL)
        .gap(RightGap.SMALL)
        .resizableColumn()
        .component

    return LazyLoggerRow(cxLogger, placeholderIcon, placeholderHelp, placeholderName)
}

internal suspend fun lazyLoggerDetails(
    project: Project, coroutineScope: CoroutineScope, lazyLoggerRow: LazyLoggerRow,
) {
    coroutineScope.ensureActive()
    val cxLogger = lazyLoggerRow.cxLogger

    coroutineScope.launch {
        val psiElement = smartReadAction(project) {
            val javaPsiFacade = JavaPsiFacade.getInstance(project)

            javaPsiFacade.findPackage(cxLogger.name)
                ?: javaPsiFacade.findClass(cxLogger.name, GlobalSearchScope.allScope(project))
        }

        val icon = smartReadAction(project) {
            psiElement?.getIcon(Iconable.ICON_FLAG_VISIBILITY or Iconable.ICON_FLAG_READ_STATUS)
        } ?: HybrisIcons.Log.Identifier.NA

        lateinit var iconComponent: JComponent
        lateinit var navigableComponent: JComponent
        var placeholderHelp: JComponent? = null

        val pointer = if (psiElement != null) smartReadAction(project) {
            SmartPointerManager.getInstance(project).createSmartPsiElementPointer(psiElement)
        } else null


        panel {
            row {
                cxLogger.presentableParent
                    ?.let { placeholderHelp = contextHelp(it).component }

                iconComponent = icon(icon).component

                if (pointer != null) {
                    navigableComponent = link(cxLogger.name) {
                        when (val resolvedPsiElement = pointer.element) {
                            is PsiPackage -> {
                                CoroutineScope(Dispatchers.Default).launch {
                                    val directory = smartReadAction(project) {
                                        resolvedPsiElement.getDirectories(GlobalSearchScope.allScope(project))
                                            .firstOrNull()
                                    } ?: return@launch

                                    edtWriteAction {
                                        ProjectView.getInstance(project).selectPsiElement(directory, true)
                                    }
                                }
                            }

                            is PsiClass -> PsiNavigationSupport.getInstance()
                                .createNavigatable(project, resolvedPsiElement.containingFile.virtualFile, resolvedPsiElement.startOffset)
                                .navigate(true)

                            else -> Notifications.warning(
                                "Logger declaration could not be found",
                                "Unable to locate logger declaration for name: ${cxLogger.name}.",
                            )
                                .hideAfter(10)
                                .notify(project)
                        }
                    }.component
                } else {
                    navigableComponent = label(cxLogger.name).component
                }
            }
        }

        withContext(Dispatchers.EDT) {
            ensureActive()
            lazyLoggerRow.placeholderIcon.component = iconComponent
            lazyLoggerRow.placeholderName.component = navigableComponent

            if (placeholderHelp != null) lazyLoggerRow.placeholderHelp.component = placeholderHelp
        }
    }
}

internal fun Row.logLevelComboBox(): Cell<ComboBox<CxLogLevel>> = comboBox(
    model = EnumComboBoxModel(CxLogLevel::class.java),
    renderer = SimpleListCellRenderer.create { label, value, _ ->
        if (value != null) {
            label.icon = value.icon
            label.text = value.name
        }
    }
)

/**
 * Text field that serves a dual purpose: the user can type a logger name and
 * press Enter (or click the associated Apply Logger button) to add it, and as
 * they type the rendered logger list below is filtered by a case-insensitive
 * substring match via [onFilterChanged].
 *
 * The field is a [TextFieldWithCompletion] wired to a [LoggerCompletionProvider]
 * that queries [PsiShortNamesCache] on demand for each popup refresh. There is
 * no upfront enumeration — resolution happens lazily, prefix-filtered via the
 * index, and stops as soon as [MAX_VISIBLE_SUGGESTIONS] matches are collected.
 * Opening the panel is free; typing is bounded by the cap, not by project size.
 *
 * Suggestions only appear after the user has typed at least
 * [MIN_PREFIX_LENGTH] characters. When more matches exist than the cap, a
 * single non-selectable hint row (`… N more matches — refine search`) is
 * appended to prompt the user to narrow their prefix. True Search-Everywhere
 * style pagination isn't feasible via the lookup API — see the comment on
 * [LoggerCompletionProvider] for the rationale.
 *
 * The blank-text validation only fires on apply (via `validateAll()` in the
 * caller's apply handler), so it never flashes a red ring during filtering.
 *
 * Enter-to-apply is wired through a key listener on the editor's content
 * component and is intentionally suppressed while the completion lookup is
 * showing, so pressing Enter in the popup picks the highlighted suggestion
 * instead of applying a partial string.
 */
internal fun Row.newLoggerTextField(
    project: Project,
    parentDisposable: Disposable,
    onFilterChanged: (String) -> Unit = {},
    apply: () -> Unit,
): Cell<TextFieldWithCompletion> {
    // Subclass to hook editor creation — this is the pattern the platform
    // itself uses when a LanguageTextField needs per-instance editor tweaks.
    // It gives us a reliable place to attach the Enter key listener once the
    // editor actually exists, without racing against addNotify()/focus order.
    val field = object : TextFieldWithCompletion(
        project,
        LoggerCompletionProvider(project),
        /* value = */ "",
        /* oneLineMode = */ true,
        /* autoPopup = */ true,
        /* forceAutoPopup = */ false,
        /* showHint = */ true,
    ) {
        override fun createEditor(): EditorEx {
            val editor = super.createEditor()
            // Enter-to-apply. Suppressed while the completion lookup is
            // visible so Enter there picks the highlighted suggestion
            // instead of applying a partial string.
            editor.contentComponent.addKeyListener(object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent) {
                    if (e.keyCode != KeyEvent.VK_ENTER) return
                    if (e.isShiftDown || e.isControlDown || e.isAltDown || e.isMetaDown) return
                    if (LookupManager.getActiveLookup(editor) != null) return

                    e.consume()
                    apply()
                }
            })
            return editor
        }
    }

    // Document listener on the editor document drives the row-filter. Fires
    // on every keystroke; filter evaluation is O(n) over the current logger
    // list and only flips AtomicBooleanProperty values, so it's cheap.
    val docListener = object : EditorDocumentListener {
        override fun documentChanged(event: EditorDocumentEvent) {
            onFilterChanged(field.text)
        }
    }
    field.document.addDocumentListener(docListener)
    Disposer.register(parentDisposable) { field.document.removeDocumentListener(docListener) }

    return cell(field)
        .resizableColumn()
        .align(AlignX.FILL)
        .validationOnApply {
            if (it.text.isBlank()) error("Please enter a logger name")
            else null
        }
}

/**
 * Minimum prefix length before suggestions start appearing. One character is
 * enough to bound the result set meaningfully via the cap; shorter than that
 * is just "every symbol in the project" which isn't useful.
 */
private const val MIN_PREFIX_LENGTH = 2

/**
 * Maximum number of real suggestions shown in the completion popup. Past this,
 * a single non-selectable hint row (`… N+ more matches — refine search`) is
 * appended so the popup stays readable on projects with thousands of classes.
 */
private const val MAX_VISIBLE_SUGGESTIONS = 50

/**
 * After the cap is reached we keep counting a little further to produce an
 * honest "N more" number rather than just "many more"; once this budget is
 * exhausted we show "N+" and stop, so a pathological prefix that matches
 * everything still returns promptly.
 */
private const val OVERFLOW_PROBE_BUDGET = 50

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
 * therefore be wrapped in [Application.runReadAction] explicitly.
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
private class LoggerCompletionProvider(
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

        fun buildElement(fqn: String, shortName: String?): LookupElement {
            val builder = LookupElementBuilder.create(fqn)
            return if (matchOnShortName && shortName != null) builder.withLookupString(shortName)
            else builder
        }

        fun accept(fqn: String, shortName: String? = null): Boolean {
            // Matching is done with plain case-insensitive startsWith —
            // same semantics as the PlainPrefixMatcher we set in
            // applyPrefixMatcher with caseInsensitive(), but evaluated here
            // so we can cheaply filter before allocating a LookupElement.
            val matches = fqn.startsWith(prefix, ignoreCase = true) ||
                (matchOnShortName && shortName != null && shortName.startsWith(prefix, ignoreCase = true))
            if (!matches) return true

            if (matched.size < MAX_VISIBLE_SUGGESTIONS) {
                matched += buildElement(fqn, shortName)
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
            walkPackages(project, scope, prefix) { pkgFqn -> accept(pkgFqn) }

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
            outer@ for (shortName in matchingShortNames) {
                ProgressManager.checkCanceled()
                var keepGoing = true
                shortNames.processClassesWithName(shortName, { psiClass ->
                    ProgressManager.checkCanceled()
                    val fqn = psiClass.qualifiedName
                    if (fqn != null) keepGoing = accept(fqn, shortName)
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
}

internal fun noLoggersView(
    messageText: String,
    status: EditorNotificationPanel.Status = EditorNotificationPanel.Status.Warning
) = panel {
    row {
        cell(
            InlineBanner(
                messageText,
                status
            )
                .showCloseButton(false)
        )
            .align(Align.CENTER)
            .resizableColumn()
    }.resizableRow()
}

internal fun Row.cellNoData(property: AtomicBooleanProperty, text: String) = text(text)
    .visibleIf(property)
    .align(Align.CENTER)
    .resizableColumn()