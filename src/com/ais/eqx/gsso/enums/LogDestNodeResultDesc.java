package com.ais.eqx.gsso.enums;

public enum LogDestNodeResultDesc {

	CONNECTION_TIMEOUT("Connection timeout")

	, ERROR("Error")

	, REJECT("Reject")

	, ABORT("Abort")

	, RESULT_CODE_ERROR("ResultCode Error")

	;

	private String	logDestNodeResultDesc;

	private LogDestNodeResultDesc(final String logDestNodeResultDesc) {
		this.setLogDestNodeResultDesc(logDestNodeResultDesc);
	}

	public String getLogDestNodeResultDesc() {
		return logDestNodeResultDesc;
	}

	public void setLogDestNodeResultDesc(String logDestNodeResultDesc) {
		this.logDestNodeResultDesc = logDestNodeResultDesc;
	}

}
