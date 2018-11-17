package de.iogames.rolo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Client implements Runnable {
    private final Socket mConnectionSocket;

    public Client(Socket connectionSocket) {
        this.mConnectionSocket = connectionSocket;
    }

    public Socket getConnectionSocket() {
        return this.mConnectionSocket;
    }

    @Override
    public void run() {
        StreamProcess streamProcess = new StreamProcess(this);

        while (true) {
            try {
                streamProcess.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
