package model.Gateway;

import messaging.requestreply.RequestReply;
import model.ConnectionData;
import model.Gateway.Serializer.Serializer;
import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;
import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.HashMap;


/**
 * Created by Niels Verheijen on 13/02/2019.
 */
public class BankAppGateway extends AppGateway{

    private int lowestInterest;
    private int highestInterest;
    private int maxLoanTime;

    private BankGatewayManager bankGatewayManager;
    private HashMap<String, BankInterestRequest> bankInterestRequestHashMap;

    public BankAppGateway(Serializer serializer, String senderString, String receiverString, int lowestInterest, int highestInterest, int maxLoanTime){
        super(serializer,senderString,receiverString);
        this.lowestInterest = lowestInterest;
        this.highestInterest = highestInterest;
        this.maxLoanTime = maxLoanTime;
        bankInterestRequestHashMap = new HashMap<>();
        receiver.setListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                try {
                    BankInterestRequest bankInterestRequest = bankInterestRequestHashMap.get(message.getJMSCorrelationID());
                    String textMessage = ((TextMessage) message).getText();
                    BankInterestReply bankInterestReply = (BankInterestReply) serializer.replyFromString(textMessage);
                    onBankReplyArrived(bankInterestReply, bankInterestRequest, message.getJMSCorrelationID(), message.getIntProperty(ConnectionData.AGGREGATOR));
                }
                catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onBankReplyArrived(BankInterestReply reply, BankInterestRequest request, String id, int aggregator){
        RequestReply requestReply = new RequestReply(request, reply);
        bankGatewayManager.newDataReceived(requestReply, id, aggregator);
    }

    public void setBankGatewayManager(BankGatewayManager bankGatewayManager){
        this.bankGatewayManager = bankGatewayManager;
    }

    public boolean isInterestedInRequest(int loan, int loanTime){
        try {
            String checkString = "#{amount} >= " + lowestInterest + " && #{amount} <= " + highestInterest + " && #{time} <= " + maxLoanTime;
            Evaluator evaluator = new Evaluator();
            evaluator.putVariable("amount", Integer.toString(loan));
            evaluator.putVariable("time", Integer.toString(loanTime));
            return evaluator.evaluate(checkString).equals("1.0");
        }
        catch (EvaluationException e) {
            e.printStackTrace();
        }
        return false;
        //return(loan >= lowestInterest && loan <= highestInterest && loanTime <= maxLoanTime);
    }

    public void sendBankRequest(BankInterestRequest request, String Id, int Aggregator){
        try {
            String result = serializer.requestToString(request);
            Message msg = sender.createTextMessage(result);
            msg.setJMSCorrelationID(Id);
            msg.setIntProperty(ConnectionData.AGGREGATOR, Aggregator);
            sender.send(msg);
            bankInterestRequestHashMap.put(Id,request);
        }
        catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
