package model.Gateway;

import model.Gateway.Serializer.BankSerializer;
import model.Gateway.Serializer.Serializer;

/**
 * Created by Niels Verheijen on 13/02/2019.
 */
public class BankAppGateway extends AppGateway{

    public BankAppGateway(Serializer serializer, String senderString, String receiverString){
        super(serializer,senderString,receiverString);
    }
}
