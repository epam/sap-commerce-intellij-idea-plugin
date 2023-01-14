// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * null:settingsElemType interface.
 */
public interface Settings extends DomElement {

    /**
     * Returns the list of setting children.
     *
     * @return the list of setting children.
     */
    @NotNull
    @SubTagList("setting")
    @Required
    java.util.List<GenericDomValue<Setting>> getSettings();

    /**
     * Adds new child to the list of setting children.
     *
     * @return created child
     */
    @SubTagList("setting")
    GenericDomValue<Setting> addSetting();


}
