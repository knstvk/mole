package mole.app;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.prefs.Preferences;

public class Main extends Application {

    private MainWindow mainWindow;
    private Stage stage;

    private double x;
    private double y;
    private double height;
    private double width;

    private class ResizeListener implements ChangeListener<Number> {
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            if (stage.getX() >= 0 && stage.getY() >= 0) {
                x = stage.getX();
                y = stage.getY();
                height = stage.getHeight();
                width = stage.getWidth();
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;

        stage.getIcons().add(new Image(getClass().getResourceAsStream("/mole/app/images/icon-512.png")));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("main_window.fxml"));
        Parent root = loader.load();
        mainWindow = loader.getController();

        stage.setTitle("Mole");
        stage.setScene(new Scene(root, 1024, 768));
        restorePosition();

        stage.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                mainWindow.pasteFromClipboard();
            }
        });

        stage.show();

        ResizeListener resizeListener = new ResizeListener();
        stage.xProperty().addListener(resizeListener);
        stage.yProperty().addListener(resizeListener);
        stage.heightProperty().addListener(resizeListener);
        stage.widthProperty().addListener(resizeListener);
    }

    @Override
    public void stop() throws Exception {
        savePosition();
    }

    private void savePosition() {
        Preferences userPrefs = Preferences.userNodeForPackage(getClass());
        userPrefs.putBoolean("stage.maximized", stage.isMaximized());
        userPrefs.putDouble("stage.x", x);
        userPrefs.putDouble("stage.y", y);
        userPrefs.putDouble("stage.width", width);
        userPrefs.putDouble("stage.height", height);
    }

    private void restorePosition() {
        Preferences userPrefs = Preferences.userNodeForPackage(getClass());
        stage.setMaximized(userPrefs.getBoolean("stage.maximized", false));
        if (!stage.isMaximized()) {
            x = userPrefs.getDouble("stage.x", 100);
            stage.setX(x);
            y = userPrefs.getDouble("stage.y", 100);
            stage.setY(y);
            width = userPrefs.getDouble("stage.width", 1024);
            stage.setWidth(width);
            height = userPrefs.getDouble("stage.height", 768);
            stage.setHeight(height);
        }
    }
}
