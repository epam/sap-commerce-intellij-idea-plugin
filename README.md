[![Email](https://img.shields.io/badge/Help-Contact%20us-blue)](mailto:hybrisideaplugin@epam.com)
[![JetBrains IntelliJ Platform SDK Docs](https://jb.gg/badges/docs.svg)](https://plugins.jetbrains.com/docs/intellij)

![Rating](https://img.shields.io/jetbrains/plugin/r/rating/12867-sap-commerce-developers-toolset)
![Downloads](https://img.shields.io/jetbrains/plugin/d/12867-sap-commerce-developers-toolset)
[![Version](https://img.shields.io/jetbrains/plugin/v/12867-sap-commerce-developers-toolset)](https://plugins.jetbrains.com/plugin/12867-sap-commerce-developers-toolset)

## SAP Commerce Developers Toolset ##

This plugin provides [SAP Commerce](https://www.sap.com/products/crm/e-commerce-platforms.html) <sup>(Hybris)</sup> integration into [Intellij IDEA](https://www.jetbrains.com/idea/) and another IDE based on it.

## Documentation

* [Type System Diagram Legend](docs%2FLEGEND_TYPE_SYSTEM_DIAGRAM.md)

## Contribution guidelines ##

* Please read [Contributor License Agreement](http://developercertificate.org)
* Available tasks are in our [project board](https://github.com/epam/sap-commerce-intellij-idea-plugin/projects/1) 
* [How to Configure Project Environment For Plugin Developers](https://www.jetbrains.org/intellij/sdk/docs/basics/getting_started/setting_up_environment.html)
* We are working with [Pull Requests](https://help.github.com/articles/about-pull-requests/). You need to fork this repository, implement a feature in a separate branch, then send us a pull request.
* Be sure to include into your pull request and all commit messages the following line: "Signed-off-by: Your Real Name your.email@email.com" otherwise it can not be accepted. Use your real name (sorry, no pseudonyms or anonymous contributions).
* Start with official [JetBrains Plugin SDK](https://plugins.jetbrains.com/docs/intellij)
* Checkout plugin development [community support](https://intellij-support.jetbrains.com/hc/en-us/community/topics/200366979-IntelliJ-IDEA-Open-API-and-Plugin-Development) for common question
* For additional questions you can send an [email](mailto:hybrisideaplugin@epam.com).

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
* By default, IDEA Ultimate version will be started and if it closed Community edition will be started right away

### Contributors and Developers

This project exists thanks to all the people who contribute.


<img src="https://img.shields.io/badge/-Alexander%20Bartash-grey"/> <img src="https://img.shields.io/badge/-Vlad%20Bozhenok-grey"/> 
<img src="https://img.shields.io/badge/-Martin%20Zdarsky--Jones-grey"/> <img src="https://img.shields.io/badge/-Alexander%20Nosov-grey"/>
<img src="https://img.shields.io/badge/-Eugene%20Kudelevsky-grey"/> <img src="https://img.shields.io/badge/-Cristian%20Caprar-grey"/>
<img src="https://img.shields.io/badge/-Daniel%20Carter-grey"/> <img src="https://img.shields.io/badge/-Oleksandr%20Mishchuk-grey"/>
<img src="https://img.shields.io/badge/-Michael%20Golubev-grey"/> <img src="https://img.shields.io/badge/-Nicko%20Cadell-grey"/>
<img src="https://img.shields.io/badge/-Dan%20Wanigasekera-grey"/> <img src="https://img.shields.io/badge/-Markus%20Priegl-grey"/>
<img src="https://img.shields.io/badge/-Sergei%20Aksenenko-grey"/> <img src="https://img.shields.io/badge/-Roger%20Ye-grey"/>
<img src="https://img.shields.io/badge/-Hector%20Longarte-grey"/> <img src="https://img.shields.io/badge/-Fabian%20Necci-grey"/>
<img src="https://img.shields.io/badge/-Markus%20Perndorfer-grey"/> <img src="https://img.shields.io/badge/-FAIR%20Consulting%20Group-grey"/>
<img src="https://img.shields.io/badge/-Mykyta%20Kostiuk-grey"/> <img src="https://img.shields.io/badge/-Dmytro%20Lytvynenko-grey"/>
<img src="https://img.shields.io/badge/-Oleksandr%20Shkurat-grey"/> <img src="https://img.shields.io/badge/-Maxim%20Bilohay-grey"/>
<img src="https://img.shields.io/badge/-Eugene%20Koryakin-grey"/> <img src="https://img.shields.io/badge/-Yevhenii%20Koshevyi-grey"/>
<img src="https://img.shields.io/badge/-Mykhailo%20Lytvyn-grey"/> <img src="https://img.shields.io/badge/-Viktors%20Jengovatovs-grey"/>
<img src="https://img.shields.io/badge/-Rustam%20Burmenskyi-grey"/> <img src="https://img.shields.io/badge/-Oleksandr%20Dihtiar-grey"/>
<img src="https://img.shields.io/badge/-Andrei%20Lisetskii-grey"/>

## Licence ##
[GNU Lesser General Public License 3.0](http://www.gnu.org/licenses/)

Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>

Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>

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