package com.binance.account.common.constant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileInfo {
    private byte[] data;
    private String ext;

    public FileInfo(byte[] data, String ext) {
        this.data = data;
        if ("image/png".equalsIgnoreCase(ext)) {
            this.ext = ".png";
        } else {
            this.ext = ".jpg";
        }
    }

}
