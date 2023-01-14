// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/wizard-config

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/wizard-config:CancelType interface.
 */
public interface Cancel extends DomElement, AbstractAction {

    /**
     * Returns the list of revert children.
     *
     * @return the list of revert children.
     */
    @NotNull
    @SubTagList("revert")
    java.util.List<Revert> getReverts();

    /**
     * Adds new child to the list of revert children.
     *
     * @return created child
     */
    @SubTagList("revert")
    Revert addRevert();


}
