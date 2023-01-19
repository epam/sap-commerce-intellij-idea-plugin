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

// Generated on Wed Jan 18 00:35:54 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/editorArea

package com.intellij.idea.plugin.hybris.system.cockpitng.model.integration;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Namespace;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTag;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/editorArea:customElement interface.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public interface CustomElement extends DomElement, AbstractPositioned {

    /**
     * Returns the value of the default child.
     * <pre>
     * <h3>Element http://www.hybris.com/cockpitng/component/editorArea:default documentation</h3>
     * Defines default element definition - if no specific definition exists for
     * 								current locale, then default is used. Definition may contain localization properties in
     * 								[] braces and/or SpEL expressions in {} braces.
     * </pre>
     *
     * @return the value of the default child.
     */
    @NotNull
    @SubTag("default")
    @Required
    GenericDomValue<String> getDefault();


    /**
     * Returns the list of locale children.
     * <pre>
     * <h3>Element http://www.hybris.com/cockpitng/component/editorArea:locale documentation</h3>
     * Defines element definition for specified locale. Definition may contain
     * 								localization properties in [] braces and/or SpEL expressions in {} braces.
     * </pre>
     *
     * @return the list of locale children.
     */
    @NotNull
    @SubTagList("locale")
    java.util.List<LocaleCustomElement> getLocales();

    /**
     * Adds new child to the list of locale children.
     *
     * @return created child
     */
    @SubTagList("locale")
    LocaleCustomElement addLocale();


}
