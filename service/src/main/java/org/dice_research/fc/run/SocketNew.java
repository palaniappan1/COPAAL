package org.dice_research.fc.run;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.dice_research.fc.IFactChecker;
import org.dice_research.fc.data.FactCheckingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component()
public class SocketNew  {

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketNew.class);

    @Autowired
    ApplicationContext ctx;

    //InputStream to read the contents from the client
    InputStream inputStream = null;

    //OutputStream to send the data to the client
    DataOutputStream outputStream  = null;

    //ServerSocket Object
    ServerSocket serverSocket = null;

    //Client Socket Object
    Socket clientSocket = null;

    public void serverStart(int portNumber) {
        try {
            serverSocket = new ServerSocket(portNumber);
            clientSocket = serverSocket.accept();
            LOGGER.info("Client Accepted " + clientSocket.getLocalPort() + "  " + clientSocket.getPort());
            LOGGER.info("Context is  " + ctx);
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
    }

    public void listenAndRespondToData(){
        try {
            inputStream = clientSocket.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            outputStream = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String data = "";
            while (true) {
                try {
                    data = reader.readLine();
                    LOGGER.info("GOT DATA AND DATA IS " + data);
                    String[] dataArray = data.split(" ");
                    String subject = dataArray[0];
                    String object = dataArray[1];
                    String property = dataArray[2];
                    outputStream.writeUTF(data);
                    try {
                        FactCheckingResult result = evaluateTriples(subject,object,property);
                        outputStream.writeUTF(String.valueOf(result.getVeracityValue()));
                    } catch (Exception e) {
                        LOGGER.info("SOME EXCEPTION OCCURED " + e);
                    }
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
    }

    public FactCheckingResult evaluateTriples(String subject, String object, String property){
        Resource subjectURI = ResourceFactory.createResource(subject);
        Resource objectURI = ResourceFactory.createResource(object);
        Property propertyURI = ResourceFactory.createProperty(property);
        LOGGER.info("The data are " + subjectURI + " " + objectURI + " " + propertyURI);
        IFactChecker factChecker = ctx.getBean(IFactChecker.class);
        FactCheckingResult result = factChecker.check(subjectURI, propertyURI, objectURI);
        return result;
    }

    @PostConstruct
    public void startSocketProcedure() {
        serverStart(3333);
        listenAndRespondToData();
    }

//    "http://dbpedia.org/resource/Barack_Obama"
//    "http://dbpedia.org/resource/United_States"
//    "http://dbpedia.org/ontology/nationality"
//    "http://dbpedia.org/resource/Barack_Obama http://dbpedia.org/resource/United_States http://dbpedia.org/ontology/nationality"
}
