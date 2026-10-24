/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.project.actionSystem

import com.intellij.icons.AllIcons
import sap.commerce.toolset.project.settings.FavoriteFoldersSettings

class AddFolderToFavoritesAction : FavoriteFolderAction(
    "Add to Favorites",
    "Adds the selected folder to the Favorites node of the Project view",
    AllIcons.Nodes.Favorite
) {

    override fun isApplicable(settings: FavoriteFoldersSettings, path: String) = !settings.isFavorite(path)

    override fun perform(settings: FavoriteFoldersSettings, paths: List<String>) = settings.add(paths)
}
