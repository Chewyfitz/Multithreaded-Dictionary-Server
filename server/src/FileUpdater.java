// FileUpdater.java: A dictionary dumper
/* Aidan Fitzpatrick (fitzpatricka) 835833 for Distributed Systems COMP90015
 * Dumps the dictionary to disk.
 * This task is run every certain amount of time by a timer in the Dictionary class.
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class FileUpdater extends TimerTask {

    // Going to be accessing the dictionary's public JsonObject to save to the file.
    public volatile Dictionary dict;
    File file;
    Timer timer;

    FileUpdater(Dictionary dict, File file){
        this.dict = dict;
        this.file = file;
        timer = new Timer();
    }

    @Override
    public void run() {
        // Update the file
        try {
            FileWriter fw = new FileWriter(file);
            fw.write(dict.dictionary.toString());
            fw.close();
        } catch (NullPointerException | IOException e){
            // Dictionary does not exist, or
            // Write failed due to IOException
            e.printStackTrace();
        }
        // Update complete
    }
}
