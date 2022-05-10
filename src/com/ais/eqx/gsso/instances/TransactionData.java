package com.ais.eqx.gsso.instances;

public class TransactionData {

	private String	service					= "";
	private String	otp						= "";
	private String	refNumber				= "";
	private long	otpExpireTime			= 0;
	private int		hackTime				= 0;
	private long	transactionIdExpireTime	= 0;
	private String	seedKey					= "";
	private String	origInvoke				= "";
	private boolean	isActive				= false;
	private boolean	isAuthAndMissingSeedKey	= false;
	
	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getOtp() {
		return otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}

	public long getOtpExpireTime() {
		return otpExpireTime;
	}

	public void setOtpExpireTime(long otpExpireTime) {
		this.otpExpireTime = otpExpireTime;
	}

	public int getHackTime() {
		return hackTime;
	}

	public void setHackTime(int hackTime) {
		this.hackTime = hackTime;
	}

	public long getTransactionIdExpireTime() {
		return transactionIdExpireTime;
	}

	public void setTransactionIdExpireTime(long transactionIdExpireTime) {
		this.transactionIdExpireTime = transactionIdExpireTime;
	}

	public String getRefNumber() {
		return refNumber;
	}

	public void setRefNumber(String refNumber) {
		this.refNumber = refNumber;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public String getSeedKey() {
		return seedKey;
	}

	public void setSeedKey(String seedKey) {
		this.seedKey = seedKey;
	}

	public String getOrigInvoke() {
		return origInvoke;
	}

	public void setOrigInvoke(String origInvoke) {
		this.origInvoke = origInvoke;
	}

	public boolean isAuthAndMissingSeedKey() {
		return isAuthAndMissingSeedKey;
	}

	public void setAuthAndMissingSeedKey(boolean isAuthAndMissingSeedKey) {
		this.isAuthAndMissingSeedKey = isAuthAndMissingSeedKey;
	}

	

}
