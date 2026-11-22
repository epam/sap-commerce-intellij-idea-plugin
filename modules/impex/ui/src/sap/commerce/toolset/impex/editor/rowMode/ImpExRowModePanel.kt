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

package sap.commerce.toolset.impex.editor.rowMode

import com.intellij.icons.AllIcons
import com.intellij.ide.setToolTipText
import com.intellij.ide.util.PsiNavigationSupport
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadConstraint
import com.intellij.openapi.application.constrainedReadAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.text.HtmlChunk
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.components.ActionLink
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.RightGap
import com.intellij.ui.dsl.builder.actionButton
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.util.application
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.i18n
import sap.commerce.toolset.impex.psi.ImpExValueLine
import sap.commerce.toolset.typeSystem.meta.TSGlobalMetaModel
import sap.commerce.toolset.typeSystem.meta.TSMetaModelStateService
import sap.commerce.toolset.typeSystem.meta.TSModificationTracker
import sap.commerce.toolset.typeSystem.meta.event.TSMetaModelChangeListener
import sap.commerce.toolset.ui.header
import sap.commerce.toolset.ui.inlineBanner
import sap.commerce.toolset.ui.scrollPanel
import java.awt.BorderLayout
import java.awt.KeyboardFocusManager
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.LayoutFocusTraversalPolicy
import javax.swing.ScrollPaneConstants

/**
 * Displays the value line under the caret of the [editor] as a list of the fields, one per column of the header.
 *
 * The refreshes are requested from any thread, they are debounced and resolved in the background.
 * The displayed state and the components are accessed from the EDT only.
 *
 * @param onCollapse invoked by the collapse button of the header, which is displayed for any state of the panel
 */
class ImpExRowModePanel(
    private val project: Project,
    private val coroutineScope: CoroutineScope,
    private val editor: Editor,
    onCollapse: () -> Unit,
) : Disposable {

    val component = JPanel(BorderLayout())

    private val contentHolder = JPanel(BorderLayout())

    private val rowService = ImpExRowService.getInstance(project)

    private var displayedState: ImpExRowModeState? = null
    private var contentDisposable: Disposable? = null
    private var synchronizers = emptyList<ImpExRowCellSynchronizer>()
    private var fields = emptyList<ImpExRowValueField>()
    private var restoreFocusIndex: Int? = null

    private val refreshRequests = Channel<Long>(Channel.CONFLATED)
    private val forceRefresh = AtomicBoolean()
    private val refreshJob: Job

    @Volatile
    private var columnTypesCache: ColumnTypesCache? = null

    init {
        // the header is not a part of the content, so the panel can be collapsed regardless of the displayed state
        component.add(
            panel {
                header(i18n("hybris.impex.editor.row_mode.title")) {
                    actionButton(object : DumbAwareAction(
                        i18n("hybris.impex.editor.row_mode.collapse"),
                        i18n("hybris.impex.editor.row_mode.collapse.description"),
                        AllIcons.General.ChevronRight
                    ) {
                        override fun getActionUpdateThread() = ActionUpdateThread.EDT

                        override fun actionPerformed(e: AnActionEvent) = onCollapse()
                    }).align(AlignX.RIGHT)
                }
            },
            BorderLayout.NORTH
        )
        component.add(contentHolder, BorderLayout.CENTER)

        editor.caretModel.addCaretListener(object : CaretListener {
            override fun caretPositionChanged(event: CaretEvent) = requestRefresh()
            override fun caretAdded(event: CaretEvent) = requestRefresh()
            override fun caretRemoved(event: CaretEvent) = requestRefresh()
        }, this)

        editor.document.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) = requestRefresh()
        }, this)

        // the fields are created read-only or editable depending on the file, so they have to be created again when it changes
        project.messageBus.connect(this).subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
            override fun after(events: List<VFileEvent>) {
                val file = FileDocumentManager.getInstance().getFile(editor.document) ?: return

                if (events.any { it is VFilePropertyChangeEvent && it.file == file && it.propertyName == VirtualFile.PROP_WRITABLE }) requestRefresh(force = true)
            }
        })

        // the icons of the types are not available until the type system is calculated
        project.messageBus.connect(this).subscribe(TSMetaModelChangeListener.TOPIC, object : TSMetaModelChangeListener {
            override fun onChanged(newState: TSGlobalMetaModel) = requestRefresh(force = true)
        })

        refreshJob = coroutineScope.launch {
            refreshRequests.receiveAsFlow().collectLatest { delayMs ->
                delay(delayMs)
                refresh()
            }
        }

        requestRefresh(delayMs = 0)
    }

    override fun dispose() {
        refreshJob.cancel()
        refreshRequests.close()
    }

    /**
     * @param force the displayed state cannot be reused even though neither the document nor the row has been changed
     */
    private fun requestRefresh(delayMs: Long = REFRESH_DELAY_MS, force: Boolean = false) {
        if (force) forceRefresh.set(true)
        refreshRequests.trySend(delayMs)
    }

    private suspend fun refresh() {
        val force = forceRefresh.getAndSet(false)
        val snapshot = withContext(Dispatchers.EDT) { snapshot(force) } ?: return

        val state = constrainedReadAction(ReadConstraint.withDocumentsCommitted(project)) { resolveState(snapshot) }
            ?: return

        withContext(Dispatchers.EDT) {
            if (editor.isDisposed || Disposer.isDisposed(this@ImpExRowModePanel)) return@withContext
            // the ranges are stale if the document has been changed since they were resolved
            if (editor.document.modificationStamp != snapshot.modificationStamp) return@withContext requestRefresh()

            if (displayedState?.hasSameLayoutAs(state) == true && synchronizers.all { it.isConsistent } && isCoveringCells(state)) {
                // the layout is the same, so the fields stay, but the next refresh has to know the document they are in sync with
                displayedState = state
                return@withContext
            }

            render(state)
        }
    }

    // the value of a cell may grow outside of the part of the field, which is editable, once the text is added to its group in the source
    private fun isCoveringCells(state: ImpExRowModeState): Boolean = state !is ImpExRowModeState.Row
        || state.row.cells.size != synchronizers.size
        || state.row.cells.indices.all { synchronizers[it].isCovering(state.row.cells[it].range) }

    private fun snapshot(force: Boolean): Snapshot? {
        if (project.isDisposed || editor.isDisposed) return null

        return Snapshot(
            caretCount = editor.caretModel.caretCount,
            caretOffset = editor.caretModel.offset,
            modificationStamp = editor.document.modificationStamp,
            editable = !editor.isViewer && editor.document.isWritable,
            displayedState = displayedState.takeUnless { force },
        )
    }

    /**
     * @return `null` if the displayed state is still actual
     */
    private fun resolveState(snapshot: Snapshot): ImpExRowModeState? {
        if (snapshot.caretCount > 1) return ImpExRowModeState.MultipleCarets

        val valueLine = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
            ?.let { rowService.findValueLine(it, snapshot.caretOffset) }
            ?: return ImpExRowModeState.NoValueLine

        // the caret has been moved within the same row and nothing has been changed since, there is no need to resolve the row again
        val displayed = snapshot.displayedState
        if (displayed is ImpExRowModeState.Row
            && displayed.modificationStamp == snapshot.modificationStamp
            && displayed.row.range.startOffset == valueLine.textRange.startOffset
        ) return null

        val row = rowService.resolve(valueLine)
            ?: return ImpExRowModeState.NoHeader

        return ImpExRowModeState.Row(
            row = row,
            pointer = SmartPointerManager.createPointer(valueLine),
            editable = snapshot.editable,
            columnTypes = columnTypes(valueLine, row),
            modificationStamp = snapshot.modificationStamp,
        )
    }

    // the types depend on the header only, but they are resolved against the type system for each column
    private fun columnTypes(valueLine: ImpExValueLine, row: ImpExRow): List<ImpExRow.ColumnType> {
        val key = listOf(
            row.contextPrefix,
            TSMetaModelStateService.getInstance(project).ready(),
            TSModificationTracker.getInstance(project).modificationCount,
        )

        return columnTypesCache
            ?.takeIf { it.key == key }
            ?.types
            ?: rowService.resolveColumnTypes(valueLine)
                .also { columnTypesCache = ColumnTypesCache(key, it) }
    }

    private fun render(state: ImpExRowModeState) {
        // the fields are created again when the layout changes, e.g. the first modification of a cell of a missing group adds it to the line
        restoreFocusIndex = fields.indexOfFirst { it.editor?.contentComponent?.isFocusOwner == true }
            .takeIf { it >= 0 && state is ImpExRowModeState.Row }
        fields = emptyList()
        synchronizers = emptyList()

        contentDisposable?.let { Disposer.dispose(it) }
        val disposable = Disposer.newDisposable("ImpEx row mode content")
            .also {
                Disposer.register(this, it)
                contentDisposable = it
            }

        val content = when (state) {
            is ImpExRowModeState.Row -> rowPanel(state.row, state.pointer, state.editable, state.columnTypes, disposable)
            ImpExRowModeState.NoValueLine -> bannerPanel("hybris.impex.editor.row_mode.no_value_line")
            ImpExRowModeState.NoHeader -> bannerPanel("hybris.impex.editor.row_mode.no_header", EditorNotificationPanel.Status.Warning)
            ImpExRowModeState.MultipleCarets -> bannerPanel("hybris.impex.editor.row_mode.multiple_carets", EditorNotificationPanel.Status.Warning)
        }

        val hadFocus = component.isAncestorOf(KeyboardFocusManager.getCurrentKeyboardFocusManager().focusOwner)

        contentHolder.removeAll()
        contentHolder.add(
            scrollPanel(content, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)
                // keeps the focus within the panel, so Tab moves it from a field to the next one and not to the editor
                .apply {
                    focusTraversalPolicy = LayoutFocusTraversalPolicy()
                    isFocusCycleRoot = true
                },
            BorderLayout.CENTER
        )
        contentHolder.revalidate()
        contentHolder.repaint()

        // the fields are gone, so the focus would be lost together with the caret
        if (hadFocus && state !is ImpExRowModeState.Row) IdeFocusManager.getInstance(project).requestFocus(editor.contentComponent, true)

        // the fields are focusable once they are displayed
        restoreFocusIndex
            ?.let { fields.getOrNull(it) }
            ?.let { field -> application.invokeLater({ field.requestFocus() }, ModalityState.stateForComponent(component)) }

        displayedState = state
    }

    private fun bannerPanel(
        messageKey: String,
        status: EditorNotificationPanel.Status = EditorNotificationPanel.Status.Info
    ): JComponent = panel {
        inlineBanner("<p style='width: 100%'>${i18n(messageKey)}</p>", status)
    }.apply { border = JBUI.Borders.empty(16) }

    private fun rowPanel(
        rowModel: ImpExRow,
        pointer: SmartPsiElementPointer<ImpExValueLine>,
        editable: Boolean,
        columnTypes: List<ImpExRow.ColumnType>,
        disposable: Disposable,
    ): JComponent {
        val cellSynchronizers = rowModel.cells.map { cell ->
            ImpExRowCellSynchronizer(editor.document, rowModel.range, cell.range, disposable, rowModel.contextPrefix, pointer, cell.missingSeparators)
        }
        synchronizers = cellSynchronizers

        val cellFields = cellSynchronizers.mapIndexed { index, synchronizer ->
            ImpExRowValueField(project, synchronizer, editable, caretAtEnd = index == restoreFocusIndex, disposable)
        }
        fields = cellFields

        // the sections have their own gaps, so the separators between them take the whole width of the panel
        return panel {
            panel {
                row(i18n("hybris.impex.editor.row_mode.line")) {
                    label(rowModel.lineNumber(editor.document).toString())
                }
                row(i18n("hybris.impex.editor.row_mode.header_type")) {
                    link(rowModel.headerType) { navigate(pointer, rowService::headerTypeDeclaration) }
                }
                rowModel.subType?.let {
                    row(i18n("hybris.impex.editor.row_mode.sub_type")) {
                        link(it) { navigate(pointer, rowService::subTypeDeclaration) }
                    }
                }
            }.customize(UnscaledGaps(0, 16, 8, 16))

            separator()

            panel {
                rowModel.cells.forEachIndexed { index, rowCell ->
                    row {
                        // a document id is declared by the column itself, so there is nothing to navigate to
                        if (rowCell.documentIdDeclaration) {
                            label(rowCell.label)
                                .applyToComponent { if (rowCell.unique) markUnique() }
                        } else {
                            link(rowCell.label) { navigate(pointer) { rowService.columnDeclaration(it, index) } }
                                .applyToComponent {
                                    // Tab is for moving between the fields
                                    isFocusable = false
                                    if (rowCell.unique) markUnique()
                                }
                        }
                    }.bottomGap(BottomGap.NONE)

                    row {
                        columnTypes.getOrNull(index)?.let { type ->
                            icon(type.icon)
                                .applyToComponent { setToolTipText(type.name?.let(HtmlChunk::text)) }
                                .gap(RightGap.SMALL)
                        }

                        cell(cellFields[index])
                            .align(AlignX.FILL)
                            .resizableColumn()

                        actionButton(object : DumbAwareAction(
                            i18n("hybris.impex.editor.row_mode.jump_to_source"),
                            i18n("hybris.impex.editor.row_mode.jump_to_source.description"),
                            AllIcons.General.Locate
                        ) {
                            override fun getActionUpdateThread() = ActionUpdateThread.EDT

                            override fun actionPerformed(e: AnActionEvent) = jumpToSource(cellSynchronizers[index].sourceGroupOffset)
                        })
                    }
                }
            }.customize(UnscaledGaps(8, 16, 10, 16))
        }
    }

    private fun JLabel.markUnique() {
        icon = HybrisIcons.ImpEx.COLUMN_UNIQUE
        setToolTipText(HtmlChunk.text(i18n("hybris.impex.editor.row_mode.unique")))
    }

    private fun ActionLink.markUnique() {
        icon = HybrisIcons.ImpEx.COLUMN_UNIQUE
        setToolTipText(HtmlChunk.text(i18n("hybris.impex.editor.row_mode.unique")))
    }

    private fun jumpToSource(offset: Int) {
        if (editor.isDisposed) return

        editor.caretModel.moveToOffset(offset.coerceIn(0, editor.document.textLength))
        editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
        IdeFocusManager.getInstance(project).requestFocus(editor.contentComponent, true)
    }

    private fun navigate(pointer: SmartPsiElementPointer<ImpExValueLine>, declaration: (ImpExValueLine) -> PsiElement?) {
        coroutineScope.launch {
            val navigatable = readAction {
                pointer.element
                    ?.let(declaration)
                    ?.let { PsiNavigationSupport.getInstance().getDescriptor(it) }
            }
                ?: return@launch

            withContext(Dispatchers.EDT) {
                if (navigatable.canNavigate()) navigatable.navigate(true)
            }
        }
    }

    /**
     * The editor state is read on the EDT, then the row is resolved in the background.
     */
    private class Snapshot(
        val caretCount: Int,
        val caretOffset: Int,
        val modificationStamp: Long,
        val editable: Boolean,
        val displayedState: ImpExRowModeState?,
    )

    private class ColumnTypesCache(val key: List<Any>, val types: List<ImpExRow.ColumnType>)

    companion object {
        private const val REFRESH_DELAY_MS = 100L
    }
}
