package loanbroker;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import messaging.requestreply.RequestReply;
import model.Gateway.BankAppGateway;
import model.Gateway.BankGatewayManager;
import model.Gateway.LoanClientAppGateway;
import model.Gateway.NewDataListener;
import model.Gateway.Serializer.BankSerializer;
import model.Gateway.Serializer.LoanSerializer;
import model.bank.*;
import model.loan.LoanReply;
import model.loan.LoanRequest;
import model.ConnectionData;


public class LoanBrokerFrame extends JFrame implements NewDataListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private DefaultListModel<JListLine> listModel = new DefaultListModel<JListLine>();
	private JList<JListLine> list;

	private HashMap<String, LoanRequest> loanRequestHashMap; //Bind the messageID of the message to a RequestReply

	private BankGatewayManager bankGatewayManager;
	private LoanClientAppGateway loanClientAppGateway;
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoanBrokerFrame frame = new LoanBrokerFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void newDataReceived(RequestReply requestReply, String Id){
		if(requestReply.getRequest() instanceof LoanRequest) {
			LoanRequest loanRequest = (LoanRequest) requestReply.getRequest();
			add(loanRequest);
			loanRequestHashMap.put(Id, loanRequest);
			BankInterestRequest bankInterestRequest = new BankInterestRequest(loanRequest.getAmount(), loanRequest.getTime());
			bankGatewayManager.sendMessage(bankInterestRequest, Id);
		}
		else if(requestReply.getReply() != null && requestReply.getReply() instanceof BankInterestReply){
			BankInterestReply bankInterestReply = (BankInterestReply) requestReply.getReply();
			LoanRequest loanRequest = loanRequestHashMap.get(Id);
			add(loanRequest,bankInterestReply);
			LoanReply loanReply = new LoanReply(bankInterestReply.getInterest(), bankInterestReply.getQuoteId());
			loanClientAppGateway.sendLoanReply(loanReply, Id);
		}
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
		loanClientAppGateway = new LoanClientAppGateway(new LoanSerializer(), ConnectionData.BROKERTOCLIENT, ConnectionData.CLIENTTOBROKER);
		loanRequestHashMap = new HashMap<>();
		loanClientAppGateway.subscribeToEvent(this);
		bankGatewayManager = new BankGatewayManager();
		bankGatewayManager.subscribeToEvent(this);
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
