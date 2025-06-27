import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import java.util.Scanner;

/**
 * Aplicação de uma Árvore Binária de Busca com interface gráfica em JavaFX.
 * O programa permite inserir, buscar e remover nós, atualizando a visualização
 * a cada operação.
 */
public class ArvoreBinaria extends Application {

    // Instância da estrutura de dados principal.
    private final BinarySearchTree bst = new BinarySearchTree();
    // Variáveis para gerenciar a janela (Stage) e a área de desenho (Canvas).
    private Stage visualizerStage;
    private Canvas canvas;

    /**
     * O método main serve apenas para lançar a aplicação JavaFX.
     * O JavaFX cuidará de criar a thread de UI e chamar o método start().
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Ponto de entrada principal da aplicação JavaFX.
     * É aqui que a UI é inicializada e a thread do console também.
     */
    @Override
    public void start(Stage primaryStage) {
        // Ignora o 'primaryStage' inicial, pois gerencio minha própria janela.
        primaryStage.close();
        // Configura a janela que será usada durante toda a execução.
        setupVisualizerStage();

        // Cria e inicia uma nova thread para o loop do console,
        // para não travar a thread da interface gráfica.
        Thread consoleThread = new Thread(this::runConsoleLoop);
        consoleThread.setDaemon(true); // Garante que a thread feche junto com o programa.
        consoleThread.start();
    }

    /**
     * Prepara a janela (Stage) e a área de desenho (Canvas) uma única vez.
     */
    private void setupVisualizerStage() {
        visualizerStage = new Stage();
        visualizerStage.setTitle("Visualizador de Árvore Binária de Busca");

        canvas = new Canvas(800, 600); // Tamanho inicial

        Group rootGroup = new Group(canvas);
        Scene scene = new Scene(rootGroup, Color.LIGHTSEAGREEN);
        visualizerStage.setScene(scene);

        // Define a ação de fechar o programa ao fechar a janela.
        visualizerStage.setOnCloseRequest(_ -> {
            System.out.println("Janela fechada. Saindo do programa...");
            Platform.exit();
            System.exit(0);
        });
    }

    /**
     * Contém o loop infinito que mostra o menu e processa a entrada do usuário no console.
     */
    private void runConsoleLoop() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n----- MENU ÁRVORE BINÁRIA DE BUSCA -----");
            System.out.println("1. Inserir");
            System.out.println("2. Buscar");
            System.out.println("3. Remover");
            System.out.println("4. Sair");
            System.out.print("Escolha uma opção: ");

            try {
                int choice = scanner.nextInt();
                switch (choice) {
                    case 1:
                        System.out.print("Digite um valor inteiro para inserir: ");
                        bst.insert(scanner.nextInt());
                        visualizeTree();
                        break;
                    case 2:
                        System.out.print("Digite um valor inteiro para buscar: ");
                        int valueToSearch = scanner.nextInt();
                        int depth = bst.search(valueToSearch);

                        if (depth != -1) {
                            System.out.println("RESULTADO: O valor " + valueToSearch + " foi encontrado na profundidade " + depth + ".");
                        } else {
                            System.out.println("RESULTADO: O valor " + valueToSearch + " não foi encontrado.");
                        }
                        break;
                    case 3:
                        System.out.print("Digite um valor inteiro para remover: ");
                        bst.remove(scanner.nextInt());
                        visualizeTree();
                        break;
                    case 4:
                        System.out.println("Saindo do programa...");
                        Platform.exit();
                        System.exit(0);
                        return;
                    default:
                        System.out.println("Opção inválida.");
                }
            } catch (Exception e) {
                System.out.println("Entrada inválida. Por favor, insira um número inteiro.");
                scanner.next(); // Limpa o buffer do scanner em caso de erro.
            }
        }
    }

    /**
     * Agenda a atualização da interface gráfica na thread correta do JavaFX.
     */
    private void visualizeTree() {
        // Platform.runLater é essencial para garantir que qualquer modificação na UI
        // aconteça na JavaFX Application Thread, evitando erros.
        Platform.runLater(() -> {
            Node root = bst.getRoot();
            if (root == null) {
                visualizerStage.hide();
                System.out.println("A árvore está vazia. A janela foi fechada.");
                return;
            }

            // Calcula dinamicamente a largura e altura necessárias para a árvore.
            int height = bst.getHeight();
            double newWidth = Math.max(800, Math.pow(2, height -1) * 60);
            double newHeight = Math.max(600, height * 120);

            // Aplica os novos tamanhos ao canvas e à janela.
            canvas.setWidth(newWidth);
            canvas.setHeight(newHeight);
            visualizerStage.setWidth(newWidth);
            visualizerStage.setHeight(newHeight);

            // Chama a função para redesenhar a árvore.
            drawTreeOnCanvas(canvas, root);

            // Garante que a janela esteja visível e na frente.
            if (!visualizerStage.isShowing()) {
                visualizerStage.show();
            }
            visualizerStage.centerOnScreen();
            visualizerStage.toFront();
        });
    }

    // --- CLASSE DA ÁRVORE BINÁRIA DE BUSCA ---
    // Contém toda a lógica de manipulação da estrutura de dados.
    static class BinarySearchTree {
        private Node root; // A raiz da árvore.

        // --- INSERÇÃO ---
        public void insert(int data) { root = insertRec(root, data); }
        private Node insertRec(Node current, int data) {
            // Caso base: se o nó atual é nulo, encontramos o local de inserção.
            if (current == null) {
                System.out.println("Valor " + data + " inserido.");
                return new Node(data); // Cria e retorna o novo nó.
            }
            // Passo recursivo: compara o valor com o nó atual.
            if (data < current.data) {
                current.left = insertRec(current.left, data);
            } else if (data > current.data) {
                current.right = insertRec(current.right, data);
            }
            else {
                System.out.println("Valor " + data + " já existe.");
            }
            return current; // Retorna o nó (potencialmente modificado).
        }

        // --- BUSCA ---
        public int search(int data) { return searchRec(root, data, 0); }
        private int searchRec(Node current, int data, int depth) {
            // Caso base 1: se o nó é nulo, o valor não está na árvore.
            if (current == null) return -1;
            // Caso base 2: valor encontrado. Retornamos a profundidade atual.
            if (data == current.data) return depth;
            // Passo recursivo: desce para a esquerda ou direita, incrementando a profundidade
            if (data < current.data) {
                return searchRec(current.left, data, depth + 1);
            } else {
                return searchRec(current.right, data, depth + 1);
            }
        }

        // --- REMOÇÃO ---
        public void remove(int data) { root = removeRec(root, data); }
        private Node removeRec(Node current, int data) {
            if (current == null) { System.out.println("Valor " + data + " não encontrado."); return null; }
            // Navega até encontrar o nó.
            if (data < current.data) {
                current.left = removeRec(current.left, data);
                return current;
            } else if (data > current.data) {
                current.right = removeRec(current.right, data);
                return current;
            } else {
                // Caso 1: Nó folha (sem filhos).
                if (current.left == null && current.right == null) {
                    return null;
                }
                // Caso 2: Nó com um único filho.
                if (current.right == null) {
                    return current.left;
                }
                if (current.left == null) {
                    return current.right;
                }

                // Caso 3: Nó com dois filhos.
                // Substituímos o valor do nó pelo seu sucessor.
                // Sucessor: o menor valor na sub-árvore direita.
                int smallestValue = findSmallestValue(current.right);
                // Copia o valor do sucessor para o nó atual.
                current.data = smallestValue;
                // Remove o nó sucessor (que agora está duplicado) da sub-árvore direita.
                current.right = removeRec(current.right, smallestValue);

                return current;
            }
        }

        // Encontra o menor valor em uma sub-árvore (sempre indo para a esquerda).
        private int findSmallestValue(Node root) {
            return root.left == null ? root.data : findSmallestValue(root.left);
        }

        // Calcula a altura da árvore para o redimensionamento da janela.
        public int getHeight() { return getHeight(root); }
        private int getHeight(Node node) {
            if (node == null) return 0;
            return 1 + Math.max(getHeight(node.left), getHeight(node.right));
        }

        public Node getRoot() {
            return this.root;
        }
    }

    // --- CLASSES DE ESTRUTURA E DESENHO ---
    // Representa um único nó na árvore.
    static class Node {
        int data; Node left; Node right;
        public Node(int data) { this.data = data; }
    }

    // Desenha a árvore inteira no canvas.
    private void drawTreeOnCanvas(Canvas canvas, Node root) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawNode(gc, root, canvas.getWidth() / 2, 50, canvas.getWidth() / 4);
    }

    // Desenha recursivamente um único nó e suas conexões.
    private void drawNode(GraphicsContext gc, Node node, double x, double y, double xOffset) {
        if (node == null) return;
        gc.setFill(Color.LIGHTGREEN);
        gc.fillOval(x - 20, y - 20, 40, 40);
        gc.setStroke(Color.DARKOLIVEGREEN);
        gc.strokeOval(x - 20, y - 20, 40, 40);
        gc.setFill(Color.BLACK);
        gc.setFont(new Font("Arial", 16));
        String text = String.valueOf(node.data);
        gc.fillText(text, x - (text.length() * 4.5), y + 5);
        double childY = y + 100;
        if (node.left != null) {
            double childX = x - xOffset;
            gc.setStroke(Color.BLACK);
            gc.strokeLine(x, y + 20, childX, childY - 20);
            drawNode(gc, node.left, childX, childY, xOffset / 2);
        }
        if (node.right != null) {
            double childX = x + xOffset;
            gc.setStroke(Color.BLACK);
            gc.strokeLine(x, y + 20, childX, childY - 20);
            drawNode(gc, node.right, childX, childY, xOffset / 2);
        }
    }
}