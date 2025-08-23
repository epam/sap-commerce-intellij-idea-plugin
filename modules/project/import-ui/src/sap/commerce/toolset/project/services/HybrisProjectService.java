/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.project.services;

import org.jetbrains.annotations.NotNull;
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor;

import java.io.File;

public interface HybrisProjectService {

    boolean isConfigModule(@NotNull File file);

    @Deprecated(since = "Review this usage")
    boolean isCCv2Module(@NotNull File file);

    @Deprecated(since = "Review this usage")
    boolean isAngularModule(@NotNull File file);

    boolean isPlatformModule(@NotNull File file);

    boolean isPlatformExtModule(@NotNull File file);

    boolean isCoreExtModule(@NotNull File file);

    boolean isHybrisModule(@NotNull File file);

    boolean isOutOfTheBoxModule(@NotNull File file, @NotNull HybrisProjectDescriptor rootProjectDescriptor);

    @Deprecated(since = "Review this usage")
    boolean isMavenModule(File rootProjectDirectory);

    @Deprecated(since = "Review this usage")
    boolean isEclipseModule(File rootProjectDirectory);

    @Deprecated(since = "Review this usage")
    boolean isGradleModule(File file);

    @Deprecated(since = "Review this usage")
    boolean isGradleKtsModule(File file);

    boolean hasVCS(File rootProjectDirectory);
}
