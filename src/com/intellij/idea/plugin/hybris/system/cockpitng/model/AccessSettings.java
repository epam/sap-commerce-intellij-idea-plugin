// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

/**
 * null:AccessSettings interface.
 */
public interface AccessSettings extends DomElement {

    /**
     * Returns the value of the add child.
     *
     * @return the value of the add child.
     */
    @NotNull
    @SubTag("add")
    GenericDomValue<String> getAdd();


    /**
     * Returns the value of the remove child.
     *
     * @return the value of the remove child.
     */
    @NotNull
    @SubTag("remove")
    GenericDomValue<String> getRemove();


    /**
     * Returns the value of the replace child.
     *
     * @return the value of the replace child.
     */
    @NotNull
    @SubTag("replace")
    GenericDomValue<String> getReplace();


}
