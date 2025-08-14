/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.flexibleSearch.codeInsight.lookup

import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.idea.plugin.hybris.codeInsight.completion.AutoPopupInsertHandler
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchColumnAliasName
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTableAliasName
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchYColumnName
import com.intellij.idea.plugin.hybris.flexibleSearch.settings.state.FlexibleSearchSettingsState
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.TSResolveResultUtil
import com.intellij.psi.ResolveResult

object FxSLookupElementFactory {

    fun buildYColumn(addComma: Boolean) = LookupElementBuilder.create("{}" + if (addComma) "," else "")
        .withPresentableText(" ")
        .withTailText("{...}")
        .withIcon(HybrisIcons.FlexibleSearch.Y_COLUMN_PLACEHOLDER)
        .withTypeText(null, HybrisIcons.Y.LOGO_BLUE, true)
        .withTypeIconRightAligned(true)
        .withInsertHandler(object : AutoPopupInsertHandler() {
            override fun handle(context: InsertionContext, item: LookupElement) {
                val cursorOffset = context.editor.caretModel.offset
                val moveBackTo = if (addComma) 2 else 1
                context.editor.caretModel.moveToOffset(cursorOffset - moveBackTo)
            }
        })

    fun buildYSubSelect() = LookupElementBuilder.create("{{  }}")
        .withPresentableText(" ")
        .withTailText("{{...}}")
        .withTypeText(message("hybris.fxs.completion.subQuery"), true)
        .withIcon(HybrisIcons.FlexibleSearch.Y_COLUMN_PLACEHOLDER)
        .withTypeText(null, HybrisIcons.Y.LOGO_BLUE, true)
        .withTypeIconRightAligned(true)
        .withInsertHandler { ctx, _ ->
            val cursorOffset = ctx.editor.caretModel.offset
            ctx.editor.caretModel.moveToOffset(cursorOffset - 3)
        }

    fun buildYFrom() = LookupElementBuilder.create("{}")
        .withPresentableText(" ")
        .withTailText("{...}")
        .withTypeText(null, HybrisIcons.Y.LOGO_BLUE, true)
        .withTypeIconRightAligned(true)
        .withIcon(HybrisIcons.FlexibleSearch.Y_FROM_PLACEHOLDER)
        .withInsertHandler(object : AutoPopupInsertHandler() {
            override fun handle(context: InsertionContext, item: LookupElement) {
                val cursorOffset = context.editor.caretModel.offset
                context.editor.caretModel.moveToOffset(cursorOffset - 1)
            }
        })

    fun buildFromParen() = LookupElementBuilder.create("()")
        .withPresentableText(" ")
        .withTailText("(...)")
        .withIcon(HybrisIcons.FlexibleSearch.FROM_PARENS_PLACEHOLDER)
        .withInsertHandler { ctx, _ ->
            val cursorOffset = ctx.editor.caretModel.offset
            ctx.editor.caretModel.moveToOffset(cursorOffset - 1)
        }

    fun buildIn() = LookupElementBuilder.create("IN ()")
        .bold()
        .withPresentableText("IN")
        .withTailText(" (...)")
        .withIcon(HybrisIcons.FlexibleSearch.KEYWORD)
        .withInsertHandler { ctx, _ ->
            val cursorOffset = ctx.editor.caretModel.offset
            ctx.editor.caretModel.moveToOffset(cursorOffset - 1)
        }

    fun buildNotIn() = LookupElementBuilder.create("NOT IN ()")
        .bold()
        .withPresentableText("NOT IN")
        .withTailText(" (...)")
        .withIcon(HybrisIcons.FlexibleSearch.KEYWORD)
        .withInsertHandler { ctx, _ ->
            val cursorOffset = ctx.editor.caretModel.offset
            ctx.editor.caretModel.moveToOffset(cursorOffset - 1)
        }

    fun buildYColumnAll(addComma: Boolean) = LookupElementBuilder.create("*" + if (addComma) "," else "")
        .withPresentableText(" ")
        .withTailText(message("hybris.fxs.completion.column.star"))
        .withIcon(HybrisIcons.FlexibleSearch.Y_COLUMN_ALL)

    fun tryBuildLocalizedName(resolveResult: ResolveResult, featureName: String) = if (TSResolveResultUtil.isLocalized(resolveResult, featureName)) {
        LookupElementBuilder.create("$featureName[]")
            .withPresentableText("[]")
            .withTailText(" ${message("hybris.fxs.completion.column.postfix.localized")}")
            .withIcon(HybrisIcons.TypeSystem.LOCALIZED)
            .withInsertHandler(object : AutoPopupInsertHandler() {
                override fun handle(context: InsertionContext, item: LookupElement) {
                    val cursorOffset = context.editor.caretModel.offset
                    context.editor.caretModel.moveToOffset(cursorOffset - 1)
                }
            })
    } else {
        null
    }

    fun buildKeywords(keywords: Collection<String>, fxsSettings: FlexibleSearchSettingsState) = keywords
        .map {
            LookupElementBuilder.create(if (fxsSettings.completion.injectSpaceAfterKeywords) "$it " else it)
                .withPresentableText(it)
                .bold()
                .withCaseSensitivity(false)
                .withIcon(HybrisIcons.FlexibleSearch.KEYWORD)
        }

    fun buildTableAliases(aliases: Collection<String>) = aliases
        .map {
            LookupElementBuilder.create(it)
                .bold()
                .withCaseSensitivity(false)
                .withIcon(HybrisIcons.FlexibleSearch.TABLE_ALIAS)
        }

    fun buildOuterJoin() = LookupElementBuilder.create(":o")
        .withTailText(" ${message("hybris.fxs.completion.column.postfix.outerJoin")}")
        .withIcon(HybrisIcons.FlexibleSearch.OUTER_JOIN)

    fun buildTablePostfixExclamationMark(prefix: String) = LookupElementBuilder.create("$prefix${HybrisConstants.FXS_TABLE_POSTFIX_EXCLAMATION_MARK}")
        .withPresentableText(HybrisConstants.FXS_TABLE_POSTFIX_EXCLAMATION_MARK)
        .withTailText(" ${message("hybris.fxs.completion.table.name.postfix.exclamationMark")}")
        .withIcon(HybrisIcons.FlexibleSearch.TABLE_SUFFIX)

    fun buildTablePostfixStar(prefix: String) = LookupElementBuilder.create("$prefix${HybrisConstants.FXS_TABLE_POSTFIX_STAR}")
        .withPresentableText(HybrisConstants.FXS_TABLE_POSTFIX_STAR)
        .withTailText(" ${message("hybris.fxs.completion.table.name.postfix.star")}")
        .withIcon(HybrisIcons.FlexibleSearch.TABLE_SUFFIX)

    fun buildSeparatorDot(prefix: String) = LookupElementBuilder.create("$prefix${HybrisConstants.FXS_TABLE_ALIAS_SEPARATOR_DOT}")
        .withPresentableText(HybrisConstants.FXS_TABLE_ALIAS_SEPARATOR_DOT)
        .withTailText(" ${message("hybris.fxs.completion.table.alias.separator.dot")}")
        .withIcon(HybrisIcons.FlexibleSearch.TABLE_ALIAS_SEPARATOR)
        .withInsertHandler(AutoPopupInsertHandler.INSTANCE)

    fun buildSeparatorColon(prefix: String) = LookupElementBuilder.create("$prefix${HybrisConstants.FXS_TABLE_ALIAS_SEPARATOR_COLON}")
        .withPresentableText(HybrisConstants.FXS_TABLE_ALIAS_SEPARATOR_COLON)
        .withTailText(" ${message("hybris.fxs.completion.table.alias.separator.colon")}")
        .withIcon(HybrisIcons.FlexibleSearch.TABLE_ALIAS_SEPARATOR)
        .withInsertHandler(AutoPopupInsertHandler.INSTANCE)

    fun build(tableAlias: FlexibleSearchTableAliasName, addComma: Boolean, fxsSettings: FlexibleSearchSettingsState): LookupElementBuilder {
        val separator = if (fxsSettings.completion.injectTableAliasSeparator) fxsSettings.completion.defaultTableAliasSeparator
        else ""
        return build(tableAlias, addComma, separator)
    }

    fun build(tableAlias: FlexibleSearchTableAliasName, addComma: Boolean = false, separator: String = "") = LookupElementBuilder
        .create(tableAlias.text.trim() + separator + (if (addComma) "," else ""))
        .withPresentableText(tableAlias.text.trim())
        .withTypeText(tableAlias.table?.text)
        .withIcon(HybrisIcons.FlexibleSearch.TABLE_ALIAS)
        .withInsertHandler(object : AutoPopupInsertHandler() {
            override fun handle(context: InsertionContext, item: LookupElement) {
                if (addComma) {
                    val cursorOffset = context.editor.caretModel.offset
                    context.editor.caretModel.moveToOffset(cursorOffset - 1)
                }
            }
        })

    fun build(columnAlias: FlexibleSearchColumnAliasName, addComma: Boolean) = LookupElementBuilder
        .create(columnAlias.text.trim() + (if (addComma) "," else ""))
        .withPresentableText(columnAlias.text.trim())
        .withIcon(HybrisIcons.FlexibleSearch.COLUMN_ALIAS)

    fun build(yColumnName: FlexibleSearchYColumnName, addComma: Boolean) = LookupElementBuilder
        .create(yColumnName.text.trim() + (if (addComma) "," else ""))
        .withPresentableText(yColumnName.text.trim())
        .withIcon(HybrisIcons.Y.LOGO_BLUE)

}