module mole.app {

    requires java.prefs;
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.web;

    requires mole.api;

    exports mole.app to javafx.graphics, javafx.fxml;
    opens mole.app to javafx.fxml;
}