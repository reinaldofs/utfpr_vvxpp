package view;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
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

    private List<String> nos = new ArrayList<>();

    @FXML
    private ScrollPane scrollPaneOutput;

    @FXML
    private AnchorPane panelnternoScroll;

    private List<String> linhas = new ArrayList<>();

    @FXML
    private PieChart graficoPizza;

    @FXML
    private Label lblQtNosExecut;

    @FXML
    private BarChart<Integer, Integer> barChartFx;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private TextField classeTxtField;

    @FXML
    private TextField metodoTxtField;


    public void buscar() {
        stringBuffer = new StringBuffer();

        fileChooser = new FileChooser();
        fileChooser.setTitle("Selecione o Arquivo de Mapeamento");
        File selectedFile = fileChooser.showOpenDialog(null);

        File xPlusPlusSelected = null;
        if (selectedFile != null) {
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(selectedFile));

                String s = bufferedReader.readLine();

                while (s != null) {
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
                while (s2 != null) {
                    stringBufferXPluPlus.append(s2);
                    s2 = bufferedReader2.readLine();
                    linhas.add(s2);
                }

                fileChooser = new FileChooser();
                fileChooser.setTitle("Selecione o Arquivo de nós");
                xPlusPlusSelected = fileChooser.showOpenDialog(null);

                BufferedReader bufferedReader3 = new BufferedReader(new FileReader(xPlusPlusSelected));

                String s3 = bufferedReader3.readLine();
                nos = new ArrayList<>();
                nos.add(s3);
                while (s3 != null) {
                    s3= bufferedReader3.readLine();
                    if(!"".equals(s3)){
                        nos.add(s3);
                    }
                }

                processaContextoGrafico(maps);

                criaGraficoPizza(maps);


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public List<Mapeamento> processaMapeamento(String a) {

        String vectorMap[] = a.replace("{", "1").split("\\|");
        List<Mapeamento> mapeamentos = new ArrayList<>();

        for (int i = 0; i < vectorMap.length; i++) {

            String vetorPontoEVirgula[] = vectorMap[i].split(";");
            Map<String, Integer> objetos = new HashMap();

            for (int c = 0; c < vetorPontoEVirgula.length; c++) {

                String value = vetorPontoEVirgula[c].split("=")[0].trim();
                if ("beginColumn".equals(value) || "endColumn".equals(value) || "endLine".equals(value) || "beginLine".equals(value) || "number".equals(value)) {
                    objetos.put(value, Integer.parseInt(vetorPontoEVirgula[c].split("=")[1]));
                }
            }

            Mapeamento mapeamento = new Mapeamento();
            mapeamento.setColunaInicio(objetos.get("beginColumn"));
            mapeamento.setColunaFim(objetos.get("endColumn"));
            mapeamento.setLinhaFim(objetos.get("endLine"));
            mapeamento.setLinhaInicio(objetos.get("beginLine"));
            mapeamento.setNumber(objetos.get("number"));
            mapeamentos.add(mapeamento);
        }
        return mapeamentos;
    }

    public void processaContextoGrafico(List<Mapeamento> maps) {

        int posicaox = 15;
        int posicaoy = 13;

        Canvas canvas = new Canvas(637, 327);

        GraphicsContext gc2 = canvas.getGraphicsContext2D();
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc2.setFill(Color.BLUE);
        gc.setFill(Color.RED);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        Font theFont = Font.font("Times New Roman", FontWeight.BOLD, 12);
        gc.setFont(theFont);
        gc2.setFont(theFont);

        panelnternoScroll.getChildren().add(canvas);


        for (int i = 0; i < linhas.size(); i++) {
            posicaoy = posicaoy + 13;

            gc.fillText(linhas.get(i), posicaox, posicaoy);
        }


    }

    public void criaGraficoPizza(List<Mapeamento> maps) {
    	graficoPizza.getData().clear();
    	barChartFx.getData().clear();

        Integer totalExecutado = 0;


        stringBufferXPluPlus.trimToSize();

        Map<Integer, Integer> nosQtExecutado = new HashMap();

        for (String no:nos) {

            if(no != null) {
                System.out.println(no.split(":")[0]);
                String a = no.split(":")[0];
                if(a != null && !"".equals(a) && !"null".equals(a) ) {
                    nosQtExecutado.put(Integer.parseInt(a), 0);
                }
            }
        }

        for (Mapeamento mapeamento : maps) {
            if(mapeamento.getNumber() != null){
                if(nosQtExecutado.containsKey(mapeamento.getNumber())){
                    nosQtExecutado.put(mapeamento.getNumber(), nosQtExecutado.get(mapeamento.getNumber()) + 1);
                }
            }
        }
        List<String> nosChave = new ArrayList<>();
        Integer qtExecutados = 0;
        Integer qtNaoExecutados = 0;
        for (Map.Entry<Integer, Integer> entry : nosQtExecutado.entrySet())
        {
            nosChave.add(entry.getKey().toString());
            System.out.println(entry.getKey() + "/" + entry.getValue());
            if(entry.getValue() == 0){
                qtNaoExecutados ++;
            }else{
                qtExecutados ++;
            }
        }

        System.out.println("QT EXECUTADOS:" + qtExecutados);
        System.out.println("QT N EXECUTADOS:" + qtNaoExecutados);
        lblQtNosExecut.setText("Qt. nós Executados:" + qtExecutados + " \nQt nós não Executados " + qtNaoExecutados);
    //Porcentagem de nós executados por não executados


        Integer totalExecutadosEnaoExecutados = qtNaoExecutados + qtExecutados;

        Double porcentagemExecutado = (qtExecutados.doubleValue() / totalExecutadosEnaoExecutados) *100;

        Double porcentagemNaoExecutado  = (qtNaoExecutados.doubleValue()/totalExecutadosEnaoExecutados) * 100;



        graficoPizza.setTitle("Porcentagem de Execução de Nós");
        ObservableList<PieChart.Data> datas = FXCollections.observableArrayList(
                new PieChart.Data("Executado", porcentagemExecutado.intValue()),
                new PieChart.Data("Não Executado", porcentagemNaoExecutado.intValue()));

        datas.forEach(data ->
                data.nameProperty().bind(
                        Bindings.concat(
                                data.getName(), " ", data.pieValueProperty(), "%"
                        )
                )
        );

        graficoPizza.setData(datas);
        graficoPizza.setLabelsVisible(true);
        graficoPizza.setLegendVisible(true);

        graficoPizza.setVisible(true);
        ObservableList<String> chavesNo = FXCollections.observableArrayList();

        chavesNo.addAll(nosChave);

        //Monta o Barchart
        barChartFx.setTitle("Quantidade de vezes que o nó foi utilizado");
        xAxis.setCategories(chavesNo);

        XYChart.Series<Integer, Integer> series = new XYChart.Series<>();
        for (Map.Entry<Integer, Integer> entry : nosQtExecutado.entrySet())
        {
            series.getData().add(new XYChart.Data(entry.getKey().toString(), entry.getValue()));

        }
        barChartFx.getData().add(series);


        // barChartFx.getData().addAll(datas);


    }

   /* public void gerarGrafoBtn(){
        try {
            Main.generate(classeTxtField.getText(), metodoTxtField.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

}
