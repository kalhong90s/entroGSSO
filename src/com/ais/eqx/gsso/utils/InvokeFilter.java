package com.ais.eqx.gsso.utils;

public class InvokeFilter {

	private String	originInvoke;
	private String	subState;

	public static String getOriginInvoke(String incomingInvoke) {
		String originInvoke;

		try {
			String[] invoke = incomingInvoke.split("/");
			originInvoke = invoke[0];
		}
		catch (Exception e) {
			originInvoke = incomingInvoke;
		}

		return originInvoke;
	}

	public static String getSubState(String incomingInvoke) {
		String subState;

		try {
			String[] invoke = incomingInvoke.split("/");
			String[] appInvoke = invoke[1].split("\\.");
			subState = appInvoke[0];

		}
		catch (Exception e) {
			subState = null;
		}

		return subState;
	}

	public String getOriginInvoke() {
		return originInvoke;
	}

	public void setOriginInvoke(String originInvoke) {
		this.originInvoke = originInvoke;
	}

	public String getSubState() {
		return subState;
	}

	public void setSubState(String subState) {
		this.subState = subState;
	}
}
