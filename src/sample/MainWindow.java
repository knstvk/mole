package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.ResourceBundle;

public class MainWindow implements Initializable {

    @FXML
    private TextField searchField;
    @FXML
    private WebView webTop;
    @FXML
    private WebView webBot;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void searchFromClipboard() {
        Clipboard systemClipboard = Clipboard.getSystemClipboard();
        String string = systemClipboard.getString();
        searchField.setText(string);
        search();
    }

    public void search() {
        String text = searchField.getText();
        webTop.getEngine().load("https://www.google.com/search?q=\"" + text + "\"&hl=en");

        webBot.getEngine().load("http://context.reverso.net/translation/english-russian/" + text.replace(" ", "+"));
    }

    public void searchKeyPressed(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            search();
        }
    }
}
