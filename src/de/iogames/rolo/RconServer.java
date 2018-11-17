package de.iogames.rolo;

public class RconServer implements Runnable {
    @Override
    public void run() {
        TcpServer tcpServer = new TcpServer();

        Thread thread = new Thread(tcpServer);
        thread.start();
    }
}
