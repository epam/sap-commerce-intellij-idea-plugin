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

// Generated on Thu Jan 19 16:25:49 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/dynamicForms

package com.intellij.idea.plugin.hybris.system.cockpitng.model.dynamicForm;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Namespace;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/dynamicForms:dynamicFormsElemType interface.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public interface DynamicForms extends DomElement {

	/**
	 * Returns the value of the modelProperty child.
	 * @return the value of the modelProperty child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("modelProperty")
	GenericAttributeValue<String> getModelProperty();


	/**
	 * Returns the list of attribute children.
	 * @return the list of attribute children.
	 */
	@NotNull
	@SubTagList ("attribute")
	java.util.List<DynamicAttribute> getAttributes();
	/**
	 * Adds new child to the list of attribute children.
	 * @return created child
	 */
	@SubTagList ("attribute")
	DynamicAttribute addAttribute();


	/**
	 * Returns the list of section children.
	 * @return the list of section children.
	 */
	@NotNull
	@SubTagList ("section")
	java.util.List<DynamicSection> getSections();
	/**
	 * Adds new child to the list of section children.
	 * @return created child
	 */
	@SubTagList ("section")
	DynamicSection addSection();


	/**
	 * Returns the list of tab children.
	 * @return the list of tab children.
	 */
	@NotNull
	@SubTagList ("tab")
	java.util.List<DynamicTab> getTabs();
	/**
	 * Adds new child to the list of tab children.
	 * @return created child
	 */
	@SubTagList ("tab")
	DynamicTab addTab();


	/**
	 * Returns the list of visitor children.
	 * @return the list of visitor children.
	 */
	@NotNull
	@SubTagList ("visitor")
	java.util.List<DynamicVisitor> getVisitors();
	/**
	 * Adds new child to the list of visitor children.
	 * @return created child
	 */
	@SubTagList ("visitor")
	DynamicVisitor addVisitor();


}
