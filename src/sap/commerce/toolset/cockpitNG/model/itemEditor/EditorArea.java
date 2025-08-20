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

// Generated on Thu Jan 19 16:25:07 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/editorArea

package sap.commerce.toolset.cockpitNG.model.itemEditor;

import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;
import sap.commerce.toolset.HybrisConstants;

/**
 * http://www.hybris.com/cockpitng/component/editorArea:editorAreaElemType interface.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public interface EditorArea extends DomElement {

	/**
	 * Returns the value of the name child.
	 * @return the value of the name child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("name")
	GenericAttributeValue<String> getName();


	/**
	 * Returns the value of the viewMode child.
	 * @return the value of the viewMode child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("viewMode")
	GenericAttributeValue<String> getViewMode();


	/**
	 * Returns the value of the hideTabNameIfOnlyOneVisible child.
	 * @return the value of the hideTabNameIfOnlyOneVisible child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("hideTabNameIfOnlyOneVisible")
	GenericAttributeValue<Boolean> getHideTabNameIfOnlyOneVisible();


	/**
	 * Returns the value of the logic-handler child.
	 * @return the value of the logic-handler child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("logic-handler")
	GenericAttributeValue<String> getLogicHandler();


	/**
	 * Returns the value of the essentials child.
	 * @return the value of the essentials child.
	 */
	@NotNull
	@SubTag ("essentials")
	Essentials getEssentials();


	/**
	 * Returns the list of customTab children.
	 * @return the list of customTab children.
	 */
	@NotNull
	@SubTagList ("customTab")
	java.util.List<CustomTab> getCustomTabs();
	/**
	 * Adds new child to the list of customTab children.
	 * @return created child
	 */
	@SubTagList ("customTab")
	CustomTab addCustomTab();


	/**
	 * Returns the list of tab children.
	 * @return the list of tab children.
	 */
	@NotNull
	@SubTagList ("tab")
	java.util.List<Tab> getTabs();
	/**
	 * Adds new child to the list of tab children.
	 * @return created child
	 */
	@SubTagList ("tab")
	Tab addTab();


}
