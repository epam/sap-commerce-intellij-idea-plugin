// Generated on Fri Nov 17 20:45:54 CET 2017
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.bean.model;

import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * null:abstractPojo interface.
 */
public interface AbstractPojo extends DomElement {

    /**
     * Returns the value of the class child.
     *
     * @return the value of the class child.
     */
    @NotNull
    @Attribute("class")
    @Required
    GenericAttributeValue<String> getClazz();


    /**
     * Returns the value of the template child.
     *
     * @return the value of the template child.
     */
    @NotNull
    GenericAttributeValue<String> getTemplate();


}
