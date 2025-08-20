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

// Generated on Thu Jan 19 16:25:49 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/dynamicForms

package sap.commerce.toolset.cockpitNG.model.dynamicForm;

import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;
import sap.commerce.toolset.HybrisConstants;
import sap.commerce.toolset.cockpitNG.model.config.hybris.Mergeable;
import sap.commerce.toolset.cockpitNG.model.config.hybris.Positioned;
import sap.commerce.toolset.util.xml.SpringBeanReferenceConverter;

/**
 * http://www.hybris.com/cockpitng/component/dynamicForms:dynamicVisitor interface.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public interface DynamicVisitor extends DomElement, Positioned, Mergeable {

	/**
	 * Returns the value of the id child.
	 * @return the value of the id child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("id")
	@Required
	GenericAttributeValue<String> getId();


	/**
	 * Returns the value of the beanId child.
	 * @return the value of the beanId child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("beanId")
	@Required
	@Referencing(SpringBeanReferenceConverter.class)
	GenericAttributeValue<String> getBeanId();


	/**
	 * Returns the value of the scriptingConfig child.
	 * @return the value of the scriptingConfig child.
	 */
	@NotNull
	@SubTag ("scriptingConfig")
	ScriptingConfig getScriptingConfig();


}
