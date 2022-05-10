package com.ais.eqx.gsso.instances;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Subscriber")
public class SubscriberOfVas {

//	@XmlElement(name = "Msisdn")
	private String	msisdn;

//	@XmlElement(name = "customerId")
	private String	customerId;

//	@XmlElement(name = "State")
	private String	state;

//	@XmlElement(name = "Lang")
	private String	language;

//	@XmlElement(name = "Cos")
	private String	cos;

	@XmlElement(name = "spName")
	private String	spName;
	
	//After this line is O
	
	private String	machineID;
	private String	stt;
	private String	hacktime;
	@XmlElement(name = "MProductID")
	private String	mProductID;
	
	private String	mobileLocation;
	@XmlElement(name = "ServicePackageID")
	private String	servicePackageID;
	
	@XmlElement(name = "SCPID")
	private String	scpid;
	
	@XmlElement(name = "CBPID")
	private String	cbpid;
	
	private String	gprsTBCF;
	private String	altAccPrefixF;
	private String	altAccPrefixCos;
	private String	altAccToggleState;
	private String	altAccToggleCos;
	
	@XmlElement(name = "ToggleF")
	private String	toggleF;
	
	private String	nwt;
	private String	chm;
	private String	brandId;
	private String	cfAddress;
	@XmlElement(name = "CustomerCategory")
	private String	customerCategory;
	
	@XmlElement(name = "CustomerSubCategory")
	private String	customerSubCategory;
	
	@XmlElement(name = "CustomerSegment")
	private String	customerSegment;

	
	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getCos() {
		return cos;
	}

	public void setCos(String cos) {
		this.cos = cos;
	}

	public String getSpName() {
		return spName;
	}

	public void setSpName(String spName) {
		this.spName = spName;
	}

	public String getMachineID() {
		return machineID;
	}

	public void setMachineID(String machineID) {
		this.machineID = machineID;
	}

	public String getStt() {
		return stt;
	}

	public void setStt(String stt) {
		this.stt = stt;
	}

	public String getHacktime() {
		return hacktime;
	}

	public void setHacktime(String hacktime) {
		this.hacktime = hacktime;
	}

	public String getmProductID() {
		return mProductID;
	}

	public void setmProductID(String mProductID) {
		this.mProductID = mProductID;
	}

	public String getMobileLocation() {
		return mobileLocation;
	}

	public void setMobileLocation(String mobileLocation) {
		this.mobileLocation = mobileLocation;
	}

	public String getServicePackageID() {
		return servicePackageID;
	}

	public void setServicePackageID(String servicePackageID) {
		this.servicePackageID = servicePackageID;
	}

	public String getScpid() {
		return scpid;
	}

	public void setScpid(String scpid) {
		this.scpid = scpid;
	}

	public String getCbpid() {
		return cbpid;
	}

	public void setCbpid(String cbpid) {
		this.cbpid = cbpid;
	}

	public String getGprsTBCF() {
		return gprsTBCF;
	}

	public void setGprsTBCF(String gprsTBCF) {
		this.gprsTBCF = gprsTBCF;
	}

	public String getAltAccPrefixF() {
		return altAccPrefixF;
	}

	public void setAltAccPrefixF(String altAccPrefixF) {
		this.altAccPrefixF = altAccPrefixF;
	}

	public String getAltAccPrefixCos() {
		return altAccPrefixCos;
	}

	public void setAltAccPrefixCos(String altAccPrefixCos) {
		this.altAccPrefixCos = altAccPrefixCos;
	}

	public String getAltAccToggleState() {
		return altAccToggleState;
	}

	public void setAltAccToggleState(String altAccToggleState) {
		this.altAccToggleState = altAccToggleState;
	}

	public String getAltAccToggleCos() {
		return altAccToggleCos;
	}

	public void setAltAccToggleCos(String altAccToggleCos) {
		this.altAccToggleCos = altAccToggleCos;
	}

	public String getToggleF() {
		return toggleF;
	}

	public void setToggleF(String toggleF) {
		this.toggleF = toggleF;
	}

	public String getNwt() {
		return nwt;
	}

	public void setNwt(String nwt) {
		this.nwt = nwt;
	}

	public String getChm() {
		return chm;
	}

	public void setChm(String chm) {
		this.chm = chm;
	}

	public String getBrandId() {
		return brandId;
	}

	public void setBrandId(String brandId) {
		this.brandId = brandId;
	}

	public String getCfAddress() {
		return cfAddress;
	}

	public void setCfAddress(String cfAddress) {
		this.cfAddress = cfAddress;
	}

	public String getCustomerCategory() {
		return customerCategory;
	}

	public void setCustomerCategory(String customerCategory) {
		this.customerCategory = customerCategory;
	}

	public String getCustomerSubCategory() {
		return customerSubCategory;
	}

	public void setCustomerSubCategory(String customerSubCategory) {
		this.customerSubCategory = customerSubCategory;
	}

	public String getCustomerSegment() {
		return customerSegment;
	}

	public void setCustomerSegment(String customerSegment) {
		this.customerSegment = customerSegment;
	}

}