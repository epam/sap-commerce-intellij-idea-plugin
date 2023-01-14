// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpit/config/hybris

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpit/config/hybris:base interface.
 */
public interface Base extends DomElement {

    /**
     * Returns the value of the labels child.
     *
     * @return the value of the labels child.
     */
    @NotNull
    @SubTag("labels")
    Labels getLabels();


    /**
     * Returns the value of the preview child.
     *
     * @return the value of the preview child.
     */
    @NotNull
    @SubTag("preview")
    Preview getPreview();


}
