This program is a project for a subject I took in Semester 1 2020.  
The idea for the project is a multithreaded dictionary which is served from a server (with a UI) to a client (also with a UI).  
  
The server is implemented with a thread-pool architecture and serves from the file dictionary.json, which is supplied in the `server.jar`'s runtime arguments.  
The below is the context provided to the marking team as my project would not run without linking the javafx module.
  

I hate to have to include this, but you'll need some special run instructions for my project:  
the standard run arguments are  
Client:  
-  `java -jar ./client.jar`  
-  `java -jar ./client.jar -cli [hostname] [port]`  
Server:  
-  `java -jar ./server.jar [port] [dictionary-file]`  

but unfortunately my packager bugged out and I couldn't get it to attach JavaFX properly, so instead you'll need to go to  
https://gluonhq.com/products/javafx/ and get the sdk (if you don't already have it), and run the client as  
Client:  
-  `java --module-path [path-to-javafx-sdk] --add-modules javafx.controls,javafx.fxml -jar ./client.jar`