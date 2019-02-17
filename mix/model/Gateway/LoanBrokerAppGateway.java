package model.Gateway;

import messaging.requestreply.RequestReply;
import model.ConnectionData;
import model.Gateway.Serializer.BankSerializer;
import model.Gateway.Serializer.LoanSerializer;
import model.Gateway.Serializer.Serializer;
import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;
import model.loan.LoanReply;
import model.loan.LoanRequest;

import javax.jms.*;
import java.util.HashMap;

/**
 * Created by Niels Verheijen on 13/02/2019.
 */
public class LoanBrokerAppGateway extends AppGateway {

    private NewDataListener listener;
    private HashMap<String, LoanRequest> requestMap;
    private HashMap<String, Integer> aggregatorMap;

    private void setupMessageListener() {
        MessageListener messageListener;
        if(this.serializer instanceof LoanSerializer) {
            messageListener = new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    try {
                        LoanRequest loanRequest = requestMap.get(message.getJMSCorrelationID());
                        String textMessage = ((TextMessage) message).getText();
                        LoanReply loanreply = (LoanReply) serializer.replyFromString(textMessage);
                        onLoanReplyArrived(loanRequest, loanreply, message.getJMSCorrelationID());
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            };
        }
        else if(this.serializer instanceof BankSerializer){
            messageListener = new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    try {
                        String textMessage = ((TextMessage) message).getText();
                        BankInterestRequest bankInterestRequest = (BankInterestRequest)serializer.requestFromString(textMessage);
                        onBankRequestArrived(bankInterestRequest, message.getJMSCorrelationID(), message.getIntProperty(ConnectionData.AGGREGATOR));
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            };
        }
        else{
            return;
        }
        receiver.setListener(messageListener);
    }

    public LoanBrokerAppGateway(Serializer serializer, String senderString, String receiverString){
        super(serializer,senderString,receiverString);
        requestMap = new HashMap<>();
        aggregatorMap = new HashMap<>();
        setupMessageListener();
    }

    public void applyForLoan(LoanRequest request){
        try {
            String result = serializer.requestToString(request);
            Message msg = sender.createTextMessage(result);
            sender.send(msg);
            requestMap.put(msg.getJMSMessageID(), request);
        }
        catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void subscribeToEvent(NewDataListener listener){
        this.listener = listener;
    }

    public void onLoanReplyArrived(LoanRequest request, LoanReply reply, String Id){
        RequestReply requestReply = new RequestReply(request, reply);
        listener.newDataReceived(requestReply, Id);
    }

    public void onBankRequestArrived(BankInterestRequest bankInterestRequest, String Id, int aggregator){
        RequestReply requestReply = new RequestReply(bankInterestRequest, null);
        aggregatorMap.put(Id, Integer.valueOf(aggregator));
        listener.newDataReceived(requestReply, Id);
    }

    public void replyToRequest(BankInterestReply bankInterestReply, String Id){
        try{
        String result = serializer.replyToString(bankInterestReply);
        Message msg = sender.createTextMessage(result);
        msg.setJMSCorrelationID(Id);
        msg.setIntProperty(ConnectionData.AGGREGATOR, aggregatorMap.get(Id));
        sender.send(msg);
        }
        catch (JMSException e) {
        e.printStackTrace();
        }
    }
}
