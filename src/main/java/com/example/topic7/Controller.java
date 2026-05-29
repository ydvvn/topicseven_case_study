package com.example.topic7;

import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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

        // Load Enum to ChoiceBox
        cbYear.getItems().setAll(YearLevel.values());

        // Table Columns Binding
        colId.setCellValueFactory(data -> data.getValue().idProperty().asObject());
        colName.setCellValueFactory(data -> data.getValue().nameProperty());
        colCourse.setCellValueFactory(data -> data.getValue().courseProperty());
        colYear.setCellValueFactory(data -> data.getValue().yearLevelProperty());

        loadData();

        // Row click event
        table.setOnMouseClicked(e -> {
            Student s = table.getSelectionModel().getSelectedItem();
            if (s != null) {
                selectedId = s.getId();
                txtName.setText(s.getName());
                txtCourse.setText(s.getCourse());

                // Convert String back to Enum
                for (YearLevel y : YearLevel.values()) {
                    if (y.toString().equals(s.getYearLevel())) {
                        cbYear.setValue(y);
                    }
                }
            }
        });
    }

    private void loadData() {
        list.clear();
        try {
            String query = "SELECT * FROM students";
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

    private boolean isInputValid() {
        String errorMessage = "";

        if (txtName.getText() == null || txtName.getText().trim().isEmpty()) {
            errorMessage += "Name field cannot be empty.\n";
        }
        if (txtCourse.getText() == null || txtCourse.getText().trim().isEmpty()) {
            errorMessage += "Course field cannot be empty.\n";
        }
        if (cbYear.getValue() == null) {
            errorMessage += "Please select a Year Level.\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Error!");
            alert.setHeaderText("Missing Form Inputs");
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return false;
        }
    }

    @FXML
    public void addStudent() {
        // Validate inputs first
        if (!isInputValid()) {
            return;
        }

        try {
            String query = "INSERT INTO students(name, course, year_level) VALUES (?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(query);

            pst.setString(1, txtName.getText().trim());
            pst.setString(2, txtCourse.getText().trim());
            pst.setString(3, cbYear.getValue().toString());

            pst.executeUpdate();
            loadData();
            clearFields();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void updateStudent() {

        if (selectedId == -1) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error!");
            alert.setHeaderText(null);
            alert.setContentText("Select a student from the table first to update.");
            alert.showAndWait();
            return;
        }

        // Validate text field states
        if (!isInputValid()) {
            return;
        }

        try {
            String query = "UPDATE students SET name=?, course=?, year_level=? WHERE id=?";
            PreparedStatement pst = conn.prepareStatement(query);

            pst.setString(1, txtName.getText().trim());
            pst.setString(2, txtCourse.getText().trim());
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
    public void deleteStudent() {
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
    public void clearFields() {
        txtName.clear();
        txtCourse.clear();
        cbYear.setValue(null);
        selectedId = -1;
    }
}


