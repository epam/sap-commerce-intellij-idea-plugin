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

// Generated on Thu Jan 19 16:24:45 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/listView

package com.intellij.idea.plugin.hybris.system.cockpitng.model.listView;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.util.xml.SpringBeanReferenceConverter;
import com.intellij.util.xml.*;
import com.intellij.util.xml.DomElement;
import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.hybris.Positioned;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/listView:list-column interface.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public interface ListColumn extends DomElement, Positioned {

	/**
	 * Returns the value of the qualifier child.
	 * @return the value of the qualifier child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("qualifier")
	GenericAttributeValue<String> getQualifier();


	/**
	 * Returns the value of the auto-extract child.
	 * @return the value of the auto-extract child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("auto-extract")
	GenericAttributeValue<Boolean> getAutoExtract();


	/**
	 * Returns the value of the label child.
	 * @return the value of the label child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("label")
	GenericAttributeValue<String> getLabel();


	/**
	 * Returns the value of the type child.
	 * @return the value of the type child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("type")
	GenericAttributeValue<String> getType();


	/**
	 * Returns the value of the sortable child.
	 * @return the value of the sortable child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("sortable")
	GenericAttributeValue<Boolean> getSortable();


	/**
	 * Returns the value of the class child.
	 * @return the value of the class child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("class")
	GenericAttributeValue<String> getClazz();


	/**
	 * Returns the value of the width child.
	 * @return the value of the width child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("width")
	GenericAttributeValue<String> getWidth();


	/**
	 * Returns the value of the hflex child.
	 * @return the value of the hflex child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("hflex")
	GenericAttributeValue<String> getHflex();


	/**
	 * Returns the value of the spring-bean child.
	 * @return the value of the spring-bean child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("spring-bean")
	@Referencing(SpringBeanReferenceConverter.class)
	GenericAttributeValue<String> getSpringBean();


	/**
	 * Returns the value of the merge-mode child.
	 * @return the value of the merge-mode child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("merge-mode")
	GenericAttributeValue<String> getMergeMode();


	/**
	 * Returns the value of the maxChar child.
	 * @return the value of the maxChar child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("maxChar")
	GenericAttributeValue<Integer> getMaxChar();


	/**
	 * Returns the value of the link child.
	 * @return the value of the link child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("link")
	GenericAttributeValue<Boolean> getLink();


	/**
	 * Returns the value of the link-value child.
	 * @return the value of the link-value child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("link-value")
	GenericAttributeValue<String> getLinkValue();

}
