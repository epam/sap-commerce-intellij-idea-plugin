// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/spring

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/spring:abstract-extender interface.
 */
public interface AbstractExtender extends DomElement {

    /**
     * Returns the value of the bean child.
     *
     * @return the value of the bean child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("bean")
    GenericAttributeValue<String> getBean();


    /**
     * Returns the value of the property child.
     *
     * @return the value of the property child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("property")
    GenericAttributeValue<String> getProperty();


    /**
     * Returns the value of the getter child.
     *
     * @return the value of the getter child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("getter")
    GenericAttributeValue<String> getGetter();


    /**
     * Returns the value of the mapper child.
     *
     * @return the value of the mapper child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("mapper")
    GenericAttributeValue<String> getMapper();


    /**
     * Returns the value of the setter child.
     *
     * @return the value of the setter child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("setter")
    GenericAttributeValue<String> getSetter();


    /**
     * Returns the value of the id child.
     *
     * @return the value of the id child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("id")
    GenericAttributeValue<String> getId();


    /**
     * Returns the value of the abstract child.
     *
     * @return the value of the abstract child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("abstract")
    GenericAttributeValue<Boolean> getAbstract();


}
