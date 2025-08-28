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

import de.hybris.platform.hac.facade.HacLog4JFacade

/*
======= Version: 2025.1.6 =======

This script is being used by the Plugin to retrieve current Loggers details from the remote server via hAC API.

The following contract is expected:
 - script must print the results as a return value of the script
 - each logger details must be defined on a new line
 - each logger details must consist of three values separated with the " | "
    - 1: fqn logger name
    - 2: any log level as a string
    - 3: parent logger name. Root logger must return "null" as a string

======= Example =======
    INFO | solrStatisticLogger | root
    INFO | root | null
    ERROR | org.springframework.aop.framework.CglibAopProxy | org.springframework
    ERROR | org.springframework.aop.framework.Cglib2AopProxy | org.springframework
    WARN | org.springframework | root
 */

(hacLog4JFacade as HacLog4JFacade).getLoggers()
        .sort { it.name }
        .reverse()
        .collect { "${it.name} | ${it.effectiveLevel} | ${it.parentName}" }
        .join("\n")
