// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/valuechooser

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/valuechooser:value-chooserElemType interface.
 */
public interface ValueChooser extends DomElement {

    /**
     * Returns the list of option children.
     *
     * @return the list of option children.
     */
    @NotNull
    @SubTagList("option")
    @Required
    java.util.List<Option> getOptions();

    /**
     * Adds new child to the list of option children.
     *
     * @return created child
     */
    @SubTagList("option")
    Option addOption();


}
