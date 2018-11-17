package de.iogames.rolo;

import javax.xml.bind.DatatypeConverter;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class StreamProcess {
    public static final String STATE_VALID_HANDSHAKE = "VALID_HANDSHAKE";

    public static final String STATE_VALID_PASSWORD = "STATE_VALID_PASSWORD";

    public static final String STATE_INVALID_PASSWORD = "STATE_INVALID_PASSWORD";

    private static final int BYTES_HANDSHAKE_END = 12;

    private static final String HANDSHAKE_EXPECTED_PASSWORD = "tralalalal1234";

    private String state = "NO_CONNECTION";

    public void read(InputStream inputStream) {
        StreamReader streamReader = new StreamReader(inputStream);
        streamReader.read();
        System.out.println("Received: " + streamReader.getHexString());

        ArrayList<String> hexArray = streamReader.getHexStringAsArray();
        if (isHandshake(hexArray)) {
            System.out.println("Received handshake is valid");
            state = STATE_VALID_HANDSHAKE;
        } else {
            System.out.println("Invalid handshake!");
        }

        if (state.equals(STATE_VALID_HANDSHAKE)) {
            if (isPasswordValid(hexArray)) {
                System.out.println("Received password is valid");
                state = STATE_VALID_PASSWORD;
            } else {
                System.out.println("Received password is invalid!");
                state = STATE_INVALID_PASSWORD;
            }
        }
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
}
