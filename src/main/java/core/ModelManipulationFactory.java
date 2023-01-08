/*
 * Licensed under the EUPL-1.2-or-later.
 * Copyright (c) 2022, gridDigIt Kft. All rights reserved.
 * @author Chavdar Ivanov
 */

package core;

import application.MainController;
import customWriter.CustomRDFFormat;
import javafx.stage.DirectoryChooser;
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



        //TODO do something to be able to import zipped files
        //for (File item : MainController.IDModel) {
        //    if (item.getName().toLowerCase().endsWith(".zip")) {
        //    }

        //}
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
            } else {
                convEQModel.add(stmtH);
            }
        }

        //add header for SSH
        headerRes = modelSSH.listSubjectsWithProperty(RDF.type, (RDFNode) ResourceFactory.createProperty("http://iec.ch/TC57/61970-552/ModelDescription/1#","FullModel")).nextResource();
        for (StmtIterator n = modelSSH.listStatements(new SimpleSelector(headerRes, null, (RDFNode) null)); n.hasNext(); ) {
            Statement stmtH = n.next();
            if (stmtH.getPredicate().getLocalName().equals("Model.profile")) {
                convSSHModel.add(ResourceFactory.createStatement(stmtH.getSubject(), stmtH.getPredicate(), ResourceFactory.createPlainLiteral("http://iec.ch/TC57/ns/CIM/SteadyStateHypothesis-EU/3.0")));
            } else {
                convSSHModel.add(stmtH);
            }
        }

        //add header for SV
        headerRes = modelSV.listSubjectsWithProperty(RDF.type, (RDFNode) ResourceFactory.createProperty("http://iec.ch/TC57/61970-552/ModelDescription/1#","FullModel")).nextResource();
        for (StmtIterator n = modelSV.listStatements(new SimpleSelector(headerRes, null, (RDFNode) null)); n.hasNext(); ) {
            Statement stmtH = n.next();
            if (stmtH.getPredicate().getLocalName().equals("Model.profile")) {
                convSVModel.add(ResourceFactory.createStatement(stmtH.getSubject(), stmtH.getPredicate(), ResourceFactory.createPlainLiteral("http://iec.ch/TC57/ns/CIM/StateVariables-EU/3.0")));
            } else {
                convSVModel.add(stmtH);
            }
        }

        //add header for TP
        headerRes = modelTP.listSubjectsWithProperty(RDF.type, (RDFNode) ResourceFactory.createProperty("http://iec.ch/TC57/61970-552/ModelDescription/1#","FullModel")).nextResource();
        for (StmtIterator n = modelTP.listStatements(new SimpleSelector(headerRes, null, (RDFNode) null)); n.hasNext(); ) {
            Statement stmtH = n.next();
            if (stmtH.getPredicate().getLocalName().equals("Model.profile")) {
                convTPModel.add(ResourceFactory.createStatement(stmtH.getSubject(), stmtH.getPredicate(), ResourceFactory.createPlainLiteral("http://iec.ch/TC57/ns/CIM/Topology-EU/3.0")));
            } else {
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

        //check if EQ has ConnectivityNodes
        int hasCN=0;
        if (modelEQ.listStatements(null,RDF.type,ResourceFactory.createProperty(cim16NS,"ConnectivityNode")).hasNext()){
            hasCN=1;
        }
        for (StmtIterator c = modelTP.listStatements(new SimpleSelector(null, RDF.type, (RDFNode) null)); c.hasNext(); ) { // loop on all classes
            Statement stmtC = c.next();
            String className = stmtC.getObject().asResource().getLocalName();
            if (!className.equals("FullModel")) {

                int hasMRid = 0;
                Resource newCNres = null;

                if (hasCN == 0) { // need to create CN and add it to EQ and link here in TP
                    String uuidCN = String.valueOf(UUID.randomUUID());
                    newCNres = ResourceFactory.createResource(euNS + "_" + uuidCN);
                    convEQModel.add(ResourceFactory.createStatement(newCNres, RDF.type, ResourceFactory.createProperty(cim17NS, "ConnectivityNode")));
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
                    if (newPre.getLocalName().equals("IdentifiedObject.name") && hasCN == 0) {
                        convEQModel.add(ResourceFactory.createStatement(newCNres, newPre, ResourceFactory.createProperty(newSub.toString())));
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

                    convSSHModel.add(ResourceFactory.createStatement(newSub, newPre, newObj));

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

                    convEQModel.add(ResourceFactory.createStatement(newSub, newPre, newObj));

                    if (newPre.getLocalName().equals("IdentifiedObject.mRID")) {
                        hasMRid = 1;
                    }
                    if (newPre.getLocalName().equals("OperationalLimitType.direction")) {
                        hasDir = 1;
                    }
                    if (newPre.getLocalName().equals("ACDCTerminal.sequenceNumber")) {
                        hasTerSeqNum = 1;
                    }

                }
                //add Equipment.inservice to SSH
                if (getEqInService.contains(className)) {
                    convSSHModel.add(ResourceFactory.createStatement(newSub, RDF.type, ResourceFactory.createProperty(cim17NS, "Equipment")));
                    convSSHModel.add(ResourceFactory.createStatement(newSub, ResourceFactory.createProperty(cim17NS, "Equipment.inService"), ResourceFactory.createPlainLiteral("true")));
                }

                //add SvStatus.inservice to SV
                if (getSvStInService.contains(className)) {
                    //TODO check is SvStatus.inservice is existing and if yes then take the value
                    String uuidSvStatus = String.valueOf(UUID.randomUUID());
                    Resource newSvStatusres = ResourceFactory.createResource(cim17NS + "_" + uuidSvStatus);
                    convSVModel.add(ResourceFactory.createStatement(newSvStatusres, RDF.type, ResourceFactory.createProperty(cim17NS, "SvStatus")));
                    convSVModel.add(ResourceFactory.createStatement(newSvStatusres, ResourceFactory.createProperty(cim17NS, "SvStatus.inService"), ResourceFactory.createPlainLiteral("true")));
                    convSVModel.add(ResourceFactory.createStatement(newSvStatusres, ResourceFactory.createProperty(cim17NS, "SvStatus.ConductingEquipment"), ResourceFactory.createProperty(newSub.toString())));
                }
                //add mrid if not there
                if (hasMRid == 0 && getMRIDEQ.contains(stmtC.getObject().asResource().getLocalName())) {
                    convEQModel.add(ResourceFactory.createStatement(rebaseResource(stmtC.getSubject(), cim17NS), mrid, ResourceFactory.createPlainLiteral(stmtC.getSubject().getLocalName().substring(1))));
                }
                //add OperationalLimitType.direction if not there
                if (hasDir == 0 && className.equals("OperationalLimitType")) {
                    convEQModel.add(ResourceFactory.createStatement(rebaseResource(stmtC.getSubject(), cim17NS), ResourceFactory.createProperty(cim17NS, "OperationalLimitType.direction"), ResourceFactory.createProperty(cim17NS, "OperationalLimitDirectionKind.absoluteValue")));
                }

                //add OperationalLimitType.acceptableDuration if not there
                if (hasTerSeqNum == 0) {
                    //TODO if in the list of single terminal devices add 1
                    //if more add 1, 2, 3
                    //convEQModel.add(ResourceFactory.createStatement(rebaseResource(stmtC.getSubject(), cim17NS), ResourceFactory.createProperty(cim17NS,"ACDCTerminal.sequenceNumber"), ResourceFactory.createProperty(cim17NS,"OperationalLimitDirectionKind.absoluteValue")));
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

            if (nameMap.size()!=0){
                saveProperties.replace("filename", nameMap.get(entry.getKey()));
            }else{
                saveProperties.replace("filename", entry.getKey());
            }


            InstanceDataFactory.saveInstanceData(entry.getValue(), saveProperties);
        }
    }
}

