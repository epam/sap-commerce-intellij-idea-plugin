[![Email](https://img.shields.io/badge/Help-Contact%20us-blue)](mailto:hybrisideaplugin@epam.com)
[![slack](https://img.shields.io/badge/slack-join-blueviolet.svg?logo=slack)](https://join.slack.com/t/sapcommercede-0kz9848/shared_invite/zt-29gnz3fd2-mz_69mla52NOFqGGsG1Zjw)
![Rating](https://img.shields.io/jetbrains/plugin/r/rating/12867-sap-commerce-developers-toolset)
![Downloads](https://img.shields.io/jetbrains/plugin/d/12867-sap-commerce-developers-toolset)
[![Version](https://img.shields.io/jetbrains/plugin/v/12867-sap-commerce-developers-toolset)](https://plugins.jetbrains.com/plugin/12867-sap-commerce-developers-toolset)

[![JetBrains IntelliJ Platform SDK Docs](https://jb.gg/badges/docs.svg)](https://plugins.jetbrains.com/docs/intellij)
[![JetBrains IntelliJ Platform SDK Samples](https://img.shields.io/badge/JB-SDK%20samples-lightgreen)](https://github.com/JetBrains/intellij-sdk-code-samples)
[![JetBrains IntelliJ Platform UI Guidelines](https://img.shields.io/badge/JB-UI%20Guidelines-lightgreen)](https://jetbrains.github.io/ui/)

## SAP Commerce Developers Toolset ##

<!-- Plugin description -->
This plugin provides [SAP Commerce](https://www.sap.com/products/crm/e-commerce-platforms.html) <sup>(Hybris)</sup> integration into [IntelliJ IDEA](https://www.jetbrains.com/idea/) and another IDE based on it.

> **Valuable Note**: This is an open-source project. Since around 2023, its maintenance and evolution have been driven by a very small group of dedicated contributors volunteering their time outside of regular working hours.
> 
> Please take a moment to acknowledge the contributors listed on the project’s [Contributors & Insights](https://github.com/epam/sap-commerce-intellij-idea-plugin/graphs/contributors?from=10%2F07%2F2022) page!   

## References
- Overview of the releases [2023.1.0 - 2023.2.7](https://hybrismart.com/2023/09/04/part-iii-sap-commerce-developers-toolset-v-2023-1-0-intellij-idea-plugin/).
- Overview of the releases [2022.3.1 - 2023.1.0](https://hybrismart.com/2023/08/24/part-ii-sap-commerce-developers-toolset-v-2023-1-0-intellij-idea-plugin).
- Overview of the releases [2022.2.0 - 2022.3.1](https://hybrismart.com/2023/05/08/sap-commerce-dev-toolset-2022-3-1-updates).
- Complete change log can be found [here](https://github.com/epam/sap-commerce-intellij-idea-plugin/blob/main/CHANGELOG.md).

## Features

- Import of SAP Commerce extensions to IntelliJ IDEA with automatic dependency resolution and classpath configuration optimized for fast compilation.
- Override module grouping via `hybris4intellij.properties`, available after project import in the `config` module.
- Integration with SAP **CCv2** CI/CD.
- Enhanced **Debugger** for Model classes with lazy evaluation.
- Advanced in-IDE build & compilation process.
- Search Anywhere for Type & Bean Systems.
- Import your custom _Eclipse_, _Maven_ and _Gradle_ extensions together with SAP Commerce platform.
- Tight integration with [kotlinnature](https://github.com/mlytvyn/kotlinnature) which will enhance SAP Commerce with **Kotlin** language support
- Extended support and customization for multiple 3rd-party IntelliJ IDEA plugins, such as:
  - Spring, Cron, Junit, Java, Grid, i18n, JavaEE WEB, JavaEE EL, Jrebel, Ant, Groovy, Angular, Database, Diagram, Properties, Copyright, Javascript, IntelliLang. 
- Custom editors for various custom languages with automatic formatting, find usages and go to declaration actions, validation and import of files into a remote SAP Commerce instance right from your IDE by a single click of a button, so far supported custom languages:
  - [ImpEx](https://help.sap.com/docs/SAP_COMMERCE/d0224eca81e249cb821f2cdf45a82ace/8bee24e986691014b97bcd2c7e6ff732.html?locale=en-US&version=LATEST)
  - [Acl](https://help.sap.com/docs/SAP_COMMERCE/d0224eca81e249cb821f2cdf45a82ace/8b4aa00e866910148df2920f69d68b27.html?locale=en-US&version=LATEST)
  - [FlexibleSearch](https://help.sap.com/docs/SAP_COMMERCE/d0224eca81e249cb821f2cdf45a82ace/8bc399c186691014b8fce25e96614547.html)
  - [Polyglot Query](https://help.sap.com/docs/SAP_COMMERCE/d0224eca81e249cb821f2cdf45a82ace/651d603ed81247c2be1708f22baed11b.html)
- Custom editors for `items.xml`, `beans.xml` & `cockpitng` with validation, best-practices analyses, quick-fix autosuggestion and easy navigation and custom automated IntelliJ refactorings actions.
- Preview and customize Loggers per SAP Commerce instance.
- Extended support for such files as: `external-dependencies.xml`, `extensioninfo.xml`, `localextensions.xml` and `core-advanced-deployment.xml`
- Visualization of Business Process, [Type System](https://github.com/epam/sap-commerce-intellij-idea-plugin/blob/main/docs%2FLEGEND_TYPE_SYSTEM_DIAGRAM.md) and Module Dependencies (use context menu of the file "Diagrams/Show Diagram", only Ultimate IDEA).
- Preview for Type & Bean Systems.
- Comprehensive integration with the SAP Commerce instance through `hAC` API.
- Enhanced project view tree.
- Execution of Groovy scripts on a remote SAP Commerce.
- Execution of queries on remote Solr instances.

## Contribution guidelines ##

* Join project's [Slack channel](https://join.slack.com/t/sapcommercede-0kz9848/shared_invite/zt-29gnz3fd2-mz_69mla52NOFqGGsG1Zjw).
* Please read [Contributor License Agreement](https://developercertificate.org).
* Available tasks are in our [project board](https://github.com/epam/sap-commerce-intellij-idea-plugin/projects/1) or reach out _Mykhailo Lytvyn_ in a project's Slack.
* [How to Configure Project Environment For Plugin Developers](https://www.jetbrains.org/intellij/sdk/docs/basics/getting_started/setting_up_environment.html).
* We are working with [Pull Requests](https://help.github.com/articles/about-pull-requests/). You need to fork this repository, implement a feature in a separate branch, then send us a pull request.
* Be sure to include in your pull request and all commit messages the following line: "Signed-off-by: Your Real Name your.email@email.com" otherwise it can not be accepted. Use your real name (sorry, no pseudonyms or anonymous contributions).
* Start with official [JetBrains Plugin SDK](https://plugins.jetbrains.com/docs/intellij).
* Checkout plugin development [community support](https://platform.jetbrains.com) for common question.
* For additional questions, you can send an [email](mailto:hybrisideaplugin@epam.com).

<!-- Plugin description end -->

### Quick start

> Useful [Technical Notes](TECH_NOTES.md) for special cases.

* Fork and checkout most-active `main` branch the project
* Refresh gradle dependencies
* Execute gradle task `Run IDE - Ultimate` or `Run IDE - Community` for community edition

### Build process debugging

* Start the sandbox
* Enable `Debug Build Process` via actions menu
* Start the build via sandbox
* Start `Debug - Build` run configuration via Plugin development IDE

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