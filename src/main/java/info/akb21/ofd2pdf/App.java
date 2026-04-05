package info.akb21.ofd2pdf;

import info.akb21.ofd2pdf.service.ConversionManager;
import info.akb21.ofd2pdf.service.AppPreferences;
import info.akb21.ofd2pdf.service.OfdrwConverterService;
import info.akb21.ofd2pdf.service.OutputPathResolver;
import info.akb21.ofd2pdf.ui.MainController;
import info.akb21.ofd2pdf.ui.MainView;
import info.akb21.ofd2pdf.util.FontSupport;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;
import javafx.scene.image.Image;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        FontSupport.initialize();

        OutputPathResolver outputPathResolver = new OutputPathResolver();
        ConversionManager conversionManager = new ConversionManager(new OfdrwConverterService());
        AppPreferences preferences = new AppPreferences();
        MainController controller = new MainController(
                stage,
                outputPathResolver,
                conversionManager,
                preferences,
                loadAppVersion()
        );
        MainView mainView = new MainView(controller);

        Scene scene = new Scene(mainView.getRoot(), 980, 640);
        stage.setTitle("OFD 转 PDF");
        InputStream iconStream = App.class.getResourceAsStream("/icons/app.png");
        if (iconStream != null) {
            stage.getIcons().add(new Image(iconStream));
        }
        stage.setMinWidth(900);
        stage.setMinHeight(560);
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> controller.shutdown());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private String loadAppVersion() {
        String fallback = "dev";
        try (InputStream in = App.class.getResourceAsStream("/app.properties")) {
            if (in == null) {
                return fallback;
            }
            Properties properties = new Properties();
            properties.load(in);
            String version = properties.getProperty("app.version");
            if (version == null || version.isBlank()) {
                return fallback;
            }
            return version.trim();
        } catch (IOException ex) {
            return fallback;
        }
    }
}
