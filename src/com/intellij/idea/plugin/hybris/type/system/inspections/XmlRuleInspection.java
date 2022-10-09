/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.type.system.inspections;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInsight.daemon.HighlightDisplayKey;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ex.InspectionProfileImpl;
import com.intellij.codeInspection.ex.InspectionProfileWrapper;
import com.intellij.idea.plugin.hybris.type.system.meta.MetaType;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaClassifier;
import com.intellij.idea.plugin.hybris.type.system.utils.TypeSystemUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.profile.codeInspection.ProjectInspectionProfileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.DomElement;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathExpressionException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.intellij.idea.plugin.hybris.common.HybrisConstants.RULESET_XML;

public class XmlRuleInspection extends LocalInspectionTool {

    private static final Logger LOG = Logger.getInstance(XmlRuleInspection.class);
    // TODO : extract TS Meta related cache to own class, ensure that it is cleaned after processing
    private static final Map<MetaType, CaseInsensitiveMap<String, TSMetaClassifier<? extends DomElement>>> META_CACHE = new ConcurrentHashMap<>();
    private static volatile Map<String, XmlRule> myRules;
    private final Object lock = new Object();

    @SuppressWarnings("unchecked")
    public static <T> CaseInsensitiveMap<String, T> getMetaType(final MetaType metaType) {
        return (CaseInsensitiveMap<String, T>) META_CACHE.computeIfAbsent(metaType, mt -> new CaseInsensitiveMap<>());
    }

    @Nullable
    @Override
    public ProblemDescriptor[] checkFile(
        final @NotNull PsiFile file,
        final @NotNull InspectionManager manager,
        final boolean isOnTheFly
    ) {
        try {
            if (!TypeSystemUtils.isTypeSystemXmlFile(file) || !TypeSystemValidationUtils.isCustomExtensionFile(file)) {
                return null;
            }

            final XmlFile xmlFile = (XmlFile) file;

            final ValidateContext sharedContext = ValidateContextImpl.createFileContext(manager, isOnTheFly, xmlFile);
            if (sharedContext == null) {
                return null;
            }

            final InspectionProfileImpl profile = ProjectInspectionProfileManager.getInstance(manager.getProject()).getCurrentProfile();
            final InspectionProfileWrapper inspectProfile = new InspectionProfileWrapper(profile);
            final HighlightDisplayLevel ruleLevel = inspectProfile.getErrorLevel(HighlightDisplayKey.find(getShortName()), file);
            final XmlRule rule = getRules(file).get(getID());

            if (rule == null) {
                return new ProblemDescriptor[0];
            }

            final List<ProblemDescriptor> result = new ArrayList<>();
//            final Instant from = Instant.now();
//            LOG.warn(Thread.currentThread().getId() + " - [STARTED] Rule " + getID());
            try {
                validateOneRule(rule, sharedContext, result, ruleLevel);
            } catch (XPathExpressionException e) {
                result.add(this.createValidationFailedProblem(sharedContext, xmlFile, e));
            }
//            LOG.warn(Thread.currentThread().getId() + " - [COMPLETED] Rule " + getID() + " took " + Duration.between(from, Instant.now()));

            return result.toArray(new ProblemDescriptor[result.size()]);
        } finally {
            META_CACHE.clear();
        }
    }

    @NotNull
    private Map<String, XmlRule> getRules(final @NotNull PsiFile file) {
        if (myRules == null) {
            synchronized (lock) {
                if (myRules == null) {
                    try {
                        myRules = loadRules();
                    } catch (IOException e) {
                        LOG.error("Error loading ruleset", e);
                        myRules = Collections.emptyMap();
                    }
                }
            }
        }

        return myRules;
    }

    protected void validateOneRule(
        @NotNull final XmlRule rule,
        @NotNull final ValidateContext context,
        @NotNull final Collection<? super ProblemDescriptor> output,
        @NotNull final HighlightDisplayLevel ruleLevel
    )
    throws XPathExpressionException {
        final XPathService xPathService = ApplicationManager.getApplication().getService(XPathService.class);

        final NodeList selection = xPathService.computeNodeSet(rule.getSelectionXPath(), context.getDocument());
        for (int i = 0; i < selection.getLength(); i++) {
            final Node nextSelected = selection.item(i);
            boolean passed = xPathService.computeBoolean(rule.getTestXPath(), nextSelected);
            if (rule.isFailOnTestQuery()) {
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

    private Map<String, XmlRule> loadRules() throws IOException {
        try (final InputStream input = this.getClass().getClassLoader().getResourceAsStream(RULESET_XML)) {
            if (input == null) {
                throw new IOException("Ruleset file is not found");
            }
            return new XmlRuleParser().parseRules(new BufferedInputStream(input)).stream()
                .collect(Collectors.toMap(XmlRule::getID, rule -> rule));
        }
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

}
