// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/common

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/common:imagePreview interface.
 * <pre>
 * <h3>Type http://www.hybris.com/cockpitng/config/common:imagePreview documentation</h3>
 * A general purpose type to define configuration of image preview
 * </pre>
 */
public interface ImagePreview extends DomElement {

    /**
     * Returns the value of the display-thumbnail child.
     *
     * @return the value of the display-thumbnail child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("display-thumbnail")
    GenericAttributeValue<Boolean> getDisplayThumbnailAttr();


    /**
     * Returns the value of the display-preview child.
     *
     * @return the value of the display-preview child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("display-preview")
    GenericAttributeValue<Boolean> getDisplayPreviewAttr();


    /**
     * Returns the value of the display-default-image child.
     *
     * @return the value of the display-default-image child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("display-default-image")
    GenericAttributeValue<Boolean> getDisplayDefaultImageAttr();


    /**
     * Returns the value of the default-image child.
     *
     * @return the value of the default-image child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("default-image")
    GenericAttributeValue<String> getDefaultImageAttr();


    /**
     * Returns the value of the display-thumbnail child.
     *
     * @return the value of the display-thumbnail child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("display-thumbnail")
    GenericAttributeValue<Boolean> getDisplayThumbnail();


    /**
     * Returns the value of the display-preview child.
     *
     * @return the value of the display-preview child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("display-preview")
    GenericAttributeValue<Boolean> getDisplayPreview();


    /**
     * Returns the value of the display-default-image child.
     *
     * @return the value of the display-default-image child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("display-default-image")
    GenericAttributeValue<Boolean> getDisplayDefaultImage();


    /**
     * Returns the value of the default-image child.
     *
     * @return the value of the default-image child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("default-image")
    GenericAttributeValue<String> getDefaultImage();


}
