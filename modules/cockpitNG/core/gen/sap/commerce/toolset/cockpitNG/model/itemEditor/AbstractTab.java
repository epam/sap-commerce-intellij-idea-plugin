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

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Namespace;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;
import sap.commerce.toolset.HybrisConstants;
import sap.commerce.toolset.cockpitNG.model.config.hybris.Mergeable;

/**
 * http://www.hybris.com/cockpitng/component/editorArea:abstractTab interface.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public interface AbstractTab extends DomElement, AbstractPositioned, Mergeable {

	/**
	 * Returns the value of the name child.
	 * @return the value of the name child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("name")
	@Required
	GenericAttributeValue<String> getName();


	/**
	 * Returns the value of the displayEssentialSectionIfPresent child.
	 * @return the value of the displayEssentialSectionIfPresent child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("displayEssentialSectionIfPresent")
	GenericAttributeValue<Boolean> getDisplayEssentialSectionIfPresent();


	/**
	 * Returns the value of the tooltipText child.
	 * @return the value of the tooltipText child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("tooltipText")
	GenericAttributeValue<String> getTooltipText();


	/**
	 * Returns the value of the initiallyOpened child.
	 * @return the value of the initiallyOpened child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("initiallyOpened")
	GenericAttributeValue<Boolean> getInitiallyOpened();

}
