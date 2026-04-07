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

package sap.commerce.toolset.impex.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.text.StringUtilRt
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.PsiReference
import com.intellij.psi.util.*
import com.intellij.util.asSafely
import sap.commerce.toolset.impex.psi.ImpExFullHeaderParameterTSContext
import sap.commerce.toolset.impex.psi.ImpExTypes
import sap.commerce.toolset.impex.psi.ImpExValue
import sap.commerce.toolset.spring.SpringFallbackScope
import sap.commerce.toolset.spring.psi.reference.SpringReference
import sap.commerce.toolset.typeSystem.meta.TSModificationTracker
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaItem
import java.io.Serial

abstract class ImpExValueMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiLanguageInjectionHost, ImpExValue {

    override fun isValidHost() = true
    override fun updateText(text: String) = this
    override fun createLiteralTextEscaper() = LiteralTextEscaper.createSimple(this)

    override fun getFieldValue(index: Int): PsiElement? = getFieldValues()
        .getOrNull(index)

    override fun getReferences(): Array<PsiReference> = CachedValuesManager.getManager(project).getCachedValue(this) {
        CachedValueProvider.Result.create(
            collectReferences(),
            TSModificationTracker.getInstance(project), this
        )
    }

    override fun isNonImportable(): Boolean = !isImportable()

    override fun isQuotable(): Boolean {
        val trimmedText = text.trim()
        if (trimmedText.isEmpty()) return false
        if (trimmedText.startsWith('\"')) return false
        val leafs = childLeafs()
            .groupBy { it.elementType }

        return !(leafs.size == 1 && leafs[ImpExTypes.MACRO_USAGE]?.size == 1)
    }

    override fun isNotQuotable(): Boolean = !isQuotable

    override fun isImportable(): Boolean = firstLeaf().elementType
        .let { it != ImpExTypes.FIELD_VALUE_IGNORE && it != ImpExTypes.FIELD_VALUE_NULL }

    private fun collectReferences(): Array<PsiReference> {
        val fullHeaderParameter = valueGroup?.fullHeaderParameter
            ?: return emptyArray()
        val tsContext = fullHeaderParameter.typeSystemContext
            ?: return emptyArray()

        return fullHeaderParameter.collectDocIdReferences(this, tsContext)
            ?: fullHeaderParameter.collectTSReferences(this, tsContext) { getFieldValues() }
            ?: collectSpringReferences(tsContext)
            ?: emptyArray()
    }

    private fun collectSpringReferences(tsContext: ImpExFullHeaderParameterTSContext): Array<PsiReference>? = tsContext.meta
        .asSafely<TSGlobalMetaItem.TSGlobalMetaItemAttribute>()
        ?.takeIf { this.macroUsageDecList.isEmpty() }
        ?.takeIf { this.isImportable }
        ?.takeIf { attr ->
            val metaItem = attr.owner.name ?: return@takeIf false

            predefinedSpringReferenceAttributes.contains("$metaItem.${attr.name}".lowercase()) || predefinedSpringIdAttributes.contains(attr.name.lowercase())
        }
        ?.let { arrayOf(SpringReference(this, StringUtilRt.unquoteString(this.text), SpringFallbackScope.CUSTOM_MODULES)) }

    private fun getFieldValues(): Array<out PsiElement> = findChildrenByType(ImpExTypes.FIELD_VALUE, PsiElement::class.java)

    @Suppress("SpellCheckingInspection")
    companion object {
        @Serial
        private val serialVersionUID: Long = 8258794639693010240L

        // Lowercased on purpose
        private val predefinedSpringReferenceAttributes = setOf(
            "solrindexedproperty.fieldvalueprovider",
            "solrindexedproperty.facetdisplaynameprovider",
            "solrindexedproperty.facetsortprovider",
            "solrindexedproperty.facettopvaluesprovider",
            "solrindexedproperty.topvaluesprovider",
            "solrindexedproperty.customFacetSortProvider",
            "solrsearchqueryproperty.facetdisplaynameprovider",
            "solrsearchqueryproperty.facetsortprovider",
            "solrsearchqueryproperty.facettopvaluesprovider",
            "solrsearchquerytemplate.ftsquerybuilder",
            "abstractasfacetconfiguration.valuesSortProvider",
            "abstractasfacetconfiguration.valuesdisplaynameprovider",
            "abstractasfacetconfiguration.topvaluesprovider",
            "solrextindexercronjob.queryparameterprovider",
            "solrindexedtype.identityprovider",
            "eventconfiguration.converterbean",
            "basestore.productsearchstrategy",
            "conversionmediaformat.conversionstrategy",
            "abstractrulebasedpromotionaction.strategyid",
            "distributedprocess.handlerbeanid",
            "automatedworkflowactiontemplate.jobhandler",
            "servicelayerjob.springidcronjobfactory",
        )
        private val predefinedSpringIdAttributes = setOf(
            "springid",
            "runnerbean"
        )
    }
}
