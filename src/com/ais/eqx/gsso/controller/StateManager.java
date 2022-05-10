package com.ais.eqx.gsso.controller;

import com.ais.eqx.gsso.enums.States;
import com.ais.eqx.gsso.states.IDLE;
import com.ais.eqx.gsso.states.W_ACTIVE;
import com.ais.eqx.gsso.states.W_WSDL;

import ec02.af.abstracts.AbstractAFStateManager;

public class StateManager extends AbstractAFStateManager {

	public StateManager(final String currentState) {

		if (States.IDLE.getState().equals(currentState)) {
			this.afState = new IDLE();

		}
		else if (States.W_WSDL.getState().equals(currentState)) {
			this.afState = new W_WSDL();

		}
		else if (States.W_ACTIVE.getState().equals(currentState)) {
			this.afState = new W_ACTIVE();

		}
	}
}
