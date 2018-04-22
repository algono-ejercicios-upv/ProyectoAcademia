/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyecto.controller;

import accesoaBD.AccesoaBD;
import java.io.IOException;
import modelo.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * 
 *  Suponemos que: <p>
 *          - Dos Alumnos son iguales si tienen el mismo DNI <p>
 *          - Dos Cursos son iguales si tienen el mismo título <p>
 * @author aleja
 */
public class PrincipalController implements Initializable {
    @FXML
    private Tab tabAlumnos;
    @FXML
    private Tab tabCursos;
    @FXML
    private ListView<Alumno> listAlumnos;
    @FXML
    private ListView<Curso> listCursos;
    @FXML
    private ListView<Alumno> listAlumnosDeCurso;
    @FXML
    private ImageView image;
    @FXML
    private Label labelNombre;
    @FXML
    private Label labelDNI;
    @FXML
    private Label labelEdad;
    @FXML
    private Label labelDireccion;
    @FXML
    private Label labelFechaAlta;
    @FXML
    private ComboBox<Curso> comboCursos;
    @FXML
    private Button buttonAlta;
    @FXML
    private Button buttonBaja;
    @FXML
    private Button buttonMatricular;
    @FXML
    private Button buttonDesmatricular;
    @FXML
    private Button buttonNewCurso;
    @FXML
    private Button buttonViewCurso;
    @FXML
    private Button buttonRemoveCurso;
    
    private final AccesoaBD acceso = new AccesoaBD();
    //Formato para mostrar las fechas de una forma más cómoda para un usuario hispanohablante
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/LL/yyyy");
    //Dialogo de exito (mostrado en varias pantallas, así que solo vamos cambiando el texto mostrado).
    private final Alert exito = new Alert(AlertType.INFORMATION);
    //Listas
    private final ObservableList<Alumno> dataAlumnos = FXCollections.observableList(acceso.getAlumnos());
    private final ObservableList<Curso> dataCursos = FXCollections.observableList(acceso.getCursos());
    private final ArrayList<Matricula> dataMatriculas = (ArrayList<Matricula>) acceso.getMatriculas();
    private ObservableList<Alumno> dataAlumnosDeCurso = FXCollections.observableArrayList();
    private ObservableList<Curso> dataCursosDisponibles = FXCollections.observableArrayList();
    
    //ListCells para Alumno y Curso
    class AlumnoListCell extends ListCell<Alumno> {
        @Override
        protected void updateItem(Alumno item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) { setText(null); }
            else { setText(item.getNombre()); }
        }
    }
    class CursoListCell extends ListCell<Curso> {
        @Override
        protected void updateItem(Curso item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) { setText(null); }
            else { setText(item.getTitulodelcurso()); }
        }
    }
    /**
     * "Curso Disponible" es aquel que: <p>
     *      - No tiene el mismo horario que otro curso en el que el alumno ya esta matriculado <p>
     *      - No tenga matriculados el numero maximo de alumnos <p>
     * @param c Curso del que se comprueba si esta disponible
     * @param a Alumno para el que debe estar disponible el curso
     * @return Si el curso esta disponible o no para el alumno
     */
    private boolean isAvailable(Curso c, Alumno a) { 
        List<Alumno> alumnosDeCurso = acceso.getAlumnosDeCurso(c);  
        /* Si el numero de alumnos del curso excede del maximo, no se puede matricular otro
        (si la lista de alumnos del curso es 'null', entonces no tiene alumnos) */
        if (alumnosDeCurso != null && alumnosDeCurso.size() == c.getNumeroMaximodeAlumnos()) return false;
        //Almacenamos los dias y la hora del curso
        List<Dias> diasImparte = c.getDiasimparte(); 
        LocalTime hora = c.getHora();
        //Buscamos los cursos en los que el alumno tiene matricula
        for (Matricula m : dataMatriculas) {
            // Si la matricula es del alumno, comprueba las horas del curso
            if (m.getAlumno().getDni().equals(a.getDni())) { 
                //Almacenamos la hora del curso en el que ya estaba matriculado
                LocalTime mHora = m.getCurso().getHora();
                // Si las horas coinciden, comprueba los dias
                if (mHora.equals(hora)) {
                    List<Dias> mDias = m.getCurso().getDiasimparte();
                    // Si alguno de los dias coincide, no esta disponible
                    for (Dias d : diasImparte) { if (mDias.contains(d)) return false; }
                }
            }
        }
        return true; //Si todo se ha cumplido correctamente, esta disponible
    }
    private ObservableList<Curso> getAvailableCursos(Alumno a) {
        List<Curso> res = new ArrayList<>(); 
        if (dataCursos != null && a != null) {
            for (Curso c : dataCursos) {
                if (isAvailable(c, a)) { res.add(c); }
            }
        }
        return FXCollections.observableList(res);
    }
    
    private void setAlumnosDeCurso(Curso newValue) {
        if (newValue == null) { dataAlumnosDeCurso.clear(); }
            else {
                List<Alumno> alumnosDeCurso = acceso.getAlumnosDeCurso(newValue);
                if (alumnosDeCurso == null) { dataAlumnosDeCurso.clear(); }
                else {
                    dataAlumnosDeCurso = FXCollections.observableList(alumnosDeCurso);
                    listAlumnosDeCurso.setItems(dataAlumnosDeCurso);
                }
            }
    }
    
    private void gotoDialogueCursos(Curso actCurso) {
        try {
            Stage stage = new Stage();
            FXMLLoader myLoader = new FXMLLoader(getClass().getResource("/proyecto/view/DialogueCursoView.fxml"));
            Parent root = (Parent) myLoader.load();
            DialogueCursoController cursoCreator = myLoader.<DialogueCursoController>getController();
            cursoCreator.init(stage, dataCursos, actCurso);
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException ex) {}
    }
    @FXML
    private void gotoDialogueAlumnos() {
        try {
            Stage stage = new Stage();
            FXMLLoader myLoader = new FXMLLoader(getClass().getResource("/proyecto/view/DialogueAlumnoView.fxml"));
            Parent root = (Parent) myLoader.load();
            DialogueAlumnoController alumnoCreator = myLoader.<DialogueAlumnoController>getController();
            alumnoCreator.init(stage, dataAlumnos, image.getFitHeight(), image.getFitWidth());
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException ex) {}
    }
    //Metodos para eliminar alumno o curso. Tienen en cuenta si tienen matriculas asociadas o no, y avisan de ello al usuario.
    private void remove(Alumno a) {
        ArrayList<Matricula> matDelAlumno = new ArrayList<>();
        ObservableList<Curso> cursosDelAlumno = FXCollections.observableArrayList();
        //Buscamos los cursos en los que el alumno tiene matricula
        for (Matricula m : dataMatriculas) {
            if (m.getAlumno().getDni().equals(a.getDni())) {
                matDelAlumno.add(m);
                cursosDelAlumno.add(m.getCurso());
            }
        }
        if (!matDelAlumno.isEmpty()) {
            Alert aviso = new Alert(AlertType.CONFIRMATION);
            aviso.setContentText("Si da de baja al alumno, este será desmatriculado automáticamente de los siguientes cursos:\n ");
            ListView<Curso> expList = new ListView<>();
            expList.setFocusTraversable(false);
            expList.setCellFactory(c -> new CursoListCell());
            expList.setItems(cursosDelAlumno);
            aviso.getDialogPane().setExpandableContent(expList);
            Optional<ButtonType> result = aviso.showAndWait();
            if (result.isPresent() && result.get() != ButtonType.OK) return;
            dataMatriculas.removeAll(matDelAlumno);
        }
        dataAlumnos.remove(a);
        acceso.salvar();
        exito.setContentText("El alumno ha sido dado de baja correctamente");
        exito.show();
    }
    private void remove(Curso c) {
        if (!dataAlumnosDeCurso.isEmpty()) {
            Alert aviso = new Alert(AlertType.CONFIRMATION);
            aviso.setContentText("Si elimina el curso, los siguientes alumnos serán desmatriculados automáticamente:\n ");
            ListView<Alumno> expList = new ListView<>();
            expList.setFocusTraversable(false);
            expList.setCellFactory(lc -> new AlumnoListCell());
            expList.setItems(dataAlumnosDeCurso);
            aviso.getDialogPane().setExpandableContent(expList);
            Optional<ButtonType> result = aviso.showAndWait();
            if (result.isPresent() && result.get() != ButtonType.OK) return;
            List<Matricula> matriculasDeCurso = acceso.getMatriculasDeCurso(c);
            for (Matricula m : matriculasDeCurso) {
                dataMatriculas.remove(m);
            }
        }
        dataCursos.remove(c);
        acceso.salvar();
        exito.setContentText("El curso ha sido eliminado correctamente");
        exito.show();
    }
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        exito.setHeaderText(null);
        // Fijamos las CellFactory
        listAlumnos.setCellFactory(c -> new AlumnoListCell());
        listAlumnosDeCurso.setCellFactory(c -> new AlumnoListCell());
        listCursos.setCellFactory(c -> new CursoListCell());
        comboCursos.setButtonCell(new CursoListCell()); // En un comboBox, esta Cell hace referencia a la vista cuando esta seleccionado
        comboCursos.setCellFactory(c -> new CursoListCell()); // Y esta a cuando se ve en la lista del comboBox.
        
        //Llenamos las listas con sus respectivos datos
        listAlumnos.setItems(dataAlumnos);
        listCursos.setItems(dataCursos);
        listAlumnosDeCurso.setItems(dataAlumnosDeCurso);
        // Hacemos que cada vez que cambies de pestaña, las listas se actualicen
        tabAlumnos.setOnSelectionChanged(evt -> {
            if (tabAlumnos.isSelected()) {
                dataCursosDisponibles = getAvailableCursos(listAlumnos.getSelectionModel().getSelectedItem());
                comboCursos.setItems(dataCursosDisponibles);
            }
        });
        tabCursos.setOnSelectionChanged(evt -> {
            if (tabCursos.isSelected()) {
                setAlumnosDeCurso(listCursos.getSelectionModel().getSelectedItem());
            }
        });
        // Hacemos que aquellos botones que dependan de que haya un elemento seleccionado en una lista solo esten disponibles cuando lo haya
        buttonMatricular.disableProperty().bind(
                Bindings.equal(-1,
                        comboCursos.getSelectionModel().selectedIndexProperty()));
        buttonDesmatricular.disableProperty().bind(
                Bindings.equal(-1,
                        listAlumnosDeCurso.getSelectionModel().selectedIndexProperty()));
        buttonBaja.disableProperty().bind(
                Bindings.equal(-1,
                        listAlumnos.getSelectionModel().selectedIndexProperty()));
        buttonViewCurso.disableProperty().bind(
                Bindings.equal(-1,
                        listCursos.getSelectionModel().selectedIndexProperty()));
        buttonRemoveCurso.disableProperty().bind(
                Bindings.equal(-1,
                        listCursos.getSelectionModel().selectedIndexProperty()));
        
        //Cada vez que se seleccione un alumno, mostramos sus datos
        listAlumnos.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) {
                image.setImage(null);
                labelNombre.setText("");
                labelDNI.setText("");
                labelEdad.setText("");
                labelDireccion.setText("");
                labelFechaAlta.setText("");
                dataCursosDisponibles.clear();
            } else {
                image.setImage(newValue.getFoto());
                labelNombre.setText("Nombre: " + newValue.getNombre());
                labelDNI.setText("DNI: " + newValue.getDni());
                labelEdad.setText("Edad: " + newValue.getEdad());
                labelDireccion.setText("Dirección: " + newValue.getDireccion());
                labelFechaAlta.setText("Fecha de alta: " + newValue.getFechadealta().format(formatter));
                dataCursosDisponibles = getAvailableCursos(newValue);
                comboCursos.setItems(dataCursosDisponibles);
            }
        });
        
        //Mostramos los alumnos matriculados en un curso al seleccionarlo
        listCursos.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            setAlumnosDeCurso(newValue);
        });
        
        //Codigo para matricular a un alumno en un curso
        buttonMatricular.setOnAction((evt) -> {
            Alumno a = listAlumnos.getSelectionModel().getSelectedItem();
            Curso c = comboCursos.getSelectionModel().getSelectedItem();
            dataMatriculas.add(new Matricula(LocalDate.now(), c, a));
            acceso.salvar();
            dataCursosDisponibles.remove(c);
            //Actualiza la lista de cursos disponibles (ya que para el alumno seleccionado podría haber cambiado)
            dataCursosDisponibles = getAvailableCursos(a);
            comboCursos.setItems(dataCursosDisponibles);
            exito.setContentText("El alumno ha sido matriculado correctamente");
            exito.show();
        });
        
        //Codigo para desmatricular a un alumno de un curso
        buttonDesmatricular.setOnAction((evt) -> {
            Alumno a = listAlumnosDeCurso.getSelectionModel().getSelectedItem();
            Curso c = listCursos.getSelectionModel().getSelectedItem();
            int count = 0; //Buscamos el indice en la lista de la matricula
            while (count < dataMatriculas.size() 
                    && (!dataMatriculas.get(count).getAlumno().getDni().equals(a.getDni())
                    || !dataMatriculas.get(count).getCurso().getTitulodelcurso().equals(c.getTitulodelcurso()))) { count++; }
            if (count < dataMatriculas.size()) {
                dataMatriculas.remove(count);
                acceso.salvar();
                dataAlumnosDeCurso.remove(a);
                exito.setContentText("El alumno ha sido desmatriculado correctamente");
                exito.show();
            } else {
                new Alert(AlertType.ERROR, "Ha habido un error inesperado. Inténtelo de nuevo más tarde.").show();
            }
        });
        buttonNewCurso.setOnAction(evt -> gotoDialogueCursos(null));
        buttonViewCurso.setOnAction(evt -> gotoDialogueCursos(listCursos.getSelectionModel().getSelectedItem()));
        buttonBaja.setOnAction(evt -> remove(listAlumnos.getSelectionModel().getSelectedItem()));
        buttonRemoveCurso.setOnAction(evt -> remove(listCursos.getSelectionModel().getSelectedItem()));
    }
}
