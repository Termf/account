package com.binance.account.common.query;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by liwei on 2018/09/07.
 */
@Getter
@Setter
@ToString
public class CompanyCertificateQuery extends JumioQuery {

	private String companyName;

	private String companyCountry;

	private String applyerName;

}
