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

import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.Namespace;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;
import sap.commerce.toolset.HybrisConstants;

/**
 * http://www.hybris.com/cockpitng/component/editorArea:panel interface.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public interface Panel extends DomElement, AbstractPanel {

	/**
	 * Returns the list of attribute children.
	 * @return the list of attribute children.
	 */
	@NotNull
	@SubTagList ("attribute")
	java.util.List<com.intellij.util.xml.Attribute> getAttributes();
	/**
	 * Adds new child to the list of attribute children.
	 * @return created child
	 */
	@SubTagList ("attribute")
    Attribute addAttribute();


	/**
	 * Returns the list of custom children.
	 * <pre>
	 * <h3>Element http://www.hybris.com/cockpitng/component/editorArea:custom documentation</h3>
	 * Allows to insert custom html into section. Html code may contain
	 * 									SpEL expressions regarding edited object - SpEL expressions need to be in braces
	 * 									{}
	 * </pre>
	 * @return the list of custom children.
	 */
	@NotNull
	@SubTagList ("custom")
	java.util.List<CustomElement> getCustoms();
	/**
	 * Adds new child to the list of custom children.
	 * @return created child
	 */
	@SubTagList ("custom")
	CustomElement addCustom();


}
