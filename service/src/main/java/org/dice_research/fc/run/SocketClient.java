package org.dice_research.fc.run;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketClient {

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;


    private static final Logger LOGGER = LoggerFactory.getLogger(SocketClient.class);



    public Socket startConnection(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        return clientSocket;
    }

    public void createVariables(Socket clientSocket){
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Float sendMessage(String subject , String object , String property) throws IOException {
        Resource subjectURI = ResourceFactory.createResource(subject);
        Resource objectURI = ResourceFactory.createResource(object);
        Property propertyURI = ResourceFactory.createProperty(property);
        //TODO Verify the given Fact for the veracity score
        LOGGER.info("Message is " + subject,object,property);
        out.println(1.0);
        Float resp = Float.parseFloat(in.readLine());
        return resp;
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }

    public static void main (String[] args){
//        SocketClient socketClient = new SocketClient();
//        try {
//            socketClient.startConnection("127.0.0.1", 9898);
//        }
//        catch (Exception exception){
//
//        }
    }
}
