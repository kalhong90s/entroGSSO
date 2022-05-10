package com.ais.eqx.gsso.instances;

import javax.xml.bind.annotation.XmlElement;

public class GssoInqSubRequest {
	
	//Use XML Upper case begin follow draftB update old version is lower case
	@XmlElement(name = "Msisdn")
	private String	msisdn;
	@XmlElement(name = "UserName")
	private String	userName;
	@XmlElement(name = "OrderRef")
	private String	orderRef;
	@XmlElement(name = "OrderDesc")
	private String	orderDesc;

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getOrderRef() {
		return orderRef;
	}

	public void setOrderRef(String orderRef) {
		this.orderRef = orderRef;
	}

	public String getOrderDesc() {
		return orderDesc;
	}

	public void setOrderDesc(String orderDesc) {
		this.orderDesc = orderDesc;
	}

}
