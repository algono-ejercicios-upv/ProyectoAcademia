/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyecto.controller;

import accesoaBD.AccesoaBD;
import modelo.*;
import java.net.URL;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
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
    public static AccesoaBD acceso = new AccesoaBD();
    
    class TimeStringConverter extends IntegerStringConverter {
        @Override
        public String toString(Integer value) {
            return String.format("%02d", value);
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
        }
        for (Node n : form.getChildren()) { n.setMouseTransparent(curso != null); n.setFocusTraversable(curso == null);}
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
    
    @FXML
    private void createCurso() {
        ArrayList<Curso> listCursos = (ArrayList<Curso>) acceso.getCursos();
        Curso curso = new Curso(
            textTitulo.getText(),  //Titulo del curso
            textProfesor.getText(), //Profesor asignado
            spinnerMaxAlumnos.getValue(), //Numero maximo de alumnos
            dateInicio.getValue(), //Fecha de inicio
            dateFin.getValue(), //Fecha de fin
            LocalTime.of(spinnerHours.getValue(), spinnerMinutes.getValue()), //Hora
            getDias(), //Dias de la semana que se imparte
            textAula.getText()); //Aula
        listCursos.add(curso);
        acceso.salvar();
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
        IntegerSpinnerValueFactory valueFactory = new IntegerSpinnerValueFactory(0, acceso.getAlumnos().size());
        spinnerMaxAlumnos.setValueFactory(valueFactory);
        valueFactory = new IntegerSpinnerValueFactory(0, 23);
        valueFactory.setConverter(new TimeStringConverter());
        spinnerHours.setValueFactory(valueFactory);
        valueFactory = new IntegerSpinnerValueFactory(0, 59);
        valueFactory.setConverter(new TimeStringConverter());
        spinnerMinutes.setValueFactory(valueFactory);
    }    
}
