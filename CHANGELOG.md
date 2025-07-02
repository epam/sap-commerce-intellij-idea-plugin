## [2025.1.5]

### `Access Control Lists` enhancements
- Introduced Grammar & Lexer for new ACL Language (`.acl`) [#1405](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1405)
- Support Copyright for `.acl` files [#1406](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1406)
- Introduced actions toolbar for `.acl` files [#1407](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1407)
- Added language commenter [#1408](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1408)
- Added language color light & dark schemes [#1409](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1409)
- Added language formatter [#1410](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1410)
- Added default file template [#1411](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1411)
- Added language folding support [#1412](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1412)
- Added reference resolution for `Type` column [#1413](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1413)
- Added reference resolution for `Target` column [#1414](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1414)
- Added code completion for common UserRights and Permission elements [#1415](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1415)
- Added colorization for Type row [#1416](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1416)
- Added references verification used in the `Type` and `Target` columns [#1417](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1417)
- Adjusted language Lexer [#1424](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1424)

### `ImpEx` enhancements
- Display notification panel about possible usage of the `Access Control Lists` language [#1418](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1418)
- Improved table range detection [#1419](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1419)

### `FlexibleSearch` enhancements
- Added API to retrieve item type and expression of the bind parameter [#1420](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1420)
- Improved code formatting for expressions within `WHERE` clause [#1421](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1421)
- Added the parameters panel for `FlexibleSearch` editor [#1425](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1425)
- Improved scroll for parameters panel and parameters representation [#1426](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1426)
- Added Date Picker for Date type parameters [#1429](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1429)
- Display an Inlay Hint for a bind parameter in `FlexibleSearch` editor [#1427](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1427)

### `Groovy` enhancements
- Adjusted execution mode selection `commit`/`rollback` [#1422](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1422)

### `Other` enhancements
- Added possibility to select Replica for HAC connection [#1423](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1423)

## [2025.1.4]

### `ImpEx` enhancements
- Introduced `Data Edit` mode per individual table statements per ImpEx file [#1392](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1392)
- Introduced ImpEx-like custom CSV Format for `Data Edit` mode [#1393](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1393)
- Added new configurable setting _First row is header_ for `Data Edit` mode [#1394](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1394)
- Added new configurable setting _Trim whitespace_ for `Data Edit` mode [#1395](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1395)
- Added folding for macros usages in the header line [#1401](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1401)
- Added code completion & reference resolution for User Rights `Type` column [#1402](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1402)

### `Access Control Lists` enhancements
- Create Type/attributes references only within `Target` column [#1400](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1400)

### `FlexibleSearch` enhancements
- Simplified _Copy to Clipboard_ action for injected FxS into Java classes [#1396](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1396)

### `Polyglot Query` enhancements
- Simplified _Copy to Clipboard_ action for injected PgQ into Java classes [#1397](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1397)
- Created new Language icon [#1398](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1398)

### `Java` enhancements
- Introduced `ysai` live template for Spring `@Autowired` declaration [#1382](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1382)
- Introduced `ysaqi` live template for Spring `@Autowired` + `@Qualifier` declaration [#1383](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1383)

### `Kotlin` enhancements
- Introduced `ysai` live template for Spring `@Autowired` declaration [#1384](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1384)
- Introduced `ysaqi` live template for Spring `@Autowired` + `@Qualifier` declaration [#1385](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1385)

### `Cockpit NG` enhancements
- Re-calculate Cockpit NG Meta Model on some file operations [#1399](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1399)

### `Spring` enhancements
- Fallback to `platform` module in case of Library class [#1403](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1403)

### Fixes
- Comply with _Companion object in extensions_ Plugin DevKit inspection [#1386](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1386)
- Comply with _Cancellation exception handled incorrectly_ Plugin DevKit inspection [#1388](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1388)
- Comply with _Incorrect parentDisposable parameter_ Plugin DevKit inspection [#1389](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1389)
- Comply with _Incorrect service retrieving_ Plugin DevKit inspection [#1390](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1390)
- Comply with _JComponent must use UiDataProvider_ Plugin DevKit inspection [#1391](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1391)

### Other
- Added first version of the Privacy Policy [#1387](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1387)

## [2025.1.3]

### `Project Import` enhancements
- Ensure that `[y] Remote debug` Run Configuration selected after first project import [#1355](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1355)
- Register various XSD Backoffice schemas during project import/refresh [#1359](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1359)

### `Meta System` enhancements
- TS & BS Meta Model 2.0 re-implemented using `StateFlow` [#1354](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1354)
- Respect Meta declarations within Libraries (not Module-aware) [#1358](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1358)

### `Type System` enhancements
- Added multi-resolve for `extends` for Item Types [#1343](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1343)
- Added multi-resolve for `type` for Relation Types [#1344](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1344)
- Added support of the `metatype` for Item Types [#1345](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1345)
- Added support of the `metatype` for Relation Elements [#1347](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1347)
- Addressed deadlock for circular extends of two Item Types [#1348](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1348)
- Reworked caching mechanism for Global TS Meta Model [#1351](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1351)

### `Bean System` enhancements
- Reworked caching mechanism for Global BS Meta Model [#1353](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1353)

### `Cockpit NG` enhancements
- Incorporated Global Cockpit NG Meta Model into new Meta evaluation approach [#1356](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1356)
- Added reference resolution & code contribution for various `i18n` attributes [#1357](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1357)

### `items.xml` inspection rules
- Item Type `metatype` must be a type extending ComposedType [#1346](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1346)
- Relation Element `metatype` must be a type extending RelationDescriptor [#1349](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1349)
- Ensure that inspection rules are using latest TypeSystem [#1352](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1352)

### `FlexibleSearch` enhancements
- Improved language Lexer to recognize closing `}`, `}}` tokens [#1379](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1379)

### `ImpEx` enhancements
- Enhanced Enum values reference resolution within `Enum` column [#1360](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1360)
- Added Item values reference resolution within `ComposedType` attribute columns [#1361](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1361)
- Resolve macro usages in the header parameter for attribute sub-type [#1362](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1362)
- Added code completion and reference resolution for `&DocId` usages [#1364](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1364)
- Added rename capabilities for Document ID `&DocId` elements [#1365](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1365)
- Show variants for Document ID `&DocId` usage [#1366](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1366)
- Added code completion and reference resolution within `ComposedType` _relation_ columns [#1367](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1367)
- Added code completion and reference resolution for values within nested `ComposedType` attribute columns [#1368](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1368)
- Added code completion and reference resolution within `ComposedType` _collection_ columns [#1369](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1369)
- Added support for custom collection delimiter to inject references within _collection_ columns [#1370](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1370)
- Added reference support for nested `ComposedType` attributes within _item_ columns [#1371](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1371)
- Added support for multiple Document ID `&DocId` references within _collection_ columns [#1372](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1372)
- Prefer macro references over `&DocId` or column type specific [#1373](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1373)
- Inject Java class references into `java.lang.Class` columns [#1374](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1374)
- Inject FlexibleSearch language into `AfterRetentionCleanupRule`:`itemFilterExpression` columns [#1375](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1375)
- Added possibility to apply custom Color Scheme for `Header Lines` [#1380](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1380)

### `Terminal` integration
- Added support of the `Terminal` plugin and set default directory to the project root for new Terminal Windows [#1342](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1342)

### `items.xml` inspection rules
- Removed "_Boolean field cannot be optional_" inspection rule [#1377](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1377)
- Ignore numbers in the "_Enum value must be uppercase_" inspection rule [#1378](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1378)

### Fixes
- Do not populate Tomcat JVM Properties for pure Unit Tests [#1350](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1350)
- Fixed ImpEx formatter for `item(CMSLinkComponent.$contentCV)` [#1363](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1363)
- Omit blank default values in the ImpEx default value collector [#1376](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1376)

## [2025.1.2]

### `Project Import` enhancements
- Added support of the Angular modules [#1337](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1337)

### `ImpEx` enhancements
- Allow "REMOVE" header for non-dynamic enums [#1335](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1335)
- Fix the message in quick-fix for non-dynamic enums in ImpEx files [#1336](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1336)

## [2025.1.1]

### Other
- Disable failing JRebelPlugin during build of the Searchable Options [#1325](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1325)
- Support configurable session cookie name for remote HAC connection [#1334](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1334)

## [2025.1.0]

### Other
- Compatibility release [#1324](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1324)

## [2024.3.8]

### `JUnit` enhancements
- Fix File Path Handling on Windows [#1339](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1339)

## [2024.3.7]

### `CCv2` enhancements
- Added Node.js version 22 to the CCv2 js-storefront manifest file [#1327](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1327)

### Fixes
- Fixed incorrect class import in the ImpEx Monitor [#1328](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1328)
- Load VM Properties no longer from the Properties File. Use instead the wrapper.conf files from the Tomcat Folder [#1330](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1330)
- Debugger is sometimes not attached correctly when Starting in Debug Mode [#1331](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1331)
- Fixed `test connection` for HAC and SOLR settings [#1333](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1333)

## [2024.3.6]

### `CCv2` enhancements
- Introduced possibility to schedule an auto-deployment of the Build [#1311](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1311)
- Allow downloading build logs for Builds in Unknown status [#1309](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1309)
- Aligned Build removal confirmation message with SAP's one [#1310](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1310)
- Enable `mode` & `strategy` only if `Deploy` is selected in the Build dialog [#1312](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1312)
- Added Help pointing to SAP Help Portal for both Deploy and Build dialogs [#1313](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1313)
- Allow re-deployment for environments in `failed` state [#1314](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1314)
- Interrupt deployment tracking in case of `fail` during the deployment [#1315](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1315)
- Do not hide confirmation notifications [#1316](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1316)
- Improved cancellation of the deployment tracking [#1317](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1317)
- Ensure build progress update during the warming-up phase [#1319](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1319)
- Improved deployment tracking [#1321](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1321)
- Improved deployment history representation per build [#1322](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1322)

### Other
- Adjusted dependencies used by Type/Bean/CNG MetaModels [#1318](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1318)

## [2024.3.5]

### `CCv2` enhancements
- Added possibility to Track progress of the Build [#1304](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1304)
- Show revision of the Build [#1305](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1305)
- Added possibility to Track progress of the Deployment [#1306](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1306)
- Added Deployment specific action to Track its progress [#1307](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1307)

### `JUnit Tests` enhancements
- For Pure JUnit Tests (annotated with `@UnitTest`) the JUnit Tests is no longer enhanced. It is also not necessary to enhance the Classpath if the `platformhome` is properly set. [#1302](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1302)

### `Type System` enhancements
- Added completion and navigation for `restrictionTypeCode` property of the `CMSRestrictionEvaluatorMapping` within the Spring XML file [#1300](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1300)

### Fixes
- Added read actions to build Type/Bean GlobalMetaModel [#1301](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1301)
- Improved read action usage during creation of the Type and Bean Systems [#1308](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1308)

## [2024.3.4]

### `Integrations` enhancements
- Added Windows WSL support [#1292](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1292)

### `ImpEx` enhancements
- Introduced enum value reference resolution in the Value Lines [#1296](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1296)
- Added enum value completion in the Value Lines [#1297](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1297)
- Added static enum value styling in the Value Lines [#1298](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1298)
- Label OOTB enum value completion options [#1299](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1299)

### `items.xml` inspection rules
- Enum values must be all uppercase (ignore `_`) [#1294](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1294)

### `ImpEx` inspection rules
- Respect no value in the key column for `No Unique Value` inspection rule [#1295](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1295)

## [2024.3.3]

### `CCv2` enhancements
- Unified UI elements [#1288](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1288)

### `Project Import` enhancements
- Added Windows WSL support [#1286](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1286)

### `Type System` enhancements
- Migrated the Type System Global Meta Model to coroutines [#1287](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1287)

### `Bean System` enhancements
- Migrated the Bean System Global Meta Model to coroutines [#1289](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1289)

### Fixes
- Fixed creation of the non-ssl remote configuration [#1290](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1290)

## [2024.3.2]

### `SAP CX Logging` enhancements
- Fixed the notification message when a server is NULL [#1253](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1253)
- Improved the notification type if the server responds with an error [#1254](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1254)
- Shorten a logger name in the notification message, i.e. converts each package segment to its first letter [#1272](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1272)
- Added logging support for enum, record and interface types [#1273](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1273)
- Removed logger actions from the Actions tab in the Search Everywhere popup [#1275](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1275)
- Added ALL and OFF modes for logging [#1277](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1277)
- Added the active HAC connection in the logging menu [#1281](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1281)

### `Other` enhancements
- Do not check for Ultimate plugins when using the Community edition during project import [#1276](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1276)
- Unified HAC actions API [#1282](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1282)

### Fixes
- Fixed running Integration Tests via IntelliJ JUnit [#1279](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1279)

## [2024.3.1]

### `Search Everywhere` enhancements
- Introduced possibility to search through Type System via _Search Everywhere_ [#1267](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1267)
- Added scope and preview for Type System Search Everywhere [#1269](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1269)
- Added possibility to search through Bean System via _Search Everywhere_ [#1270](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1270)
- Quick access new _Search Everywhere_ via shortcut `control` + `shift` + `H` [#1271](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1271)

### `ImpEx` enhancements
- Added **CronExp** language injection for **Trigger** type into String literal [#1256](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1256)
- Added **CronExp** language injection for **Trigger** type into non-String literal [#1257](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1257)
- Improved **Groovy** language injection for **Script** type [#1255](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1255)
- Properly resolve references to macro declaration [#1258](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1258)
- Cache macro declaration references [#1259](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1259)
- Use closest macro declaration value for folded macro usage [#1260](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1260)
- Use macros imported via `impex.includeExtenalData(Resource)` in the folded values [#1261](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1261)
- Resolve macros defined in the external ImpEx file and imported via `impex.includeExtenalData(Resource)` [#1262](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1262)
- Removed legacy macro reference search [#1264](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1264)
- Macro shouldn't resolve itself in own declaration [#1265](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1265)
- Rely on references during resolution of the folded values [#1266](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1266)

### `ImpEx` inspection rules
- Adjusted Unknown Macro inspection in case of external ImpEx files [#1263](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1263)

### Fixes
- Correctly define Scope to filter for Properties File [#1247](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1247)
- Add all module Classpaths and necessary Properties to JUnit Tests [#1241](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1241)
- Hide CCv2 actions in the _Search Everywhere_ [#1268](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1268)

### Other
- Replaced direct usage of the `actionPerformed` [#1274](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1274)

## [2024.3.0]

### `SAP CX Logging` enhancements
- Added an inlay hint to the logger popup, allowing users to select a log level and apply it to the active server [#1239](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1239)

### `Project Import` enhancements
- Ensure that dependencies on non-custom extensions are properly configured [#1244](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1244)

### `CCv2` enhancements
- Added support for the `enabledRepositories` for `js-storefront` [#1240](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1240)

### Other
- Added 2024.3 IntelliJ IDEA support [#1231](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1231)
- Updated libraries versions [#1235](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1235)
- MavenProjectBuilder API migration [#1237](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1237)
- Improved navigation to the file located in the virtual `Junk` directory [#1245](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1245)
- Added explicit dependency on a [JSON](https://plugins.jetbrains.com/plugin/25364-json) plugin [#1246](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1246)
- Added additional dependencies on a [JSON](https://plugins.jetbrains.com/plugin/25364-json) plugin [#1249](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1249)

### Deprecated
- Use `IDE` modality [#1232](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1232)
- Resource bundle fallback message [#1233](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1233)
- AnAction text in toolbar configuration [#1236](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1236)
- Omit usage of the internal Java Compiler API [#1242](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1242)
- Migrate Gradle project import API [#1243](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1243)

## [2024.2.4]

### `Type System` enhancements
- Cache item and attribute references [#1221](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1221)

### `Cockpit NG` enhancements
- Added folding for `na:notifications` [#1228](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1228)
- Added code completion and reference resolution for `wz:assign`:`property` [#1225](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1225)
- Improved resolution of the flow references [#1224](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1224)
- Improved resolution of the nested references to editors [#1222](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1222)
- Cache various references [#1220](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1220)

### `Bean System` enhancements
- Cache bean references [#1219](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1219)

### `OCC` enhancements
- Added reference resolution and code completion for nested properties [#1226](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1226)
- Improved resolution of the OCC level references [#1223](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1223)
- Highlight only unresolved references [#1216](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1216)
- Respect spaces in the property reference [#1217](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1217)
- Cache property references [#1218](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1218)
- Improved property reference resolution when it ends with `)` [#1227](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1227)

### `CCv2` enhancements
- Added possibility to specify alias for S-User [#1215](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1215)
- Use build icon for build details [#1214](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1214)
- Show build icon in the environments view per build [#1213](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1213)
- Open build details from the environments view [#1212](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1212)
- Copy Cloud Storage account name on click [#1211](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1211)

## [2024.2.3]

### `CCv2` enhancements
- Build status view options are not displayed [#1202](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1202)
- Show deployments as groups for the build [#1203](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1203)
- Show environments even if there is no response from the v1 API [#1208](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1208)
- Hide environment from the deployment list if there is no link to it [#1209](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1209)

### Other
- Enable K2 compiler [#1207](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1207)

## [2024.2.2]

### `CCv2` enhancements
- Added icon for build details deployment status [#1196](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1196)
- Added deployment date for build details [#1197](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1197)
- Unable to fetch CCv2 information if a user has any subscription without environment permission [#1199](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1199)

### `Type System` enhancements
- Added `String` to allowed types [#1195](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1195)
- Added folding for `core-advanced-deployment.xml` [#1198](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1198)

## [2024.2.1]

### `CCv2` enhancements
- Added build details sections to know more about the build [#1192](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1192)

### `Type System` enhancements
- Added dom model for `core-advanced-deployment.xml` [#1187](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1187)
- Added code completion and reference resolution for primitive types [#1188](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1188)
- Added code completion and reference resolution for additional object types [#1189](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1189)
- Added code completion and reference resolution for special types [#1190](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1190)

### Other
- Omit internal IntelliJ API usage in `ProjectBeforeCompilerTask` [#1186](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1186)

### Fixes
- Actions for CCv2 Build were not showing [#1191](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1191)

## [2024.2.0]

### `CCv2` enhancements
- Introduced possibility to restart a service replica [#1183](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1183)

### `ImpEx` enhancements
- Improved PSI operations performance [#1167](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1167)
- Migrated `Remove Column` to coroutine with progress [#1169](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1169)
- Migrated `Remove Table` to coroutine with progress [#1170](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1170)
- Migrated `Split Table` to coroutine with progress [#1171](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1171)
- Re-enabled `odd` | `even` value lines coloring [#1178](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1178)
- Cache value lines by header line [#1180](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1180)

### `Cockpit NG` enhancements
- Added `Spring EL` language injection into the body of the `y:shortLabel` tag [#1168](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1168)
- Added code completion for `cb:available-molds`:`default-mold` [#1175](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1175)
- Added code completion and reference resolution for `cb:mold`:`class` [#1176](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1176)
- Added code folding for `cb:available-molds`:`cb:mold` [#1177](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1177)

### `Type System` enhancements
- Improved resolution and navigation from Java to `items.xml` [#1162](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1162)

### `items.xml` inspection rules
- Enum values must be all uppercase [#1159](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1159), see [Java Enum Types](https://docs.oracle.com/javase/tutorial/java/javaOO/enum.html)

### Fixes
- Improved connection with remote `HAC` instances [#1182](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1182)
- Class initialization must not depend on services [#1158](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1158), [#1160](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1160)
- `What's New` is not shown when JCEF is not supported by the JDK [#1161](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1161)
- Moved actions from `EDT` to `BGT` [#1166](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1166)
- CCv2 actions should not be available outside the ToolWindow [#1172](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1172)
- Console statement is not executed on first time [#1181](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1181)

### Other
- Migrated to `IntelliJ Platform Gradle Plugin 2.0` [#1184](https://github.com/epam/sap-commerce-intellij-idea-plugin/issues/1184)
- Improved resolution of the XML Dom based files [#1164](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1164)
- Dropped legacy `items.xml` XML based validation [#1157](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1157)
- Added 2024.2 IntelliJ IDEA support [#1156](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1156)
- Declare explicit dependency on JBR 21 [#1163](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1163)
- Added dependency on Foojay Toolchains Plugin [#1165](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1165)

## [2024.1.3]

### `Project Import` enhancements
- Do not group external modules by default [#1152](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1152)

### `Project Build` enhancements
- Generate `models.jar` for JUnit Run Configuration only when needed [#1128](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1128)

### `CCv2` enhancements
- Introduced possibility to specify access token per subscription [#1135](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1135)
- Show modified time in the Service view [#1127](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1127)
- Show Green deployment information in the Service view [#1129](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1129)
- Show Initial Passwords in the `hcs_admin` Service view [#1130](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1130)
- Added possibility to re-fetch Service details [#1132](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1132)
- List environment specific data backups [#1136](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1136)
- Updated JSON-schema for js-storefront manifest.json to reflect changes from SAP documentation [#1154](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1154)

### `Cockpit NG` enhancements
- Cache widget stub reference [#1140](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1140)
- Cache widget stub reference [#1141](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1141)
- Cache flow type system item reference [#1142](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1142)
- Cache widget definition reference [#1143](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1143)
- Cache widget reference [#1144](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1144)
- Cache initialize property reference [#1145](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1145)
- Cache editor definition reference [#1146](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1146)
- Cache action definition reference [#1147](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1147)

### `ImpEx` enhancements
- Cache document id reference [#1148](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1148)
- Cache header abbreviation reference [#1149](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1149)
- Cache java enum value reference [#1150](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1150)
- Cache java class reference [#1151](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1151)

### `OCC` enhancements
- Cache bean property reference [#1138](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1138)
- Cache level mapping reference [#1139](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1139)

### `items.xml` enhancements
- Added reference resolution for index `include`:`attribute` [#1134](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1134)

### Other
- Migrated to `IntelliJ Platform Gradle Plugin 2.beta8` [#1155](https://github.com/epam/sap-commerce-intellij-idea-plugin/issues/1155)
- Migrated to `IntelliJ Platform Gradle Plugin 2.beta5` [#1137](https://github.com/epam/sap-commerce-intellij-idea-plugin/issues/1137)
- Migrated to `IntelliJ Platform Gradle Plugin 2.beta3` [#1133](https://github.com/epam/sap-commerce-intellij-idea-plugin/issues/1133)
- Migrated to `IntelliJ Platform Gradle Plugin 2.beta2` [#1124](https://github.com/epam/sap-commerce-intellij-idea-plugin/issues/1124)
- Show once `CHANGELOG.md` in a new `What's New` tab once per version [#1125](https://github.com/epam/sap-commerce-intellij-idea-plugin/issues/1125)
- Show bundled `What's New` tab with fallback to GitHub page [#1131](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1131)
- Updated gradle wrapper to 8.7 [#1126](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1126)

## [2024.1.2]

### `CCv2` enhancements
- Removed `SAP CCM` based integration [#1098](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1098)
- Do not retrieve build for environments with `UNDEPLOYED` and `UNKNOWN` deployment status [#1101](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1101)
- Integrated with CCv2 API V1 to show Dynatrace link [#1102](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1102)
- Show OpenSearch link for each environment [#1103](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1103)
- Show the number of problems for each environment [#1104](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1104)
- Added possibility to download Build Logs [#1105](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1105)
- Filter out Builds by Status [#1106](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1106)
- Point OpenSearch link to `Discover` view [#1107](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1107)
- Added a browser link for Environment name [#1108](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1108)
- Sort environments by type and name [#1109](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1109)
- Added a browser link for Build name [#1110](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1110)
- Added a browser link for Deployment code [#1111](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1111)
- Filter out Environments by Status [#1112](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1112)
- Use `CCv1` api to get subscription permissions [#1113](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1113)
- Show environment details in a new tab [#1114](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1114)
- List Cloud Storages available for an environment [#1115](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1115)
- Show build information for an environment [#1116](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1116)
- List services for an environment [#1117](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1117)
- Cache list of services for an environment [#1118](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1118)
- Added possibility to re-fetch environment [#1119](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1119)
- Added a browser link for Cloud Storage name [#1120](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1120)
- Show basic Environment Service details [#1121](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1121)
- Retrieve and show Service customer properties [#1122](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1122)

### `Project Import` enhancements
- Loading backoffice sources provided by SAP on project import [#1096](https://github.com/epam/sap-commerce-intellij-idea-plugin/issues/1096)

### Other
- Migrated to `IntelliJ Platform Gradle Plugin 2.beta1` [#1099](https://github.com/epam/sap-commerce-intellij-idea-plugin/issues/1099)

## [2024.1.1]

### `Project Import` enhancements
- Persist DB password forever during auto-configuration [#1046](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1046)
- Unescape DB url during auto-configuration [#1047](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1047)

### `CCv2` enhancements
- Brand new `CCv2` tool window for integration with Cloud Portal CI/CD
- Added possibility to specify directory of the `SAP CCM` CLI [#1048](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1048)
- Added possibility to set developer-specific `SAP CCM` CLI token in the secure storage [#1049](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1049)
- Added possibility to set developer-specific and project-aware CCv2 subscription [#1058](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1058)
- Added possibility to set CCv2 subscriptions to be used with `SAP CCM` CLI [#1050](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1050)
- Added possibility to **schedule** new build [#1060](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1060)
- Added possibility to **redo** existing build [#1061](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1061)
- Added possibility to **delete** existing build [#1062](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1062)
- Show cloud icon for CCv2 subscription [#1051](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1051)
- Fetch and show CCv2 environments details [#1055](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1055)
- Fetch and show CCv2 builds details [#1057](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1057)
- Fetch and show CCv2 deployments details [#1064](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1064)
- Added timeout configuration for `SAP CCM` [#1056](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1056)
- Improved visual representation of the environments and builds [#1059](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1059)
- Open CCv2 settings pane from the toolbar [#1063](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1063)
- Sync CCv2 subscriptions changes for all open projects [#1065](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1065)
- Enhanced CCv2 deployments view [#1066](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1066)
- Notify user on CCv2 settings validation errors [#1068](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1068)
- Migrated `SAP CCM` logic to Kotlin coroutines [#1069](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1069)
- Introduced `Native` integration via OpenAPI [#1070](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1070)
- Added possibility to change Integration Protocol and current subscription [#1071](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1071)
- Fetch CCv2 builds via `Native` integration [#1072](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1072)
- Fetch CCv2 deployments via `Native` integration [#1073](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1073)
- Schedule new build via `Native` integration [#1074](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1074)
- Delete existing build via `Native` integration [#1075](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1075)
- Schedule deployment for existing build [#1076](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1076)
- Allow blank subscription for new build Dialog [#1079](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1079)
- List only permitted environments in the Deployment Dialog (`Native` integration) [#1087](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1087)
- Show current build for environment (`Native` integration) [#1088](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1088)
- Hide current build for environment (`CCM` integration) [#1090](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1090)
- Use BGT for CCv2 Open Settings actions [#1089](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1089)
- Added CCv2 api specs yaml file [#1095](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1095), approved to be used by SAP in the [412082/2024](https://me.sap.com/case/002075129500004120822024)

### `Type System` enhancements
- Show Item line marker for jalo classes [#1067](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1067)
- Show parent type for Enum in the completion list [#1080](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1080)

### `Cockpit NG` enhancements
- Added code completion for various `merge-mode` attributes [#1022](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1022)
- Ignore case of the `merge-mode` attribute [#1023](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1023)
- Improved xsd mapping for `ea:attribute` [#1024](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1024)
- Added code completion for `lv:column`:`class` [#1028](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1028)
- Added code completion for `sv`:`custom-section`|`custom-attribute`:`class` [#1029](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1029)
- Added code completion for `ea`:`customPanel`|`customTab`|`customSection`:`class` [#1030](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1030)
- Added code completion and reference resolution for `ss`:`sort-field`:`name` [#1033](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1033)
- Added code completion and reference resolution for `as`:`sort-field`:`name` [#1034](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1034)
- Added reference resolution for `y`:`labels`:`beanId` [#1035](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1035)
- Added `Spring EL` language injection into the `y:preview`:`urlQualifier` attribute [#1036](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1036)
- Added `Spring EL` language injection into the `y:preview`:`description` tag body [#1037](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1037)
- Added `Spring EL` reference resolution in the `visible` attribute based on `template-bean` [#1086](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1086)
- Added reference resolution and code completion for nested references [#1038](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1038)
- Added reference resolution and code completion for fields referenced by `template-bean` [#1085](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1085)

### `Module Dependencies Diagram` enhancements
- Added possibility to display extension details within the Module node [#1041](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1041)
- Added nodes colorization based on the extension type [#1042](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1042)
- Added edges colorization based on the extension type [#1043](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1043)
- Show additional extension details within the Module node [#1044](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1044)

### `Groovy` enhancements
- Actions toolbar for disabled for Groovy IDE Scripts by default [#1040](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1040)

### `Bean System` enhancements
- Show bean-type-specific inline navigable icon [#1045](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1045)

### `ImpEx` enhancements
- Parse and style field value prefix `model://` [#1077](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1077)

### Fixes
- Configure SAP CX JavaDoc for OOTB Modules [#1027](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1027) for Issue [1026](https://github.com/epam/sap-commerce-intellij-idea-plugin/issues/1026)
- Removed dependency on Kotlin PSI utils methods [#1053](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1053) for Issue [1052](https://github.com/epam/sap-commerce-intellij-idea-plugin/issues/1052)
- Fix Path separator to enable Model Item Declaration icons and navigation on Windows [#1054](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1054)
- Adjusted FlexibleSearch language injection [#1081](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1081)
- Fix import with custom extension directory override [#1084](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1084)
- Fix readonly problem for custom extension in a custom path [#1091](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1091)
- Fix navigation to Enum values declared in the `beans.xml` [#1092](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1092)

### Other
- Added JetBrains Marketplace error reporting [#1039](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1027) for Issue [1026](https://github.com/epam/sap-commerce-intellij-idea-plugin/issues/1039)
- Retrieve SOLR Cores view background process [#1082](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1082)

## [2024.1.0]

### Features
- Group all non-hybris gradle, ant, eclipse and maven modules in project view panel [#967](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/967) | [#975](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/975) | [#980](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/980)
- Use blue logo for custom module groups in the project view [#973](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/973)
- Show globe icon for localized attributes in the code completion pane [#999](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/999)
- Added custom icon for `hybrislicence.jar` [#1000](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1000)

### `CCv2` enhancements
- Added new extension pack `hybris-datahub-integration-suite` [#1013](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1009)

### `items.xml` enhancements
- Improved folding, show '!' for mandatory properties and relations [#1009](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1009) | [#1010](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1010)

### `Remote Connection` enhancements
- Introduced `Project` and `Personal` scope for remote connections [#971](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/971)
- Redesign HAC connection settings and rely on `PasswordSafe` to store passwords [#968](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/968) | [#986](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/986) | [#987](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/987)
- Redesign SOLR connection settings and rely on `PasswordSafe` to store passwords [#971](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/971) | [#985](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/985)
- Focus connection name field on dialog show [#972](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/972)
- Store connection credentials in the [Sensitive Storage](https://plugins.jetbrains.com/docs/intellij/persisting-sensitive-data.html#storage) [#988](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/988) | [#995](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/995)
- Show inline test HAC connection results in the dialog [#989](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/989)
- Show inline test SOLR connection results and unify both dialogs [#990](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/990)
- Show current host details for each request in the Console [#996](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/996)

### `Project Import` enhancements
- Redesigned `check required plugins` import step [#983](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/983)
- Added web links to missing plugins on `check required plugins` import step [#984](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/984)
- Update platform and javadoc version on project refresh [#991](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/991)

### `Project Build` enhancements
- Added ability to change the output directory for custom modules from `classes` to `eclipsebin` [#1012](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1012)

### `ImpEx` enhancements
- Inject `FlexibleSearch` language into suitable ImpExValue elements [#992](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/992) | [#994](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/994)
- Improved reference resolution for `Document ID` [#997](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/997) | [#1001](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1001)
- Added find usages for `Document ID` [#997](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/997)
- Added inline renaming capabilities for `Document ID` [#997](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/997)
- Temporary disabled `odd` / `even` lines coloring as it affect performance [#1004](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1004)

### `FlexibleSearch` enhancements
- Improved language parser to support bind parameters in the `IN` expression [#993](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/993)

### `Cockpit NG` enhancements
- Inject spring bean reference into `n:dynamic-node`:`populator-bean-id` [#1006](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1006)
- Register `backoffice-widgets.xml` as `Widgets` [#1014](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1014)
- Register `http://www.hybris.com/schema/cockpitng/widgets.xsd` schema during project import [#1015](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1015)

### `items.xml` inspection rules
- Show details for attribute type validation message [#1007](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1007)
- Show details for various validation messages [#1008](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1008)

### Other
- IntelliJ IDEA API compatibility [#966](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/966) | [#998](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/998)
- Updated gradle wrapper to 8.5 [#969](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/969)
- Redesigned SAP Commerce facet dialog layout [#974](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/974)
- Slow operation in the After Compiler Task [#1003](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1003)

### Fixes
- IDE error when running FlexibleSearch query [#976](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/976)
- SOLR connection settings are not properly changed [#977](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/977)
- Fixed code generation on Windows [#979](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/979)
- Fixed request param cast issue for ImpEx execution [#1002](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1002)
- Fixed duplicated remote connections settings [#1005](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1005)

## [2023.3.4]

### Features
- Pre-load project properties on project startup [#957](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/957)

### `Project Import` enhancements
- Added possibility to exclude directories from the project scanning during the Project Refresh [#962](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/962)
- Added possibility to exclude directories from the project scanning during the Project Import [#964](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/964)

### `Project Build` enhancements
- Make code generation timeout configurable [#954](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/954)
- Do not unlink gradle projects during the project refresh [#956](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/956)
- Changed output directory for custom modules to `classes` [#958](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/958)
- Re-generate `jrebel.xml` on project refresh [#960](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/960)

### Other
- Updated project libraries [#959](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/959)

### Fixes
- `AntInstallation` class not found on project refresh [#955](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/955)
- Fixed Kotlin library automatic registration [#961](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/961)

## [2023.3.3]

### Features
- Added `extends` information for Type System Item completion results [#892](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/892)
- Moved project settings to developer-specific settings [#935](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/935)
- Unified consoles execution settings visual representation [#942](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/942)
- Omit slow operations for simple Spring bean resolution in a Community edition [#949](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/949)

### `SAP CX Run Configuration` Feature
- SAP CX Run Configuration for simple Starting and Debugging SAP CX Platform [#936](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/936)

### `Project Build` enhancements
- Re-generate the code and `models.jar` before the `Build` -> `Rebuild Project` [#906](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/906)
- Added configurable setting to toggle automatic re-generation of the code [#907](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/907)
- Clean `modelclasses` and `gensrc` directories before the code generation [#909](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/909)
- Trigger file system change after the code generation [#910](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/910)
- Trigger code generation even if there are modules of various types [#911](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/911)
- Trigger file system change if code generation failed [#919](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/919)
- Generate code with `UTF-8` encoding charset [#945](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/945)

### `Project Import` enhancements
- Added autoconfiguration of the JRebel Facet for custom extensions [#869](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/869)
- Omit slow operations after project import [#871](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/871)
- Do not remove custom third-party modules during the `Project Refresh` [#948](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/948)
- Do not modify hybris directory during the `Project Refresh` [#950](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/950)

### `Tests` enhancements
- Add JDK Export Properties to JUnit Tests to run Integration Tests [#864](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/864)
- Load Properties from Files and Environment [#864](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/864)

### `External Dependencies` enhancements
- Automatically download missing external dependencies via `Ant` [#876](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/876)

### `Ant` enhancements
- Allow common targets for custom extensions [#873](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/873)

### `Cockpit NG` enhancements
- Added code completion for Editor wrapping types [#925](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/925)
- Improved Editor wrapping type text range identification [#926](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/926)
- Respect all fields of the DTO as a type [#927](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/927)
- Add a type of the field in code completion for DTO as a type [#928](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/928)
- Added code completion and reference resolution for full text search [#943](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/943)
- Added code folding for `navigation-node` element [#944](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/944)
- Added `Spring EL` language injection into the body of the `y:label` tag [#946](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/946)
- Added `Spring EL` language injection into the `visible` attribute of the wizard tags [#947](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/947)

### `ImpEx` enhancements
- Added `disable.interceptor.types` type modifier [#889](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/889)
- Added `disable.interceptor.beans` type modifier [#890](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/890)
- Added `disable.UniqueAttributesValidator.for.types` type modifier [#891](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/891)
- Added reference resolution for not quoted value of the `disable.interceptor.beans` [#893](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/893)
- Added reference resolution for not quoted value of the `disable.interceptor.types` [#894](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/894)
- Added reference resolution for not quoted value of the `disable.UniqueAttributesValidator.for.types` [#895](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/895)
- Customized folding for `User Rights` permission flags [#897](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/897)
- Improved **Groovy** language injection based on control markers [#912](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/912)
- Prepend default imports for injected **Groovy** language [#913](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/913)
- Inject `impex` variable type of `ImpExImportReader` into **Groovy** language [#914](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/914)
- Inject `line` variable type of `Map<Integer, String>` into **Groovy** language [#915](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/915)
- Parse and style field values `jar:`, `file:` and `zip:` [#918](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/918)
- Parse and style field value `http:` [#929](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/929)
- Parse and style field value `/medias/` for exploded jar [#930](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/930)
- Parse and style password encoding prefixes as field value [#931](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/931)
- Suggest the next available type code when there is no value [#923](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/923)
- Improved code completion for `translator` modifier [#932](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/932)
- Added special translator modifiers `expr`, `version`, `system`, `class` [#933](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/933)
- Removed a catalog version option from the ImpEx console [#934](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/934)
- Enhanced Parser and Lexer with single line scripting `#%` elements [#937](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/937)
- Style scripting action elements `beforeEach:`, `afterEach:`, `if:` and `endif:` [#938](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/938)
- Inject **Groovy** language into a single line scripting block [#939](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/939)
- Style scripting marker and action within multi-line block [#940](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/940)
- Enhanced ImpEx Console with additional execution settings [#941](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/941)
- Improved language Lexer for scripting blocks [#951](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/951)

### `ImpEx` inspection rules
- Inspect type set for `disable.UniqueAttributesValidator.for.types` type modifier [#896](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/896)
- Use inverted comma for attribute modifiers [#899](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/899)
- Inspect a value set for `processor` modifier [#900](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/900)
- Inspect a value set for `translator` modifier [#901](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/901)
- Inspect a value set for `cellDecorator` modifier [#902](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/902)
- Inspect a value set for `mode` modifier [#903](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/903)
- Inspect a value set for `disable.interceptor.types` modifier [#916](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/916)
- Inspect a value set for modifier of a boolean type [#904](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/904)
- Disable `Unique attribute without an index` for `pk` attribute [#905](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/905)
- Allow various Translators for `translator` modifier [#917](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/917)

### `FlexibleSearch` enhancements
- Improved `words case` and `table alias` actions performance [#874](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/874)

### `Polyglot Query` enhancements
- Improved `words case` action performance [#875](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/875)

### `Business Process` enhancements
- `ScriptAction` should not expect `bean` attribute [#881](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/881)

### `Groovy` enhancements
- Disable actions toolbar for the test `.groovy` files located in the `groovytestsrc` folder [#883](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/883)

### `items.xml` enhancements
- Added groups icons in the Structure view [#865](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/865)
- Added alternative dynamic attribute handler Spring bean resolution as Gutter icon [#886](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/886)

### `items.xml` inspection rules
- Inspect deployment code according to `reservedTypecodes.txt` [#922](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/922)

### `beans.xml` enhancements
- Added inlay hints for enum values [#920](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/920)

### `Spring Framework` enhancements
- Improved Spring bean resolution in IntelliJ IDEA Ultimate [#887](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/887)
- Improved Spring bean resolution in IntelliJ IDEA Community [#888](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/888)

### Other
- Incorrect `companion` object usage [#866](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/866)
- Updated Slack invitation link [#868](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/868)
- Removed SOLR and FlexibleSearch queries persistence in the Consoles [#870](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/870)
- Use Plugin's icon and menubar title in case of macOS [#872](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/872) | [#878](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/878)
- Use Plugin's icon and menubar title in case of macOS [#872](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/872) | [#880](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/880)
- Removed no longer working Maven Javadoc and Source downloading [#885](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/885)
- Updated icons usage [#921](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/921)
- Improved performance by adding properties files aware cache for properties [#924](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/924)

### Fixes
- Code styles fixes [#867](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/867)
- Changed Maven plugin id from `maven` to `org.jetbrains.idea.maven` [#879](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/879)
- FlexibleSearch is not injected into Kotlin simple string literals [#882](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/882)
- IntelliLang pattern fix applied multiple times on each project re-opening during the same session [#884](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/884)
- `Select ImpEx Statement` removes User Rights block [#898](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/898)
- Fixed `items.xml` Enum value inlay icon when value is not yet generated [#908](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/908)

## [2023.3.2]

### `Project Import` enhancements
- Do not scan `hidden` directories for extensions [#862](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/862)

### `ImpEx` enhancements
- Inject `ImpEx` language into `header` property of the `DefaultImpexConverter` spring bean declaration [#858](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/858)
- Inject `ImpEx` language into `header` property of the `abstractDefaultImpexConverter` spring bean declaration [#859](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/859)

### Other
- Made `java`, `properties` and `xdebugger` mandatory dependencies [#855](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/855)
- Updated [Code of Conduct](CODE_OF_CONDUCT.md) [#854](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/854)
- Execute IntelliLang Startup Activity in smart mode [#856](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/856)

### Fixes
- Windows: Problems with Type System `items.xml` - Not Yet Generated Message [#860](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/860)

## [2023.3.1]

### Features
- Inject `Java` language into the `items.xml` custom properties value tags [#849](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/849)

### `Groovy` enhancements
- Added shortcut `control + shift + alt + enter` for execute Groovy Script with commit action [#842](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/842)
- Added shortcut `shift + alt + enter` for execute Groovy Script action [#843](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/843)

### `Polyglot Query` enhancements
- Added shortcut `shift + alt + enter` for execute Polyglot Query action [#841](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/841)

### `FlexibleSearch` enhancements
- Added shortcut `shift + alt + enter` for execute FlexibleSearch Query action [#840](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/840)

### `ImpEx` enhancements
- Added shortcut `control + shift + alt + backspace` for remove table action [#836](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/836)
- Added shortcut `control + shift + alt + backslash` for split table action [#837](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/837)
- Added shortcuts `control + shift + alt` + `,` | `.` for move left | right column actions [#838](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/838)
- Added shortcut `shift + alt + enter` for execute ImpEx action [#839](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/839)
- Added shortcut `control + alt + V` for Validate ImpEx action [#844](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/844)
- Added shortcut `control + shift + alt + O` for Open ImpEx action [#845](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/845)
- Omit slow operation on Column Highlighting under the caret [#847](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/847)
- Enable column actions on value groups with whitespaces [#848](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/848)

### Other
- Adjust optional dependency on `AntPlugin` during project refresh [#833](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/833)
- Invoke later slow operation `ImpEx Column header name Highlight` [#834](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/834)
- Invoke later slow operation `ImpEx Column values Highlight` [#835](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/835)
- Omit slow operation on `HybrisWritingAccessProvider` [#846](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/846)
- Added custom icon for `buildcallbacks.xml` file [#850](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/850)
- Simplify `HybrisWritingAccessProvider` logic due slow operations [#851](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/851)

## [2023.3.0]

### Other
- Updated plugin dependencies for IntelliJ IDEA 2023.3 [#825](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/825)
- Execute `intelliLang` startup activity on background read thread [#829](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/829)

### Deprecated
- Migrate from `lang` to `lang3` [#826](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/826)
- Migrate `ToolWindowFactory` API [#827](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/827)
- Migrate `AnAction` API [#828](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/828)

## [2023.2.13]

### Features
- Rely on `intelliLang` plugin to inject `FlexibleSearch` and `Polyglot Query` languages into the Java code [#817](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/817)
- Inject `FlexibleSearch` language into the `FlexibleSearchQuery` and its descendants in Java code [#819](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/819)
- Added navigation to the `items.xml` Ordering Attribute from the generated classes [#818](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/818)

### `Project Import` enhancements
- Register `? - Web Classes` library only for non-custom modules [#820](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/820)
- Register `acceleratoraddon` module dependencies on other `acceleratoraddon` modules [#821](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/821)
- Register `commonwebsrc` and `addonsrc` as generated sources for modifiable web modules [#822](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/822)
- Register `src` dirs for `platformservices` module [#823](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/823)

### `Polyglot Query` enhancements
- Added possibility to preview preformatted Polyglot Queries and copy them to Clipboard [#810](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/810)
- Inject Polyglot Query Language into `ImpEx` string literals [#811](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/811)
- Inject Polyglot Query Language into `Java` string literals [#812](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/812)
- Inject Polyglot Query Language into `Kotlin` string literals [#813](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/813)

### `Cockpit NG` enhancements
- Added Spring bean reference resolution for `wz:custom`:`handler` [#809](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/809)

### `CCv2` enhancements
- Updated commerce and js-storefront manifest [#816](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/816)

### Fixes
- NPE on copy to console [#815](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/815)

## [2023.2.12]

### `Project Import` enhancements
- Auto-registration of the XSD namespace for `action-definition.xsd` [#806](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/806)
- Auto-registration of the XSD namespace for `widget-definition.xsd` [#805](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/805)
- Auto-registration of the XSD namespace for `editor-definition.xsd` [#804](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/804)

### `ImpEx` enhancements
- Added colorization for `odd` and `even` user rights value lines [#802](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/802)

### Fixes
- Fixed type system Item's `extends` identification [#803](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/803)

## [2023.2.11]

### `Project Import` enhancements
- Introduced customization of the XSD schema for `y` namespace [#786](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/786)
- Improved classpath configuration for OOTB modules and readonly mode [#783](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/783)
- Adjusted `platformservices` source directories in readonly mode [#792](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/792)
- Register `web.addonsrc` as source directories for `?.web - Web Classes` library [#796](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/796)
- Ignore `addontestsrc` directory for `web` sub-module [#797](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/797)
- Ignore only `testclasses` directory for `web` sub-module [#798](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/798)

### `Cockpit NG` enhancements
- Extend config schema namespace with `editors` type [#784](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/784)
- Added code completion and reference resolution for `y:property` : `editor` [#787](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/787)
- Added code completion and reference resolution for `cv:attribute` : `editor` [#788](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/788)
- Added code completion and reference resolution for `fs:field` : `editor` [#790](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/790)
- Added code completion and reference resolution for `cv:attribute` : `qualifier` [#789](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/789)

### `Kotlin` enhancements
- Automatically configure `KotlinJavaRuntime` and register it for dependant modules [#793](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/793)
- Introduced `ysri` live template for Spring `@Resource` declaration [#780](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/780)
- Adjusted `log4j2` live template [#781](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/781)
- Adjusted `slf4j` live template [#782](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/782)

### `OCC` enhancements
- Exclude nested properties from the reference resolution [#799](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/799)

### Other
- Allow modification of the non-project root files [#794](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/794)
- Adjust type system merge for `Product` type (it extends both `GenericItem` and `LocalizableItem`) [#785](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/785)

### Fixes
- OOTB `commonweb` sub-modules are not re-imported on project refresh [#795](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/795)

## [2023.2.10]

### `Project Import` enhancements
- Register web spring files available due `ext.?.extension.webmodule.webroot` property [#776](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/776)
- Do not register SAP javadocs for custom extensions [#763](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/763)
- Use custom library names for all custom libraries [#764](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/764)
- Ignore non-`[y]` project roots if they will affect project import [#774](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/774)

### `items.xml` inspection rules
- Improved detailed message for `TSDeploymentTableMustNotBeRedeclaredInChildTypes` [#772](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/772)

### `ImpEx` enhancements
- Use single space after Header Mode [#761](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/761)
- Added shortcut `control + alt + backspace` for remove column action [#775](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/775)

### `OCC` enhancements
- Added code completion `dtoClass` Level Mapping tags [#777](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/777)

### `Cockpit NG` enhancements
- Improved detection of the `config` files [#760](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/760)
- Improved detection of the `widgets` files [#759](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/759)
- Added code completion and reference resolution for `wz:property` : `editor` [#769](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/769)

### Other
- Upgraded Gradle `8.2.1` -> `8.4` [#758](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/758)
- Make the Plugin incompatible with other plugins [#757](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/757)
- Migrate to [Light Services](https://plugins.jetbrains.com/docs/intellij/plugin-services.html#light-services) [#768](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/768)
- Index project root files at `All Scope` [#770](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/770)
- Do not rely on grazie API [#771](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/771)
- Use `@JvmStatic` for Companion methods [#773](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/773)

### Fixes
- Fixed NPE during project import [#765](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/765)
- Fixed possibility to attach sources and javadocs to jars in the custom library [#764](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/764)

## [2023.2.9.1]

### Fixes
- IntelliJ Community cannot build TypeSystem due dependency on Ultimate plugin [#754](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/754)

## [2023.2.9]

### Features
- Introduced pre-built `ysri` live template for **Java** [#730](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/730)

### `ImpEx` enhancements
- Added custom color setting for multi-line ` \ ` separator [#727](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/727)

### `Cockpit NG` enhancements
- Resolve Spring reference declared via `wz:initialize` : `template-bean` [#739](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/739)
- Enable code completion and reference resolution for `wz:content` : `wz:property` [#740](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/740)
- Ignore code completion and reference resolution in case of `wz:initialize` : `template-bean` [#742](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/742)
- Added reference resolution for `wz:property-list` : `root` [#743](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/743)
- Added code completion for `wz:property-list` : `root` [#744](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/744)
- Added reference resolution for `wz:content` : `wz:property` : `qualifier` [#746](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/746)
- Added code completion for `wz:content` : `wz:property` : `qualifier` init part [#747](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/747)
- Added code completion and reference resolution for `wz:content` : `wz:property` : `qualifier` attr part [#748](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/748)

### `Spring Framework` enhancements
- Resolve Spring xml configuration files defined in `META-INF` directory in jar files located in the`WEB-INF/lib` [#737](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/737)

### `items.xml` enhancements
- Resolve `many-to-many` Relation in item declaration [#720](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/720)
- Skip `many-to-many` Relation declared as Item type [#721](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/721)
- Resolve Index attributes for `many-to-many` Relation declared as Item type [#722](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/722)
- Added code completion of the Index attributes for `many-to-many` Relation declared as Item type [#723](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/723)
- Suggest a getter method for a boolean property (starts with 'is') in the `*-items.xml` files [#755](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/755)

### `Project Import` enhancements
- Register spring xml files set via properties file, but available in another extension (see `HybrisContextFactory`) [#735](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/735)
- Register `bootstrap/gensrc` and other `src` directories as source directories with respect of readonly state [#718](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/718)
- Register jar sources in the `doc/sources` for module libraries [#724](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/724)
- Improved IDE compilation and navigation in the Platform code [#726](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/726)
- Change scope to `provided` for `Backoffice Classes` library [#731](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/731)
- Attach standard sources in the `doc/sources` for web inf libraries [#732](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/732)
- Include `backoffice.web` classes in the `Backoffice Library` (**depends** on [IDEA-332845](https://youtrack.jetbrains.com/issue/IDEA-332845/Project-Library-ignores-classes-in-the-folders)) [#733](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/733)
- Include `backoffice.web` libraries in the `Backoffice Library` [#734](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/734)

### Fixes
- `SAP Commerce` tool window sometimes appears without any content [#725](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/725)
- Register sub-modules source directories for custom modules [#728](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/728)
- Register test source directories for custom backoffice modules [#736](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/736)
- Not all custom search scopes registered [#738](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/738)
- Do not validate `items.xml` for non-SAP Commerce projects [#745](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/745)

## [2023.2.8]

### Features
- Introduced pre-built `log4j2` and `slf4j` live templates for **Java** [#699](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/699)
- Introduced pre-built `log4j2` and `slf4j` live templates for **Kotlin** [#700](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/700)

### `Project Import` enhancements
- Register `platformservices` source directories to improve compilation due missing `bin/<>.jar` [#715](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/715)

### `CCv2` enhancements
- Improved CCv2 SAP Commerce Cloud `manifest.json` schema support [#685](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/685)
- Added code completion for `addons` and `storefronts` in the `manifest.json` schema [#686](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/686)
- Enabled extension name inspection for `addons` and `storefronts` elements [#687](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/687)

### `ImpEx` enhancements
- Introduced inlay hint to display default value in value lines [#670](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/670)
- Added new column-based `Split Table` action [#691](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/691)
- Added colorization for `odd` and `even` value lines [#667](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/667)
- Added inline collection merge modes: `(+)`, `(-)`, `(+?)` [#671](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/671)
- Added colorization for `(+)`, `(-)` and `(+?)` inline collection merge modes [#672](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/672)
- Added colorization for unique header parameter name [#683](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/683)
- Improved colorization for `odd` and `even` value lines [#710](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/710)
- Added support of the `<null>` special value [#673](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/673)
- Automatically add `=` after type and attribute modifier and trigger code completion [#674](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/674)
- Automatically trigger code completion after choosing the mode [#675](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/675)
- Detect all allowed by `AtomicValueTranslator` boolean tokens [#676](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/676)
- Added support of the multi-line ` \ ` separator for **macro declarations** [#677](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/677)
- Added support of the multi-line ` \ ` separator for **macro values** [#678](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/678)
- Added support of the multi-line ` \ ` separator for **header parameters** [#679](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/679)
- Added support of the multi-line ` \ ` separator for **value groups** [#680](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/680)
- Allow multi-line ` \ ` separator for **value groups** as a last character [#714](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/714)
- Adjusted Table selection logic [#689](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/689)
- Improved Column highlighting logic [#690](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/690)
- Added folding for `zip:` file load translator prefix [#692](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/692)
- Added folding for `file:` file load translator prefix [#693](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/693)
- Adjusted folding for `jar:` file load translator prefix [#694](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/694)
- Improved folding for all file load translator prefixes [#695](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/695)

### `ImpEx` inspection rules
- Omit usage of the ` \ ` multi-line separator for macro declaration [#677](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/677)
- Added local fix for missing Config import processor [#711](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/711)

### `Groovy` enhancements
- Disable actions toolbar for the test `.groovy` files [#704](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/704)
- Dynamically toggle the actions toolbar for the opened `.groovy` files in case of settings change [#705](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/705)
- Replace the action 'Execute Groovy Script' by two actions `Execute: Commit Mode ON` and `Execute: Commit Mode OFF` [#713](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/713)

### `items.xml` enhancements
- Improved folding for `persistence`:`columntype` tags [#662](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/662)
- Improved folding for `index`:`key` tags [#663](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/663)
- Improved folding for `custom-properties`:`property` tags [#664](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/664)

### `Business Process` enhancements
- Added folding for business process files [#698](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/698)
- Added folding configuration [#701](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/701)

### `Bean System` enhancements
- Added folding configuration [#701](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/701)

### `Cockpit NG` enhancements
- Added folding configuration [#701](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/701)
- Added table-like folding for `type-node` [#702](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/702)
- Added table-like folding for `list`:`column`, `parameter`, `search`:`field` [#703](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/703)
- Adjusted reference resolution for wizards using POJO [#706](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/706)
- Enabled field level reference resolution for wizards using POJO [#707](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/707)
- Enabled field level reference resolution for wizards using Java 14 Record [#708](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/708)
- Added field level code contribution for wizards using POJO & Java 14 Record [#709](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/709)

### Fixes
- Disable reference and code completion for `order` property of the Spring Interceptor bean declaration [#697](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/697)
- Fixed preview of the inlay hint for Dynamic attributes [#668](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/668)
- Enable custom action toolbars only for SAP Commerce projects [#669](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/669)
- Fixed `ImpEx` unique value inspection to support multi-line ` \ ` separator [#681](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/681)
- Fixed `ImpEx` macro-declaration Annotator to support multi-line ` \ ` separator [#682](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/682)

### Other
- Do not activate on start SAP Commerce toolwindow [#696](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/696)
- Added VCS issue navigation for IntelliJ [#665](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/665)
- New UI Tool Window Icons ([sdk docs](https://plugins.jetbrains.com/docs/intellij/work-with-icons-and-images.html#new-ui-tool-window-icons)) [#688](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/688)

## [2023.2.7]

### `items.xml` enhancements
- Added configurable table-like folding for various tags [#660](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/660)

### `beans.xml` enhancements
- Added configurable table-like folding for Bean properties [#659](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/659)

### `ImpEx` inspection rules
- Add missing value groups for value lines [#641](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/641)
- Delete blank header parameter [#642](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/642)
- Delete orphan value group [#643](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/643)

### `External Dependencies` inspection rules
- Inspect is declared dependency downloaded [#644](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/644)

### Fixes
- Bus is already disposed for consoles [#645](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/645)
- Compact middle packages not working [#646](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/646)
- Remove required sub-tags validation for Business Process `wait` node [#649](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/649)
- Unable to load the Plugin when Gradle plugin is disabled [#652](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/652)
- Block project import if `Platform` module is not detected [#654](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/654)
- Distinct modules to import by name [#656](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/656)
- Unable to import project in some circumstances [#657](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/657)
- Do not identify `core-customize`, `js-storefront` and `datahub` CCv2 folders as Gradle or Maven modules [#658](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/658)

### Other
- Added custom icons Bean System [#648](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/648)
- Added custom icon for `unmanaged-dependencies.txt` [#640](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/640)

## [2023.2.6]

### `OCC` enhancements
- Added code completion and references of Bean properties in the Level Mapping tags [#578](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/578)
- Display a flatten type for an item in the code completion menu for Level Mapping tags [#592](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/592)

### `Groovy` enhancements
- Introduced actions toolbar for `.groovy` files [#564](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/564)
- Added `Open Groovy Script` action
- Added `Execute Groovy Script` action [#565](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/565)
- Added Groovy settings pane and possibility to toggle actions toolbar visibility [#566](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/566)

### `FlexibleSearch` enhancements
- Introduced actions toolbar for `.fxs` files [#547](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/547)
- Added actions for connection settings in the toolbar [#549](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/549)
- Added `Execute` action to the toolbar [#552](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/552)

### `Polyglot Query` enhancements
- Introduced new Console to enable execution of the Polyglot queries on a remote server [#585](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/585)
- Introduced actions toolbar for `.pgq` files [#586](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/586)
- Added `Copy to console` context action [#587](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/587)

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
- Added code completion of the [Header Abbreviations](https://help.sap.com/docs/SAP_COMMERCE/d0224eca81e249cb821f2cdf45a82ace/2fb5a2a780c94325b4a48ff62b36ab23.html#using-header-abbreviations) [#613](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/613)
- Added reference resolution for `Header Abbreviations` [#615](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/615)
- Added own color scheme for `Header Abbreviations` [#617](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/617)
- Preselect special part of the `Header Abbreviation` starting with `@` character [#620](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/620)
- Adjusted Lexer to enable support of the `@` character for `Header Parameter` [#616](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/616)
- Boost child types code completion for inline header parameter [#634](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/634)
- Show documentation for `Header Abbreviation` parameters [#636](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/636)

### `ImpEx` inspection rules
- Ensure that all macros required by `Header Abbreviation` are declared [#619](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/619)
- Show distinct required macros required by `Header Abbreviation` inspection [#635](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/635)
- Do not inspect special parameter for uniqueness [#621](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/621)
- Ignore case for parameter for unique column without an index inspection [#637](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/637)

### `Cockpit NG` enhancements
- Added code completion for AdvancedSearch `operator` parameter [#537](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/537)
- Improved `qualifier` reference handling in case of set `spring-bean` for `lv:column` [#628](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/628)
- Added code completion and reference resolution for Enum attributes [#629](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/629)
- Improved code completion for `context` tag by boosting most suitable types [#632](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/632)
- Improved code completion boosting for `context` tag for Enum type [#633](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/633)

### `Cockpit NG` inspection rules
- Validate `parent` value according to `type` value of the `context` tag in case of `merge-by="type"` mode [#631](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/631)
- Adjusted DOM inspection fpr `AbstractActionType` tag [#626](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/626)
- Adjusted DOM inspection for `Essentials` tag [#627](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/627)
- Adjusted `CngContextParentIsNotValid` inspection to ignore `merge-by="type"` [#630](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/630)

### `HAC` enhancements
- Enhanced Cluster support, support node routing [#543](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/543)
- Allow blank port for connection settings [#542](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/542)
- Respect `JSESSIONID` and cookies per `HAC` connection settings
- Improved handling of the `Set-Cookie` header during login [#544](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/544)
- Automatically re-login in case of `405` response code [#548](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/548)
- Show connection URL in the tooltip for connection switcher [#588](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/588)

### `beans.xml` enhancements
- Navigate to Enum and Bean generated file via inlay hint for `class` attribute [#597](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/597)
- Added code completion of all available Beans for `class` attribute [#598](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/598)
- Added code completion of all available Beans for `extends` attribute [#600](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/600)
- Added code completion of all available Enums for `class` attribute [#599](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/599)
- Added reference resolution and advanced code completion for Bean `extends` attribute [#603](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/603)
- Added advanced code completion for Bean `type` property attribute [#605](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/605)
- Added reference resolution for Bean `type` property attribute [#606](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/606)
- Added folding for `beans.xml` files [#607](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/607)
- Added find usages for bean properties [#608](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/608)
- Added code completion for Hint `name` attribute [#609](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/609)
- Added Bean Generics code completion for Property `type` attribute [#610](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/610)
- Added navigation from a declared bean property to the generated java property [#624](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/624)

### `beans.xml` inspection rules
- Java keywords and reserved words cannot be used as Bean property **name** [#591](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/591)
- Unescaped `<` sign usage is not allowed in the Bean property **type** [#594](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/594)
- Escape `>` sign in the Bean property **type** [#595](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/595)
- Escape `>` and `<` sign in the Bean attribute **class** [#604](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/604)
- Common `java.lang` package can be omitted in the Bean property **type** [#596](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/596)
- Validate DOM structure and references [#601](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/601)

### Features
- Added configurable possibility to hide full Module name in the Project View [#590](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/590)
- Added possibility to import Gradle KTS projects as modules [#534](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/534)
- Improved folding for Relation tags in the `items.xml` [#555](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/555)
- Improved support of the Bean System `extends` and `generics` for beans [#570](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/570)
- Bean System enhanced with support of the properties defined in parent beans [#571](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/571)

### Fixes
- Type and Bean System preview is not always displayed [#611](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/611)
- Type is not detected for Cockpit NG Wizard when there is no `initialize` section [#602](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/602)
- Inject `FlexibleSearch` into Kotlin String template [#535](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/535)
- Readonly mode is broken in IDEA 2023.2 [#533](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/533)
- `ImpEx` Code Style Formatter is not configurable [#540](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/540)
- `ImpEx` functional reference type validation does not work [#546](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/546)
- Fixed `ImpEx` context actions [#551](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/551)
- Reset `ImpEx` highlighting cache on moving columns [#567](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/567)
- Fixed Compact Middle Packages in the Project View [#568](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/568)
- Fixed `commonweb` sources attaching [#572](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/572), [#573](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/573)
- Added `hmc` sub-module dependency on `hmc.web` [#574](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/574)
- Improved detection of the Gradle modules [#575](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/575)
- Use main config folder during project import [#576](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/576)
- Fixed header text for Other modules step of the Project Import Wizard [#580](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/580)
- Register Web Facet for `commonweb` [#581](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/581)
- Register Web Facet for `acceleratoraddon` [#583](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/583)
- Do not register `hmc` sub-modules if there is no `hmc` extension [#582](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/582)
- DB Datasource cannot be configured due backslash [#584](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/584)
- Some classes are not visible from `storefrontcommons` if searchable class is in read-only storefrontcommons [#579](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/579)
- Properties defined in the `advanced.properties` are not available [#612](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/612)
- Dependant modules are not imported [#614](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/614)
- Ensure that `Properties` Plugin dependant code works only when corresponding Plugin is enabled [#618](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/618)
- Also hide full Module name for `web` sub-module in the Project View [#622](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/622)

### Other
- Adjusted inline documentation for Type System [#539](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/539)
- Hide custom module libraries in the Project View [#569](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/569)
- Added Slack badge to README.md [#577](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/577)
- Do not show `resources` folders in the `External Libraries` [#589](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/589)
- Use `HAC` project properties during first project import for remote connections [#623](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/623)

## [2023.2.5]

### Features
- Added ordering attributes support for 1-to-m relations (those ending with `POS`) [#523](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/523)

### `FlexibleSearch` enhancements
- Inject language only if query starts with `SELECT` [#519](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/519)

### Fixes
- Do not register DataSource in IntelliJ Community [#530](https://github.com/epam/sap-commerce-intellij-idea-plugin/issues/530)
- Unable to import project from existing sources since upgrading to Idea 2023.2 [#526](https://github.com/epam/sap-commerce-intellij-idea-plugin/issues/526)
- Unable to import project in IntelliJ 2023.2 Community Version [#527](https://github.com/epam/sap-commerce-intellij-idea-plugin/issues/527)

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
- Updated Gradle plugin to 1.16.0 due to the exception NoClassDefFoundError: org/gradle/api/publish/ivy/internal/publication/DefaultIvyPublicationIdentity in Gradle 8.4 [#767](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/767)

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
- Fix UI freeze due new IntelliJ IDEA startup activity API [#504](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/504)
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
- Added 2023.2 IntelliJ IDEA support [#405](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/405)
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
- Added injection of the **Groovy** Language into `Script.content` in the ImpEx files [#361](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/361)
- Added injection of the **Groovy** Language into `script.type=groovy` in the Business Process files [#363](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/363)
- Added injection of the **JavaScript** Language into `script.type=javascript` in the Business Process files [#364](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/364)
- Added injection of the **JavaScript** Language into `Script.content` in the ImpEx files [#362](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/362)
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
- NPE during building Cockpit NG [#367](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/367)

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
- Cockpit NG: added `merge-by` dependant contribution for `parent` [#247](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/247)
- Cockpit NG: added `merge-by` dependant complex contribution &amp; inspections for `parent` [#248](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/248)
- Updated custom icons for Business Process diagrams [#246](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/246)
- Show progress of the Type System Global Meta Model creation [#252](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/252)
- Show progress of the Bean System Global Meta Model creation [#253](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/253)
- Show progress of the Cockpit NG Global Meta Model creation [#254](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/254)
- Improved project startup and shutdown, show `items.xml` validation progress [#255](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/255)

### Fixes
- Fixed code completion for `requires-extension` within `extensioninfo` [#244](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/244)

### Deprecated
- Decreased usage of the Deprecated API [#251](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/251)

## [2023.1.0]

### Features
- Compatibility adjustments [#195](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/195)
- Added CCv2 SAP Commerce Cloud `manifest.json` schema support [#225](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/225)
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
- Added listing of references for multiple Cockpit NG definitions [#216](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/216)
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
- Added code completion and navigation for Cockpit NG 'spring-bean' element [#184](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/184)
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
- Added navigation to `items.xml` for Types used in the Cockpit NG configuration files [#166](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/166)
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
- Added Cockpit NG files (widgets, config, definition) support, Model, completion and navigation [#165](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/165),
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
- Added 2022.3 IntelliJ IDEA support
- Introduction the Type System Management - the powerful tool to observe SAP Commerce type system [#62](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/62),
  [#63](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/63),
  [#66](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/66),
  [#60](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/60)
- Added support for the Enum completion in ImpEx/FlexibleSearch
- Added support for the Relation completion in ImpEx/FlexibleSearch
- Added MapType support for `items.xml`
- Added postgresql DB mapping
- Added next `typecode` suggestion via `items.xml` Inspection
- Added possibility to change [y] `items.xml` Inspection levels (previously they were hardcoded in ruleset.xml)
- Improved performance of the `items.xml` inspection
- Extended/refactored type system inspection rules
- Merged ToolsWindows "Hybris" (as Remote Instances) and "Hybris Console" (as Consoles) altogether with "Type system" into "Hybris" as a single entry point for all [y] related actions
- Gutter Annotator for Enum Types (alternative definitions within items.xml) [#67](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/64)
- Project refresh is broken when Gradle project was part of the initial import [#67](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/67)

### Deprecated
- Decreased usage of the Deprecated API

## [2022.2]

### Features
- Added 2022.2 IntelliJ IDEA support
- Feature Add inline actions for ImpEx & FlexibleSearch files [#53](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/53)
- Do not index `node_modules` directories [#52](https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/52)
