// Controller.java: The dictionary itself
/* Aidan Fitzpatrick (fitzpatricka) 835833 for Distributed Systems COMP90015 */

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;

public class Controller {
    // (This controller is a bit of a chonker)
    enum UIState{
        NO_SERVER,          // There is no server (Enter one!)
        LOST_SERVER,        // We lost the server! Reconnect!
        CONNECTING_SERVER,  // Connecting to the server
        WORD_LIST_LOADING,  // Loading word list
        IDLE,               // Waiting for something cool to happen
        WORD_EDITING,       // Editing an existing word
        CLOSE_EDITOR        // Closing the editor
    }

    private UIState state;

    public static String del_text = "Delete";
    public static String close_text = "Close";


    @FXML private Label status;

    // The server
    @FXML private TextField serverAddress;
    @FXML private TextField serverPort;
    @FXML private Button connectButton;

    private ServerIO server;

    // A query (search)
    @FXML private TextField queryBox;
    @FXML private Button searchButton;

    private ArrayList<String> words;
    @FXML private TreeView<String> definitions;

    @FXML private TextField wordTitle;
    @FXML private TextArea editPanel;
    @FXML private Button saveEditButton;

    @FXML private Button addWordButton;
    @FXML private Button editWordButton;
    @FXML private Button deleteWordButton;

    private TreeItem<String> root = new TreeItem<String>("");
    private TreeItem<String> selected = null;

    public Controller(){
    }

    @FXML public void initialize(){
        definitions.setRoot(root);
        // Listener for selecting an item in the word list
        definitions.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldVal, newVal) -> {
                    if(newVal != null) {
                        this.selected = newVal;
                        this.editWordButton.setDisable(false);
                        this.deleteWordButton.setDisable(false);
                    } else {
                        this.selected = null;
                        this.editWordButton.setDisable(true);
                        this.deleteWordButton.setDisable(true);
                    }
                });
        this.state = UIState.NO_SERVER;
        words = new ArrayList<>();
    }

    private void changeState(UIState newState){
        // A big chunky transition function to centralise the UI state changes.
        System.out.println("Changing from state "+this.state+" to "+newState);
        switch(newState){
            case NO_SERVER:
                if(this.state == UIState.CONNECTING_SERVER){
                    status.setText("Server not Found.");
                }
                break;
            case LOST_SERVER:
                status.setText("Server lost.");
                break;
            case CONNECTING_SERVER:
                status.setText("Connecting...");
                break;
            case WORD_LIST_LOADING:
                status.setText("Loading...");
                break;
            case WORD_EDITING:
                // Set status text
                status.setText("Editing Word...");
                // Disable
                addWordButton.setDisable(true);
                editWordButton.setDisable(true);
                // Enable
                wordTitle.setDisable(false);
                editPanel.setDisable(false);
                deleteWordButton.setDisable(false);
                saveEditButton.setDisable(false);
                // Update button text
                deleteWordButton.setText(close_text);
                break;
            case IDLE:
                // Probably not an amazing idea, but the idle will probably come from a bunch of
                // different places
                switch(this.state){
                    case WORD_LIST_LOADING:
                        // Set status text
                        status.setText("List loaded.");
                        // Once connection has been established, activate the interface (and disable the server settings)
                        // Disable
                        serverAddress.setDisable(true);
                        serverPort.setDisable(true);
                        connectButton.setDisable(true);
                        saveEditButton.setDisable(true);
                        // Enable
                        definitions.setDisable(false);
                        queryBox.setDisable(false);
                        searchButton.setDisable(false);
                        addWordButton.setDisable(false);
                        break;
                    case WORD_EDITING:
                        // Set status text
                        status.setText("Word saved.");
                    case CLOSE_EDITOR:
                        // Disable
                        saveEditButton.setDisable(true);
                        editPanel.setDisable(true);
                        wordTitle.setDisable(true);
                        // Enable
                        addWordButton.setDisable(false);
                        editWordButton.setDisable(false);
                        // Update button text
                        deleteWordButton.setText(del_text);
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
        this.state = newState;
    }

    @FXML public void connect(){
        if(this.state == UIState.NO_SERVER) {
            changeState(UIState.CONNECTING_SERVER);
            // Cleanup input fields
            int port_asInt;
            try{
                port_asInt = Integer.parseInt(serverPort.getText());
            } catch (NumberFormatException e){
                changeState(UIState.NO_SERVER);
                return;
            }
            String clean_address = serverAddress.getText();
            this.server = new ServerIO(clean_address, port_asInt);

            if(!this.server.openSocket()){
                changeState(UIState.NO_SERVER);
                return;
            }

            changeState(UIState.WORD_LIST_LOADING);
            String[] wordList;
            if((wordList = server.getWordList()) == null){
                changeState(UIState.LOST_SERVER);
            } else {
                for(String word: wordList){
                    addWordItem(word);
                }
                changeState(UIState.IDLE);
            }
        }
    }
    private int addWordItem(String word){
        TreeItem<String> treeWord = new TreeItem<>(word){
            private boolean isLeaf = false;
            private boolean gotChildren = false;
            @Override public ObservableList<TreeItem<String>> getChildren(){
                if(!gotChildren){
                    gotChildren = true;
                    super.getChildren().setAll(getDefs(this));
                }
                return super.getChildren();
            }
            @Override public boolean isLeaf(){
                if(gotChildren){
                    return super.isLeaf();
                }
                return isLeaf;
            }
        };

        if(!words.contains(word)){
            words.add(word);
            definitions.getRoot().getChildren().add(treeWord);
        }
        return definitions.getRoot().getChildren().indexOf(treeWord);
    }
    private void addWord(String word, String def){
        int index;
        if(!words.contains(word)){
            words.add(word);
            index = words.indexOf(word);
            definitions.getRoot().getChildren().add(new TreeItem<>(word));
        } else {
            index = words.indexOf(word);
        }
        definitions.getRoot().getChildren().get(index).getChildren().add(new TreeItem<>(def));
    }

    private TreeItem<String>[] getDefs(TreeItem<String> word){
        String[] defs = server.getDefs(word.getValue());
        TreeItem<String>[] defList = new TreeItem[defs.length];
        for(int i = 0; i<defs.length; i++){
            TreeItem<String> ti = new TreeItem<String>();
            ti.setValue(defs[i]);
            defList[i] = ti;
        }
        return defList;
    }

    @FXML public void addEmptyItem(){
        TreeItem<String> empty = new TreeItem<>("Lorem Ipsum");
        empty.getChildren().add(new TreeItem<>("dolor sit amet"));

        definitions.getRoot().getChildren().add(empty);
    }
    @FXML public void editEntry(){
        TreeItem<String> tempWord;
        TreeItem<String> tempDef;

        // Check to see if anything has been selected
        if(selected != null){

            // See if we've selected a word or a definition
            if(selected.getParent() == root){
                tempWord = selected;

                // If the word is defined (it should be) then select its first definition
                // in case it isn't create a new definition.
                if(selected.getChildren().size() > 0) {
                    tempDef = selected.getChildren().get(0);
                } else {
                    tempDef = new TreeItem<String>("");
                }
            } else {
                // Selected a definition
                tempWord =  selected.getParent();
                tempDef = selected;
            }

            // Enable the edit panel
            setEditMode(tempWord, tempDef);
        } else {
            status.setText("No word selected!");
        }
    }
    @FXML public void addButtonHandler() {
        if (state == UIState.WORD_EDITING) {
            // We're adding a word we've written the definition for
            if (server.addWord(wordTitle.getText(), editPanel.getText())) {
                addWord(wordTitle.getText(), editPanel.getText());
            } else {
                status.setText("Word saving failed...");
                changeState(UIState.IDLE);
                return;
            }
            status.setText("Word Saved!");
            changeState(UIState.IDLE);
        } else{
            // Our editor is closed
            setEditMode();
        }
    }
    @FXML public void saveButtonHandler(){
        if(words.contains(wordTitle.getText())) {
            int wordIndex = words.indexOf(wordTitle.getText());
            int defIndex;
            if(definitions.getRoot().getChildren().get(wordIndex).getChildren().contains(selected)){
                defIndex = definitions.getRoot().getChildren().get(wordIndex).getChildren().indexOf(selected);
            } else {
                defIndex = 0;
            }

            if(server.setDef(wordTitle.getText(), defIndex, editPanel.getText())) {
                // TreeItem<String> temp = definitions.getRoot().getChildren().get(index).getChildren().get(index);
                TreeItem<String> temp = new TreeItem<>(editPanel.getText());
                definitions.getRoot().getChildren().get(wordIndex).getChildren().set(defIndex, temp);
            } else {
                status.setText("Word saving failed...");
                changeState(UIState.IDLE);
                return;
            }
            status.setText("Word Saved!");
            changeState(UIState.IDLE);
        } else {
            addButtonHandler();
        }
    }
    public void setEditMode(){
        this.setEditMode(new TreeItem<>(""), new TreeItem<>(""));
    }
    public void setEditMode(TreeItem<String> word, TreeItem<String> def){
        // State transition
        changeState(UIState.WORD_EDITING);

        // Copy text into the edit panel
        wordTitle.setText(word.getValue());
        editPanel.setText(def.getValue());
    }
    @FXML public void deleteEntry() {
        if(state == UIState.WORD_EDITING){
            wordTitle.setText("");
            editPanel.setText("");
            changeState(UIState.CLOSE_EDITOR);
            changeState(UIState.IDLE);
        } else {
            removeWordOrDef(selected);
        }
    }
    public void removeWordOrDef(TreeItem<String> sel){
        TreeItem<String> wd = sel.getParent();
        int index = wd.getChildren().indexOf(sel);
        if(wd == definitions.getRoot()){
            // Delete a Word
            if(server.removeWord(sel.getValue())){
                wd.getChildren().remove(index);
                words.remove(wd.getValue());
                status.setText("Word deleted!");
            } else {
                status.setText("Word deletion failed!");
            }
        } else {
            // Delete the definition of a word
            if(server.removeDef(wd.getValue(), index)){
                wd.getChildren().remove(index);
                status.setText("Definition deleted!");
            } else {
                status.setText("Definition deletion failed!");
            }
        }
    }
    @FXML public void searchButtonClicked(){
        String queryWord = queryBox.getText();
        if(words.contains(queryWord)){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Query Result");
            alert.setHeaderText(queryWord);
            int index = words.indexOf(queryWord);
            StringBuilder defString = new StringBuilder();
            List<TreeItem<String>> defs = definitions.getRoot().getChildren().get(index).getChildren();
            for(TreeItem<String> def : defs){
                defString.append(def.getValue());
                defString.append("\n");
            }
            alert.setContentText(defString.toString());

            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Query Result");
            alert.setHeaderText(queryWord);
            alert.setContentText("Not Found :(");

            alert.showAndWait();
        }
    }
}
