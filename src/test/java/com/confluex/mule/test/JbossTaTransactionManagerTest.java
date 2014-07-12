package com.confluex.mule.test;

public class JbossTaTransactionManagerTest extends BaseTransactionManagerTest {

    public String getConfigFile() {
        return "using-jbossta.xml";
    }

}
