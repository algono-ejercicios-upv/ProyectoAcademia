/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyecto.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Alejandro
 */
public class DialogueController implements Initializable {
    
    @FXML
    private Button buttonCreate;
    
    private Stage primaryStage;
    
    public void init(Stage stage) {
        primaryStage = stage;
        //primaryStage.setTitle("Insert name here");
    }
    
    @FXML
    private void closeDialogue(ActionEvent event) {
        Node mynode = (Node) event.getSource();
        mynode.getScene().getWindow().hide();
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //TODO
    }    
    
}
