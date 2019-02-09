package loanbroker;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Properties;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import messaging.requestreply.RequestReply;
import model.bank.*;
import model.loan.LoanReply;
import model.loan.LoanRequest;
import model.ConnectionData;


public class LoanBrokerFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private DefaultListModel<JListLine> listModel = new DefaultListModel<JListLine>();
	private JList<JListLine> list;

	private HashMap<String, RequestReply> RequestReplyHashmap; //Bind the messageID of the message to a RequestReply
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoanBrokerFrame frame = new LoanBrokerFrame();
					frame.setVisible(true);
					frame.PrepareToReceiveMessagesFromClient();
					frame.PrepareToReceiveMessagesFromBank();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Implemented by Niels Verheijen.
	 * Handles messages received from clients, redirecting them to the bank.
	 */
	private void PrepareToReceiveMessagesFromClient(){
		ConnectionData.PrepareToReceiveMessages(ConnectionData.CLIENTTOBROKER, new MessageListener() {
				@Override
				public void onMessage(Message msg) {
					try {
						RequestReply requestReply = (RequestReply)((ObjectMessage)msg).getObject();
						LoanRequest loanRequest = (LoanRequest)requestReply.getRequest();
						BankInterestRequest bankInterestRequest = new BankInterestRequest(loanRequest.getAmount(),loanRequest.getTime()); //Convert loanrequest to bankrequest
						RequestReply newRequestReply = new RequestReply(bankInterestRequest, null);
						add(loanRequest); //Show the data on the frame
						ConnectionData.SendMessage(ConnectionData.BROKERTOBANK, newRequestReply, msg.getJMSMessageID()); //Send the data to the bank and return the uID belonging to this message.
						RequestReplyHashmap.put(msg.getJMSMessageID(), requestReply); //Put the ID of the request and the RequestReply belonging to it in map, allowing for easier find.
					}
					catch(JMSException e){
						e.printStackTrace();
					}
				}
			});

	}

	/**
	 * Implemented by Niels Verheijen.
	 * Handles messages received from a bank, redirecting them to the client.
	 */
	private void PrepareToReceiveMessagesFromBank(){
		ConnectionData.PrepareToReceiveMessages(ConnectionData.BANKTOBROKER, new MessageListener() {
				@Override
				public void onMessage(Message msg) {
					try {
						RequestReply requestReply = (RequestReply)((ObjectMessage)msg).getObject();
						BankInterestReply bankInterestReply = (BankInterestReply) requestReply.getReply();
						String ID = msg.getJMSCorrelationID(); //Take the ID belonging to the client from the message
						RequestReply rr = RequestReplyHashmap.get(ID); //Acquire the RequestReply object belonging to the client's ID
						LoanReply loanReply = new LoanReply(bankInterestReply.getInterest(), bankInterestReply.getQuoteId());
						rr.setReply(loanReply); //Add the reply to the RequestReply
						add(((LoanRequest)rr.getRequest()), bankInterestReply); //Show the requestreply data on the frame
						ConnectionData.SendMessage(ConnectionData.BROKERTOCLIENT, rr, ID); //Send message back to the client
					}
					catch(JMSException e){
						e.printStackTrace();
					}
				}
			});
		}


	/**
	 * Create the frame.
	 */
	public LoanBrokerFrame() {
		setTitle("Loan Broker");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{46, 31, 86, 30, 89, 0};
		gbl_contentPane.rowHeights = new int[]{233, 23, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 7;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		contentPane.add(scrollPane, gbc_scrollPane);
		
		list = new JList<JListLine>(listModel);
		scrollPane.setViewportView(list);
		RequestReplyHashmap = new HashMap<>();
	}
	
	 private JListLine getRequestReply(LoanRequest request){    
	     
	     for (int i = 0; i < listModel.getSize(); i++){
	    	 JListLine rr =listModel.get(i);
	    	 if (rr.getLoanRequest() == request){
	    		 return rr;
	    	 }
	     }
	     return null;
	   }
	
	public void add(LoanRequest loanRequest){		
		listModel.addElement(new JListLine(loanRequest));		
	}

	public void add(LoanRequest loanRequest,BankInterestRequest bankRequest){
		JListLine rr = getRequestReply(loanRequest);
		if (rr!= null && bankRequest != null){
			rr.setBankRequest(bankRequest);
            list.repaint();
		}		
	}
	
	public void add(LoanRequest loanRequest, BankInterestReply bankReply){
		JListLine rr = getRequestReply(loanRequest);
		if (rr!= null && bankReply != null){
			rr.setBankReply(bankReply);
            list.repaint();
		}		
	}


}
