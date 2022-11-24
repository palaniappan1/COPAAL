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
import java.nio.charset.StandardCharsets;

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
                    LOGGER.info("Client Accepted " + clientSocket.getLocalPort() + "  " + clientSocket.getPort());
                    listenAndRespondToData();
                }
            } catch (IOException e) {
                System.err.println("Unable to process client request");
                e.printStackTrace();
            }
        };
        Thread serverThread = new Thread(serverTask);
        serverThread.start();
        serverThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                LOGGER.info("SOME EXCEPTION OCCURED " + e);
                try {
                    outputStream.writeUTF(e.toString());
                    outputStream.close();
                    bufferedReader.close();
                    inputStream.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
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
                String data;
                DataInputStream in = new DataInputStream(inputStream);
                byte[] buffer = new byte[1024]; // or 4096, or more
                in.read(buffer);
                data = new String(buffer, StandardCharsets.UTF_8).trim();
                // At the eof, empty line is being sent to avoid that adding this check
                if(data.equals("")){
//                    LOGGER.info("Connection closed");
//                    clientSocket.close();
                    return;
                }
                if(data.charAt(0) == 'b') {
                    data = data.substring(2, data.length() - 1);
                }
                JSONObject jsonObject = new JSONObject(data);
                if(jsonObject.getString("type").equals("call") && (jsonObject.getString("content").equals("type"))){
                    JSONObject acknowledgeresponse = new JSONObject();
                    acknowledgeresponse.put("type","type_response");
                    acknowledgeresponse.put("content","unsupervised");
                    outputStream.write(acknowledgeresponse.toString().getBytes(StandardCharsets.UTF_8));
                }
                else{
                    LOGGER.info("GOT DATA AND DATA IS " + jsonObject);
                    String subject = jsonObject.getString("subject");
                    String property = jsonObject.getString("predicate");
                    String object = jsonObject.getString("object");
                    LOGGER.info("GOT DATA AND Subject is  " + subject + " and object is " + object + "and the predicate is" + property);
                    try {
                        FactCheckingResult result = evaluateTriples(subject,object,property);
                        JSONObject response = new JSONObject();
                        response.put("type","test_result");
                        response.append("score",String.valueOf(result.getVeracityValue()));
//                        response.put("score",0.789);
                        outputStream.write(response.toString().getBytes(StandardCharsets.UTF_8));
                    } catch (Exception e) {
                        LOGGER.info("SOME EXCEPTION OCCURED " + e);
                        outputStream.writeUTF(e.toString());
                        outputStream.close();
                        bufferedReader.close();
                        inputStream.close();
                    }
                }
            }
        }
        catch(IOException ioException){
            ioException.printStackTrace();
            try {
                outputStream.writeUTF(ioException.toString());
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
