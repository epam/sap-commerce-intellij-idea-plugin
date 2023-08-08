[![Email](https://img.shields.io/badge/Help-Contact%20us-blue)](mailto:hybrisideaplugin@epam.com)
![Rating](https://img.shields.io/jetbrains/plugin/r/rating/12867-sap-commerce-developers-toolset)
![Downloads](https://img.shields.io/jetbrains/plugin/d/12867-sap-commerce-developers-toolset)
[![Version](https://img.shields.io/jetbrains/plugin/v/12867-sap-commerce-developers-toolset)](https://plugins.jetbrains.com/plugin/12867-sap-commerce-developers-toolset)

[![JetBrains IntelliJ Platform SDK Docs](https://jb.gg/badges/docs.svg)](https://plugins.jetbrains.com/docs/intellij)
[![JetBrains IntelliJ Platform SDK Samples](https://img.shields.io/badge/JB-SDK%20samples-lightgreen)](https://github.com/JetBrains/intellij-sdk-code-samples)
[![JetBrains IntelliJ Platform UI Guidelines](https://img.shields.io/badge/JB-UI%20Guidelines-lightgreen)](https://jetbrains.github.io/ui/)

## SAP Commerce Developers Toolset ##

<!-- Plugin description -->
This plugin provides [SAP Commerce](https://www.sap.com/products/crm/e-commerce-platforms.html) <sup>(Hybris)</sup> integration into [Intellij IDEA](https://www.jetbrains.com/idea/) and another IDE based on it.

## Features

- Import of SAP Commerce extensions to Intellij IDEA with automatic dependency resolution and classpath configuration optimized for fast compilation.
- Automatic configuration of Spring, Web, Ant, Database Connector Intellij IDEA plugins.
- Import your custom Eclipse, Maven and Gradle extensions together with SAP Commerce platform.
- Tight integration with [kotlinnature](https://github.com/mlytvyn/kotlinnature) which will enhance SAP Commerce with **Kotlin** language support
- Custom editor for [Polyglot Query.](https://help.sap.com/docs/SAP_COMMERCE/d0224eca81e249cb821f2cdf45a82ace/651d603ed81247c2be1708f22baed11b.html)
- Custom editor for [FlexibleSearch](https://help.sap.com/docs/SAP_COMMERCE/d0224eca81e249cb821f2cdf45a82ace/8bc399c186691014b8fce25e96614547.html) queries with an ability to execute them on a remote SAP Commerce instance right from your IDE by a single click of a button.
- Custom editor for [ImpEx](https://help.sap.com/docs/SAP_COMMERCE/d0224eca81e249cb821f2cdf45a82ace/8bee24e986691014b97bcd2c7e6ff732.html) files with automatic formatting, find usages and go to declaration actions, validation and import of ImpEx files into a remote SAP Commerce instance right from your IDE by a single click of a button.
- Custom editor for `items.xml` with validation, best-practices analyses, quick-fix autosuggestion and easy navigation.
- Custom editor for `beans.xml` & `cockpitng` with custom automated Intellij refactorings actions.
- Visualization of Business Process, [Type System](https://github.com/epam/sap-commerce-intellij-idea-plugin/blob/main/docs%2FLEGEND_TYPE_SYSTEM_DIAGRAM.md) and Module Dependencies (use context menu of the file "Diagrams/Show Diagram", only Ultimate IDEA).
- Preview for Type and Bean Systems.
- Enhanced Debugger for Model classes with lazy evaluation.
- Enhanced project view tree.
- Execution of FlexibleSearch queries, Groovy scripts, ImpEx files though IDE using HAC Integration tool.
- Execution of queries on remote Solr instances.
- Override module grouping via `hybris4intellij.properties`
- And much more, complete change log can be found [here](https://github.com/epam/sap-commerce-intellij-idea-plugin/blob/main/CHANGELOG.md).

## Contribution guidelines ##

* Please read [Contributor License Agreement](https://developercertificate.org)
* Available tasks are in our [project board](https://github.com/epam/sap-commerce-intellij-idea-plugin/projects/1) 
* [How to Configure Project Environment For Plugin Developers](https://www.jetbrains.org/intellij/sdk/docs/basics/getting_started/setting_up_environment.html)
* We are working with [Pull Requests](https://help.github.com/articles/about-pull-requests/). You need to fork this repository, implement a feature in a separate branch, then send us a pull request.
* Be sure to include into your pull request and all commit messages the following line: "Signed-off-by: Your Real Name your.email@email.com" otherwise it can not be accepted. Use your real name (sorry, no pseudonyms or anonymous contributions).
* Start with official [JetBrains Plugin SDK](https://plugins.jetbrains.com/docs/intellij)
* Checkout plugin development [community support](https://intellij-support.jetbrains.com/hc/en-us/community/topics/200366979-IntelliJ-IDEA-Open-API-and-Plugin-Development) for common question
* For additional questions you can send an [email](mailto:hybrisideaplugin@epam.com).

<!-- Plugin description end -->

### Quick start ##

* Fork and checkout most-active branch the project
* Refresh gradle dependencies
* Execute gradle task `buildPlugin` which can be found under `intellij` Tasks group
  * it will create <i>The Plugin</i> zip file under `build/distributions`
  * try to execute this step every time before starting the Intellij IDEA
* Execute gradle task `runIde` under the same `intellij` Task group
  * it can be executed in both Run and Debug mode
* Manually install <i>The Plugin</i> from the local file located in the `build/distributions` in case of
  * if this is a first execution of the `runIde` task
  * or if `clean` was executed, because `clean` task will delete `build/idea-sandbox`
  * once installed, restart IDE
* By default, IDEA Ultimate version will be started. To be able to start IDEA Community edition `ideDir` has to be configured for `runIde` Gradle task

### Contributors and Developers

This project exists thanks to all the people who <a href="https://github.com/epam/sap-commerce-intellij-idea-plugin/graphs/contributors" target="_blank">contribute</a>!

List of all ever contributors can be found here: [CONTRIBUTING](CONTRIBUTING.md)

## Licence ##
[GNU Lesser General Public License 3.0](https://www.gnu.org/licenses/)

Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>

Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com> and contributors

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.