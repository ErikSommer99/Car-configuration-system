package org.semesteroppgave;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class UserSignInController {

    @FXML
    private TextField txtUsername, txtPassword;

    @FXML
    void btnRegister(ActionEvent event) throws IOException {
        Main.setRoot("userregister");
    }

    @FXML
    void btnSignin(ActionEvent event) throws IOException {
        Main.setRoot("userbuildcar");
    }

    @FXML
    void btnCancel(ActionEvent event) throws IOException {
        Main.setRoot("primary");
    }
}