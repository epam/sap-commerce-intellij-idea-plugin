// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/editorArea

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/editorArea:essentials interface.
 */
public interface Essentials extends DomElement {

    /**
     * Returns the value of the initiallyOpened child.
     *
     * @return the value of the initiallyOpened child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("initiallyOpened")
    GenericAttributeValue<Boolean> getInitiallyOpened();


    /**
     * Returns the value of the essentialCustomSection child.
     *
     * @return the value of the essentialCustomSection child.
     */
    @NotNull
    @SubTag("essentialCustomSection")
    @Required
    EssentialCustomSection getEssentialCustomSection();


    /**
     * Returns the value of the essentialSection child.
     *
     * @return the value of the essentialSection child.
     */
    @NotNull
    @SubTag("essentialSection")
    @Required
    EssentialSection getEssentialSection();


}
