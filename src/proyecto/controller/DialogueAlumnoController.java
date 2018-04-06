/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyecto.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import modelo.Alumno;

/**
 * FXML Controller class
 *
 * @author Alejandro
 */
public class DialogueAlumnoController implements Initializable {

    @FXML
    private Button buttonCreate;
    @FXML
    private Button buttonCerrar;
    @FXML
    private TextField textNombre;
    @FXML
    private TextField textDNI;
    @FXML
    private Spinner<Integer> spinnerEdad;
    @FXML
    private TextField textDireccion;
    @FXML
    private TextField textImagePath;
    @FXML
    private Button buttonExaminar;
    
    public static final int MAX_EDAD = 200; //Valor en base a la esperanza de vida de una persona
    private Stage primaryStage;
    private ObservableList<Alumno> dataAlumnos;
    private final FileChooser imageChooser = new FileChooser();
    private ObjectProperty<File> imageFile = new SimpleObjectProperty<>();
    
    public void init(Stage stage, ObservableList<Alumno> dA) {
        primaryStage = stage;
        primaryStage.setTitle("Nuevo Alumno");
        dataAlumnos = dA;
    }
    
    @FXML
    private void createAlumno(ActionEvent event) {
        String nombre = textNombre.getText();
        String DNI = textDNI.getText();
        String direccion = textDireccion.getText();
        String sImagePath = textImagePath.getText();
        if (nombre.isEmpty()
            || DNI.isEmpty()
            || direccion.isEmpty())
        {
            Alert alert = new Alert(AlertType.ERROR, "Alguno de los campos está vacío. Revíselo e inténtelo de nuevo.");
            alert.setHeaderText("Campo vacío");
            alert.show();
        }
        else if (imageFile.getValue() == null) {
            Alert alert = new Alert(AlertType.ERROR, "La ruta del archivo de imagen es errónea o no está definida. Revíselo e inténtelo de nuevo.");
            alert.setHeaderText("Ruta vacía o errónea");
            alert.show();
            textImagePath.requestFocus();
        }
        else {
            int count = 0;
            while (count < dataAlumnos.size() && !dataAlumnos.get(count).getDni().equals(DNI)) count++;
            if (count < dataAlumnos.size()) {
                Alert alert = new Alert(AlertType.ERROR, "Hay un alumno dado de alta con el mismo DNI. Compruebe los datos y vuelva a intentarlo.");
                alert.setHeaderText("El alumno ya existe");
                alert.show();
            } else {
                Image foto = new Image(imageFile.getValue().toURI().toString());
                Alumno a = new Alumno(DNI, nombre, spinnerEdad.getValue(), direccion, LocalDate.now(), foto);
                dataAlumnos.add(a);
                Alert exito = new Alert(AlertType.INFORMATION, "El alumno ha sido dado de alta correctamente");
                exito.setHeaderText(null);
                exito.showAndWait();
                closeDialogue(event);
            }
        }       
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
        spinnerEdad.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, MAX_EDAD));
        FileChooser.ExtensionFilter fileExtensions = 
            new FileChooser.ExtensionFilter(
              "Archivos de imagen", "*.jpg", "*.jpeg", "*.png", "*.bmp");
        imageChooser.getExtensionFilters().add(fileExtensions);
        buttonExaminar.setOnAction((event) -> {
                imageFile.setValue(imageChooser.showOpenDialog(primaryStage));
        });
        Bindings.bindBidirectional(textImagePath.textProperty(), imageFile, new StringConverter<File>() {
            @Override
            public String toString(File object) {
                if (object != null) {
                    try { return object.getCanonicalPath(); } catch (IOException ex) {}
                }
                return "";
            }
            @Override
            public File fromString(String string) {
                File file = new File(string);
                return file.isFile() ? file : null;
            }
        });
    }        
}
