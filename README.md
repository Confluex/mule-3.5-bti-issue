mule-3.5-bti-issue
==================

Demonstrates a bug when using the Bitronix Transaction Manager with JMS.  Run BitronixTransactionManagerTest to see it fail.  Run JbossTaTransactionManagerTest to see the same flow succeed.

The error I observed is:
```
	2014-07-11 20:34:53 ERROR [activeMqConnector.receiver.02] DefaultSystemExceptionStrategy:361 - Caught exception in Exception Strategy: null
	java.lang.reflect.UndeclaredThrowableException
		at com.sun.proxy.$Proxy19.receive(Unknown Source)
		at org.mule.transport.jms.XaTransactedJmsMessageReceiver.getMessages(XaTransactedJmsMessageReceiver.java:224)
		at org.mule.transport.jms.XaTransactedJmsMessageReceiver$1.process(XaTransactedJmsMessageReceiver.java:172)
		at org.mule.transport.jms.XaTransactedJmsMessageReceiver$1.process(XaTransactedJmsMessageReceiver.java:166)
		at org.mule.execution.ExecuteCallbackInterceptor.execute(ExecuteCallbackInterceptor.java:16)
		at org.mule.execution.HandleExceptionInterceptor.execute(HandleExceptionInterceptor.java:30)
		at org.mule.execution.HandleExceptionInterceptor.execute(HandleExceptionInterceptor.java:14)
		at org.mule.execution.BeginAndResolveTransactionInterceptor.execute(BeginAndResolveTransactionInterceptor.java:54)
		at org.mule.execution.ResolvePreviousTransactionInterceptor.execute(ResolvePreviousTransactionInterceptor.java:44)
		at org.mule.execution.SuspendXaTransactionInterceptor.execute(SuspendXaTransactionInterceptor.java:50)
		at org.mule.execution.ValidateTransactionalStateInterceptor.execute(ValidateTransactionalStateInterceptor.java:40)
		at org.mule.execution.IsolateCurrentTransactionInterceptor.execute(IsolateCurrentTransactionInterceptor.java:41)
		at org.mule.execution.ExternalTransactionInterceptor.execute(ExternalTransactionInterceptor.java:48)
		at org.mule.execution.RethrowExceptionInterceptor.execute(RethrowExceptionInterceptor.java:28)
		at org.mule.execution.RethrowExceptionInterceptor.execute(RethrowExceptionInterceptor.java:13)
		at org.mule.execution.TransactionalErrorHandlingExecutionTemplate.execute(TransactionalErrorHandlingExecutionTemplate.java:109)
		at org.mule.execution.TransactionalErrorHandlingExecutionTemplate.execute(TransactionalErrorHandlingExecutionTemplate.java:30)
		at org.mule.transport.jms.XaTransactedJmsMessageReceiver.poll(XaTransactedJmsMessageReceiver.java:210)
		at org.mule.transport.AbstractPollingMessageReceiver.performPoll(AbstractPollingMessageReceiver.java:216)
		at org.mule.transport.PollingReceiverWorker.poll(PollingReceiverWorker.java:80)
		at org.mule.transport.PollingReceiverWorker.run(PollingReceiverWorker.java:49)
		at org.mule.transport.TrackingWorkManager$TrackeableWork.run(TrackingWorkManager.java:267)
		at org.mule.work.WorkerContext.run(WorkerContext.java:286)
		at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1145)
		at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:615)
		at java.lang.Thread.run(Thread.java:744)
	Caused by: java.lang.reflect.InvocationTargetException
		at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
		at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
		at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
		at java.lang.reflect.Method.invoke(Method.java:606)
		at com.mulesoft.mule.bti.jms.BitronixMessageConsumerInvocationHandler.invoke(BitronixMessageConsumerInvocationHandler.java:40)
		... 26 more
```