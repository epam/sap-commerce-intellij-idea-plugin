// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/dragAndDrop

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/dragAndDrop:drag-and-dropElemType interface.
 */
public interface DragAndDrop extends DomElement {

    /**
     * Returns the value of the strategy child.
     *
     * @return the value of the strategy child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("strategy")
    @Required
    GenericAttributeValue<String> getStrategy();


}
