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

// Generated on Thu Jan 19 16:23:36 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/wizard-config

package com.intellij.idea.plugin.hybris.system.cockpitng.model.wizardConfig;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.hybris.Mergeable;
import com.intellij.idea.plugin.hybris.util.xml.SpringBeanReferenceConverter;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/wizard-config:InitializeType interface.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public interface Initialize extends DomElement, Mergeable {

	String TEMPLATE_BEAN = "template-bean";
	String TYPE = "type";
	String PROPERTY = "property";

	/**
	 * Returns the value of the property child.
	 * @return the value of the property child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (PROPERTY)
	@Required
	GenericAttributeValue<String> getProperty();


	/**
	 * Returns the value of the type child.
	 * @return the value of the type child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (TYPE)
	GenericAttributeValue<String> getType();


	/**
	 * Returns the value of the template-bean child.
	 * @return the value of the template-bean child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (TEMPLATE_BEAN)
	@Referencing(SpringBeanReferenceConverter.class)
	GenericAttributeValue<String> getTemplateBean();


}
