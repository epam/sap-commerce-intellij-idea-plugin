// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpit/config/hybris

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpit/config/hybris:action-group-split interface.
 */
public interface ActionGroupSplit extends DomElement, ActionGroup {

    /**
     * Returns the value of the primary-action child.
     *
     * @return the value of the primary-action child.
     */
    @NotNull
    @SubTag("primary-action")
    @Required
    Action getPrimaryAction();


}
