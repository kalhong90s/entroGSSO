package com.ais.eqx.gsso.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.ais.eqx.gsso.enums.ConfigName;
import com.ais.eqx.gsso.enums.EquinoxEvent;
import com.ais.eqx.gsso.enums.Statistic;
import com.ais.eqx.gsso.instances.APPInstance;
import com.ais.eqx.gsso.instances.EC02Instance;
import com.ais.eqx.gsso.instances.GssoOTPRequest;
import com.ais.eqx.gsso.instances.OrigInvokeProfile;
import com.ais.eqx.gsso.instances.SendWSOTPRequest;
import com.ais.eqx.gsso.interfaces.EventAction;
import com.ais.eqx.gsso.interfaces.IdleMessageFormat;

import ec02.af.abstracts.AbstractAF;
import ec02.af.data.EquinoxRawData;
import ec02.af.utils.Log;
import ec02.common.data.E01Data;

public class TimeoutManagement {

	private static long getMinTimeoutPending(ArrayList<HashMap<String, TimeoutCalculator>> listMapTimeoutOfPending) {

		ArrayList<Long> ListMinValue = new ArrayList<Long>();
		for (HashMap<String, TimeoutCalculator> mapTimeOfPending : listMapTimeoutOfPending) {

			ArrayList<Long> listTimeouts = new ArrayList<Long>();
			Iterator<Entry<String, TimeoutCalculator>> itr2 = mapTimeOfPending.entrySet().iterator();
			while (itr2.hasNext()) {
				Entry<String, TimeoutCalculator> entry = (Entry<String, TimeoutCalculator>) itr2.next();
				listTimeouts.add(entry.getValue().getUpdateTimeout());
			}

			if (!listTimeouts.isEmpty()) {
				long minValue = Collections.min(listTimeouts);
				ListMinValue.add(minValue);
				Log.d("Minimum of ListTimeout[" + listTimeouts.size() + "] : " + minValue);
			}

		}
		long timeoutIsMin = 0;
		if (!ListMinValue.isEmpty()) {
			timeoutIsMin = Collections.min(ListMinValue);
		}

		return timeoutIsMin;
	}

	public static String setTimeout(APPInstance appInstance, ArrayList<EquinoxRawData> rawDatas, AbstractAF abstractAF) {

		String usmpInqsubInterface = ConfigureTool.getConfigure(ConfigName.USMP_INQUIRYSUB_INTERFACE);
		String usmpPortChkInterface = ConfigureTool.getConfigure(ConfigName.USMP_PORTCHECK_INTERFACE);
		String smppGwInterface = ConfigureTool.getConfigure(ConfigName.SMPPGW_INTERFACE);
		String smppGwRoamingInterface = ConfigureTool.getConfigure(ConfigName.SMPPGW_ROAMING_INTERFACE);
		String mailServerInterface = ConfigureTool.getConfigure(ConfigName.MAILSERVER_INTERFACE);

		int usmpTimeout = Integer.parseInt(ConfigureTool.getConfigure(ConfigName.USMP_TIMEOUT));
		int smppGwTimeout = Integer.parseInt(ConfigureTool.getConfigure(ConfigName.SMPPGW_TIMEOUT));
		int mailServerTimeout = Integer.parseInt(ConfigureTool.getConfigure(ConfigName.MAILSERVER_TIMEOUT));
		int e01Timeout = Integer.parseInt(ConfigureTool.getConfigure(ConfigName.E01_TIMEOUT));

		ArrayList<HashMap<String, TimeoutCalculator>> listMapTimeoutOfPending = new ArrayList<HashMap<String, TimeoutCalculator>>();
		HashMap<String, TimeoutCalculator> mapTimeoutOfInvokePending = new HashMap<String, TimeoutCalculator>();
		HashMap<String, TimeoutCalculator> mapTimeoutOfTransactionID = new HashMap<String, TimeoutCalculator>();
		HashMap<String, TimeoutCalculator> mapTimeoutOfWaitDR = new HashMap<String, TimeoutCalculator>();
		HashMap<String, TimeoutCalculator> mapTimeoutOfWaitRefund = new HashMap<String, TimeoutCalculator>();

		TimeoutCalculator timeoutPending;

		Log.d("Find Timeout of Invoke...");
		String invokeOutgoing;
		for (EquinoxRawData rawData : rawDatas) {
			invokeOutgoing = rawData.getInvoke();
			if (usmpInqsubInterface.equals(rawData.getTo()) || usmpPortChkInterface.equals(rawData.getTo())) {
				appInstance.getMapTimeoutOfInvokePending().put(invokeOutgoing, TimeoutCalculator.initialTimeout(usmpTimeout));
			}
			else if (smppGwInterface.equals(rawData.getTo())) {
				appInstance.getMapTimeoutOfInvokePending().put(invokeOutgoing, TimeoutCalculator.initialTimeout(smppGwTimeout));
			}
			else if (smppGwRoamingInterface.equals(rawData.getTo())) {
				appInstance.getMapTimeoutOfInvokePending().put(invokeOutgoing, TimeoutCalculator.initialTimeout(smppGwTimeout));
			}
			else if (mailServerInterface.equals(rawData.getTo())) {
				appInstance.getMapTimeoutOfInvokePending().put(invokeOutgoing, TimeoutCalculator.initialTimeout(mailServerTimeout));
			}
		}

		try {
			/** ADD NEW TIME E01 OUT **/
			if (abstractAF.getEquinoxUtils().getDataBuffer().getE01Commands().size() != 0) {
				/** SET OUT TIME FOR REQ **/
				for (E01Data e01Data : abstractAF.getEquinoxUtils().getDataBuffer().getE01Commands()) {
					appInstance.getMapTimeoutOfInvokePending().put(e01Data.getId(), TimeoutCalculator.initialTimeout(e01Timeout));
				}
			}
		}
		catch (Exception e) {
			Log.d("Get Invoke From E01 Error...");
		}

		Iterator<Entry<String, TimeoutCalculator>> iterator = appInstance.getMapTimeoutOfInvokePending().entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, TimeoutCalculator> entry = (Entry<String, TimeoutCalculator>) iterator.next();
			invokeOutgoing = entry.getKey();

			timeoutPending = entry.getValue();
			if (timeoutPending != null) {
				mapTimeoutOfInvokePending.put(invokeOutgoing, timeoutPending);
				Log.d("Invoke= " + entry.getKey() + " : " + timeoutPending.getUpdateTimeout());
			}

		}

		if (!mapTimeoutOfInvokePending.isEmpty()) {
			Log.d("Total Invoke OutGoing : " + mapTimeoutOfInvokePending.size());
			listMapTimeoutOfPending.add(mapTimeoutOfInvokePending);
		}
		else {
			Log.d("Invoke Outgoing to Pending is Empty");
		}

		Log.d("Find Timeout of TransactionID...");
		String transactionIDProcess;
		try {
			Iterator<Entry<String, TimeoutCalculator>> iteratorTransactionTimer = appInstance.getMapTimeoutOfTransactionID()
					.entrySet().iterator();
			while (iteratorTransactionTimer.hasNext()) {
				Entry<String, TimeoutCalculator> entry = (Entry<String, TimeoutCalculator>) iteratorTransactionTimer.next();
				transactionIDProcess = entry.getKey();
				timeoutPending = entry.getValue();
				if (timeoutPending != null) {
					mapTimeoutOfTransactionID.put(transactionIDProcess, timeoutPending);
					Log.d("TransactionID= " + transactionIDProcess + " : " + timeoutPending.getUpdateTimeout());
				}

			}
		}
		catch (Exception ex) {
			Log.d("Find Timeout of TransactionID...[Fail]");
		}

		if (!mapTimeoutOfTransactionID.isEmpty()) {
			Log.d("Total TransactionID Process : " + mapTimeoutOfTransactionID.size());
			listMapTimeoutOfPending.add(mapTimeoutOfTransactionID);
		}
		else {
			Log.d("TransactionID Process is Empty");
		}

		Log.d("Find Timeout of Wait DR...");
		String waitDRProcess;
		try {
			Iterator<Entry<String, TimeoutCalculator>> iteratorWaitDRTimer = appInstance.getMapTimeoutOfWaitDR().entrySet().iterator();
			while (iteratorWaitDRTimer.hasNext()) {
				Entry<String, TimeoutCalculator> entry = (Entry<String, TimeoutCalculator>) iteratorWaitDRTimer.next();
				waitDRProcess = entry.getKey();
				timeoutPending = entry.getValue();
				if (timeoutPending != null) {
					mapTimeoutOfWaitDR.put(waitDRProcess, timeoutPending);
					Log.d("Wait DR = " + waitDRProcess + " : " + timeoutPending.getUpdateTimeout());
				}

			}
		}
		catch (Exception ex) {
			Log.d("Find Timeout of Wait DR...[Fail]");
		}

		if (!mapTimeoutOfWaitDR.isEmpty()) {
			Log.d("Total Wait DR Process : " + mapTimeoutOfWaitDR.size());
			listMapTimeoutOfPending.add(mapTimeoutOfWaitDR);
		}
		else {
			Log.d("Wait DR Process is Empty");
		}

		Log.d("Find Timeout of Wait Refund...");
		String waitRefundProcess;
		try {
			Iterator<Entry<String, TimeoutCalculator>> iteratorWaitRefundTimer = appInstance.getMapTimeoutOfWaitRefund().entrySet().iterator();
			while (iteratorWaitRefundTimer.hasNext()) {
				Entry<String, TimeoutCalculator> entry = (Entry<String, TimeoutCalculator>) iteratorWaitRefundTimer.next();
				waitRefundProcess = entry.getKey();
				timeoutPending = entry.getValue();
				if (timeoutPending != null) {
					mapTimeoutOfWaitRefund.put(waitRefundProcess, timeoutPending);
					Log.d("Wait Refund = " + waitRefundProcess + " : " + timeoutPending.getUpdateTimeout());
				}

			}
		}
		catch (Exception ex) {
			Log.d("Find Timeout of Wait Refund...[Fail]");
		}

		if (!mapTimeoutOfWaitRefund.isEmpty()) {
			Log.d("Total Wait Refund Process : " + mapTimeoutOfWaitRefund.size());
			listMapTimeoutOfPending.add(mapTimeoutOfWaitRefund);
		}
		else {
			Log.d("Wait Refund Process is Empty");
		}

		String minTimeout = Long.toString(getMinTimeoutPending(listMapTimeoutOfPending));

		Log.d("Timeout of Instance : " + minTimeout);

		return minTimeout;

	}

	public static ArrayList<EquinoxRawData> cleanTimeoutIncoming(EC02Instance ec02Instance, ArrayList<EquinoxRawData> rawDatas) {

		String invokeIncoming;

		for (EquinoxRawData rawData : rawDatas) {

			if (EquinoxEvent.TIMEOUT.getCode().equals(rawData.getRet())) {

				rawDatas = new ArrayList<EquinoxRawData>();

				Log.d("GSSO Received Timeout!!!");

				ArrayList<EquinoxRawData> listOutOfTimeout = new ArrayList<EquinoxRawData>();
				ArrayList<EquinoxRawData> listInvokeOFT = new ArrayList<EquinoxRawData>();
				ArrayList<EquinoxRawData> listTransactionOFT = new ArrayList<EquinoxRawData>();
				ArrayList<EquinoxRawData> listWaitDROutOfTimeout = new ArrayList<EquinoxRawData>();
				ArrayList<EquinoxRawData> listWaitRefundOutOfTimeout = new ArrayList<EquinoxRawData>();

				EquinoxRawData rawDataTimeout = null;
				TimeoutCalculator timeToWait;

				Log.d("Checking Invoke is out of Timeout...");
				String invokeIsTimeout;
				Iterator<Entry<String, TimeoutCalculator>> iteratorTimeoutOfInvoke = ec02Instance.getAppInstance()
						.getMapTimeoutOfInvokePending().entrySet().iterator();
				while (iteratorTimeoutOfInvoke.hasNext()) {
					Entry<String, TimeoutCalculator> entry = (Entry<String, TimeoutCalculator>) iteratorTimeoutOfInvoke.next();
					invokeIsTimeout = entry.getKey();
					timeToWait = entry.getValue();
					if (timeToWait != null && timeToWait.isOutOfTimeout()) {

						rawDataTimeout = new EquinoxRawData();
						rawDataTimeout.setInvoke(invokeIsTimeout);
						rawDataTimeout.setRet(EquinoxEvent.TIMEOUT.getCode());
						rawDataTimeout.setType(EventAction.RESPONSE);

						listInvokeOFT.add(rawDataTimeout);

						Log.d("Out of Timeout Invoke : " + invokeIsTimeout);
						/**
						 * Clear Invoke is Out of Timeout
						 */
						iteratorTimeoutOfInvoke.remove();
					}
				}
				if (!listInvokeOFT.isEmpty()) {
					listOutOfTimeout.addAll(listOutOfTimeout.size(), listInvokeOFT);
				}
				Log.d("Invoke is out of Timeout [" + listInvokeOFT.size() + "]");

				Log.d("Checking TransactionID is out of Timeout...");
				String transactionIsTimeout;
				Iterator<Entry<String, TimeoutCalculator>> iteratorTimeoutOfTransaction = ec02Instance.getAppInstance()
						.getMapTimeoutOfTransactionID().entrySet().iterator();
				while (iteratorTimeoutOfTransaction.hasNext()) {
					Entry<String, TimeoutCalculator> entry = (Entry<String, TimeoutCalculator>) iteratorTimeoutOfTransaction.next();
					transactionIsTimeout = entry.getKey();
					timeToWait = entry.getValue();
					if (timeToWait != null && timeToWait.isOutOfTimeout()) {

						rawDataTimeout = new EquinoxRawData();
						rawDataTimeout.setInvoke(transactionIsTimeout);
						rawDataTimeout.setRet(EquinoxEvent.TIMEOUT.getCode());
						rawDataTimeout.setType(EventAction.REQUEST);

						listTransactionOFT.add(rawDataTimeout);
						ec02Instance.getAppInstance().setTransaction(true);
						
						Log.d("Out of Timeout TransactionID : " + transactionIsTimeout);
						/**
						 * Clear Invoke is Out of Timeout
						 */

						/** REMOVE TRANSACTION ID **/
						iteratorTimeoutOfTransaction.remove();
						ec02Instance.getAppInstance().setTimeoutOfConfirmReq(true);
						ec02Instance.getAppInstance().getTransactionidData().remove(transactionIsTimeout);
						ec02Instance.incrementsStat(Statistic.GSSO_NOT_RECEIVED_CONFIRM_OTP.getStatistic());
					}
				}
				if (!listTransactionOFT.isEmpty()) {
					listOutOfTimeout.addAll(listOutOfTimeout.size(), listTransactionOFT);
				}
				Log.d("TransactionID is out of Timeout [" + listTransactionOFT.size() + "]");

				Log.d("Checking Wait DR is out of Timeout...");
				String drIsTimeout;
				Iterator<Entry<String, TimeoutCalculator>> iteratorTimeoutOfWaitDR = ec02Instance.getAppInstance()
						.getMapTimeoutOfWaitDR().entrySet().iterator();
				while (iteratorTimeoutOfWaitDR.hasNext()) {
					Entry<String, TimeoutCalculator> entry = (Entry<String, TimeoutCalculator>) iteratorTimeoutOfWaitDR.next();
					drIsTimeout = entry.getKey();
					timeToWait = entry.getValue();
					if (timeToWait != null && timeToWait.isOutOfTimeout()) {

						rawDataTimeout = new EquinoxRawData();
						rawDataTimeout.setInvoke(drIsTimeout);
						rawDataTimeout.setRet(EquinoxEvent.TIMEOUT.getCode());
						rawDataTimeout.setType(EventAction.REQUEST);

						listWaitDROutOfTimeout.add(rawDataTimeout);

						Log.d("Out of Timeout Wait DR : " + drIsTimeout);
						/**
						 * Clear Invoke is Out of Timeout
						 */
						iteratorTimeoutOfWaitDR.remove();
						break;
					}
				}
				if (!listWaitDROutOfTimeout.isEmpty()) {
					listOutOfTimeout.addAll(listOutOfTimeout.size(), listWaitDROutOfTimeout);
				}
				Log.d("Wait DR is out of Timeout [" + listWaitDROutOfTimeout.size() + "]");

				Log.d("Checking Wait Refund is out of Timeout...");
				String RefundIsTimeout;
				Iterator<Entry<String, TimeoutCalculator>> iteratorTimeoutOfWaitRefund = ec02Instance.getAppInstance()
						.getMapTimeoutOfWaitRefund().entrySet().iterator();
				while (iteratorTimeoutOfWaitRefund.hasNext()) {
					Entry<String, TimeoutCalculator> entry = (Entry<String, TimeoutCalculator>) iteratorTimeoutOfWaitRefund.next();
					RefundIsTimeout = entry.getKey();
					timeToWait = entry.getValue();
					if (timeToWait != null && timeToWait.isOutOfTimeout()) {

						rawDataTimeout = new EquinoxRawData();
						rawDataTimeout.setInvoke(RefundIsTimeout);
						rawDataTimeout.setRet(EquinoxEvent.TIMEOUT.getCode());
						rawDataTimeout.setType(EventAction.RESPONSE);

						listWaitRefundOutOfTimeout.add(rawDataTimeout);

						Log.d("Out of Timeout Wait Refund : " + RefundIsTimeout);
						/**
						 * Clear Invoke is Out of Timeout
						 */
						iteratorTimeoutOfWaitRefund.remove();
					}
				}
				if (!listWaitRefundOutOfTimeout.isEmpty()) {
					listOutOfTimeout.addAll(listOutOfTimeout.size(), listWaitRefundOutOfTimeout);
				}
				Log.d("Wait Refund is out of Timeout [" + listWaitRefundOutOfTimeout.size() + "]");

				rawDatas = listOutOfTimeout;
			}
			else {
				invokeIncoming = rawData.getInvoke();
				if (ec02Instance.getAppInstance().getMapTimeoutOfInvokePending().get(invokeIncoming) != null) {
					ec02Instance.getAppInstance().getMapTimeoutOfInvokePending().remove(invokeIncoming);
					Log.d("Reset Time to wait of : " + invokeIncoming);
				}
			}
		}

		return rawDatas;
	}

	public static void initialIncomingOTPReq(APPInstance appInstance, EquinoxRawData rawDatas, String messageType, long reqTimeStamp) {
		ArrayList<String> listProcessing = appInstance.getListInvokeProcessing();
		OrigInvokeProfile invokeProfile;

		String invokeOrigIncoming = rawDatas.getInvoke();
		
		if (!listProcessing.contains(invokeOrigIncoming)) {
			invokeProfile = new OrigInvokeProfile();
			invokeProfile.setOrigEquinoxRawData(rawDatas);
			appInstance.getMapOrigProfile().put(invokeOrigIncoming, invokeProfile);
			appInstance.getMapOrigProfile().get(invokeOrigIncoming).setStartTimeOfInvoke(reqTimeStamp);
			
			String messageXML = rawDatas.getRawDataMessage();
			
			if(messageXML.contains(IdleMessageFormat.SOAP_WS_AUTH_OTP_ID_REQ)){
				appInstance.getMapOrigProfile().get(invokeOrigIncoming).setSendWSOTPRequest(new SendWSOTPRequest());
			}
			else if(messageXML.contains(IdleMessageFormat.SOAP_WS_AUTH_OTP_REQ)){
				appInstance.getMapOrigProfile().get(invokeOrigIncoming).setSendWSOTPRequest(new SendWSOTPRequest());
			}
			else if(messageXML.contains(IdleMessageFormat.SOAP_WS_CREATE_OTP_REQ)){
				appInstance.getMapOrigProfile().get(invokeOrigIncoming).setSendWSOTPRequest(new SendWSOTPRequest());
			}
			else if(messageXML.contains(IdleMessageFormat.SOAP_WS_GENERATE_OTP_REQ)){
				appInstance.getMapOrigProfile().get(invokeOrigIncoming).setSendWSOTPRequest(new SendWSOTPRequest());
			}
			else if(messageXML.contains(IdleMessageFormat.SOAP_SEND_OTP_REQ)){
				appInstance.getMapOrigProfile().get(invokeOrigIncoming).setGssoOTPRequest(new GssoOTPRequest());
			}
			
			appInstance.getMapOrigProfile().get(invokeOrigIncoming).setIncomingMessageType(messageType);
		}
	}
}
