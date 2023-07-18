package com.binance.account;

import org.junit.Test;

import com.binance.master.utils.HashAlgorithms;

public class PartitionCalculation {
    
    private final static int TABLE_SIZE = 20;
    
    /**
     * 别继续删我的代码！！！
     */
    @Test
    public void StringPartitionCalculation() {
        String email = new String("08035057337");
        int index = Math.abs(HashAlgorithms.FNVHash1(email) % TABLE_SIZE);
        System.out.println("分区:"+index);
    }

    
}
