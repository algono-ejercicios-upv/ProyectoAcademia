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
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.converter.LocalTimeStringConverter;

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
    private Spinner<LocalTime> spinnerTime;
    @FXML
    private TextField textTitulo;
    @FXML
    private TextField textProfesor;
    @FXML
    private TextField textAula;
    @FXML
    private ToolBar toolBarDias;

    private Stage primaryStage;
    public static final Dias[] SEMANA = Dias.values();

    class TimeSpinnerValueFactory extends SpinnerValueFactory<LocalTime> {
        
        public TimeSpinnerValueFactory() {
            super();
            setConverter(new LocalTimeStringConverter());
            setValue(LocalTime.MIDNIGHT);
        }
        @Override
        public void increment(int steps) {
            setValue(getValue().plusMinutes(steps));
        }
        @Override
        public void decrement(int steps) {
            setValue(getValue().minusMinutes(steps));
        }
    }

    public void init(Stage stage, Curso curso) {
        primaryStage = stage;
        if (curso == null) {
            stage.setTitle("Nuevo Curso");
        } else {
            stage.setTitle("Ver Curso");
            textTitulo.setText(curso.getTitulodelcurso());
            textProfesor.setText(curso.getProfesorAsignado());
            spinnerMaxAlumnos.getValueFactory().setValue(curso.getNumeroMaximodeAlumnos());
            dateInicio.setValue(curso.getFechainicio());
            dateFin.setValue(curso.getFechafin());
            spinnerTime.getValueFactory().setValue(curso.getHora());
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
        if (dias.isEmpty()) { throw new IllegalArgumentException(); }
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
     * El spinner no actualiza los datos puestos a mano si el usuario no pulsa 'ENTER',
     * asi que nos aseguramos de que lo haga antes de que se cree el curso utilizando 'spinner.increment(0)'.
     * Ademas, comprobamos que los campos de texto no esten vacios.
     */
    private void checkFields() {
        ObservableList<Node> children = form.getChildren();
        int i = 0;
        while (i < children.size()) {
            Node n = children.get(i);
            //Comprueba que los campos de texto no esten vacios
            if (n instanceof TextField) {
                if (((TextField) n).getText().isEmpty()){
                    String message = "Campo vacío;"
                            + "Alguno de los campos está vacío.\nRellene todos los campos y vuelva a intentarlo.";
                    throw new IllegalArgumentException(message);
                }
            }
            i++;
        }
    }
    //Comprueba que las fechas sean validas (La fecha de inicio no es anterior a la actual, y la de inicio es anterior a la de fin)
    private void checkDates() {
        LocalDate inicio = dateInicio.getValue();
        LocalDate fin = dateFin.getValue();
        String message;
        if (inicio.isBefore(LocalDate.now())) {
            message = "Fecha de inicio inválida;"
                    + "La fecha de inicio no debe ser anterior a la fecha de hoy.\nIntroduzca una fecha válida y vuelva a intentarlo.";
            dateInicio.requestFocus();
            throw new IllegalArgumentException(message);
        } else if (!inicio.isBefore(fin)) {
            message = "Fecha de fin inválida;"
                    + "La fecha de fin no debe ser anterior a la de inicio.\nIntroduzca una fecha válida y vuelva a intentarlo.";
            dateFin.requestFocus();
            throw new IllegalArgumentException(message);
        }
    }
    @FXML
    private void createCurso(ActionEvent event) {
        try {
            checkFields();            
            checkDates();
            AccesoaBD acceso = new AccesoaBD();
            ArrayList<Curso> listCursos = (ArrayList<Curso>) acceso.getCursos();
            Curso curso = new Curso(
                    textTitulo.getText(), //Titulo del curso
                    textProfesor.getText(), //Profesor asignado
                    spinnerMaxAlumnos.getValue(), //Numero maximo de alumnos
                    dateInicio.getValue(), //Fecha de inicio
                    dateFin.getValue(), //Fecha de fin
                    spinnerTime.getValue(), //Hora
                    getDias(), //Dias de la SEMANA que se imparte
                    textAula.getText()); //Aula
            listCursos.add(curso);
            acceso.salvar();
            closeDialogue(event);
        } catch (IllegalArgumentException e) {
            String[] split = e.getMessage().split(";");
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
        spinnerMaxAlumnos.setValueFactory(new IntegerSpinnerValueFactory(0, Integer.MAX_VALUE));
        spinnerTime.setValueFactory(new TimeSpinnerValueFactory());
        dateInicio.setValue(LocalDate.now());
        dateFin.setValue(LocalDate.now());
    }
}
