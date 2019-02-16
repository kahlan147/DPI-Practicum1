package model.Gateway;

import model.ConnectionData;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

/**
 * Created by Niels Verheijen on 13/02/2019.
 */
public class MessageReceiverGateway {

    private Connection connection;
    private Session session;
    private Destination destination;
    private MessageConsumer consumer = null;


    public MessageReceiverGateway(String channelName){
        try {
            Properties props = new Properties();
            props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
            props.put(("queue." + channelName), channelName);

            Context jndiContext = new InitialContext(props);
            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");
            ((ActiveMQConnectionFactory) connectionFactory).setTrustAllPackages(true); //Allow ActiveMQ to trust all package (possibly change later to only allow certain classes. (setTrustPackages(<Class>))
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            // connect to the receiver destination
            destination = (Destination) jndiContext.lookup(channelName);
            consumer = session.createConsumer(destination); //Create a consumer
            connection.start(); // this is needed to start receiving messages
        }
        catch(NamingException | JMSException e){
            e.printStackTrace();
        }
    }

    public void setListener(MessageListener messageListener){
        try {
            consumer.setMessageListener(messageListener); //Sets the messagelistener event.
        }
        catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
