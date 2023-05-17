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

// Generated on Thu Jan 19 16:25:27 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/gridView

package com.intellij.idea.plugin.hybris.system.cockpitng.model.gridView;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.hybris.MergeMode;
import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.hybris.Positioned;
import com.intellij.idea.plugin.hybris.util.xml.SpringBeanReferenceConverter;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/gridView:renderer interface.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public interface Renderer extends DomElement, Positioned {

	/**
	 * Returns the value of the spring-bean child.
	 * @return the value of the spring-bean child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("spring-bean")
	@Referencing(SpringBeanReferenceConverter.class)
	@Required
	GenericAttributeValue<String> getSpringBean();


	/**
	 * Returns the value of the merge-mode child.
	 * @return the value of the merge-mode child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("merge-mode")
	GenericAttributeValue<MergeMode> getMergeMode();


	/**
	 * Returns the list of parameter children.
	 * @return the list of parameter children.
	 */
	@NotNull
	@SubTagList ("parameter")
	java.util.List<Parameter> getParameters();
	/**
	 * Adds new child to the list of parameter children.
	 * @return created child
	 */
	@SubTagList ("parameter")
	Parameter addParameter();


}
