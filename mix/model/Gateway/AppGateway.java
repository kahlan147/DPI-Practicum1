package model.Gateway;

import model.Gateway.Serializer.Serializer;

/**
 * Created by Niels Verheijen on 13/02/2019.
 */
public class AppGateway {

    public MessageSenderGateway sender;
    public MessageReceiverGateway receiver;
    public Serializer serializer;

    public AppGateway(Serializer serializer, String senderString, String receiverString){
        this.serializer = serializer;
        this.sender = new MessageSenderGateway(senderString);
        this.receiver = new MessageReceiverGateway(receiverString);
    }
}
