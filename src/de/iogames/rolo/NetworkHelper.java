package de.iogames.rolo;

import java.nio.ByteBuffer;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

/**
 * Helping methods for Network stuff.
 */
public class NetworkHelper {
    /**
     * Creates an RCON packet.
     *
     * @param id      the packet ID (not an opcode)
     * @param type    the type
     * @param command the command
     * @return the bytes representing the packet
     */
    public static byte[] createPacket(int id, int type, String command) {
        ByteBuffer packet = ByteBuffer.allocate(StringHelper.utf8Length(command) + 14);
        packet.order(LITTLE_ENDIAN);
        packet.putInt(StringHelper.utf8Length(command) + 10).putInt(id).putInt(type).put(command.getBytes()).put((byte) 0x00);

        return packet.array();
    }

    /**
     * Build 1st ack connection bytes and return them.
     *
     * @return byte[]
     */
    public static byte[] getFirstAckConnectionBytes() {
        return new byte[]{
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
    }

    /**
     * Build 2nd ack connection bytes and return them.
     *
     * @return byte[]
     */
    public static byte[] getSecondAckConnectionBytes() {
        return new byte[]{
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
    }
}
