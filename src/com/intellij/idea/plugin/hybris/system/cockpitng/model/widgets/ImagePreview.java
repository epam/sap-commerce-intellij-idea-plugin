/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

// Generated on Wed Jan 18 00:35:36 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/common

package com.intellij.idea.plugin.hybris.system.cockpitng.model.widgets;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/common:image-preview interface.
 * <pre>
 * <h3>Type http://www.hybris.com/cockpitng/config/common:image-preview documentation</h3>
 * This type is deprecated since version 2005. Please use mergeMode instead
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
