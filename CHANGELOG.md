## [2023.2.6]

### `Groovy Script` enhancements
- Introduced actions toolbar for `.groovy` files [#564](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/564)
- Added `Open Groovy Script` action
- Added `Execute Groovy Script` action [#565](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/565)
- Added Groovy settings pane and possibility to toggle actions toolbar visibility [#566](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/566)

### `FlexibleSearch` enhancements
- Introduced actions toolbar for `.fxs` files [#547](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/547)
- Added actions for connection settings in the toolbar [#549](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/549)
- Added `Execute` action to the toolbar [#552](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/552)

### `ImpEx` enhancements
- Introduced actions toolbar for `.impex` files [#550](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/550)
- Added `Execute` and `Validate` actions [#553](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/553)
- Added `Select Statement` action [#557](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/557)
- Added `Remove Column` action [#556](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/556)
- Added `Move Column Left` & `Move Column Right` actions [#562](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/562)
- Added `Insert Column Left` & `Insert Column Right` actions [#563](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/563)
- Added `Remove Table` action [#559](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/559)
- Enabled removal of the UserRights block with `Remove Table` action [#560](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/560)
- Ensure that Actions will respect readonly state of the files [#558](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/558)
- Ignore case for type in the reference type (e.g. `groups(customer.uid)`) [#545](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/545)

### `CockpitNG` enhancements
- Added code completion for AdvancedSearch `operator` parameter [#537](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/537)

### `HAC` enhancements
- Enhanced Cluster support, support node routing [#543](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/543)
- Allow blank port for connection settings [#542](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/542)
- Respect `JSESSIONID` and cookies per `HAC` connection settings
- Improved handling of the `Set-Cookie` header during login [#544](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/544)
- Automatically re-login in case of `405` response code [#548](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/548)

### Features
- Added possibility to import Gradle KTS projects as modules [#534](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/534)
- Improved folding for Relation tags in the `items.xml` [#555](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/555)

### Fixes
- Inject `FlexibleSearch` into Kotlin String template [#535](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/535)
- Readonly mode is broken in IDEA 2023.2 [#533](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/533)
- `ImpEx` Code Style Formatter is not configurable [#540](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/540) 
- `ImpEx` functional reference type validation does not work [#546](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/546) 
- Fixed `ImpEx` context actions [#551](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/551) 
- Reset `ImpEx` highlighting cache on moving columns [#567](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/567) 
- Fixed compact middle packages Project View [#568](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/568) 

### Other
- Adjusted inline documentation for Type System [#539](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/539)

## [2023.2.5]

### Features
- Added ordering attributes support for 1-to-m relations (those ending with `POS`) [#523](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/523)

### `FlexibleSearch` enhancements
- Inject language only if query starts with `SELECT` [#519](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/519)

### Fixes
- Do not register DataSource in Intellij Community [#530](https://github.com/epam/sap-commerce-intellij-idea-plugin/issues/530)
- Unable to import project from existing sources since upgrading to Idea 2023.2 [#526](https://github.com/epam/sap-commerce-intellij-idea-plugin/issues/526)
- Unable to import project in Intellij 2023.2 Community Version [#527](https://github.com/epam/sap-commerce-intellij-idea-plugin/issues/527)

### Other
- Added custom icon for `hybris4intellij.properties` file [#525](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/525)
- Do not mark `ExtensibleItem`, `LocalizableItem` & `GenericItem` as not generated in `items.xml` [#522](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/522)
- Improve performance of the inlay for `DynamicAttributeHandler` [#521](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/521) 
- Improve performance of the widely used `ModelsUtils` [#520](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/520)

## [2023.2.4]

### `FlexibleSearch` enhancements
- Improved language injection into Java files [#515](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/515)

### Features
- Improved performance of the Item attribute resolution [#516](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/516)
- Added possibility to skip non-existing source directories during project import [#511](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/511)

### Fixes
- Missing navigation to Bean Enum declaration from java class [#517](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/517)
- Fixed deadlock when ItemType name equals to its extends [#513](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/513)
- Refactored `ImportProjectProgressModalWindow` so that is calls so that project state retrieved only once [#512](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/512)
- Changed generated `*.iml` file name when grouping is not selected so file name does not start with a dot [#512](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/512)

### Other
- Updated Kotlin to 1.9.0
- Updated Gradle plugin to 1.15.0
- Updated Gradle to 8.2.1

### Deprecated
- Updated usage of the Diagram API

## [2023.2.3]

### Features
- Automatically configure Database based on project settings during project import/refresh [#509](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/509) 
- Show modifiers and persistence information for Items in the Type System preview [#505](psi_https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/505)
- Import CCv2 `core-customize` as a separate module [#498](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/498)
- Don't scan CCv2 `js-storefront` and `datahub` sub-folders during project import/refresh [#497](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/497) 

### `ImpEx` enhancements
- Added documentation for Header type name [#506](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/506)
- Added documentation for sub-type in the Value line [#507](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/507)
- Improved substitution of the `$config-xxx` properties [#496](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/496)
- New documentation settings added to the Project Settings 

### `FlexibleSearch` enhancements
- Added documentation for type name in the `FROM` statement [#508](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/508)
- New documentation settings added to the Project Settings

### Fixes
- Fix UI freeze due new Intellij IDEA startup activity API [#504](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/504)
- Improve disposing of the `ImpEx` Editor listeners [#504](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/504)
- Ensure that console storage is under `.idea` folder [#503](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/503)
- Fix UI freeze due legacy `items.xml` analysis [#502](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/502)
- Use DPI-aware borders [#501](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/501)
- Fixed single character header column width in TS/BS Systems previews [#500](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/500)
- Java single characters are not respected when copying `FlexibleSearch` query [#495](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/495)

### Other
- Updated Gradle plugin to 1.14.2

## [2023.2.2]

### `ImpEx` enhancements
- Show documentation for Header line attribute modifiers [#487](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/487)
- Show documentation for Header line type modifiers [#488](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/488)

### `FlexibleSearch` enhancements
- Add case-insensitive suggestion support in FxS query for attribute parameters [#484](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/484)

### Fixes
- Make language code case-insensitive for FlexibleSearch, ImpEx and Polyglot languages [#485](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/485)
- Missing text for `ImpEx` actions [#486](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/486)
- Fixed warning message for Settings panels [#492](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/492)
- Fixed NPE for refactored Kotlin classes [#491](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/491)

## [2023.2.1]

### Features
- Added folding for `items.xml` files [#478](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/478)
- Added folding for `-backoffice-config.xml` files [#479](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/479)

### `Project Import 2.0` 
- Due mass API changes it is required to re-import the Project
- Create new IDEA modules for each extension sub-module: `backoffice`, `acceleratoraddon`, `web`, `commonweb`, `hmc`, `hac`
- Introduced new **SAP Commerce** module facet, which will contains all extension settings 
- Module-type specific icons in the Project View
- Module groups will always have correct custom icons
- Show mandatory _Ext_ and _Platform_ extensions at the end of the Import Project Wizard
- Improved compilation
- Removed circular dependencies
- Multiple other improvements [#477](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/477)

### `External Dependencies` enhancements
- Added custom icon for `external-dependencies.xml` file [#470](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/470)

### `ImpEx` enhancements
- Only `UPDATE` allowed for non-dynamic enum inspection [#474](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/474)

### Fixes
- NPE in manifest files [#469](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/469)
- Add locale to JAVADOC_URL [#472](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/472)

### Other
- Navigate to the extension tag, not `name` attribute [#475](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/475)
- Updated Gradle plugin to 1.14.0 [#471](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/471)
- Added priority for project root tag [#473](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/473)
- [Gradle](https://gradle.org/releases/): 8.1 -> 8.1.1
- [jsoup](https://jsoup.org/): 1.15.4 -> 1.16.1
- [Apache commons-io](https://commons.apache.org/proper/commons-io/): 2.11.0 -> 2.12.0
- [Apache Maven model](https://maven.apache.org/): 3.8.7 -> 3.9.2
- [Jakarta XML Binding](https://eclipse-ee4j.github.io/jaxb-ri/): 4.0.1 -> 4.0.2
- [Apache Solr Java client](https://solr.apache.org/): 8.8.2 -> 8.11.2
- [Kotlin plugins for Gradle](https://plugins.gradle.org/plugin/org.jetbrains.kotlin.jvm): 1.8.20 -> 1.8.21
- Added links for quick check of new versions availability

## [2023.2.0]

### Features
- Added 2023.2 Intellij IDEA support [#405](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/405)
- Improved code completion and reference resolution for Enum attributes in impex/fxs/pgq files [#414](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/414)
- Added Node.js version 18 to the CCv2 js-storefront manifest file [#413](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/413)
- Migrated change log to `gradle-changelog-plugin` [#429](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/429)

### `Kotlin` language support
- Integration with [kotlinnature](https://github.com/mlytvyn/kotlinnature) extension
- Inject FlexibleSearch language into not concatenated Strings and String Templates without params [#432](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/432)
- Register Kotlin Facet for extensions with `kotlinsrc` / `kotlintestsrc` directories [#407](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/407)
- Automatically adjust Kotlin Compiler based on `kotlinnature` settings and project JDK [#409](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/409)
- Show different icon for `kotlinnature` extension [#408](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/408)

### `ImpEx` enhancements
- Rename `Impex` to its official name - `ImpEx` [#449](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/449)
- Added reference resolution/code completion for Item sub-types declared for individual value line [#425](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/425)
- Added reference validation for Item sub-types declared for individual value line [#426](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/426)
- Respect inline sub-type for header line parameters, see [ImpEx Header](https://help.sap.com/docs/SAP_COMMERCE/d0224eca81e249cb821f2cdf45a82ace/1c8f5bebdc6e434782ff0cfdb0ca1847.html?locale=en-US) [#457](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/457)
- Improved code completion for inline types and added new project-level settings [#460](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/460)
- If Type equal to Item sub-type it will be highlighted differently [#427](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/427)
- Improved code style for macro usages, distinguish `$config-` and reference to another macro [#421](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/421)
- Improved macro renaming, it will check for name collisions beforehand [#422](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/422)
- Improved macro usages [#424](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/424)
- Implemented inplace renaming for macros [#423](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/423)
- Added code completion of all available languages for `lang` modifier [#416](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/416)
- Added reference support for `lang` modifier value [#420](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/420)
- Added code completion and reference support for parameters of the `Collection` type [#453](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/453)
- Added code completion and reference support for parameters of the `Map` type [#455](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/455)
- Added `env.properties` support for project properties code completion [#419](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/419)
- Inject FlexibleSearch language into suitable macro declaration values [#433](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/433)
- Inject FlexibleSearch language into `SearchRestriction :: query` respecting `restrictedType` [#434](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/434)
- Inject complete `User Rights` block on code completion for `$START_USERRIGHTS` [#446](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/446)
- Inject space after mode keyword [#448](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/448)
- Inspection rule: validate that inline type for reference parameter extends its type [#458](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/458)
- Inspection rule: validate that inline type for reference parameter exists [#459](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/459)
- Inspection rule: validate that `lang` modifier value is present in the `lang.packs` [#417](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/417)
- Inspection rule: validate that `lang` modifier is used only for localized attributes [#418](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/418)
- Inspection rule: unique document id rule will report both DocId and reference qualifier [#463](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/463)

### `ImpEx` - `User Rights` 2.0
- SAP Help Portal - [User Rights](https://help.sap.com/docs/SAP_COMMERCE/50c996852b32456c96d3161a95544cdb/e472718cafe840c39fbb5ceb00002e52.html?locale=en-US)
- Re-implemented from the scratch Lexer and Parser for `User Rights` block [#435](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/435)
- Brand-new formatting model for `User Rights` block [#436](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/436)
- Added `Type System` related code completion and reference support for `Type` column [#437](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/437)
- Added `Type System` related code completion and reference support for `Target` column [#438](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/438)
- Added validation of the references to `Type System` [#439](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/439)
- Ignore case for header line parameters [#445](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/445)
- Added support of the inherited `.` permission identifier [#450](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/450)
- Added validation of the header parameter order [#451](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/451)
- Added possibility to skip `Password` column [#452](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/452)

### `FlexibleSearch` enhancements
- Added possibility to copy FlexibleSearch from the Java 15 text block `"""SELECT * FROM {Product}"""` [#428](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/428)
- Added FlexibleSearch language injection into Java 15 text block `"""SELECT * FROM {Product}"""` [#430](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/430)
- Resolve [y] column by table name if alias is not provided (enabled by default) [#444](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/444)
- Resolve `Link` relation ends for relation references [#464](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/464)
- Remove spaces around `.` and `:` characters [#442](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/442)
- Remove spaces before `,` character [#443](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/443)

### Fixes
- Check additional active plugin for java EE [#406](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/406)
- IDEA 2023.2: the expensive method should not be called inside the Highlighting Pass [#411](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/411)
- Flaky ProcessCanceledException during TS/BS files modification [#412](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/412)
- Incorrect resolution of the relation attributes in the `ImpEx` header line [#440](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/440)
- Improved code completion for ImpEx sub-types [#447](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/447)
- `ImpEx` Inspection rule for `lang` attribute will resolve value if it is a macro usage [#456](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/456)
- `ImpEx` alignment strategy is not file specific and fails in multithreading environment [#454](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/454)
- `ImpEx` inline type reference is not correctly validated [#461](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/461)
- Inject `FlexibleSearch` language only into `query` column of the `SearchRestriction` in `ImpEx` files [#441](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/441)
- Do not inject `FlexibleSearch` language into strings starting with `#%`  in `ImpEx` files [#462](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/462)

### Deprecated
- `TreeSpeedSearch` -> `TreeUIHelper` [#415](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/415)

### Other
- Migration: Java to Kotlin [#466](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/466)

## [2023.1.6]

### Features
- Project import/refresh will register `groovysrc` as the source directory [#402](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/402)
- Added navigate to the Dynamic Handler class for `dynamic` inlay hint on Ctrl + click [#396](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/396)
- Added possibility to Unify table alias separator for FlexibleSearch with a single click [#389](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/389)
- Added `Go to Declaration` action for Type System preview Tree [#384](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/384)
- Added `Go to Declaration` action for Type System preview Tables [#385](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/385)
- Added `Go to Declaration` action for Bean System preview Tree [#386](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/386)
- Added `Go to Declaration` action for Bean System preview Tables [#387](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/387)
- Added new FlexibleSearch folding settings [#383](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/383)
- Project import/refresh will register `kotlinsrc`/`kotlintestsrc` as the source directory [#403](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/403)
- For details on **Kotlin** integration with SAP Commerce see [Kotlin nature extension](https://github.com/mlytvyn/kotlinnature), as for now - registration of the Kotlin library has to be done manually via IDE

### `Polyglot Query Language` enhancements

- Integration with Type System (references, code completion)
- Code suggestions for localized attributes
- Code formatting
- Improved Lexer to support `order` keyword as type and attribute [#380](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/380)
- Project specific settings
- Added possibility to unify case of keywords [#381](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/381)
- Respect value of the `show language` flag [#382](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/382)
- Other changes [#373](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/373),
  [#374](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/374),
  [#375](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/375),
  [#376](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/376),
  [#377](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/377),
  [#378](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/378),
  [#379](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/379)

### `Type System preview` enhancements
- Improved synchronization of the Tree with Type System GlobalMetaModel
- Resolved "flickering" issue on any TypeSystem related changes
- Added possibility to remove Enum's values from the details pane
- Added possibility to remove Item's attributes/custom properties/indexes from the details pane
- Other changes [#392](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/392),
  [#395](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/395)

### `Bean System preview` enhancements
- Improved synchronization of the Tree with Bean System GlobalMetaModel [#391](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/391)
- Resolved "flickering" issue on any TypeSystem related changes
- Added possibility to remove Enum's values from the details pane
- Added possibility to remove Bean's properties/annotations/hints/imports from the details pane

### Fixes
- Suggest outer join `:o` after `]` symbol [#388](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/388)
- Adjusted Polyglot language Parser [#372](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/372)

### Other
- Improved FlexibleSearch color scheme [#390](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/390)
- Styled FlexibleSearch operation signs [#394](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/394)
- Respect FlexibleSearch settings responsible for table alias suggestions [#371](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/371)

## [2023.1.5]

### Features
- Added injection of the Groovy Language into `Script.content` in the ImpEx files [#361](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/361)
- Added injection of the Groovy Language into `script.type=groovy` in the Business Process files [#363](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/363)
- Added injection of the JavaScript Language into `script.type=javascript` in the Business Process files [#364](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/364)
- Added injection of the JavaScript Language into `Script.content` in the ImpEx files [#362](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/362)
- Improved support of the ImpEx nested attributes [#339](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/339)
- Improved FlexibleSearch value parameters code completion [#345](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/345)
- Improved FlexibleSearch & ImpEx code completion performance [#344](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/344)
- Improved FlexibleSearch & ImpEx Enum code completion and validation [#346](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/346)
- Improved FlexibleSearch & ImpEx Relation code completion and validation [#347](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/347)
- Added generate Diagram run line marker for Business Process [#331](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/331)
- Added generate Diagram run line marker for `items.xml`
- Added generate Diagram run line marker for `extensioninfo.xml` [#332](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/332)

### `FlexibleSearch` 2.0
- Rewritten from the Scratch
- Folding support
- Brand-new possibility to Unify case of the reserved words, change to upper or lowercase with a single click
- Added support of the multiline comment block `/**/`
- Added find usages for table & column aliases
- Added support of the localized attributes
- Improved inspection and validation of the FXS
- Improved Formatting, better table based formatting for multiline entities (eg, several joins)
- Improved Color Scheme
- Improved code completion
- Refactoring: rename of the table & column alias
- Project specific Settings pane
- FxS copied from the Java classes will be properly formatted
- Dozens of other improvements
- Changes [#349](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/349),
  [#350](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/350),
  [#351](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/351),
  [#352](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/352),
  [#357](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/357),
  [#368](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/368)

### `Polyglot Query Language` support
- Syntax highlighting
- Single and multi-line comments
- Several `GET` statements per file
- Braces matcher - opening and closing braces will be highlighted
- Custom configurable Color Schema
- Elements Folding
- Create new file via context menu
- Changes [#348](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/348),
  [#356](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/356),
  [#353](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/353)

### `ImpEx` enhancements
- Improved folding support of the nested attributes [#340](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/340)
- Added folding of the boolean `false` modifier [#341](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/341)
- Added folding of the Type modifiers [#342](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/342)
- Omit package name of the Class in case of the `jar:` property prefix [#343](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/343)
- Type attribute validation for complex header [#338](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/338)

### Fixes
- Navigate to Model will filter out non platform classes [#358](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/358)
- Non-navigable relation ends will be correctly resolved as `source` or `target` [#359](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/359)
- NPE for items file validation [#360](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/360)
- NPE during building CockpitNG [#367](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/367)

### Other
- Updated to Gradle 8.1 and added GitHub Actions [#337](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/337)

## [2023.1.4]

### Features
- Enabled Wizard-based Project Import from the Welcome Screen [#306](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/306)
- Added possibility to specify custom Project Icon, if not selected default one will be used [#323](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/323)
- Added navigation to the Extension declaration via Gutter icons for `localextensions.xml` [#320](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/320)
- Added navigation to the Extension declaration via Gutter icons for `extensioninfo.xml` [#321](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/321)
- Added completion and navigation for `typeCode` Interceptor property within the Spring XML file [#326](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/326)
- Added Project Refresh Action for not yet imported extensions declared as dependencies via `extensioninfo.xml` and `localextensions.xml` [#322](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/322)
- Improved support of the custom DOM files [#301](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/301)
- Enabled selection of the CCv2 modules during Project import [#303](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/303)
- Enabled possibility to disable validation of the generated classes [#313](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/313)
- Project import will respect extensions registered via `path` with `autoload=true` [#319](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/319)

### `items.xml` inspection rules
- Relation qualifier and modifiers must not be declared for `navigable='false'` end [#307](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/307)
- Only one side of many-to-many relation must be `navigable='false'` [#315](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/315)
- Qualifier must exist for `navigable='true'` part in many-to-many relation [#318](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/318)

### Fixes
- VirtualFile is null for ModelsUtils [#312](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/312)
- VirtualFile is null for BeansUtils [#317](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/317)
- Do not configure spring context for CCv2 modules [#316](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/316)

### Other
- Added Project icon for Plugin repository [#302](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/302)
- Replaced Caffeine cache with IDEA user data [#304](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/304)
- Updated Gradle plugin to 1.13.3
- Improved default XML values handling

## [2023.1.3]

### Fixes
- Cannot refresh CCv2 Project [#305](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/305)

## [2023.1.2]

### Features
- Added node type specific icons for Business Process code completion [#297](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/297)
- Added navigation to generated Item and Enum classes from the `items.xml` [#284](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/284)
- Added navigation to generated Enum Values fields from the `items.xml` [#286](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/286)
- Added "collapse all"/"expand all" actions for TS and BS views [#262](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/262)
- Added `sld.enabled` modifier support for ImpEx type [#290](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/290)
- Added Line Marker Provider settings and unified API usage [#294](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/294)
- Added navigation to Bean siblings in the `beans.xml` [#295](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/295)
- Improved PSI cache usage [#258](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/258)
- Improved performance of the Global Meta Model and TS Line Marker [#257](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/257)
- IDEA modules by default will be stored in the `/.idea/idea-modules` [#259](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/259)

### Introduced `Type System Diagram` 
- [Legend](https://github.com/epam/sap-commerce-intellij-idea-plugin/blob/main/docs%2FLEGEND_TYPE_SYSTEM_DIAGRAM.md)
- Initial version [#270](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/270)
- Added nodes removal &amp; Reset Exclusions Action [#271](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/271)
- Added Type Name exclusion Node Action [#282](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/282)
- Added Scope support [#272](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/272)
- Added Transitive Dependencies support [#273](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/273)
- Added PartOf edges support [#274](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/274)
- Added Dependencies edges support [#276](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/276)
- Added Node Collapse/Expand actions support [#278](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/278)
- Added possibility to show non-transitive non-Item custom types [#280](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/280)
- Added Legend MD file [#279](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/279)
- Enhanced tooltip content of the Node [#281](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/281)
- Improved Diagram settings and added Stop Type names [#277](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/277)
- Different header background color for Custom Type Nodes [#275](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/275)

### `Business Process Diagram` Improvements
- Added node properties [#267](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/267)
- Added edge coloring [#265](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/265)
- Added cycle edges coloring [#266](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/266)
- Added Context Parameters node [#287](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/287)
 
### `FlexibleSearch` inspection rules 
- `Item Type` is not defined [#264](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/264)
- `Attribute` is not defined

### `ImpEx` inspection rules
- Type modifier is unknown [#291](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/291)
- Attribute modifier is unknown [#292](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/292)

### `items.xml` inspection rules
  - Deployment tag must not be declared for one-to-many relation [#289](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/289)
  - Deployment table name must not exceed max length `deployment.tablename.maxlength` [#293](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/293)

### Fixes
- Console is not releasing Document on Project dispose [#260](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/260)
- Fixed Ant targets registration after Project Refresh
- Fixed Type System Item extends identification

### Other
- Improved cleanup on Project dispose
- Added registration of the latest Ant targets
- Added custom module icons
- Removed Business Process JAXB mapping
- Migrated Business Process Diagram generation to Kotlin
- Migrated Module Dependencies Diagram generation to Kotlin
- Updated to Kotlin 1.8
- Updated Gradle plugin to 1.13.2
- Removed custom `rt-ant` support, it did not work at all

## [2023.1.1]

### Features
- Added possibility to import CCv2 folders as modules [#238](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/238)
- Added more custom icons [#237](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/237)
- Added custom icons for CCv2 project tree modules [#239](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/239)
- Added completion and navigation for `items.xml` - `metatype` attribute [#242](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/242)
- Added inspection for `items.xml` - `metatype` attribute [#243](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/243)
- Added possibility to identify if extension is an addon (project refresh required) [#245](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/245)
- Improved Inlay hints for `dynamic` attributes [#250](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/250)
- CockpitNG: added `merge-by` dependant contribution for `parent` [#247](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/247)
- CockpitNG: added `merge-by` dependant complex contribution &amp; inspections for `parent` [#248](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/248)
- Updated custom icons for Business Process diagrams [#246](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/246)
- Show progress of the Type System Global Meta Model creation [#252](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/252)
- Show progress of the Bean System Global Meta Model creation [#253](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/253)
- Show progress of the CockpitNG Global Meta Model creation [#254](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/254)
- Improved project startup and shutdown, show `items.xml` validation progress [#255](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/255)

### Fixes
- Fixed code completion for `requires-extension` within `extensioninfo` [#244](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/244)

### Deprecated
- Decreased usage of the Deprecated API [#251](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/251)

## [2023.1.0]

### Features
- Compatibility adjustments [#195](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/195)
- Added CCv2 SAP Commerce `manifest.json` schema support [#225](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/225)
- Added CCv2 DataHub `manifest.json` schema support [#226](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/226)
- Added CCv2 Javascript Storefront `manifest.json` schema support [#227](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/227)
- Added completion of the available extensions for CCv2 SAP Commerce `manifest.json` file [#229](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/229)
- Added extension name validation Inspection for CCv2 SAP Commerce `manifest.json` file [#231](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/231)
- Added completion and validation for extension pack for CCv2 SAP Commerce `manifest.json` file [#234](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/234)
- Added completion and validation for template extension for CCv2 SAP Commerce `manifest.json` file [#235](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/235)
- Added possibility to group by Item parent for Type System preview [#211](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/211)
- Added completion and navigation for Index attributes within `items.xml` [#207](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/207)
- Added completion for meta tags within `extensioninfo.xml` [#230](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/230)
- Added listing of references for `id` attribute within Business Process definition [#214](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/214)
- Added listing of references for multiple Cockpitng definitions [#216](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/216)
- Added completion and navigation for `onError` attribute within Business Process definition [#215](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/215)
- Improved representation of the available extensions listing [#234](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/234)
- Improved project startup performance [#210](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/210)
- Improved Project Structure information notification content [#217](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/217)
- Improved code completion within `items.xml` [#221](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/221)
- Improved validation of properties within bean declaration to highlight duplicate properties [#220](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/220)
- Disable spell check for properties with custom named values [#228](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/228)

### Fixes
- ImpEx & FlexibleSearch actions always visible [#223](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/223)
- SAP Commerce Project specific Settings should be visible only for [y] projects [#206](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/206)
- Not all extension names shown in the code completion (project refresh required) [#232](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/232)

### Other
- Upgraded to Gradle 8 [#222](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/222)

## [2022.3.1]

### Features - IDEA Ultimate
- Added navigation to TypeCode Interceptor declaration(s) [#188](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/188)
- Added code completion and navigation for Cockpitng 'spring-bean' element [#184](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/184)
- Added highlighting if Spring bean used in Business Process is not available [#170](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/170)
- Register `-backoffice-spring.xml` files, Project re-import is required [#183](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/183)
- Introduced new `items.xml` Annotator for Attribute Handler [#117](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/117)

### Features
- Enabled IDEA capability to open new Project as SAP Commerce Project [#132](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/132)
- Introduced the Bean System Management - the powerful tool to observe SAP Commerce bean system [#76](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/76)
- Introduced new Debugger Java Type Renderers for Model classes [#123](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/123)
- Introduced automatic Plugin Update Checker [#127](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/127)
- Introduced flattened Types for Code Completion [#153](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/153)
- Moved Remote Instances under Project settings, as well as Active Remote Instance selection [#133](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/133)
- Filter out already defined extension dependencies in the completion for `extensioninfo.xml` [#150](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/150)
- Enabled [y] plugin settings sharing through [Settings Sync](https://www.jetbrains.com/help/idea/sharing-your-ide-settings.html#IDE_settings_sync) [#163](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/163)
- Redesigned and improved performance of the Structure View for `items.xml` files [#114](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/114)
- Show label for dynamic attribute during ImpEx code completion [#110](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/110)
- Inspection for `beans.xml` rely on whole Bean System, not only current file [#88](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/88)
- Regrouped [y] Application settings into separate sections [#130](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/130)
- New Remote Instance Wizard will prefill some data from [y] properties [#143](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/143)
- Decreases cognitive complexity for Code Completion and custom Icons [#152](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/152)
- Added completion for Relations and Enums for FXS files [#200](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/200)
- Added completion for `required-extension` under `extensioninfo.xml` [#149](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/149)
- Added possibility to change current Project settings [#131](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/131)
- Added possibility to preview preformatted FlexibleSearch Queries and copy them to Clipboard [#118](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/118)
- Added customized Structure View for `beans.xml` files [#113](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/113)
- Added navigation to `items.xml` Type attributes from generated classes [#120](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/120),
  [#71](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/71)
- Added navigation to `items.xml` Relation declaration from generated classes [#168](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/168)
- Added navigation to `items.xml` Enum values declaration from generated classes [#115](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/115)
- Added navigation to `beans.xml` Enum values declaration from generated classes [#111](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/111)
- Added navigation to `beans.xml` Bean property declaration from generated classes [#112](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/112)
- Added navigation to alternative declarations of the beans within `beans.xml` [#78](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/78)
- Added navigation to `items.xml` for Types used in the Cockpitng configuration files [#166](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/166)
- Added code completion for ImpEx `translator` modifier [#158](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/158)
- Added code completion for ImpEx `cell-decorator` modifier [#159](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/159)
- Added code completion and enriched navigation within Business Process [#171](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/171)
- Added code inspection for ImpEx: unknown type [#160](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/160)
- Added extra visibility filters for Type and Bean System views [#99](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/99)
- Added `dynamic` prefix getters/setters in the Java classes [#74](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/74)
- Added `-backoffice-config.xml` DOM model and custom Icon [#161](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/161)
- Added `-backoffice-widgets.xml` DOM model and custom Icon [#164](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/164)
- Added `localextensions.xml` DOM model and custom Icon [#155](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/155)
- Added `extensioninfo.xml` DOM model and custom Icon [#147](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/147)
- Added `process.xml` DOM model and custom Icon [#148](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/148)
- Added Cockpitng files (widgets, config, definition) support, Model, completion and navigation [#165](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/165),
[#174](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/174),
[#169](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/169),
[#172](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/172),
[#173](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/173),
[#175](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/175),
[#176](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/176),
[#177](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/177),
[#182](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/182),
[#178](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/178),
[#179](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/179),
[#180](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/180),
[#181](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/181),
[#185](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/185),
[#186](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/186)

### `localextensions.xml` inspection rules
- Unknown Extension declared as dependency [#156](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/156)

### `extensioninfo.xml` inspection rules
- Unknown Extension declared as dependency [#154](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/154)
- Dependency on the same Extension declared multiple times [#154](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/154)

### `beans.xml` inspection rules
  - Duplicate Enum definition inspection [#88](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/88)
  - Duplicate Enum Value definition inspection
  - Duplicate Bean Property definition inspection

### Fixes
- [y] Tool Window Logo too dark for New UI [#95](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/95)
- [y] Tool Window is not available after project import [#125](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/125)
- Wrong range in reference in ImpEx/FlexibleSearch files in case of header Type or Columns changes [#196](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/196)
- FlexibleSearch code completion should not be case-sensitive for attributes [#167](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/167)
- ImpEx config processor inspection does not work [#100](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/100)
- Process diagram layout shows actions in reverse order [#151](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/151)
- Focus is not propagated properly on Copy ImpEx/FlexibleSearch to Console action [#124](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/124)
- Generate Business Process Diagram action is not available [#96](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/96)
- Platform module should not be identified as Gradle project [#141](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/141)
- Consoles are not disposable [#142](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/142)
- Exception on Copy to console if triggered on file without extension [#157](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/157)
- Adjusted FlexibleSearch icon [#192](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/192)
- Default values for Type System modifiers

### Deprecated
- Decreased usage of the Deprecated API [#107](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/107),
[#106](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/106),
[#101](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/101),
[#94](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/94),
[#91](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/91),
[#89](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/89),
[#134](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/134),
[#135](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/135),
[#136](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/136),
[#137](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/137),
[#138](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/138),
[#139](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/139),
[#140](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/140),
[#83](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/83),
[#81](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/81),
[#146](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/146)

## [2022.3.0]

### Features
- Added 2022.3 Intellij IDEA support
- Introduction the Type System Management - the powerful tool to observe SAP Commerce type system [#62](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/62),
[#60](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/60)
- Added support of the Enum completion in ImpEx/FSQ
- Added support of the Relation completion in ImpEx/FSQ
- Added MapType support for `items.xml`
- Added postgresql DB mapping
- Added next `typecode` suggestion via `items.xml` Inspection
- Added possibility to change [y] `items.xml` Inspection levels (previously they were hardcoded in ruleset.xml) 
- Improved performance of the `items.xml` inspection
- Extended/refactored type system inspection rules
- Merged ToolsWindows "Hybris" (as Remote Instances) and "Hybris Console" (as Consoles) altogether with "Type system" into "Hybris" as a single entry point for all [y] related actions

### Deprecated
- Decreased usage of the Deprecated API

## [2022.2]

### Features
- Added 2022.2 Intellij IDEA support
- Feature Add inline actions for ImpEx & FlexibleSearch files [#53](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/53)
- Do not index `node_modules` directories [#52](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/52)
