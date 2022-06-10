package org.dice_research.fc.run;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.dice_research.fc.IFactChecker;
import org.dice_research.fc.config.RequestParameters;
import org.dice_research.fc.data.FactCheckingResult;
import org.dice_research.fc.paths.verbalizer.IPathVerbalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        Runnable serverTask = () -> {
            try {
                serverSocket = new ServerSocket(portNumber);
                LOGGER.info("Socket is up and running on Local Port" + serverSocket.getLocalPort() + " and on Local Socket Address"+serverSocket.getLocalSocketAddress() );
                LOGGER.info("Waiting for clients to connect...");
                while (true) {
                    clientSocket = serverSocket.accept();
                    listenAndRespondToData();
                    LOGGER.info("Client Accepted " + clientSocket.getLocalPort() + "  " + clientSocket.getPort());
                    LOGGER.info("Context is  " + ctx);
                }
            } catch (IOException e) {
                System.err.println("Unable to process client request");
                e.printStackTrace();
            }
        };
        Thread serverThread = new Thread(serverTask);
        serverThread.start();
//            serverSocket = new ServerSocket(portNumber);
//            SocketAddress sockeAddress = new InetSocketAddress("127.0.0.1",portNumber);
//            serverSocket.bind(sockeAddress);
//            clientSocket = serverSocket.accept();
    }

    public void listenAndRespondToData(){
        try {
            inputStream = clientSocket.getInputStream();
            outputStream = new DataOutputStream(clientSocket.getOutputStream());
            DataInputStream in = new DataInputStream(inputStream);
            String data = "";
            while (true) {
                byte[] buffer = new byte[1024]; // or 4096, or more
                in.read(buffer);
                data = new String(buffer, "ISO-8859-1").trim();
                data = data.substring(0,data.length() - 1);
                data = data.replaceAll("[><]","");
                LOGGER.info("GOT DATA AND DATA IS " + data);
                String[] dataArray = data.split(" ");
                LOGGER.info("Length of data array is" + dataArray.length);
                String subject = dataArray[0];
                String property = dataArray[1];
                String object = dataArray[2];
                try {
                        FactCheckingResult result = evaluateTriples(subject,object,property);
                        outputStream.writeUTF(String.valueOf(result.getVeracityValue()));
                } catch (Exception e) {
                    LOGGER.info("SOME EXCEPTION OCCURED " + e);
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

        IPathVerbalizer verbalizer = ctx.getBean(IPathVerbalizer.class, ctx.getBean(QueryExecutionFactory.class), new RequestParameters());
        verbalizer.verbalizeResult(result);

        LOGGER.info("Result is " + result.getVeracityValue());
        return result;
    }

    @PostConstruct
    public void startSocketProcedure() {
        serverStart(3333);
//        listenAndRespondToData();
    }

//    "http://dbpedia.org/resource/Barack_Obama"
//    "http://dbpedia.org/resource/United_States"
//    "http://dbpedia.org/ontology/nationality"
//    "http://dbpedia.org/resource/Barack_Obama http://dbpedia.org/resource/United_States http://dbpedia.org/ontology/nationality"
    // "http://dbpedia.org/resource/Alexander_Kerensky http://dbpedia.org/resource/Ulyanovsk http://dbpedia.org/ontology/birthPlace"
    //"http://localhost:8080/api/v1/validate?subject=http://dbpedia.org/resource/Barack_Obama&object=http://dbpedia.org/resource/United_States&property=http://dbpedia.org/ontology/nationality&pathlength=2"
    //http://localhost:8080/api/v1/validate?subject=http://dbpedia.org/resource/Alexander_Kerensky&object=http://dbpedia.org/resource/Ulyanovsk&property=http://dbpedia.org/ontology/birthplace&pathlength=2
}
