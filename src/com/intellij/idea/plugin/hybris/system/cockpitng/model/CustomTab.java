// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/editorArea

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/editorArea:customTab interface.
 */
public interface CustomTab extends DomElement, Tab {

    /**
     * Returns the value of the class child.
     *
     * @return the value of the class child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("class")
    GenericAttributeValue<String> getClazz();


    /**
     * Returns the value of the spring-bean child.
     *
     * @return the value of the spring-bean child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("spring-bean")
    GenericAttributeValue<String> getSpringBean();


    /**
     * Returns the list of render-parameter children.
     *
     * @return the list of render-parameter children.
     */
    @NotNull
    @SubTagList("render-parameter")
    java.util.List<Parameter> getRenderParameters();

    /**
     * Adds new child to the list of render-parameter children.
     *
     * @return created child
     */
    @SubTagList("render-parameter")
    Parameter addRenderParameter();


}
