package com.confluex.mule.test;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.sql.DataSource;

import org.apache.activemq.ActiveMQXAConnectionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.api.MuleContext;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;
import org.springframework.jms.core.JmsTemplate;

import com.confluex.mule.test.event.BlockingEndpointListener;
import com.confluex.mule.test.event.BlockingMessageProcessorListener;
import com.confluex.mule.test.event.BlockingTransactionListener;


public abstract class BaseTransactionManagerTest extends BetterFunctionalTestCase {

    Logger log = LoggerFactory.getLogger(getClass());
    
    private ActiveMQXAConnectionFactory amqConnectionFactory;

    private JdbcTemplate jdbc;
    private JmsTemplate jms;
    private JmsTemplate jmsAdmin;
    private Connection keepaliveConnection;
    private java.sql.Connection keepaliveSqlConnection;
    

    @Before
    public void initJdbc() throws SQLException {
        DataSource dataSource = muleContext.getRegistry().lookupObject("dataSource");
        
        jdbc = new JdbcTemplate(dataSource);
        keepaliveSqlConnection = dataSource.getConnection();
    }
    
    @BeforeMule
    public void initJms(MuleContext muleContext) throws JMSException {
        amqConnectionFactory = muleContext.getRegistry().lookupObject("amqConnectionFactory");

        UserCredentialsConnectionFactoryAdapter adminConnectionFactory = new UserCredentialsConnectionFactoryAdapter();
        log.debug("Setting up admin connection factory " + amqConnectionFactory.getBrokerURL());
        adminConnectionFactory.setTargetConnectionFactory(amqConnectionFactory);
        adminConnectionFactory.setUsername("god");
        adminConnectionFactory.setPassword("password");

        jmsAdmin = new JmsTemplate(adminConnectionFactory);
        jmsAdmin.setReceiveTimeout(100);
        
        jms = new JmsTemplate(amqConnectionFactory);
        jms.setReceiveTimeout(5000);
        keepaliveConnection = amqConnectionFactory.createConnection();
        
        createQueue("input");
        createQueue("DLQ.input");
    }
    
    @After
    public void stopKeepaliveConnections() throws JMSException, SQLException {
        keepaliveConnection.close();
        keepaliveSqlConnection.close();
    }
    
    @Test
    public void messageShouldMakeItToDatabaseAndJmsQueue() throws Exception {
        createQueue("output");
        BlockingEndpointListener listener = listenForEndpoint("databaseInsertEndpoint");
        
        jms.convertAndSend("input", "test data");
        
        String publishedToJms = (String) jms.receiveAndConvert("output");
        assertNotNull("Expected message not found on queue \"output\"", publishedToJms);
        assertEquals("test data", publishedToJms);
        
        assertTrue("Did not detect expected message sent to databaseInsertEndpoint", listener.waitForMessages());
        Thread.sleep(1000);
        
        List<Map<String, Object>> insertedIntoDatabase = jdbc.queryForList("SELECT data FROM test_table");
        assertNotNull("Expected row not found in database table \"test_table\"", insertedIntoDatabase);
        assertEquals("test data", insertedIntoDatabase.get(0).get("data"));
    }
    
    @Test
    public void messageShouldNotMakeItToDatabase_NorBeConsumed_WhenOutboundQueueDoesNotExist() {
        BlockingMessageProcessorListener txidListener = listenForMessageProcessor("txidCapture");
        BlockingTransactionListener txListener = listenForTransaction();

        jms.convertAndSend("input", "test data");
        
        assertTrue("Did not detect expected message on txidCapture message processor", txidListener.waitForMessages());
        String transactionId = txidListener.getMessages().get(0).getProperty("txid", PropertyScope.OUTBOUND);
        assertNotNull("Did not find txid message property", transactionId);
        assertTrue("Did not detect expected rollback event for transaction " + transactionId, txListener.waitForRollback(transactionId, 1000));
        
        assertEquals("Number of rows in the database", 0, jdbc.queryForList("SELECT data FROM test_table").size());
        String deadLetterQueueMessage = (String) jms.receiveAndConvert("DLQ.input");
        assertEquals("Message on dead letter queue", "test data", deadLetterQueueMessage);
    }
    
    private void createQueue(String queue) {
        jmsAdmin.receive(queue);
    }
}
