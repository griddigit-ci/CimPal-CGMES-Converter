/*
 * Licensed under the EUPL-1.2-or-later.
 * Copyright (c) 2023, gridDigIt Kft. All rights reserved.
 * @author Chavdar Ivanov
 */
package application;

import core.InstanceDataFactory;
import core.ModelManipulationFactory;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static core.ModelManipulationFactory.ConvertCGMESv2v3;
import static core.ModelManipulationFactory.LoadRDFS;


public class MainController implements Initializable {



    public Button btnResetIGM;
    public Tab tabOutputWindow;
    public Font x3;
    public TabPane tabPaneConstraintsDetails;
    public Button btnResetSwitchingdevices;
    public Button btnModify;
    public CheckBox fcbexportMapfile;
    public CheckBox fcbSM;
    public CheckBox fCBrafo;
    public CheckBox fCBlines;
    public CheckBox fcbAllCondEQ;
    public Button fBrowseMapFile;
    public TextField fPathMapFile;
    public CheckBox fCBmapFile;
    public Button fBrowseIGMCGM;
    public TextField fPathIGMCGM;
    public ChoiceBox fCBCGMESstd;
    public CheckBox fCBmodCGM;
    public CheckBox fCBmodIGM;
    public CheckBox fcbRegCont;
    @FXML
    private TextField fPathIGM;

    @FXML
    private Button btnConvert;
    @FXML
    private CheckBox fcbKeepExt;

    @FXML
    private TextArea foutputWindow;

    public static Preferences prefs;

    @FXML
    private ProgressBar progressBar;

    public static List<File> IDModel;

    public static List<File> MappingMapFile;

    @FXML
    private TabPane tabPaneDown;
    @FXML
    private CheckBox fCBconvIGM;

    @FXML
    private CheckBox fCBconvBD;

    @FXML
    private  CheckBox fCBconv24To3;

    @FXML
    private  CheckBox fCBconvSplitBDREF;

    @FXML
    private CheckBox fCBconvBDSplitPerBorder;

    @FXML
    private Button fBrowse;
    @FXML
    private  CheckBox fcbEQonly;
    @FXML
    private CheckBox fcbApplyEQmap;



    private static Map<String, Map> loadDataMap;


    public static TextArea foutputWindowVar;
    public static boolean ibBDconversion;

    public MainController() {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) {
                appendText(String.valueOf((char) b));
            }
        };
        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
        foutputWindowVar = foutputWindow;

        try {
            if (!Preferences.userRoot().nodeExists("CimPalCGMESConverter")){
                prefs = Preferences.userRoot().node("CimPalCGMESConverter");
                //set the default preferences
                PreferencesController.prefDefault();
            }else{
                prefs = Preferences.userRoot().node("CimPalCGMESConverter");
            }
        } catch (BackingStoreException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        loadDataMap= new HashMap<>();

        fCBCGMESstd.getItems().addAll(
                "CGMES v2.4 (IEC TS 61970-600-1,-2:2017)",
                "CGMES v3.0 (IEC 61970-600-1,-2:2021)"

        );
        fCBCGMESstd.getSelectionModel().selectFirst();

    }

    @FXML
    // action on menu Preferences
    private void actionMenuPreferences() {
        try {
            Stage guiPrefStage = new Stage();
            //Scene for the menu Preferences
            Parent rootPreferences = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/preferencesGui.fxml")));
            Scene preferences = new Scene(rootPreferences);
            guiPrefStage.setScene(preferences);
            guiPrefStage.setTitle("Preferences");
            guiPrefStage.initModality(Modality.APPLICATION_MODAL);
            //PreferencesController PreferencesController=fxmlLoader.getController();
            PreferencesController.initData(guiPrefStage);
            guiPrefStage.showAndWait();

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    //Action for menu item "Quit"
    private void menuQuit() {
        Platform.exit(); // Exit the application
    }

    @FXML
    //action button Browse IGM
    private void actionBrowseIGM() throws URISyntaxException {
        progressBar.setProgress(0);

        //select file 1
        FileChooser filechooser = new FileChooser();
        filechooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Instance files", "*.xml","*.zip"));
        List<File> fileL;
        filechooser.setInitialDirectory(new File(MainController.prefs.get("LastWorkingFolder", "")));
        try {
            fileL = filechooser.showOpenMultipleDialog(null);
        }catch (Exception e){
            filechooser.setInitialDirectory(new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath()));
            fileL = filechooser.showOpenMultipleDialog(null);
        }


        if (fileL != null) {// the file is selected

            MainController.prefs.put("LastWorkingFolder", fileL.get(0).getParent());
            fPathIGM.setText(fileL.toString());
            btnConvert.setDisable(false);
            MainController.IDModel=fileL;
        } else{
            fPathIGM.clear();
        }
    }

    @FXML
    //action check box Conversion of Individual Grid Model (IGM)
    private void actionCBconvIGM() {

        if (fCBconvIGM.isSelected()){
            fBrowse.setDisable(false);
            fcbKeepExt.setDisable(false);
            fcbKeepExt.setSelected(false);
            fcbEQonly.setDisable(false);
            fcbEQonly.setSelected(false);
            fCBconvBD.setDisable(true);
            fcbRegCont.setDisable(false);
            fcbRegCont.setSelected(false);
        }else{
            fBrowse.setDisable(true);
            fcbKeepExt.setDisable(true);
            fCBconvBD.setDisable(false);
            fcbKeepExt.setSelected(false);
            fcbEQonly.setDisable(true);
            fcbEQonly.setSelected(false);
            fcbRegCont.setDisable(true);
            fcbRegCont.setSelected(false);
        }

    }

    @FXML
    //action check box Conversion of Boundary dataset
    private void actionCBconvBD() {

        if (fCBconvBD.isSelected()){
            fCBconvIGM.setDisable(true);
            fCBconv24To3.setDisable(false);
            fCBconvSplitBDREF.setDisable(false);
            fCBconvBDSplitPerBorder.setDisable(false);
        }else{
            fCBconvIGM.setDisable(false);
            fCBconv24To3.setDisable(true);
            fCBconvSplitBDREF.setDisable(true);
            fCBconvBDSplitPerBorder.setDisable(true);
            fCBconv24To3.setSelected(false);
            fCBconvSplitBDREF.setSelected(false);
            fCBconvBDSplitPerBorder.setSelected(false);
        }

    }

    @FXML
    //action check box Conversion of Boundary dataset check box 1
    private void actionCBconvBD1() {

        if (fCBconv24To3.isSelected()){
            fCBconvSplitBDREF.setDisable(true);
            fCBconvBDSplitPerBorder.setDisable(true);
        }else{
            fCBconvSplitBDREF.setDisable(false);
            fCBconvBDSplitPerBorder.setDisable(false);
        }

    }

    @FXML
    //action check box Conversion of Boundary dataset check box 2
    private void actionCBconvBD2() {

        if (fCBconvSplitBDREF.isSelected()){
            fCBconv24To3.setDisable(true);
            fCBconvBDSplitPerBorder.setDisable(true);
        }else{
            fCBconv24To3.setDisable(false);
            fCBconvBDSplitPerBorder.setDisable(false);
        }

    }

    @FXML
    //action check box Conversion of Boundary dataset check box 3
    private void actionCBconvBD3() {

        if (fCBconvBDSplitPerBorder.isSelected()){
            fCBconv24To3.setDisable(true);
            fCBconvSplitBDREF.setDisable(true);
        }else{
            fCBconv24To3.setDisable(false);
            fCBconvSplitBDREF.setDisable(false);
        }

    }


    @FXML
    // action on menu About
    private void actionMenuAbout() {
        try {
            Stage guiAboutStage = new Stage();
            //Scene for the menu Preferences
            Parent rootAbout = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/aboutGui.fxml")));
            Scene about = new Scene(rootAbout);
            guiAboutStage.setScene(about);
            guiAboutStage.setTitle("About");
            guiAboutStage.initModality(Modality.APPLICATION_MODAL);
            AboutController.initData(guiAboutStage);
            guiAboutStage.showAndWait();

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    //Action for button "Reset"
    private void actionBtnReset() {
        fPathIGM.clear();
        fCBconvIGM.setSelected(false);
        fCBconvBD.setSelected(false);
        fcbKeepExt.setSelected(false);
        fCBconv24To3.setSelected(false);
        fCBconvSplitBDREF.setSelected(false);
        fCBconvBDSplitPerBorder.setSelected(false);
        //btnConvert.setDisable(true);
        progressBar.setProgress(0);

    }

    @FXML
    //Action for button "Clear" related to the output window
    private void actionBtnClear() {
        if (tabPaneDown.getSelectionModel().getSelectedItem().getText().equals("Output window")) { //clears Output window
            foutputWindow.clear();
        }
    }

    @FXML
    //action button Convert
    private void actionBtnConvert() throws IOException, URISyntaxException {


        //datatype test

//        Model model = org.apache.jena.rdf.model.ModelFactory.createDefaultModel();
//        model.setNsPrefix("cim","http://iec.ch/TC57/CIM100#");
//        Statement testStmt = ResourceFactory.createStatement(ResourceFactory.createResource("http://iec.ch/TC57/CIM100#123"),ResourceFactory.createProperty("http://iec.ch/TC57/CIM100#","testDatatype"),ResourceFactory.createLangLiteral("test datatype","en"));
//        model.add(testStmt);
//        int endTest=1;
//        RDFLangString.rdfLangString
//        http://www.w3.org/1999/02/22-rdf-syntax-ns#langString




        if (loadDataMap.isEmpty()) {
            // load all profile models

            List<File> modelFiles = LoadRDFS("CGMESv3.0");
            Map<String, Model> profileModelMap;
            String xmlBase = "http://iec.ch/TC57/CIM100";
            Lang rdfProfileFormat = null;

            try {
                profileModelMap = InstanceDataFactory.modelLoad(modelFiles, xmlBase, rdfProfileFormat);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            loadDataMap.put("profileModelMap", profileModelMap);
        }




        progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);

        if (fCBconvIGM.isSelected()){
            ibBDconversion=false;
            int keepE;
            int eqo;
            int fixRegCont;
            if (fcbKeepExt.isSelected()){
                keepE = 1;
            }else{
                keepE = 0;
            }
            if (fcbEQonly.isSelected()){
                eqo = 1;
            }else{
                eqo = 0;
            }
            if (fcbRegCont.isSelected()){
                fixRegCont = 1;
            }else {
                fixRegCont = 0;
            }
            ConvertCGMESv2v3(loadDataMap,keepE,eqo,fixRegCont);
        }

        if (fCBconvBD.isSelected()){
            ibBDconversion=true;
            if (fCBconv24To3.isSelected()){
                ModelManipulationFactory.ConvertBoundarySetCGMESv2v3();
            }else if (fCBconvSplitBDREF.isSelected()){
                ModelManipulationFactory.SplitBoundaryAndRefData();
            }else if (fCBconvBDSplitPerBorder.isSelected()){
                ModelManipulationFactory.SplitBoundaryPerBorder();

            }
        }

        System.out.print("Conversion finished.\n");
        progressBar.setProgress(1);
    }

    public void appendText(String valueOf) {
        Platform.runLater(() -> foutputWindow.appendText(valueOf));
    }

    public void actionBtnModify() throws URISyntaxException, IOException {

        String cgmesVersion="";
        if(fCBCGMESstd.getSelectionModel().getSelectedItem().toString().equals("CGMES v2.4 (IEC TS 61970-600-1,-2:2017)")){
            cgmesVersion = "CGMESv2.4";
        }else if (fCBCGMESstd.getSelectionModel().getSelectedItem().toString().equals("CGMES v3.0 (IEC 61970-600-1,-2:2021)")){
            cgmesVersion = "CGMESv3.0";
        }

        /*if (loadDataMap.isEmpty()) {
            // load all profile models
            List<File> modelFiles = LoadRDFS(cgmesVersion);
            Map<String, Model> profileModelMap;
            String xmlBase = "http://iec.ch/TC57/CIM100";
            Lang rdfProfileFormat = null;

            try {
                profileModelMap = InstanceDataFactory.modelLoad(modelFiles, xmlBase, rdfProfileFormat);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            loadDataMap.put("profileModelMap", profileModelMap);
        }*/

        progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);

        boolean impMap = fCBmapFile.isSelected();
        boolean applyAllCondEq = fcbAllCondEQ.isSelected();
        boolean applyLine = fCBlines.isSelected();
        boolean applyTrafo = fCBrafo.isSelected();
        boolean applySynMach = fcbSM.isSelected();
        boolean expMap = fcbexportMapfile.isSelected();
        boolean applyEQmap = fcbApplyEQmap.isSelected();

        if (fCBmodIGM.isSelected()){
            ModelManipulationFactory.ModifyIGM(loadDataMap,cgmesVersion,impMap,applyAllCondEq,applyLine,applyTrafo,applySynMach,expMap,applyEQmap);
        }else if (fCBmodCGM.isSelected()){
            ModelManipulationFactory.ModifyCGM(loadDataMap,cgmesVersion,impMap,applyAllCondEq,applyLine,applyTrafo,applySynMach,expMap);
        }

        System.out.print("Modification finished.\n");
        progressBar.setProgress(1);
    }

    public void actionBtnResetModify() {
        fPathIGMCGM.clear();
        fPathMapFile.clear();
        fCBmodIGM.setSelected(false);
        fCBmodCGM.setSelected(false);
        fCBmapFile.setSelected(false);
        fcbAllCondEQ.setSelected(false);
        fCBlines.setSelected(false);
        fCBrafo.setSelected(false);
        fcbSM.setSelected(false);
        fcbexportMapfile.setSelected(false);
        progressBar.setProgress(0);
        fBrowseIGMCGM.setDisable(true);
        fBrowseMapFile.setDisable(true);
    }

    public void actionBrowseMapFile() throws URISyntaxException {
        progressBar.setProgress(0);

        FileChooser filechooser = new FileChooser();
        filechooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Excel file", "*.xlsx"));
        List<File> fileL;
        filechooser.setInitialDirectory(new File(MainController.prefs.get("LastWorkingFolder", "")));
        filechooser.setTitle("Select mapping file");
        try {
            fileL = filechooser.showOpenMultipleDialog(null);
        }catch (Exception e){
            filechooser.setInitialDirectory(new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath()));
            fileL = filechooser.showOpenMultipleDialog(null);
        }

        if (fileL != null) {// the file is selected

            MainController.prefs.put("LastWorkingFolder", fileL.getFirst().getParent());
            fPathMapFile.setText(fileL.toString());
            MainController.MappingMapFile=fileL;
        } else{
            fPathMapFile.clear();
        }
    }

    public void actionBrowseIGMCGM() throws URISyntaxException {
        progressBar.setProgress(0);

        String title = "";
        if (fCBmodIGM.isSelected()){
            title = "Select an IGM (either EQ, SSH or EQ, SSH, TP, SV and boundary set)";
        }else if (fCBmodCGM.isSelected()){
            title = "Select an CGM including boundary set";;
        }

        FileChooser filechooser = new FileChooser();
        filechooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Instance files", "*.xml","*.zip"));
        List<File> fileL;
        filechooser.setInitialDirectory(new File(MainController.prefs.get("LastWorkingFolder", "")));
        filechooser.setTitle(title);
        try {
            fileL = filechooser.showOpenMultipleDialog(null);
        }catch (Exception e){
            filechooser.setInitialDirectory(new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath()));
            fileL = filechooser.showOpenMultipleDialog(null);
        }

        if (fileL != null) {// the file is selected

            MainController.prefs.put("LastWorkingFolder", fileL.getFirst().getParent());
            fPathIGMCGM.setText(fileL.toString());
            MainController.IDModel=fileL;
        } else{
            fPathIGMCGM.clear();
        }
    }

    public void actionCBmodIGM() {
        if (fCBmodIGM.isSelected()){
            fCBmodCGM.setSelected(false);
            fBrowseIGMCGM.setDisable(false);
        }else{
            fCBmodCGM.setSelected(false);
            fBrowseIGMCGM.setDisable(true);
        }
    }

    public void actionCBmodCGM() {
        if (fCBmodCGM.isSelected()){
            fCBmodIGM.setSelected(false);
            fBrowseIGMCGM.setDisable(false);
        }else{
            fCBmodIGM.setSelected(false);
            fBrowseIGMCGM.setDisable(true);
        }
    }

    public void actionImpMapping() {
        if (fCBmapFile.isSelected()){
            fBrowseMapFile.setDisable(false);
        }else{
            fBrowseMapFile.setDisable(true);
            fPathMapFile.clear();
        }
    }

    public void actionCBallCondEq() {
        if (fcbAllCondEQ.isSelected()){
            fCBlines.setDisable(true);
            //fCBrafo.setDisable(true);
            fcbSM.setDisable(true);
        }else{
            fCBlines.setDisable(false);
            //fCBrafo.setDisable(false);
            fcbSM.setDisable(false);
        }
        fCBlines.setSelected(false);
        //fCBrafo.setSelected(false);
        fcbSM.setSelected(false);
    }

    public void actionCBapplyLines() {
        if (fCBlines.isSelected()) {
            fcbAllCondEQ.setDisable(true);
            fcbAllCondEQ.setSelected(false);
        }else{
            if (!fCBrafo.isSelected() && !fcbSM.isSelected()){
                //fcbAllCondEQ.setDisable(false);
            }
        }
    }

    public void actionCBapplyPT() {
        if (fCBrafo.isSelected()) {
            fcbAllCondEQ.setDisable(true);
            fcbAllCondEQ.setSelected(false);
        }else{
            if (!fCBlines.isSelected() && !fcbSM.isSelected()){
                //fcbAllCondEQ.setDisable(false);
            }
        }
    }

    public void actionCBApplySM() {
        if (fcbSM.isSelected()) {
            fcbAllCondEQ.setDisable(true);
            fcbAllCondEQ.setSelected(false);
        }else{
            if (!fCBlines.isSelected() && !fCBrafo.isSelected()){
                //fcbAllCondEQ.setDisable(false);
            }
        }
    }
}

