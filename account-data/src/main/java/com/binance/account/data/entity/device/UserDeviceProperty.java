package com.binance.account.data.entity.device;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class UserDeviceProperty implements Serializable {
    private static final long serialVersionUID = 7658532087081303453L;
    private Long id;

    private Byte status;

    private String agentType;

    private String propertyName;

    private String propertyKey;

    private String propertyRule;

    private Integer propertyWeight = 1;

    private Date createTime;

    private Date updateTime;

    private Byte isDel;

    public enum STATUS{
        OPEN((byte)1), CLOSE((byte)4);
        private byte code;
        STATUS(byte code){
            this.code = code;
        }

        public byte getCode(){
            return this.code;
        }


    }

}