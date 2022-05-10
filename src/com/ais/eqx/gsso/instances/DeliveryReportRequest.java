package com.ais.eqx.gsso.instances;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.persistence.oxm.annotations.XmlPath;

@XmlRootElement(name = "mandatory")
@XmlAccessorType(XmlAccessType.FIELD)
public class DeliveryReportRequest {

	@XmlPath("service_type/@value")
	private String	serviceType;

	@XmlPath("source_addr_ton/@value")
	private String	sourceAddrTon;

	@XmlPath("source_addr_npi/@value")
	private String	sourceAddrNpi;

	@XmlPath("source_addr/@value")
	private String	sourceAddr;

	@XmlPath("dest_addr_ton/@value")
	private String	destAddrTon;

	@XmlPath("dest_addr_npi/@value")
	private String	destAddrNpi;

	@XmlPath("destination_addr/@value")
	private String	destinationAddr;

	@XmlPath("esm_class/MessagingMode/@value")
	private String	emmessagingMode;

	@XmlPath("esm_class/MessageType/@value")
	private String	emmessageType;

	@XmlPath("esm_class/GSMNetworkSpecificFeatures/@value")
	private String	emgsmNetworkSpecificFeatures;

	@XmlPath("protocol_id/@value")
	private String	protocolId;

	@XmlPath("priority_flag/@value")
	private String	priorityFlag;

	@XmlPath("schedule_delivery_time/@value")
	private String	scheduleDeliveryTime;

	@XmlPath("validity_period/@value")
	private String	validityPeriod;

	@XmlPath("registered_delivery/SMSCDeliveryReceipt/@value")
	private String	rdsmsCDeliveryReceipt;

	@XmlPath("registered_delivery/SMEOriginatedAck/@value")
	private String	rdsmeOriginatedAck;

	@XmlPath("registered_delivery/IntermediateNotification/@value")
	private String	rdintermediateNotification;

	@XmlPath("replace_if_present_flag/@value")
	private String	replaceIfPresentFlag;

	@XmlPath("data_coding/@value")
	private String	dataCoding;

	@XmlPath("sm_default_msg_id/@value")
	private String	smDefaultMsgId;

	@XmlPath("sm_length/@value")
	private String	smLength;

	@XmlPath("short_message/@value")
	private String	shortMessage;

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public String getSourceAddrTon() {
		return sourceAddrTon;
	}

	public void setSourceAddrTon(String sourceAddrTon) {
		this.sourceAddrTon = sourceAddrTon;
	}

	public String getSourceAddrNpi() {
		return sourceAddrNpi;
	}

	public void setSourceAddrNpi(String sourceAddrNpi) {
		this.sourceAddrNpi = sourceAddrNpi;
	}

	public String getSourceAddr() {
		return sourceAddr;
	}

	public void setSourceAddr(String sourceAddr) {
		this.sourceAddr = sourceAddr;
	}

	public String getDestAddrTon() {
		return destAddrTon;
	}

	public void setDestAddrTon(String destAddrTon) {
		this.destAddrTon = destAddrTon;
	}

	public String getDestAddrNpi() {
		return destAddrNpi;
	}

	public void setDestAddrNpi(String destAddrNpi) {
		this.destAddrNpi = destAddrNpi;
	}

	public String getDestinationAddr() {
		return destinationAddr;
	}

	public void setDestinationAddr(String destinationAddr) {
		this.destinationAddr = destinationAddr;
	}

	public String getEmmessagingMode() {
		return emmessagingMode;
	}

	public void setEmmessagingMode(String emmessagingMode) {
		this.emmessagingMode = emmessagingMode;
	}

	public String getEmmessageType() {
		return emmessageType;
	}

	public void setEmmessageType(String emmessageType) {
		this.emmessageType = emmessageType;
	}

	public String getEmgsmNetworkSpecificFeatures() {
		return emgsmNetworkSpecificFeatures;
	}

	public void setEmgsmNetworkSpecificFeatures(String emgsmNetworkSpecificFeatures) {
		this.emgsmNetworkSpecificFeatures = emgsmNetworkSpecificFeatures;
	}

	public String getProtocolId() {
		return protocolId;
	}

	public void setProtocolId(String protocolId) {
		this.protocolId = protocolId;
	}

	public String getPriorityFlag() {
		return priorityFlag;
	}

	public void setPriorityFlag(String priorityFlag) {
		this.priorityFlag = priorityFlag;
	}

	public String getScheduleDeliveryTime() {
		return scheduleDeliveryTime;
	}

	public void setScheduleDeliveryTime(String scheduleDeliveryTime) {
		this.scheduleDeliveryTime = scheduleDeliveryTime;
	}

	public String getValidityPeriod() {
		return validityPeriod;
	}

	public void setValidityPeriod(String validityPeriod) {
		this.validityPeriod = validityPeriod;
	}

	public String getRdsmsCDeliveryReceipt() {
		return rdsmsCDeliveryReceipt;
	}

	public void setRdsmsCDeliveryReceipt(String rdsmsCDeliveryReceipt) {
		this.rdsmsCDeliveryReceipt = rdsmsCDeliveryReceipt;
	}

	public String getRdsmeOriginatedAck() {
		return rdsmeOriginatedAck;
	}

	public void setRdsmeOriginatedAck(String rdsmeOriginatedAck) {
		this.rdsmeOriginatedAck = rdsmeOriginatedAck;
	}

	public String getRdintermediateNotification() {
		return rdintermediateNotification;
	}

	public void setRdintermediateNotification(String rdintermediateNotification) {
		this.rdintermediateNotification = rdintermediateNotification;
	}

	public String getReplaceIfPresentFlag() {
		return replaceIfPresentFlag;
	}

	public void setReplaceIfPresentFlag(String replaceIfPresentFlag) {
		this.replaceIfPresentFlag = replaceIfPresentFlag;
	}

	public String getDataCoding() {
		return dataCoding;
	}

	public void setDataCoding(String dataCoding) {
		this.dataCoding = dataCoding;
	}

	public String getSmDefaultMsgId() {
		return smDefaultMsgId;
	}

	public void setSmDefaultMsgId(String smDefaultMsgId) {
		this.smDefaultMsgId = smDefaultMsgId;
	}

	public String getSmLength() {
		return smLength;
	}

	public void setSmLength(String smLength) {
		this.smLength = smLength;
	}

	public String getShortMessage() {
		return shortMessage;
	}

	public void setShortMessage(String shortMessage) {
		this.shortMessage = shortMessage;
	}

}
