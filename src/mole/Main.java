package mole;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.*;
import java.util.prefs.Preferences;

import static java.nio.file.StandardWatchEventKinds.*;

public class Main extends Application {

    private MainWindow mainWindow;
    private Stage stage;
    private volatile boolean stop;
    private WatchService watchService;

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

        stage.getIcons().add(new Image(getClass().getResourceAsStream("/mole/images/icon-16.png")));
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/mole/images/icon-32.png")));
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/mole/images/icon-64.png")));
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/mole/images/icon-128.png")));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("main_window.fxml"));
        Parent root = loader.load();
        mainWindow = loader.getController();

        stage.setTitle("Mole");
        stage.setScene(new Scene(root, 1024, 768));
        restorePosition();
        stage.show();

        ResizeListener resizeListener = new ResizeListener();
        stage.xProperty().addListener(resizeListener);
        stage.yProperty().addListener(resizeListener);
        stage.heightProperty().addListener(resizeListener);
        stage.widthProperty().addListener(resizeListener);

        registerWatchService();
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

    private void registerWatchService() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            Path path = Paths.get(System.getProperty("user.dir"));
            path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY);

            Thread thread = new Thread(this::processEvents, "WatchServiceThread");
            thread.setDaemon(true);
            thread.start();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void processEvents() {
        System.out.println("Start watching");
        while (!stop && watchService != null) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException e) {
                System.out.println("Watching interrupted");
                return;
            } catch (ClosedWatchServiceException e) {
                System.out.println("Watching error: " + e);
                return;
            }
            Path dir = (Path) key.watchable();
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                // This key is registered only for ENTRY_CREATE events, but an OVERFLOW event can
                // occur regardless if events are lost or discarded.
                if (kind == OVERFLOW) {
                    continue;
                }

                // The filename is the context of the event.
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path filename = ev.context();
                Path path = dir.resolve(filename);

                System.out.println("File " + path + " changed: " + kind.name());
                if (path.getFileName().toString().equals("mole_trigger")) {
                    processTrigger(path);
                }
            }

            // Reset the key -- this step is critical if you want to receive further watch events.
            // If the key is no longer valid, the directory is inaccessible so exit the loop.
            boolean valid = key.reset();
            if (!valid) {
                System.out.println("Directory " + dir + " became inaccessible");
                break;
            }
        }
    }

    private void processTrigger(Path path) {
        Platform.runLater(() -> {
            byte[] bytes;
            try {
                bytes = Files.readAllBytes(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (bytes.length > 0) {
                String str = new String(bytes);
                mainWindow.searchString(str);
            } else {
                mainWindow.searchFromClipboard();
            }
            stage.toFront();
        });
    }
}
