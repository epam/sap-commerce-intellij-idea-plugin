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

package com.intellij.idea.plugin.hybris.impex.constants;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Class contains array of keywords for use to completion on Impex files.
 * keywords got from <a href="https://help.sap.com/docs/SAP_COMMERCE/d0224eca81e249cb821f2cdf45a82ace/1c8f5bebdc6e434782ff0cfdb0ca1847.html?locale=en-US#header-mode">here</a>
 *
 * @author Aleksandr Nosov <nosovae.dev@gmail.com>
 */
public final class ImpexKeywords {

    private ImpexKeywords() {
    }

    /**
     * @return set of keywords.
     */
    public static Set<String> keywords() {
        return Sets.newHashSet(
            "INSERT",
            "UPDATE",
            "INSERT_UPDATE",
            "REMOVE"
        );
    }

    /**
     * @return set of keywords.
     */
    public static Set<String> keywordMacros() {
        return Sets.newHashSet(
            "$START_USERRIGHTS",
            "$END_USERRIGHTS"
        );
    }


}
