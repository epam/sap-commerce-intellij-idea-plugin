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

package com.intellij.idea.plugin.hybris.business.process.jaxb.services;

import com.intellij.idea.plugin.hybris.business.process.jaxb.Process;
import jakarta.xml.bind.UnmarshalException;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.File;

/**
 * Created 9:12 PM 02 February 2016.
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public interface BpJaxbService {

    @Nonnull
    Process unmarshallBusinessProcessXml(@NotNull File file) throws UnmarshalException;

}
