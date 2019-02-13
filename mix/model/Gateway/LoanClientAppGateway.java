package model.Gateway;

import model.Gateway.Serializer.Serializer;

/**
 * Created by Niels Verheijen on 13/02/2019.
 */
public class LoanClientAppGateway extends AppGateway{

    public LoanClientAppGateway(Serializer serializer, String senderString, String receiverString){
        super(serializer,senderString,receiverString);
    }
}
