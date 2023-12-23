/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.tools.remote.http;

import org.apache.http.Header;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CookieParser {

    private final static Pattern PATTERN = Pattern.compile("JSESSIONID=[a-zA-Z0-9_.-]*");

    private static final CookieParser cookieParser = new CookieParser();

    public static CookieParser getInstance() {
        return cookieParser;
    }

    private CookieParser() {
    }

    public String getSpecialCookie(final Header[] headers) {
        if(headers.length > 0) {
            for (final Header header : headers) {
                if ("Set-Cookie".equalsIgnoreCase(header.getName())) {
                    final Matcher matcher = PATTERN.matcher(header.getValue());
                    if (matcher.find()) {
                        final String jsessionId = matcher.group(0);
                        if (jsessionId != null && jsessionId.split("=").length > 1) {
                            return jsessionId.split("=")[1];
                        }
                    }
                }
            }
        }
        return null;
    }

}
