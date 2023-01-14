// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
 * null:WidgetConnectionRemove interface.
 * <pre>
 * <h3>Type null:WidgetConnectionRemove documentation</h3>
 * Removes a connection between two widgets.
 * </pre>
 */
public interface WidgetConnectionRemove extends DomElement {

    /**
     * Returns the value of the name child.
     * <pre>
     * <h3>Attribute null:name documentation</h3>
     * Human readable name of the connection (optional).
     * </pre>
     *
     * @return the value of the name child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("name")
    GenericAttributeValue<String> getName();


    /**
     * Returns the value of the sourceWidgetId child.
     * <pre>
     * <h3>Attribute null:sourceWidgetId documentation</h3>
     * ID of the connection's source widget.
     * </pre>
     *
     * @return the value of the sourceWidgetId child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("sourceWidgetId")
    GenericAttributeValue<String> getSourceWidgetId();


    /**
     * Returns the value of the outputId child.
     * <pre>
     * <h3>Attribute null:outputId documentation</h3>
     * Output socket ID of the source widget.
     * </pre>
     *
     * @return the value of the outputId child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("outputId")
    GenericAttributeValue<String> getOutputId();


    /**
     * Returns the value of the targetWidgetId child.
     * <pre>
     * <h3>Attribute null:targetWidgetId documentation</h3>
     * ID of the connection's target widget.
     * </pre>
     *
     * @return the value of the targetWidgetId child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("targetWidgetId")
    GenericAttributeValue<String> getTargetWidgetId();


    /**
     * Returns the value of the inputId child.
     * <pre>
     * <h3>Attribute null:inputId documentation</h3>
     * Input socket ID of the target widget.
     * </pre>
     *
     * @return the value of the inputId child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("inputId")
    GenericAttributeValue<String> getInputId();


}
