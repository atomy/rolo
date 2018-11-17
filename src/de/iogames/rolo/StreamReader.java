package de.iogames.rolo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Reading the network stream and transforming what we read.
 */
public class StreamReader {
    protected InputStream mInputStream;

    ByteArrayOutputStream mByteArrayOutputStream;

    public StreamReader(InputStream inputStream) {
        mInputStream = inputStream;
    }

    public void read() {
        mByteArrayOutputStream = new ByteArrayOutputStream();

        byte[] read = new byte[1024];
        int len;

        try {
            if ((len = mInputStream.read(read)) > -1) {
                mByteArrayOutputStream.write(read, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getHexString() {
        // this is the final byte array which contains the data
        // read from Socket
        byte[] bytes = mByteArrayOutputStream.toByteArray();

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }

        return sb.toString();
    }

    public ArrayList<String> getHexStringAsArray() {
        byte[] bytes = mByteArrayOutputStream.toByteArray();

        ArrayList<String> hexArray = new ArrayList<>();

        for (byte b : bytes) {
            hexArray.add(String.format("%02X ", b));
        }

        return hexArray;
    }
}
