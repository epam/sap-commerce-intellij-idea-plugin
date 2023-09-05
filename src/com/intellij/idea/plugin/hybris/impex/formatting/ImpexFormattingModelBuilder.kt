/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.impex.formatting

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings

class ImpexFormattingModelBuilder : FormattingModelBuilder {

    override fun createModel(formattingContext: FormattingContext): FormattingModel {
        val element = formattingContext.psiElement
        val settings = formattingContext.codeStyleSettings
        val impexBlock = ImpexBlock(
            node = element.node,
            alignment = Alignment.createAlignment(),
            spacingBuilder = ImpExSpacingBuilder(settings, settings.getCustomSettings(ImpexCodeStyleSettings::class.java)),
            codeStyleSettings = settings,
            alignmentStrategy = getAlignmentStrategy(settings)
        )

        return FormattingModelProvider.createFormattingModelForPsiFile(element.containingFile, impexBlock, settings)
    }

    private fun getAlignmentStrategy(settings: CodeStyleSettings): ImpExAlignmentStrategy {
        val impexCodeStyleSettings = settings.getCustomSettings(ImpexCodeStyleSettings::class.java)

        return if (impexCodeStyleSettings.TABLIFY) ImpExTableAlignmentStrategy()
        else ImpExColumnsAlignmentStrategy()
    }

    override fun getRangeAffectingIndent(file: PsiFile, offset: Int, elementAtOffset: ASTNode?) = null
}
