// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * null:Requirement interface.
 * <pre>
 * <h3>Type http://www.hybris.com/cockpit/config:Requirement documentation</h3>
 * Specifies a resource that is required for this XML file to work. Allows to
 * 				assure that some elements are already defined when specific file is loaded.
 * </pre>
 */
public interface Requirement extends DomElement {

    /**
     * Returns the value of the resource child.
     *
     * @return the value of the resource child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("resource")
    @Required
    GenericAttributeValue<String> getResourceAttr();


    /**
     * Returns the value of the resource child.
     *
     * @return the value of the resource child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("resource")
    @Required
    GenericAttributeValue<String> getResource();


}
