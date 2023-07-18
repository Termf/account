package com.binance.account.service.file;

public interface IFileStorage {

    void save(byte[] content, String objKey) throws Exception;

    byte[] load(String objKey) throws Exception;

}
