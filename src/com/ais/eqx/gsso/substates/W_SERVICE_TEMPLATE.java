package com.ais.eqx.gsso.substates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.ais.eqx.gsso.enums.ConfigName;
import com.ais.eqx.gsso.enums.E01ResultCode;
import com.ais.eqx.gsso.enums.EquinoxEvent;
import com.ais.eqx.gsso.enums.EventLog;
import com.ais.eqx.gsso.enums.IncomingMessageType;
import com.ais.eqx.gsso.enums.JsonResultCode;
import com.ais.eqx.gsso.enums.LogDestNodeResultDesc;
import com.ais.eqx.gsso.enums.SoapResultCode;
import com.ais.eqx.gsso.enums.Statistic;
import com.ais.eqx.gsso.enums.SubStates;
import com.ais.eqx.gsso.exception.ValidationException;
import com.ais.eqx.gsso.instances.APPInstance;
import com.ais.eqx.gsso.instances.EC02Instance;
import com.ais.eqx.gsso.instances.GssoE01Datas;
import com.ais.eqx.gsso.instances.GssoGenPasskeyRequest;
import com.ais.eqx.gsso.instances.GssoOTPRequest;
import com.ais.eqx.gsso.instances.GssoServiceTemplate;
import com.ais.eqx.gsso.instances.MapDetailsAndConfigType;
import com.ais.eqx.gsso.instances.OrigInvokeProfile;
import com.ais.eqx.gsso.instances.SendOneTimePWRequest;
import com.ais.eqx.gsso.instances.SendWSOTPRequest;
import com.ais.eqx.gsso.interfaces.EventCtype;
import com.ais.eqx.gsso.interfaces.EventName;
import com.ais.eqx.gsso.interfaces.GlobaldataEventType;
import com.ais.eqx.gsso.interfaces.IAFSubState;
import com.ais.eqx.gsso.interfaces.OTPChannel;
import com.ais.eqx.gsso.utils.ConfigureTool;
import com.ais.eqx.gsso.utils.GssoComposeDebugLog;
import com.ais.eqx.gsso.utils.GssoComposeDetailsLog;
import com.ais.eqx.gsso.utils.GssoComposeSummaryLog;
import com.ais.eqx.gsso.utils.GssoConstructMessage;
import com.ais.eqx.gsso.utils.GssoDataManagement;
import com.ais.eqx.gsso.utils.GssoGenerator;
import com.ais.eqx.gsso.utils.InvokeFilter;
import com.ais.eqx.gsso.validator.VerifyMessage;

import ec02.af.abstracts.AbstractAF;
import ec02.af.data.EquinoxRawData;
import ec02.af.utils.Log;
import ec02.common.data.E01Data;

public class W_SERVICE_TEMPLATE implements IAFSubState {

	private EC02Instance				ec02Instance;
	private APPInstance					appInstance;

	private EquinoxRawData				rawDataIncoming;
	private ArrayList<EquinoxRawData>	rawDatasOutgoing;
	private AbstractAF					abstractAF;
	private String						nextState;
	private E01Data						e01Data;

	private String						incomingInvoke;
	private JsonResultCode				jsonCode;
	private String						description					= "";
	private String						path						= "";
	private String						origInvoke;

	private String						destNodeName				= "E01";
	private String						destNodeResultDescription	= "null";
	private String						destNodeResultCode			= "null";
	private String						destNodeCommand				= "";

	private GssoComposeDetailsLog		composeDetailsLog;
	private GssoComposeSummaryLog		composeSummary;
	private GssoComposeDebugLog			composeDebugLog;
	private GssoE01Datas				gssoE01Datas;
	private MapDetailsAndConfigType		mapDetails;
	private GssoServiceTemplate			thisServiceTemplate;

	@Override
	public ArrayList<EquinoxRawData> doActionSubStateE01(AbstractAF abstractAF, EC02Instance ec02Instance,
			EquinoxRawData equinoxRawData, E01Data e01Data) {

		this.nextState = SubStates.W_SERVICE_TEMPLATE.toString();
		/** END **/

		/************** INITIAL *****************/
		serviceTemplateInitInstanceAndLog(equinoxRawData, abstractAF, e01Data, ec02Instance);

		/************** CODING ******************/
//		 System.out.println("Start W_SERVICE_TEMPLATE");

		/** NORMAL FLOW **/
		HashMap<String, ArrayList<String>> maplistWQuiryService = appInstance.getMaplistWQuiryService();
		/** TIMEOUT **/
		if (equinoxRawData.getRet().equals(EquinoxEvent.TIMEOUT.getCode())) {

			ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_E01_QUERYSERVICETEMPLATE_REQUEST_TIMEOUT.getStatistic());
			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_E01_QUERYSERVICETEMPLATE_REQUEST_TIMEOUT.getStatistic());
			}
			this.destNodeResultDescription = LogDestNodeResultDesc.CONNECTION_TIMEOUT.getLogDestNodeResultDesc();

			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
			// DETAILS======================================================
			appInstance.getMapOrigInvokeEventDetailInput().put(origInvoke, EventLog.QUERY_SERVICE_TEMPLATE.getEventLog());
			appInstance.getMapOrigInvokeDetailScenario().put(origInvoke,
					appInstance.getMapOrigProfile().get(origInvoke).getScenarioName());

			try {
				this.composeDetailsLog.initialIncoming(equinoxRawData, appInstance);
				this.composeDetailsLog.addScenario(appInstance, equinoxRawData, SubStates.W_SERVICE_TEMPLATE.name());
			}
			catch (Exception ex) {
				Log.e(ex.getMessage());
			}
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
			// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			String serviceName;
			if(appInstance.getMapOrigProfile().get(origInvoke).getGssoOTPRequest()!=null){
				serviceName = appInstance.getMapOrigProfile().get(origInvoke).getGssoOTPRequest().getSendOneTimePW().getService();
			}
			else{
				serviceName = appInstance.getMapOrigProfile().get(origInvoke).getSendWSOTPRequest().getService();
			}
			
			/** GET ALL LIST WAIT SERVICE **/
			if (maplistWQuiryService.get(serviceName) != null) {
				ArrayList<String> listWaitService = maplistWQuiryService.get(serviceName);
				for (String invokeOrigWaiting : listWaitService) {
					this.destNodeCommand = EventLog.QUERY_SERVICE_TEMPLATE.getEventLog();
					this.jsonCode = JsonResultCode.E01_TIMEOUT;
					this.description = JsonResultCode.E01_TIMEOUT.getDescription();

					this.composeDetailsLog.addScenario(appInstance, equinoxRawData, SubStates.W_SERVICE_TEMPLATE.name());

					// ===============================================WRITE
					// SUMMARY======================================================
					/** INITIATE SUMMARY-LOG **/
//					this.composeSummary = new GssoComposeSummaryLog(abstractAF);
					this.composeSummary.setWriteSummary();

					if (description.equalsIgnoreCase("GSSO_MISSING_SERVICE_TEMPLATE_WHEN_COMPARE")) {
						destNodeCommand = "QueryServiceTemplate";
						destNodeResultCode = "null";
						destNodeResultDescription = "Oper is not found";
					}
					this.composeSummary.addDestinationBean(destNodeName, destNodeCommand, destNodeResultCode,
							destNodeResultDescription);
					// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
					// SUMMARY^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

					if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
						// ===============================================DEBUG
						// LOG==========================================================
						/** writeLog LOG **/
						if (description.equalsIgnoreCase("GSSO_MISSING_SERVICE_TEMPLATE_WHEN_COMPARE")) {
							composeDebugLog.serviceTemplateMisMatch();
						}
						else {
							composeDebugLog.setFailureAvp(path + " " + description);
						}
						// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
						// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					}

					rawDatasOutgoing.add(GssoConstructMessage.createReturnErrorMessageServiceTemplate(invokeOrigWaiting, ec02Instance,
							jsonCode, composeDebugLog, composeSummary));

					/* REMOVE PROFILE */
					GssoDataManagement.removeProfile(invokeOrigWaiting, appInstance);
				}
			}
		}
		else if (GlobaldataEventType.NORMAL.equals(abstractAF.getEquinoxUtils().getGlobalData().getGlobaldataEventType())) {

			/** VALID MESSAGE **/
			if (messageValidator(abstractAF, equinoxRawData, e01Data)) {
 				writeLogSuccess(abstractAF, equinoxRawData, e01Data);

				/** EXTRACT SERVICE TEMPLATE **/
				extractE01ToObject(abstractAF);

				String serviceName = abstractAF.getEquinoxUtils().getGlobalData().getDataResult().get(this.incomingInvoke).getKeyObjects()
						.get(0).getKey1();
				
				/** GET ALL LIST WAIT SERVICE **/
				if (maplistWQuiryService != null && maplistWQuiryService.get(serviceName) != null) {
					ArrayList<String> listWaitService = maplistWQuiryService.get(serviceName);
					for (String invokeOrigWaiting : listWaitService) {
						normalFlow(invokeOrigWaiting, e01Data, equinoxRawData, abstractAF);
					}
					maplistWQuiryService.remove(serviceName);
				}
				
			}
			/** INVALID MESSAGE **/
			else {
				String serviceName;
				if(appInstance.getMapOrigProfile().get(origInvoke).getGssoOTPRequest()!=null){
					serviceName = appInstance.getMapOrigProfile().get(origInvoke).getGssoOTPRequest().getSendOneTimePW().getService();
				}
				else{
					serviceName = appInstance.getMapOrigProfile().get(origInvoke).getSendWSOTPRequest().getService();
				}
				
				/** GET ALL LIST WAIT SERVICE **/
				if (maplistWQuiryService != null && maplistWQuiryService.get(serviceName) != null) {
					ArrayList<String> listWaitService = maplistWQuiryService.get(serviceName);
					for (String invokeOrigWaiting : listWaitService) {

						// ===============================================WRITE
						// SUMMARY======================================================
						/** INITIATE SUMMARY-LOG **/
//						this.composeSummary = new GssoComposeSummaryLog(abstractAF);
						this.composeSummary.setWriteSummary();

						if (description.equalsIgnoreCase("GSSO_MISSING_SERVICE_TEMPLATE_WHEN_COMPARE")) {
							destNodeCommand = "QueryServiceTemplate";
							destNodeResultCode = "null";
							destNodeResultDescription = "Oper is not found";

						}
						this.composeSummary.addDestinationBean(destNodeName, destNodeCommand, destNodeResultCode,
								destNodeResultDescription);
						// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
						// SUMMARY^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

						if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
							// ===============================================DEBUG
							// LOG==========================================================
							/** writeLog LOG **/
							if (description.equalsIgnoreCase("GSSO_MISSING_SERVICE_TEMPLATE_WHEN_COMPARE")) {
								composeDebugLog.serviceTemplateMisMatch();
							}
							else {
								composeDebugLog.setFailureAvp(path + " " + description);
							}
							// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
							// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
						}

						rawDatasOutgoing.add(GssoConstructMessage.createReturnErrorMessageServiceTemplate(invokeOrigWaiting,
								ec02Instance, jsonCode, composeDebugLog, composeSummary));

						/* REMOVE PROFILE */
						GssoDataManagement.removeProfile(invokeOrigWaiting, appInstance);
					}
				}
			}
		}
		/** NO FLOW **/
		else {
			errorCase(abstractAF, equinoxRawData, e01Data);
			
			String serviceName;
			if(appInstance.getMapOrigProfile().get(origInvoke).getGssoOTPRequest()!=null){
				serviceName = appInstance.getMapOrigProfile().get(origInvoke).getGssoOTPRequest().getSendOneTimePW().getService();
			}
			else{
				serviceName = appInstance.getMapOrigProfile().get(origInvoke).getSendWSOTPRequest().getService();
			}
			
			/** GET ALL LIST WAIT SERVICE **/
			if (maplistWQuiryService.get(serviceName) != null) {
				ArrayList<String> listWaitService = maplistWQuiryService.get(serviceName);
				for (String invokeOrigWaiting : listWaitService) {

					// ===============================================WRITE
					// SUMMARY======================================================
					/** INITIATE SUMMARY-LOG **/
//					this.composeSummary = new GssoComposeSummaryLog(abstractAF);
					composeSummary.setWriteSummary();

					if (description.equalsIgnoreCase("GSSO_MISSING_SERVICE_TEMPLATE_WHEN_COMPARE")) {
						destNodeCommand = "QueryServiceTemplate";
						destNodeResultCode = "null";
						destNodeResultDescription = "Oper is not found";
					}
					composeSummary.addDestinationBean(destNodeName, destNodeCommand, destNodeResultCode, destNodeResultDescription);
					// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
					// SUMMARY^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

					if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
						// ===============================================DEBUG
						// LOG==========================================================
						/** writeLog LOG **/
						if (description.equalsIgnoreCase("GSSO_MISSING_SERVICE_TEMPLATE_WHEN_COMPARE")) {
							composeDebugLog.serviceTemplateMisMatch();
						}
						else {
							composeDebugLog.setFailureAvp(path + " " + description);
						}
						// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
						// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					}

					rawDatasOutgoing.add(GssoConstructMessage.createReturnErrorMessageServiceTemplate(invokeOrigWaiting, ec02Instance,
							jsonCode, composeDebugLog, composeSummary));

					/* REMOVE PROFILE */
					GssoDataManagement.removeProfile(invokeOrigWaiting, appInstance);
				}
			}
		}

		/* SAVE LOG */
		serviceTemplateSaveLog();

		return this.rawDatasOutgoing;
	}
	
	private boolean messageValidator(AbstractAF abstractAF, EquinoxRawData equinoxRawData, E01Data e01Data) {
		boolean isMessageValid = false;

		Map<String, E01Data> dataResult = abstractAF.getEquinoxUtils().getGlobalData().getDataResult();
		try {
			VerifyMessage.verifyE01ServiceTemplate(abstractAF, appInstance, equinoxRawData, this.origInvoke);

			isMessageValid = true;

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				this.composeDebugLog.messageResponseSuccess(dataResult.get(this.incomingInvoke).getResultCode());
			}

		}
		catch (ValidationException e) {
			isMessageValid = false;

			String resultCode = "";
			String logStat = "";
			String logEvent = "";
			logEvent = EventLog.QUERY_SERVICE_TEMPLATE.getEventLog();

			if (dataResult.get(this.incomingInvoke).getResultCode() == null
					|| dataResult.get(this.incomingInvoke).getResultCode().isEmpty()) {
				this.destNodeResultDescription = LogDestNodeResultDesc.RESULT_CODE_ERROR.getLogDestNodeResultDesc();
				resultCode = "missing";
				logStat = Statistic.GSSO_RECEIVED_BAD_E01_QUERYSERVICETEMPLATE_RESPONSE.getStatistic();
			}
			else if (dataResult.get(this.incomingInvoke).getResultCode().equals(E01ResultCode.NO_SUCH_OBJECT.getCode())) {
				this.destNodeResultCode = E01ResultCode.NO_SUCH_OBJECT.getCode();
				this.destNodeResultDescription = E01ResultCode.NO_SUCH_OBJECT.getDescription();
				resultCode = dataResult.get(this.incomingInvoke).getResultCode();
				logStat = Statistic.GSSO_RECEIVED_E01_QUERYSERVICETEMPLATE_NOT_FOUND.getStatistic();
			}
			else if (!dataResult.get(this.incomingInvoke).getResultCode().equals(E01ResultCode.SUCCESS.getCode())) {
				this.destNodeResultDescription = LogDestNodeResultDesc.RESULT_CODE_ERROR.getLogDestNodeResultDesc();
				resultCode = dataResult.get(this.incomingInvoke).getResultCode();
				logStat = Statistic.GSSO_RECEIVED_E01_QUERYSERVICETEMPLATE_RESPONSE_RESULTCODE_ERROR.getStatistic();
			}
			else {
				this.destNodeResultDescription = e.getMandatoryPath() + " " + e.getMessage();
				resultCode = dataResult.get(this.incomingInvoke).getResultCode();
				if("sessionId".equals(e.getMandatoryPath()) || "refId".equals(e.getMandatoryPath()))
					logStat = Statistic.GSSO_RECEIVED_E01_QUERYSERVICETEMPLATE_RESPONSE_SUCCESS.getStatistic();
				else
					logStat = Statistic.GSSO_RECEIVED_BAD_E01_QUERYSERVICETEMPLATE_RESPONSE.getStatistic();
			}

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				// ===============================================DEBUG
				// LOG==========================================================
				this.composeDebugLog.messageResponseFailed(resultCode);
				this.composeDebugLog.setMessageValidator(EventName.INCOMPLETE);
				this.composeDebugLog.addStatisticIn(logStat);
				// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
				// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			}

			ec02Instance.incrementsStat(logStat);
			// ===============================================WRITE
			// DETAILS======================================================
			appInstance.getMapOrigInvokeEventDetailInput().put(origInvoke, logEvent);
			appInstance.getMapOrigInvokeDetailScenario().put(origInvoke,
					appInstance.getMapOrigProfile().get(origInvoke).getScenarioName());

			try {
				this.composeDetailsLog.initialIncomingFromE01(abstractAF, appInstance, equinoxRawData, e01Data);
				this.composeDetailsLog.addScenarioFromE01(appInstance, e01Data, SubStates.W_SERVICE_TEMPLATE.name(), this.nextState,
						equinoxRawData);
			}
			catch (Exception ex) {
				Log.e(ex.getMessage());
			}
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
			// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

			this.destNodeCommand = logEvent;
			this.jsonCode = e.getJsonResultCode();
			this.description = e.getMessage();
			this.path = e.getMandatoryPath();

		}

		return isMessageValid;
	}

	private void writeLogSuccess(AbstractAF abstractAF, EquinoxRawData equinoxRawData, E01Data e01Data) {

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			// ===============================================DEBUG
			// LOG==========================================================
			this.composeDebugLog.setMessageValidator(EventName.COMPLETE);
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
			// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		}

		/** VALID OTP REQ STATICTIC **/
		ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_E01_QUERYSERVICETEMPLATE_RESPONSE_SUCCESS.getStatistic());
		// ===============================================WRITE
		// DETAILS======================================================
		appInstance.getMapOrigInvokeEventDetailInput().put(origInvoke, EventLog.QUERY_SERVICE_TEMPLATE.getEventLog());
		appInstance.getMapOrigInvokeDetailScenario()
				.put(origInvoke, appInstance.getMapOrigProfile().get(origInvoke).getScenarioName());

		try {
			this.composeDetailsLog.initialIncomingFromE01(abstractAF, appInstance, equinoxRawData, e01Data);
			this.composeDetailsLog.addScenarioFromE01(appInstance, e01Data, SubStates.W_SERVICE_TEMPLATE.name(), this.nextState,
					equinoxRawData);
		}
		catch (Exception e) {
			Log.e(e.getMessage());
		}
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_E01_QUERYSERVICETEMPLATE_RESPONSE_SUCCESS.getStatistic());
		}
	}

	private void errorCase(AbstractAF abstractAF, EquinoxRawData equinoxRawData, E01Data e01Data) {

		this.destNodeCommand = EventLog.QUERY_SERVICE_TEMPLATE.getEventLog();

		String event = abstractAF.getEquinoxUtils().getGlobalData().getGlobaldataEventType();
		if (GlobaldataEventType.REJECT.equals(event)) {
			ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_E01_QUERYSERVICETEMPLATE_RESPONSE_REJECT.getStatistic());

			this.destNodeResultDescription = LogDestNodeResultDesc.REJECT.getLogDestNodeResultDesc();

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_E01_QUERYSERVICETEMPLATE_RESPONSE_REJECT.getStatistic());
			}
			// ===============================================WRITE
			// DETAILS======================================================
			appInstance.getMapOrigInvokeEventDetailInput().put(origInvoke, EventLog.QUERY_SERVICE_TEMPLATE.getEventLog());
			appInstance.getMapOrigInvokeDetailScenario().put(origInvoke,
					appInstance.getMapOrigProfile().get(origInvoke).getScenarioName());
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
			// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

			this.jsonCode = JsonResultCode.E01_ERROR;
		}

		else if (GlobaldataEventType.ABORT.equals(event)) {
			ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_E01_QUERYSERVICETEMPLATE_RESPONSE_ABORT.getStatistic());

			this.destNodeResultDescription = LogDestNodeResultDesc.ABORT.getLogDestNodeResultDesc();

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_E01_QUERYSERVICETEMPLATE_RESPONSE_ABORT.getStatistic());
			}

			// ===============================================WRITE
			// DETAILS======================================================
			appInstance.getMapOrigInvokeEventDetailInput().put(origInvoke, EventLog.QUERY_SERVICE_TEMPLATE.getEventLog());
			appInstance.getMapOrigInvokeDetailScenario().put(origInvoke,
					appInstance.getMapOrigProfile().get(origInvoke).getScenarioName());
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
			// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

			this.jsonCode = JsonResultCode.E01_ERROR;
		}

		else if (GlobaldataEventType.ERROR.equals(event)) {
			ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_E01_QUERYSERVICETEMPLATE_RESPONSE_ERROR.getStatistic());

			this.destNodeResultDescription = LogDestNodeResultDesc.ERROR.getLogDestNodeResultDesc();

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_E01_QUERYSERVICETEMPLATE_RESPONSE_ERROR.getStatistic());
			}

			// ===============================================WRITE
			// DETAILS======================================================
			appInstance.getMapOrigInvokeEventDetailInput().put(origInvoke, EventLog.QUERY_SERVICE_TEMPLATE.getEventLog());
			appInstance.getMapOrigInvokeDetailScenario().put(origInvoke,
					appInstance.getMapOrigProfile().get(origInvoke).getScenarioName());
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
			// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

			this.jsonCode = JsonResultCode.E01_ERROR;
		}

		else if (GlobaldataEventType.TIMEOUT.equals(event)) {
			ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_E01_QUERYSERVICETEMPLATE_REQUEST_TIMEOUT.getStatistic());

			this.destNodeResultDescription = LogDestNodeResultDesc.CONNECTION_TIMEOUT.getLogDestNodeResultDesc();

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_E01_QUERYSERVICETEMPLATE_REQUEST_TIMEOUT.getStatistic());
			}

			// ===============================================WRITE
			// DETAILS======================================================
			appInstance.getMapOrigInvokeEventDetailInput().put(origInvoke, EventLog.QUERY_SERVICE_TEMPLATE.getEventLog());
			appInstance.getMapOrigInvokeDetailScenario().put(origInvoke,
					appInstance.getMapOrigProfile().get(origInvoke).getScenarioName());
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
			// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

			this.jsonCode = JsonResultCode.E01_TIMEOUT;
		}

		// ===============================================WRITE
		// DETAILS======================================================
		try {
			this.composeDetailsLog.initialIncomingFromE01(abstractAF, appInstance, equinoxRawData, e01Data);
			this.composeDetailsLog.addScenarioFromE01(appInstance, e01Data, SubStates.W_SERVICE_TEMPLATE.name(), this.nextState,
					equinoxRawData);
		}
		catch (Exception ex) {
			Log.e(ex.getMessage());
		}
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

	}

	private void extractE01ToObject(AbstractAF abstractAF) {
		String dataFromE01 = abstractAF.getEquinoxUtils().getGlobalData().getDataResult().get(this.incomingInvoke).getData();
		gssoE01Datas = GssoDataManagement.extractGssoServiceTemplate(dataFromE01);
		
		if (gssoE01Datas != null && gssoE01Datas.getServiceTemplate() != null && gssoE01Datas.getServiceTemplate().size() != 0) {
			String serviceName = abstractAF.getEquinoxUtils().getGlobalData().getDataResult().get(this.incomingInvoke).getKeyObjects()
					.get(0).getKey1();
			appInstance.getMapE01dataofService().put(serviceName.toUpperCase(), gssoE01Datas);
		}
		
	}

	private void normalFlow(String invokeOrigWaiting, E01Data e01Data, EquinoxRawData equinoxRawData, AbstractAF abstractAF) {
		String origInvoke = invokeOrigWaiting;
		OrigInvokeProfile origInvokeProfile = appInstance.getMapOrigProfile().get(origInvoke);
		

		APPInstance appInstance = ec02Instance.getAppInstance();
		OrigInvokeProfile origProfile = appInstance.getMapOrigProfile().get(origInvoke);
		
		EquinoxRawData origEquinoxRawData = origInvokeProfile.getOrigEquinoxRawData();
		GssoOTPRequest 				otpRequest 	= new GssoOTPRequest();
		SendOneTimePWRequest 		sendOneTimePW = new SendOneTimePWRequest();
		SendWSOTPRequest		sendWSAuthOTPRequest = new SendWSOTPRequest();
		
		
		if(origProfile.getSendWSOTPRequest()!=null){
			sendWSAuthOTPRequest = origInvokeProfile.getSendWSOTPRequest();
			thisServiceTemplate = GssoDataManagement.findServiceTemplateMatchAccountType(appInstance, sendWSAuthOTPRequest.getService(),
					appInstance.getProfile().getOper());
			
		}
		else if(origProfile.getGssoOTPRequest()!=null){
			otpRequest = origInvokeProfile.getGssoOTPRequest();
			sendOneTimePW = otpRequest.getSendOneTimePW();
			thisServiceTemplate = GssoDataManagement.findServiceTemplateMatchAccountType(appInstance, sendOneTimePW.getService(),
					appInstance.getProfile().getOper());
			
		}

		/* ACCOUNT TYPE MATCH SERVICE TEMPLATE */
		boolean isFoundServiceTemplate = thisServiceTemplate != null;
		if (isFoundServiceTemplate) {
			
			/* SAVE ServiceTemplate to instance */
			this.appInstance.getMapOrigProfile().get(origInvoke).setRefundFlag(thisServiceTemplate.getRefundFlag());
			String ctype = origInvokeProfile.getOrigEquinoxRawData().getCType();
			
			origInvokeProfile.setOrderRefLog(GssoGenerator.generateOrderReference(
					ConfigureTool.getConfigure(ConfigName.APPLICATION_NODENAME), appInstance.getListOrderReference()));

			if (!appInstance.getListInvokeProcessing().contains(origInvoke)) {
				appInstance.getListInvokeProcessing().add(origInvoke);
			}

			if (ctype.equalsIgnoreCase(EventCtype.XML)) {
				
				/** IDLE_SEND_OTP_REQ **/
				if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.SEND_OTP_SOAP.getMessageType())) {
					String otpChannel = null;
					
					
						otpChannel = sendOneTimePW.getOtpChannel();
						/** CHOOSE LIFE TIMEOUT MIN **/
						GssoDataManagement.chooseDefaultValues(otpRequest, thisServiceTemplate);
						
						/* CREATE TRANSACTION ID PROFILE */
						origInvokeProfile.setServiceKey(appInstance.getMapE01dataofService().get(sendOneTimePW.getService().toUpperCase())
								.getServiceKey());
					
					
					/* CREATE TRANSACTION ID PROFILE */
					origInvokeProfile.setTransactionID(GssoDataManagement.createNewTransaction(appInstance, origInvoke,
							thisServiceTemplate.getSeedkey()));
					

					/** FOR SMS **/
					if (otpChannel.equalsIgnoreCase(OTPChannel.SMS)) {
						rawDatasOutgoing.add(GssoConstructMessage.createSMSReqMessage(origInvoke, thisServiceTemplate, ec02Instance,
								composeDebugLog));

					}
					/** FOR EMAIL **/
					else if (otpChannel.equalsIgnoreCase(OTPChannel.EMAIL)) {
						rawDatasOutgoing.add(GssoConstructMessage.createEMAILReqMessage(origInvoke, thisServiceTemplate, ec02Instance,
								composeDebugLog));

					}
					/** FOR ALL **/
					else if(otpChannel.equalsIgnoreCase(OTPChannel.ALL)) {
						rawDatasOutgoing.add(GssoConstructMessage.createSMSReqMessage(origInvoke, thisServiceTemplate, ec02Instance,
								composeDebugLog));

						rawDatasOutgoing.add(GssoConstructMessage.createEMAILReqMessage(origInvoke, thisServiceTemplate, ec02Instance,
								composeDebugLog));

					}
				}
				else if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.WS_AUTHEN_OTP_SOAP.getMessageType())) {
					boolean isSms = false;
					boolean isEmail = false;
						/** CHOOSE LIFE TIMEOUT MIN **/
						GssoDataManagement.chooseDefaultValuesWSCommand(sendWSAuthOTPRequest, thisServiceTemplate);
						
						if(sendWSAuthOTPRequest.getOtpMobile()!=null&&(!sendWSAuthOTPRequest.getOtpMobile().isEmpty())){
							isSms = true;
						}
						if(sendWSAuthOTPRequest.getEmail()!=null&&(!sendWSAuthOTPRequest.getEmail().isEmpty())){
							isEmail = true;
						}

					/* CREATE TRANSACTION ID PROFILE */
					origInvokeProfile.setServiceKey(appInstance.getMapE01dataofService().get(sendWSAuthOTPRequest.getService().toUpperCase())
							.getServiceKey());
						
						
					/* CREATE TRANSACTION ID PROFILE */
					origInvokeProfile.setTransactionID(GssoDataManagement.createNewTransaction(appInstance, origInvoke,
							thisServiceTemplate.getSeedkey()));
					
					/** FOR SMS **/
					if (isSms == true && isEmail == false) {
						rawDatasOutgoing.add(GssoConstructMessage.createSMSReqMessage(origInvoke, thisServiceTemplate, ec02Instance,
								composeDebugLog));

					}
					/** FOR EMAIL **/
					else if (isEmail==true && isSms == false) {
						rawDatasOutgoing.add(GssoConstructMessage.createEMAILReqMessage(origInvoke, thisServiceTemplate, ec02Instance,
								composeDebugLog));

					}
					/** FOR ALL **/
					else if(isSms == true && isEmail == true) {
						rawDatasOutgoing.add(GssoConstructMessage.createSMSReqMessage(origInvoke, thisServiceTemplate, ec02Instance,
								composeDebugLog));

						rawDatasOutgoing.add(GssoConstructMessage.createEMAILReqMessage(origInvoke, thisServiceTemplate, ec02Instance,
								composeDebugLog));

					}
					
					
				}
				else if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.WS_AUTHEN_OTP_ID_SOAP.getMessageType())) {
					boolean isSms = false;
					boolean isEmail = false;
					
						/** CHOOSE LIFE TIMEOUT MIN **/
						GssoDataManagement.chooseDefaultValuesWSCommand(sendWSAuthOTPRequest, thisServiceTemplate);
						
						if(sendWSAuthOTPRequest.getOtpMobile()!=null&&(!sendWSAuthOTPRequest.getOtpMobile().isEmpty())){
							isSms = true;
						}
						if(sendWSAuthOTPRequest.getEmail()!=null&&(!sendWSAuthOTPRequest.getEmail().isEmpty())){
							isEmail = true;
						}
					origInvokeProfile.setServiceKey(appInstance.getMapE01dataofService().get(sendWSAuthOTPRequest.getService().toUpperCase())
								.getServiceKey());
						
						
						
					/* CREATE TRANSACTION ID PROFILE */
					origInvokeProfile.setTransactionID(GssoDataManagement.createNewTransaction(appInstance, origInvoke,
							thisServiceTemplate.getSeedkey()));
					
					/** FOR SMS **/
					if (isSms == true && isEmail == false) {
						rawDatasOutgoing.add(GssoConstructMessage.createSMSReqMessage(origInvoke, thisServiceTemplate, ec02Instance,
								composeDebugLog));

					}
					/** FOR EMAIL **/
					else if (isEmail==true && isSms == false) {
						rawDatasOutgoing.add(GssoConstructMessage.createEMAILReqMessage(origInvoke, thisServiceTemplate, ec02Instance,
								composeDebugLog));

					}
					/** FOR ALL **/
					else if(isSms == true && isEmail == true) {
						rawDatasOutgoing.add(GssoConstructMessage.createSMSReqMessage(origInvoke, thisServiceTemplate, ec02Instance,
								composeDebugLog));

						rawDatasOutgoing.add(GssoConstructMessage.createEMAILReqMessage(origInvoke, thisServiceTemplate, ec02Instance,
								composeDebugLog));

					}
					
				}
				else if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.WS_CREATE_OTP_SOAP.getMessageType())) {
					/** CHOOSE LIFE TIMEOUT MIN **/
					GssoDataManagement.chooseDefaultValuesWSCommand(sendWSAuthOTPRequest, thisServiceTemplate);
					
					origInvokeProfile.setServiceKey(appInstance.getMapE01dataofService().get(sendWSAuthOTPRequest.getService().toUpperCase())
							.getServiceKey());
						
					/* CREATE TRANSACTION ID PROFILE */
					origInvokeProfile.setTransactionID(GssoDataManagement.createNewTransaction(appInstance, origInvoke,
							thisServiceTemplate.getSeedkey()));
					
					/** FOR SMS **/
					rawDatasOutgoing.add(GssoConstructMessage.createSMSReqMessage(origInvoke, thisServiceTemplate, ec02Instance,
							composeDebugLog));

				}
				else if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.WS_GENERATE_ONETIMEPW_SOAP.getMessageType())) {
					
					/** CHOOSE LIFE TIMEOUT MIN **/
					GssoDataManagement.chooseDefaultValuesWSCommand(sendWSAuthOTPRequest, thisServiceTemplate);
					
					origInvokeProfile.setServiceKey(appInstance.getMapE01dataofService().get(sendWSAuthOTPRequest.getService().toUpperCase())
							.getServiceKey());
						
					/* CREATE TRANSACTION ID PROFILE */
					origInvokeProfile.setTransactionID(GssoDataManagement.createNewTransaction(appInstance, origInvoke,
							thisServiceTemplate.getSeedkey()));
					
					/** FOR SMS **/
					rawDatasOutgoing.add(GssoConstructMessage.createSMSReqMessage(origInvoke, thisServiceTemplate, ec02Instance,
							composeDebugLog));

				}
				
				/** IDLE_GENERATE_PK **/
				else if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.GENERATE_PASSKEY_SOAP.getMessageType())) {

					GssoGenPasskeyRequest genPasskeyReq = GssoDataManagement.extractGssoGenPasskeyRequest(origEquinoxRawData);

					/* compose gen passkey response success */
					rawDatasOutgoing.add(GssoConstructMessage.createGenpassResp(appInstance,
							origInvokeProfile.getOrigEquinoxRawData(), thisServiceTemplate, composeDebugLog, genPasskeyReq));

					ec02Instance.incrementsStat(Statistic.GSSO_RETURN_GENERATEPASSKEY_RESPONSE_SUCCESS.getStatistic());
					// =========== WRITE DETAILS ===========
					appInstance.getMapOrigInvokeEventDetailOutput().put(origEquinoxRawData.getInvoke(),
							EventLog.GENARATE_PASSKEY.getEventLog());
					// ^^^^^^^^^^^ WRITE DETAILS ^^^^^^^^^^^

					if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
						// =========== DEBUG LOG ===========
						/** writeLog LOG **/
						composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_GENERATEPASSKEY_RESPONSE_SUCCESS.getStatistic());
						// ^^^^^^^^^^^ DEBUG LOG ^^^^^^^^^^^
					}

					// =========== WRITE SUMMARY ===========
					try {
						composeSummary.setWriteSummary();
						composeSummary.initialSummary(this.appInstance, appInstance.getMapOrigProfile().get(origEquinoxRawData.getInvoke())
								.getStartTimeOfInvoke(), origEquinoxRawData.getInvoke(), EventLog.GENARATE_PASSKEY.getEventLog(),
								SoapResultCode.SUCCESS.getCode(), SoapResultCode.SUCCESS.getDescription());
						composeSummary.getSummaryLog(appInstance.getMapDestinationBean(), origEquinoxRawData.getInvoke());
					}
					catch (Exception e) {
						Log.e(e.getMessage());
					}
					if (composeSummary.isWriteSummary()) {
						appInstance.getListSummaryLog().add(composeSummary.getSummaryLog());
					}
					// ^^^^^^^^^^^ WRITE SUMMARY ^^^^^^^^^^^

					/* REMOVE PROFILE */
					GssoDataManagement.removeProfile(origEquinoxRawData.getInvoke(), appInstance);

				}
			}
			/** TEXT/PLAIN **/
			else if (ctype.equalsIgnoreCase(EventCtype.PLAIN)) {
				/** IDLE_SEND_OTP_REQ **/
				if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.SEND_OTP_JSON.getMessageType())) {

					String otpChannel = sendOneTimePW.getOtpChannel();
					/** CHOOSE LIFE TIMEOUT MIN **/
					GssoDataManagement.chooseDefaultValues(otpRequest, thisServiceTemplate);

					/* CREATE TRANSACTION ID PROFILE */
					origInvokeProfile.setTransactionID(GssoDataManagement.createNewTransaction(appInstance, origInvoke,
							thisServiceTemplate.getSeedkey()));
					origInvokeProfile.setServiceKey(appInstance.getMapE01dataofService().get(sendOneTimePW.getService().toUpperCase())
							.getServiceKey());

					/** FOR SMS **/
					if (otpChannel.equalsIgnoreCase(OTPChannel.SMS)) {

						rawDatasOutgoing.add(GssoConstructMessage.createSMSReqMessage(origInvoke, thisServiceTemplate, ec02Instance,
								composeDebugLog));

					}
					/** FOR EMAIL **/
					else if (otpChannel.equalsIgnoreCase(OTPChannel.EMAIL)) {

						rawDatasOutgoing.add(GssoConstructMessage.createEMAILReqMessage(origInvoke, thisServiceTemplate, ec02Instance,
								composeDebugLog));

					}
					/** FOR ALL **/
					else {
						
						rawDatasOutgoing.add(GssoConstructMessage.createSMSReqMessage(origInvoke, thisServiceTemplate, ec02Instance,
								composeDebugLog));

						rawDatasOutgoing.add(GssoConstructMessage.createEMAILReqMessage(origInvoke, thisServiceTemplate, ec02Instance,
								composeDebugLog));

					}

				}
				/** IDLE_GENERATE_PK **/
				if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.GENERATE_PASSKEY_JSON.getMessageType())) {

					GssoGenPasskeyRequest genPasskeyReq = GssoDataManagement.extractGssoGenPasskeyRequest(origEquinoxRawData);

					/* compose gen passkey response success */
					rawDatasOutgoing.add(GssoConstructMessage.createGenpassResp(appInstance,
							origInvokeProfile.getOrigEquinoxRawData(), thisServiceTemplate, composeDebugLog, genPasskeyReq));

					ec02Instance.incrementsStat(Statistic.GSSO_RETURN_GENERATEPASSKEY_RESPONSE_SUCCESS.getStatistic());
					// =========== WRITE DETAILS ===========
					appInstance.getMapOrigInvokeEventDetailOutput().put(origEquinoxRawData.getInvoke(),
							EventLog.GENARATE_PASSKEY.getEventLog());
					// ^^^^^^^^^^^ WRITE DETAILS ^^^^^^^^^^^

					if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
						// =========== DEBUG LOG ===========
						/** writeLog LOG **/
						composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_GENERATEPASSKEY_RESPONSE_SUCCESS.getStatistic());
						// ^^^^^^^^^^^ DEBUG LOG ^^^^^^^^^^^
					}

					// =========== WRITE SUMMARY ===========
					try {
						composeSummary.setWriteSummary();
						composeSummary.initialSummary(this.appInstance, appInstance.getMapOrigProfile().get(origEquinoxRawData.getInvoke())
								.getStartTimeOfInvoke(), origEquinoxRawData.getInvoke(), EventLog.GENARATE_PASSKEY.getEventLog(),
								JsonResultCode.SUCCESS.getCode(), JsonResultCode.SUCCESS.getDescription());
						composeSummary.getSummaryLog(appInstance.getMapDestinationBean(), origEquinoxRawData.getInvoke());
					}
					catch (Exception e) {
						Log.e(e.getMessage());
					}
					if (composeSummary.isWriteSummary()) {
						appInstance.getListSummaryLog().add(composeSummary.getSummaryLog());
					}
					// ^^^^^^^^^^^ WRITE SUMMARY ^^^^^^^^^^^

					/* REMOVE PROFILE */
					GssoDataManagement.removeProfile(origEquinoxRawData.getInvoke(), appInstance);

				}

			}
		}
		else {
			this.jsonCode = JsonResultCode.SERVICE_NOT_ALLOW;
			this.description = "GSSO_MISSING_SERVICE_TEMPLATE_WHEN_COMPARE";

			// ===============================================WRITE
			// SUMMARY======================================================
			/** INITIATE SUMMARY-LOG **/
//			this.composeSummary = new GssoComposeSummaryLog(abstractAF);
			composeSummary.setWriteSummary();

			if (description.equalsIgnoreCase("GSSO_MISSING_SERVICE_TEMPLATE_WHEN_COMPARE")) {
				destNodeCommand = "QueryServiceTemplate";
				destNodeResultCode = "null";
				destNodeResultDescription = "Oper is not found";
			}
			composeSummary.addDestinationBean(destNodeName, destNodeCommand, destNodeResultCode, destNodeResultDescription);
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
			// SUMMARY^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				// ===============================================DEBUG
				// LOG==========================================================
				/** writeLog LOG **/
				if (description.equalsIgnoreCase("GSSO_MISSING_SERVICE_TEMPLATE_WHEN_COMPARE")) {
					composeDebugLog.serviceTemplateMisMatch();
				}
				else {
					composeDebugLog.setFailureAvp(path + " " + description);
				}
				// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
				// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			}

			rawDatasOutgoing.add(GssoConstructMessage.createReturnErrorMessageServiceTemplate(origInvoke, ec02Instance, jsonCode,
					composeDebugLog, composeSummary));

			/* REMOVE PROFILE */
			GssoDataManagement.removeProfile(origInvoke, appInstance);
		}
	}

	private void serviceTemplateInitInstanceAndLog(EquinoxRawData equinoxRawData, AbstractAF abstractAF, E01Data e01Data,
			EC02Instance ec02Instance) {
		this.rawDatasOutgoing = new ArrayList<EquinoxRawData>();
		this.ec02Instance = (EC02Instance) ec02Instance;
		this.appInstance = this.ec02Instance.getAppInstance();
		this.incomingInvoke = equinoxRawData.getInvoke();
		this.abstractAF = abstractAF;
		this.e01Data = e01Data;
		this.origInvoke = InvokeFilter.getOriginInvoke(equinoxRawData.getInvoke());

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			// ===============================================DEBUG
			// LOG==========================================================
			/** INITIAL LOG **/
			this.composeDebugLog = new GssoComposeDebugLog(appInstance, ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED));
			this.composeDebugLog.setSubState(this.nextState);
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
			// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		}

		// ===============================================WRITE
		// DETAILS======================================================
		/** INITIAL LOG **/
		this.composeDetailsLog = new GssoComposeDetailsLog(appInstance, abstractAF.getEquinoxProperties().getState() + "." + "BEGIN",
				abstractAF);
		/** SET DTAILS IDENTITY **/
		this.composeDetailsLog.setIdentity(appInstance.getMapOrigProfile().get(origInvoke).getDetailsService());

		this.composeDetailsLog.setDataOrig(origInvoke, rawDataIncoming, appInstance);
		this.mapDetails = new MapDetailsAndConfigType();
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		// ===============================================WRITE
		// SUMMARY======================================================
		/** INITIATE SUMMARY-LOG **/
		this.composeSummary = new GssoComposeSummaryLog(abstractAF, appInstance.getMapOrigProfile().get(origInvoke).getDetailsService());
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// SUMMARY^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	}

	private void serviceTemplateSaveLog() {
		// ===============================================WRITE
		// DETAILS======================================================
		int outPutSize = this.rawDatasOutgoing.size();
		for (EquinoxRawData rawDataOut : this.rawDatasOutgoing) {
			try {
				this.composeDetailsLog.initialOutgoing(rawDataOut, appInstance, outPutSize);

				/** IF SMS **/
//				String origInvoke = InvokeFilter.getOriginInvoke(rawDataOut.getInvoke());
//				String subState = InvokeFilter.getSubState(rawDataOut.getInvoke());
//				if (subState != null && subState.equals(SubStates.W_SEND_SMS.name())) {
//					this.appInstance.getMapOrigProfile().get(origInvoke)
//							.setSubmitSmRequestTime(this.composeDetailsLog.getDetailTimeOutgoing());
//				}
			}
			catch (Exception e) {
				Log.e(e.getMessage());
			}
		}
		mapDetails.setDetail(this.composeDetailsLog.getDetailsLog());
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		// ===============================================SAVE
		// DETAILS======================================================
		appInstance.getListDetailsLog().add(mapDetails);

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			// ===============================================WRITE DEBUG
			// LOG===================================================
			/** writeLog LOG **/
			this.composeDebugLog.initialGssoSubStateLogE01Res(abstractAF, rawDataIncoming, e01Data);
			this.composeDebugLog.writeDebugSubStateLog();
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE DEBUG
			// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		}

		/*
		 * CLEAR E01 INVOKE
		 */
		abstractAF.getEquinoxUtils().getGlobalData().setTransactionId("default_invoke");
	}

	@Override
	public ArrayList<EquinoxRawData> doActionSubState(AbstractAF abstractAF, EC02Instance ec02Instance, EquinoxRawData equinoxRawData) {
		// TODO Auto-generated method stub
		return null;
	}

}