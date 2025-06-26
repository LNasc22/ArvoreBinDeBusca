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

public class ArvoreBinaria extends Application {

    private final BinarySearchTree bst = new BinarySearchTree();
    private Stage visualizerStage;
    private Canvas canvas;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.close();
        setupVisualizerStage();

        Thread consoleThread = new Thread(this::runConsoleLoop);
        consoleThread.setDaemon(true);
        consoleThread.start();
    }

    private void setupVisualizerStage() {
        visualizerStage = new Stage();
        visualizerStage.setTitle("Visualizador de Árvore Binária de Busca");

        canvas = new Canvas(800, 600);

        Group rootGroup = new Group(canvas);
        Scene scene = new Scene(rootGroup, Color.LIGHTCYAN);
        visualizerStage.setScene(scene);

        visualizerStage.setOnCloseRequest(_ -> {
            System.out.println("Janela fechada. Saindo do programa...");
            Platform.exit();
            System.exit(0);
        });
    }

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
                scanner.next();
            }
        }
    }

    private void visualizeTree() {
        Platform.runLater(() -> {
            Node root = bst.getRoot();
            if (root == null) {
                visualizerStage.hide();
                System.out.println("A árvore está vazia. A janela foi fechada.");
                return;
            }

            int height = bst.getHeight();
            double newWidth = Math.max(800, Math.pow(2, height -1) * 60);
            double newHeight = Math.max(600, height * 120);

            canvas.setWidth(newWidth);
            canvas.setHeight(newHeight);
            visualizerStage.setWidth(newWidth);
            visualizerStage.setHeight(newHeight);

            drawTreeOnCanvas(canvas, root);

            if (!visualizerStage.isShowing()) {
                visualizerStage.show();
            }
            visualizerStage.centerOnScreen();
            visualizerStage.toFront();
        });
    }

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

        public int search(int data) { return searchRec(root, data, 0); }
        private int searchRec(Node current, int data, int depth) {
            if (current == null) return -1;
            if (data == current.data) return depth;
            if (data < current.data) {
                return searchRec(current.left, data, depth + 1);
            } else {
                return searchRec(current.right, data, depth + 1);
            }
        }

        public void remove(int data) { root = removeRec(root, data); }
        private Node removeRec(Node current, int data) {
            if (current == null) { System.out.println("Valor " + data + " não encontrado."); return null; }
            if (data < current.data) {
                current.left = removeRec(current.left, data);
                return current;
            } else if (data > current.data) {
                current.right = removeRec(current.right, data);
                return current;
            } else {
                if (current.left == null && current.right == null) {
                    return null;
                }
                if (current.right == null) {
                    return current.left;
                }
                if (current.left == null) {
                    return current.right;
                }

                int smallestValue = findSmallestValue(current.right);
                current.data = smallestValue;
                current.right = removeRec(current.right, smallestValue);

                return current;
            }
        }

        private int findSmallestValue(Node root) {
            return root.left == null ? root.data : findSmallestValue(root.left);
        }

        public int getHeight() { return getHeight(root); }
        private int getHeight(Node node) {
            if (node == null) return 0;
            return 1 + Math.max(getHeight(node.left), getHeight(node.right));
        }

        public Node getRoot() {
            return this.root;
        }
    }

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