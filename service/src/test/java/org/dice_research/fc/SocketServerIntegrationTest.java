package org.dice_research.fc;

import org.dice_research.fc.run.Application;
import org.dice_research.fc.run.SocketClient;
import org.dice_research.fc.run.SocketServer;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class SocketServerIntegrationTest {
    private SocketClient client;

    private static int port;

    @BeforeClass
    public static void start() throws InterruptedException, IOException {

        // Take an available port
        ServerSocket s = new ServerSocket(0);
        port = s.getLocalPort();
        s.close();

        Executors.newSingleThreadExecutor()
                .submit(() -> {
                    try {
                        new SocketServer().start(port);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        Thread.sleep(500);
    }

    @Before
    public void init() {
        client = new SocketClient();
        try {
            client.startConnection("127.0.0.1", port);
        }
        catch (IOException exception){
            exception.printStackTrace();
        }

    }

    @Test
    public void givenSocketClient_whenServerRespondsWhenStarted_thenCorrect() {
        Float response = 0f;
        String subject = "http://dbpedia.org/resource/Barack_Obama";
        String object = "http://dbpedia.org/ontology/nationality";
        String property = "http://dbpedia.org/resource/United_States";
        try{
//            response = client.sendMessage(fact);
            response = client.sendMessage(subject,object,property);
        }
        catch (IOException exception){
            exception.printStackTrace();
        }
        assertEquals(1.0,response,0.1);
    }

    @After
    public void finish() {
        try {
            client.stopConnection();
        }catch (IOException exception){
            exception.printStackTrace();
        }
    }
}
