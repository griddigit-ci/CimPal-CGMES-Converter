/*
 * Licensed under the EUPL-1.2-or-later.
 * Copyright (c) 2022, gridDigIt Kft. All rights reserved.
 * @author Chavdar Ivanov
 */

package core;

import application.MainController;
import customWriter.CustomRDFFormat;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ModelManipulationFactory {

public static Map<String,String> nameMap;

 //Convert CGMES v2.4 to CGMES v3.0
    public static void ConvertCGMESv2v3(Map<String, Map> loadDataMap, int keepExtensions) throws IOException {


        //Map<String, Map> loadDataMap= new HashMap<>();
        String xmlBase = "http://iec.ch/TC57/CIM100";
        //String xmlBase = "";

        //set properties for the export

        Map<String, Object> saveProperties = new HashMap<>();

        saveProperties.put("filename", "test");
        saveProperties.put("showXmlDeclaration", "true");
        saveProperties.put("showDoctypeDeclaration", "false");
        saveProperties.put("tab", "2");
        saveProperties.put("relativeURIs", "same-document");
        saveProperties.put("showXmlEncoding", "true");
        saveProperties.put("xmlBase", xmlBase);
        saveProperties.put("rdfFormat", CustomRDFFormat.RDFXML_CUSTOM_PLAIN_PRETTY);
        //saveProperties.put("rdfFormat", CustomRDFFormat.RDFXML_CUSTOM_PLAIN);
        saveProperties.put("useAboutRules", true); //switch to trigger file chooser and adding the property
        saveProperties.put("useEnumRules", true); //switch to trigger special treatment when Enum is referenced
        saveProperties.put("useFileDialog", false);
        saveProperties.put("fileFolder", "C:");
        saveProperties.put("dozip", false);
        saveProperties.put("instanceData", "true"); //this is to only print the ID and not with namespace
        saveProperties.put("showXmlBaseDeclaration", "false");

        saveProperties.put("putHeaderOnTop", true);
        saveProperties.put("headerClassResource", "http://iec.ch/TC57/61970-552/ModelDescription/1#FullModel");
        saveProperties.put("extensionName", "RDF XML");
        saveProperties.put("fileExtension", "*.xml");
        saveProperties.put("fileDialogTitle", "Save RDF XML for");
        //RDFFormat rdfFormat=RDFFormat.RDFXML;
        //RDFFormat rdfFormat=RDFFormat.RDFXML_PLAIN;
        //RDFFormat rdfFormat = RDFFormat.RDFXML_ABBREV;
        //RDFFormat rdfFormat = CustomRDFFormat.RDFXML_CUSTOM_PLAIN_PRETTY;
        //RDFFormat rdfFormat = CustomRDFFormat.RDFXML_CUSTOM_PLAIN;


        //TODO to be improved what file names should be assigned. Now it takes same names
        nameMap=new HashMap<>();
        for (File item : MainController.IDModel){
            if (item.toString().contains("_EQ")){
                nameMap.put("EQ", item.getName());
            }else if (item.toString().contains("_SSH")){
                nameMap.put("SSH", item.getName());
            }else if (item.toString().contains("_SV")){
                nameMap.put("SV", item.getName());
            }else if (item.toString().contains("_TP")){
                nameMap.put("TP", item.getName());
            }
        }

        Map<String,Model> baseInstanceModelMap = InstanceDataFactory.modelLoad(MainController.IDModel, xmlBase, null);

        Model modelEQ = baseInstanceModelMap.get("EQ");
        Model modelSSH = baseInstanceModelMap.get("SSH");
        Model modelSV = baseInstanceModelMap.get("SV");
        Model modelTP = baseInstanceModelMap.get("TP");
        Model modelTPBD = baseInstanceModelMap.get("TPBD");

        Map<String,Model> convertedModelMap=new HashMap<>();

        //create the new models
        Model convEQModel = createModel();
        Model convSSHModel = createModel();
        Model convSVModel = createModel();
        Model convTPModel = createModel();

        //check for extensions

        if (keepExtensions==1){
            Map<String, String> oldPrefix = modelEQ.getNsPrefixMap();
            for (Map.Entry<String, String> entry : oldPrefix.entrySet()) {
                if (!entry.getKey().equals("cim") && !entry.getKey().equals("eu") && !entry.getKey().equals("entsoe") && !entry.getKey().equals("md") && !entry.getKey().equals("rfd")){
                    convEQModel.setNsPrefix(entry.getKey(),entry.getValue());
                }
            }
            oldPrefix = modelSSH.getNsPrefixMap();
            for (Map.Entry<String, String> entry : oldPrefix.entrySet()) {
                if (!entry.getKey().equals("cim") && !entry.getKey().equals("eu") && !entry.getKey().equals("entsoe") && !entry.getKey().equals("md") && !entry.getKey().equals("rfd")){
                    convSSHModel.setNsPrefix(entry.getKey(),entry.getValue());
                }
            }
            oldPrefix = modelSV.getNsPrefixMap();
            for (Map.Entry<String, String> entry : oldPrefix.entrySet()) {
                if (!entry.getKey().equals("cim") && !entry.getKey().equals("eu") && !entry.getKey().equals("entsoe") && !entry.getKey().equals("md") && !entry.getKey().equals("rfd")){
                    convSVModel.setNsPrefix(entry.getKey(),entry.getValue());
                }
            }
            oldPrefix = modelTP.getNsPrefixMap();
            for (Map.Entry<String, String> entry : oldPrefix.entrySet()) {
                if (!entry.getKey().equals("cim") && !entry.getKey().equals("eu") && !entry.getKey().equals("entsoe") && !entry.getKey().equals("md") && !entry.getKey().equals("rfd")){
                    convTPModel.setNsPrefix(entry.getKey(),entry.getValue());
                }
            }
        }


        Property mrid=ResourceFactory.createProperty("http://iec.ch/TC57/CIM100#IdentifiedObject.mRID");

        //add header for EQ
        Resource headerRes = modelEQ.listSubjectsWithProperty(RDF.type, (RDFNode) ResourceFactory.createProperty("http://iec.ch/TC57/61970-552/ModelDescription/1#","FullModel")).nextResource();
        for (StmtIterator n = modelEQ.listStatements(new SimpleSelector(headerRes, null, (RDFNode) null)); n.hasNext(); ) {
            Statement stmtH = n.next();
            if (stmtH.getPredicate().getLocalName().equals("Model.profile")) {
                if (stmtH.getObject().asLiteral().getString().equals("http://entsoe.eu/CIM/EquipmentCore/3/1")) {
                    convEQModel.add(ResourceFactory.createStatement(stmtH.getSubject(), stmtH.getPredicate(), ResourceFactory.createPlainLiteral("http://iec.ch/TC57/ns/CIM/CoreEquipment-EU/3.0")));
                }else if (stmtH.getObject().asLiteral().getString().equals("http://entsoe.eu/CIM/EquipmentShortCircuit/3/1")) {
                    convEQModel.add(ResourceFactory.createStatement(stmtH.getSubject(), stmtH.getPredicate(), ResourceFactory.createPlainLiteral("http://iec.ch/TC57/ns/CIM/ShortCircuit-EU/3.0")));
                }else if (stmtH.getObject().asLiteral().getString().equals("http://entsoe.eu/CIM/EquipmentOperation/3/1")) {
                    convEQModel.add(ResourceFactory.createStatement(stmtH.getSubject(), stmtH.getPredicate(), ResourceFactory.createPlainLiteral("http://iec.ch/TC57/ns/CIM/Operation-EU/3.0")));
                }
            } else if (stmtH.getPredicate().getLocalName().equals("Model.created")) {
                if (!stmtH.getObject().toString().endsWith("Z")){
                    convEQModel.add(ResourceFactory.createStatement(stmtH.getSubject(), stmtH.getPredicate(), ResourceFactory.createPlainLiteral(stmtH.getObject().toString()+"Z")));
                }else{
                    convEQModel.add(stmtH);
                }

            } else if (stmtH.getPredicate().getLocalName().equals("Model.scenarioTime")) {
                if (!stmtH.getObject().toString().endsWith("Z")){
                    convEQModel.add(ResourceFactory.createStatement(stmtH.getSubject(), stmtH.getPredicate(), ResourceFactory.createPlainLiteral(stmtH.getObject().toString()+"Z")));
                }else{
                    convEQModel.add(stmtH);
                }
            }else{
                convEQModel.add(stmtH);
            }
        }

        //add header for SSH
        RDFNode sshMAS = null;
        headerRes = modelSSH.listSubjectsWithProperty(RDF.type, (RDFNode) ResourceFactory.createProperty("http://iec.ch/TC57/61970-552/ModelDescription/1#","FullModel")).nextResource();
        for (StmtIterator n = modelSSH.listStatements(new SimpleSelector(headerRes, null, (RDFNode) null)); n.hasNext(); ) {
            Statement stmtH = n.next();
            if (stmtH.getPredicate().getLocalName().equals("Model.profile")) {
                convSSHModel.add(ResourceFactory.createStatement(stmtH.getSubject(), stmtH.getPredicate(), ResourceFactory.createPlainLiteral("http://iec.ch/TC57/ns/CIM/SteadyStateHypothesis-EU/3.0")));
            } else if (stmtH.getPredicate().getLocalName().equals("Model.created")) {
                if (!stmtH.getObject().toString().endsWith("Z")){
                    convSSHModel.add(ResourceFactory.createStatement(stmtH.getSubject(), stmtH.getPredicate(), ResourceFactory.createPlainLiteral(stmtH.getObject().toString()+"Z")));
                }else{
                    convSSHModel.add(stmtH);
                }

            } else if (stmtH.getPredicate().getLocalName().equals("Model.scenarioTime")) {
                if (!stmtH.getObject().toString().endsWith("Z")){
                    convSSHModel.add(ResourceFactory.createStatement(stmtH.getSubject(), stmtH.getPredicate(), ResourceFactory.createPlainLiteral(stmtH.getObject().toString()+"Z")));
                }else {
                    convSSHModel.add(stmtH);
                }
            }else{
                convSSHModel.add(stmtH);
            }
            if (stmtH.getPredicate().getLocalName().equals("Model.modelingAuthoritySet")) {
                sshMAS=stmtH.getObject();
            }
        }

        //add header for SV
        headerRes = modelSV.listSubjectsWithProperty(RDF.type, (RDFNode) ResourceFactory.createProperty("http://iec.ch/TC57/61970-552/ModelDescription/1#","FullModel")).nextResource();
        for (StmtIterator n = modelSV.listStatements(new SimpleSelector(headerRes, null, (RDFNode) null)); n.hasNext(); ) {
            Statement stmtH = n.next();
            if (stmtH.getPredicate().getLocalName().equals("Model.profile")) {
                convSVModel.add(ResourceFactory.createStatement(stmtH.getSubject(), stmtH.getPredicate(), ResourceFactory.createPlainLiteral("http://iec.ch/TC57/ns/CIM/StateVariables-EU/3.0")));
            } else if (stmtH.getPredicate().getLocalName().equals("Model.created")) {
                if (!stmtH.getObject().toString().endsWith("Z")){
                    convSVModel.add(ResourceFactory.createStatement(stmtH.getSubject(), stmtH.getPredicate(), ResourceFactory.createPlainLiteral(stmtH.getObject().toString()+"Z")));
                }else{
                    convSVModel.add(stmtH);
                }

            } else if (stmtH.getPredicate().getLocalName().equals("Model.scenarioTime")) {
                if (!stmtH.getObject().toString().endsWith("Z")){
                    convSVModel.add(ResourceFactory.createStatement(stmtH.getSubject(), stmtH.getPredicate(), ResourceFactory.createPlainLiteral(stmtH.getObject().toString()+"Z")));
                }else{
                    convSVModel.add(stmtH);
                }
            }else{
                convSVModel.add(stmtH);
            }
            if (stmtH.getPredicate().getLocalName().equals("Model.modelingAuthoritySet")) {
                convSVModel.add(ResourceFactory.createStatement(stmtH.getSubject(), stmtH.getPredicate(), sshMAS));
            }
        }
        if (!convSVModel.listStatements(new SimpleSelector(null, ResourceFactory.createProperty("http://iec.ch/TC57/61970-552/ModelDescription/1#","Model.modelingAuthoritySet"),(RDFNode) null)).hasNext()){
            convSVModel.add(ResourceFactory.createStatement(headerRes, ResourceFactory.createProperty("http://iec.ch/TC57/61970-552/ModelDescription/1#","Model.modelingAuthoritySet"), sshMAS));
        }

        //add header for TP
        headerRes = modelTP.listSubjectsWithProperty(RDF.type, (RDFNode) ResourceFactory.createProperty("http://iec.ch/TC57/61970-552/ModelDescription/1#","FullModel")).nextResource();
        for (StmtIterator n = modelTP.listStatements(new SimpleSelector(headerRes, null, (RDFNode) null)); n.hasNext(); ) {
            Statement stmtH = n.next();
            if (stmtH.getPredicate().getLocalName().equals("Model.profile")) {
                convTPModel.add(ResourceFactory.createStatement(stmtH.getSubject(), stmtH.getPredicate(), ResourceFactory.createPlainLiteral("http://iec.ch/TC57/ns/CIM/Topology-EU/3.0")));
            } else if (stmtH.getPredicate().getLocalName().equals("Model.created")) {
                if (!stmtH.getObject().toString().endsWith("Z")){
                    convTPModel.add(ResourceFactory.createStatement(stmtH.getSubject(), stmtH.getPredicate(), ResourceFactory.createPlainLiteral(stmtH.getObject().toString()+"Z")));
                }else {
                    convTPModel.add(stmtH);
                }

            } else if (stmtH.getPredicate().getLocalName().equals("Model.scenarioTime")) {
                if (!stmtH.getObject().toString().endsWith("Z")){
                    convTPModel.add(ResourceFactory.createStatement(stmtH.getSubject(), stmtH.getPredicate(), ResourceFactory.createPlainLiteral(stmtH.getObject().toString()+"Z")));
                }else{
                    convTPModel.add(stmtH);
                }
            }else{
                convTPModel.add(stmtH);
            }
        }


        //conversion process
        String cim17NS="http://iec.ch/TC57/CIM100#";
        String cim16NS="http://iec.ch/TC57/2013/CIM-schema-cim16#";
        String euNS="http://iec.ch/TC57/CIM100-European#";
        List<String> skipList= new LinkedList<>();
        skipList.add("ConnectivityNode.boundaryPoint");
        List<String> getMRID= new LinkedList<>();
        getMRID.add("ConnectivityNode");
        getMRID.add("Line");
        getMRID.add("EnergySchedulingType");
        getMRID.add("GeographicalRegion");
        getMRID.add("SubGeographicalRegion");
        getMRID.add("Terminal");
        getMRID.add("Substation");
        getMRID.add("BaseVoltage");
        getMRID.add("VoltageLevel");
        getMRID.add("Bay");
        getMRID.add("Junction");


        List<String> getMRIDSV= new LinkedList<>();
        getMRIDSV.add("DCTopologicalIsland");
        getMRIDSV.add("TopologicalIsland");

        List<String> getMRIDTP= new LinkedList<>();
        getMRIDTP.add("DCTopologicalNode");
        getMRIDTP.add("TopologicalNode");

        List<String> getMRIDSSH= new LinkedList<>();
        getMRIDSSH.add("Equipment");

        List<String> getMRIDEQ= new LinkedList<>();
        getMRIDEQ.add("ActivePowerLimit");
        getMRIDEQ.add("BusNameMarker");
        getMRIDEQ.add("Ground");
        getMRIDEQ.add("PhotoVoltaicUnit");
        getMRIDEQ.add("LoadBreakSwitch");
        getMRIDEQ.add("StationSupply");
        getMRIDEQ.add("CurrentTransformer");
        getMRIDEQ.add("ReportingGroup");
        getMRIDEQ.add("WaveTrap");
        getMRIDEQ.add("ReactiveCapabilityCurve");
        getMRIDEQ.add("PowerTransformerEnd");
        getMRIDEQ.add("Junction");
        getMRIDEQ.add("TapSchedule");
        getMRIDEQ.add("EnergySchedulingType");
        getMRIDEQ.add("PhaseTapChangerSymmetrical");
        getMRIDEQ.add("NonConformLoad");
        getMRIDEQ.add("NonConformLoadSchedule");
        getMRIDEQ.add("PhaseTapChangerTable");
        getMRIDEQ.add("ExternalNetworkInjection");
        getMRIDEQ.add("ControlAreaGeneratingUnit");
        getMRIDEQ.add("PhaseTapChangerTabular");
        getMRIDEQ.add("GrossToNetActivePowerCurve");
        getMRIDEQ.add("AsynchronousMachine");
        getMRIDEQ.add("Substation");
        getMRIDEQ.add("HydroGeneratingUnit");
        getMRIDEQ.add("DCConverterUnit");
        getMRIDEQ.add("Bay");
        getMRIDEQ.add("OperationalLimitSet");
        getMRIDEQ.add("VsCapabilityCurve");
        getMRIDEQ.add("ConformLoadSchedule");
        getMRIDEQ.add("GeneratingUnit");
        getMRIDEQ.add("CombinedCyclePlant");
        getMRIDEQ.add("SwitchSchedule");
        getMRIDEQ.add("SubLoadArea");
        getMRIDEQ.add("Line");
        getMRIDEQ.add("Jumper");
        getMRIDEQ.add("PetersenCoil");
        getMRIDEQ.add("GeographicalRegion");
        getMRIDEQ.add("PostLineSensor");
        getMRIDEQ.add("RatioTapChangerTable");
        getMRIDEQ.add("EquivalentShunt");
        getMRIDEQ.add("ConformLoad");
        getMRIDEQ.add("HydroPump");
        getMRIDEQ.add("WindPowerPlant");
        getMRIDEQ.add("NonConformLoadGroup");
        getMRIDEQ.add("Breaker");
        getMRIDEQ.add("DCTerminal");
        getMRIDEQ.add("DCLine");
        getMRIDEQ.add("DCChopper");
        getMRIDEQ.add("Fuse");
        getMRIDEQ.add("DCGround");
        getMRIDEQ.add("SubGeographicalRegion");
        getMRIDEQ.add("LoadResponseCharacteristic");
        getMRIDEQ.add("PotentialTransformer");
        getMRIDEQ.add("DayType");
        getMRIDEQ.add("FossilFuel");
        getMRIDEQ.add("ConnectivityNode");
        getMRIDEQ.add("CurrentLimit");
        getMRIDEQ.add("SolarGeneratingUnit");
        getMRIDEQ.add("WindGeneratingUnit");
        getMRIDEQ.add("StaticVarCompensator");
        getMRIDEQ.add("DCDisconnector");
        getMRIDEQ.add("SeriesCompensator");
        getMRIDEQ.add("RegulatingControl");
        getMRIDEQ.add("BatteryUnit");
        getMRIDEQ.add("LinearShuntCompensator");
        getMRIDEQ.add("LoadArea");
        getMRIDEQ.add("FaultIndicator");
        getMRIDEQ.add("TapChangerControl");
        getMRIDEQ.add("EnergySource");
        getMRIDEQ.add("BusbarSection");
        getMRIDEQ.add("DCShunt");
        getMRIDEQ.add("RegulationSchedule");
        getMRIDEQ.add("OperationalLimitType");
        getMRIDEQ.add("DCBreaker");
        getMRIDEQ.add("Terminal");
        getMRIDEQ.add("PhaseTapChangerLinear");
        getMRIDEQ.add("VoltageLimit");
        getMRIDEQ.add("NuclearGeneratingUnit");
        getMRIDEQ.add("DCLineSegment");
        getMRIDEQ.add("DCNode");
        getMRIDEQ.add("ACDCConverterDCTerminal");
        getMRIDEQ.add("Switch");
        getMRIDEQ.add("Clamp");
        getMRIDEQ.add("Season");
        getMRIDEQ.add("SynchronousMachine");
        getMRIDEQ.add("EquivalentBranch");
        getMRIDEQ.add("ConformLoadGroup");
        getMRIDEQ.add("BaseVoltage");
        getMRIDEQ.add("Disconnector");
        getMRIDEQ.add("SolarPowerPlant");
        getMRIDEQ.add("GroundingImpedance");
        getMRIDEQ.add("Cut");
        getMRIDEQ.add("ThermalGeneratingUnit");
        getMRIDEQ.add("GroundDisconnector");
        getMRIDEQ.add("ApparentPowerLimit");
        getMRIDEQ.add("RatioTapChanger");
        getMRIDEQ.add("PowerTransformer");
        getMRIDEQ.add("SurgeArrester");
        getMRIDEQ.add("EnergyConsumer");
        getMRIDEQ.add("BoundaryPoint");
        getMRIDEQ.add("CsConverter");
        getMRIDEQ.add("DCSeriesDevice");
        getMRIDEQ.add("ACLineSegment");
        getMRIDEQ.add("PhaseTapChangerAsymmetrical");
        getMRIDEQ.add("VsConverter");
        getMRIDEQ.add("CAESPlant");
        getMRIDEQ.add("NonlinearShuntCompensator");
        getMRIDEQ.add("HydroPowerPlant");
        getMRIDEQ.add("PowerElectronicsWindUnit");
        getMRIDEQ.add("PowerElectronicsConnection");
        getMRIDEQ.add("VoltageLevel");
        getMRIDEQ.add("DCSwitch");
        getMRIDEQ.add("EquivalentNetwork");
        getMRIDEQ.add("ControlArea");
        getMRIDEQ.add("TieFlow");
        getMRIDEQ.add("CogenerationPlant");
        getMRIDEQ.add("EquivalentInjection");
        getMRIDEQ.add("DisconnectingCircuitBreaker");
        getMRIDEQ.add("DCBusbar");





        List<String> getEqInService= new LinkedList<>();
        getEqInService.add("ConformLoad");
        getEqInService.add("PotentialTransformer");
        getEqInService.add("Ground");
        getEqInService.add("HydroPump");
        getEqInService.add("WaveTrap");
        getEqInService.add("EnergyConsumer");
        getEqInService.add("DCChopper");
        getEqInService.add("PowerElectronicsWindUnit");
        getEqInService.add("PhotoVoltaicUnit");
        getEqInService.add("ExternalNetworkInjection");
        getEqInService.add("AsynchronousMachine");
        getEqInService.add("Jumper");
        getEqInService.add("CurrentTransformer");
        getEqInService.add("BatteryUnit");
        getEqInService.add("WindGeneratingUnit");
        getEqInService.add("EquivalentShunt");
        getEqInService.add("DCShunt");
        getEqInService.add("SynchronousMachine");
        getEqInService.add("EquivalentBranch");
        getEqInService.add("NonlinearShuntCompensator");
        getEqInService.add("ThermalGeneratingUnit");
        getEqInService.add("PostLineSensor");
        getEqInService.add("DCDisconnector");
        getEqInService.add("Switch");
        getEqInService.add("DCBusbar");
        getEqInService.add("CsConverter");
        getEqInService.add("Cut");
        getEqInService.add("Breaker");
        getEqInService.add("DCSwitch");
        getEqInService.add("SeriesCompensator");
        getEqInService.add("SurgeArrester");
        getEqInService.add("Fuse");
        getEqInService.add("SolarGeneratingUnit");
        getEqInService.add("StaticVarCompensator");
        getEqInService.add("DCGround");
        getEqInService.add("NonConformLoad");
        getEqInService.add("ACLineSegment");
        getEqInService.add("HydroGeneratingUnit");
        getEqInService.add("FaultIndicator");
        getEqInService.add("DCBreaker");
        getEqInService.add("VsConverter");
        getEqInService.add("LoadBreakSwitch");
        getEqInService.add("DCLineSegment");
        getEqInService.add("BusbarSection");
        getEqInService.add("Disconnector");
        getEqInService.add("GeneratingUnit");
        getEqInService.add("PowerTransformer");
        getEqInService.add("LinearShuntCompensator");
        getEqInService.add("DisconnectingCircuitBreaker");
        getEqInService.add("GroundDisconnector");
        getEqInService.add("PetersenCoil");
        getEqInService.add("Junction");
        getEqInService.add("PowerElectronicsConnection");
        getEqInService.add("EnergySource");
        getEqInService.add("NuclearGeneratingUnit");
        getEqInService.add("GroundingImpedance");
        getEqInService.add("StationSupply");
        getEqInService.add("Clamp");
        getEqInService.add("EquivalentInjection");

        List<String> getSvStInService= new LinkedList<>();
        getSvStInService.add("NonConformLoad");
        getSvStInService.add("Jumper");
        getSvStInService.add("EquivalentShunt");
        getSvStInService.add("Fuse");
        getSvStInService.add("Cut");
        getSvStInService.add("Junction");
        getSvStInService.add("PowerElectronicsConnection");
        getSvStInService.add("CsConverter");
        getSvStInService.add("EnergyConsumer");
        getSvStInService.add("AsynchronousMachine");
        getSvStInService.add("SynchronousMachine");
        getSvStInService.add("EquivalentBranch");
        getSvStInService.add("Clamp");
        getSvStInService.add("DisconnectingCircuitBreaker");
        getSvStInService.add("EquivalentInjection");
        getSvStInService.add("StationSupply");
        getSvStInService.add("BusbarSection");
        getSvStInService.add("ACLineSegment");
        getSvStInService.add("StaticVarCompensator");
        getSvStInService.add("Disconnector");
        getSvStInService.add("GroundingImpedance");
        getSvStInService.add("LoadBreakSwitch");
        getSvStInService.add("PowerTransformer");
        getSvStInService.add("PetersenCoil");
        getSvStInService.add("Switch");
        getSvStInService.add("LinearShuntCompensator");
        getSvStInService.add("Ground");
        getSvStInService.add("SeriesCompensator");
        getSvStInService.add("GroundDisconnector");
        getSvStInService.add("VsConverter");
        getSvStInService.add("Breaker");
        getSvStInService.add("EnergySource");
        getSvStInService.add("NonlinearShuntCompensator");
        getSvStInService.add("ExternalNetworkInjection");
        getSvStInService.add("ConformLoad");


        Resource newSub = null;
        Property newPre;
        RDFNode newObj;

        List<String> excludeTPBDattributes= new LinkedList<>();
        excludeTPBDattributes.add("TopologicalNode.fromEndName");
        excludeTPBDattributes.add("TopologicalNode.fromEndNameTso");
        excludeTPBDattributes.add("TopologicalNode.fromEndIsoCode");
        excludeTPBDattributes.add("TopologicalNode.toEndName");
        excludeTPBDattributes.add("TopologicalNode.toEndNameTso");
        excludeTPBDattributes.add("TopologicalNode.toEndIsoCode");
        excludeTPBDattributes.add("TopologicalNode.boundaryPoint");

        //convert SV
        for (StmtIterator c = modelSV.listStatements(new SimpleSelector(null, RDF.type, (RDFNode) null)); c.hasNext(); ) { // loop on all classes
            Statement stmtC = c.next();
            String className = stmtC.getObject().asResource().getLocalName();
            if (!className.equals("FullModel")) {

                int hasMRid = 0;

                for (StmtIterator a = modelSV.listStatements(new SimpleSelector(stmtC.getSubject(), null, (RDFNode) null)); a.hasNext(); ) { // loop on all attributes
                    Statement stmtA = a.next();

                    Statement stmtArebase = rebaseStatement(stmtA, cim17NS, euNS);
                    newSub=stmtArebase.getSubject();
                    newPre=stmtArebase.getPredicate();
                    newObj=stmtArebase.getObject();

                    if (newPre.getLocalName().equals("VsConverter.uf")) {
                        newPre = ResourceFactory.createProperty(cim17NS, "VsConverter.uv");
                    }
                    convSVModel.add(ResourceFactory.createStatement(newSub, newPre, newObj));

                    if (newPre.getLocalName().equals("IdentifiedObject.mRID")) {
                        hasMRid = 1;
                    }
                }
                //add mrid if not there
                if (hasMRid == 0 && getMRIDSV.contains(stmtC.getObject().asResource().getLocalName())) {
                    convSVModel.add(ResourceFactory.createStatement(rebaseResource(stmtC.getSubject(), cim17NS), mrid, ResourceFactory.createPlainLiteral(stmtC.getSubject().getLocalName().substring(1))));
                }
            }
        }
        // add SvSwitch
        //check if in the EQ there is Switch or subclass in CGMES v2.4
        for (StmtIterator c = modelEQ.listStatements(new SimpleSelector(null, RDF.type, (RDFNode) null)); c.hasNext(); ) { // loop on all classes
            Statement stmtC = c.next();
            String className = stmtC.getObject().asResource().getLocalName();
            if (className.equals("Switch") || className.equals("GroundDisconnector") ||className.equals("Disconnector") ||className.equals("LoadBreakSwitch") ||className.equals("Breaker")){
                //get status in SSH
                Statement swopen = modelSSH.listStatements(new SimpleSelector(stmtC.getSubject(), ResourceFactory.createProperty(cim16NS,"Switch.open"), (RDFNode) null)).nextStatement();
                //add .locked in SSH
                convSSHModel.add(ResourceFactory.createStatement(rebaseResource(stmtC.getSubject(),cim17NS),ResourceFactory.createProperty(cim17NS,"Switch.locked"),ResourceFactory.createPlainLiteral("false")));
                //create the SvSwitch
                //Resource clRes=rebaseResource(stmtC.getSubject(), cim17NS);
                String uuidSvSwitch = String.valueOf(UUID.randomUUID());
                Resource clRes = ResourceFactory.createResource(euNS + "_" + uuidSvSwitch);
                convSVModel.add(ResourceFactory.createStatement(clRes,RDF.type,ResourceFactory.createProperty(cim17NS,"SvSwitch")));
                convSVModel.add(ResourceFactory.createStatement(clRes,ResourceFactory.createProperty(cim17NS,"SvSwitch.open"),swopen.getObject()));
                convSVModel.add(ResourceFactory.createStatement(clRes,ResourceFactory.createProperty(cim17NS,"SvSwitch.Switch"),ResourceFactory.createProperty(cim17NS,swopen.getSubject().getLocalName())));
            }
        }

        //clean up SvVoltage=0
        List<Statement> stmtDelete = new LinkedList<>();
        for (StmtIterator c = convSVModel.listStatements(new SimpleSelector(null, RDF.type, ResourceFactory.createProperty(cim17NS,"SvVoltage"))); c.hasNext(); ) { // loop on all classes
            Statement stmtC = c.next();
            float voltage = convSVModel.getRequiredProperty(stmtC.getSubject(),ResourceFactory.createProperty(cim17NS,"SvVoltage.v")).getFloat();
            if (voltage==0){
                stmtDelete.addAll(convSVModel.listStatements(new SimpleSelector(stmtC.getSubject(), null, (RDFNode)  null)).toList());
                Statement tnref = convSVModel.getRequiredProperty(stmtC.getSubject(), ResourceFactory.createProperty(cim17NS,"SvVoltage.TopologicalNode"));
                if (convSVModel.contains(ResourceFactory.createStatement(stmtC.getSubject(),ResourceFactory.createProperty(cim17NS,"TopologicalIsland.TopologicalNodes"),tnref.getObject()))){
                    stmtDelete.add(ResourceFactory.createStatement(stmtC.getSubject(),ResourceFactory.createProperty(cim17NS,"TopologicalIsland.TopologicalNodes"),tnref.getObject()));
                }
            }
        }
        convSVModel.remove(stmtDelete);

        //TODO see if something should be done for SvPowerFlow = 0

        //convert TP
        //TODO the 2 DC related associations
        for (StmtIterator c = modelTP.listStatements(new SimpleSelector(null, RDF.type, (RDFNode) null)); c.hasNext(); ) { // loop on all classes
            Statement stmtC = c.next();
            String className = stmtC.getObject().asResource().getLocalName();
            if (className.equals("Terminal")) {
                RDFNode tnProp = modelTP.getRequiredProperty(stmtC.getSubject(),ResourceFactory.createProperty(cim16NS,"Terminal.TopologicalNode")).getObject();
                Resource tnRes = tnProp.asResource();
                if (modelTPBD.listStatements(new SimpleSelector(tnRes,null,(RDFNode) null)).hasNext()){
                    Statement stmtArebase = null;
                    for (StmtIterator a = modelTPBD.listStatements(new SimpleSelector(tnRes,null,(RDFNode) null)); a.hasNext(); ) { // loop on all attributes
                        Statement stmtA = a.next();
                        stmtArebase = rebaseStatement(stmtA, cim17NS, euNS);
                        if (!excludeTPBDattributes.contains(stmtA.getPredicate().getLocalName())) {
                            if (stmtA.getPredicate().getLocalName().equals(RDF.type.toString())) {
                                convTPModel.add(stmtArebase.getSubject(), RDF.type, ResourceFactory.createProperty(cim17NS, "TopologicalNode"));
                            }else{
                                convTPModel.add(stmtArebase.getSubject(), stmtArebase.getPredicate(), stmtArebase.getObject());
                            }
                            convTPModel.add(stmtArebase.getSubject(), mrid, ResourceFactory.createPlainLiteral(stmtArebase.getSubject().getLocalName().split("_",2)[1]));

                        }
                    }
                    Resource cnRes = modelTPBD.listStatements(new SimpleSelector(null,ResourceFactory.createProperty(cim16NS,"ConnectivityNode.TopologicalNode"),tnProp)).next().getSubject();
                    convTPModel.add(ResourceFactory.createResource(cim17NS+cnRes.getLocalName()), RDF.type, ResourceFactory.createProperty(cim17NS, "ConnectivityNode"));
                    convEQModel.add(stmtC.getSubject(),ResourceFactory.createProperty(cim17NS,"Terminal.ConnectivityNode"),ResourceFactory.createProperty(cim17NS,cnRes.getLocalName()));
                    assert stmtArebase != null;
                    convTPModel.add(ResourceFactory.createResource(cim17NS+cnRes.getLocalName()), ResourceFactory.createProperty(cim17NS,"ConnectivityNode.TopologicalNode"), stmtArebase.getSubject());
                }
            }
        }

        //check if EQ has ConnectivityNodes
        int hasCN=0;
        if (modelEQ.listStatements(null,RDF.type,ResourceFactory.createProperty(cim16NS,"ConnectivityNode")).hasNext()){
            hasCN=1;
        }
        // convert TN of TP
        for (StmtIterator c = modelTP.listStatements(new SimpleSelector(null, RDF.type, (RDFNode) null)); c.hasNext(); ) { // loop on all classes
            Statement stmtC = c.next();
            String className = stmtC.getObject().asResource().getLocalName();
            if (!className.equals("FullModel")) {

                int hasMRid = 0;
                Resource newCNres = null;

                if (hasCN == 0 && className.equals("TopologicalNode")) { // need to create CN and add it to EQ and link here in TP
                    String uuidCN = String.valueOf(UUID.randomUUID());
                    newCNres = ResourceFactory.createResource(cim17NS + "_" + uuidCN);
                    convEQModel.add(ResourceFactory.createStatement(newCNres, RDF.type, ResourceFactory.createProperty(cim17NS, "ConnectivityNode")));
                    convEQModel.add(ResourceFactory.createStatement(newCNres, mrid, ResourceFactory.createPlainLiteral(uuidCN)));
                    convTPModel.add(ResourceFactory.createStatement(newCNres, RDF.type, ResourceFactory.createProperty(cim17NS, "ConnectivityNode")));
                    convTPModel.add(ResourceFactory.createStatement(newCNres, ResourceFactory.createProperty(cim17NS, "ConnectivityNode.TopologicalNode"), ResourceFactory.createProperty(stmtC.getSubject().toString())));
                }

                for (StmtIterator a = modelTP.listStatements(new SimpleSelector(stmtC.getSubject(), null, (RDFNode) null)); a.hasNext(); ) { // loop on all attributes
                    Statement stmtA = a.next();

                    Statement stmtArebase = rebaseStatement(stmtA, cim17NS, euNS);
                    newSub=stmtArebase.getSubject();
                    newPre=stmtArebase.getPredicate();
                    newObj=stmtArebase.getObject();

                    convTPModel.add(ResourceFactory.createStatement(newSub, newPre, newObj));

                    if (newPre.getLocalName().equals("IdentifiedObject.mRID")) {
                        hasMRid = 1;
                    }
                    if (hasCN == 0 && className.equals("TopologicalNode")) {
                        if (newPre.getLocalName().equals("IdentifiedObject.name")) {
                            convEQModel.add(ResourceFactory.createStatement(newCNres, newPre, newObj));
                        }
                        if (newPre.getLocalName().equals("TopologicalNode.ConnectivityNodeContainer")) {
                            convEQModel.add(ResourceFactory.createStatement(newCNres, ResourceFactory.createProperty(cim17NS, "ConnectivityNode.ConnectivityNodeContainer"), newObj));
                        }

                        for (StmtIterator t = modelTP.listStatements(new SimpleSelector(null,ResourceFactory.createProperty(cim16NS, "Terminal.TopologicalNode"),stmtC.getSubject())); t.hasNext(); ) { // loop on all classes
                            Statement stmtT = t.next();
                            convEQModel.add(ResourceFactory.createStatement(stmtT.getSubject(),ResourceFactory.createProperty(cim17NS, "Terminal.ConnectivityNode"),ResourceFactory.createProperty(newCNres.toString())));
                        }
                    }

                }
                //add mrid if not there
                if (hasMRid == 0 && getMRIDTP.contains(stmtC.getObject().asResource().getLocalName())) {
                    convTPModel.add(ResourceFactory.createStatement(rebaseResource(stmtC.getSubject(), cim17NS), mrid, ResourceFactory.createPlainLiteral(stmtC.getSubject().getLocalName().substring(1))));
                }
            }
        }

        //convert SSH
        for (StmtIterator c = modelSSH.listStatements(new SimpleSelector(null, RDF.type, (RDFNode) null)); c.hasNext(); ) { // loop on all classes
            Statement stmtC = c.next();
            String className = stmtC.getObject().asResource().getLocalName();
            if (!className.equals("FullModel")) {
                int hasMRid = 0;

                for (StmtIterator a = modelSSH.listStatements(new SimpleSelector(stmtC.getSubject(), null, (RDFNode) null)); a.hasNext(); ) { // loop on all attributes
                    Statement stmtA = a.next();

                    Statement stmtArebase = rebaseStatement(stmtA, cim17NS, euNS);
                    newSub=stmtArebase.getSubject();
                    newPre=stmtArebase.getPredicate();
                    newObj=stmtArebase.getObject();

                    if (className.equals("EquivalentInjection") && (newPre.getLocalName().equals("EquivalentInjection.regulationStatus") || newPre.getLocalName().equals("EquivalentInjection.regulationTarget"))){
                        if (modelEQ.listStatements(new SimpleSelector(stmtC.getSubject(),ResourceFactory.createProperty(cim16NS,"EquivalentInjection.regulationCapability"), (RDFNode) null)).hasNext()){
                            String regulationCapability=modelEQ.listStatements(new SimpleSelector(stmtC.getSubject(),ResourceFactory.createProperty(cim16NS,"EquivalentInjection.regulationCapability"), (RDFNode) null)).next().getObject().toString();
                            if (regulationCapability.equals("true")){
                                convSSHModel.add(ResourceFactory.createStatement(newSub, newPre, newObj));
                            }
                        }
                    }else {
                        convSSHModel.add(ResourceFactory.createStatement(newSub, newPre, newObj));
                    }

                    if (newPre.getLocalName().equals("IdentifiedObject.mRID")) {
                        hasMRid = 1;
                    }
                }
                //add mrid if not there
                if (hasMRid == 0 && getMRIDSSH.contains(stmtC.getObject().asResource().getLocalName())) {
                    convSSHModel.add(ResourceFactory.createStatement(rebaseResource(stmtC.getSubject(), cim17NS), mrid, ResourceFactory.createPlainLiteral(stmtC.getSubject().getLocalName().substring(1))));
                }
            }
        }
        //add limits to SSH
        for (StmtIterator c = modelEQ.listStatements(new SimpleSelector(null, RDF.type, (RDFNode) null)); c.hasNext(); ) { // loop on all classes
            Statement stmtC = c.next();

            String className = stmtC.getObject().asResource().getLocalName();
            if (className.equals("VoltageLimit") || className.equals("CurrentLimit") || className.equals("ApparentPowerLimit") || className.equals("ActivePowerLimit")){
                convSSHModel.add(ResourceFactory.createStatement(rebaseResource(stmtC.getSubject(), cim17NS),RDF.type,rebaseRDFNode(stmtC.getObject(), cim17NS)));
                RDFNode limvalue = modelEQ.getRequiredProperty(stmtC.getSubject(),ResourceFactory.createProperty(cim16NS,className+".value")).getObject();
                convSSHModel.add(ResourceFactory.createStatement(rebaseResource(stmtC.getSubject(), cim17NS),ResourceFactory.createProperty(cim17NS,className+".value"),limvalue));
            }
        }




        //convert EQ
        for (StmtIterator c = modelEQ.listStatements(new SimpleSelector(null, RDF.type, (RDFNode) null)); c.hasNext(); ) { // loop on all classes
            Statement stmtC = c.next();
            String className = stmtC.getObject().asResource().getLocalName();
            if (!className.equals("FullModel")) {
                int hasMRid = 0;
                int hasDir = 0;
                int hasTerSeqNum = 0;
                int hasContainment = 0;

                for (StmtIterator a = modelEQ.listStatements(new SimpleSelector(stmtC.getSubject(), null, (RDFNode) null)); a.hasNext(); ) { // loop on all attributes
                    Statement stmtA = a.next();

                    Statement stmtArebase = rebaseStatement(stmtA, cim17NS, euNS);
                    newSub=stmtArebase.getSubject();
                    newPre=stmtArebase.getPredicate();
                    newObj=stmtArebase.getObject();

                    //fix operational limits
                    if (className.equals("VoltageLimit") || className.equals("CurrentLimit") || className.equals("ApparentPowerLimit") || className.equals("ActivePowerLimit")) {
                        if (newPre.getLocalName().contains(".value")) {
                            newPre = ResourceFactory.createProperty(cim17NS, className + ".normalValue");
                        }
                    }

                    //fix operational limit type
                    if (className.equals("OperationalLimitType")) {
                        if (newPre.getLocalName().equals("OperationalLimitType.limitType")) {
                            newPre = ResourceFactory.createProperty(euNS, "OperationalLimitType.kind");
                            String kindType = null;
                            if (newObj.toString().contains(".patl")) {
                                kindType = ".patl";
                                convEQModel.add(ResourceFactory.createStatement(newSub, ResourceFactory.createProperty(cim17NS, "OperationalLimitType.isInfiniteDuration"), ResourceFactory.createPlainLiteral("true")));
                            } else if (newObj.toString().contains(".patlt")) {
                                kindType = ".patlt";
                                convEQModel.add(ResourceFactory.createStatement(newSub, ResourceFactory.createProperty(cim17NS, "OperationalLimitType.isInfiniteDuration"), ResourceFactory.createPlainLiteral("false")));
                            } else if (newObj.toString().contains(".tatl")) {
                                kindType = ".tatl";
                                convEQModel.add(ResourceFactory.createStatement(newSub, ResourceFactory.createProperty(cim17NS, "OperationalLimitType.isInfiniteDuration"), ResourceFactory.createPlainLiteral("false")));
                            } else if (newObj.toString().contains(".tc")) {
                                kindType = ".tc";
                                convEQModel.add(ResourceFactory.createStatement(newSub, ResourceFactory.createProperty(cim17NS, "OperationalLimitType.isInfiniteDuration"), ResourceFactory.createPlainLiteral("false")));
                            } else if (newObj.toString().contains(".tct")) {
                                kindType = ".tct";
                                convEQModel.add(ResourceFactory.createStatement(newSub, ResourceFactory.createProperty(cim17NS, "OperationalLimitType.isInfiniteDuration"), ResourceFactory.createPlainLiteral("false")));
                            } else if (newObj.toString().contains(".highVoltage")) {
                                kindType = ".highVoltage";
                                convEQModel.add(ResourceFactory.createStatement(newSub, ResourceFactory.createProperty(cim17NS, "OperationalLimitType.isInfiniteDuration"), ResourceFactory.createPlainLiteral("true")));
                            } else if (newObj.toString().contains(".lowVoltage")) {
                                kindType = ".lowVoltage";
                                convEQModel.add(ResourceFactory.createStatement(newSub, ResourceFactory.createProperty(cim17NS, "OperationalLimitType.isInfiniteDuration"), ResourceFactory.createPlainLiteral("true")));
                            }
                            newObj = ResourceFactory.createProperty(euNS, "LimitKind" + kindType);
                        }
                    }
                    //fix operational limit set
                    if (className.equals("OperationalLimitSet")) {
                        if (newPre.getLocalName().equals("OperationalLimitSet.Equipment")){
                            if (!modelEQ.listStatements(new SimpleSelector(stmtA.getSubject(),ResourceFactory.createProperty(cim16NS,"OperationalLimitSet.Terminal"),(RDFNode) null)).hasNext()){
                                Resource termRes = modelEQ.listStatements(new SimpleSelector(null,ResourceFactory.createProperty(cim16NS,"Terminal.ConductingEquipment"),stmtA.getObject())).next().getSubject();
                                convEQModel.add(newSub,ResourceFactory.createProperty(cim17NS,"OperationalLimitSet.Terminal"),ResourceFactory.createProperty(cim17NS,termRes.getLocalName()));
                            }

                        }

                    }

                    if (newPre.getLocalName().equals("Equipment.aggregate")){
                        if (!className.equals("EquivalentBranch") && !className.equals("EquivalentShunt") && !className.equals("EquivalentInjection")) {
                            convEQModel.add(ResourceFactory.createStatement(newSub, newPre, newObj));
                        }
                    }else{
                        convEQModel.add(ResourceFactory.createStatement(newSub, newPre, newObj));
                    }

                    if (newPre.getLocalName().equals("IdentifiedObject.mRID")) {
                        hasMRid = 1;
                    }
                    if (newPre.getLocalName().equals("OperationalLimitType.direction")) {
                        hasDir = 1;
                    }
                    if (newPre.getLocalName().equals("ACDCTerminal.sequenceNumber")) {
                        hasTerSeqNum = 1;
                    }
                    if (newPre.getLocalName().equals("Equipment.EquipmentContainer")) {
                        hasContainment = 1;
                    }

                }
                assert newSub != null;
                //add Equipment.inservice to SSH
                if (getEqInService.contains(className)) {
                    //if contains SvStatus add Equipment.inService with the same status
                    if (modelSV.contains(ResourceFactory.createResource(cim16NS+newSub.getLocalName()),ResourceFactory.createProperty(cim16NS, "SvStatus.inService"))){
                        Statement oldObj = modelSV.getRequiredProperty(ResourceFactory.createResource(cim16NS+newSub.getLocalName()),ResourceFactory.createProperty(cim16NS, "SvStatus.inService"));
                        if (!modelSSH.listStatements(new SimpleSelector(null, RDF.type, ResourceFactory.createProperty(cim16NS, className))).hasNext()) {
                            convSSHModel.add(ResourceFactory.createStatement(newSub, RDF.type, ResourceFactory.createProperty(cim17NS, "Equipment")));
                        }
                        convSSHModel.add(ResourceFactory.createStatement(newSub, ResourceFactory.createProperty(cim17NS, "Equipment.inService"), oldObj.getObject()));
                    }else {
                        //if it does not contain SvStatus, check ACDCTerminal.connected in SSH. If 1 or 2 Terminal device => connected false means inservice false; if 3 terminals if 2 terminals are true then inservice is true
                        //if Terminal.connected is true check is there is Switch and check the switch status. But the switch has to be related to the equipment
                        if (hasCN==1){ // there are ConnectivityNodes in EQ
                            List<Statement> stmtList = modelEQ.listStatements(new SimpleSelector(null, ResourceFactory.createProperty(cim16NS,"Terminal.ConductingEquipment"),ResourceFactory.createProperty(cim16NS+newSub.getLocalName()))).toList();
                            int countTerm = stmtList.size();
                            if (countTerm==1){
                                Resource termRes = stmtList.get(0).getSubject();
                            }else if (countTerm==2){

                            }else if (countTerm>2){

                            }
                        }else{

                        }
                    }


                    if (!modelSSH.listStatements(new SimpleSelector(null, RDF.type, ResourceFactory.createProperty(cim16NS, className))).hasNext()) {
                        convSSHModel.add(ResourceFactory.createStatement(newSub, RDF.type, ResourceFactory.createProperty(cim17NS, "Equipment")));
                    }
                    convSSHModel.add(ResourceFactory.createStatement(newSub, ResourceFactory.createProperty(cim17NS, "Equipment.inService"), ResourceFactory.createPlainLiteral("true")));
                }

                //add SvStatus.inservice to SV
                if (getSvStInService.contains(className)) {
                    //if (!modelSV.contains(ResourceFactory.createResource(cim16NS+newSub.getLocalName()),ResourceFactory.createProperty(cim16NS, "SvStatus.inService"))) {
                    if (!modelSV.listStatements(new SimpleSelector(null,ResourceFactory.createProperty(cim16NS, "SvStatus.ConductingEquipment"),ResourceFactory.createProperty(newSub.toString()))).hasNext()){
                        //Statement oldObj = modelSV.getRequiredProperty(ResourceFactory.createResource(cim16NS+newSub.getLocalName()),ResourceFactory.createProperty(cim16NS, "SvStatus.inService"));
                        //Resource newSvStatusres = ResourceFactory.createResource(cim17NS + newSub.getLocalName());
                        //convSVModel.add(ResourceFactory.createStatement(newSvStatusres, RDF.type, ResourceFactory.createProperty(cim17NS, "SvStatus")));
                        //convSVModel.add(ResourceFactory.createStatement(newSvStatusres, ResourceFactory.createProperty(cim17NS, "SvStatus.inService"), oldObj.getObject()));
                        //convSVModel.add(ResourceFactory.createStatement(newSvStatusres, ResourceFactory.createProperty(cim17NS, "SvStatus.ConductingEquipment"), ResourceFactory.createProperty(newSub.toString())));
                        // }else {
                        String uuidSvStatus = String.valueOf(UUID.randomUUID());
                        Resource newSvStatusres = ResourceFactory.createResource(cim17NS + "_" + uuidSvStatus);
                        convSVModel.add(ResourceFactory.createStatement(newSvStatusres, RDF.type, ResourceFactory.createProperty(cim17NS, "SvStatus")));
                        convSVModel.add(ResourceFactory.createStatement(newSvStatusres, ResourceFactory.createProperty(cim17NS, "SvStatus.inService"), ResourceFactory.createPlainLiteral("true")));
                        convSVModel.add(ResourceFactory.createStatement(newSvStatusres, ResourceFactory.createProperty(cim17NS, "SvStatus.ConductingEquipment"), ResourceFactory.createProperty(newSub.toString())));
                    }
                }
                //add mrid if not there
                if (hasMRid == 0 && getMRIDEQ.contains(stmtC.getObject().asResource().getLocalName())) {
                    convEQModel.add(ResourceFactory.createStatement(rebaseResource(stmtC.getSubject(), cim17NS), mrid, ResourceFactory.createPlainLiteral(stmtC.getSubject().getLocalName().substring(1))));
                }
                //add OperationalLimitType.direction if not there
                if (hasDir == 0 && className.equals("OperationalLimitType")) {
                    convEQModel.add(ResourceFactory.createStatement(rebaseResource(stmtC.getSubject(), cim17NS), ResourceFactory.createProperty(cim17NS, "OperationalLimitType.direction"), ResourceFactory.createProperty(cim17NS, "OperationalLimitDirectionKind.absoluteValue")));
                }

                //add ACDCTerminal.sequenceNumber if not there
                if (className.equals("Terminal")) {
                    if (hasTerSeqNum == 0) {
                        Statement condEQ = modelEQ.getRequiredProperty(stmtC.getSubject(),ResourceFactory.createProperty(cim16NS,"Terminal.ConductingEquipment"));
                        List<Statement> terminals = modelEQ.listStatements(new SimpleSelector(null,ResourceFactory.createProperty(cim16NS,"Terminal.ConductingEquipment"),condEQ.getObject())).toList();
                        if (terminals.size()==1){
                            convEQModel.add(rebaseResource(stmtC.getSubject(), cim17NS),ResourceFactory.createProperty(cim17NS,"ACDCTerminal.sequenceNumber"),ResourceFactory.createPlainLiteral("1"));

                        }else{
                            Statement condEQnameStmt = modelEQ.getRequiredProperty(condEQ.getObject().asResource(),RDF.type);
                            String condEQname = condEQnameStmt.getObject().asResource().getLocalName();
                            if (condEQname.equals("PowerTransformer")){
                                for (StmtIterator pt = modelEQ.listStatements(new SimpleSelector(null, ResourceFactory.createProperty(cim16NS,"PowerTransformerEnd.PowerTransformer"), condEQ.getObject())); pt.hasNext(); ) { // loop on all classes
                                    Statement stmtPT = pt.next();
                                    RDFNode endTerminal = modelEQ.getRequiredProperty(stmtPT.getSubject(),ResourceFactory.createProperty(cim16NS,"TransformerEnd.Terminal")).getObject();
                                    if (endTerminal.asResource().getLocalName().equals(stmtC.getSubject().getLocalName())) {
                                        String endnumber = modelEQ.getRequiredProperty(stmtPT.getSubject(), ResourceFactory.createProperty(cim16NS, "TransformerEnd.endNumber")).getObject().toString();
                                        convEQModel.add(rebaseResource(stmtC.getSubject(), cim17NS), ResourceFactory.createProperty(cim17NS, "ACDCTerminal.sequenceNumber"), ResourceFactory.createPlainLiteral(endnumber));
                                    }
                                }
                            }else{
                                int count =1;
                                for (Statement st : terminals) {
                                    if (!convEQModel.listStatements(new SimpleSelector(st.getSubject(),ResourceFactory.createProperty(cim17NS, "ACDCTerminal.sequenceNumber"),(RDFNode) null)).hasNext()) {
                                        convEQModel.add(rebaseResource(stmtC.getSubject(), cim17NS), ResourceFactory.createProperty(cim17NS, "ACDCTerminal.sequenceNumber"), ResourceFactory.createPlainLiteral(Integer.toString(count)));
                                    }
                                    count = count+1;
                                }
                            }
                        }
                    }
                }

                //Add line is missing' also assumes that there are no lines in the model
                if (className.equals("ACLineSegment")) {
                    if (hasContainment == 0) {

                        String uuidLine = String.valueOf(UUID.randomUUID());
                        Resource newLineres = ResourceFactory.createResource(cim17NS + "_" + uuidLine);
                        convEQModel.add(ResourceFactory.createStatement(newLineres, RDF.type, ResourceFactory.createProperty(cim17NS, "Line")));
                        convEQModel.add(ResourceFactory.createStatement(newLineres, ResourceFactory.createProperty(cim17NS, "IdentifiedObject.name"), ResourceFactory.createPlainLiteral("new line")));
                        convEQModel.add(ResourceFactory.createStatement(newLineres, mrid, ResourceFactory.createPlainLiteral(uuidLine)));

                        convEQModel.add(ResourceFactory.createStatement(rebaseResource(stmtC.getSubject(), cim17NS), ResourceFactory.createProperty(cim17NS, "Equipment.EquipmentContainer"), ResourceFactory.createProperty(newLineres.toString())));

                    }
                }
            }
        }

        //add the model to the map
        convertedModelMap.put("EQ",convEQModel);
        convertedModelMap.put("SSH",convSSHModel);
        convertedModelMap.put("SV",convSVModel);
        convertedModelMap.put("TP",convTPModel);

        //save the borders
        saveInstanceModelData(convertedModelMap, saveProperties, loadDataMap.get("profileModelMap"));

    }

    //Split boundary per TSO border
    public static void SplitBoundaryPerBorder() throws IOException {


        Map<String, Map> loadDataMap= new HashMap<>();
        String xmlBase = "http://iec.ch/TC57/CIM100";
        //String xmlBase = "";

        //set properties for the export

        Map<String, Object> saveProperties = new HashMap<>();

        saveProperties.put("filename", "test");
        saveProperties.put("showXmlDeclaration", "true");
        saveProperties.put("showDoctypeDeclaration", "false");
        saveProperties.put("tab", "2");
        saveProperties.put("relativeURIs", "same-document");
        saveProperties.put("showXmlEncoding", "true");
        saveProperties.put("xmlBase", xmlBase);
        saveProperties.put("rdfFormat", CustomRDFFormat.RDFXML_CUSTOM_PLAIN_PRETTY);
        //saveProperties.put("rdfFormat", CustomRDFFormat.RDFXML_CUSTOM_PLAIN);
        saveProperties.put("useAboutRules", true); //switch to trigger file chooser and adding the property
        saveProperties.put("useEnumRules", true); //switch to trigger special treatment when Enum is referenced
        saveProperties.put("useFileDialog", false);
        saveProperties.put("fileFolder", "C:");
        saveProperties.put("dozip", false);
        saveProperties.put("instanceData", "true"); //this is to only print the ID and not with namespace
        saveProperties.put("showXmlBaseDeclaration", "false");

        saveProperties.put("putHeaderOnTop", true);
        saveProperties.put("headerClassResource", "http://iec.ch/TC57/61970-552/ModelDescription/1#FullModel");
        saveProperties.put("extensionName", "RDF XML");
        saveProperties.put("fileExtension", "*.xml");
        saveProperties.put("fileDialogTitle", "Save RDF XML for");
        //RDFFormat rdfFormat=RDFFormat.RDFXML;
        //RDFFormat rdfFormat=RDFFormat.RDFXML_PLAIN;
        //RDFFormat rdfFormat = RDFFormat.RDFXML_ABBREV;
        //RDFFormat rdfFormat = CustomRDFFormat.RDFXML_CUSTOM_PLAIN_PRETTY;
        //RDFFormat rdfFormat = CustomRDFFormat.RDFXML_CUSTOM_PLAIN;



        Map<String,ArrayList<Object>> profileDataMap=new HashMap<>();
        Map<String,Model> profileDataMapAsModel=new HashMap<>();

        // load all profile models
        Map<String,Model> profileModelMap = null;


        loadDataMap.put("profileDataMap",profileDataMap);
        loadDataMap.put("profileDataMapAsModel",profileDataMapAsModel);
        loadDataMap.put("profileModelMap",profileModelMap);

        // load base instance models
        FileChooser filechooser = new FileChooser();
        filechooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Boundary equipment", "*.xml"));
        filechooser.setInitialDirectory(new File(MainController.prefs.get("LastWorkingFolder","")));
        File file;

        try {
            file = filechooser.showOpenDialog(null);
        } catch (Exception k){
            filechooser.setInitialDirectory(new File("C:\\\\"));
            file = filechooser.showOpenDialog(null);
        }
        List<File> baseInstanceModelFiles = new LinkedList<>();
        if (file != null) {// the file is selected

            baseInstanceModelFiles.add(file);

            Map<String,Model> baseInstanceModelMap = InstanceDataFactory.modelLoad(baseInstanceModelFiles, xmlBase, null);
            loadDataMap.put("baseInstanceModelMap",baseInstanceModelMap);

        }
        Map<String,Model> instanceModelMap= loadDataMap.get("baseInstanceModelMap");
        Model instanceModelBD = null;
        //Model instanceModelBD = org.apache.jena.rdf.model.ModelFactory.createDefaultModel();
        for (Map.Entry<String, Model> entry : instanceModelMap.entrySet()) {
            if (!entry.getKey().equals("unionModel") && !entry.getKey().equals("modelUnionWithoutHeader")) {
                if (entry.getKey().equals("EQBD")) {
                    instanceModelBD=entry.getValue();
                }
            }

        }

        Map<String,Model> newBDModelMap=new HashMap<>();

        assert instanceModelBD != null;
        for (ResIterator i = instanceModelBD.listSubjectsWithProperty(RDF.type,ResourceFactory.createProperty("http://iec.ch/TC57/CIM100-European#BoundaryPoint")); i.hasNext(); ) {
            Resource resItem = i.next();
            String fromTSOname = instanceModelBD.getRequiredProperty(resItem,ResourceFactory.createProperty("http://iec.ch/TC57/CIM100-European#BoundaryPoint.fromEndNameTso")).getObject().toString();
            String toTSOname = instanceModelBD.getRequiredProperty(resItem,ResourceFactory.createProperty("http://iec.ch/TC57/CIM100-European#BoundaryPoint.toEndNameTso")).getObject().toString();

            if (newBDModelMap.containsKey("Border-"+fromTSOname+"-"+toTSOname+".xml")){
                Model borderModel = newBDModelMap.get("Border-"+fromTSOname+"-"+toTSOname+".xml");
                //add the BoundaryPoint
                borderModel=addBP(instanceModelBD,borderModel,resItem);
                newBDModelMap.put("Border-"+fromTSOname+"-"+toTSOname+".xml",borderModel);

            }else if (newBDModelMap.containsKey("Border-"+toTSOname+"-"+fromTSOname+".xml")){
                Model borderModel = newBDModelMap.get("Border-"+toTSOname+"-"+fromTSOname+".xml");
                //add the BoundaryPoint
                borderModel=addBP(instanceModelBD,borderModel,resItem);
                newBDModelMap.put("Border-"+toTSOname+"-"+fromTSOname+".xml",borderModel);

            }else{
                Model newBoderModel = org.apache.jena.rdf.model.ModelFactory.createDefaultModel();
                newBoderModel.setNsPrefixes(instanceModelBD.getNsPrefixMap());
                //add the header statements
                Resource headerRes=ResourceFactory.createResource("urn:uuid:"+UUID.randomUUID());
                newBoderModel.add(ResourceFactory.createStatement(headerRes,RDF.type,ResourceFactory.createProperty("http://iec.ch/TC57/61970-552/ModelDescription/1#FullModel")));
                for (StmtIterator n = instanceModelBD.listStatements(new SimpleSelector(instanceModelBD.listSubjectsWithProperty(RDF.type,ResourceFactory.createProperty("http://iec.ch/TC57/61970-552/ModelDescription/1#FullModel")).nextResource(), null,(RDFNode) null)); n.hasNext();) {
                    Statement stmt = n.next();
                    newBoderModel.add(ResourceFactory.createStatement(headerRes,stmt.getPredicate(),stmt.getObject()));
                }
                //add the BoundaryPoint
                newBoderModel=addBP(instanceModelBD,newBoderModel,resItem);
                //add the model to the map
                newBDModelMap.put("Border-"+fromTSOname+"-"+toTSOname+".xml",newBoderModel);
            }
        }


        //save the borders
        saveInstanceModelData(newBDModelMap, saveProperties, profileModelMap);

    }

    //Split Boundary and Reference data (CGMES v3.0)
    public static void SplitBoundaryAndRefData() throws IOException {


        Map<String, Map> loadDataMap= new HashMap<>();
        String xmlBase = "http://iec.ch/TC57/CIM100";
        //String xmlBase = "";

        //set properties for the export

        Map<String, Object> saveProperties = new HashMap<>();

        saveProperties.put("filename", "test");
        saveProperties.put("showXmlDeclaration", "true");
        saveProperties.put("showDoctypeDeclaration", "false");
        saveProperties.put("tab", "2");
        saveProperties.put("relativeURIs", "same-document");
        saveProperties.put("showXmlEncoding", "true");
        saveProperties.put("xmlBase", xmlBase);
        saveProperties.put("rdfFormat", CustomRDFFormat.RDFXML_CUSTOM_PLAIN_PRETTY);
        //saveProperties.put("rdfFormat", CustomRDFFormat.RDFXML_CUSTOM_PLAIN);
        saveProperties.put("useAboutRules", true); //switch to trigger file chooser and adding the property
        saveProperties.put("useEnumRules", true); //switch to trigger special treatment when Enum is referenced
        saveProperties.put("useFileDialog", false);
        saveProperties.put("fileFolder", "C:");
        saveProperties.put("dozip", false);
        saveProperties.put("instanceData", "true"); //this is to only print the ID and not with namespace
        saveProperties.put("showXmlBaseDeclaration", "false");

        saveProperties.put("putHeaderOnTop", true);
        saveProperties.put("headerClassResource", "http://iec.ch/TC57/61970-552/ModelDescription/1#FullModel");
        saveProperties.put("extensionName", "RDF XML");
        saveProperties.put("fileExtension", "*.xml");
        saveProperties.put("fileDialogTitle", "Save RDF XML for");
        //RDFFormat rdfFormat=RDFFormat.RDFXML;
        //RDFFormat rdfFormat=RDFFormat.RDFXML_PLAIN;
        //RDFFormat rdfFormat = RDFFormat.RDFXML_ABBREV;
        //RDFFormat rdfFormat = CustomRDFFormat.RDFXML_CUSTOM_PLAIN_PRETTY;
        //RDFFormat rdfFormat = CustomRDFFormat.RDFXML_CUSTOM_PLAIN;



        Map<String,ArrayList<Object>> profileDataMap=new HashMap<>();
        Map<String,Model> profileDataMapAsModel=new HashMap<>();

        // load all profile models
        Map<String,Model> profileModelMap = null;


        loadDataMap.put("profileDataMap",profileDataMap);
        loadDataMap.put("profileDataMapAsModel",profileDataMapAsModel);
        loadDataMap.put("profileModelMap",profileModelMap);

        // load base instance models
        FileChooser filechooser = new FileChooser();
        filechooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Boundary equipment", "*.xml"));
        filechooser.setInitialDirectory(new File(MainController.prefs.get("LastWorkingFolder","")));
        File file;

        try {
            file = filechooser.showOpenDialog(null);
        } catch (Exception k){
            filechooser.setInitialDirectory(new File("C:\\\\"));
            file = filechooser.showOpenDialog(null);
        }
        List<File> baseInstanceModelFiles = new LinkedList<>();
        if (file != null) {// the file is selected

            baseInstanceModelFiles.add(file);

            Map<String,Model> baseInstanceModelMap = InstanceDataFactory.modelLoad(baseInstanceModelFiles, xmlBase, null);
            loadDataMap.put("baseInstanceModelMap",baseInstanceModelMap);

        }
        Map<String,Model> instanceModelMap= loadDataMap.get("baseInstanceModelMap");
        Model instanceModelBD = null;
        //Model instanceModelBD = org.apache.jena.rdf.model.ModelFactory.createDefaultModel();
        for (Map.Entry<String, Model> entry : instanceModelMap.entrySet()) {
            if (!entry.getKey().equals("unionModel") && !entry.getKey().equals("modelUnionWithoutHeader")) {
                if (entry.getKey().equals("EQBD")) {
                    instanceModelBD=entry.getValue();
                }
            }

        }

        List<String> keepInBD= new LinkedList<>();
        keepInBD.add("ConnectivityNode");
        keepInBD.add("BoundaryPoint");
        keepInBD.add("Line");
        keepInBD.add("Substation");
        keepInBD.add("VoltageLevel");
        keepInBD.add("Bay");
        keepInBD.add("Terminal");
        keepInBD.add("Junction");
        keepInBD.add("FullModel");

        Map<String,Model> newBDModelMap=new HashMap<>();

        //create the new model for BP
        Model newBoderModel = org.apache.jena.rdf.model.ModelFactory.createDefaultModel();
        newBoderModel.setNsPrefix("cim","http://iec.ch/TC57/CIM100#");
        newBoderModel.setNsPrefix("eu","http://iec.ch/TC57/CIM100-European#");
        newBoderModel.setNsPrefix("md","http://iec.ch/TC57/61970-552/ModelDescription/1#");
        newBoderModel.setNsPrefix("rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#");

        //create the new model for ref data
        Model newRefModel = org.apache.jena.rdf.model.ModelFactory.createDefaultModel();
        newRefModel.setNsPrefix("cim","http://iec.ch/TC57/CIM100#");
        newRefModel.setNsPrefix("eu","http://iec.ch/TC57/CIM100-European#");
        newRefModel.setNsPrefix("md","http://iec.ch/TC57/61970-552/ModelDescription/1#");
        newRefModel.setNsPrefix("rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#");

        assert instanceModelBD != null;
        Map<String, String> oldPrefix = instanceModelBD.getNsPrefixMap();
        int keepExtensions = 1; // keep extensions
        if (oldPrefix.containsKey("cgmbp")){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setContentText("The Boundary Set includes non CIM (cgmbp) extensions. Do you want to keep them in the split Boundary Set?");
            alert.setHeaderText(null);
            alert.setTitle("Question - cgmbp extensions are present");
            ButtonType btnYes = new ButtonType("Yes");
            ButtonType btnNo = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(btnYes, btnNo);
            Optional<ButtonType> result = alert.showAndWait();

            if (result.get() == btnNo) {
                keepExtensions=0;
            }else{
                if (oldPrefix.containsKey("cgmbp")){
                    newBoderModel.setNsPrefix("cgmbp","http://entsoe.eu/CIM/Extensions/CGM-BP/2020#");
                    newRefModel.setNsPrefix("cgmbp","http://entsoe.eu/CIM/Extensions/CGM-BP/2020#");
                }
            }
        }


        for (StmtIterator i = instanceModelBD.listStatements(null,RDF.type,(RDFNode) null); i.hasNext(); ) {
            Statement stmt = i.next();
            if (keepExtensions==1) {
                if (keepInBD.contains(stmt.getObject().asResource().getLocalName())) {
                    for (StmtIterator k = instanceModelBD.listStatements(stmt.getSubject(), null, (RDFNode) null); k.hasNext(); ) {
                        Statement stmtKeep = k.next();
                        newBoderModel.add(stmtKeep);
                        if (stmt.getObject().asResource().getLocalName().equals("FullModel")){
                            newRefModel.add(stmtKeep);
                        }
                    }
                } else {
                    for (StmtIterator r = instanceModelBD.listStatements(stmt.getSubject(), null, (RDFNode) null); r.hasNext(); ) {
                        Statement stmtRef = r.next();
                        newRefModel.add(stmtRef);
                    }
                }
            }else{
                if (!stmt.getObject().asResource().getNameSpace().equals("http://entsoe.eu/CIM/Extensions/CGM-BP/2020#")){
                    if (keepInBD.contains(stmt.getObject().asResource().getLocalName())) {
                        for (StmtIterator k = instanceModelBD.listStatements(stmt.getSubject(), null, (RDFNode) null); k.hasNext(); ) {
                            Statement stmtKeep = k.next();
                            if (!stmtKeep.getPredicate().getNameSpace().equals("http://entsoe.eu/CIM/Extensions/CGM-BP/2020#")) {
                                newBoderModel.add(stmtKeep);
                                if (stmt.getObject().asResource().getLocalName().equals("FullModel")){
                                    newRefModel.add(stmtKeep);
                                }
                            }
                        }
                    } else {
                        for (StmtIterator r = instanceModelBD.listStatements(stmt.getSubject(), null, (RDFNode) null); r.hasNext(); ) {
                            Statement stmtRef = r.next();
                            if (!stmtRef.getPredicate().getNameSpace().equals("http://entsoe.eu/CIM/Extensions/CGM-BP/2020#")) {
                                newRefModel.add(stmtRef);
                            }
                        }
                    }
                }
            }
        }


        newBDModelMap.put("BoundaryData.xml",newBoderModel);
        newBDModelMap.put("ReferenceData.xml",newRefModel);
        //save the borders
        saveInstanceModelData(newBDModelMap, saveProperties, profileModelMap);

    }

    // Convert CGMES v2.4 Boundary Set to CGMES v3.0
    public static void ConvertBoundarySetCGMESv2v3() throws IOException {


        Map<String, Map> loadDataMap= new HashMap<>();
        String xmlBase = "http://iec.ch/TC57/CIM100";
        //String xmlBase = "";

        //set properties for the export

        Map<String, Object> saveProperties = new HashMap<>();

        saveProperties.put("filename", "test");
        saveProperties.put("showXmlDeclaration", "true");
        saveProperties.put("showDoctypeDeclaration", "false");
        saveProperties.put("tab", "2");
        saveProperties.put("relativeURIs", "same-document");
        saveProperties.put("showXmlEncoding", "true");
        saveProperties.put("xmlBase", xmlBase);
        saveProperties.put("rdfFormat", CustomRDFFormat.RDFXML_CUSTOM_PLAIN_PRETTY);
        //saveProperties.put("rdfFormat", CustomRDFFormat.RDFXML_CUSTOM_PLAIN);
        saveProperties.put("useAboutRules", true); //switch to trigger file chooser and adding the property
        saveProperties.put("useEnumRules", true); //switch to trigger special treatment when Enum is referenced
        saveProperties.put("useFileDialog", false);
        saveProperties.put("fileFolder", "C:");
        saveProperties.put("dozip", false);
        saveProperties.put("instanceData", "true"); //this is to only print the ID and not with namespace
        saveProperties.put("showXmlBaseDeclaration", "false");

        saveProperties.put("putHeaderOnTop", true);
        saveProperties.put("headerClassResource", "http://iec.ch/TC57/61970-552/ModelDescription/1#FullModel");
        saveProperties.put("extensionName", "RDF XML");
        saveProperties.put("fileExtension", "*.xml");
        saveProperties.put("fileDialogTitle", "Save RDF XML for");
        //RDFFormat rdfFormat=RDFFormat.RDFXML;
        //RDFFormat rdfFormat=RDFFormat.RDFXML_PLAIN;
        //RDFFormat rdfFormat = RDFFormat.RDFXML_ABBREV;
        //RDFFormat rdfFormat = CustomRDFFormat.RDFXML_CUSTOM_PLAIN_PRETTY;
        //RDFFormat rdfFormat = CustomRDFFormat.RDFXML_CUSTOM_PLAIN;



        Map<String,ArrayList<Object>> profileDataMap=new HashMap<>();
        Map<String,Model> profileDataMapAsModel=new HashMap<>();
        //Map<String,Model> conversionInstruction=new HashMap<>();

        // load all profile models
        Map<String,Model> profileModelMap = null;


        loadDataMap.put("profileDataMap",profileDataMap);
        loadDataMap.put("profileDataMapAsModel",profileDataMapAsModel);
        loadDataMap.put("profileModelMap",profileModelMap);
        loadDataMap.put("conversionInstruction",profileModelMap);



        //xmlBase = "http://iec.ch/TC57/2013/CIM-schema-cim16";
        // load base instance models
        FileChooser filechooser = new FileChooser();
        filechooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Boundary equipment", "*.xml"));
        filechooser.setInitialDirectory(new File(MainController.prefs.get("LastWorkingFolder","")));
        File file;

        try {
            file = filechooser.showOpenDialog(null);
        } catch (Exception k){
            filechooser.setInitialDirectory(new File("C:\\\\"));
            file = filechooser.showOpenDialog(null);
        }
        List<File> baseInstanceModelFiles = new LinkedList<>();
        if (file != null) {// the file is selected

            baseInstanceModelFiles.add(file);

            Map<String,Model> baseInstanceModelMap = InstanceDataFactory.modelLoad(baseInstanceModelFiles, xmlBase, null);
            loadDataMap.put("baseInstanceModelMap",baseInstanceModelMap);

        }
        Map<String,Model> instanceModelMap= loadDataMap.get("baseInstanceModelMap");
        Model instanceModelBD = null;
        //Model instanceModelBD = org.apache.jena.rdf.model.ModelFactory.createDefaultModel();
        for (Map.Entry<String, Model> entry : instanceModelMap.entrySet()) {
            if (!entry.getKey().equals("unionModel") && !entry.getKey().equals("modelUnionWithoutHeader")) {
                if (entry.getKey().equals("EQBD")) {
                    instanceModelBD=entry.getValue();
                }
            }

        }

        Map<String,Model> newBDModelMap=new HashMap<>();

        //create the new model
        Model newBoderModel = org.apache.jena.rdf.model.ModelFactory.createDefaultModel();
        newBoderModel.setNsPrefix("cim","http://iec.ch/TC57/CIM100#");
        newBoderModel.setNsPrefix("eu","http://iec.ch/TC57/CIM100-European#");
        newBoderModel.setNsPrefix("md","http://iec.ch/TC57/61970-552/ModelDescription/1#");
        newBoderModel.setNsPrefix("rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#");

        //check for extensions

        assert instanceModelBD != null;
        Map<String, String> oldPrefix = instanceModelBD.getNsPrefixMap();
        int keepExtensions = 1; // keep extensions
        if (oldPrefix.containsKey("cgmbp")){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setContentText("The Boundary Set includes non CIM (cgmbp) extensions. Do you want to keep them in the converted Boundary Set?");
            alert.setHeaderText(null);
            alert.setTitle("Question - cgmbp extensions are present");
            ButtonType btnYes = new ButtonType("Yes");
            ButtonType btnNo = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(btnYes, btnNo);
            Optional<ButtonType> result = alert.showAndWait();

            if (result.get() == btnNo) {
                keepExtensions=0;
            }else{
                if (oldPrefix.containsKey("cgmbp")){
                    newBoderModel.setNsPrefix("cgmbp","http://entsoe.eu/CIM/Extensions/CGM-BP/2020#");
                }
            }
        }

        //conversion process
        String cim17NS="http://iec.ch/TC57/CIM100#";
        String cim16NS="http://iec.ch/TC57/2013/CIM-schema-cim16#";
        String euNS="http://iec.ch/TC57/CIM100-European#";
        List<String> skipList= new LinkedList<>();
        skipList.add("ConnectivityNode.boundaryPoint");
        List<String> getMRID= new LinkedList<>();
        getMRID.add("ConnectivityNode");
        getMRID.add("Line");
        getMRID.add("EnergySchedulingType");
        getMRID.add("GeographicalRegion");
        getMRID.add("SubGeographicalRegion");
        getMRID.add("Terminal");
        getMRID.add("Substation");
        getMRID.add("BaseVoltage");
        getMRID.add("VoltageLevel");
        getMRID.add("Bay");
        getMRID.add("Junction");

        Property mrid=ResourceFactory.createProperty("http://iec.ch/TC57/CIM100#IdentifiedObject.mRID");


        for (StmtIterator i = instanceModelBD.listStatements(new SimpleSelector(null, RDF.type, (RDFNode) null)); i.hasNext(); ) { // loop on all classes
            Statement stmt = i.next();

            //add the header statements
            if (stmt.getObject().asResource().getLocalName().equals("FullModel")) {
                for (StmtIterator n = instanceModelBD.listStatements(new SimpleSelector(stmt.getSubject(), null,(RDFNode) null)); n.hasNext();) {
                    Statement stmtH = n.next();
                    if (stmtH.getPredicate().getLocalName().equals("Model.profile")){
                        newBoderModel.add(ResourceFactory.createStatement(stmtH.getSubject(),stmtH.getPredicate(), ResourceFactory.createPlainLiteral("http://iec.ch/TC57/ns/CIM/EquipmentBoundary-EU/3.0")));
                    }else {
                        newBoderModel.add(stmtH);
                    }
                }
            }else {
                Resource newSub;
                Property newPre;
                RDFNode newObj;
                Resource newBPres=null;

                if (stmt.getObject().asResource().getLocalName().equals("ConnectivityNode")) {
                    //Create BoundaryPoint
                    String uuidBP = String.valueOf(UUID.randomUUID());
                    newBPres = ResourceFactory.createResource(euNS + "_" + uuidBP);
                    RDFNode euBP = ResourceFactory.createProperty(euNS, "BoundaryPoint");
                    newBoderModel.add(ResourceFactory.createStatement(newBPres, RDF.type, euBP));
                    newBoderModel.add(ResourceFactory.createStatement(newBPres, mrid, ResourceFactory.createPlainLiteral(uuidBP)));
                    newBoderModel.add(ResourceFactory.createStatement(newBPres, ResourceFactory.createProperty(euNS,"BoundaryPoint.ConnectivityNode"), ResourceFactory.createProperty(stmt.getSubject().toString())));
                }

                if (!stmt.getObject().asResource().getLocalName().equals("Junction") && !stmt.getObject().asResource().getLocalName().equals("Terminal")) {// this is to filter Junction and Terminal. TODO create option in GUT to make this more flexible
                    //Add Terminal.ConnectivityNode
                    if (stmt.getObject().asResource().getLocalName().equals("Junction")) {
                        Statement EqCont = instanceModelBD.listStatements(new SimpleSelector(stmt.getSubject(), ResourceFactory.createProperty(cim16NS, "Equipment.EquipmentContainer"), (RDFNode) null)).next();
                        Statement conNode = instanceModelBD.listStatements(new SimpleSelector(null, ResourceFactory.createProperty(cim16NS, "ConnectivityNode.ConnectivityNodeContainer"), EqCont.getObject())).next();
                        Statement terminalJunction = instanceModelBD.listStatements(new SimpleSelector(null, ResourceFactory.createProperty(cim16NS, "Terminal.ConductingEquipment"), stmt.getSubject())).next();
                        if (!instanceModelBD.listStatements(new SimpleSelector(terminalJunction.getSubject(), ResourceFactory.createProperty(cim16NS, "Terminal.ConnectivityNode"), conNode.getSubject())).hasNext()) {
                            newBoderModel.add(terminalJunction.getSubject(), ResourceFactory.createProperty(cim17NS, "Terminal.ConnectivityNode"), conNode.getSubject());
                        }
                        if (!instanceModelBD.listStatements(new SimpleSelector(terminalJunction.getSubject(), ResourceFactory.createProperty(cim16NS, "ACDCTerminal.sequenceNumber"), (RDFNode) null)).hasNext()) {
                            newBoderModel.add(terminalJunction.getSubject(), ResourceFactory.createProperty(cim17NS, "ACDCTerminal.sequenceNumber"), ResourceFactory.createPlainLiteral("1"));
                        }
                    }
                    int addmrid = 1;

                    for (StmtIterator a = instanceModelBD.listStatements(new SimpleSelector(stmt.getSubject(), null, (RDFNode) null)); a.hasNext(); ) { // loop on all attributes
                        Statement stmtA = a.next();
                        if (!skipList.contains(stmtA.getPredicate().getLocalName())) {
                            if (stmtA.getSubject().getNameSpace().equals("http://iec.ch/TC57/2013/CIM-schema-cim16#")) {
                                newSub = rebaseResource(stmtA.getSubject(), cim17NS);
                            } else if (stmtA.getSubject().getNameSpace().equals("http://entsoe.eu/CIM/SchemaExtension/3/1#")) {
                                newSub = rebaseResource(stmtA.getSubject(), euNS);
                            } else {
                                newSub = stmtA.getSubject();
                            }
                            if (stmtA.getPredicate().getNameSpace().equals("http://iec.ch/TC57/2013/CIM-schema-cim16#")) {
                                newPre = rebaseProperty(stmtA.getPredicate(), cim17NS);
                            } else if (stmtA.getPredicate().getNameSpace().equals("http://entsoe.eu/CIM/SchemaExtension/3/1#")) {
                                newPre = rebaseProperty(stmtA.getPredicate(), euNS);
                            } else {
                                newPre = stmtA.getPredicate();
                            }
                            if (stmtA.getObject().isResource()) {
                                if (stmtA.getObject().asResource().getNameSpace().equals("http://iec.ch/TC57/2013/CIM-schema-cim16#")) {
                                    newObj = rebaseRDFNode(stmtA.getObject(), cim17NS);
                                } else if (stmtA.getObject().asResource().getNameSpace().equals("http://entsoe.eu/CIM/SchemaExtension/3/1#")) {
                                    newObj = rebaseRDFNode(stmtA.getObject(), euNS);
                                } else {
                                    newObj = stmtA.getObject();
                                }
                            } else {
                                newObj = stmtA.getObject();
                            }
                            if (stmt.getObject().asResource().getLocalName().equals("ConnectivityNode")) {
                                String stmtAPredicate = stmtA.getPredicate().getLocalName();
                                switch (stmtAPredicate) {
                                    case "ConnectivityNode.toEndName":
                                        newPre = ResourceFactory.createProperty(newPre.getNameSpace(), "BoundaryPoint.toEndName");
                                        newSub = newBPres;
                                        break;
                                    case "ConnectivityNode.fromEndName":
                                        newPre = ResourceFactory.createProperty(newPre.getNameSpace(), "BoundaryPoint.fromEndName");
                                        newSub = newBPres;
                                        break;
                                    case "ConnectivityNode.toEndNameTso":
                                        newPre = ResourceFactory.createProperty(newPre.getNameSpace(), "BoundaryPoint.toEndNameTso");
                                        newSub = newBPres;
                                        break;
                                    case "ConnectivityNode.toEndIsoCode":
                                        newPre = ResourceFactory.createProperty(newPre.getNameSpace(), "BoundaryPoint.toEndIsoCode");
                                        newSub = newBPres;
                                        break;
                                    case "ConnectivityNode.fromEndIsoCode":
                                        newPre = ResourceFactory.createProperty(newPre.getNameSpace(), "BoundaryPoint.fromEndIsoCode");
                                        newSub = newBPres;
                                        break;
                                    case "ConnectivityNode.fromEndNameTso":
                                        newPre = ResourceFactory.createProperty(newPre.getNameSpace(), "BoundaryPoint.fromEndNameTso");
                                        newSub = newBPres;
                                        break;
                                    case "IdentifiedObject.name":
                                        newPre = ResourceFactory.createProperty(newPre.getNameSpace(), "IdentifiedObject.name");
                                        newBoderModel.add(ResourceFactory.createStatement(newSub, newPre, newObj));
                                        newSub = newBPres;
                                        break;
                                    case "IdentifiedObject.description":
                                        newPre = ResourceFactory.createProperty(newPre.getNameSpace(), "IdentifiedObject.description");
                                        newBoderModel.add(ResourceFactory.createStatement(newSub, newPre, newObj));
                                        newSub = newBPres;
                                        Resource lineSub = instanceModelBD.getRequiredProperty(stmtA.getSubject(), ResourceFactory.createProperty(cim16NS, "ConnectivityNode.ConnectivityNodeContainer")).getObject().asResource();
                                        String lineDesc = instanceModelBD.getRequiredProperty(lineSub, ResourceFactory.createProperty(cim16NS, "IdentifiedObject.description")).getObject().toString();
                                        if (lineDesc.contains("HVDC")) {
                                            newBoderModel.add(ResourceFactory.createStatement(newSub, ResourceFactory.createProperty(euNS, "BoundaryPoint.isDirectCurrent"), ResourceFactory.createPlainLiteral("true")));
                                        }
                                        break;
                                    case "IdentifiedObject.shortName":
                                        newPre = ResourceFactory.createProperty(newPre.getNameSpace(), "IdentifiedObject.shortName");
                                        newBoderModel.add(ResourceFactory.createStatement(newSub, newPre, newObj));
                                        newSub = newBPres;
                                        break;
                                    case "IdentifiedObject.energyIdentCodeEic":
                                        newPre = ResourceFactory.createProperty(newPre.getNameSpace(), "IdentifiedObject.energyIdentCodeEic");
                                        newBoderModel.add(ResourceFactory.createStatement(newSub, newPre, newObj));
                                        newSub = newBPres;
                                        break;
                                }
                            }


                            newBoderModel.add(ResourceFactory.createStatement(newSub, newPre, newObj));
                        }
                        if (getMRID.contains(stmt.getObject().asResource().getLocalName()) && addmrid == 1) {
                            newBoderModel.add(ResourceFactory.createStatement(stmt.getSubject(), mrid, ResourceFactory.createPlainLiteral(stmt.getSubject().getLocalName().substring(1))));
                            addmrid = 0;
                        }
                    }
                }

            }

        }

        //filter extensions
        List<Statement> StmtDeleteList = new LinkedList<>();
        int deleteClass;
        if (keepExtensions==0){
            for (StmtIterator i = newBoderModel.listStatements(new SimpleSelector(null, RDF.type, (RDFNode) null)); i.hasNext(); ) { // loop on all classes
                Statement stmt = i.next();
                deleteClass=0;
                if (stmt.getObject().asResource().getNameSpace().equals("http://entsoe.eu/CIM/Extensions/CGM-BP/2020#")){
                    StmtDeleteList.add(stmt);
                    deleteClass=1;
                }
                for (StmtIterator a = newBoderModel.listStatements(new SimpleSelector(stmt.getSubject(), null, (RDFNode) null)); a.hasNext(); ) { // loop on all attributes
                    Statement stmtA = a.next();
                    if (deleteClass==1) {
                        StmtDeleteList.add(stmtA);
                    }else{
                        if (stmtA.getPredicate().getNameSpace().equals("http://entsoe.eu/CIM/Extensions/CGM-BP/2020#")) {
                            StmtDeleteList.add(stmtA);
                        }
                    }
                }
            }
            newBoderModel.remove(StmtDeleteList);
        }



        //add the model to the map
        newBDModelMap.put("ConvertedBoundaryCGMESv3"+".xml",newBoderModel);



        //save the borders
        saveInstanceModelData(newBDModelMap, saveProperties, profileModelMap);

    }


    //add BP
    private static Model addBP(Model modelSource, Model newModel, Resource resItem) {
        for (StmtIterator bp = modelSource.listStatements(new SimpleSelector(resItem, null,(RDFNode) null)); bp.hasNext();) {
            Statement stmt = bp.next();
            newModel.add(stmt);
            // Add ConnectivityNode
            if (stmt.getPredicate().asResource().getLocalName().equals("BoundaryPoint.ConnectivityNode")) {
                for (StmtIterator cn = modelSource.listStatements(new SimpleSelector(stmt.getObject().asResource(), null,(RDFNode) null)); cn.hasNext();) {
                    Statement stmtcn = cn.next();
                    newModel.add(stmtcn);
                    //Add Line container
                    if (stmtcn.getPredicate().asResource().getLocalName().equals("ConnectivityNode.ConnectivityNodeContainer")) {
                        for (StmtIterator con = modelSource.listStatements(new SimpleSelector(stmtcn.getObject().asResource(), null,(RDFNode) null)); con.hasNext();) {
                            Statement stmtcon = con.next();
                            newModel.add(stmtcon);
                        }
                    }
                }
            }
        }
        return newModel;
    }


    //Create model
    private static Model createModel()  {

        Model model = org.apache.jena.rdf.model.ModelFactory.createDefaultModel();
        model.setNsPrefix("cim","http://iec.ch/TC57/CIM100#");
        model.setNsPrefix("eu","http://iec.ch/TC57/CIM100-European#");
        model.setNsPrefix("md","http://iec.ch/TC57/61970-552/ModelDescription/1#");
        model.setNsPrefix("rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#");

        return model;
    }
    //Replace namespace
    private static Property rebaseProperty(Property prop, String newBase)  {

        return ResourceFactory.createProperty(newBase+prop.getLocalName());
    }

    //Replace namespace
    private static RDFNode rebaseRDFNode(RDFNode prop, String newBase)  {

        return ResourceFactory.createProperty(newBase+prop.asResource().getLocalName());
    }

    //Replace namespace
    private static Resource rebaseResource(Resource res, String newBase)  {

        return ResourceFactory.createResource(newBase+res.getLocalName());
    }

    //Replace namespace
    private static Statement rebaseStatement(Statement stmtA, String cim17NS, String euNS)  {

        Resource newSub;
        Property newPre;
        RDFNode newObj;

        if (stmtA.getSubject().getNameSpace().equals("http://iec.ch/TC57/2013/CIM-schema-cim16#")) {
            newSub = rebaseResource(stmtA.getSubject(), cim17NS);
        } else if (stmtA.getSubject().getNameSpace().equals("http://entsoe.eu/CIM/SchemaExtension/3/1#")) {
            newSub = rebaseResource(stmtA.getSubject(), euNS);
        } else {
            newSub = stmtA.getSubject();
        }
        if (stmtA.getPredicate().getNameSpace().equals("http://iec.ch/TC57/2013/CIM-schema-cim16#")) {
            newPre = rebaseProperty(stmtA.getPredicate(), cim17NS);
        } else if (stmtA.getPredicate().getNameSpace().equals("http://entsoe.eu/CIM/SchemaExtension/3/1#")) {
            newPre = rebaseProperty(stmtA.getPredicate(), euNS);
        } else {
            newPre = stmtA.getPredicate();
        }
        if (stmtA.getObject().isResource()) {
            if (stmtA.getObject().asResource().getNameSpace().equals("http://iec.ch/TC57/2013/CIM-schema-cim16#")) {
                newObj = rebaseRDFNode(stmtA.getObject(), cim17NS);
            } else if (stmtA.getObject().asResource().getNameSpace().equals("http://entsoe.eu/CIM/SchemaExtension/3/1#")) {
                newObj = rebaseRDFNode(stmtA.getObject(), euNS);
            } else {
                newObj = stmtA.getObject();
            }
        } else {
            newObj = stmtA.getObject();
        }

        return ResourceFactory.createStatement(newSub,newPre,newObj);
    }

    //Save data
    public static void saveInstanceModelData(Map<String, Model> instanceDataModelMap, Map<String,Object> saveProperties, Map<String,Model> profileModelMap) throws IOException {

        boolean useFileDialog=(boolean) saveProperties.get("useFileDialog");
        if (!useFileDialog){
            DirectoryChooser folderchooser = new DirectoryChooser();
            folderchooser.setInitialDirectory(new File(MainController.prefs.get("LastWorkingFolder","")));
            File file;

            try {
                file = folderchooser.showDialog(null);
            } catch (Exception k){
                folderchooser.setInitialDirectory(new File("C:\\\\"));
                file = folderchooser.showDialog(null);
            }
            saveProperties.replace("fileFolder", file);
        }

        for (Map.Entry<String, Model> entry : instanceDataModelMap.entrySet()) {

            //this is related to the save of the data
            Set<Resource> rdfAboutList = new HashSet<>();
            Set<Resource> rdfEnumList = new HashSet<>();
            if ((boolean) saveProperties.get("useAboutRules")) {
                if (profileModelMap!=null) {
                    if (profileModelMap.get(entry.getKey()).listSubjectsWithProperty(ResourceFactory.createProperty("http://iec.ch/TC57/1999/rdf-schema-extensions-19990926#stereotype"), "Description").hasNext()) {
                        rdfAboutList = profileModelMap.get(entry.getKey()).listSubjectsWithProperty(ResourceFactory.createProperty("http://iec.ch/TC57/1999/rdf-schema-extensions-19990926#stereotype"), "Description").toSet();
                    }
                }
                rdfAboutList.add(ResourceFactory.createResource(saveProperties.get("headerClassResource").toString()));
            }

            if ((boolean) saveProperties.get("useEnumRules")) {
                if (profileModelMap!=null) {
                    for (ResIterator i = profileModelMap.get(entry.getKey()).listSubjectsWithProperty(ResourceFactory.createProperty("http://iec.ch/TC57/1999/rdf-schema-extensions-19990926#stereotype"),
                            ResourceFactory.createProperty("http://iec.ch/TC57/NonStandard/UML#enumeration")); i.hasNext(); ) {
                        Resource resItem = i.next();
                        for (ResIterator j = profileModelMap.get(entry.getKey()).listSubjectsWithProperty(RDF.type, resItem); j.hasNext(); ) {
                            Resource resItemProp = j.next();
                            rdfEnumList.add(resItemProp);
                        }
                    }
                }
            }

            if (saveProperties.containsKey("rdfAboutList")) {
                saveProperties.replace("rdfAboutList", rdfAboutList);
            }else{
                saveProperties.put("rdfAboutList", rdfAboutList);
            }
            if (saveProperties.containsKey("rdfEnumList")) {
                saveProperties.replace("rdfEnumList", rdfEnumList);
            }else{
                saveProperties.put("rdfEnumList", rdfEnumList);
            }

            if (MainController.ibBDconversion) {
                saveProperties.replace("filename", entry.getKey());
            }else{
                if (nameMap.size() != 0) {
                    saveProperties.replace("filename", nameMap.get(entry.getKey()));
                } else {
                    saveProperties.replace("filename", entry.getKey());
                }
            }


            InstanceDataFactory.saveInstanceData(entry.getValue(), saveProperties);
        }
    }
}

