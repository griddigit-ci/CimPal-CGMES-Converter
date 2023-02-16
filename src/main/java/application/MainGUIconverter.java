/*
 * Licensed under the EUPL-1.2-or-later.
 * Copyright (c) 2020, gridDigIt Kft. All rights reserved.
 * @author Chavdar Ivanov
 */
package application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Objects;

public class MainGUIconverter extends Application {

    private Stage MainAppStage;
    private static Scene mainApp;

    public MainGUIconverter() {

    }

    @Override
    public void init() throws Exception {
        super.init();
        // Scene for the Main App
        // Load root layout from fxml file.
        Parent rootMainApp = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/CimPalCGMESconverterGui - Copy.fxml")));
        //Parent rootMainApp = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/aboutGui.fxml")));
        mainApp = new Scene(rootMainApp);

    }


 /*   @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Hello World!");
        Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                System.out.println("Hello World!");
            }
        });

        StackPane root = new StackPane();
        root.getChildren().add(btn);
        primaryStage.setScene(new Scene(root, 300, 250));
        primaryStage.show();
    }*/
    @Override
    public void start(Stage primaryStage) {
        try {

            MainAppStage=primaryStage;

           //Parent root = loader.load();

            primaryStage.setTitle("gridDigIt: CimPal CGMES Converter");

            // Show the scene containing the root layout.
            primaryStage.setScene(mainApp);
            primaryStage.setMaximized(false);

            /*// Get current screen of the stage
            ObservableList<Screen> screens = Screen.getScreensForRectangle(new Rectangle2D(primaryStage.getX(), primaryStage.getY(), primaryStage.getWidth(), primaryStage.getHeight()));

            // Change stage properties
            Rectangle2D bounds = screens.get(0).getVisualBounds();
            primaryStage.setX(bounds.getMinX());
            primaryStage.setY(bounds.getMinY());
            primaryStage.setWidth(bounds.getWidth());
            primaryStage.setHeight(bounds.getHeight());
            
*/

            //Scene for the menu Preferences
            //Parent rootPreferences = FXMLLoader.load(getClass().getResource("/fxml/preferencesGui.fxml"));
            //Scene preferences = new Scene(rootPreferences);

            //primaryStage.setScene(mainApp);
            primaryStage.initStyle(StageStyle.DECORATED);

            primaryStage.show();
            notifyPreloader(new Preloader.ProgressNotification(0.99));



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        Platform.exit(); // Exit the application
    }

    public static void main(String[] args) {
        //Application.launch(args);
        System.setProperty("javafx.preloader", "preload.PreloadApp");
        Application.launch(MainGUIconverter.class, args);
    }
}
