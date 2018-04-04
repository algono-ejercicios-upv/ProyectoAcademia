/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyecto.controller;

import accesoaBD.AccesoaBD;
import modelo.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

/**
 * FXML Controller class
 *
 * @author Alejandro
 */
public class DialogueCursoController implements Initializable {
    
    @FXML
    private Button buttonCreate;
    @FXML
    private Button buttonCerrar;
    @FXML
    private GridPane form;
    @FXML
    private DatePicker dateInicio;
    @FXML
    private DatePicker dateFin;
    @FXML
    private Spinner<Integer> spinnerMaxAlumnos;
    @FXML
    private Spinner<Integer> spinnerHours;
    @FXML
    private Spinner<Integer> spinnerMinutes;
    @FXML
    private TextField textTitulo;
    @FXML
    private TextField textProfesor;
    @FXML
    private TextField textAula;
    @FXML
    private ToolBar toolBarDias;
    
    private Stage primaryStage;
    private boolean errorSpinner = false;
    
    class CyclicIntegerSpinnerValueFactory extends IntegerSpinnerValueFactory {
        public CyclicIntegerSpinnerValueFactory(int min, int max) {
            super(min, max);
            setConverter(new IntegerStringConverter() {
                @Override
                public Integer fromString(String value) {
                    if (value.matches("\\d*")) return super.fromString(value);
                    else { errorSpinner = true; return 0; }
                }
                @Override
                public String toString(Integer value) {
                    return String.format("%02d", value);
                }
            });
        }
        @Override
        public void increment(int steps) {
            if (getValue() == null) { errorSpinner = true; setValue(0); }
            else if (getValue() == getMax()) { setValue(getMin()); super.increment(steps-1); }
            else { super.increment(steps); }
        }
        @Override
        public void decrement(int steps) {
            if (getValue() == null) { errorSpinner = true; setValue(0); }
            else if (getValue() == getMin()) { setValue(getMax()); super.decrement(steps-1); } 
            else { super.decrement(steps); }
        }
    }
    
    public void init(Stage stage, Curso curso) {
        primaryStage = stage;
        stage.setTitle("Nuevo Curso");
        if (curso != null) {
            textTitulo.setText(curso.getTitulodelcurso()); 
            textProfesor.setText(curso.getProfesorAsignado());
            textAula.setText(curso.getAula());
            spinnerMaxAlumnos.getValueFactory().setValue(curso.getNumeroMaximodeAlumnos());
            setDias(curso.getDiasimparte());
            buttonCreate.setDisable(true);
            form.setMouseTransparent(true);
            primaryStage.addEventFilter(KeyEvent.ANY, e -> e.consume());
            buttonCerrar.requestFocus();
        }
    }
    
    private void setDias(ArrayList<Dias> dias) {
        for (Node n : toolBarDias.getItems()) {
            ToggleButton b = ((ToggleButton) n);
            Dias dia = null;
            switch(b.getText().charAt(0)) { //Sabemos que el texto de los botones siempre sera el mismo
                case 'L': dia = Dias.Lunes; break;
                case 'M': dia = Dias.Martes; break;
                case 'X': dia = Dias.Miercoles; break;
                case 'J': dia = Dias.Jueves; break;
                case 'V': dia = Dias.Viernes; break;
                case 'S': dia = Dias.Sabado; break;
                case 'D': dia = Dias.Domingo; break;
            }
            b.setSelected(dias.contains(dia));
        }
    }
    
    private ArrayList<Dias> getDias() {
        ArrayList<Dias> dias = new ArrayList<>();
        for (Node n : toolBarDias.getItems()) {
            ToggleButton b = ((ToggleButton) n);
            if (b.isSelected()) {
                switch(b.getText().charAt(0)) { //Sabemos que el texto de los botones siempre sera el mismo
                    case 'L': dias.add(Dias.Lunes); break;
                    case 'M': dias.add(Dias.Martes); break;
                    case 'X': dias.add(Dias.Miercoles); break;
                    case 'J': dias.add(Dias.Jueves); break;
                    case 'V': dias.add(Dias.Viernes); break;
                    case 'S': dias.add(Dias.Sabado); break;
                    case 'D': dias.add(Dias.Domingo); break;
                }
            }
        }
        return dias;
    }
    /** El spinner no actualiza los datos puestos a mano si el usuario no pulsa 'ENTER', 
     * asi que nos aseguramos de que lo haga antes de que se cree el curso utilizando 'spinner.increment(0)'.
     */
    private boolean checkData() {
        Alert errorAlert = new Alert(AlertType.ERROR);
        boolean correct = true;
        errorSpinner = false; //Antes de comprobar los datos, limpia el flag.
        
        //Comprueba que las fechas sean correctas
        LocalDate inicio = dateInicio.getValue();
        LocalDate fin = dateFin.getValue();
        correct = inicio != null && fin != null && inicio.isBefore(fin);
        if (!correct) { errorAlert.show(); return false; }
        
        ObservableList<Node> children = form.getChildren();
        int i = 0;
        while (i < children.size() && correct) {
            Node n = children.get(i);
            //Comprueba que los datos de los spinners sean correctos
            if (n instanceof Spinner) {
                ((Spinner) n).increment(0);
                if (errorSpinner) {
                    errorAlert.setHeaderText("Campo erróneo");
                    errorAlert.setContentText("Uno de los campos ha sido rellenado con un valor erróneo. Compruebe los datos y vuelva a intentarlo.");
                    correct = false;
                }
            }
            //Comprueba que los campos de texto no esten vacios
            else if (n instanceof TextField) {
                if (((TextField) n).getText().isEmpty()){
                    errorAlert.setHeaderText("Campo vacío");
                    errorAlert.setContentText("Alguno de los campos está vacío. Rellene todos los campos y vuelva a intentarlo.");
                    correct = false;
                }
            }
            i++;
        }
        if (!correct) { errorAlert.show(); return false; }
        
        //Si todo lo anterior fue correcto, comprueba que al menos hay un dia seleccionado
        children = toolBarDias.getItems();
        i = 0;
        boolean dayPicked = false;
        while (i < children.size() && !dayPicked) {
            ToggleButton b = (ToggleButton) children.get(i);
            dayPicked = b.isSelected();
            i++;
        }
        if (!dayPicked) {
                errorAlert.setHeaderText("Días no asignados");
                errorAlert.setContentText("No ha asignado ningún día de la semana para el curso. Asigne al menos uno y vuelva a intentarlo.");
                correct = false;
        }
        if (!correct) { errorAlert.show(); return false; }
        
        return correct;
    }
    
    @FXML
    private void createCurso(ActionEvent event) {
        boolean correct = checkData();
        if (correct) {
            AccesoaBD acceso = new AccesoaBD();
            ArrayList<Curso> listCursos = (ArrayList<Curso>) acceso.getCursos();
            Curso curso = new Curso(
                    textTitulo.getText(), //Titulo del curso
                    textProfesor.getText(), //Profesor asignado
                    spinnerMaxAlumnos.getValue(), //Numero maximo de alumnos
                    dateInicio.getValue(), //Fecha de inicio
                    dateFin.getValue(), //Fecha de fin
                    LocalTime.of(spinnerHours.getValue(), spinnerMinutes.getValue()), //Hora
                    getDias(), //Dias de la semana que se imparte
                    textAula.getText()); //Aula
            listCursos.add(curso);
            acceso.salvar();
            closeDialogue(event);
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
        spinnerMaxAlumnos.setValueFactory(new IntegerSpinnerValueFactory(0, Integer.MAX_VALUE));
        //spinnerMaxAlumnos.getEditor().textProperty().addListener();
        spinnerHours.setValueFactory(new CyclicIntegerSpinnerValueFactory(0, 23));
        spinnerMinutes.setValueFactory(new CyclicIntegerSpinnerValueFactory(0, 59));
    }    
}
