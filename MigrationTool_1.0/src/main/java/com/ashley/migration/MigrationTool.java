package com.ashley.migration;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


// GUI created using JavaFX
public class MigrationTool extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Scene.fxml"));
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");
        
		stage.setResizable(false);
		stage.getIcons().add(new Image("/fxml/e.png"));
		stage.setTitle("MigrationTool");
		stage.setScene(scene);
		stage.show();
        
    }

    public static void main(String[] args) {
        launch(args);
    }

}
