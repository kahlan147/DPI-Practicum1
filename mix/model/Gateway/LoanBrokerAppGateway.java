package model.Gateway;

import model.ConnectionData;
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
public class LoanBrokerAppGateway extends AppGateway {

    private HashMap<String, LoanRequest> requestMap;

    public LoanBrokerAppGateway(Serializer serializer, String senderString, String receiverString){
        super(serializer,senderString,receiverString);
        requestMap = new HashMap<>();
        receiver.setListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                try {
                    LoanRequest loanRequest = requestMap.get(message.getJMSCorrelationID());
                    String textMessage = ((TextMessage)message).getText();
                    LoanReply loanreply = (LoanReply)serializer.replyFromString(textMessage);
                    onLoanReplyArrived(loanRequest,loanreply);
                }
                catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void applyForLoan(LoanRequest request){
        String result = serializer.requestToString(request);
        Message msg = sender.createTextMessage(result);
        sender.send(msg);
        try {
            requestMap.put(msg.getJMSMessageID(), request);
        }
        catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void onLoanReplyArrived(LoanRequest request, LoanReply reply){

    }
}
