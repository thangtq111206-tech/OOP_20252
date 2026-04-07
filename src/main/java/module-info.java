module com.example.oop_20252 {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.oop_20252 to javafx.fxml;
    exports com.example.oop_20252;
    exports com.example.oop_20252.controller;
    opens com.example.oop_20252.controller to javafx.fxml;
    exports com.example.oop_20252.view;
    opens com.example.oop_20252.view to javafx.fxml;
    exports com.example.oop_20252.util;
}