package model.Gateway;

import model.Gateway.Serializer.BankSerializer;
import model.Gateway.Serializer.Serializer;
import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.HashMap;

/**
 * Created by Niels Verheijen on 13/02/2019.
 */
public class BankAppGateway extends AppGateway{

    private HashMap<String, BankInterestRequest> bankInterestRequestHashMap;

    public BankAppGateway(Serializer serializer, String senderString, String receiverString){
        super(serializer,senderString,receiverString);
        bankInterestRequestHashMap = new HashMap<>();
        receiver.setListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                try {
                    BankInterestRequest bankInterestRequest = bankInterestRequestHashMap.get(message.getJMSCorrelationID());
                    String textMessage = ((TextMessage) message).getText();
                    BankInterestReply bankInterestReply = (BankInterestReply) serializer.replyFromString(textMessage);
                    onBankReplyArrived(bankInterestReply, bankInterestRequest);
                }
                catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onBankReplyArrived(BankInterestReply reply, BankInterestRequest request){

    }

    public void sendBankRequest(BankInterestRequest request, String Id){
        try {
            String result = serializer.requestToString(request);
            Message msg = sender.createTextMessage(result);
            msg.setJMSCorrelationID(Id);
            sender.send(msg);
            bankInterestRequestHashMap.put(Id,request);
        }
        catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
