package de.iogames.rolo;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

public class StreamProcess {
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static final String STATE_VALID_HANDSHAKE = "VALID_HANDSHAKE";

    public static final String STATE_VALID_PASSWORD = "STATE_VALID_PASSWORD";

    public static final String STATE_INVALID_PASSWORD = "STATE_INVALID_PASSWORD";

    public static final String STATE_FRESH_CONNECTION = "STATE_FRESH_CONNECTION";

    public static final int PAKET_EXECUTE_COMMAND_RESPONSE = 4;

    private static final int BYTES_HANDSHAKE_END = 12;

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

                mClient.getConnectionSocket().getOutputStream().write(getStatusCommandReply(packetId));
                mClient.getConnectionSocket().getOutputStream().flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println(String.format("Unknown command '%s'!", inputCommand));
        }
    }

    private byte[] getStatusCommandReply(int requestId) {
        String replyString = "hostname: Rcon Legacy 28026"
        + "version : 2112 secure (secure mode enabled, connected to Steam3)"
        + "map     : Procedural Map"
        + "players : 0 (500 max) (0 queued) (0 joining)"
        + ""
        + "id name ping connected addr owner violation kicks";

        return createPacket(requestId, PAKET_EXECUTE_COMMAND_RESPONSE, replyString);
    }

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

    // 0a:00:00:00:b0:04:00:00:02:00:00:00:00:00
    private void acknowledgeConnection() throws IOException {
        OutputStream outputStream = mClient.getConnectionSocket().getOutputStream();

        byte[] ack1 = {
            (byte) 0x0a,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0xb0,
            (byte) 0x04,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00
        };
        byte[] ack2 = {
            (byte) 0x0a,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0xb0,
            (byte) 0x04,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x02,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00
        };

        outputStream.write(ack1);
        outputStream.write(ack2);
    }

    private boolean isPasswordValid(ArrayList<String> hexArray) {
        StringBuilder stringBuilder = new StringBuilder();



        // -2 = 2 null-bytes at the end
        for (int i = BYTES_HANDSHAKE_END; i < hexArray.size() - 2; i++) {
            String trimmedElement = hexArray.get(i).trim();
            byte[] bytes = DatatypeConverter.parseHexBinary(trimmedElement);
            trimmedElement = new String(bytes, StandardCharsets.UTF_8);

            stringBuilder.append(trimmedElement);
        }

        String inputPassword = stringBuilder.toString();
        System.out.println("Received password is: " + inputPassword);
        
        return inputPassword.equals(HANDSHAKE_EXPECTED_PASSWORD);
    }

    private boolean isHandshake(ArrayList<String> hexArray) {
        boolean allMatch = true;
        ArrayList<String> expectedHandshake = getExpectedHandshake();

        // ignore first byte, it's the length of the whole thing, we don't care
        for (int i = 1; i < BYTES_HANDSHAKE_END; i++) {
            String trimmedElement = hexArray.get(i).trim();
            if (!trimmedElement.equals(expectedHandshake.get(i))) {
                allMatch = false;
                break;
            }
        }

        return allMatch;
    }

    private ArrayList<String> getExpectedHandshake() {
        ArrayList<String> handShake = new ArrayList<>();
        handShake.add("20"); // size, ignored
        handShake.add("00");
        handShake.add("00");
        handShake.add("00");
        handShake.add("B0");
        handShake.add("04");
        handShake.add("00");
        handShake.add("00");
        handShake.add("03");
        handShake.add("00");
        handShake.add("00");
        handShake.add("00");

        return handShake;
    }

    /**
     * Creates an RCON packet.
     *
     * @param id      the packet ID (not an opcode)
     * @param type    the type
     * @param command the command
     * @return the bytes representing the packet
     */
    public static byte[] createPacket(int id, int type, String command) {
        ByteBuffer packet = ByteBuffer.allocate(utf8Length(command) + 14);
        packet.order(LITTLE_ENDIAN);
        packet.putInt(utf8Length(command) + 10).putInt(id).putInt(type).put(command.getBytes()).put((byte) 0x00);

        return packet.array();
    }

    /**
     * Return length of a String using utf-8 encoding
     * @param sequence
     * @return
     */
    public static int utf8Length(CharSequence sequence) {
        int count = 0;
        for (int i = 0, len = sequence.length(); i < len; i++) {
            char ch = sequence.charAt(i);
            if (ch <= 0x7F) {
                count++;
            } else if (ch <= 0x7FF) {
                count += 2;
            } else if (Character.isHighSurrogate(ch)) {
                count += 4;
                ++i;
            } else {
                count += 3;
            }
        }
        return count;
    }
}
