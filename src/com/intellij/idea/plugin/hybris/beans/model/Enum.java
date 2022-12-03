// Generated on Fri Nov 17 20:45:54 CET 2017
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.beans.model;

import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * null:enum interface.
 */
public interface Enum extends DomElement, AbstractPojo {

    /**
     * Returns the value of the deprecated child.
     * <pre>
     * <h3>Attribute null:deprecated documentation</h3>
     * Marks bean as deprecated. Allows defining a message.
     * </pre>
     *
     * @return the value of the deprecated child.
     */
    @NotNull
    GenericAttributeValue<Boolean> getDeprecated();


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


    /**
     * Returns the value of the description child.
     *
     * @return the value of the description child.
     */
    @NotNull
    GenericDomValue<String> getDescription();


    /**
     * Returns the list of value children.
     *
     * @return the list of value children.
     */
    @NotNull
    @Required
    java.util.List<EnumValue> getValues();

    /**
     * Adds new child to the list of value children.
     *
     * @return created child
     */
    GenericDomValue<String> addValue();


}
