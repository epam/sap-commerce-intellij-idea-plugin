/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.type.system.inspections.rules.legacy;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInsight.daemon.HighlightDisplayKey;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ex.InspectionProfileImpl;
import com.intellij.codeInspection.ex.InspectionProfileWrapper;
import com.intellij.idea.plugin.hybris.type.system.inspections.ItemsXmlQuickFixManager;
import com.intellij.idea.plugin.hybris.type.system.inspections.TypeSystemValidationUtils;
import com.intellij.idea.plugin.hybris.type.system.inspections.ValidateContext;
import com.intellij.idea.plugin.hybris.type.system.inspections.ValidateContextImpl;
import com.intellij.idea.plugin.hybris.type.system.inspections.XPathService;
import com.intellij.idea.plugin.hybris.type.system.utils.TypeSystemUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Computable;
import com.intellij.profile.codeInspection.ProjectInspectionProfileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathExpressionException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractTypeSystemInspection extends LocalInspectionTool {

    private static final Logger LOG = Logger.getInstance(AbstractTypeSystemInspection.class);

    protected abstract String getNameQuery();

    @Nullable
    @Override
    public ProblemDescriptor[] checkFile(
        final @NotNull PsiFile file,
        final @NotNull InspectionManager manager,
        final boolean isOnTheFly
    ) {
        if (!TypeSystemUtils.isTypeSystemXmlFile(file) || !TypeSystemValidationUtils.isCustomExtensionFile(file)) {
            return null;
        }

        final XmlFile xmlFile = (XmlFile) file;

        final ValidateContext sharedContext = ValidateContextImpl.createFileContext(manager, isOnTheFly, xmlFile);
        if (sharedContext == null) {
            return null;
        }

        final HighlightDisplayLevel ruleLevel = getHighlightDisplayLevel(file, manager);

        return ApplicationManager.getApplication().runReadAction((Computable<ProblemDescriptor[]>) () -> {
            final List<ProblemDescriptor> result = new ArrayList<>();
            final Instant from = Instant.now();
            LOG.warn(Thread.currentThread().getId() + " - [STARTED] Rule " + getID());
            try {
                validateOneRule(sharedContext, result, ruleLevel);
            } catch (XPathExpressionException e) {
                result.add(createValidationFailedProblem(sharedContext, xmlFile, e));
            }
            LOG.warn(Thread.currentThread().getId() + " - [COMPLETED] Rule " + getID() + " took " + Duration.between(
                from,
                Instant.now()
            ));

            return result.toArray(new ProblemDescriptor[result.size()]);
        });
    }

    protected void validateOneRule(
        @NotNull final ValidateContext context,
        @NotNull final Collection<? super ProblemDescriptor> output,
        @NotNull final HighlightDisplayLevel ruleLevel
    )
    throws XPathExpressionException {
        final XPathService xPathService = ApplicationManager.getApplication().getService(XPathService.class);

        final NodeList selection = xPathService.computeNodeSet(getSelectionQuery(), context.getDocument());
        for (int i = 0; i < selection.getLength(); i++) {
            final Node nextSelected = selection.item(i);
            boolean passed = xPathService.computeBoolean(getTestQuery(), nextSelected);
            if (isFailOnTestQuery()) {
                passed = !passed;
            }
            if (!passed) {
                output.add(createProblem(context, nextSelected, ruleLevel));
            }
        }
    }

    protected ProblemDescriptor createValidationFailedProblem(
        @NotNull final ValidateContext context,
        @NotNull final PsiElement file,
        @NotNull final Exception failure
    ) {

        return context.getManager().createProblemDescriptor(
            file,
            "XmlRule '" + getID() + "' has failed to validate: " + failure.getMessage(),
            true,
            ProblemHighlightType.GENERIC_ERROR,
            context.isOnTheFly()
        );
    }

    protected abstract String getSelectionQuery();

    protected abstract String getTestQuery();

    protected boolean isFailOnTestQuery() {
        return false;
    }

    protected ProblemDescriptor createProblem(
        @NotNull final ValidateContext context,
        @NotNull final Node problemNode,
        @NotNull final HighlightDisplayLevel ruleLevel
    ) {
        final PsiElement problemPsi = context.mapNodeToPsi(problemNode);
        final ProblemHighlightType highlightType = computePriority(ruleLevel);

        final LocalQuickFix[] fixes = ItemsXmlQuickFixManager.getQuickFixes(problemNode, getID());
        return context.getManager().createProblemDescriptor(
            problemPsi,
            getDisplayName(),
            true,
            highlightType,
            context.isOnTheFly(),
            fixes
        );
    }

    @NotNull
    protected ProblemHighlightType computePriority(final @NotNull HighlightDisplayLevel ruleLevel) {
        if (HighlightDisplayLevel.WEAK_WARNING.equals(ruleLevel)) {
            return ProblemHighlightType.WEAK_WARNING;
        } else if (HighlightDisplayLevel.WARNING.equals(ruleLevel)) {
            return ProblemHighlightType.WARNING;
        }

        return ProblemHighlightType.ERROR;
    }

    @NotNull
    private HighlightDisplayLevel getHighlightDisplayLevel(
        final @NotNull PsiFile file,
        final @NotNull InspectionManager manager
    ) {
        final InspectionProfileImpl profile = ProjectInspectionProfileManager.getInstance(manager.getProject())
                                                                             .getCurrentProfile();
        final InspectionProfileWrapper inspectProfile = new InspectionProfileWrapper(profile);
        return inspectProfile.getErrorLevel(HighlightDisplayKey.find(getShortName()), file);
    }

}
