/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
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

package sap.commerce.toolset.java.jarFinder;

import com.intellij.ide.IdeCoreBundle;
import com.intellij.jarFinder.MavenCentralSourceSearcher;
import com.intellij.jarFinder.SourceSearcher;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class SonatypeCentralSourceSearcher extends SourceSearcher {

    private static final Logger LOG = Logger.getInstance(MavenCentralSourceSearcher.class);

    @Override
    @Nullable
    public String findSourceJar(@NotNull final ProgressIndicator indicator,
                                @NotNull final String artifactId,
                                @NotNull final String version,
                                @NotNull final VirtualFile classesJar) {
        try {
            indicator.setText(IdeCoreBundle.message("progress.message.connecting.to", "https://central.sonatype.com"));
            indicator.checkCanceled();

            String url = "https://central.sonatype.com/solrsearch/select?rows=3&wt=xml&q=";
            final String groupId = findMavenGroupId(classesJar, artifactId);
            if (groupId != null) {
                url += "g:" + groupId + "%20AND%20";
            }
            url += "a:" + artifactId + "%20AND%20v:" + version + "%20AND%20l:sources";

            return findElements("./response/docs/docs/g", readElementCancelable(indicator, url)).stream()
                .findFirst()
                .map(Element::getValue)
                .map(artifact ->
                    "https://repo1.maven.org/maven2/" +
                        artifact.replace('.', '/') + '/' +
                        artifactId + '/' +
                        version + '/' +
                        artifactId + '-' +
                        version + "-sources.jar"
                )
                .orElse(null);
        } catch (IOException e) {
            indicator.checkCanceled(); // Cause of IOException may be canceling of operation.

            LOG.warn(e);

            throw new RuntimeException(e);
        }
    }
}
