package de.iogames.rolo;

import java.io.IOException;
import java.io.OutputStream;

public class StreamProcess {
    public static final String STATE_VALID_PASSWORD = "STATE_VALID_PASSWORD";

    public static final String STATE_INVALID_PASSWORD = "STATE_INVALID_PASSWORD";

    public static final String STATE_FRESH_CONNECTION = "STATE_FRESH_CONNECTION";

    private static final String HANDSHAKE_EXPECTED_PASSWORD = "tralalalal1234";

    private static final String COMMAND_STATUS = "status";

    private final Client mClient;

    private String state = STATE_FRESH_CONNECTION;

    public StreamProcess(Client client) {
        this.mClient = client;
    }

    // %TODO, check for dead connections
    public void read() throws IOException {
        if (mClient.getConnectionSocket().getInputStream().available() <= 0) {
            return;
        }

        StreamReader streamReader = new StreamReader(mClient.getConnectionSocket().getInputStream());
        NetworkPacket networkPacket = streamReader.read();

        if (networkPacket == null) {
            return;
        }

        if (state.equals(STATE_FRESH_CONNECTION)) {
            if (networkPacket.type == PacketType.AUTHORIZATION_PACKET) {
                if (networkPacket.content.equals(HANDSHAKE_EXPECTED_PASSWORD)) {
                    System.out.println("Received password is valid");
                    state(STATE_VALID_PASSWORD);
                    return;
                } else {
                    System.out.println("Received password is invalid!");
                    state(STATE_INVALID_PASSWORD);
                }
            }
        }

        if (state.equals(STATE_VALID_PASSWORD)) {
            handleCommand(networkPacket.content, networkPacket.id);
        }
    }

    private void handleCommand(String inputCommand, int packetId) {
        if (inputCommand.equals(COMMAND_STATUS)) {
            try {
                if (!mClient.getConnectionSocket().isConnected()) {
                    System.out.println("Connection socket not longer connected, unable to send reply!");
                    return;
                }

                mClient.getConnectionSocket().getOutputStream().write(
                        (new CommandHandler()).getStatusCommandReply(packetId)
                );
                mClient.getConnectionSocket().getOutputStream().flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println(String.format("Unknown command '%s'!", inputCommand));
        }
    }



    /**
     * Change state and act on state change.
     *
     * @param newState String
     */
    private void state(String newState) {
        state = newState;

        if (newState.equals(STATE_VALID_PASSWORD)) {
            try {
                acknowledgeConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ACK new connection.
     *
     * @throws IOException
     */
    private void acknowledgeConnection() throws IOException {
        OutputStream outputStream = mClient.getConnectionSocket().getOutputStream();
        outputStream.write(NetworkHelper.getFirstAckConnectionBytes());
        outputStream.write(NetworkHelper.getSecondAckConnectionBytes());
    }
}
