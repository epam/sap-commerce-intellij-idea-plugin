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

// Generated on Thu Jan 19 16:24:45 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/listView

package sap.commerce.toolset.cockpitNG.model.listView;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Namespace;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;
import sap.commerce.toolset.HybrisConstants;

/**
 * http://www.hybris.com/cockpitng/component/listView:list-viewElemType interface.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public interface ListView extends DomElement {

	String COLUMN = "column";

	/**
	 * Returns the value of the refresh-after-object-creation child.
	 * @return the value of the refresh-after-object-creation child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("refresh-after-object-creation")
	GenericAttributeValue<Boolean> getRefreshAfterObjectCreation();


	/**
	 * Returns the value of the show-header child.
	 * @return the value of the show-header child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("show-header")
	GenericAttributeValue<Boolean> getShowHeader();


	/**
	 * Returns the list of column children.
	 * @return the list of column children.
	 */
	@NotNull
	@SubTagList (COLUMN)
	java.util.List<ListColumn> getColumns();
	/**
	 * Adds new child to the list of column children.
	 * @return created child
	 */
	@SubTagList (COLUMN)
	ListColumn addColumn();


}
