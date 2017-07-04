package view;

/**
 * Criado por Gabriel de Paula em 30/06/2017.
 */
public class Mapeamento {

    private Integer colunaInicio;

    private  Integer colunaFim;

    private Integer linhaInicio;

    private Integer linhaFim;

    public Integer getColunaInicio() {
        return colunaInicio;
    }

    public void setColunaInicio(Integer colunaInicio) {
        this.colunaInicio = colunaInicio;
    }

    public Integer getColunaFim() {
        return colunaFim;
    }

    public void setColunaFim(Integer colunaFim) {
        this.colunaFim = colunaFim;
    }

    public Integer getLinhaInicio() {
        return linhaInicio;
    }

    public void setLinhaInicio(Integer linhaInicio) {
        this.linhaInicio = linhaInicio;
    }

    public Integer getLinhaFim() {
        return linhaFim;
    }

    public void setLinhaFim(Integer linhaFim) {
        this.linhaFim = linhaFim;
    }

    @Override
    public String toString() {
        return "Mapeamento{" +
                "colunaInicio=" + colunaInicio +
                ", colunaFim=" + colunaFim +
                ", linhaInicio=" + linhaInicio +
                ", linhaFim=" + linhaFim +
                '}';
    }
}
