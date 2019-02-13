package model.Gateway.Serializer;

import java.io.Serializable;

/**
 * Created by Niels Verheijen on 13/02/2019.
*/
public interface Serializer<REQUEST,REPLY> {

    String requestToString(REQUEST request);
    REQUEST requestFromString(String str);
    String replyToString(REPLY reply);
    REPLY replyFromString(String str);
}
