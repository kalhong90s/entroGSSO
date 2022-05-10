package com.ais.eqx.gsso.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import org.apache.commons.lang3.StringEscapeUtils;

import com.ais.eqx.gsso.controller.SubStateManager;
import com.ais.eqx.gsso.enums.ConfigName;
import com.ais.eqx.gsso.enums.States;
import com.ais.eqx.gsso.enums.SubStates;
import com.ais.eqx.gsso.instances.APPInstance;
import com.ais.eqx.gsso.instances.EC02Instance;
import com.ais.eqx.gsso.instances.MapDetailsAndConfigType;
import com.ais.eqx.gsso.interfaces.EventAction;
import com.ais.eqx.gsso.interfaces.GlobaldataEventType;
import com.ais.eqx.gsso.utils.ConfigureTool;
import com.ais.eqx.gsso.utils.GssoComposeDebugLog;
import com.ais.eqx.gsso.utils.GssoServiceManagement;
import com.ais.eqx.gsso.utils.InvokeFilter;
import com.ais.eqx.gsso.utils.TimeoutCalculator;
import com.ais.eqx.gsso.utils.TimeoutManagement;

import ais.mmt.sand.comlog.SummaryLogPrototype;
import ais.mmt.sand.comlog.exception.CommonLogException;
import ec02.af.abstracts.AbstractAF;
import ec02.af.data.EquinoxRawData;
import ec02.af.utils.Log;
import ec02.common.data.E01Data;
import ec02.common.data.GlobalData;


public class GSSOHandler {

	private AbstractAF		abstractAF;
	private EC02Instance	ec02Instance;
	private APPInstance		appInstance;
	private String			nextState;

	private void process(ArrayList<EquinoxRawData> equinoxRawDatas) {

		ArrayList<EquinoxRawData> listRawDatasOutgoing = new ArrayList<EquinoxRawData>();

		Iterator<EquinoxRawData> iterator = equinoxRawDatas.iterator();
		while (iterator.hasNext()) {
			iterator.next();
			if (appInstance.getTimeStampOutgoing() == 0) {
				appInstance.setTimeStampOutgoing(appInstance.getTimeStampIncoming());
			}
		}

		/** TIME STAMP INCOMING **/
		this.appInstance.setTimeStampIncoming(System.currentTimeMillis());

		for (EquinoxRawData rawDataIncoming : equinoxRawDatas) {
			/** CHANGE INCOMING INVOKE FOR MAP NEW INVOKE **/
			ArrayList<String> listProcessing = appInstance.getListInvokeProcessing();
			String origInvoke = InvokeFilter.getOriginInvoke(rawDataIncoming.getInvoke());
			if (!listProcessing.contains(origInvoke)) {
				String newInvoke = rawDataIncoming.getInvoke() + "@" + (UUID.randomUUID().toString()).replaceAll("-", ""); // oldInvoke@uuid(without-)
				appInstance.getMapInvokeOrig().put(newInvoke, rawDataIncoming.getInvoke());
				rawDataIncoming.setInvoke(newInvoke);
				
				// case response delay after timeout (optional 10/05/2018)
				if((origInvoke == null || origInvoke.isEmpty()) && abstractAF.getEquinoxUtils().getGlobalData()!=null){
					break;
				}
			}

			String subState = findSubState(rawDataIncoming);

			SubStateManager subStateManager = new SubStateManager(subState);
			ArrayList<EquinoxRawData> rawDatas;
			if (subState.equals(SubStates.W_SERVICE_TEMPLATE.name())) {
				rawDatas = subStateManager.doActionSubStateE01(abstractAF, ec02Instance, rawDataIncoming, new E01Data());
			}
			else {
				rawDatas = subStateManager.doActionSubState(abstractAF, ec02Instance, rawDataIncoming);
			}

			if (rawDatas != null && !rawDatas.isEmpty()) {
				listRawDatasOutgoing.addAll(listRawDatasOutgoing.size(), rawDatas);
			}
		}

		GlobalData globalData = abstractAF.getEquinoxUtils().getGlobalData();
		if (globalData.getDataResultSet() != null) {
			for (E01Data e01DataIncoming : globalData.getDataResultSet()) {
				String globaldataEventType = globalData.getGlobaldataEventType();
				if ((globaldataEventType.equals(GlobaldataEventType.NORMAL) && !e01DataIncoming.getResultCode().isEmpty())
						|| globaldataEventType.equals(GlobaldataEventType.ERROR)
						|| globaldataEventType.equals(GlobaldataEventType.REJECT)
						|| globaldataEventType.equals(GlobaldataEventType.ABORT)
						|| globaldataEventType.equals(GlobaldataEventType.TIMEOUT)) {

					/** REMOVE TIMEOUT INVOKE PENDING **/
					String invokeIncoming = e01DataIncoming.getId();
					HashMap<String, TimeoutCalculator> mapTimeoutOfInvokePending = ec02Instance.getAppInstance()
							.getMapTimeoutOfInvokePending();
					if (mapTimeoutOfInvokePending.get(invokeIncoming) != null) {
						mapTimeoutOfInvokePending.remove(invokeIncoming);
						Log.d("Reset Time to wait of : " + invokeIncoming);
					}
					/** ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ **/

					EquinoxRawData rawDataIncoming = new EquinoxRawData();
					rawDataIncoming.setInvoke(e01DataIncoming.getId());

					String subState = findSubState(rawDataIncoming);

					SubStateManager subStateManager = new SubStateManager(subState);
					ArrayList<EquinoxRawData> rawDatas;
					if (subState.equals(SubStates.W_SERVICE_TEMPLATE.name())) {
						rawDatas = subStateManager.doActionSubStateE01(abstractAF, ec02Instance, rawDataIncoming, e01DataIncoming);
					}
					else {
						rawDatas = subStateManager.doActionSubState(abstractAF, ec02Instance, rawDataIncoming);
					}

					if (rawDatas != null && !rawDatas.isEmpty()) {
						listRawDatasOutgoing.addAll(listRawDatasOutgoing.size(), rawDatas);
					}
				}
			}
		}

		// ===============================================DEBUG
		// LOG==========================================================
		/** INITIAL LOG **/
		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			GssoComposeDebugLog composeLog = new GssoComposeDebugLog(appInstance,
					ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED));
			composeLog.initialGssoHandlerLog();
		}

		Log.d("Invoke OutGoing: " + appInstance.getMapOrigProfile().size());
		Log.d("Invoke: " + appInstance.getMapOrigProfile().keySet());
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
		// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		long outTime = System.currentTimeMillis();
		this.appInstance.setTimeStampOutgoing(outTime);

		/** SET OUT PUT **/
		for (EquinoxRawData rawData : listRawDatasOutgoing) {
			/** CHANGE OUTGOING INVOKE **/
			if (rawData.getType().toLowerCase().equals(EventAction.RESPONSE)) {

				String invokeResponse = appInstance.getMapInvokeOrig().remove(rawData.getInvoke());

				if (invokeResponse != null) {
					rawData.setInvoke(invokeResponse);
				}

			}
			else {
				/** SET OUT TIME FOR REQ **/
				appInstance.getMapInvokeByOutputTime().put(rawData.getInvoke(), outTime);
			}
		}

		/** ADD NEW TIME E01 OUT **/
		if (abstractAF.getEquinoxUtils().getDataBuffer().getE01Commands().size() != 0) {
			/** SET OUT TIME FOR REQ **/
			for (E01Data e01Data : abstractAF.getEquinoxUtils().getDataBuffer().getE01Commands()) {
				appInstance.getMapInvokeByOutputTime().put(e01Data.getId(), outTime);
			}
		}

		// ==============================================SET
		// TIMEOUT=========================================================
		String timeout = TimeoutManagement.setTimeout(appInstance, listRawDatasOutgoing, abstractAF);

		/** SET OUT PUT **/
		// ===============================================WRITE
		// DETAILS======================================================
		for (MapDetailsAndConfigType mapDetailLog : appInstance.getListDetailsLog()) {
			mapDetailLog.getDetail().setOutputTimestamp(outTime);

			for (EquinoxRawData rawDataOut : listRawDatasOutgoing) {
				try {
					/** IF SMS **/
					String origInvoke = InvokeFilter.getOriginInvoke(rawDataOut.getInvoke());
					String subState = InvokeFilter.getSubState(rawDataOut.getInvoke());
					if (subState != null && subState.equals(SubStates.W_SEND_SMS.name())) {
						this.appInstance.getMapOrigProfile().get(origInvoke).setSubmitSmRequestTime(outTime);
					}
				}
				catch (Exception e) {
					Log.e(e.getMessage());
				}
			}
			
			try {
				/** ALL = 2 **/
				if (ConfigureTool.isWriteLogDetails(ConfigName.LOG_DETAIL_ENABLED) == 2) {
					String detailLog = mapDetailLog.getDetail().print().replaceAll("\\\\t", "").replaceAll("\\\\n", "");
					if(appInstance.isTransaction()){
						detailLog = detailLog.replace("invoke_unknown", "");
					}
					
					detailLog = StringEscapeUtils.unescapeJava(detailLog);
					
					ec02Instance.writeLog(true, ConfigureTool.getConfigureLogName(ConfigName.DETAIL_LOG_NAME.getName()),
							detailLog);

				}
				/** ERROR = 1 **/
				else if (ConfigureTool.isWriteLogDetails(ConfigName.LOG_DETAIL_ENABLED) == 1) {
					String detailLog = mapDetailLog.getDetail().print().replaceAll("\\\\t", "").replaceAll("\\\\n", "");
					detailLog = StringEscapeUtils.unescapeJava(detailLog);
					
					ec02Instance.writeLog(mapDetailLog.isError(), ConfigureTool.getConfigureLogName(ConfigName.DETAIL_LOG_NAME
							.getName()), detailLog);
				}
				/** CLOSE = 0 **/
				else {
				}
			}
			catch (CommonLogException e) {
				Log.e(e.getMessage());
				System.out.println(e.getMessage());
			}
		}
		appInstance.getListDetailsLog().clear();
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		// ===============================================WRITE
		// SUMMARY======================================================
		for (SummaryLogPrototype summary : appInstance.getListSummaryLog()) {
			summary.setRespTimeStamp(outTime);

			try {
				ec02Instance.writeLog(true, ConfigureTool.getConfigureLogName(ConfigName.SUMMARY_LOG_NAME.getName()), summary.print());
			}
			catch (CommonLogException e) {
				Log.e(e.getMessage());
			}
		}
		appInstance.getListSummaryLog().clear();
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// SUMMARY^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		if (appInstance.getMapTimeoutOfInvokePending().isEmpty() && appInstance.getMapTimeoutOfTransactionID().isEmpty()
				&& appInstance.getMapTimeoutOfWaitDR().isEmpty() && appInstance.getMapTimeoutOfWaitRefund().isEmpty()) {
			nextState = States.IDLE.toString();
		}
		else {
			nextState = States.W_ACTIVE.toString();
		}

		this.ec02Instance.setTimeout(timeout);
		this.ec02Instance.setEquinoxRawDatas(listRawDatasOutgoing);
		this.ec02Instance.setAppInstance(appInstance);
	}

	private String findSubState(EquinoxRawData rawData) {

		String subState = SubStates.UNKNOWN.toString();
		String origInvoke = InvokeFilter.getOriginInvoke(rawData.getInvoke());

		if (appInstance.getListInvokeProcessing().contains(origInvoke)) {
			// 123@1445230410689/W_PORT_CHECK.b5a020a6-6609-4168-a391-c59275c3fcd0
			subState = InvokeFilter.getSubState(rawData.getInvoke());

		}
		else {

			ArrayList<String> listIdleService = ConfigureTool.getConfigureArray(ConfigName.IDLE_SERVICE);
			ArrayList<String> listSMPPService = ConfigureTool.getConfigureArray(ConfigName.SMPPGW_INTERFACE);
			ArrayList<String> listSMPPRoamingService = ConfigureTool.getConfigureArray(ConfigName.SMPPGW_ROAMING_INTERFACE);
			
			String serviceIncoming = null;
			String type = null;
			try {
				String[] service = rawData.getOrig().split("\\.");
				serviceIncoming = service[0] + "." + service[1] + "." + service[2];
			}
			catch (Exception e) {
				serviceIncoming = "";
			}
			try {
				type = rawData.getType();
			}
			catch (Exception e) {
				type = "";
			}

			/**
			 * UNKNOWN
			 */
			if (serviceIncoming.isEmpty() && type.isEmpty()) {
				subState = SubStates.UNKNOWN.toString();
			}
			else {
				/**
				 * Do IDLE_SUB
				 */
				if (GssoServiceManagement.containService(listIdleService, serviceIncoming) && type.equals(EventAction.REQUEST)) {
					subState = SubStates.IDLE_MANAGEMENT.toString();
				}
				/**
				 * Do W_DELIVERY_REPORT
				 */
				else if ((GssoServiceManagement.containService(listSMPPService, serviceIncoming) || GssoServiceManagement
						.containService(listSMPPRoamingService, serviceIncoming)) && type.equals(EventAction.REQUEST)) {
					subState = SubStates.W_DELIVERY_REPORT.toString();
				}
				/**
				 * UNKNOWN
				 */
				else {
					subState = SubStates.UNKNOWN.toString();
				}
			}
		}
		return subState;
	}

	public static String handle(AbstractAF abstractAF, Object ec02Instance, ArrayList<EquinoxRawData> equinoxRawDatas) {

		try {
			GSSOHandler gssoHandler = new GSSOHandler();
			gssoHandler.abstractAF = abstractAF;
			gssoHandler.ec02Instance = (EC02Instance) ec02Instance;
			gssoHandler.appInstance = gssoHandler.ec02Instance.getAppInstance();
			gssoHandler.process(equinoxRawDatas);
			return gssoHandler.nextState;
		}
		catch (Exception e) {
			Log.e(e.getMessage());
			return abstractAF.getEquinoxProperties().getState();
		}

	}

}