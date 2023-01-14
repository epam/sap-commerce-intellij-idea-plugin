// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * null:Import interface.
 * <pre>
 * <h3>Type http://www.hybris.com/cockpit/config:Import documentation</h3>
 * Specifies a resource for importing application configuration defined in another XML file.
 * 				Allows to
 * 				merge configuration that is distributed among many files.
 * </pre>
 */
public interface Import extends DomElement {

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
     * Returns the value of the module-url child.
     *
     * @return the value of the module-url child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("module-url")
    GenericAttributeValue<String> getModuleUrlAttr();


    /**
     * Returns the value of the resource child.
     *
     * @return the value of the resource child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("resource")
    @Required
    GenericAttributeValue<String> getResource();


    /**
     * Returns the value of the module-url child.
     *
     * @return the value of the module-url child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("module-url")
    GenericAttributeValue<String> getModuleUrl();


}
