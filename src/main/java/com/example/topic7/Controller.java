package com.example.topic7;

import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.*;

public class Controller {

    @FXML private TextField txtName;
    @FXML private TextField txtCourse;
    @FXML private ChoiceBox<YearLevel> cbYear;

    @FXML private TableView<Student> table;
    @FXML private TableColumn<Student, Integer> colId;
    @FXML private TableColumn<Student, String> colName;
    @FXML private TableColumn<Student, String> colCourse;
    @FXML private TableColumn<Student, String> colYear;

    private ObservableList<Student> list = FXCollections.observableArrayList();
    private Connection conn;
    private int selectedId = -1;


    @FXML
    public void initialize() {
        conn = DBConnection.connect();


        if (cbYear != null) {
            cbYear.getItems().setAll(YearLevel.values());
        }

        if (table != null) {
            colId.setCellValueFactory(data -> data.getValue().idProperty().asObject());
            colName.setCellValueFactory(data -> data.getValue().nameProperty());
            colCourse.setCellValueFactory(data -> data.getValue().courseProperty());
            colYear.setCellValueFactory(data -> data.getValue().yearLevelProperty());

            if (conn != null) {
                loadData();
            }

            // Row click event
            table.setOnMouseClicked(e -> {
                Student s = table.getSelectionModel().getSelectedItem();
                if (s != null) {
                    selectedId = s.getId();

                    // Extra safe guard in case text fields are separated later
                    if (txtName != null) txtName.setText(s.getName());
                    if (txtCourse != null) txtCourse.setText(s.getCourse());

                    if (cbYear != null) {
                        for (YearLevel y : YearLevel.values()) {
                            if (y.toString().equals(s.getYearLevel())) {
                                cbYear.setValue(y);
                            }
                        }
                    }
                }
            });
        }
    }

    private void loadData() {
        if (conn == null) return;
        list.clear();
        try {
            String query = "SELECT * FROM students ORDER BY id ASC";
            ResultSet rs = conn.createStatement().executeQuery(query);

            while (rs.next()) {
                list.add(new Student(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("course"),
                        rs.getString("year_level")
                ));
            }
            table.setItems(list);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void addStudent() {
        if (validateInputs()) return;
        try {
            String query = "INSERT INTO students(name, course, year_level) VALUES (?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(query);

            pst.setString(1, txtName.getText());
            pst.setString(2, txtCourse.getText());
            pst.setString(3, cbYear.getValue().toString());

            pst.executeUpdate();
            loadData();
            clearFields();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void updateStudent() {
        if (selectedId == -1 || validateInputs()) return;
        try {
            String query = "UPDATE students SET name=?, course=?, year_level=? WHERE id=?";
            PreparedStatement pst = conn.prepareStatement(query);

            pst.setString(1, txtName.getText());
            pst.setString(2, txtCourse.getText());
            pst.setString(3, cbYear.getValue().toString());
            pst.setInt(4, selectedId);

            pst.executeUpdate();
            loadData();
            clearFields();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteStudent() {
        if (selectedId == -1) return;
        try {
            String query = "DELETE FROM students WHERE id=?";
            PreparedStatement pst = conn.prepareStatement(query);

            pst.setInt(1, selectedId);
            pst.executeUpdate();

            loadData();
            clearFields();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void clearFields() {
        if (txtName != null) txtName.clear();
        if (txtCourse != null) txtCourse.clear();
        if (cbYear != null) cbYear.setValue(null);
        selectedId = -1;
    }

    // to prevent adding empty SQL records
    private boolean validateInputs() {
        if (txtName == null || txtCourse == null || cbYear == null) return false;
        if (txtName.getText().trim().isEmpty() || txtCourse.getText().trim().isEmpty() || cbYear.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "All fields are required!", ButtonType.OK);
            alert.showAndWait();
            return true;
        }
        return false;
    }
}
