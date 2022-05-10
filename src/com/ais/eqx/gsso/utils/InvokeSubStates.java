package com.ais.eqx.gsso.utils;

import java.util.UUID;

public class InvokeSubStates {

	String	invokeIncoming	= "";
	String	nextState		= "";

	public static String getInvokeOutgoing(String invokeIncoming, String nextState) {
		return invokeIncoming + "/" + nextState + "." + UUID.randomUUID().toString();
	}

	public String getInvoke() {
		return this.invokeIncoming;
	}

	public void setInvoke(String invokeIncoming) {
		this.invokeIncoming = invokeIncoming;
	}

	public String getNextState() {
		return this.nextState;
	}

	public void setNextState(String nextState) {
		this.nextState = nextState;
	}

	public String getInvokeOutgoing() {
		return invokeIncoming + "/" + nextState + "." + UUID.randomUUID().toString();
	}
}
