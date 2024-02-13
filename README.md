# CimPal CGMES Converter

This is a module of CimPal which has the functionality to convert CGMES v2.4 to CGMES v3.0 (IEC 61970-600-1:2021 and IEC 61970-600-2:2021). DC part of the CGMES may not be fully covered by the converter.
Improvements are expected in case this application turns out to be useful and appreciated by the community. gridDigIt will be glad to support further development based on requirements. 

In order to run the application and use the conversion you need to download the RDFSCGMESv3 folder and paste it in the same folder where the CimPalCGMESConverter.jar is.

# CimPal Model modifications

This app was extended to cover another use case where it is necessary to add breakers to a model initially designed as a bus-branch model. Breakers are added to lines and synchronous machines in order to support creation of topological remedial actions that apply on switching devices.

## General
CimPal is an open source Java application published by gridDigIt and Licensed under the [EUPL-1.2](https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12)-or-later.
The application is based on Apache Jena (Apache License v2.0), TopBraid SHACL API (Apache License v2.0), and JAVAFX (GPLv2+CE license).

CimPal helps CIM implementation by providing tools facilitation the work related to implementation of different CIM based data exchange standards and specifications.

gridDigIt aims at providing support and further enhancement of the CimPal. Please address any requests either via the support email or in GitHub, by submitting an issue in the repository.

Web site: https://cimpal.app/ \
Support email: cimpal@griddigit.eu 

Copyright 2023, [gridDigIt Kft](https://griddigit.eu). All rights reserved.
