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
 * Classe principal que gerencia a Árvore Binária de Busca.
 * Agora, esta classe estende 'Application' para gerenciar corretamente
 * o ciclo de vida da UI do JavaFX.
 */
public class ArvoreBinaria extends Application {

    // --- Instâncias da UI e da Árvore ---
    private final BinarySearchTree bst = new BinarySearchTree();
    private Stage visualizerStage; // A única janela para a visualização
    private Canvas canvas;         // O único canvas que será redesenhado

    // --- Lógica Principal da Aplicação ---
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Ponto de entrada do JavaFX. Chamado na Thread de UI.
     * Prepara a janela e inicia a thread do console.
     */
    @Override
    public void start(Stage primaryStage) {
        // Ignoramos o primaryStage, pois vamos gerenciar nossa própria janela.
        primaryStage.close();

        // Configura nossa janela persistente (visualizerStage)
        setupVisualizerStage();

        // Inicia o loop do console em uma thread separada para não travar a UI
        Thread consoleThread = new Thread(this::runConsoleLoop);
        consoleThread.setDaemon(true);
        consoleThread.start();
    }

    /**
     * Configura a janela (Stage) que será usada para exibir a árvore.
     * Isso é chamado apenas uma vez.
     */
    private void setupVisualizerStage() {
        visualizerStage = new Stage();
        visualizerStage.setTitle("Visualizador de Árvore Binária de Busca");

        // O canvas começa com um tamanho padrão
        canvas = new Canvas(600, 400);

        Group rootGroup = new Group(canvas);
        Scene scene = new Scene(rootGroup, Color.LIGHTCYAN);
        visualizerStage.setScene(scene);

        // Adicionamos um evento para fechar o programa quando a janela for fechada
        visualizerStage.setOnCloseRequest(e -> {
            System.out.println("Janela fechada. Saindo do programa...");
            Platform.exit();
            System.exit(0);
        });
    }

    /**
     * Contém o loop principal que interage com o usuário no console.
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
                        System.out.print("Digite o valor para INSERIR: ");
                        bst.insert(scanner.nextInt());
                        visualizeTree();
                        break;
                    case 2:
                        System.out.print("Digite o valor para BUSCAR: ");
                        int valueToSearch = scanner.nextInt();
                        if (bst.search(valueToSearch)) {
                            System.out.println("RESULTADO: O valor " + valueToSearch + " foi ENCONTRADO.");
                        } else {
                            System.out.println("RESULTADO: O valor " + valueToSearch + " NÃO foi encontrado.");
                        }
                        break;
                    case 3:
                        System.out.print("Digite o valor para REMOVER: ");
                        bst.remove(scanner.nextInt());
                        visualizeTree();
                        break;
                    case 4:
                        System.out.println("Saindo do programa...");
                        Platform.exit();
                        System.exit(0);
                        return; // Sai do loop e da thread
                    default:
                        System.out.println("Opção inválida.");
                }
            } catch (Exception e) {
                System.out.println("Entrada inválida. Por favor, insira um número inteiro.");
                scanner.next(); // Limpa o buffer
            }
        }
    }

    /**
     * Agenda a ATUALIZAÇÃO da janela da árvore na thread correta do JavaFX.
     */
    private void visualizeTree() {
        Platform.runLater(() -> {
            Node root = bst.getRoot();
            if (root == null) {
                visualizerStage.hide(); // Se a árvore ficar vazia, esconde a janela
                System.out.println("A árvore está vazia. A janela foi fechada.");
                return;
            }

            // Calcula dinamicamente o novo tamanho necessário para a janela e o canvas
            int height = bst.getHeight();
            double newWidth = Math.max(600, (int) Math.pow(2, height - 1) * 100);
            double newHeight = Math.max(400, height * 120);

            // Redimensiona o canvas e a janela
            canvas.setWidth(newWidth);
            canvas.setHeight(newHeight);

            // Redesenha a árvore no canvas existente
            drawTreeOnCanvas(canvas, root);

            // Se a janela não estiver visível, mostra e centraliza
            if (!visualizerStage.isShowing()) {
                visualizerStage.show();
                visualizerStage.centerOnScreen();
            }
            // Traz a janela para a frente, caso esteja atrás de outras
            visualizerStage.toFront();
        });
    }

    // --- CLASSE DA ÁRVORE (sem alterações) ---
    static class BinarySearchTree {
        private Node root;

        public void insert(int data) { root = insertRec(root, data); }
        private Node insertRec(Node current, int data) {
            if (current == null) { System.out.println("Valor " + data + " inserido."); return new Node(data); }
            if (data < current.data) { current.left = insertRec(current.left, data); }
            else if (data > current.data) { current.right = insertRec(current.right, data); }
            else { System.out.println("Valor " + data + " já existe."); }
            return current;
        }

        public boolean search(int data) { return searchRec(root, data); }
        private boolean searchRec(Node current, int data) {
            if (current == null) return false;
            if (data == current.data) return true;
            return data < current.data ? searchRec(current.left, data) : searchRec(current.right, data);
        }

        public void remove(int data) { root = removeRec(root, data); }
        private Node removeRec(Node current, int data) {
            if (current == null) { System.out.println("Valor " + data + " não encontrado."); return null; }
            if (data == current.data) {
                if (current.left == null && current.right == null) return null;
                if (current.right == null) return current.left;
                if (current.left == null) return current.right;
                int smallestValue = findSmallestValue(current.right);
                current.data = smallestValue;
                current.right = removeRec(current.right, smallestValue);
                return current;
            }
            if (data < current.data) current.left = removeRec(current.left, data);
            else current.right = removeRec(current.right, data);
            return current;
        }

        private int findSmallestValue(Node root) { return root.left == null ? root.data : findSmallestValue(root.left); }
        public int getHeight() { return getHeight(root); }
        private int getHeight(Node node) {
            if (node == null) return 0;
            return 1 + Math.max(getHeight(node.left), getHeight(node.right));
        }

        public Node getRoot() {
            return this.root;
        }
    }

    // --- CLASSES DE DESENHO (sem alterações) ---
    static class Node {
        int data; Node left; Node right;
        public Node(int data) { this.data = data; }
    }

    private void drawTreeOnCanvas(Canvas canvas, Node root) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawNode(gc, root, canvas.getWidth() / 2, 50, canvas.getWidth() / 4);
    }

    private void drawNode(GraphicsContext gc, Node node, double x, double y, double xOffset) {
        if (node == null) return;
        gc.setFill(Color.SKYBLUE);
        gc.fillOval(x - 20, y - 20, 40, 40);
        gc.setStroke(Color.DARKBLUE);
        gc.strokeOval(x - 20, y - 20, 40, 40);
        gc.setFill(Color.BLACK);
        gc.setFont(new Font("Arial", 16));
        String text = String.valueOf(node.data);
        gc.fillText(text, x - (text.length() * 4.5), y + 5);
        double childY = y + 100;
        if (node.left != null) {
            double childX = x - xOffset;
            gc.setStroke(Color.DARKGRAY);
            gc.strokeLine(x, y + 20, childX, childY - 20);
            drawNode(gc, node.left, childX, childY, xOffset / 2);
        }
        if (node.right != null) {
            double childX = x + xOffset;
            gc.setStroke(Color.DARKGRAY);
            gc.strokeLine(x, y + 20, childX, childY - 20);
            drawNode(gc, node.right, childX, childY, xOffset / 2);
        }
    }
}