I hate to have to include this, but you'll need some special run instructions for my project:  
the standard run arguments are  
Client:  
  java -jar ./client.jar  
  java -jar ./client.jar -cli [hostname] [port]  
Server:  
  java -jar ./server.jar [port] [dictionary-file]  

but unfortunately my packager bugged out and I couldn't get it to attach JavaFX properly, so instead you'll need to go to  
https://gluonhq.com/products/javafx/ and get the sdk (if you don't already have it), and run the client as  
Client:  
  java --module-path [path-to-javafx-sdk] --add-modules javafx.controls,javafx.fxml -jar ./client.jar