package com.ais.eqx.gsso.enums;

import java.util.HashMap;

public enum EventMethod {

	POST("POST"), PUT("PUT"), GET("GET"), DELETE("DELETE");

	private String	method;

	private EventMethod(final String method) {
		this.method = method;
	}

	public String getMethod() {
		return this.method;
	}

	// Method Lookup
	private static final HashMap<String, EventMethod>	lookup	= new HashMap<String, EventMethod>();
	static {
		for (final EventMethod e : EventMethod.values()) {
			lookup.put(e.getMethod(), e);
		}
	}

	public static EventMethod getEventMethodFrom(final String code) {
		return lookup.get(code.toUpperCase());
	}

}
