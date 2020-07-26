import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

public class Command {
    private String cmd;
    private String word;
    private Map<Integer, String> definitions;

    Command(String cmdString){
        String[] args;
        if (cmdString.charAt(0) != '.'){
            this.cmd = "GET";
            // Assume a single word
            args = cmdString.split(" ", 2);
            this.word = parseCmd(args[0]);
            return;
        } else {
            args = cmdString.substring(1).split(" ", 3);
            this.cmd = parseCmd(args[0]);
        }

        // Assume the next argument is the word (as is the syntax of the commands)
        this.word = args[1];

        // Now Parse the rest of the command
        this.definitions = parseArgs(this.cmd, args[2]);
    }

    String parseCmd(String cmd){
        String theCmd;
        switch(cmd.toLowerCase()){
            case "put":
            case "new":
                theCmd = "NEW";
                break;
            case "set":
            case "update":
                theCmd = "SET";
                break;
            case "rem":
            case "remove":
            case "del":
            case "delete":
                theCmd = "DEL";
                break;
            case "get":
            case "query":
            default:
                theCmd = "GET";
                break;
        }
        return theCmd;
    }

    Map<Integer, String> parseArgs(String cmd, String args){
        HashMap<Integer, String> tmp = new HashMap<Integer, String>();
        if(cmd.equals("DEL")) {
            // arg should be either nothing, or a number
            int tempInt = -1;
            try {
                // Try to parse the args as integer
                tempInt = Integer.parseInt(args);
                // we expect a case where there is no following integer
            } catch (NumberFormatException e){}

            tmp.put(tempInt, "");
        }
        return tmp;
    }

    @Override
    public String toString() {
        return cmd + word + definitions.toString();
    }
}
