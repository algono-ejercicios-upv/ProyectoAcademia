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
    
    private ObservableList<Curso> dataCursos;
    private Stage primaryStage;
    public static final Dias[] SEMANA = Dias.values();

    public void init(Stage stage, ObservableList<Curso> dC, Curso curso) {
        primaryStage = stage;
        dataCursos = dC;
        if (curso == null) {
            stage.setTitle("Nuevo Curso");
        } else {
            stage.setTitle("Ver Curso");
            textTitulo.setText(curso.getTitulodelcurso());
            textProfesor.setText(curso.getProfesorAsignado());
            spinnerMaxAlumnos.getValueFactory().setValue(curso.getNumeroMaximodeAlumnos());
            dateInicio.setValue(curso.getFechainicio());
            dateFin.setValue(curso.getFechafin());
            LocalTime hora = curso.getHora();
            spinnerHours.getValueFactory().setValue(hora.getHour());
            spinnerMinutes.getValueFactory().setValue(hora.getMinute());
            setDias(curso.getDiasimparte());
            textAula.setText(curso.getAula());
            buttonCreate.setDisable(true);
            form.setMouseTransparent(true);
            primaryStage.addEventFilter(KeyEvent.ANY, e -> e.consume()); //Evita que puedas realizar ninguna accion con el teclado
            buttonCerrar.requestFocus();
        }
    }
    /**
    * El orden de los botones en {@link #toolBarDias ToolBarDias} es el mismo en el que estan colocados (L,M,X,J,V,S,D)
    * Por tanto, podemos obtener sus dias en el mismo orden
    */
    private ArrayList<Dias> getDias() {
        ArrayList<Dias> dias = new ArrayList<>();
        ObservableList<Node> items = toolBarDias.getItems();
        for (int i = 0; i < items.size(); i++) {
            ToggleButton b = (ToggleButton) items.get(i);
            if (b.isSelected()) dias.add(SEMANA[i]);
        }
        if (dias.isEmpty()) {
            String message = "Días no seleccionados;"
                            + "No ha seleccionado ningún día para el curso.\nSeleccione por lo menos uno y vuelva a intentarlo.";
            throw new IllegalArgumentException(message); 
        }
        return dias;
    }
    /**
     * @param dias Array ordenado (ya que {@link #getDias() getDias()} los inserta en orden).
     */
    private void setDias(ArrayList<Dias> dias) {
        ObservableList<Node> items = toolBarDias.getItems();
        int i = 0, j = 0;
        while (i < dias.size()) {
            Dias dia = dias.get(i);
            while (j < SEMANA.length && !dia.equals(SEMANA[j])) j++;
            if (j < SEMANA.length) {
                ToggleButton b = (ToggleButton) items.get(j);
                b.setSelected(true);
            }
            i++; j++;
        }
    }
    /**
     * Comprobamos que el campo de texto no este vacio y lo devuelve.
     */
    private String getText(TextField field) {
        String res = field.getText();
        if (res.isEmpty()) {
            String message = "Campo vacío;"
                            + "Alguno de los campos está vacío.\nRellene todos los campos y vuelva a intentarlo.";
            throw new IllegalArgumentException(message); 
        }
        return res;
    }
    //Comprueba que las fechas sean validas y las devuelve(La fecha de inicio no es anterior a la actual, y la de inicio es anterior a la de fin)
    private LocalDate getFechaInicio() {
        LocalDate inicio = dateInicio.getValue();
        if (inicio.isBefore(LocalDate.now())) {
            String message = "Fecha de inicio inválida;"
                    + "La fecha de inicio no debe ser anterior a la fecha de hoy.\nIntroduzca una fecha válida y vuelva a intentarlo.";
            dateInicio.requestFocus();
            throw new IllegalArgumentException(message);
        }
        return inicio;
    }
    private LocalDate getFechaFin() {
        LocalDate inicio = dateInicio.getValue();
        LocalDate fin = dateFin.getValue();
        if (inicio.isAfter(fin)) {
            String message = "Fecha de fin inválida;"
                    + "La fecha de fin no debe ser anterior a la de inicio.\nIntroduzca una fecha válida y vuelva a intentarlo.";
            dateFin.requestFocus();
            throw new IllegalArgumentException(message);
        }
        return fin;
    }
    @FXML
    private void createCurso(ActionEvent event) {
        try {
            AccesoaBD acceso = new AccesoaBD();
            Curso curso = new Curso(
                    getText(textTitulo), //Titulo del curso
                    getText(textProfesor), //Profesor asignado
                    spinnerMaxAlumnos.getValue(), //Numero maximo de alumnos
                    getFechaInicio(), //Fecha de inicio
                    getFechaFin(), //Fecha de fin
                    LocalTime.of(spinnerHours.getValue(), spinnerMinutes.getValue()), //Hora
                    getDias(), //Dias de la SEMANA que se imparte
                    getText(textAula)); //Aula
            dataCursos.add(curso);
            acceso.salvar();
            Alert exito = new Alert(AlertType.INFORMATION, "El curso ha sido creado correctamente");
            exito.setHeaderText(null);
            exito.showAndWait();
            closeDialogue(event);
        } catch (IllegalArgumentException ex) {
            String[] split = ex.getMessage().split(";");
            Alert error = new Alert(AlertType.ERROR);
            error.setHeaderText(split[0]);
            error.setContentText(split[1]);
            error.show();
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
        //Hacemos un converter personalizado para que el texto tenga un formato como "02:03", en vez de "2:3".
        IntegerStringConverter timeConverter = new IntegerStringConverter() {
            @Override public String toString(Integer value) { return String.format("%02d", value); }
            @Override public Integer fromString(String string) { return super.fromString(string); }
        };
        //No permitimos la creación de un curso con maxAlumnos = 0, ya que no tendría sentido
        spinnerMaxAlumnos.setValueFactory(new IntegerSpinnerValueFactory(1, Integer.MAX_VALUE));
        IntegerSpinnerValueFactory vF = new IntegerSpinnerValueFactory(0, 23);
        vF.setConverter(timeConverter);
        vF.setWrapAround(true);
        spinnerHours.setValueFactory(vF);
        vF = new IntegerSpinnerValueFactory(0, 59);
        vF.setConverter(timeConverter);
        vF.setWrapAround(true);
        spinnerMinutes.setValueFactory(vF);
        dateInicio.setValue(LocalDate.now());
        dateFin.setValue(LocalDate.now());
    }
}
