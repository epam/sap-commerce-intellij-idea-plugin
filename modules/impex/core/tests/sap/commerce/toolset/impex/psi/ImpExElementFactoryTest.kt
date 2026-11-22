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

package sap.commerce.toolset.impex.psi

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import sap.commerce.toolset.impex.ImpExLanguage
import sap.commerce.toolset.impex.ImpExParserDefinition
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ImpExElementFactoryTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()

        // plugin descriptors are not loaded for the module tests, the ImpEx language has to be registered manually
        LanguageParserDefinitions.INSTANCE.addExplicitExtension(ImpExLanguage, ImpExParserDefinition(), testRootDisposable)
    }

    fun `test physical file is created as a physical PSI file`() {
        val file = ImpExElementFactory.createPhysicalFile(project, IMPEX_CONTENT)

        assertTrue(file.isPhysical)
    }

    fun `test default file is created as a non-physical PSI file`() {
        val file = ImpExElementFactory.createFile(project, IMPEX_CONTENT)

        assertFalse(file.isPhysical)
    }

    fun `test physical file keeps the provided content`() {
        val file = ImpExElementFactory.createPhysicalFile(project, IMPEX_CONTENT)

        assertTrue(file.text == IMPEX_CONTENT)
    }

    fun `test problem can be anchored on a value of the physical file`() {
        val file = ImpExElementFactory.createPhysicalFile(project, IMPEX_CONTENT)
        val value = checkNotNull(PsiTreeUtil.findChildOfType(file, ImpExValue::class.java))

        val descriptor = InspectionManager.getInstance(project).createProblemDescriptor(
            value,
            "problem",
            true,
            LocalQuickFix.EMPTY_ARRAY,
            ProblemHighlightType.WEAK_WARNING,
        )

        assertTrue(descriptor.psiElement == value)
    }

    companion object {
        private const val IMPEX_CONTENT = "INSERT Product;code\n;abc"
    }
}
