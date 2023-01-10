/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.project.settings.jaxb.extensioninfo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Configures an hMC module for the extension. Required directory: /web.
 * <p/>
 * <p>Java class for webmoduleType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="webmoduleType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="webroot" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="additionalclasspath" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="jspcompile" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="sourceavailable" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "webmoduleType")
public class WebModuleType {

    @XmlAttribute(required = true)
    protected String webroot;
    @XmlAttribute
    protected String additionalclasspath;
    @XmlAttribute
    protected Boolean jspcompile;
    @XmlAttribute
    protected Boolean sourceavailable;

    /**
     * Gets the value of the webroot property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getWebroot() {
        return webroot;
    }

    /**
     * Sets the value of the webroot property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setWebroot(String value) {
        this.webroot = value;
    }

    /**
     * Gets the value of the additionalclasspath property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getAdditionalclasspath() {
        return additionalclasspath;
    }

    /**
     * Sets the value of the additionalclasspath property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAdditionalclasspath(String value) {
        this.additionalclasspath = value;
    }

    /**
     * Gets the value of the jspcompile property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isJspcompile() {
        return jspcompile;
    }

    /**
     * Sets the value of the jspcompile property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setJspcompile(Boolean value) {
        this.jspcompile = value;
    }

    /**
     * Gets the value of the sourceavailable property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isSourceavailable() {
        return sourceavailable;
    }

    /**
     * Sets the value of the sourceavailable property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setSourceavailable(Boolean value) {
        this.sourceavailable = value;
    }

}
