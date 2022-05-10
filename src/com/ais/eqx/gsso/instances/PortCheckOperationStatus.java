package com.ais.eqx.gsso.instances;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "OperationStatus")
public class PortCheckOperationStatus {

	@XmlElement(name = "IsSuccess")
	String	isSuccess;

	@XmlElement(name = "Code")
	String	code;

	@XmlElement(name = "Description")
	String	description;

	@XmlElement(name = "TransactionId")
	String	transactionID;

	@XmlElement(name = "OrderRef")
	String	orderRef;

	public String getIsSuccess() {
		return isSuccess;
	}

	public void setIsSuccess(final String isSuccess) {
		this.isSuccess = isSuccess;
	}

	public String getCode() {
		return code;
	}

	public void setCode(final String code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public String getTransactionID() {
		return transactionID;
	}

	public void setTransactionID(final String transactionID) {
		this.transactionID = transactionID;
	}

	public String getOrderRef() {
		return orderRef;
	}

	public void setOrderRef(final String orderRef) {
		this.orderRef = orderRef;
	}

}
