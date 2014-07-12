package com.confluex.mule.test;

public class BitronixTransactionManagerTest extends BaseTransactionManagerTest {

    public String getConfigFile() {
        return "using-bti.xml";
    }
}
