package de.iogames.rolo;

/**
 * Handling incomming rcon commands.
 */
public class CommandHandler {
    /**
     * @param requestId int packet-id of request
     * @return byte[]
     */
    public byte[] getStatusCommandReply(int requestId) {
        String replyString = "hostname: Rcon Legacy 28026"
                + "version : 2112 secure (secure mode enabled, connected to Steam3)"
                + "map     : Procedural Map"
                + "players : 0 (500 max) (0 queued) (0 joining)"
                + ""
                + "id name ping connected addr owner violation kicks";

        return NetworkHelper.createPacket(requestId, PacketType.EXECUTE_COMMAND_RESPONSE, replyString);
    }

    public byte[] getPlayerListCommandReply(int requestId) {
        String replyString = "hostname: Rcon Legacy 28026"
                + "version : 2112 secure (secure mode enabled, connected to Steam3)"
                + "map     : Procedural Map"
                + "players : 0 (500 max) (0 queued) (0 joining)"
                + ""
                + "id name ping connected addr owner violation kicks";

        return NetworkHelper.createPacket(requestId, PacketType.EXECUTE_COMMAND_RESPONSE, replyString);
    }
}
