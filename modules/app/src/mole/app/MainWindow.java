package mole.app;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
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
        searchFromClipboard();
    }

    public void pasteFromClipboard() {
        Clipboard systemClipboard = Clipboard.getSystemClipboard();
        String string = systemClipboard.getString();
        if (string == null)
            string = "";
        searchField.setText(string);
    }

    public void searchFromClipboard() {
        pasteFromClipboard();
        search();
    }

    public void searchString(String str) {
        searchField.setText(str);
        search();
    }

    public void search() {
        String text = searchField.getText();
        webTop.getEngine().load("https://www.google.com/search?q=\"" + text + "\"&hl=en");
        webBot.getEngine().load("http://context.reverso.net/translation/english-russian/" + text.replace(" ", "+"));
    }
}
