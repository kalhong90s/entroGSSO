package com.ais.eqx.gsso.instances;

public class PortCheckReq {

	String	value;

	String	UserName;

	String	OrderRef;

	String	OrderDesc;

	String	Msisdn;

	public String getUserName() {
		return UserName;
	}

	public void setUserName(final String userName) {
		UserName = userName;
	}

	public String getOrderRef() {
		return OrderRef;
	}

	public void setOrderRef(final String orderRef) {
		OrderRef = orderRef;
	}

	public String getOrderDesc() {
		return OrderDesc;
	}

	public void setOrderDesc(final String orderDesc) {
		OrderDesc = orderDesc;
	}

	public String getMsisdn() {
		return Msisdn;
	}

	public void setMsisdn(final String msisdn) {
		Msisdn = msisdn;
	}

}
