// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/gridView

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/gridView:parameter interface.
 */
public interface Parameter extends DomElement {

    /**
     * Returns the value of the name child.
     *
     * @return the value of the name child.
     */
    @NotNull
    @SubTag("name")
    @Required
    GenericDomValue<String> getName();


    /**
     * Returns the value of the value child.
     *
     * @return the value of the value child.
     */
    @NotNull
    @SubTag("value")
    @Required
    GenericDomValue<String> getValue();


}
