package com.ais.eqx.gsso.enums;

import java.util.HashMap;

public enum EquinoxEvent {

	NORMAL("0"), ERROR("1"), REJECT("2"), ABORT("3"), TIMEOUT("4");

	private String	code;

	public String getCode() {
		return code;
	}

	private EquinoxEvent(final String code) {
		this.code = code;
	}

	// Equinox Event Lookup
	private static final HashMap<String, EquinoxEvent>	lookup	= new HashMap<String, EquinoxEvent>();
	static {
		for (final EquinoxEvent e : EquinoxEvent.values()) {
			lookup.put(e.getCode(), e);
		}
	}

	public static EquinoxEvent getEquinoxEventFrom(final String code) {
		return lookup.get(code);
	}

}
