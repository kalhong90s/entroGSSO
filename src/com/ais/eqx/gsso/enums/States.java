package com.ais.eqx.gsso.enums;

public enum States {

    IDLE("IDLE")
    
    ,W_WSDL("W_WSDL")

    , W_ACTIVE("W_ACTIVE")

    ;

	private String	state;

	private States(final String state) {
		this.setState(state);
	}

	public String getState() {
		return state;
	}

	public void setState(final String state) {
		this.state = state;

	}

}
