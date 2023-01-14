// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/wizard-config

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/wizard-config:DoneType interface.
 */
public interface Done extends DomElement, AbstractAction {

    /**
     * Returns the list of assign children.
     *
     * @return the list of assign children.
     */
    @NotNull
    @SubTagList("assign")
    java.util.List<Assign> getAssigns();

    /**
     * Adds new child to the list of assign children.
     *
     * @return created child
     */
    @SubTagList("assign")
    Assign addAssign();

}
