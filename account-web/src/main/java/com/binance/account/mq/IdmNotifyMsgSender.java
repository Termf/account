package com.binance.account.mq;

import javax.annotation.Resource;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.binance.inspector.vo.idm.request.BaseInfoKycIdmRequest;
import com.binance.inspector.vo.idm.request.IdmNotifyMqVo;
import com.binance.inspector.vo.idm.request.IdmNotifyTypeEnum;

@Service
public class IdmNotifyMsgSender {
	@Resource
	private RabbitTemplate rabbitTemplate;

	public void notifyJumioIdm(BaseInfoKycIdmRequest request) {
		IdmNotifyMqVo vo = new IdmNotifyMqVo();
		vo.setRequest(request);
		vo.setType(IdmNotifyTypeEnum.JUMIO);
		notifyMq(vo);
	}

	public void notifyAddress(BaseInfoKycIdmRequest request) {
		IdmNotifyMqVo vo = new IdmNotifyMqVo();
		vo.setRequest(request);
		vo.setType(IdmNotifyTypeEnum.ADDRESS);
		notifyMq(vo);
	}

	public void notifyAddressTag(BaseInfoKycIdmRequest request) {
		IdmNotifyMqVo vo = new IdmNotifyMqVo();
		vo.setRequest(request);
		vo.setType(IdmNotifyTypeEnum.ADDRESS_TAG);
		notifyMq(vo);
	}

	public void notifyFace(BaseInfoKycIdmRequest request) {
		IdmNotifyMqVo vo = new IdmNotifyMqVo();
		vo.setRequest(request);
		vo.setType(IdmNotifyTypeEnum.FACE);
		notifyMq(vo);
	}

	public void notifyIdmReject(BaseInfoKycIdmRequest request) {
		IdmNotifyMqVo vo = new IdmNotifyMqVo();
		vo.setRequest(request);
		vo.setType(IdmNotifyTypeEnum.IDM_REJECT);
		notifyMq(vo);
	}
	
	public void notifyIdmAccept(BaseInfoKycIdmRequest request) {
		IdmNotifyMqVo vo = new IdmNotifyMqVo();
		vo.setRequest(request);
		vo.setType(IdmNotifyTypeEnum.IDM_ACCEPT);
		notifyMq(vo);
	}

	public void notifyMq(IdmNotifyMqVo vo) {
		rabbitTemplate.convertAndSend("account.idm.notify", null, vo);
	}
}
