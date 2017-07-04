package view;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Controller {

    @FXML
    private TextField inputCaminho;

    private FileChooser fileChooser;

    private StringBuffer stringBuffer;

    private StringBuffer stringBufferXPluPlus;

    @FXML
    private ScrollPane scrollPaneOutput;

    @FXML
    private AnchorPane panelnternoScroll;

    private List<String> linhas = new ArrayList<>();

    public void buscar(){
        stringBuffer = new StringBuffer();

        fileChooser = new FileChooser();
        fileChooser.setTitle("Selecione o Arquivo de Mapeamento");
        File selectedFile = fileChooser.showOpenDialog(null);

        File xPlusPlusSelected = null;
        if (selectedFile != null) {
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(selectedFile));

                String s = bufferedReader.readLine();

                while (s != null){
                    stringBuffer.append(s);
                    s = bufferedReader.readLine();
                }

                List<Mapeamento> maps = processaMapeamento(stringBuffer.toString());

                fileChooser = new FileChooser();
                fileChooser.setTitle("Selecione o X++");
                xPlusPlusSelected = fileChooser.showOpenDialog(null);

                BufferedReader bufferedReader2 = new BufferedReader(new FileReader(xPlusPlusSelected));

                String s2 = bufferedReader2.readLine();
                stringBufferXPluPlus = new StringBuffer();
                linhas.add(s2);
                while (s2 != null){
                    stringBufferXPluPlus.append(s2);
                    s2 = bufferedReader2.readLine();
                    linhas.add(s2);
                }

                processaContextoGrafico(maps);



            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

    }

    public List<Mapeamento>  processaMapeamento(String a){

        String vectorMap[] = a.replace("{", "1").split("\\|");
        List<Mapeamento> mapeamentos = new ArrayList<>();

        for (int i = 0; i < vectorMap.length; i++){

            String vetorPontoEVirgula[] = vectorMap[i].split(";");
            Map<String, Integer> objetos = new HashMap();

            for (int c = 0; c < vetorPontoEVirgula.length; c++) {

                String value = vetorPontoEVirgula[c].split("=")[0].trim();
                if("beginColumn".equals(value) || "endColumn".equals(value) || "endLine".equals(value) || "beginLine".equals(value)) {
                    objetos.put(value, Integer.parseInt(vetorPontoEVirgula[c].split("=")[1]));
                }
            }

            Mapeamento mapeamento = new Mapeamento();
            mapeamento.setColunaInicio(objetos.get("beginColumn"));
            mapeamento.setColunaFim(objetos.get("endColumn"));
            mapeamento.setLinhaFim(objetos.get("endLine"));
            mapeamento.setLinhaInicio(objetos.get("beginLine"));
            mapeamentos.add(mapeamento);
        }
        return mapeamentos;
    }

    public void processaContextoGrafico( List<Mapeamento> maps){

        int posicaox = 15;
        int posicaoy = 13;

        Canvas canvas = new Canvas( 637, 327);

        GraphicsContext gc2 = canvas.getGraphicsContext2D();
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc2.setFill( Color.BLUE );
        gc.setFill( Color.RED );
        gc.setStroke( Color.BLACK );
        gc.setLineWidth(2);
        Font theFont = Font.font( "Times New Roman", FontWeight.BOLD, 12 );
        gc.setFont( theFont );
        gc2.setFont(theFont);

        panelnternoScroll.getChildren().add(canvas);


        for (int i = 0; i<linhas.size(); i++) {
            posicaoy = posicaoy + 13;

            gc.fillText( linhas.get(i), posicaox, posicaoy );
        }

    }

}
