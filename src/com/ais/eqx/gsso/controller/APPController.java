package com.ais.eqx.gsso.controller;

import java.util.ArrayList;

import javax.xml.bind.JAXBException;

import com.ais.eqx.gsso.enums.States;
import com.ais.eqx.gsso.exception.ConfigException;
import com.ais.eqx.gsso.instances.APPInstance;
import com.ais.eqx.gsso.instances.EC02Instance;
import com.ais.eqx.gsso.interfaces.RetNumber;
import com.ais.eqx.gsso.jaxb.InstanceContext;
import com.ais.eqx.gsso.utils.InstanceHandler;
import com.ais.eqx.gsso.utils.TimeoutManagement;
import com.ais.eqx.gsso.validator.VerifyConfig;

import ec02.af.abstracts.AbstractAF;
import ec02.af.data.ECDialogue;
import ec02.af.data.EquinoxProperties;
import ec02.af.data.EquinoxRawData;
import ec02.af.exception.ActionProcessException;
import ec02.af.exception.ComposeInstanceException;
import ec02.af.exception.ConstructRawDataException;
import ec02.af.exception.ExtractInstanceException;
import ec02.af.exception.ExtractRawDataException;
import ec02.af.interfaces.IEC02;
import ec02.af.utils.Log;

public class APPController extends AbstractAF implements IEC02 {

	@Override
	public ECDialogue actionProcess(EquinoxProperties properties, ArrayList<EquinoxRawData> rawDatas, Object instance)
			throws ActionProcessException {

		EC02Instance ec02Instance = (EC02Instance) instance;
		ec02Instance.setEquinoxProperties(properties);
		ec02Instance.setAbstractAF(this);

		String currentState = properties.getState();

		if(!States.W_WSDL.getState().equals(currentState)){
			/** TIMEOUT HANDLER **/
			if (properties.isTimeout() && rawDatas.size() == 0) {
	
				rawDatas.clear();
	
				EquinoxRawData equinoxRawData = new EquinoxRawData();
				equinoxRawData.setRet(RetNumber.TIMEOUT);
				equinoxRawData.setRawMessage("[[TIMEOUT]]");
	
				rawDatas.add(equinoxRawData);
	
			}
			else if (properties.isTimeout() && rawDatas.size() >= 0) {
				for (EquinoxRawData equinoxRawData : rawDatas) {
					equinoxRawData.setRet(RetNumber.TIMEOUT);
				}
			}
		}
		
		// /** CHECK MATH INVOKE OUTGOING IF RAWDATA INCOMING IS RET = 4 **/
		ArrayList<EquinoxRawData> listRawDatasIncoming = TimeoutManagement.cleanTimeoutIncoming(ec02Instance, rawDatas);

		StateManager stateManager = new StateManager(currentState);
		
		/** TIMEOUT HANDLER WSDL**/
		if(States.W_WSDL.getState().equals(currentState)){
			if (properties.isTimeout() && rawDatas.size() == 0) {
	
				rawDatas.clear();
	
				EquinoxRawData equinoxRawData = new EquinoxRawData();
				equinoxRawData.setRet(RetNumber.TIMEOUT);
				equinoxRawData.setRawMessage("[[TIMEOUT]]");
	
				rawDatas.add(equinoxRawData);
	
			}
		}
		
		String nextState = stateManager.doAction(this, ec02Instance, listRawDatasIncoming);

		properties.setState(nextState);
		properties.setRet(ec02Instance.getRet());
		properties.setTimeout(ec02Instance.getTimeout());
		properties.setDiag("");
		

		return new ECDialogue(properties, instance);
	}

	@Override
	public boolean verifyAFConfiguration(String afConfig) {

		try {
			VerifyConfig.verifyConfig(afConfig, this);

			InstanceContext.initGson();
			InstanceContext.initDeliveryReportRequestContext();
			InstanceContext.initDeliveryReportResContext();
			InstanceContext.initSubmitSMResponseContext();

			InstanceContext.initSendOneTimePWRequestContext();
			InstanceContext.initGeneratePasskeyRequestContext();
			InstanceContext.initConfirmOneTimePWRequestContext();
			InstanceContext.initInquiryVASSubscriberResponseContext();
			InstanceContext.initInquirySubscriberResponseContext();
			InstanceContext.initPortCheckResponseContext();
			
			// New Insterface
			InstanceContext.initSendWSConfirmOTPRequestContext();
			InstanceContext.initSendWSConfirmOTPWithIDRequestContext();
			InstanceContext.initSendWSOTPRequestContext();

			return true;
		}
		catch (JAXBException e) {
			Log.e(e.getMessage());
			return false;
		}
		catch (ConfigException e) {
			Log.e(e.getWriteLog());
			return false;
		}

	}

	@Override
	public String composeInstance(Object instance) throws ComposeInstanceException {

		EC02Instance ec02Instance = (EC02Instance) instance;
		APPInstance appInstance = ec02Instance.getAppInstance();
		String encoded = InstanceHandler.encode(appInstance);

		return encoded;

	}

	@Override
	public ArrayList<EquinoxRawData> constructRawData(Object instance) throws ConstructRawDataException {

		EC02Instance ec02Instance = (EC02Instance) instance;

		return ec02Instance.getEquinoxRawDatas();
	}

	@Override
	public Object extractInstance(String instance) throws ExtractInstanceException {

		EC02Instance ec02Instance = new EC02Instance();
		APPInstance appInstance = null;

		appInstance = (instance != null && !instance.isEmpty()) ? InstanceHandler.decode(instance) : new APPInstance();
		ec02Instance.setAppInstance(appInstance);

		return ec02Instance;
	}

	@Override
	public void extractRawData(Object instance, ArrayList<EquinoxRawData> rawDatas) throws ExtractRawDataException {

	}

}
