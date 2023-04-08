/*
 * Licensed under the EUPL-1.2-or-later.
 * Copyright (c) 2023, gridDigIt Kft. All rights reserved.
 * @author Chavdar Ivanov
 */
package application;

import core.InstanceDataFactory;
import core.ModelManipulationFactory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;

import javax.swing.*;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static core.ModelManipulationFactory.ConvertCGMESv2v3;



public class MainController implements Initializable {



    public Button btnResetIGM;
    public Tab tabOutputWindow;
    public Font x3;
    public TabPane tabPaneConstraintsDetails;
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
            fCBconvBD.setDisable(true);

        }else{
            fBrowse.setDisable(true);
            fcbKeepExt.setDisable(true);
            fCBconvBD.setDisable(false);
            fcbKeepExt.setSelected(false);
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

        if (loadDataMap.size()==0) {
            // load all profile models
            Lang rdfProfileFormat = null;
            Map<String, Model> profileModelMap = null;
            String xmlBase = "http://iec.ch/TC57/CIM100";
            List<File> modelFiles = new LinkedList<>();
            File EQ;
            File FH;
            File SC;
            File OP;
            File SSH;
            File TP;
            File SV;
            File EQBD;

            String pathclass = getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            File jarFile = new File(pathclass);
            String jarDir = jarFile.getParentFile().getAbsolutePath();

            EQ = new File(jarDir + "/RDFSCGMESv3/IEC61970-600-2_CGMES_3_0_0_RDFS2020_EQ.rdf");
            FH = new File(jarDir + "/RDFSCGMESv3/FileHeader_RDFS2019.rdf");
            EQBD = new File(jarDir + "/RDFSCGMESv3/IEC61970-600-2_CGMES_3_0_0_RDFS2020_EQBD.rdf");
            OP = new File(jarDir + "/RDFSCGMESv3/IEC61970-600-2_CGMES_3_0_0_RDFS2020_OP.rdf");
            SC = new File(jarDir + "/RDFSCGMESv3/IEC61970-600-2_CGMES_3_0_0_RDFS2020_SC.rdf");
            SSH = new File(jarDir + "/RDFSCGMESv3/IEC61970-600-2_CGMES_3_0_0_RDFS2020_SSH.rdf");
            SV = new File(jarDir + "/RDFSCGMESv3/IEC61970-600-2_CGMES_3_0_0_RDFS2020_SV.rdf");
            TP = new File(jarDir + "/RDFSCGMESv3/IEC61970-600-2_CGMES_3_0_0_RDFS2020_TP.rdf");

            if (!EQ.exists() && !FH.exists() && !EQBD.exists() && !OP.exists() && !SC.exists() && !SSH.exists() && !SV.exists() && !TP.exists()){
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("RFS folder does not exist. Please select the folder with Select or cancel conversion.");
                alert.setHeaderText(null);
                alert.setTitle("Warning - RDFS folder does not exist");
                ButtonType btnYes = new ButtonType("Select RDFS folder");
                ButtonType btnNo = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(btnYes, btnNo);
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == btnNo) {
                    return;
                }else if (result.get() == btnYes){
                    DirectoryChooser directoryChooser = new DirectoryChooser();
                    directoryChooser.setInitialDirectory(new File(MainController.prefs.get("LastWorkingFolder","")));
                    File folder = directoryChooser.showDialog(null);
                    String rdfsdir = folder.getAbsolutePath();
                    EQ = new File(rdfsdir + "/IEC61970-600-2_CGMES_3_0_0_RDFS2020_EQ.rdf");
                    FH = new File(rdfsdir + "/FileHeader_RDFS2019.rdf");
                    EQBD = new File(rdfsdir + "/IEC61970-600-2_CGMES_3_0_0_RDFS2020_EQBD.rdf");
                    OP = new File(rdfsdir + "/IEC61970-600-2_CGMES_3_0_0_RDFS2020_OP.rdf");
                    SC = new File(rdfsdir + "/IEC61970-600-2_CGMES_3_0_0_RDFS2020_SC.rdf");
                    SSH = new File(rdfsdir + "/IEC61970-600-2_CGMES_3_0_0_RDFS2020_SSH.rdf");
                    SV = new File(rdfsdir + "/IEC61970-600-2_CGMES_3_0_0_RDFS2020_SV.rdf");
                    TP = new File(rdfsdir + "/IEC61970-600-2_CGMES_3_0_0_RDFS2020_TP.rdf");
                }
            }

            modelFiles.add(EQ);
            modelFiles.add(FH);
            modelFiles.add(EQBD);
            modelFiles.add(OP);
            modelFiles.add(SC);
            modelFiles.add(SSH);
            modelFiles.add(SV);
            modelFiles.add(TP);

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
            if (fcbKeepExt.isSelected()){
                ConvertCGMESv2v3(loadDataMap,1);
            }else{
                ConvertCGMESv2v3(loadDataMap,0);
            }
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
}

