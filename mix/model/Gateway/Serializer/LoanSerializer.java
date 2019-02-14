package model.Gateway.Serializer;


import com.owlike.genson.Genson;
import model.loan.LoanReply;
import model.loan.LoanRequest;

/**
 * Created by Niels Verheijen on 13/02/2019.
 */
public class LoanSerializer implements Serializer {

    private Genson genson;

    public LoanSerializer(){
        genson = new Genson();
    }

    @Override
    public String requestToString(Object o) {
        return genson.serialize(o);
    }

    @Override
    public Object requestFromString(String str) {
        return genson.deserialize(str, LoanRequest.class);
    }

    @Override
    public String replyToString(Object o) {
        return genson.serialize(o);
    }

    @Override
    public Object replyFromString(String str) {
        return genson.deserialize(str, LoanReply.class);
    }
}
