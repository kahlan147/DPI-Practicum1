package model.Gateway;

import messaging.requestreply.RequestReply;
import model.Gateway.Serializer.Serializer;
import model.loan.LoanReply;
import model.loan.LoanRequest;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.HashMap;

/**
 * Created by Niels Verheijen on 13/02/2019.
 */
public class LoanClientAppGateway extends AppGateway{

    private NewDataListener listener;
    private HashMap<String, LoanRequest> loanRequestHashMap;

    public LoanClientAppGateway(Serializer serializer, String senderString, String receiverString){
        super(serializer,senderString,receiverString);
        loanRequestHashMap = new HashMap<>();
        receiver.setListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                try {
                    String textMessage = ((TextMessage) message).getText();
                    LoanRequest loanRequest = (LoanRequest)serializer.requestFromString(textMessage);
                    String Id = message.getJMSMessageID();
                    onLoanRequestArrived(loanRequest, Id);
                }
                catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void subscribeToEvent(NewDataListener listener){
        this.listener = listener;
    }

    private void onLoanRequestArrived(LoanRequest request, String Id){
        loanRequestHashMap.put(Id, request);
        RequestReply requestReply = new RequestReply(request, null);
        listener.newDataReceived(requestReply, Id);
    }

    public void sendLoanReply(LoanReply reply, String Id){
        try{
        String result = serializer.replyToString(reply);
        Message msg = sender.createTextMessage(result);
        msg.setJMSCorrelationID(Id);
        sender.send(msg);
        }
        catch(JMSException e){
            e.printStackTrace();
        }
    }
}
