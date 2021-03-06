package org.semesteroppgave;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.semesteroppgave.models.filehandlers.FileHandler;
import org.semesteroppgave.models.utilities.alerts.Dialogs;

import java.io.File;
import java.io.IOException;

/**
 * JavaFX Main
 */
public class Main extends Application {

    private static Scene scene;
    public static boolean folderCreated;
    public static final String FOLDERNAME = "DataMet_Configurations";

    public static Scene getScene() {
        return scene;
    }

    @Override
    public void start(Stage stage) throws IOException {
        //Creates a folder locally on your computer for file storage
        createFolderOnComputer();
        //Storing data at the end of the program
        onProgramExit(stage);
        //Loading finished products
        loadConfiguredProducts();
        scene = new Scene(loadFXML("usersignin"));
        scene.getStylesheets().add(getClass().getResource("views/stylesheet.css").toExternalForm());
        stage.setTitle("DataMet Configurations");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setWidth(750);
        stage.setHeight(575);
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("views/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

    private void loadConfiguredProducts() {
        FileHandler.openFileCvsLaunch(getClass().getResource("/org/semesteroppgave/files/onApplicationLaunch/products.csv").getFile());
    }

    private void createFolderOnComputer() {
        File filePath = new File(System.getProperty("user.home") + File.separator + FOLDERNAME);
        if (!filePath.exists()) {
            if (filePath.mkdirs()) {
                System.out.println("Creates folder locally on computer");
                folderCreated = true;
            } else {
                System.out.println("Failed to create folder");
                folderCreated = false;
            }
        } else {
            System.out.println("The folder already exists");
            folderCreated = true;
        }
    }

    //Saves to file when the program exits if the lists are not empty and the folder is created
    private void onProgramExit(Stage stage) {
        stage.setOnCloseRequest(windowEvent -> {
            if (!ApplicationData.getInstance().getRegisterProduct().getUserProductList().isEmpty() ||
                    !ApplicationData.getInstance().getRegisterComponent().getComponentList().isEmpty()) {
                Dialogs.showConfirmationDialog("Do you want to save changes before quitting the program?",
                        "The files are stored in the folder: " + FOLDERNAME,
                        response -> {
                            if (response == ButtonType.OK) {
                                if (folderCreated) {
                                    if (!ApplicationData.getInstance().getRegisterProduct().getUserProductList().isEmpty()) {
                                        FileHandler.saveFileCsvOnProgramExit(System.getProperty("user.home") + "/"
                                                + FOLDERNAME + "/savedProducts.csv");
                                    }
                                    if (!ApplicationData.getInstance().getRegisterComponent().getComponentList().isEmpty()) {
                                        FileHandler.saveFileJobjOnProgramExit(System.getProperty("user.home") + "/"
                                                + FOLDERNAME + "/savedComponents.jobj");
                                    }
                                } else {
                                    System.out.println("Failed to save because folder was not created");
                                }
                            }
                        });
            }

        });
    }
}