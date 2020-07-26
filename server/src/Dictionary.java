// Dictionary.java: The dictionary itself
/* Aidan Fitzpatrick (fitzpatricka) 835833 for Distributed Systems COMP90015
 * This class represents the database (our dictionary). It loads the dictionary
 * from disk and parses it into a JSON object, and then allows anyone (Worker
 * Threads) to query the (volatile) dictionary directly to find words.
 * It also contains a queue to hold updates to the dictionary which are
 * performed as they arrive, in the order they arrive, and a Timer to repeatedly
 * update the file on disk.
 */

import com.google.gson.*;
import javafx.util.Pair;

import java.io.*;
import java.nio.file.Files;
import java.util.Timer;
import java.util.concurrent.LinkedBlockingQueue;

public class Dictionary extends Thread {
    String filename;
    FileUpdater fileUpdater;

    public volatile JsonObject dictionary;
    private volatile LinkedBlockingQueue<Pair<Pair<Integer, String>, String>> wordUpdate;

    Dictionary(String filename){
        // update file with default period (3min) 180000ms
        this(filename, 180000);
    }

    Dictionary(String filename, long updatePeriod){
        // Set local variables
        this.filename = filename;
        File file = new File(filename);
        this.wordUpdate = new LinkedBlockingQueue<>();

        // Create an object to get JSON from the file
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        // Try to open the file (possible to not find the file here)
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));
            // Save the dictionary as a JSON Object
            dictionary = gson.fromJson(br, JsonObject.class);
            // Create a FileUpdater
            this.fileUpdater = new FileUpdater(this, file);
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(fileUpdater, updatePeriod, updatePeriod);
        } catch (FileNotFoundException e){
            System.err.println("FileIOThread Exception: "+e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public String getWordDef(String word, int defNum){
        JsonElement dictWord = dictionary.get(word);
        JsonArray definitions;
        if(dictWord != null) {
            if ((definitions = dictWord.getAsJsonObject().get("definitions").getAsJsonArray()) != null) {
                return definitions.get(defNum).getAsString();
            }
        }
        // Word not found
        return null;
    }

    public String getWord(String word){
        JsonElement dictWord = dictionary.get(word);
        if(dictWord != null) {
            JsonObject def;
            if ((def = dictWord.getAsJsonObject()) != null) {
                JsonElement defList;
                if((defList = def.get("definitions")) != null){
                    JsonArray defArray;
                    if((defArray = defList.getAsJsonArray()) != null){
                        return defArray.toString();
                    }
                }

            }
        }
        // Word not found
        return null;
    }

    public String getWordList(){
        return dictionary.keySet().toString();
    }

    public boolean updateDef(String word, int defNum, String newDef){
        this.wordUpdate.add(new Pair<>(new Pair<>(defNum, word), newDef));
        return true;
    }

    public boolean updateDef(String word, String newDef){
        // -1 == "Add to end"
        return updateDef(word, -1, newDef);
    }

    public boolean removeDef(String word, Integer defNum){
        // -2 == "Delete definition"
        return updateDef(word, -2, defNum.toString());
    }

    @Override
    public void run() {
        while(true) {
            synchronized (this) {
                try {
                    if(!wordUpdate.isEmpty()){
                        performUpdate(wordUpdate.take());
                    }
                    // Ideally this thread should sleep here so that it isn't using a thread without reason.
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void performUpdate(Pair<Pair<Integer, String>, String> update){
        // This looks pretty silly, but it's just a 3-tuple
        int defNum = update.getKey().getKey();
        String word = update.getKey().getValue();
        String def = update.getValue();

        assert(dictionary != null);

        switch(defNum){
            case -2:
                // delete
                int index = Integer.parseInt(def);
                if(index>-1) {
                    dictionary.get(word).getAsJsonObject().get("definitions").getAsJsonArray().remove(index);
                    if(dictionary.get(word).getAsJsonObject().get("definitions").getAsJsonArray().size() == 0){
                        dictionary.remove(word);
                    }
                } else {
                    dictionary.remove(word);
                }
                break;
            case -1:
                // new at end
                if(dictionary.has(word)){
                    dictionary.get(word).getAsJsonObject().get("definitions").getAsJsonArray().add(def);
                } else {
                    JsonObject newJson = new JsonObject();
                    JsonArray jArray = new JsonArray();
                    jArray.add(new JsonPrimitive(def));
                    newJson.add("definitions", jArray);
                    dictionary.add(word, newJson);
                }
                break;
            default:
                // update
                dictionary.get(word).getAsJsonObject().get("definitions").getAsJsonArray().set(defNum, new JsonPrimitive(def));
                break;
        }
    }

    private void saveToDisk(){

    }
}
