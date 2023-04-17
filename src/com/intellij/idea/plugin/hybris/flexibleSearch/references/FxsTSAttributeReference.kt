package com.intellij.idea.plugin.hybris.flexibleSearch.references

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.HybrisConstants.SOURCE_ATTRIBUTE_NAME
import com.intellij.idea.plugin.hybris.common.HybrisConstants.TARGET_ATTRIBUTE_NAME
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.*
import com.intellij.idea.plugin.hybris.psi.reference.TSReferenceBase
import com.intellij.idea.plugin.hybris.psi.utils.PsiUtils
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.AttributeResolveResult
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.EnumResolveResult
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.RelationEndResolveResult
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.*

internal class FxsTSAttributeReference(owner: FlexibleSearchColumnReference) : TSReferenceBase<FlexibleSearchColumnReference>(owner) {

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> = CachedValuesManager.getManager(project)
        .getParameterizedCachedValue(element, CACHE_KEY, provider, false, this)
        .let { PsiUtils.getValidResults(it) }

    fun getType(): String? {
        val itemType = if (hasPrefix(element)) {
            deepSearchOfTypeReference(element, element.firstChild.text)
        } else {
            findItemTypeReference(element)
        }
        return itemType
            ?.text
            ?.replace("!", "")
    }

    companion object {
        val CACHE_KEY = Key.create<ParameterizedCachedValue<Array<ResolveResult>, FxsTSAttributeReference>>("HYBRIS_TS_CACHED_REFERENCE")

        private val provider = ParameterizedCachedValueProvider<Array<ResolveResult>, FxsTSAttributeReference> { ref ->
            val featureName = ref.element.text.replace("!", "")
            val result: Array<ResolveResult> = if (hasPrefix(ref.element)) {
                findReference(ref.project, deepSearchOfTypeReference(ref.element, ref.element.firstChild.text), ref.element.lastChild.text)
            } else {
                findReference(ref.project, findItemTypeReference(ref.element), featureName)
            }

            CachedValueProvider.Result.create(
                result,
                TSMetaModelAccess.getInstance(ref.project).getMetaModel(), PsiModificationTracker.MODIFICATION_COUNT
            )
        }

        private fun hasPrefix(element: FlexibleSearchColumnReference) =
            ((element.firstChild as LeafPsiElement).elementType == FlexibleSearchTypes.TABLE_NAME_IDENTIFIER)

        private fun findReference(project: Project, itemType: FlexibleSearchTableName?, refName: String): Array<ResolveResult> {
            val metaService = TSMetaModelAccess.getInstance(project)
            val type = itemType
                ?.text
                ?.replace("!", "")
                ?: return ResolveResult.EMPTY_ARRAY
            return tryResolveByItemType(type, refName, metaService)
                ?: tryResolveByRelationType(type, refName, metaService)
                ?: tryResolveByEnumType(type, refName, metaService)
                ?: ResolveResult.EMPTY_ARRAY
        }

        private fun tryResolveByItemType(type: String, refName: String, metaService: TSMetaModelAccess): Array<ResolveResult>? =
            metaService.findMetaItemByName(type)
                ?.let { meta ->
                    val attributes = meta.allAttributes
                        .filter { refName.equals(it.name, true) }
                        .map { AttributeResolveResult(it) }

                    val relations = meta.allRelationEnds
                        .filter { refName.equals(it.name, true) }
                        .map { RelationEndResolveResult(it) }

                    (attributes + relations).toTypedArray()
                }

        private fun tryResolveByRelationType(type: String, refName: String, metaService: TSMetaModelAccess): Array<ResolveResult>? {
            val meta = metaService.findMetaRelationByName(type) ?: return null

            if (SOURCE_ATTRIBUTE_NAME.equals(refName, true)) {
                return arrayOf(RelationEndResolveResult(meta.source))
            } else if (TARGET_ATTRIBUTE_NAME.equals(refName, true)) {
                return arrayOf(RelationEndResolveResult(meta.target))
            }

            return metaService.findMetaItemByName(HybrisConstants.TS_TYPE_LINK)
                ?.attributes
                ?.get(refName)
                ?.let { arrayOf(AttributeResolveResult(it)) }
        }

        private fun tryResolveByEnumType(type: String, refName: String, metaService: TSMetaModelAccess): Array<ResolveResult>? {
            val meta = metaService.findMetaEnumByName(type) ?: return null

            return if (HybrisConstants.ENUM_ATTRIBUTES.contains(refName)) {
                arrayOf(EnumResolveResult(meta))
            } else return null
        }

        private fun findItemTypeReference(element: PsiElement) = PsiTreeUtil.getParentOfType(element, FlexibleSearchQuerySpecification::class.java)
            ?.let {
                PsiTreeUtil.findChildOfType(it, FlexibleSearchFromClause::class.java)
                    ?.tableReferenceList
                    ?.let { PsiTreeUtil.findChildOfType(it, FlexibleSearchTableName::class.java) }
            }

        private fun deepSearchOfTypeReference(elem: PsiElement, prefix: String): FlexibleSearchTableName? {
            val parent = PsiTreeUtil.getParentOfType(elem, FlexibleSearchQuerySpecification::class.java)
            val tables = PsiTreeUtil.findChildrenOfType(parent, FlexibleSearchTableReference::class.java).toList()

            val tableReference = tables.find {
                val tableName = PsiTreeUtil.findChildOfAnyType(it, FlexibleSearchTableName::class.java)
                val corName = findCorName(tableName)
                prefix == corName
            }
            return if (tableReference == null && parent != null) {
                deepSearchOfTypeReference(parent, prefix)
            } else {
                PsiTreeUtil.findChildOfType(tableReference, FlexibleSearchTableName::class.java)
            }
        }

        private fun findCorName(tableName: FlexibleSearchTableName?) = PsiTreeUtil.findSiblingForward(tableName!!.originalElement, FlexibleSearchTypes.CORRELATION_NAME, null)
            ?.text
            ?: tableName.text

    }

}
