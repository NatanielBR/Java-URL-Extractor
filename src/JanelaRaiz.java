import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JanelaRaiz {
    private JPanel janela;
    private JTextField textField1;
    private JButton carregarButton;
    private JComboBox<Valor<String>> Tipo;
    private JTextField filtro;
    private JButton filtrar;
    private JTree Valores;
    private JPanel lista;
    private JPanel alteravel;
    private JTextArea Saida;
    private JRadioButton textoPuroRadioButton;
    private JRadioButton valorDoHrefRadioButton;
    private ButtonGroup linkTipo;

    private Document document;
    private DefaultMutableTreeNode raiz = null;
    private int TextoTipo = 1;
    private SwingWorker<Document, Document> urlcatch = null;

    public JanelaRaiz() {
        textField1.addActionListener((a) -> acaoURLEntrada());
        carregarButton.addActionListener((a) -> acaoURLEntrada());
        filtrar.addActionListener((a) -> acaoFiltrar());
        filtro.addActionListener((a) -> acaoFiltrar());
        Enumeration<AbstractButton> aa = linkTipo.getElements();
        for (int i =0; aa.hasMoreElements(); i++){
            final int ii = i;
            aa.nextElement().addActionListener((a) -> acaoRadioBotao(ii));
        }
    }
    public JanelaRaiz(String debug) {
        textField1.addActionListener((a) -> acaoURLEntrada());
        textField1.setText(debug);
        carregarButton.addActionListener((a) -> acaoURLEntrada());
        filtrar.addActionListener((a) -> acaoFiltrar());
        filtro.addActionListener((a) -> acaoFiltrar());
        Enumeration<AbstractButton> aa = linkTipo.getElements();
        for (int i =0; aa.hasMoreElements(); i++){
            final int ii = i;
            aa.nextElement().addActionListener((a) -> acaoRadioBotao(ii));
        }
    }

    /**
     * Acao para o filtro, usado no botao de filtrar e na caixa de texto
     */
    public void acaoFiltrar() {
        String texto = filtro.getText();
        if (Tipo.getSelectedItem() == null) {
            return;
        }
        ((Valor<String>) Tipo.getSelectedItem()).getAcao().accept(texto);
    }

    public void acaoRadioBotao(int seuIndex) {
        TextoTipo = seuIndex;
    }

    /**
     * Açao para carregar a URL
     */
    public void acaoURLEntrada() {
        try {
            document = Jsoup.connect(textField1.getText()).get();
            modificarValores(document.getAllElements());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(filtrar, e, "Exception", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String trabalharTexto(String texto, String raiz, int tipo) {
        String retorno = null;
        switch (tipo) {
            case 0:
                //caso seja "texto puro"
                retorno = texto;
                break;
            case 1:
                //caso seja "atributo href"
                Pattern pattern = Pattern.compile("href=\\\"(.*?)\\\"");
                Matcher matcher = pattern.matcher(texto);
                if (!matcher.find()) break;
                retorno = matcher.group(0);
                retorno = retorno.replace("href=\"", "");
                retorno = retorno.substring(0, retorno.length() - 1);
                if (!retorno.contains("http")){
                    retorno = raiz + retorno;
                }

                break;
        }
        return retorno;
    }

    /**
     * Metodo para modificar a JTree usando elementos do documento
     *
     * @param ele
     */
    private void modificarValores(Elements ele) {
        Map<String, List<Element>> listMap = new HashMap<>();
        ele.forEach((a) -> {
            String tag = a.tagName();
            if (listMap.containsKey(tag)) {
                listMap.get(tag).add(a);
            } else {
                List<Element> elem = new ArrayList<>();
                elem.add(a);
                listMap.put(tag, elem);
            }
        });
        if (raiz == null) {
            raiz = new DefaultMutableTreeNode(textField1.getText());
        } else {
            raiz.removeAllChildren();
        }

        listMap.forEach((a, b) -> {
            DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(a);
            b.forEach((c) -> {
                treeNode.add(new DefaultMutableTreeNode(c.toString()));
            });
            raiz.add(treeNode);
        });
        Valores.setModel(new DefaultTreeModel(raiz));
    }

    /**
     * Metodo para criar o modelo do comboBoxModel
     *
     * @param data
     * @return
     */
    private ComboBoxModel<Valor<String>> criarModelo(Valor<String>[] data) {
        return new ComboBoxModel<Valor<String>>() {
            private Valor<String> valor;

            @Override
            public void setSelectedItem(Object anItem) {
                valor = (Valor<String>) anItem;
                valor.getAcaoInicial().accept(filtro);
            }

            @Override
            public Object getSelectedItem() {
                return valor;
            }

            @Override
            public int getSize() {
                return data.length;
            }

            @Override
            public Valor<String> getElementAt(int index) {
                return data[index];
            }

            @Override
            public void addListDataListener(ListDataListener l) {

            }

            @Override
            public void removeListDataListener(ListDataListener l) {

            }
        };
    }

    /**
     * Metodo main
     *
     * @param args
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("Java URL Extrator - 2.0");
        if (args[0] == null){
            frame.setContentPane(new JanelaRaiz().janela);
        }else{
            frame.setContentPane(new JanelaRaiz(args[0]).janela);
        }

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 320);
        frame.setVisible(true);
    }

    /**
     * Metodo para transformar o Array de String em Array de Valor's
     *
     * @return
     */
    private Valor<String>[] criarArrayDoTipo() {
        List<Valor<String>> lista = new ArrayList<>();
        Consumer<JTextField> limpar = (a) -> a.setText("");
        lista.add(new Valor<>("Tag", (Consumer<String>) s -> {
            modificarValores(s.isEmpty() ? document.getAllElements() : document.getElementsByTag(s));
        }, limpar));
        lista.add(new Valor<>("Atributo", (Consumer<String>) s -> {
            modificarValores(s.isEmpty() ? document.getAllElements() : document.getElementsByAttribute(s));
        }, limpar));
        lista.add(new Valor<>("Atributo + Contains", (Consumer<String>) s -> {
            if (!s.contains("=")) {
                return;
            }
            String[] ss = s.split("=");
            modificarValores(document.getElementsByAttributeValueContaining(ss[0], ss[1]));
        }, acaoLimpar("Use = para separar")));
        return lista.toArray(new Valor[0]);
    }

    /**
     * Metodo para limpar e colocar uma mensagem
     *
     * @param msg a mensagem
     * @return
     */
    private Consumer<JTextField> acaoLimpar(String msg) {
        return (a) -> {
            a.setText("");
            a.setToolTipText(msg);
        };
    }

    /**
     * Metodo para modificar os componentes
     */
    private void createUIComponents() {
        Tipo = new JComboBox();
        Tipo.setModel(criarModelo(criarArrayDoTipo()));
        Valores = new JTree();
        Valores.setModel(null);
        Valores.addTreeSelectionListener(e -> {
            TreePath path = e.getPath();
            if (path == null) return;
            String last = path.getLastPathComponent().toString();
            String root = path.getPathComponent(0).toString();
            String retorno = trabalharTexto(last,root,TextoTipo);
            if (retorno == null) return;
            Saida.append(retorno + System.lineSeparator());

        });
    }
}
