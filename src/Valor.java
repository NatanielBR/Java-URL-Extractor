import javax.swing.*;
import java.util.function.Consumer;

/**
 * Classe responsavel por armazenar o valor, e colocar ação pra cada tipo de valor alem de colocar uma ação no ato da
 * mudança
 * @param <T>
 */
public class Valor<T> {
    private String texto;
    private Consumer<T> acao;
    private Consumer<JTextField> acaoInicial;

    /**
     * Construtor responsavel somente para a tarefa normal, armazenar o texto e colocar ação pra esse texto
     * @param texto
     * @param acao
     */
    public Valor(String texto, Consumer<T> acao) {
        this.texto = texto;
        this.acao = acao;
    }

    /**
     * Construtor responsavel por armazenar o texto, a acao e a acao inicial
     * @param texto
     * @param acao
     * @param ini
     */
    public Valor(String texto, Consumer<T> acao, Consumer<JTextField> ini) {
        this.texto = texto;
        this.acao = acao;
        this.acaoInicial = ini;
    }

    /**
     * Metodo para obter a acao
     * @return
     */
    public Consumer<JTextField> getAcaoInicial() {
        return acaoInicial;
    }

    /**
     * metodo para obter o texto
     * @return
     */
    public String getTexto() {
        return texto;
    }

    /**
     * Metodo para obter acao
     * @return
     */
    public Consumer<T> getAcao() {
        return acao;
    }

    /**
     * ToString modificado
     * @return
     */
    @Override
    public String toString() {
        return texto;
    }
}
