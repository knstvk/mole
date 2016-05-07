package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.NativeInputEvent;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {

    private MainWindow mainWindow;
    private Stage stage;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        stage.setOnHiding(e -> unregisterKeyboardHook());

//        Parent root = loader.load(getClass().getResource("main_window.fxml"));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("main_window.fxml"));
        Parent root = loader.load();
        mainWindow = loader.getController();

        stage.setTitle("Mole");
        stage.setScene(new Scene(root, 1024, 768));
        stage.show();

        registerKeyboardHook();
    }


    public static void main(String[] args) {
        launch(args);
    }

    private void registerKeyboardHook() {
        // Get the logger for "org.jnativehook" and set the level to off.
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        logger.setUseParentHandlers(false);
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook");
            System.err.println(ex.getMessage());

            System.exit(1);
        }
        GlobalScreen.addNativeKeyListener(new MainNativeKeyListener());
    }

    private static void unregisterKeyboardHook() {
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e) {
            System.err.println("There was a problem unregistering the native hook");
            System.err.println(e.getMessage());
        }
        System.runFinalization();
    }

    public class MainNativeKeyListener implements NativeKeyListener {

        @Override
        public void nativeKeyPressed(NativeKeyEvent e) {
            if (e.getKeyCode() == NativeKeyEvent.VC_F2 && ((e.getModifiers() & NativeInputEvent.META_MASK) != 0)) {
                System.out.println("Win+F2 pressed");
                Platform.runLater(() -> {
                    stage.toFront();
                    mainWindow.searchFromClipboard();
                });
            }
        }

        @Override
        public void nativeKeyReleased(NativeKeyEvent e) {
        }

        @Override
        public void nativeKeyTyped(NativeKeyEvent e) {
        }
    }
}
