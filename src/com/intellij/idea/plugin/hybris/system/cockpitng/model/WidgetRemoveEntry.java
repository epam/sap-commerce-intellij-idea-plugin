// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * null:WidgetRemoveEntry interface.
 * <pre>
 * <h3>Type null:WidgetRemoveEntry documentation</h3>
 * Identifies widget (by ID) to be removed.
 * </pre>
 */
public interface WidgetRemoveEntry extends DomElement {

    /**
     * Returns the value of the widgetId child.
     *
     * @return the value of the widgetId child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("widgetId")
    @Required
    GenericAttributeValue<String> getWidgetId();


}
