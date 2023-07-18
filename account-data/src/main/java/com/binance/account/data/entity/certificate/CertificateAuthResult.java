package com.binance.account.data.entity.certificate;

import com.binance.master.commons.ToString;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CertificateAuthResult extends ToString {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2539613276333253729L;

	public static final String FACE_OCR = "FACE_OCR";
	
	public static final String JUMIO = "JUMIO";
	
	public static final String STATUS_REVIEW = "REVIEW";
    public static final String STATUS_PASS = "PASS";
    public static final String STATUS_REFUSED = "REFUSED";
    
    public static final int TYPE_USER = 1;
    public static final int TYPE_COMPANY = 2;
	
	private String soucre;
	
	private boolean newVersion;
	
	private String status;

	private boolean forbidPassed;
	
	private Integer certificateType;
}
