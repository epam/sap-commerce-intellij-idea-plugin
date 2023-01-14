// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/dynamicForms

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/dynamicForms:dynamicFormsElemType interface.
 */
public interface DynamicForms extends DomElement {

    /**
     * Returns the value of the modelProperty child.
     *
     * @return the value of the modelProperty child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("modelProperty")
    GenericAttributeValue<String> getModelProperty();


    /**
     * Returns the list of attribute children.
     *
     * @return the list of attribute children.
     */
    @NotNull
    @SubTagList("attribute")
    java.util.List<DynamicAttribute> getAttributes();

    /**
     * Adds new child to the list of attribute children.
     *
     * @return created child
     */
    @SubTagList("attribute")
    DynamicAttribute addAttribute();


    /**
     * Returns the list of section children.
     *
     * @return the list of section children.
     */
    @NotNull
    @SubTagList("section")
    java.util.List<DynamicSection> getSections();

    /**
     * Adds new child to the list of section children.
     *
     * @return created child
     */
    @SubTagList("section")
    DynamicSection addSection();


    /**
     * Returns the list of tab children.
     *
     * @return the list of tab children.
     */
    @NotNull
    @SubTagList("tab")
    java.util.List<DynamicTab> getTabs();

    /**
     * Adds new child to the list of tab children.
     *
     * @return created child
     */
    @SubTagList("tab")
    DynamicTab addTab();


    /**
     * Returns the list of visitor children.
     *
     * @return the list of visitor children.
     */
    @NotNull
    @SubTagList("visitor")
    java.util.List<DynamicVisitor> getVisitors();

    /**
     * Adds new child to the list of visitor children.
     *
     * @return created child
     */
    @SubTagList("visitor")
    DynamicVisitor addVisitor();


}
