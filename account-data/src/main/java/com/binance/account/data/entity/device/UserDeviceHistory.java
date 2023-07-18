package com.binance.account.data.entity.device;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class UserDeviceHistory implements Serializable {

    public enum OperateType{
        ADD((byte)1), MODIFY((byte)2), REMOVE((byte)4), LOG((byte) 8), AUTHORIZE((byte) 16);
        private byte code;
        OperateType(byte code){
            this.code = code;
        }

        public byte getCode(){
            return this.code;
        }
    }

    private static final long serialVersionUID = -2364338084748376825L;

    private Long id;

    private Long userId;

    private Long userDeviceId;

    private String agentType;

    private Byte operateType;

    private String content;

    private String memo;

    private Date createTime;

    public UserDeviceHistory(){}

    public UserDeviceHistory(UserDevice device){
        this.userId = device.getUserId();
        this.userDeviceId = device.getId();
        this.agentType = device.getAgentType();
        this.content = device.getContent();
    }
}
