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
import com.intellij.util.xml.Referencing;
import org.jetbrains.annotations.NotNull;
import sap.commerce.toolset.HybrisConstants;
import sap.commerce.toolset.cockpitNG.model.config.hybris.Mergeable;
import sap.commerce.toolset.cockpitNG.model.config.hybris.Positioned;
import sap.commerce.toolset.util.xml.SpringBeanReferenceConverter;

/**
 * http://www.hybris.com/cockpitng/component/listView:list-column interface.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public interface ListColumn extends DomElement, Positioned, Mergeable {

	String QUALIFIER = "qualifier";
	String AUTO_EXTRACT = "auto-extract";
	String LABEL = "label";
	String TYPE = "type";
	String SORTABLE = "sortable";
	String CLASS = "class";
	String WIDTH = "width";
	String HFLEX = "hflex";
	String SPRING_BEAN = "spring-bean";
	String MAX_CHAR = "maxChar";
	String LINK = "link";
	String LINK_VALUE = "link-value";

	/**
	 * Returns the value of the qualifier child.
	 * @return the value of the qualifier child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (QUALIFIER)
	GenericAttributeValue<String> getQualifier();


	/**
	 * Returns the value of the auto-extract child.
	 * @return the value of the auto-extract child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (AUTO_EXTRACT)
	GenericAttributeValue<Boolean> getAutoExtract();


	/**
	 * Returns the value of the label child.
	 * @return the value of the label child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (LABEL)
	GenericAttributeValue<String> getLabel();


	/**
	 * Returns the value of the type child.
	 * @return the value of the type child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (TYPE)
	GenericAttributeValue<String> getType();


	/**
	 * Returns the value of the sortable child.
	 * @return the value of the sortable child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (SORTABLE)
	GenericAttributeValue<Boolean> getSortable();


	/**
	 * Returns the value of the class child.
	 * @return the value of the class child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (CLASS)
	GenericAttributeValue<String> getClazz();


	/**
	 * Returns the value of the width child.
	 * @return the value of the width child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (WIDTH)
	GenericAttributeValue<String> getWidth();


	/**
	 * Returns the value of the hflex child.
	 * @return the value of the hflex child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (HFLEX)
	GenericAttributeValue<String> getHflex();


	/**
	 * Returns the value of the spring-bean child.
	 * @return the value of the spring-bean child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (SPRING_BEAN)
	@Referencing(SpringBeanReferenceConverter.class)
	GenericAttributeValue<String> getSpringBean();


	/**
	 * Returns the value of the maxChar child.
	 * @return the value of the maxChar child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (MAX_CHAR)
	GenericAttributeValue<Integer> getMaxChar();


	/**
	 * Returns the value of the link child.
	 * @return the value of the link child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (LINK)
	GenericAttributeValue<Boolean> getLink();


	/**
	 * Returns the value of the link-value child.
	 * @return the value of the link-value child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (LINK_VALUE)
	GenericAttributeValue<String> getLinkValue();

}
