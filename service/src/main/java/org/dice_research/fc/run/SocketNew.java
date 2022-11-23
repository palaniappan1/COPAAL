package org.dice_research.fc.run;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.commons.io.LineIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.dice_research.fc.IFactChecker;
import org.dice_research.fc.config.RequestParameters;
import org.dice_research.fc.data.FactCheckingResult;
import org.dice_research.fc.paths.verbalizer.IPathVerbalizer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

@Component()
public class SocketNew  {

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketNew.class);

    @Autowired
    ApplicationContext ctx;

    //InputStream to read the contents from the client
    InputStream inputStream = null;

    //OutputStream to send the data to the client
    DataOutputStream outputStream  = null;

    //
    BufferedReader bufferedReader = null;

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
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            while (true) {
                StringBuffer sb = new StringBuffer();
                try{
                    String currentLine;
                    while((currentLine = bufferedReader.readLine()) != null){
                        sb.append(currentLine);
                        if(sb.indexOf("}") != -1){
                            break;
                        }
                    }
                }
                catch(Exception e){
                  e.printStackTrace();
                }
                LOGGER.info("GOT DATA AND DATA IS " + sb);
                JSONObject jsonObject = new JSONObject(sb.toString());
                LOGGER.info("GOT DATA AND DATA IS " + jsonObject);
                LOGGER.info("Length of json object is" + jsonObject.length());
                String subject = jsonObject.getString("subject");
                String property = jsonObject.getString("predicate");
                String object = jsonObject.getString("object");
                LOGGER.info("GOT DATA AND Subject is  " + subject + " and object is " + object + "and the predicate is" + property);
                try {
                        FactCheckingResult result = evaluateTriples(subject,object,property);
                        outputStream.writeUTF(String.valueOf(result.getVeracityValue()));
                } catch (Exception e) {
                    LOGGER.info("SOME EXCEPTION OCCURED " + e);
                    outputStream.close();
                    bufferedReader.close();
                    inputStream.close();
                }
            }
        }
        catch(IOException ioException){
            ioException.printStackTrace();
            try {
                outputStream.close();
                bufferedReader.close();
                inputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
    //http://dbpedia.org/resource/Real_Madrid_CF http://dbpedia.org/property/name http://dbpedia.org/resource/Toni_Fuidias
//    "http://dbpedia.org/resource/Barack_Obama http://dbpedia.org/resource/United_States http://dbpedia.org/ontology/nationality"
    // "http://dbpedia.org/resource/Alexander_Kerensky http://dbpedia.org/resource/Ulyanovsk http://dbpedia.org/ontology/birthPlace"
    //"http://localhost:8080/api/v1/validate?subject=http://dbpedia.org/resource/Barack_Obama&object=http://dbpedia.org/resource/United_States&property=http://dbpedia.org/ontology/nationality&pathlength=2"
    //http://localhost:8080/api/v1/validate?subject=http://dbpedia.org/resource/Alexander_Kerensky&object=http://dbpedia.org/resource/Ulyanovsk&property=http://dbpedia.org/ontology/birthplace&pathlength=2

    //
}
