package de.iogames.rolo;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer implements Runnable {
    ServerSocket mServerSocket;

    @Override
    public void run() {
        try {
            StreamProcess streamProcess = new StreamProcess();
            mServerSocket = new ServerSocket(28015);
            System.out.println("Accepting connections");

            while (true) {
                Socket connectionSocket = mServerSocket.accept();
                System.out.println("New connection from: " + connectionSocket.getInetAddress());

                streamProcess.read(connectionSocket.getInputStream());

//                DataOutputStream dataOutputStream = new DataOutputStream(connectionSocket.getOutputStream());
//                String capatalizedSentence = clientSentence.toUpperCase() + 'n';
//                String capatalizedSentence = input.toUpperCase() + 'n';
//                dataOutputStream.writeBytes(capatalizedSentence);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
