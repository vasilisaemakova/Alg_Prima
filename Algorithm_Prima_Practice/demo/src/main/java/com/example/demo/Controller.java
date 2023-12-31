package com.example.demo;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Controller extends Observable {

    private Reader reader;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private AnchorPane scene;

    @FXML
    private Button loadBtn;

    @FXML
    private Button saveBtn;

    @FXML
    private Button mvBtn;

    @FXML
    private Button addBtn;

    @FXML
    private Button delBtn;

    @FXML
    private Button solveBtn;

    @FXML
    private Button nextBtn;

    @FXML
    private Button startBtn;

    @FXML
    private AnchorPane field;

    @FXML
    private TextArea logging;

    @FXML
    private GridPane table;

    private AlgorithmView algorithmView;

    @FXML
    private FlowPane tablePane;

    private GraphView graphView = new GraphView();


    @FXML
    public void dialog(String resource) throws IOException {
        reader = new AppReader(logging);
        FXMLLoader fxmlLoader = new FXMLLoader(Controller.class.getResource(resource));
        Scene dialog = new Scene(fxmlLoader.load(), 320, 240);
        Stage stage = new Stage();
        stage.setScene(dialog);
        ((Dialog)fxmlLoader.getController()).setReader(reader);
        ((Dialog)fxmlLoader.getController()).setObserver(obs);
        stage.showAndWait();
    }

    @FXML
    void addMouse(MouseEvent event) throws IOException {
        dialog("dialog.fxml");
        if( !reader.getV1().isEmpty() && !reader.getV2().isEmpty() && reader.getW() != 0 ) {
            graphView.counter_add(reader.getV1(), reader.getV2(), reader.getW(), field);
            if(graphView.getCounter_to_add() > 0)
                addBtn.setText("Готово");
            notify(Level.CLUE, "Add " + graphView.getCounter_to_add() + "vertexes by click");
            algorithmView = null;
            table.setVisible(false);
        }
    }

    private EventHandler <MouseEvent> moveField(Circle circle) {
        EventHandler<MouseEvent> eventHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                if(e.getEventType().equals(MouseEvent.MOUSE_MOVED) && mvBtn.getText().equals("Готово") && circle.getFill().equals(Color.ROSYBROWN)) {
                    circle.setCenterY(e.getSceneY() - field.getLayoutY());
                    circle.setCenterX(e.getSceneX() - field.getLayoutX());
                    graphView.updateEdges(circle);
                    graphView.updateText(circle);
                    circle.setFill(Color.ROSYBROWN);
                }
            }
        };
        return eventHandler;
    }
    boolean canClickForMove = false;
    private EventHandler <MouseEvent> clickCircle(Circle circle) {
        EventHandler<MouseEvent> eventHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                if(mvBtn.getText().equals("Готово") && !circle.getFill().equals(Color.ROSYBROWN)){
                    field.setOnMouseMoved(moveField(circle));
                    circle.setFill(Color.ROSYBROWN);
                    canClickForMove = false;
                } else {
                    circle.setFill(Color.DARKSLATEBLUE);
                    field.setOnMouseMoved(null);
                    canClickForMove = true;
                }
            }
        };
        return eventHandler;
    }
    @FXML
    void addCircle(double x, double y) {
        Circle circle = new Circle();
        circle.setCenterX(x);
        circle.setCenterY(y);
        circle.setOnMouseClicked(clickCircle(circle));
        circle.setRadius(15);
        field.getChildren().add(circle);
        graphView.addCircle(circle, field);
    }

    @FXML
    void clickScene(MouseEvent event) {
        if(addBtn.getText().equals("Готово") && graphView.getCounter_to_add() > 0) {
            double x = event.getSceneX() - field.getLayoutX();
            double y = event.getSceneY() - field.getLayoutY();
            //if()
            String name = reader.getV1();
            addCircle(x, y);
            if(graphView.getCounter_to_add() == 0)
                addBtn.setText("Добавить");
        } else if(addBtn.getText().equals("Готово")){
            addBtn.setText("Добавить");
        } else if(mvBtn.getText().equals("Готово") && canClickForMove){
            for(Node node : field.getChildren()) {
                if(node instanceof Circle && ((Circle)node).getFill().equals(Color.ROSYBROWN) ) {
                    ((Circle) node).setFill(Color.DARKSLATEBLUE);
                    field.setOnMouseMoved(null);
                }
            }
        } else {
            canClickForMove = true;
        }
    }

    @FXML
    void load(MouseEvent event) throws IOException {
        Stage primaryStage = new Stage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл");
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("TXT", "*.txt");//Расширение
        fileChooser.getExtensionFilters().add(extFilter);
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if(selectedFile != null) {
            reader = new FileReader(selectedFile);
            graphView = new GraphView();
            algorithmView = null;
            table.setVisible(false);
            graphView.setObserver(obs);
            field.getChildren().clear();
            logging.clear();
            notify(Level.INPUT, "Load file");
            addEdge();
        }
    }
    void addEdge() {
        GenerateCoords gen = new GenerateCoords(field.getHeight()/2, field.getWidth()/2, field.getHeight()/2.5, reader.getN());
        int count = 0;
        for(int i = 0; i < reader.getN(); i++) {
            reader.read();
            if (!reader.getV1().isEmpty() && !reader.getV2().isEmpty() && reader.getW() != 0) {
                notify(Level.INPUT, "add " + reader.getV1() + " " + reader.getV2() + reader.getW());
                graphView.counter_add(reader.getV1(), reader.getV2(), reader.getW(), field);
                notify(Level.INPUT, "Will be added " + graphView.getCounter_to_add() + " vertexes");
                while ( graphView.getCounter_to_add() > 0) {
                    addCircle(gen.getX(count), gen.getY(count));
                    count++;
                }
            }
        }
    }

    @FXML
    void moveVertex(MouseEvent event) {
        notify(Level.CLUE, "Select vertex for move, click for end moving");
        mvBtn.setText(mvBtn.getText().equals("Готово") ? "Двигать" : "Готово");
    }

    @FXML
    void nextStep(MouseEvent event) {
        if(algorithmView != null) {
            algorithmView.next();
            algorithmView.info();
            if(algorithmView.isResult()) {
                nextBtn.setText("Начать с 0");
                result(event);
            } else {
                graphView.defaultColor();
                notify(Level.ALGORITHM, "Включенные в МОД вершины\n" + algorithmView.nextStepVertex());
                graphView.colorVertexes(algorithmView.nextStepVertex(), Color.BLUE);
                graphView.colorResult(algorithmView.nextStepIncluded(), Color.BLUE);
                graphView.colorResult(algorithmView.nextStepCandidate(), Color.LIGHTGREEN);
                graphView.colorResult(algorithmView.nextGrayEdges(), Color.LIGHTGRAY);
                nextBtn.setText("Следующий шаг");
            }
        } else {
            notify(Level.ERROR, "Please, click start");
        }
    }

    @FXML
    void remove(MouseEvent event) throws IOException {
        dialog("remover.fxml");
        String v1 = reader.getV1(), v2 = reader.getV2();
        if(!v1.equals(v2))
            graphView.removeEdge(v1, v2, field);
        else
            graphView.removeVertex(v1, field);
        algorithmView = null;
        table.setVisible(false);
    }

    @FXML
    void result(MouseEvent event) {
        if(algorithmView == null) {
            notify(Level.ERROR, "Please, click start");
            return;
        }
        graphView.defaultColor();
        graphView.colorResult(algorithmView.getResult(), Color.BLUE);
        graphView.colorVertexes(algorithmView.getGraph(), Color.BLUE);
        graphView.colorResult(algorithmView.nextGrayEdges(), Color.LIGHTGRAY);
        notify(Level.ALGORITHM,"\nРезультт работы алгоритма.\nМножество включенных в МОД рбер :\n" + algorithmView.getResult());
    }

    @FXML
    void save(MouseEvent event) throws IOException {
        Stage primaryStage = new Stage();
        FileChooser fileChooser = new FileChooser();
        //fileChooser.setMode(FileChooserMode.save);
        fileChooser.setTitle("Save Document");//Заголовок диалога
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("TXT", "*.txt");//Расширение
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showSaveDialog(primaryStage);
        try {
            FileWriter fw = new FileWriter(file);
            fw.write(graphView.toString());
            fw.close();
        } catch (Exception e) {

        }
    }


    @FXML
    void start(MouseEvent event) throws IOException {
        Graph graph = graphView.initGraph();
        if(graph.countComponent()) {
            dialog("start.fxml");
            notify(Level.ALGORITHM, "Start vertex chosen " + reader.getV1());
            Prim prim = new Prim((graphView.initGraph()).getGraph());
            algorithmView = new AlgorithmView(prim);
            table.setVisible(true);
            algorithmView.setObserver(obs);
            algorithmView.start(reader.getV1());
            graphView.defaultColor();
            graphView.colorVertexes(algorithmView.nextStepVertex(), Color.BLUE);
            graphView.colorResult(algorithmView.nextStepIncluded(), Color.BLUE);
            graphView.colorResult(algorithmView.nextStepCandidate(), Color.LIGHTGREEN);
            graphView.colorResult(algorithmView.nextGrayEdges(), Color.LIGHTGRAY);
            notify(Level.ALGORITHM, "Старт работы алгоритма.\n Первая вершина " + algorithmView.nextStepVertex());
            //initTable();
        } else {
            notify(Level.ERROR, "Bad graph");
        }
    }
    void initTable() {
        //table = new GridPane();
        String [] vertexes = algorithmView.getGraph().split(" ");
        int numColumns = vertexes.length; // Количество столбцов
        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setPercentWidth(90.0 / numColumns); // Распределение на равные столбцы

        for (int i = 0; i < numColumns; i++) {
            table.getColumnConstraints().add(columnConstraints);
        }

        for(int i = 0; i < numColumns; i++) {
            Label label = new Label();
            label.setStyle("-fx-text-fill: black; -fx-font-size: 16;");
            label.setText(vertexes[i]);
            table.add(label,  i, 0);
        }
        table.setGridLinesVisible(true);
        table.setOpacity(1.0);
    }
    @FXML
    void resizeScene(ZoomEvent event) {
        System.out.println("resize");
        logging.setPrefWidth(scene.getWidth() / 5);
        logging.setPrefHeight(scene.getHeight() / 3);
    }

    @FXML
    void initialize() {
        assert scene != null : "fx:id=\"scene\" was not injected: check your FXML file 'hello-view.fxml'.";
        assert loadBtn != null : "fx:id=\"loadBtn\" was not injected: check your FXML file 'hello-view.fxml'.";
        assert saveBtn != null : "fx:id=\"saveBtn\" was not injected: check your FXML file 'hello-view.fxml'.";
        assert addBtn != null : "fx:id=\"addBtn\" was not injected: check your FXML file 'hello-view.fxml'.";
        assert delBtn != null : "fx:id=\"delBtn\" was not injected: check your FXML file 'hello-view.fxml'.";
        assert solveBtn != null : "fx:id=\"solveBtn\" was not injected: check your FXML file 'hello-view.fxml'.";
        assert nextBtn != null : "fx:id=\"nextBtn\" was not injected: check your FXML file 'hello-view.fxml'.";
        assert startBtn != null : "fx:id=\"startBtn\" was not injected: check your FXML file 'hello-view.fxml'.";
        assert field != null : "fx:id=\"field\" was not injected: check your FXML file 'hello-view.fxml'.";
        assert logging != null : "fx:id=\"logging\" was not injected: check your FXML file 'hello-view.fxml'.";
        assert tablePane != null : "fx:id=\"tablePane\" was not injected: check your FXML file 'hello-view.fxml'.";
        assert table != null : "fx:id=\"table\" was not injected: check your FXML file 'hello-view.fxml'.";
        logging.clear();
        logging.prefWidthProperty().bind(scene.widthProperty().divide(2));
        logging.prefHeightProperty().bind(scene.heightProperty().divide(3));
        field.prefHeightProperty().bind(scene.heightProperty().divide(3).multiply(2));
        field.prefWidthProperty().bind(scene.widthProperty().divide(2));
        //tablePane.prefHeightProperty().bind(scene.heightProperty().divide(5));
        //tablePane.prefWidthProperty().bind(scene.widthProperty().divide(2.3));
        // Установите ограничения для размеров GridPane
        table.prefWidthProperty().bind(tablePane.widthProperty());
        table.prefHeightProperty().bind(tablePane.heightProperty());

        table.setVisible(false);
        if(logging != null) reader = new AppReader(logging);
        obs = new ObserverTextArea();
        ((ObserverTextArea)obs).setTextArea(logging);
        notify(Level.INPUT, "Sucsess");
        graphView.setObserver(obs);
    }
}
