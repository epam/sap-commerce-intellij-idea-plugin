// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * null:WidgetConnection interface.
 * <pre>
 * <h3>Type null:WidgetConnection documentation</h3>
 * Describes a connection between two widgets. By specifying a widget connection between
 * 				source and target
 * 				widgets, a communication channel is enabled, allowing asynchronous data transfer from source to target
 * 				widget.
 * </pre>
 */
public interface WidgetConnection extends DomElement {

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
    @Required
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
    @Required
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
    @Required
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
    @Required
    GenericAttributeValue<String> getInputId();


    /**
     * Returns the value of the module-url child.
     *
     * @return the value of the module-url child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("module-url")
    GenericAttributeValue<String> getModuleUrl();


}
