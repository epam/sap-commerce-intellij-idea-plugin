// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * null:Socket interface.
 */
public interface Socket extends DomElement {

    /**
     * Returns the value of the id child.
     *
     * @return the value of the id child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("id")
    @Required
    GenericAttributeValue<String> getId();


    /**
     * Returns the value of the multiplicity child.
     *
     * @return the value of the multiplicity child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("multiplicity")
    GenericAttributeValue<SocketMultiplicity> getMultiplicity();


    /**
     * Returns the value of the type child.
     *
     * @return the value of the type child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("type")
    GenericAttributeValue<String> getType();


    /**
     * Returns the value of the visibility child.
     *
     * @return the value of the visibility child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("visibility")
    GenericAttributeValue<SocketVisibility> getVisibility();


}
