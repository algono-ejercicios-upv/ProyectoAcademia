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
    public static final Dias[] SEMANA = Dias.values();
    private boolean errorSpinner = false;
    public static Alert errorAlert = new Alert(AlertType.ERROR);

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
        return dias;
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
                    errorAlert.setHeaderText("Campo vacío");
                    errorAlert.setContentText("Alguno de los campos está vacío.\nRellene todos los campos y vuelva a intentarlo.");
                    throw new InputMismatchException();
                }
            }
            //Comprueba que los datos de los spinners sean correctos
            else if (n instanceof Spinner) {
                ((Spinner) n).increment(0);
                if (errorSpinner) {
                    errorAlert.setHeaderText("Número erróneo");
                    errorAlert.setContentText("Uno de los campos númericos ha sido rellenado con un valor erróneo.\nCompruebe los datos y vuelva a intentarlo.");
                    throw new InputMismatchException();
                }
            }
            i++;
        }
    }
    //Comprueba que las fechas sean correctas (Ninguna este vacia, y la de inicio sea anterior a la de fin)
    private void checkDates() {
        LocalDate inicio = dateInicio.getValue();
        LocalDate fin = dateFin.getValue();
        errorAlert.setHeaderText("Fechas incorrectas");
        if (inicio == null || fin == null) {
            errorAlert.setContentText("Alguna de las fechas introducidas es incorrecta.\nCompruébelas y vuelva a intentarlo.");
            dateInicio.requestFocus();
            throw new InputMismatchException();
        } else if (inicio.isBefore(LocalDate.now())) {
            errorAlert.setContentText("La fecha de inicio no debe ser anterior a la fecha de hoy.\nIntroduzca una fecha correcta y vuelva a intentarlo.");
            dateInicio.requestFocus();
            throw new InputMismatchException();
        } else if (!inicio.isBefore(fin)) {
            errorAlert.setContentText("La fecha de fin no debe ser anterior a la de inicio.\nIntroduzca una fecha correcta y vuelva a intentarlo.");
            dateFin.requestFocus();
            throw new InputMismatchException();
        }
    }
    //Comprueba que al menos un dia haya sido seleccionado
    private void checkDias() {
        ObservableList<Node> children = toolBarDias.getItems();
        boolean dayPicked = false;
        int i = 0;
        while (i < children.size() && !dayPicked) {
            ToggleButton b = (ToggleButton) children.get(i);
            dayPicked = b.isSelected();
            i++;
        }
        if (!dayPicked) {
                errorAlert.setHeaderText("Días no asignados");
                errorAlert.setContentText("No ha asignado ningún día de la semana para el curso.\nAsigne al menos uno y vuelva a intentarlo.");
                throw new InputMismatchException();
        }
    }

    @FXML
    private void createCurso(ActionEvent event) {
        try {
            checkFields();            
            checkDates();
            checkDias();
            AccesoaBD acceso = new AccesoaBD();
            ArrayList<Curso> listCursos = (ArrayList<Curso>) acceso.getCursos();
            Curso curso = new Curso(
                    textTitulo.getText(), //Titulo del curso
                    textProfesor.getText(), //Profesor asignado
                    spinnerMaxAlumnos.getValue(), //Numero maximo de alumnos
                    dateInicio.getValue(), //Fecha de inicio
                    dateFin.getValue(), //Fecha de fin
                    LocalTime.of(spinnerHours.getValue(), spinnerMinutes.getValue()), //Hora
                    getDias(), //Dias de la SEMANA que se imparte
                    textAula.getText()); //Aula
            listCursos.add(curso);
            acceso.salvar();
            closeDialogue(event);
        } catch (InputMismatchException e) { errorAlert.show(); }
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
