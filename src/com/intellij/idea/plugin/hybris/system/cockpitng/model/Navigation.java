// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/wizard-config

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/wizard-config:NavigationType interface.
 */
public interface Navigation extends DomElement {

    /**
     * Returns the value of the id child.
     *
     * @return the value of the id child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("id")
    GenericAttributeValue<String> getId();


    /**
     * Returns the value of the merge-mode child.
     *
     * @return the value of the merge-mode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("merge-mode")
    GenericAttributeValue<String> getMergeMode();


    /**
     * Returns the value of the cancel child.
     *
     * @return the value of the cancel child.
     */
    @NotNull
    @SubTag("cancel")
    Cancel getCancel();


    /**
     * Returns the value of the back child.
     *
     * @return the value of the back child.
     */
    @NotNull
    @SubTag("back")
    Back getBack();


    /**
     * Returns the value of the next child.
     *
     * @return the value of the next child.
     */
    @NotNull
    @SubTag("next")
    Next getNext();


    /**
     * Returns the value of the done child.
     *
     * @return the value of the done child.
     */
    @NotNull
    @SubTag("done")
    Done getDone();


    /**
     * Returns the value of the custom child.
     *
     * @return the value of the custom child.
     */
    @NotNull
    @SubTag("custom")
    Custom getCustom();


}
