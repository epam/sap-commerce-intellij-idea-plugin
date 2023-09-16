# Legend of the Type System Diagram

## Diagram Settings Pane

Type System Diagram settings are project specific and stored in the `.idea/hybrisDeveloperSpecificProjectSettings.xml`
configuration file.

They can be accessed via Intellij IDEA Settings menu (`[y] SAP Commerce`>`Project Settings`>`Type System Diagram`) or
Diagram Toolbar settings action button.

| Setting                                     | Initially                                                                                                                       | Description                                                                                                       |
|---------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------|
| Nodes&nbsp;collapsed&nbsp;by&nbsp;default   | **checked**                                                                                                                     | Influence Nodes `collapse` state during initial display of the Diagram.                                           |
| Show&nbsp;OOTB&nbsp;Map&nbsp;Nodes          | **unchecked**                                                                                                                   | Affects visibility of the **OOTB** _Map_ types as a separate Node.                                                |
| Show&nbsp;Custom&nbsp;Atomic&nbsp;Nodes     | **unchecked**                                                                                                                   | Affects visibility of the **Custom** non-transitive _Atomic_ types as a separate Node.                            |
| Show&nbsp;Custom&nbsp;Collection&nbsp;Nodes | **unchecked**                                                                                                                   | Affects visibility of the **Custom** non-transitive _Collection_ types as a separate Node.                        |
| Show&nbsp;Custom&nbsp;Enum&nbsp;Nodes       | **unchecked**                                                                                                                   | Affects visibility of the **Custom** non-transitive _Enum_ types as a separate Node.                              |
| Show&nbsp;Custom&nbsp;Map&nbsp;Nodes        | **unchecked**                                                                                                                   | Affects visibility of the **Custom** non-transitive _Map_ types as a separate Node.                               |
| Show&nbsp;Custom&nbsp;Relation&nbsp;Nodes   | **unchecked**                                                                                                                   | Affects visibility of the **Custom** non-transitive _Relation_ types without deployment table as a separate Node. |
| Excluded&nbsp;Type&nbsp;Names               | -&nbsp;GenericItem<br>-&nbsp;Item<br>-&nbsp;LocalizableItem<br>-&nbsp;ExtensibleItem<br>-&nbsp;CronJob<br>-&nbsp;CatalogVersion | Represents set of case-sensitive Type Names will be excluded from the Diagram.                                    |

## Diagram Toolbar

![diagram_ts_toolbar.png](images%2Fdiagram_ts_toolbar.png)

| Item                                                                                                                     | Description                                                                                                                                                                                                                     |
|--------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ![settings.svg](..%2Fresources%2Ficons%2Fsettings.svg)                                            | Opens Project specific Type System Diagram settings pane.                                                                                                                                                                       |
| ![resetView.svg](..%2Fresources%2Ficons%2FtypeSystem%2Fdiagram%2FresetView.svg)                                          | Restores all manually removed Nodes and triggers Diagram refresh.                                                                                                                                                               |
| ![expandall.svg](https://intellij-icons.jetbrains.design/icons/AllIcons/actions/expandall.svg)                           | Expands all Nodes and triggers Diagram refresh.                                                                                                                                                                                 |
| ![expandall.svg](https://intellij-icons.jetbrains.design/icons/AllIcons/actions/collapseall.svg)                         | Collapse all Nodes and triggers Diagram refresh.                                                                                                                                                                                |
| ![property.svg](https://intellij-icons.jetbrains.design/icons/AllIcons/nodes/property.svg)                               | Node `propeties` fields related Category Filter                                                                                                                                                                                 |
| ![customProperty.svg](..%2Fresources%2Ficons%2FtypeSystem%2FcustomProperty.svg)                                          | Node `custom properties` fields related Category Filter                                                                                                                                                                         |
| ![relation.svg](..%2Fresources%2Ficons%2FtypeSystem%2Frelation.svg)                                                      | Node `relation ends` fields related Category Filter                                                                                                                                                                             |
| ![attribute.svg](..%2Fresources%2Ficons%2FtypeSystem%2Fattribute.svg)                                                    | Node `attributes` fields related Category Filter                                                                                                                                                                                |
| ![index.svg](..%2Fresources%2Ficons%2FtypeSystem%2Findex.svg)                                                            | Node `indexes` fields related Category Filter                                                                                                                                                                                   |
| ![enumValue.svg](..%2Fresources%2Ficons%2FtypeSystem%2FenumValue.svg)                                                    | Node `enum values` fields related Category Filter                                                                                                                                                                               |
| ![db.svg](https://intellij-icons.jetbrains.design/icons/AllIcons/debugger/db_db_object.svg)                              | Node `deployment` fields related Category Filter                                                                                                                                                                                |
| ![dependencies.svg](https://intellij-icons.jetbrains.design/icons/AllIcons/toolwindows/toolWindowModuleDependencies.svg) | If enabled shows 1st level of transitive dependencies according to visible Node fields                                                                                                                                          |
| ![visibility.svg](https://intellij-icons.jetbrains.design/icons/AllIcons/actions/toggleVisibility.svg)                   | Provides two fields visibility levels: `Only Custom Fields` and `All`                                                                                                                                                           |
| ![scope.svg](https://intellij-icons.jetbrains.design/icons/AllIcons/general/filter.svg)                                  | Provides four Node related scope levels: `All`, `Custom with Extends`, `Only Custom` and `Only OOTB`.<br/>Default is `Custom with Extends`.<br/>Caution: ensure to limit down shown nodes before changing scope level to `All`. |
| ![refresh.svg](https://intellij-icons.jetbrains.design/icons/AllIcons/actions/refresh.svg)                               | Triggers re-build of the Diagram in accordance with latest `items.xml` content.                                                                                                                                                 |

## Diagram Edge Elements

| Item                                                                                 | Description                                                                  |
|--------------------------------------------------------------------------------------|------------------------------------------------------------------------------|
| ![diagram_ts_edge_extends.png](images%2Fdiagram_ts_edge_extends.png)                 | The green arrow corresponds to the `extends` tag in a Item Type declaration. |
| ![diagram_ts_edge_part_of.png](images%2Fdiagram_ts_edge_part_of.png)                 | The orange arrow corresponds to the `partOf` relation between Items.         |
| ![diagram_ts_edge_non_navigable.png](images%2Fdiagram_ts_edge_non_navigable.png)     | The grayed-out arrow corresponds to the `navigable="false"` Relation End.    |
| ![diagram_ts_edge_1_to_m.png](images%2Fdiagram_ts_edge_1_to_m.png)                   | The blue arrow corresponds to the `one`-`to`-`many` Relation.                |
| ![diagram_ts_edge_optional_1_to_m.png](images%2Fdiagram_ts_edge_optional_1_to_m.png) | The blue arrow corresponds to the **optional** `one`-`to`-`many` Relation.   |
| ![diagram_ts_edge_1_to_1.png](images%2Fdiagram_ts_edge_1_to_1.png)                   | The blue arrow corresponds to the `one`-`to`-`one` Relation.                 |
| ![diagram_ts_edge_optional_1_to_1.png](images%2Fdiagram_ts_edge_optional_1_to_1.png) | The blue arrow corresponds to the **optional** `one`-`to`-`one` Relation.    |

## Diagram Node Elements

### Diagram Node Actions

Type System Diagram supports multiple Node Actions, available under Node context menu.

Take a note that each action triggers Diagram Layout & Data refresh.

| Action Name                 | Shortcut    | Description                                                                                            |
|-----------------------------|-------------|--------------------------------------------------------------------------------------------------------|
| Delete                      | D           | Hides corresponding Node.                                                                              |
| Exclude&nbsp;Type&nbsp;Name | Ctrl+Meta+E | Adds Type Name associated with corresponding Node to the project specific list of Excluded Type Names. |
| Collapse&nbsp;Nodes         | C           | Hides all Node fields, such as Attributes, Properties, Indexes, etc.                                   |
| Expand&nbsp;Nodes           | E           | Shows all Node fields, which are allowed to be shown according to Category Visibility settings.        |

### Diagram Node Header

Node tooltip with additional details will be shown on hover over Node Header title.

| State                                                                                                        | Description                                                                                  |
|--------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------|
| ![diagram_ts_node_collapsed.png](images%2Fdiagram_ts_node_collapsed.png)                                     | Special grayed-out text `collapsed` will be displayed when Node is Collapsed.                | 
| ![diagram_ts_node_header_bg_modified_type.png](images%2Fdiagram_ts_node_header_bg_modified_type.png)         | The darker Node header background color corresponds to the _Custom_ or _modified OOTB_ Item. |
| ![diagram_ts_node_header_bg_non_modified_type.png](images%2Fdiagram_ts_node_header_bg_non_modified_type.png) | The lighter Node header background color corresponds to the _OOTB_ Item.                     |

### Diagram Node Fields

| Field                                                                                                | Description                                               |
|------------------------------------------------------------------------------------------------------|-----------------------------------------------------------|
| ![diagram_ts_node_field_db.png](images%2Fdiagram_ts_node_field_db.png)                               | Lists Deployment tag details of the ItemType or Relation. |
| ![diagram_ts_node_field_custom_properties.png](images%2Fdiagram_ts_node_field_custom_properties.png) | Lists declared Custom Properties for **current** Item.    |
| ![diagram_ts_node_field_enum_values.png](images%2Fdiagram_ts_node_field_enum_values.png)             | Lists available enum values.                              |
| ![diagram_ts_node_field_attributes.png](images%2Fdiagram_ts_node_field_attributes.png)               | Lists declared Attributes for **current** Item.           |
| ![diagram_ts_node_field_relation_ends.png](images%2Fdiagram_ts_node_field_relation_ends.png)         | Lists declared Relation Ends with corresponding icon.     |
| ![diagram_ts_node_field_indexes.png](images%2Fdiagram_ts_node_field_indexes.png)                     | Lists declared Indexes for **current** Item.              |
| ![diagram_ts_node_field_properties.png](images%2Fdiagram_ts_node_field_properties.png)               | Lists transitive or indirectly declared Item fields.      |
