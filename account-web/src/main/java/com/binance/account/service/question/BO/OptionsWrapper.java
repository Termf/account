package com.binance.account.service.question.BO;

import java.util.List;

import com.binance.master.commons.ToString;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class OptionsWrapper extends ToString {
	private static final long serialVersionUID = 8562569519931202276L;
	private List<String> options;
	private List<String> answers;

	public List<String> getOptions() {
		return options;
	}

	public List<String> getAnswers() {
		return answers;
	}

}
