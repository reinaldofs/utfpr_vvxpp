package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Para passar por parametro no windows usar essa string de exemplo:
 * "C:\Users\gabri\Desktop\Verificacao e validacao aps teste\olamundo.x"
 *
 * Já no MAC usar essa:
 */
public class Main extends Application {

   private static String caminhoArquivo;
  
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Cobertura X++");
        primaryStage.setScene(new Scene(root, 1280, 690));
        primaryStage.centerOnScreen();
        primaryStage.setMaximized(true);
        primaryStage.show();
    }


    public static void main(String[] args) {
        setCaminhoArquivo( args[0] );
        launch(args);
    }

    public static String getCaminhoArquivo() {
        return caminhoArquivo;
    }

    public static void setCaminhoArquivo(String caminhoArquivo) {
        Main.caminhoArquivo = caminhoArquivo;
    }
}
