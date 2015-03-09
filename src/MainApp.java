/**
 * An application to schedule games for a competition.
 */

import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MainApp extends Application {
    private static final double MESSAGE_TRANSITION_MS = 1000, MESSAGE_DISPLAY_TIME_MS = 4000;
    private static final int WINDOW_MIN_WIDTH = 850, WINDOW_MIN_HEIGHT = 550;
    private BorderPane root;
    private Stage stage;
    private Scene scene;

    //Instance Variables (UI):
    private VBox teamsVBox, datesVBox;
    private TextField addTeamField;
    private Button addTeamButton, scheduleButton, loadButton, saveScheduleButton;
    private DatePicker datePicker;
    private Label messageLabel;
    private SequentialTransition messageTransition;
    private ChoiceBox<String> dateSelector;
    private GridPane tablePane, fixturesPane;
    private RadioButton byPointsRadio;
    private RadioButton byPercentRadio;

    //Instance Variables (data):
    private ArrayList<String> teams;
    private ArrayList<LocalDate> gameDates;
    private ArrayList<Game> games;

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        root = new BorderPane();
        scene = new Scene(new StackPane(root), WINDOW_MIN_WIDTH, WINDOW_MIN_HEIGHT); //width and height of application
        stage.setScene(scene);
        stage.setTitle("Game Scheduler");  //text for the title bar of the window
        stage.setMinWidth(WINDOW_MIN_WIDTH);
        stage.setMinHeight(WINDOW_MIN_HEIGHT);
        stage.setOnCloseRequest(e -> closeApplication());

        //Initializations (UI):
        teamsVBox = new VBox();
        datesVBox = new VBox();
        addTeamField = new TextField();
        addTeamButton = new Button("Add");
        datePicker = new DatePicker();
        scheduleButton = new Button("Generate\nSchedule");
        loadButton = new Button("Load\nSchedule");
        messageLabel = new Label();
        messageTransition = new SequentialTransition();
        dateSelector = new ChoiceBox<>();
        tablePane = new GridPane();
        fixturesPane = new GridPane();
        saveScheduleButton = new Button("Save");
        byPointsRadio = new RadioButton("By Points");
        byPercentRadio = new RadioButton("By Percent");

        //Initializations (data):
        teams = new ArrayList<>();
        gameDates = new ArrayList<>();

        //Fonts and styles:
        Font.loadFont(MainApp.class.getResource("LuckiestGuy.ttf").toExternalForm(), 10);
        Font.loadFont(MainApp.class.getResource("LilitaOne-Regular.ttf").toExternalForm(), 10);
        teamsVBox.setId("teams");
        datesVBox.setId("dates");
        scene.getStylesheets().add("styles.css");

        datePicker.setEditable(false);
        byPointsRadio.setToggleGroup(new ToggleGroup());
        byPercentRadio.setToggleGroup(byPointsRadio.getToggleGroup());
        byPointsRadio.setSelected(true);

        //Screen layout:
        root.setTop(new VBox(new HBox(new Text("Game Scheduler")), new HBox(messageLabel)));
        root.getTop().setId("top");
        displayDataEntryScreen();

        //Events:
        addTeamButton.setOnAction(e -> addTeam());
        datePicker.setOnAction(e -> dateSelected());
        addTeamField.setOnKeyPressed(e -> {
            if (e.getCode().getName().equals("Enter"))
                addTeam();
        });
        loadButton.setOnAction(e -> loadSavedSchedule());
        scheduleButton.setOnAction(e -> checkTeamsDates());

        stage.show();
    }

    private void closeApplication() {
        stage.close();
        Platform.exit();
        System.exit(0);
    }

    private void loadSavedSchedule() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = fc.showOpenDialog(stage);
        try {
            List<String> content = Files.readAllLines(file.toPath());

        } catch (Exception s) {
            showMessage("Error Reading File");
        }
    }


    private void showMessage(String message) {
        clearMessage();

        messageLabel.setText(message);
        FadeTransition fadeIn = new FadeTransition(new Duration(MESSAGE_TRANSITION_MS), messageLabel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        FadeTransition fadeOut = new FadeTransition(new Duration(MESSAGE_TRANSITION_MS), messageLabel);
        fadeOut.setDelay(new Duration(MESSAGE_DISPLAY_TIME_MS));
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        messageTransition.getChildren().addAll(fadeIn, fadeOut);
        messageTransition.play();
    }

    private void clearMessage() {
        messageTransition.stop();
        messageTransition.getChildren().clear();
        messageLabel.setText("");
    }


    private void displayDataEntryScreen() {
        root.setCenter(new HBox(new VBox(new HBox(new Text("Teams")), new HBox(addTeamField, addTeamButton), new ScrollPane(teamsVBox)), new VBox(new HBox(new Text("Game Dates")), datePicker, new ScrollPane(datesVBox)), new VBox(loadButton, new Label("Standings:"), byPointsRadio, byPercentRadio, scheduleButton)));
        root.getCenter().setId("data");
    }

    private void dateSelected() {
        LocalDate date = datePicker.getValue();

        if (gameDates.contains(date )) {
            showMessage("Date Already Added");
        } else {
            int index = 0;

            while (index < gameDates.size() && date.compareTo(gameDates.get(index)) > 0) {
                index++;
            }

            gameDates.add(index, date);
            String displayDate = date.format(DateTimeFormatter.ofPattern("E, MMM d, u"));

            Button deleteButton = new Button("Delete");
            HBox dateHBox = new HBox(deleteButton, new Text(displayDate));
            deleteButton.setOnAction(e -> deleteDate(date));

            datesVBox.getChildren().add(index, dateHBox);

            showMessage("\"" + date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, u")) + "\" Added");
        }

    }

    private void deleteDate(LocalDate date) {
        int index = gameDates.indexOf(date);
        gameDates.remove(index);
        datesVBox.getChildren().remove(index);
        datePicker.requestFocus();
    }

    private void addTeam() {
        String teamName = addTeamField.getText().trim().toUpperCase();

        StringBuilder clean = new StringBuilder();

        for (int i = 0; i < teamName.length(); i++) {
            char ch = teamName.charAt(i);
            if (Character.isAlphabetic(ch) || Character.isDigit(ch) || Character.isSpaceChar(ch))
                clean.append(ch);
        }

        final String team = clean.toString();

        if (team.length() == 0) {
            showMessage("Invalid Team Name");
        } else if (teams.contains(team)) {
            showMessage("Team Already Added");
        } else {
            int index = 0;

            while (index < teams.size() && team.compareTo(teams.get(index)) > 0) {
                index++;
            }

            teams.add(index, team);
            Button deleteButton = new Button("Delete");
            HBox teamHBox = new HBox(deleteButton, new Text(team));
            teamsVBox.getChildren().add(index, teamHBox);

            deleteButton.setOnAction(e -> deleteTeam(team));

            showMessage("\"" + team + "\" Added");
            addTeamField.setText("");
            addTeamField.requestFocus();
        }
    }

    private void deleteTeam(String team) {
        int index = teams.indexOf(team);
        teams.remove(index);
        teamsVBox.getChildren().remove(index);
        addTeamField.requestFocus();
    }

    private void checkTeamsDates() {
        //check for enough teams and dates
        if (teams.size() < 2) {
            showMessage("At Least Two Teams Needed to Generate Schedule");
        } else if (teams.size() % 2 == 0 && gameDates.size() < teams.size() - 1) {
            showMessage("At Least " + (teams.size() - 1) + " Game Dates are Needed");
        } else if (teams.size() % 2 == 1 && gameDates.size() < teams.size()) {
            showMessage("At Least " + teams.size() + " Game Dates are Needed");
        } else {
            showMessage("Calculating Schedule");
            root.setCenter(new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS));
            Task task = new Task() {
                @Override
                protected Object call() throws Exception {
                    try {
                        generateGames();
                        assignGameDates();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
            task.setOnSucceeded(e -> scheduleDone());

            Thread th = new Thread(task);
            th.setDaemon(true);
            th.start();
        }
    }

    /*
    Method to create an array list of all objects of the Game class.
     */
    private void generateGames() {
        games = new ArrayList<>();

        for (int i = 0; i < teams.size(); i++) {
            for (int j = 0; j < i + 1; j++) {
                if (i != j) {
                    games.add(new Game(teams.get(i), teams.get(j)));
                }
            }
        }

    }

    /*
    Assign dates to each of the games so that no team plays twice on the same date.
    The fewest number fo dates possible should be used.
     */
    private void assignGameDates() {

        for (int i = 0; i < games.size(); i++) {

            games.get(i).setDate(gameDates.get(0));
        }
        
    }


    private void scheduleDone() {
        for (LocalDate date : gameDates) {
            dateSelector.getItems().add(date.format(DateTimeFormatter.ofPattern("E, MMM d, u")));
        }

        displayLeagueTable();

        root.setCenter(new HBox(new VBox(dateSelector, fixturesPane), new VBox(tablePane)));

        dateSelector.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            displayFixtures(gameDates.get(dateSelector.getSelectionModel().getSelectedIndex()));
        });

        dateSelector.getSelectionModel().select(0);
    }

    private void displayFixtures(LocalDate date) {
        fixturesPane.getChildren().clear();
        fixturesPane.setStyle("-fx-padding: 1");
        
        showMessage(date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, u")));

        for (int i = 0; i < games.size(); i++) {
            Game game = games.get(i);

            if (game.isScheduled() && game.getDate().equals(date)) {
                fixturesPane.addRow(i, new HBox(new Label(game.getHomeTeam())), new HBox(new Label(game.isPlayed() ? game.getHomeScore() + " - " + game.getAwayScore() : "vs")), new HBox(new Label(game.getAwayTeam())));
                
                final int gameIndex = i;

                fixturesPane.getChildren().get(fixturesPane.getChildren().size() - 3).setStyle("-fx-max-width: 150");
                fixturesPane.getChildren().get(fixturesPane.getChildren().size() - 3).setStyle("-fx-alignment: center-right");
                fixturesPane.getChildren().get(fixturesPane.getChildren().size() - 2).setStyle("-fx-min-width: 90");
                fixturesPane.getChildren().get(fixturesPane.getChildren().size() - 2).setStyle("-fx-cursor: hand");
                fixturesPane.getChildren().get(fixturesPane.getChildren().size() - 1).setStyle("-fx-max-width: 150");
                fixturesPane.getChildren().get(fixturesPane.getChildren().size() - 1).setStyle("-fx-alignment: center-left");
                fixturesPane.getChildren().get(fixturesPane.getChildren().size() - 2).setOnMouseClicked(e -> inputResult(games.get(gameIndex)));
            }
        }
    }

    private void inputResult(Game game) {
        StackPane stackPane = (StackPane)root.getParent();
        Rectangle r = new Rectangle(scene.getWidth(), scene.getHeight());
        r.setFill(Color.BLACK);
        r.setOpacity(0.5);
        dateSelector.setDisable(true);
        
        GridPane gp = new GridPane();
        TextField homeField = new TextField();
        TextField awayField = new TextField();
        
        gp.addRow(0,new HBox(new Label(game.getHomeTeam())), new HBox(homeField));
        gp.addRow(1,new HBox(new Label(game.getAwayTeam())), new HBox(awayField));
        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");
        HBox saveBox = new HBox(cancelButton, saveButton);
        gp.addRow(2, saveBox);
        gp.setColumnSpan(saveBox, 2);
        
        VBox vBox  =new VBox(gp);
        vBox.setAlignment(Pos.CENTER);

        homeField.setOnKeyPressed(e -> {
            if (e.getCode().getName().equals("Enter")) {

                int s = 0;

                try {
                    Integer.parseInt(homeField.getText());
                } catch (NumberFormatException e1) {
                    homeField.setText("0");
                }

                awayField.requestFocus();
            } else if (e.getCode().getName().equals("Esc")) {
                stackPane.getChildren().remove(stackPane.getChildren().size()-1);
                stackPane.getChildren().remove(stackPane.getChildren().size()-1);
                dateSelector.setDisable(false);
            }
        });

        awayField.setOnKeyPressed(e -> {
            if (e.getCode().getName().equals("Enter")) {

                int s = 0;

                try {
                    Integer.parseInt(awayField.getText());
                } catch (NumberFormatException e1) {
                    awayField.setText("0");
                }

                saveResult(game, homeField.getText(), awayField.getText());
            } else if (e.getCode().getName().equals("Esc")) {
                stackPane.getChildren().remove(stackPane.getChildren().size()-1);
                stackPane.getChildren().remove(stackPane.getChildren().size()-1);
                dateSelector.setDisable(false);
            }
        });
        
        saveButton.setOnAction(e -> saveResult(game, homeField.getText(), awayField.getText()));
        
        cancelButton.setOnAction(e -> {
            stackPane.getChildren().remove(stackPane.getChildren().size()-1);
            stackPane.getChildren().remove(stackPane.getChildren().size()-1);
            dateSelector.setDisable(false);
        });

        stackPane.getChildren().addAll(r, new HBox(vBox));
    }

    private void saveResult(Game game, String home, String away) {
        try {
            int h = Integer.parseInt(home);
            int a = Integer.parseInt(away);
            
            game.setScore(h, a);

            displayLeagueTable();
            displayFixtures(gameDates.get(dateSelector.getSelectionModel().getSelectedIndex()));
            
        } catch (NumberFormatException e) {
            showMessage("Invalid Result");
        }
        StackPane stackPane = (StackPane)root.getParent();
        stackPane.getChildren().remove(stackPane.getChildren().size()-1);
        stackPane.getChildren().remove(stackPane.getChildren().size()-1);
        dateSelector.setDisable(false);
    }


    private void displayLeagueTable() {
        tablePane.getChildren().clear();
        tablePane.addRow(0, new HBox(new Label("")), new HBox(new Label("Standings")), new HBox(new Label("G")), new HBox(new Label("W")), new HBox(byPointsRadio.isSelected() ? new Label("D") : new Label("T")), new HBox(new Label("L")), new HBox(new Label("P")));
        

        for (int i = 0; i < tablePane.getChildren().size(); i++) {
            tablePane.getChildren().get(i).setStyle("-fx-background-color: lightskyblue");
        }

        sortTeamsForStandings();

        for (int i = 0; i < teams.size(); i++) {
            String team = teams.get(i);
            HBox teamBox = new HBox(new Label(team));
            teamBox.setStyle("-fx-alignment: center-left");
            tablePane.addRow(i + 1, new HBox(new Label(i + 1 + "")), teamBox, new HBox(new Label(getTeamPlayed(team) + "")), new HBox(new Label(getTeamWins(team) + "")), new HBox(new Label(getTeamDraws(team) + "")), new HBox(new Label(getTeamLosses(team) + "")), new HBox(new Label((byPointsRadio.isSelected() ? getTeamPoints(team) : getTeamPercent(team)) + "")));
        }
    }

    private void sortTeamsForStandings() {
        for (int i = teams.size() - 1; i > 1; i--) {
            for (int j = 0; j < i; j++) {
                if ((byPointsRadio.isSelected() && getTeamPoints(teams.get(j)) < getTeamPoints(teams.get(j + 1))) ||
                        (byPercentRadio.isSelected() && getTeamPercent(teams.get(j)) < getTeamPercent(teams.get(j + 1)))) {
                      teams.add(j, teams.remove(j + 1));
                }
            }
        }
    }

    /* calculate how many games a team have won  */
    private int getTeamWins(String team) {
        int wins = 0;

        for (Game game : games) {
            if (game.hasWinner() && game.getWinner().equals(team))
                wins++;
        }

        return wins;
    }

    /* calculate how many games a team have tied/drawn  */
    private int getTeamDraws(String team) {
        return getTeamTies(team);
    }
    
    /* calculate how many games a team have tied/drawn  */
    private int getTeamTies(String team) {
        int c = 0;

        for (Game game : games) {
            if (game.isDraw() && game.involves(team))
                c++;
        }

        return c;
    }

    /* calculate how many games a team have lost  */
    private int getTeamLosses(String team) {
        int c = 0;

        for (Game game : games) {
            if (game.hasWinner() && game.involves(team) && !game.getWinner().equals(team))
                c++;
        }

        return c;
    }

    /* calculate how many games a team have played  */
    private int getTeamPlayed(String team) {
        int p = 0;

        for (Game game : games) {
            if (game.isPlayed() && (game.getHomeTeam().equals(team) || game.getAwayTeam().equals(team)))
                p++;
        }

        return p;
    }

    /* calculate the points of a team  */
    private int getTeamPoints(String team) {
        return getTeamWins(team) * 3 + getTeamDraws(team);
    }

    /* calculate the winning percent of a team */
    private double getTeamPercent(String team) {
        if (getTeamPlayed(team) == 0)
            return 0;
        
        double pct = (double)(getTeamWins(team) * 2 + getTeamDraws(team)) / (getTeamPlayed(team) * 2);
        
        pct = Math.round(pct * 1000) / 1000.0;

        return pct;
    }


    public static void main(String[] args) {
        launch(args);
    }
}