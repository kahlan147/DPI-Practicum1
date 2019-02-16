package model.Gateway;

import messaging.requestreply.RequestReply;
import model.ConnectionData;
import model.Gateway.Serializer.BankSerializer;
import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Niels Verheijen on 16/02/2019.
 */
public class BankGatewayManager implements NewDataListener{


    private List<BankAppGateway> banks;

    private HashMap<String, List<BankOffer>> bankOfferMap;
    private NewDataListener listener;

    private class BankOffer{
        public RequestReply requestReply;
    }

    public BankGatewayManager(){
        banks = new ArrayList<>();
        bankOfferMap = new HashMap<>();
        BankAppGateway INGBank = new BankAppGateway(new BankSerializer(), ConnectionData.BROKERTOING, ConnectionData.INGTOBROKER, 0, 100000);
        INGBank.subscribeToEvent(this);
        banks.add(INGBank);
        BankAppGateway RaboBank = new BankAppGateway(new BankSerializer(), ConnectionData.BROKERTORABO, ConnectionData.RABOTOBROKER, 0, 250000);
        RaboBank.subscribeToEvent(this);
        banks.add(RaboBank);
        BankAppGateway ABNAMROBank = new BankAppGateway(new BankSerializer(), ConnectionData.BROKERTOABN, ConnectionData.ABNTOBROKER, 200000, 300000);
        ABNAMROBank.subscribeToEvent(this);
        banks.add(ABNAMROBank);
    }

    public void subscribeToEvent(NewDataListener listener){
        this.listener = listener;
    }

    public void sendMessage(BankInterestRequest bankInterestRequest, String Id){
        System.out.println("1");
        int addedId = -1;
        List<BankOffer> interestedInOffer = new ArrayList<>();
        for(BankAppGateway bankAppGateway : banks){
            if(bankAppGateway.isInterestedInRequest(bankInterestRequest.getAmount())){
                addedId++;
                String newId = Id + addedId;
                bankAppGateway.sendBankRequest(bankInterestRequest, newId);
                interestedInOffer.add(new BankOffer());
                System.out.println("2 " + addedId);
                System.out.println("Bankoffer size " + bankOfferMap.size());
            }
        }
        if(interestedInOffer.size() != 0){
            bankOfferMap.put(Id, interestedInOffer);
        }
        else{
            RequestReply requestReply = new RequestReply(bankInterestRequest, new BankInterestReply(0, " No bank has any interest"));
            listener.newDataReceived(requestReply, Id);
        }
    }

    @Override
    public void newDataReceived(RequestReply requestReply, String Id) {
        int IdSize = Id.length();
        String originalId = Id.substring(0, IdSize-1);
        int bankIdentifier = Integer.parseInt(Id.substring(IdSize-1, IdSize));
        bankOfferMap.get(originalId).get(bankIdentifier).requestReply = requestReply;
        checkForAllBanksReplied(originalId);
    }

    private void checkForAllBanksReplied(String Id){
        for(BankOffer bankOffer : bankOfferMap.get(Id)){
            if(bankOffer.requestReply == null){
                return;
            }
        }
        BankOffer currentBestInterest = null;
        for(BankOffer bankOffer : bankOfferMap.get(Id)){
            BankInterestReply bankOfferInterest = (BankInterestReply)bankOffer.requestReply.getReply();
            if(currentBestInterest == null || Double.compare(((BankInterestReply)currentBestInterest.requestReply.getReply()).getInterest(), bankOfferInterest.getInterest()) > 0) {
                currentBestInterest = bankOffer;
            }
        }
        listener.newDataReceived(currentBestInterest.requestReply, Id);
    }


}
