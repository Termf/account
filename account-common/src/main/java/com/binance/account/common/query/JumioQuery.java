package com.binance.account.common.query;

import com.binance.account.common.enums.JumioType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * Created by liwei on 2018/5/22.
 */
@Getter
@Setter
@ToString
public class JumioQuery extends BaseQuery {

	private static final long serialVersionUID = 27196112000877001L;

	private String firstName;

    private String lastName;

    private String country;

    private String issuingCountry;

	private String documentType;

	private String number;

	private String source;

	private String scanReference;

	private JumioType jumioType;

	private String faceStatus;

}
