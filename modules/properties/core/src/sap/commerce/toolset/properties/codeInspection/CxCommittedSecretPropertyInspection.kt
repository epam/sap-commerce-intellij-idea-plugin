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

package sap.commerce.toolset.properties.codeInspection

import com.intellij.lang.properties.IProperty
import com.intellij.psi.PsiFile
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.properties.codeInspection.fix.CxClearPropertyValueFix
import sap.commerce.toolset.properties.codeInspection.fix.CxMovePropertyToLocalPropertiesFix
import sap.commerce.toolset.properties.meta.CxPropertyRule
import sap.commerce.toolset.properties.meta.CxSecretKind
import sap.commerce.toolset.properties.meta.CxSecretValue

/**
 * Reports a secret written into a file which is part of the build.
 *
 * A `project.properties` is committed, shipped in every build and readable by everyone with access to the repository.
 * A password written there is not configuration, it is a disclosure - and deleting the line afterwards does not undo
 * it, because the value stays in the history. Only rotating the secret does.
 *
 * `local.properties` is deliberately not inspected: it is where the other inspections send these values, and saying
 * both "put it here" and "do not put it here" would be no guidance at all.
 *
 * Keys the [CxPropertyRule] catalogue already covers are left to the inspection which owns them, so one line is never
 * reported twice for the same reason.
 */
class CxCommittedSecretPropertyInspection : CxPropertyDeclarationInspection<Unit>() {

    override fun prepare(file: PsiFile) = Unit.takeIf { file.name == ProjectConstants.File.PROJECT_PROPERTIES }

    override fun problemOf(property: IProperty, file: PsiFile, context: Unit): CxPropertyProblem? {
        val key = property.key ?: return null
        if (CxPropertyRule.of(key) != null) return null

        val kind = CxSecretKind.of(key) ?: return null
        val value = property.value ?: return null
        if (!CxSecretValue.isSecret(value)) return null

        return CxPropertyProblem(
            message = "'$key' looks like ${kind.title} and ${ProjectConstants.File.PROJECT_PROPERTIES} is part of " +
                "every build - move it out and rotate it, the committed value stays in the repository history",
            fixes = listOfNotNull(
                CxMovePropertyToLocalPropertiesFix().takeIf { CxMovePropertyToLocalPropertiesFix.isAvailable(file.project) },
                CxClearPropertyValueFix(),
            ),
        )
    }
}
