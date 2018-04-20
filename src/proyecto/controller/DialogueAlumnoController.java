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
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import modelo.Alumno;

/**
 * FXML Controller class
 *
 * @author Alejandro
 */
public class DialogueAlumnoController implements Initializable {

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
    
    public static final int MAX_EDAD = 200; //Valor en base a la esperanza de vida de una persona
    private Stage primaryStage;
    private ObservableList<Alumno> dataAlumnos;
    //El tamaño máximo al que se representará la imagen en pantalla (pensado para la pantalla de previsualización)
    private double imageHeight; private double imageWidth;
    private final FileChooser imageChooser = new FileChooser();
    private Image foto;
    
    public void init(Stage stage, ObservableList<Alumno> dA, double iHeight, double iWidth) {
        primaryStage = stage;
        primaryStage.setTitle("Nuevo Alumno");
        dataAlumnos = dA;
        imageHeight = iHeight; imageWidth = iWidth;
    }
    
    @FXML
    private void browseImage(ActionEvent event) {
        File initialDir = new File(textImagePath.getText()).getParentFile();
        if (initialDir != null) imageChooser.setInitialDirectory(initialDir);
        File imgFile = imageChooser.showOpenDialog(primaryStage);
        if (imgFile != null) {
            Alert preview = new Alert(AlertType.CONFIRMATION);
            Image tmpFoto = new Image(imgFile.toURI().toString());
            ImageView view = new ImageView(tmpFoto);
            view.setFitHeight(imageHeight); view.setFitWidth(imageWidth);
            preview.setResizable(false); view.setPreserveRatio(true);
            preview.setHeaderText("Previsualización");
            preview.getDialogPane().setContent(view);
            Optional<ButtonType> result = preview.showAndWait();
            if (result.get() == ButtonType.OK) {
                try { 
                    String imgPath = imgFile.getCanonicalPath();
                    textImagePath.setText(imgPath);
                    foto = tmpFoto;
                } catch (IOException ex) {
                    new Alert(AlertType.ERROR, "Ha habido un error al cargar la imagen.").show();
                }
            }
        }
    }
    
    @FXML
    private void createAlumno(ActionEvent event) {
        String nombre = textNombre.getText();
        String DNI = textDNI.getText();
        String direccion = textDireccion.getText();
        if (nombre.isEmpty() || DNI.isEmpty() || direccion.isEmpty() || foto == null) {
            Alert alert = new Alert(AlertType.ERROR, "Alguno de los campos está vacío. Revíselo e inténtelo de nuevo.");
            alert.setHeaderText("Campo vacío");
            alert.show();
        } else {
            int count = 0;
            while (count < dataAlumnos.size() && !dataAlumnos.get(count).getDni().equals(DNI)) count++;
            if (count < dataAlumnos.size()) {
                Alert alert = new Alert(AlertType.ERROR, "Hay un alumno dado de alta con el mismo DNI. Compruebe los datos y vuelva a intentarlo.");
                alert.setHeaderText("El alumno ya existe");
                alert.show();
            } else {
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
        spinnerEdad.setValueFactory(new IntegerSpinnerValueFactory(0, MAX_EDAD));
        imageChooser.setTitle("Elegir imagen");
        FileChooser.ExtensionFilter fileExtensions = 
            new FileChooser.ExtensionFilter(
              "Archivos de imagen", "*.jpg", "*.jpeg", "*.png", "*.bmp");
        imageChooser.getExtensionFilters().add(fileExtensions);
    }        
}
