// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/collectionbrowser

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/collectionbrowser:mold interface.
 */
public interface Mold extends DomElement {

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
     * Returns the value of the enable-multi-select child.
     *
     * @return the value of the enable-multi-select child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("enable-multi-select")
    GenericAttributeValue<Boolean> getEnableMultiSelect();


}
