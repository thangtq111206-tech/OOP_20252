module com.example.oop_20252 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.oop_20252 to javafx.fxml;
    exports com.example.oop_20252;
}