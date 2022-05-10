package com.ais.eqx.gsso.instances;

public class DeliveryReportLog {

	private String	dateTime;
	private String	msisdn;
	private String	serviceName;
	private String	serviceKey;
	private String	transactionID;
	private String	orderRef;

	private String	messageId;
	private String	smResultCode;
	private String	smErrorMessage;
	private String	smResponseTime	= "0";

	private String	drResultCode;
	private String	drErrorMessage;
	private String	drResponseTime	= "0";
	private String	drReport;

	private String	responseTime;

	public String getDateTime() {
		return dateTime;
	}

	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getServiceKey() {
		return serviceKey;
	}

	public void setServiceKey(String serviceKey) {
		this.serviceKey = serviceKey;
	}

	public String getTransactionID() {
		return transactionID;
	}

	public void setTransactionID(String transactionID) {
		this.transactionID = transactionID;
	}

	public String getOrderRef() {
		return orderRef;
	}

	public void setOrderRef(String orderRef) {
		this.orderRef = orderRef;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getSmResultCode() {
		return smResultCode;
	}

	public void setSmResultCode(String smResultCode) {
		this.smResultCode = smResultCode;
	}

	public String getSmErrorMessage() {
		return smErrorMessage;
	}

	public void setSmErrorMessage(String smErrorMessage) {
		this.smErrorMessage = smErrorMessage;
	}

	public String getSmResponseTime() {
		return smResponseTime;
	}

	public void setSmResponseTime(String smResponseTime) {
		this.smResponseTime = smResponseTime;
	}

	public String getDrResultCode() {
		return drResultCode;
	}

	public void setDrResultCode(String drResultCode) {
		this.drResultCode = drResultCode;
	}

	public String getDrErrorMessage() {
		return drErrorMessage;
	}

	public void setDrErrorMessage(String drErrorMessage) {
		this.drErrorMessage = drErrorMessage;
	}

	public String getDrResponseTime() {
		return drResponseTime;
	}

	public void setDrResponseTime(String drResponseTime) {
		this.drResponseTime = drResponseTime;
	}

	public String getDrReport() {
		return drReport;
	}

	public void setDrReport(String drReport) {
		this.drReport = drReport;
	}

	public String getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(String responseTime) {
		this.responseTime = responseTime;
	}

	@Override
	public String toString() {

		String logOutput = getDateTime() + "|" + getMsisdn() + "|" + getServiceName() + "|" + getServiceKey() + "|"
				+ getTransactionID() + "|" + getOrderRef() + "|" + getMessageId() + "|" + getSmResultCode() + "|"
				+ getSmErrorMessage() + "|" + getSmResponseTime() + "|" + getDrResultCode() + "|" + getDrErrorMessage() + "|"
				+ getDrResponseTime() + "|" + getDrReport() + "|" + getResponseTime();

		return logOutput;
	}
	
	public String toStringXML() {

		String logOutput = "<DeliveryReport>"
				+ "<DateTime>"+getDateTime()+"</DateTime>"
				+ "<MSISDN>"+getMsisdn()+"</MSISDN>"
				+ "<ServiceName>"+getServiceName()+"</ServiceName>"
				+ "<ServiceKey>"+getServiceKey()+"</ServiceKey>"
				+ "<TransactionID>"+getTransactionID()+"</TransactionID>"
				+ "<OrderRef>"+getOrderRef()+"</OrderRef>"
				+ "<MessageId>"+getMessageId()+"</MessageId>"
				+ "<SM_ResultCode>"+getSmResultCode()+"</SM_ResultCode>"
				+ "<SM_ErrorMessage>"+getSmErrorMessage()+"</SM_ErrorMessage>"
				+ "<SM_ResponseTime>"+getSmResponseTime()+"</SM_ResponseTime>"
				+ "<DR_ResultCode>"+getDrResultCode()+"</DR_ResultCode>"
				+ "<DR_ErrorMessage>"+getDrErrorMessage()+"</DR_ErrorMessage>"
				+ "<DR_ResponseTime>"+getDrResponseTime()+"</DR_ResponseTime>"
				+ "<DR_Report>"+getDrReport()+"</DR_Report>"
				+ "<ResponseTime>"+getResponseTime()+"</ResponseTime>"
				+ "</DeliveryReport>";
				
		return logOutput;
	}

}
