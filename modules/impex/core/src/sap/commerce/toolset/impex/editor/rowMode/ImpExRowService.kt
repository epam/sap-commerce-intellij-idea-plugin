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

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.util.childrenOfType
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.i18n
import sap.commerce.toolset.impex.psi.ImpExFullHeaderParameter
import sap.commerce.toolset.impex.psi.ImpExMacroDeclaration
import sap.commerce.toolset.impex.psi.ImpExValueLine
import sap.commerce.toolset.typeSystem.TSConstants
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess
import sap.commerce.toolset.typeSystem.meta.TSMetaModelStateService
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaClassifier
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaItem
import sap.commerce.toolset.typeSystem.meta.model.TSMetaRelation
import sap.commerce.toolset.typeSystem.psi.reference.result.TSResolveResult

/**
 * The logic of the In-Editor Row Mode, which does not depend on the UI:
 * resolving of the value line, its columns and their declarations, and the context of the files, which a single cell is edited in.
 */
@Service(Service.Level.PROJECT)
class ImpExRowService(private val project: Project) {

    // ------------------------------------------
    //                  Row
    // ------------------------------------------

    fun resolve(valueLine: ImpExValueLine): ImpExRow? {
        val headerLine = valueLine.headerLine ?: return null
        val headerType = headerLine.fullHeaderType?.headerTypeName?.text ?: return null
        val subType = valueLine.subTypeName?.text

        // the lexer classifies macros based on the declarations above, so they have to be a part of the context,
        // the declarations are the direct children of the file, there is no need to walk the whole tree on each refresh
        val context = valueLine.containingFile.childrenOfType<ImpExMacroDeclaration>()
            .filter { it.textRange.endOffset <= valueLine.textRange.startOffset }
            .map { it.text }
            .plus(headerLine.text)
            .joinToString(separator = "\n", postfix = "\n")

        val groups = valueLine.valueGroupList
        // resolving of a header parameter requires the meta model, which cancels the read action while it is being calculated
        val typeSystemReady = TSMetaModelStateService.getInstance(project).ready()

        return ImpExRow(
            range = valueLine.textRange,
            headerType = headerType,
            subType = subType,
            contextPrefix = context,
            groupCount = groups.size,
            // a cell per column of the header, a group of the value line may be missing, the groups without a column are not edited
            cells = headerLine.fullHeaderParameterList.mapIndexed { index, parameter ->
                val group = groups.getOrNull(index)

                ImpExRow.Cell(
                    label = parameter.anyHeaderParameterName.text.trim().ifEmpty { "#${index + 1}" },
                    // the group may be missing, then the cell is placed at the end of the line
                    range = group?.valueRange ?: TextRange.from(valueLine.textRange.endOffset, 0),
                    unique = typeSystemReady && parameter.isUnique,
                    documentIdDeclaration = parameter.isDocumentIdDeclaration,
                    missingSeparators = if (group == null) index - groups.size + 1 else 0,
                )
            }
        )
    }

    // ------------------------------------------
    //                Type System
    // ------------------------------------------

    /**
     * The type of each column of the header: a link for a document id declaration, otherwise the type of the attribute.
     * It is unknown, if the type system is not ready or the attribute cannot be resolved.
     */
    fun resolveColumnTypes(valueLine: ImpExValueLine): List<ImpExRow.ColumnType> {
        val access = TSMetaModelAccess.getInstance(project)
            .takeIf { TSMetaModelStateService.getInstance(project).ready() }

        return valueLine.headerLine?.fullHeaderParameterList.orEmpty().map { parameter ->
            if (parameter.isDocumentIdDeclaration) {
                ImpExRow.ColumnType(HybrisIcons.ImpEx.DOC_ID_LINK, i18n("hybris.impex.editor.row_mode.type.document_id"))
            } else {
                access?.let { resolveAttributeType(it, parameter) }
                    ?: ImpExRow.ColumnType(HybrisIcons.ImpEx.COLUMN_TYPE_UNKNOWN, null)
            }
        }
    }

    private fun resolveAttributeType(access: TSMetaModelAccess, parameter: ImpExFullHeaderParameter): ImpExRow.ColumnType? {
        // the same resolution as for the navigation to the declaration of the attribute
        val meta = parameter
            .anyHeaderParameterName
            .references
            .asSequence()
            .filterIsInstance<PsiPolyVariantReference>()
            .flatMap { it.multiResolve(false).asSequence() }
            .filterIsInstance<TSResolveResult<*>>()
            .map { it.meta }
            .firstOrNull()
            ?: return null

        val typeName = when (meta) {
            is TSGlobalMetaItem.TSGlobalMetaItemAttribute -> meta.type
            is TSMetaRelation.TSMetaRelationElement -> meta.type
            else -> null
        }
        val attributeIcon = when (meta) {
            is TSGlobalMetaClassifier<*> -> meta.icon
            is TSMetaRelation.TSMetaRelationElement -> meta.end.icon
            else -> null
        }
        // the icon of the type if it is known, otherwise the icon of the attribute itself (regular, localized, dynamic)
        val icon = typeName
            ?.removePrefix(TSConstants.Attribute.LOCALIZED_PREFIX)
            ?.let { access.findMetaClassifierByName(it) }
            ?.icon
            ?: attributeIcon
            ?: return null

        return ImpExRow.ColumnType(icon, typeName)
    }

    // ------------------------------------------
    //                Navigation
    // ------------------------------------------

    // the same declarations as the navigation from the corresponding elements of the ImpEx file leads to

    fun headerTypeDeclaration(valueLine: ImpExValueLine): PsiElement? = declarationOf(valueLine.headerLine?.fullHeaderType?.headerTypeName)

    fun subTypeDeclaration(valueLine: ImpExValueLine): PsiElement? = declarationOf(valueLine.subTypeName)

    fun columnDeclaration(valueLine: ImpExValueLine, columnIndex: Int): PsiElement? = declarationOf(
        valueLine.headerLine?.fullHeaderParameterList?.getOrNull(columnIndex)?.anyHeaderParameterName
    )

    private fun declarationOf(element: PsiElement?): PsiElement? = element
        ?.references
        ?.asSequence()
        ?.flatMap { reference ->
            if (reference is PsiPolyVariantReference) reference.multiResolve(false).asSequence().mapNotNull { it.element }
            else listOfNotNull(reference.resolve()).asSequence()
        }
        ?.firstOrNull()

    // ------------------------------------------
    //   Context of the files of the single cells
    // ------------------------------------------

    // A cell of the row mode is edited in a standalone ImpEx file, which contains only the header and the value line.
    // Everything that depends on the rest of the original file (macros, document ids) has to be resolved against the original one.

    fun attachContext(file: VirtualFile, valueLine: SmartPsiElementPointer<ImpExValueLine>) {
        file.putUserData(KEY_VALUE_LINE, valueLine)
    }

    fun isCellFile(file: VirtualFile): Boolean = file.getUserData(KEY_VALUE_LINE) != null
            || file.getUserData(HybrisConstants.Keys.ORIGINAL_FILE) != null

    fun valueLineOf(element: PsiElement): ImpExValueLine? = element.containingFile
        // a completion works on a copy of the file
        ?.originalFile
        ?.viewProvider
        ?.virtualFile
        ?.getUserData(KEY_VALUE_LINE)
        ?.element

    companion object {
        private val KEY_VALUE_LINE = Key.create<SmartPsiElementPointer<ImpExValueLine>>("impex.row_mode.context.value_line")

        fun getInstance(project: Project): ImpExRowService = project.service()
    }
}
