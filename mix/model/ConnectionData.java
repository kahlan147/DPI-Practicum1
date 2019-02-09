package model;

import messaging.requestreply.RequestReply;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;



public class ConnectionData {
    public static final String CLIENTTOBROKER = "ClientToBroker";
    public static final String BROKERTOBANK = "BrokerToBank";
    public static final String BROKERTOCLIENT = "BrokerToClient";
    public static final String BANKTOBROKER = "BankToBroker";

    public Connection connection; // to connect to the JMS
    public Session session; // session for creating consumers
    public Destination destination; //reference to a queue/topic destination

    public ConnectionData(String name){
		try {
            Properties props = new Properties();
            props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
            props.put(("queue." + name), name);

            Context jndiContext = new InitialContext(props);
            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");
            ((ActiveMQConnectionFactory) connectionFactory).setTrustAllPackages(true); //Allow ActiveMQ to trust all package (possibly change later to only allow certain classes. (setTrustPackages(<Class>))
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // connect to the receiver destination
            destination = (Destination) jndiContext.lookup(name);
        }
        catch(NamingException | JMSException e){
		    e.printStackTrace();
        }
    }

    /**
     * Implemented by Niels Verheijen
     * Sends messages through the given channel, adding requestreplies and an ID to them.
     * @param channel String belonging to a channel.
     * @param requestReply Object to send to the receiver.
     * @param ID An Id to be added to the message. Allowed to be null.
     * @return returns the messageID of the sent message.
     */
    public static String SendMessage(String channel, RequestReply requestReply, String ID){
        Session session;
        Destination sendDestination;
        MessageProducer producer;

        try{
            ConnectionData connectionData = new ConnectionData(channel);
            session = connectionData.session;
            sendDestination = connectionData.destination;
            producer = session.createProducer(sendDestination); //Create a producer

            Message msg = session.createObjectMessage(requestReply); //Adds the requestreply to the message
            if(ID != null){
                    msg.setJMSCorrelationID(ID); //If an ID has been given, add it as the correlationID
            }
            producer.send(msg); //Sends the message
            return msg.getJMSMessageID(); //Return the ID of the messageID
        }
        catch(JMSException e){
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Sets up a connection, awaiting for messages.
     * @param channel String belonging to a channel.
     * @param messageListener MessageListener object indicating what needs to happen after a message is received.
     */
    public static void PrepareToReceiveMessages(String channel, MessageListener messageListener){
        Connection connection;
        Session session;
        Destination receiveDestination;
        MessageConsumer consumer = null;
        try {
            ConnectionData connectionData = new ConnectionData(channel);
            connection = connectionData.connection;
            session = connectionData.session;
            receiveDestination = connectionData.destination;
            consumer = session.createConsumer(receiveDestination); //Create a consumer
            connection.start(); // this is needed to start receiving messages
            consumer.setMessageListener(messageListener); //Sets the messagelistener event.
        }
        catch (JMSException e) {
            e.printStackTrace();
        }
    }

}
