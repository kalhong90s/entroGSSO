package com.ais.eqx.gsso.controller;

import com.ais.eqx.gsso.abstracts.AbstractAFSubStateManager;
import com.ais.eqx.gsso.enums.SubStates;
import com.ais.eqx.gsso.substates.IDLE_AUTH_OTP;
import com.ais.eqx.gsso.substates.IDLE_CONFIRMATION;
import com.ais.eqx.gsso.substates.IDLE_CONFIRMATION_W_PK;
import com.ais.eqx.gsso.substates.IDLE_GENERATE_PK;
import com.ais.eqx.gsso.substates.IDLE_MANAGEMENT;
import com.ais.eqx.gsso.substates.IDLE_SEND_OTP_REQ;
import com.ais.eqx.gsso.substates.IDLE_WS_AUTH_OTP;
import com.ais.eqx.gsso.substates.IDLE_WS_AUTH_OTP_ID;
import com.ais.eqx.gsso.substates.IDLE_WS_CONFIRM_OPT_ID;
import com.ais.eqx.gsso.substates.IDLE_WS_CONFIRM_OTP;
import com.ais.eqx.gsso.substates.IDLE_WS_CREATE_OTP;
import com.ais.eqx.gsso.substates.IDLE_WS_GENERATE_OTP;
import com.ais.eqx.gsso.substates.UNKNOWN;
import com.ais.eqx.gsso.substates.W_DELIVERY_REPORT;
import com.ais.eqx.gsso.substates.W_INQUIRY_SUB;
import com.ais.eqx.gsso.substates.W_INQUIRY_VAS_SUB;
import com.ais.eqx.gsso.substates.W_PORT_CHECK;
import com.ais.eqx.gsso.substates.W_REFUND;
import com.ais.eqx.gsso.substates.W_SEND_EMAIL;
import com.ais.eqx.gsso.substates.W_SEND_SMS;
import com.ais.eqx.gsso.substates.W_SERVICE_TEMPLATE;

public class SubStateManager extends AbstractAFSubStateManager {

	public SubStateManager(final String currentSubState) {

		if (SubStates.IDLE_MANAGEMENT.name().equals(currentSubState)) {
			this.subState = new IDLE_MANAGEMENT();
		}
		else if (SubStates.IDLE_SEND_OTP_REQ.name().equals(currentSubState)) {
			this.subState = new IDLE_SEND_OTP_REQ();
		}
		else if (SubStates.IDLE_CONFIRMATION.name().equals(currentSubState)) {
			this.subState = new IDLE_CONFIRMATION();
		}
		else if (SubStates.IDLE_CONFIRMATION_W_PK.name().equals(currentSubState)) {
			this.subState = new IDLE_CONFIRMATION_W_PK();
		}
		else if (SubStates.IDLE_AUTH_OTP.name().equals(currentSubState)) {
			this.subState = new IDLE_AUTH_OTP();
		}
		else if (SubStates.IDLE_GENERATE_PK.name().equals(currentSubState)) {
			this.subState = new IDLE_GENERATE_PK();
		}
		else if (SubStates.W_INQUIRY_VAS_SUB.name().equals(currentSubState)) {
			this.subState = new W_INQUIRY_VAS_SUB();
		}
		else if (SubStates.W_INQUIRY_SUB.name().equals(currentSubState)) {
			this.subState = new W_INQUIRY_SUB();
		}
		else if (SubStates.W_DELIVERY_REPORT.name().equals(currentSubState)) {
			this.subState = new W_DELIVERY_REPORT();
		}
		else if (SubStates.W_PORT_CHECK.name().equals(currentSubState)) {
			this.subState = new W_PORT_CHECK();
		}
		else if (SubStates.W_SEND_EMAIL.name().equals(currentSubState)) {
			this.subState = new W_SEND_EMAIL();
		}
		else if (SubStates.W_SEND_SMS.name().equals(currentSubState)) {
			this.subState = new W_SEND_SMS();
		}
		else if (SubStates.W_SERVICE_TEMPLATE.name().equals(currentSubState)) {
			this.subState = new W_SERVICE_TEMPLATE();
		}
		else if (SubStates.IDLE_WS_AUTH_OTP.name().equals(currentSubState)) {
			this.subState = new IDLE_WS_AUTH_OTP();
		}
		else if (SubStates.IDLE_WS_AUTH_OTP_ID.name().equals(currentSubState)) {
			this.subState = new IDLE_WS_AUTH_OTP_ID();
		}
		else if (SubStates.IDLE_WS_CONFIRM_OTP.name().equals(currentSubState)) {
			this.subState = new IDLE_WS_CONFIRM_OTP();
		}
		else if (SubStates.IDLE_WS_CONFIRM_OTP_ID.name().equals(currentSubState)) {
			this.subState = new IDLE_WS_CONFIRM_OPT_ID();
		}
		else if (SubStates.IDLE_WS_CREATE_OTP.name().equals(currentSubState)) {
			this.subState = new IDLE_WS_CREATE_OTP();
		}
		else if (SubStates.IDLE_WS_GENERATE_OTP.name().equals(currentSubState)) {
			this.subState = new IDLE_WS_GENERATE_OTP();
		}
		else if (SubStates.W_REFUND.name().equals(currentSubState)) {
			this.subState = new W_REFUND();
		}
		else {
			this.subState = new UNKNOWN();
		}

	}
}
