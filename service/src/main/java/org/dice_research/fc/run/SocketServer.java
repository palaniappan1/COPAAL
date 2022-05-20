package org.dice_research.fc.run;

import org.dice_research.fc.paths.PathBasedFactChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketServer.class);

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        LOGGER.info("ServerSocket Started");
        clientSocket = serverSocket.accept();
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        LOGGER.info("Out and in variables assiged");
        out.println(1.0);
    }

    public void stop() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }

    public static void main(String[] args) {
        try {
            new SocketServer().start(9898);
            new SocketClient().sendMessage("Subject","Object","Property");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
