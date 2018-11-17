package de.iogames.rolo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * Reading the network stream and transforming what we read.
 */
public class StreamReader {
    /**
     * Max lines to parse.
     */
    private static int MAX_LINES = 40096;

    protected InputStream mInputStream;

    public StreamReader(InputStream inputStream) {
        mInputStream = inputStream;
    }

    public NetworkPacket read() throws IOException {
        byte[] inputBuffer = new byte[32000];
        int readBytes = mInputStream.read(inputBuffer, 0, 32000);
        ByteBuffer inputByteBuffer = ByteBuffer.wrap(inputBuffer).order(ByteOrder.LITTLE_ENDIAN);

        if (readBytes > 0) {
            return parseIncommingData(inputByteBuffer);
        }

        return null;
    }

    private NetworkPacket parseIncommingData(ByteBuffer data) {
        NetworkPacket networkPacket = new NetworkPacket();
        networkPacket.size = data.getInt(PacketFormat.BYTESTART_SIZE);
        networkPacket.id = data.getInt(PacketFormat.BYTESTART_ID);
        networkPacket.type = data.getInt(PacketFormat.BYTESTART_TYPE);

        if (networkPacket.size >= 32000) {
            System.out.println(String.format("Ignoring packet indicating size of: '%d' bytes", networkPacket.size));
            return null;
        }

        ByteBuffer trimmedResponse = ByteBuffer.allocate(data.capacity());

        for (int i = 12; i < MAX_LINES; i++) {
            if (data.get(i) == '\000') {
                break;
            }

            trimmedResponse.put(data.get(i));
        }

        networkPacket.content = new String(
                trimmedResponse.array(),
                0,
                networkPacket.size - 10,
                Charset.defaultCharset()
        );

        return networkPacket;
    }
}
