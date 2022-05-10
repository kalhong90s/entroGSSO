package com.ais.eqx.gsso.instances;

public class RefundLog {

	private String	reqTimeStamp;
	private String	session;
	private String	initInvoke;
	private String	cmdName;
	private String	identity;
	private String	sessionId;
	private String	tid;
	private String	refId;
	private String	resultCode;
	private String	resultDesc;

	

	public String getReqTimeStamp() {
		return reqTimeStamp;
	}

	public String getSession() {
		return session;
	}

	public String getInitInvoke() {
		return initInvoke;
	}

	public String getCmdName() {
		return cmdName;
	}

	public String getIdentity() {
		return identity;
	}

	public String getSessionId() {
		return sessionId;
	}

	public String getTid() {
		return tid;
	}

	public String getRefId() {
		return refId;
	}

	public String getResultCode() {
		return resultCode;
	}

	public String getResultDesc() {
		return resultDesc;
	}

	public void setReqTimeStamp(String reqTimeStamp) {
		this.reqTimeStamp = reqTimeStamp;
	}

	public void setSession(String session) {
		this.session = session;
	}

	public void setInitInvoke(String initInvoke) {
		this.initInvoke = initInvoke;
	}

	public void setCmdName(String cmdName) {
		this.cmdName = cmdName;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public void setTid(String tid) {
		this.tid = tid;
	}

	public void setRefId(String refId) {
		this.refId = refId;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	public void setResultDesc(String resultDesc) {
		this.resultDesc = resultDesc;
	}

	@Override
	public String toString() {

		String logOutput = getReqTimeStamp() + "|" + getSession() + "|" + getInitInvoke() + "|" + getCmdName() + "|"
				+ getIdentity() + "|" + getSessionId() + "|" + getTid() + "|" + getRefId() + "|"
				+ getResultCode() + "|" + getResultDesc();

		return logOutput;
	}
}
