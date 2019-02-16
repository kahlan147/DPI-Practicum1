package model.Gateway;

import messaging.requestreply.RequestReply;

import java.util.EventListener;

/**
 * Created by Niels Verheijen on 14/02/2019.
 */
public interface NewDataListener extends EventListener {

    void newDataReceived(RequestReply requestReply, String Id);
}
