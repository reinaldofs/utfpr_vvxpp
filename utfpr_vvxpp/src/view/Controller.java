package view;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.URL;
import java.util.*;

public class Controller implements Initializable{

    private final String infos = "Projeto Desenvolvido Pela 1ª Turma de Engenharia de Software da UTFPR Campus Dois Vizinhos";

    //olamundo eh o nome da classe start o metod depois o caminho
    private String URL_NODE = "http://localhost:8888/NOME_CLASSE/NOME_METODO//home/user/Downloads/";

    private List<String> nosDoPrograma;

    @FXML
    private PieChart graficoPizza;

    @FXML
    private BarChart<Integer, Integer> barChartFx;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private Label lblQtNosExecut;

    @FXML
    private TextField classeTxtField;

    @FXML
    private TextField metodoTxtField;

    @FXML
    private WebView webViewGrafo;

    @FXML
    private TextArea txtArquivoX;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            if(Main.getCaminhoArquivo() != null) {
                buscar(Main.getCaminhoArquivo());
            }
        } catch (FileNotFoundException e) {
            System.err.println(e + e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void buscar(String caminho) throws IOException {


        //Arquivo X++
        File primeiroArquivo = new File(caminho);
        BufferedReader bufferedReaderXMaisMais = new BufferedReader(new FileReader(primeiroArquivo));
        String arquivox = bufferedReaderXMaisMais.readLine();
        StringBuffer arquivoXMaisMaisStringBuffer = new StringBuffer();
        while (arquivox != null){
            arquivoXMaisMaisStringBuffer.append(arquivox).append("\n");
            arquivox = bufferedReaderXMaisMais.readLine();
        }

        //Arquivo arvore.txt
        BufferedReader bufferedReaderArvore = new BufferedReader(new FileReader(new File(caminho + ".arvore.txt")));
        nosDoPrograma = new ArrayList<>();
        String noAtual = bufferedReaderArvore.readLine();
        nosDoPrograma.add(noAtual);
        while (noAtual != null){
            noAtual = bufferedReaderArvore.readLine();
            nosDoPrograma.add(noAtual);
        }

        //Arquivo saida.txt
        String caminhoSaida = caminho.replace(primeiroArquivo.getName(), "saida.txt");
        BufferedReader bufferedReaderSaida = new BufferedReader(new FileReader(new File(caminhoSaida)));
        StringBuffer arquivoSaidaBuffer = new StringBuffer();
        String arqSaida = bufferedReaderSaida.readLine();
        while (arqSaida != null){
            arquivoSaidaBuffer.append(arqSaida);
            arqSaida = bufferedReaderSaida.readLine();
        }


        processaTudo(arquivoXMaisMaisStringBuffer.toString(), nosDoPrograma, arquivoSaidaBuffer.toString());

    }

    public void buscar() throws IOException {

        FileChooser fileChooser = new FileChooser();

        //Arquivo X++
        File primeiroArquivo = fileChooser.showOpenDialog(null);
        BufferedReader bufferedReaderXMaisMais = new BufferedReader(new FileReader(primeiroArquivo));
        String arquivox = bufferedReaderXMaisMais.readLine();
        StringBuffer arquivoXMaisMaisStringBuffer = new StringBuffer();
        while (arquivox != null){
            arquivoXMaisMaisStringBuffer.append(arquivox).append("\n");
            arquivox = bufferedReaderXMaisMais.readLine();
        }

        //Arquivo arvore.txt
        BufferedReader bufferedReaderArvore = new BufferedReader(new FileReader(new File(primeiroArquivo.getAbsolutePath() + ".arvore.txt")));
        nosDoPrograma = new ArrayList<>();
        String noAtual = bufferedReaderArvore.readLine();
        nosDoPrograma.add(noAtual);
        while (noAtual != null){
            noAtual = bufferedReaderArvore.readLine();
            nosDoPrograma.add(noAtual);
        }

        //Arquivo saida.txt
        String caminho = primeiroArquivo.getAbsolutePath();
        String caminhoSaida = caminho.replace(primeiroArquivo.getName(), "saida.txt");
        BufferedReader bufferedReaderSaida = new BufferedReader(new FileReader(new File(caminhoSaida)));
        StringBuffer arquivoSaidaBuffer = new StringBuffer();
        String arqSaida = bufferedReaderSaida.readLine();
        while (arqSaida != null){
            arquivoSaidaBuffer.append(arqSaida);
            arqSaida = bufferedReaderSaida.readLine();
        }


        processaTudo(arquivoXMaisMaisStringBuffer.toString(), nosDoPrograma, arquivoSaidaBuffer.toString());
    }

    public void processaTudo(String xMaisMaisTxt,  List<String> nosDoPrograma, String arquivoSaidaTxt){

        emprimeTextoX(xMaisMaisTxt);
        List<Mapeamento> mapeamento = processaMapeamentoSaida(arquivoSaidaTxt);
        criaGraficoPizza(mapeamento, nosDoPrograma);

    }

    public void emprimeTextoX(String s) {
        txtArquivoX.clear();
        txtArquivoX.setText(s);
    }

    public List<Mapeamento> processaMapeamentoSaida(String a) {

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

    public void criaGraficoPizza(List<Mapeamento> maps, List<String> nos) {
    	graficoPizza.getData().clear();
    	barChartFx.getData().clear();

        Integer totalExecutado = 0;


       // stringBufferXPluPlus.trimToSize();

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

    public void gerarGrafoBtn(){
        webViewGrafo.getEngine().load("http://www.google.com.br");
    }

    public void infosBtn(){
        new Alert(Alert.AlertType.INFORMATION, infos).show();
    }


}
