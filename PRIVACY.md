# Privacy Policy

_Last updated: 2025-06-07_

This Privacy Policy outlines the data practices of the **SAP Commerce Developer Toolset** IntelliJ IDEA plugin (hereafter referred to as "the Plugin"). The Plugin is designed to assist developers working with SAP Commerce by providing code insight, project navigation, and other productivity tools within IntelliJ IDEA.

---

## 1. Scope of the Plugin

The Plugin enhances the development experience for SAP Commerce by analyzing source code, project structure, and metadata through IntelliJ Platform APIs. All processing is performed within the developer’s local development environment.

---

## 2. Data Collection and Transmission

The Plugin **does not collect, store, or transmit** any of the following:

- Personally identifiable information (PII)
- Project-specific data (e.g., configuration files, business logic, credentials)
- Usage analytics or telemetry
- Build artifacts or logs
- User interactions

All plugin operations are performed **entirely offline** within your local IntelliJ IDEA instance. No data is sent to any external server or service.

---

## 3. Local File and PSI Access

To provide intelligent code features, the Plugin accesses:

- PSI (Program Structure Interface) elements
- File structure and metadata relevant to SAP Commerce projects
- IntelliJ IDEA APIs to read local configurations and project models

This access is used exclusively for in-IDE functionality such as inspections, code navigation, refactorings, and context-aware assistance. The data is neither logged, persisted, nor transmitted externally.

---

## 4. Third-Party Services and SDKs

The Plugin integrates with SAP Commerce Cloud Version 2 (CCv2) environments by using APIs or SDKs provided by SAP. This integration is limited to features required for local development workflows, such as:

- Reading or interacting with CCv2-specific project structures
- Fetching remote metadata (if applicable)
- Supporting deployment or synchronization features (if applicable)

In order to securely access CCv2 environments, the Plugin may store API keys or authentication tokens. These credentials are stored using OS-specific secure storage mechanisms (such as macOS Keychain, Windows Credential Store, or Linux Secret Service) and are never transmitted externally beyond the direct interaction with CCv2 services.

The Plugin does **not use** any third-party analytics, error reporting, or telemetry services.  
No user data is sent to external services beyond what is required to support direct interaction with SAP Commerce Cloud, and no personally identifiable or project-sensitive data is stored or transmitted without the user’s intent.

---

## 5. User Control

The Plugin does not require or expose user settings for telemetry or external communication because **no such features exist**. All core features operate out of the box with no background data transfer or hidden behaviors.

---

## 6. Security and Compliance

Because the Plugin does not collect or transmit any data, there is no risk of exposing personal or proprietary information via the plugin itself. This approach ensures compliance with:

- General Data Protection Regulation (GDPR)
- California Consumer Privacy Act (CCPA)
- Other relevant data protection laws

---

## 7. Changes to This Policy

This privacy policy may be updated to reflect new features or changes in data practices. Any updates will be published in this file and noted in the Plugin’s release changelog.

We recommend checking this document periodically when updating the Plugin to ensure continued understanding of its data practices.

---

## 8. Contact

If you have any questions, concerns, or requests regarding this privacy policy or the Plugin's behavior, please contact:

**EPAM Systems**  
Email: [hybrisideaplugin@epam.com](hybrisideaplugin@epam.com)  
GitHub: [https://github.com/epam/sap-commerce-intellij-idea-plugin](https://github.com/epam/sap-commerce-intellij-idea-plugin)