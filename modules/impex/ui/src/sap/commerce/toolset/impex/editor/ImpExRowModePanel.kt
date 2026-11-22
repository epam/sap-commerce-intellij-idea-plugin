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

package sap.commerce.toolset.impex.editor

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ReadConstraint
import com.intellij.openapi.application.constrainedReadAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.actionSystem.EditorActionManager
import com.intellij.openapi.editor.actionSystem.ReadonlyFragmentModificationHandler
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.event.SelectionEvent
import com.intellij.openapi.editor.event.SelectionListener
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
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
import com.intellij.ide.util.PsiNavigationSupport
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.RightGap
import com.intellij.ui.dsl.builder.TopGap
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.ui.dsl.builder.actionButton
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.i18n
import sap.commerce.toolset.impex.file.ImpExFileType
import sap.commerce.toolset.impex.psi.ImpExFile
import sap.commerce.toolset.impex.psi.ImpExValueLine
import sap.commerce.toolset.typeSystem.meta.event.TSMetaModelChangeListener
import sap.commerce.toolset.typeSystem.meta.TSGlobalMetaModel
import sap.commerce.toolset.ui.inlineBanner
import sap.commerce.toolset.ui.scrollPanel
import java.awt.BorderLayout
import java.awt.KeyboardFocusManager
import java.io.Serial
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.LayoutFocusTraversalPolicy
import javax.swing.ScrollPaneConstants
import javax.swing.SwingUtilities

class ImpExRowModePanel(
    private val project: Project,
    private val coroutineScope: CoroutineScope,
    private val editor: Editor,
) : Disposable {

    val component = JPanel(BorderLayout())

    private var refreshJob: Job? = null
    private var contentDisposable: Disposable? = null
    private var displayedState: State? = null
    private var synchronizers = emptyList<ImpExRowCellSynchronizer>()
    private var fields = emptyList<EditorTextField>()
    private var restoreFocusIndex: Int? = null

    init {
        editor.caretModel.addCaretListener(object : CaretListener {
            override fun caretPositionChanged(event: CaretEvent) = scheduleRefresh()
            override fun caretAdded(event: CaretEvent) = scheduleRefresh()
            override fun caretRemoved(event: CaretEvent) = scheduleRefresh()
        }, this)

        editor.document.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) = scheduleRefresh()
        }, this)

        // the fields are created read-only or editable depending on the file, so they have to be created again when it changes
        project.messageBus.connect(this).subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
            override fun after(events: List<VFileEvent>) {
                val file = FileDocumentManager.getInstance().getFile(editor.document) ?: return

                if (events.any { it is VFilePropertyChangeEvent && it.file == file && it.propertyName == VirtualFile.PROP_WRITABLE }) scheduleRefresh()
            }
        })

        // the icons of the types are not available until the type system is calculated
        project.messageBus.connect(this).subscribe(TSMetaModelChangeListener.TOPIC, object : TSMetaModelChangeListener {
            override fun onChanged(newState: TSGlobalMetaModel) = scheduleRefresh()
        })

        scheduleRefresh(0)
    }

    override fun dispose() {
        refreshJob?.cancel()
    }

    private fun scheduleRefresh(delayMs: Long = REFRESH_DELAY_MS) {
        refreshJob?.cancel()
        refreshJob = coroutineScope.launch {
            delay(delayMs)

            if (project.isDisposed || editor.isDisposed) return@launch

            val (state, modificationStamp) = constrainedReadAction(ReadConstraint.withDocumentsCommitted(project)) {
                resolveState() to editor.document.modificationStamp
            }

            withContext(Dispatchers.EDT) {
                if (editor.isDisposed || Disposer.isDisposed(this@ImpExRowModePanel)) return@withContext
                // the ranges are stale if the document has been changed since they were resolved
                if (editor.document.modificationStamp != modificationStamp) return@withContext scheduleRefresh()
                if (displayedState?.hasSameLayoutAs(state) == true && synchronizers.all { it.isConsistent }) return@withContext

                render(state)
            }
        }
    }

    private fun isEditable() = !editor.isViewer && ImpExRowEditability.isEditable(editor.document, project)

    private fun resolveState(): State {
        if (editor.caretModel.caretCount > 1) return State.MultipleCarets

        val file = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
            ?.takeIf { it is ImpExFile }
            ?: return State.NoValueLine
        val valueLine = ImpExRowResolver.findValueLine(file, editor.caretModel.offset)
            ?: return State.NoValueLine

        return ImpExRowResolver.resolve(valueLine)
            ?.let { State.Row(it, SmartPointerManager.createPointer(valueLine), isEditable(), ImpExRowColumnTypes.resolve(valueLine)) }
            ?: State.NoHeader
    }

    private fun render(state: State) {
        // the fields are created again when the layout changes, e.g. the first modification of a cell of a missing group adds it to the line
        restoreFocusIndex = fields.indexOfFirst { it.editor?.contentComponent?.isFocusOwner == true }
            .takeIf { it >= 0 && state is State.Row }
        fields = emptyList()
        synchronizers = emptyList()

        contentDisposable?.let { Disposer.dispose(it) }
        val disposable = Disposer.newDisposable("ImpEx row mode content")
            .also {
                Disposer.register(this, it)
                contentDisposable = it
            }

        val content = when (state) {
            is State.Row -> rowPanel(state.row, state.pointer, state.editable, state.columnTypes, disposable)
            State.NoValueLine -> bannerPanel("hybris.impex.editor.row_mode.no_value_line")
            State.NoHeader -> bannerPanel("hybris.impex.editor.row_mode.no_header", EditorNotificationPanel.Status.Warning)
            State.MultipleCarets -> bannerPanel("hybris.impex.editor.row_mode.multiple_carets", EditorNotificationPanel.Status.Warning)
        }

        val hadFocus = component.isAncestorOf(KeyboardFocusManager.getCurrentKeyboardFocusManager().focusOwner)

        component.removeAll()
        component.add(
            scrollPanel(content, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)
                // keeps the focus within the panel, so Tab moves it from a field to the next one and not to the editor
                .apply {
                    focusTraversalPolicy = LayoutFocusTraversalPolicy()
                    isFocusCycleRoot = true
                },
            BorderLayout.CENTER
        )
        component.revalidate()
        component.repaint()

        // the fields are gone, so the focus would be lost together with the caret
        if (hadFocus && state !is State.Row) IdeFocusManager.getInstance(project).requestFocus(editor.contentComponent, true)

        restoreFocusIndex
            ?.let { fields.getOrNull(it) }
            ?.let { field -> SwingUtilities.invokeLater { field.requestFocus() } }

        displayedState = state
    }

    private fun bannerPanel(
        messageKey: String,
        status: EditorNotificationPanel.Status = EditorNotificationPanel.Status.Info
    ): JComponent = panel {
        inlineBanner("<p style='width: 100%'>${i18n(messageKey)}</p>", status)
    }.apply { border = JBUI.Borders.empty(16) }

    private fun rowPanel(
        valueLine: ImpExRow,
        pointer: SmartPsiElementPointer<ImpExValueLine>,
        editable: Boolean,
        columnTypes: List<ImpExRowColumnType>,
        disposable: Disposable,
    ): JComponent {
        val cellSynchronizers = valueLine.cells.map { cell ->
            ImpExRowCellSynchronizer(editor.document, valueLine.range, cell.range, disposable, valueLine.contextPrefix, pointer, cell.missingSeparators)
        }
        synchronizers = cellSynchronizers

        val cellFields = mutableListOf<EditorTextField>()
        fields = cellFields

        // the sections have their own gaps, so the separators between them take the whole width of the panel
        return panel {
            panel {
                row {
                    label(i18n("hybris.impex.editor.row_mode.title")).bold()
                }.topGap(TopGap.SMALL)
            }.customize(UnscaledGaps(0, 16, 0, 16))

            separator(JBUI.CurrentTheme.Banner.INFO_BORDER_COLOR)

            panel {
                row(i18n("hybris.impex.editor.row_mode.line")) {
                    label(valueLine.lineNumber(editor.document).toString())
                }
                row(i18n("hybris.impex.editor.row_mode.header_type")) {
                    link(valueLine.headerType) { navigate(pointer, ImpExRowNavigation::headerTypeDeclaration) }
                }
                valueLine.subType?.let {
                    row(i18n("hybris.impex.editor.row_mode.sub_type")) {
                        link(it) { navigate(pointer, ImpExRowNavigation::subTypeDeclaration) }
                    }
                }
            }.customize(UnscaledGaps(0, 16, 8, 16))

            separator()

            panel {
                valueLine.cells.forEachIndexed { index, rowCell ->
                    row {
                        // a document id is declared by the column itself, so there is nothing to navigate to
                        if (rowCell.documentIdDeclaration) {
                            label(rowCell.label)
                                .applyToComponent {
                                    if (rowCell.unique) {
                                        icon = HybrisIcons.ImpEx.COLUMN_UNIQUE
                                        toolTipText = i18n("hybris.impex.editor.row_mode.unique")
                                    }
                                }
                        } else {
                            link(rowCell.label) { navigate(pointer) { ImpExRowNavigation.columnDeclaration(it, index) } }
                                .applyToComponent {
                                    // Tab is for moving between the fields
                                    isFocusable = false
                                    if (rowCell.unique) {
                                        icon = HybrisIcons.ImpEx.COLUMN_UNIQUE
                                        toolTipText = i18n("hybris.impex.editor.row_mode.unique")
                                    }
                                }
                        }
                    }.bottomGap(BottomGap.NONE)

                    row {
                        columnTypes.getOrNull(index)?.let { type ->
                            icon(type.icon)
                                .applyToComponent { toolTipText = type.name }
                                .gap(RightGap.SMALL)
                        }

                        cell(valueField(cellSynchronizers[index], editable, disposable, caretAtEnd = index == restoreFocusIndex).also { cellFields.add(it) })
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

    private fun valueField(synchronizer: ImpExRowCellSynchronizer, editable: Boolean, disposable: Disposable, caretAtEnd: Boolean): EditorTextField {
        // the locked part is hidden, so there is nothing to explain when an attempt to modify it is made
        EditorActionManager.getInstance().setReadonlyFragmentModificationHandler(synchronizer.mirror, ReadonlyFragmentModificationHandler { })

        return object : EditorTextField(synchronizer.mirror, project, ImpExFileType, !editable, true) {
            override fun createEditor(): EditorEx = super.createEditor().apply {
                settings.isAllowSingleLogicalLineFolding = true

                // only the cell is displayed, the rest of the file provides the context for the ImpEx language features
                val editable = synchronizer.editableRange
                foldingModel.runBatchFoldingOperation {
                    if (editable.startOffset > 0) foldingModel.addFoldRegion(0, editable.startOffset, "")?.isExpanded = false
                    if (editable.endOffset < document.textLength) foldingModel.addFoldRegion(editable.endOffset, document.textLength, "")?.isExpanded = false
                }
                caretModel.moveToOffset(if (caretAtEnd) editable.endOffset else editable.startOffset)

                caretModel.addCaretListener(object : CaretListener {
                    override fun caretPositionChanged(event: CaretEvent) {
                        val range = synchronizer.editableRange
                        val caret = event.caret ?: return
                        val offset = caret.offset.coerceIn(range.startOffset, range.endOffset)

                        if (offset != caret.offset) caret.moveToOffset(offset)
                    }
                })
                selectionModel.addSelectionListener(object : SelectionListener {
                    override fun selectionChanged(e: SelectionEvent) {
                        val range = synchronizer.editableRange
                        if (!selectionModel.hasSelection()) return
                        if (selectionModel.selectionStart >= range.startOffset && selectionModel.selectionEnd <= range.endOffset) return

                        selectionModel.setSelection(
                            selectionModel.selectionStart.coerceIn(range.startOffset, range.endOffset),
                            selectionModel.selectionEnd.coerceIn(range.startOffset, range.endOffset),
                        )
                    }
                })
            }

            @Serial
            private val serialVersionUID: Long = 2985102871954601724L
        }.apply { setDisposedWith(disposable) }
    }

    private sealed interface State {
        data object NoValueLine : State
        data object NoHeader : State
        data object MultipleCarets : State
        data class Row(
            val row: ImpExRow,
            val pointer: SmartPsiElementPointer<ImpExValueLine>,
            val editable: Boolean,
            val columnTypes: List<ImpExRowColumnType>,
        ) : State

        fun hasSameLayoutAs(other: State) = if (this is Row && other is Row) {
            editable == other.editable
                && columnTypes.map { it.name } == other.columnTypes.map { it.name }
                && row.hasSameLayoutAs(other.row)
        } else this == other
    }

    companion object {
        private const val REFRESH_DELAY_MS = 100L
    }
}
