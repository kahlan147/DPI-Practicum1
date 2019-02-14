package model.Gateway.Serializer;

import com.owlike.genson.Genson;
import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;

/**
 * Created by Niels Verheijen on 13/02/2019.
 */
public class BankSerializer implements Serializer{

    private Genson genson;

    public BankSerializer(){
        genson = new Genson();
    }

    @Override
    public String requestToString(Object o) {
        return genson.serialize(o);
    }

    @Override
    public Object requestFromString(String str) {
        return genson.deserialize(str, BankInterestRequest.class);
    }

    @Override
    public String replyToString(Object o) {
        return genson.serialize(o);
    }

    @Override
    public Object replyFromString(String str) {
        return genson.deserialize(str, BankInterestReply.class);
    }
}
