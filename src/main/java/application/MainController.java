/*
 * Licensed under the EUPL-1.2-or-later.
 * Copyright (c) 2023, gridDigIt Kft. All rights reserved.
 * @author Chavdar Ivanov
 */
package application;

import core.InstanceDataFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
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



public class MainController implements Initializable {


    @FXML
    private TextField fPathIGM;
    @FXML
    private TextField fPathOutput;
    @FXML
    private Button btnResetIGM;
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

    private static Map<String, Map> loadDataMap;


    public static TextArea foutputWindowVar;

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
            if (!Preferences.userRoot().nodeExists("CimPal")){
                prefs = Preferences.userRoot().node("CimPal");
                //set the default preferences
                PreferencesController.prefDefault();
            }else{
                prefs = Preferences.userRoot().node("CimPal");
            }
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }

        // load all profile models
        loadDataMap= new HashMap<>();
        Lang rdfProfileFormat=null;
        Map<String,Model> profileModelMap = null;
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
        try {
            EQ = new File(getClass().getResource("/RDFSCGMESv3/IEC61970-600-2_CGMES_3_0_0_RDFS2020_EQ.rdf").toURI())  ;
            FH = new File(getClass().getResource("/RDFSCGMESv3/FileHeader_RDFS2019.rdf").toURI())  ;
            EQBD = new File(getClass().getResource("/RDFSCGMESv3/IEC61970-600-2_CGMES_3_0_0_RDFS2020_EQBD.rdf").toURI())  ;
            OP = new File(getClass().getResource("/RDFSCGMESv3/IEC61970-600-2_CGMES_3_0_0_RDFS2020_OP.rdf").toURI())  ;
            SC = new File(getClass().getResource("/RDFSCGMESv3/IEC61970-600-2_CGMES_3_0_0_RDFS2020_SC.rdf").toURI())  ;
            SSH = new File(getClass().getResource("/RDFSCGMESv3/IEC61970-600-2_CGMES_3_0_0_RDFS2020_SSH.rdf").toURI())  ;
            SV = new File(getClass().getResource("/RDFSCGMESv3/IEC61970-600-2_CGMES_3_0_0_RDFS2020_SV.rdf").toURI())  ;
            TP = new File(getClass().getResource("/RDFSCGMESv3/IEC61970-600-2_CGMES_3_0_0_RDFS2020_TP.rdf").toURI())  ;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
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

        loadDataMap.put("profileModelMap",profileModelMap);
    }

    @FXML
    // action on menu Preferences
    private void actionMenuPreferences() {
        try {
            Stage guiPrefStage = new Stage();
            //Scene for the menu Preferences
            FXMLLoader fxmlLoader=new FXMLLoader();
            Parent rootPreferences = fxmlLoader.load(getClass().getResource("/fxml/preferencesGui.fxml"));
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
    private void actionBrowseIGM() {
        progressBar.setProgress(0);

        //select file 1
        FileChooser filechooser = new FileChooser();
        filechooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Instance files", "*.xml","*.zip"));
        filechooser.setInitialDirectory(new File(MainController.prefs.get("LastWorkingFolder","")));
        List<File> fileL;
        fileL = filechooser.showOpenMultipleDialog(null);

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
    //action button Browse Output
    private void actionBrowseOutput() {
        progressBar.setProgress(0);

        //select file 1
        FileChooser filechooser = new FileChooser();
        filechooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Instance files", "*.xml","*.zip"));
        filechooser.setInitialDirectory(new File(MainController.prefs.get("LastWorkingFolder","")));
        List<File> fileL;
        fileL = filechooser.showOpenMultipleDialog(null);

        if (fileL != null) {// the file is selected

            MainController.prefs.put("LastWorkingFolder", fileL.get(0).getParent());
            fPathOutput.setText(fileL.toString());
            MainController.IDModel=fileL;
        } else{
            fPathOutput.clear();
        }
    }

    @FXML
    // action on menu About
    private void actionMenuAbout() {
        try {
            Stage guiAboutStage = new Stage();
            //Scene for the menu Preferences
            FXMLLoader fxmlLoader=new FXMLLoader();
            Parent rootAbout = fxmlLoader.load(getClass().getResource("/fxml/aboutGui.fxml"));
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
        btnConvert.setDisable(true);
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
    private void actionBtnConvert() throws IOException {
        progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        if (fcbKeepExt.isSelected()){
            ConvertCGMESv2v3(loadDataMap,1);
        }else{
            ConvertCGMESv2v3(loadDataMap,0);
        }
        progressBar.setProgress(1);
    }

    public void appendText(String valueOf) {
        Platform.runLater(() -> foutputWindow.appendText(valueOf));
    }

}

