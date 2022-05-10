package com.ais.eqx.gsso.instances;

import com.google.gson.annotations.SerializedName;

public class Refund {
	
	@SerializedName("command")
	private String	command	= null;
	
	@SerializedName("sessionId")
	private String	sessionId	= null;
	
	@SerializedName("actualTime")
	private String	actualTime	= null;
	
	@SerializedName("tid")
	private String	tid	= null;
	
	@SerializedName("refId")
	private String	refId	= null;
	
	@SerializedName("rtid")
	private String	rtid	= null;
	
	@SerializedName("status")
	private String	status	= null;
	
	@SerializedName("devMessage")
	private String	devMessage	= null;
	
	public String getCommand() {
		return command;
	}

	public String getSessionId() {
		return sessionId;
	}

	public String getActualTime() {
		return actualTime;
	}

	public String getTid() {
		return tid;
	}

	public String getRefId() {
		return refId;
	}

	public String getStatus() {
		return status;
	}

	public String getDevMessage() {
		return devMessage;
	}

	public void setCommand(final String command) {
		this.command = command;
	}

	public void setSessionId(final String sessionId) {
		this.sessionId = sessionId;
	}

	public void setActualTime(final String actualTime) {
		this.actualTime = actualTime;
	}

	public void setTid(final String tid) {
		this.tid = tid;
	}

	public void setRefId(final String refId) {
		this.refId = refId;
	}

	public void setStatus(final String status) {
		this.status = status;
	}

	public void setDevMessage(final String devMessage) {
		this.devMessage = devMessage;
	}

	public String getRtid() {
		return rtid;
	}

	public void setRtid(String rtid) {
		this.rtid = rtid;
	}
}
