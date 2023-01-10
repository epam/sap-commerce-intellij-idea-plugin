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

package com.intellij.idea.plugin.hybris.business.process.jaxb.services.impl;

import com.intellij.idea.plugin.hybris.business.process.jaxb.ObjectFactory;
import com.intellij.idea.plugin.hybris.business.process.jaxb.Process;
import com.intellij.idea.plugin.hybris.business.process.jaxb.services.BpJaxbService;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.UnmarshalException;
import jakarta.xml.bind.Unmarshaller;
import java.io.File;

/**
 * Created 9:31 PM 02 February 2016.
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public class DefaultBpJaxbService implements BpJaxbService {

    private static final Logger LOG = Logger.getInstance(DefaultBpJaxbService.class);

    @Nullable
    protected LazyInitializer<JAXBContext> jaxbContext = new JAXBContextLazyInitializer();

    @Nonnull
    @Override
    public Process unmarshallBusinessProcessXml(@NotNull final File file) throws UnmarshalException {
        Validate.notNull(file);

        try {
            if (null == this.jaxbContext) {
                LOG.error(String.format(
                    "Can not unmarshall '%s' because JAXBContext has not been created.", file.getAbsolutePath()
                ));

                return null;
            }

            final Unmarshaller jaxbUnmarshaller = this.jaxbContext.get().createUnmarshaller();

            return (Process) jaxbUnmarshaller.unmarshal(file);
        } catch (Exception e) {
            throw new UnmarshalException("Can not unmarshall " + file.getAbsolutePath(), e);
        }
    }

    protected static class JAXBContextLazyInitializer extends LazyInitializer<JAXBContext> {

        @Override
        protected JAXBContext initialize() throws ConcurrentException {
            try {
                return JAXBContext.newInstance("com.intellij.idea.plugin.hybris.business.process.jaxb",
                                               ObjectFactory.class.getClassLoader());
            } catch (JAXBException e) {
                throw new ConcurrentException("Can not create JAXBContext for Business Process XML.", e);
            }
        }
    }
}
