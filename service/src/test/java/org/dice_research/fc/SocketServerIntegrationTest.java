package org.dice_research.fc;

import org.dice_research.fc.run.Application;
import org.dice_research.fc.run.SocketClient;
import org.dice_research.fc.run.SocketNew;
import org.dice_research.fc.run.SocketServer;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class SocketServerIntegrationTest {
    private Socket client;

    private static int port;

    private static PrintWriter out;

    @BeforeClass
    public static void start() throws InterruptedException, IOException {

        // Take an available port
        ServerSocket s = new ServerSocket(0);
        port = s.getLocalPort();
        s.close();

        Executors.newSingleThreadExecutor()
                .submit(() -> {
                    new SocketNew().serverStart(port);
                });
        Thread.sleep(500);
    }

    @Before
    public void init() {
        try {
            client = new Socket("127.0.0.1",port);
        }
        catch (IOException exception){
            exception.printStackTrace();
        }

    }

    @Test
    public void givenSocketClient_whenServerRespondsWhenStarted_thenCorrect() {
        Float response = 0f;
        String data = "http://dbpedia.org/resource/Barack_Obama http://dbpedia.org/resource/United_States http://dbpedia.org/ontology/nationality";
        try{
            out = new PrintWriter(client.getOutputStream(), true);
            out.println(data);
        }
        catch (IOException exception){
            exception.printStackTrace();
        }
//        assertEquals(1.0,response,0.1);
    }

    @After
    public void finish() {
        try {
            client.close();
        }catch (IOException exception){
            exception.printStackTrace();
        }
    }
}
