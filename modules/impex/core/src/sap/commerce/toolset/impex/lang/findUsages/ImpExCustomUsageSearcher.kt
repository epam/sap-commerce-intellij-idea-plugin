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
package sap.commerce.toolset.impex.lang.findUsages

import com.intellij.find.findUsages.CustomUsageSearcher
import com.intellij.find.findUsages.FindUsagesOptions
import com.intellij.openapi.application.readAction
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.usageView.UsageInfo
import com.intellij.usages.Usage
import com.intellij.usages.UsageInfoToUsageConverter
import com.intellij.usages.similarity.clustering.ClusteringSearchSession
import com.intellij.util.Processor
import com.intellij.util.asSafely
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import sap.commerce.toolset.impex.psi.ImpExMacroNameDec
import sap.commerce.toolset.impex.psi.ImpExMacroUsageDec

/**
 * Implementation based on the reverse-engineering of the: FindUsagesManager & ShowUsagesAction
 */
class ImpExCustomUsageSearcher : CustomUsageSearcher() {

    override fun processElementUsages(
        element: PsiElement,
        processor: Processor<in Usage>,
        options: FindUsagesOptions
    ) {
        if (!options.isUsages) return
        val macro = element.asSafely<ImpExMacroNameDec>() ?: return

        CoroutineScope(Dispatchers.Default).launch {
            readAction {
                ensureActive()

                if (!macro.isValid) return@readAction

                val clusteringSearchSession = ClusteringSearchSession.createClusteringSessionIfEnabled()

                PsiTreeUtil.findChildrenOfAnyType(macro.containingFile, ImpExMacroUsageDec::class.java)
                    .mapNotNull {
                        ensureActive()

                        val reference = it.reference ?: return@mapNotNull null
                        val target = reference.resolve() ?: return@mapNotNull null

                        if (target != macro) return@mapNotNull null

                        val usageInfo = UsageInfo(reference)
                        val arrayOf = arrayOf<PsiElement>(macro)

                        clusteringSearchSession
                            ?.let { UsageInfoToUsageConverter.convertToSimilarUsage(arrayOf, usageInfo, it) }
                            ?: UsageInfoToUsageConverter.convert(arrayOf, usageInfo)
                    }
                    .forEach { if (!processor.process(it)) return@readAction }
            }
        }
    }
}
