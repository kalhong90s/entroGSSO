package com.ais.eqx.gsso.states;

import java.util.ArrayList;

import org.apache.commons.lang3.StringEscapeUtils;

import com.ais.eqx.gsso.enums.ConfigName;
import com.ais.eqx.gsso.enums.E01ResultCode;
import com.ais.eqx.gsso.enums.EquinoxEvent;
import com.ais.eqx.gsso.enums.EventLog;
import com.ais.eqx.gsso.enums.EventMethod;
import com.ais.eqx.gsso.enums.LogDestNodeResultDesc;
import com.ais.eqx.gsso.enums.LogScenario;
import com.ais.eqx.gsso.enums.States;
import com.ais.eqx.gsso.enums.Statistic;
import com.ais.eqx.gsso.enums.SubStates;
import com.ais.eqx.gsso.instances.APPInstance;
import com.ais.eqx.gsso.instances.EC02Instance;
import com.ais.eqx.gsso.instances.MapDetailsAndConfigType;
import com.ais.eqx.gsso.interfaces.EquinoxAttribute;
import com.ais.eqx.gsso.interfaces.EventAction;
import com.ais.eqx.gsso.interfaces.EventCtype;
import com.ais.eqx.gsso.interfaces.EventName;
import com.ais.eqx.gsso.interfaces.GlobaldataEventType;
import com.ais.eqx.gsso.utils.ConfigureTool;
import com.ais.eqx.gsso.utils.GssoComposeDebugLog;
import com.ais.eqx.gsso.utils.GssoComposeDetailsLog;
import com.ais.eqx.gsso.utils.GssoComposeSummaryLog;
import com.ais.eqx.gsso.utils.GssoGenerator;
import com.ais.eqx.gsso.utils.InvokeFilter;

import ais.mmt.sand.comlog.SummaryLogPrototype;
import ais.mmt.sand.comlog.exception.CommonLogException;
import ec02.af.abstracts.AbstractAF;
import ec02.af.data.EquinoxRawData;
import ec02.af.interfaces.IAFState;
import ec02.af.utils.Log;
import ec02.common.data.E01Data;


public class W_WSDL implements IAFState {
	private EC02Instance 	ec02Instance;
	private APPInstance 	appInstance;
	private AbstractAF		abstractAF;
	private String 			nextState;
	
	private ArrayList<EquinoxRawData> equinoxRawDatas;
	private EquinoxRawData 	equinoxRawDataIn;
	private EquinoxRawData equinoxRawDataOut;
	private String 	statisticInput;
	private String 	statisticOutput;
	private E01Data e01Data;
	
	private GssoComposeDetailsLog		composeDetailsLog;
	private GssoComposeSummaryLog		composeSummary;
	private GssoComposeDebugLog			composeDebugLog;
	private MapDetailsAndConfigType		mapDetails;
	
	private String						destNodeName				= "null";
	private String						destNodeCommand				= "null";
	private String						destNodeResultCode			= "null";
	private String						destNodeResultDescription	= "null";
	private String						summaryResultCode;
	private String						summaryResultCodeDesc;
	
	
	@Override
	public String doAction(AbstractAF abstractAF, Object ec02Instance, ArrayList<EquinoxRawData> rawDatas) {
		this.nextState = States.W_WSDL.getState();
		
		/** Instance **/
		this.ec02Instance = (EC02Instance) ec02Instance;
		appInstance = this.ec02Instance.getAppInstance();

		/** TIMEOUT **/
		if (rawDatas!=null && !rawDatas.isEmpty() && rawDatas.get(0).getRet().equals(EquinoxEvent.TIMEOUT.getCode())) {
			
			EquinoxRawData equinoxRawData = rawDatas.get(0);
			equinoxRawData.setType(EventAction.RESPONSE);
			equinoxRawData.setInvoke(appInstance.getOutgoingInvoke());
			
			idleInitInstanceAndLog_Timeout(abstractAF);
			
			String dataString = "<WSDLQueryTemplateResponse>"+
					"<code value= \"999\" />"+
					"<description value= \"E01_TIMEOUT\" />"+
					"<isSuccess value= \"false\" />"+
					"<orderRef value= \""+GssoGenerator.generateOrderReferenceWSDL()+"\" />"+
					"</WSDLQueryTemplateResponse>";
			
			equinoxRawDataOut = new EquinoxRawData();
			equinoxRawDataOut.setName(EventName.HTTP);
			equinoxRawDataOut.setCType(EventCtype.XML);
			equinoxRawDataOut.putAttribute(EquinoxAttribute.METHOD,EventMethod.GET.getMethod());
			equinoxRawDataOut.setTo(appInstance.getOrig());
			equinoxRawDataOut.setInvoke(appInstance.getOrigInvoke());
			equinoxRawDataOut.setType(EventAction.RESPONSE);
			equinoxRawDataOut.setRawMessage(dataString);
			
			this.equinoxRawDatas = new ArrayList<EquinoxRawData>();
			this.equinoxRawDatas.add(equinoxRawDataOut);
			this.ec02Instance.setEquinoxRawDatas(equinoxRawDatas);
			statisticInput = Statistic.GSSO_RECEIVED_E01_QUERY_WSDL_TEMPLATE_REQUEST_TIMEOUT.getStatistic();
			statisticOutput = Statistic.GSSO_RETURN_QUERY_WSDL_TEMPLATE_RESPONSE_ERROR.getStatistic();
			
			this.ec02Instance.incrementsStat(statisticInput);

			this.destNodeResultDescription = LogDestNodeResultDesc.CONNECTION_TIMEOUT.getLogDestNodeResultDesc();

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				this.composeDebugLog.addStatisticIn(statisticInput);
			}
			// ============WRITE DETAILS==============
			String origInvoke = InvokeFilter.getOriginInvoke(equinoxRawData.getInvoke());
			appInstance.getMapOrigInvokeEventDetailInput().put(origInvoke, EventLog.QUERY_WSDL_TEMPLATE.getEventLog());
			appInstance.getMapOrigInvokeDetailScenario().put(origInvoke,
					LogScenario.SEND_WSDL.getLogScenario());
			
			// ===============================================WRITE
			// DETAILS======================================================
			try {
				this.composeDetailsLog.initialIncoming(equinoxRawData, appInstance);
				this.composeDetailsLog.addScenario(appInstance, equinoxRawData, SubStates.W_SERVICE_TEMPLATE.name());
			}
			catch (Exception ex) {
				Log.e(ex.getMessage());
			}
			
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
			// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			this.ec02Instance.incrementsStat(statisticOutput);
			
			// =======WRITE DETAILS========
			appInstance.getMapOrigInvokeEventDetailOutput().put(origInvoke, EventLog.SEND_WSDL.getEventLog());
			
			// ^^^^^^^^^^WRITE DETAILS^^^^^^^^^^
			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				composeDebugLog.addStatisticOut(statisticOutput);
			}
			
			destNodeName				= "E01";
			destNodeCommand				= EventLog.QUERY_WSDL_TEMPLATE.getEventLog();
			destNodeResultCode			= "null";
			destNodeResultDescription	= "Timeout";
			summaryResultCode = "999";
			summaryResultCodeDesc ="E01_TIMEOUT";
			
		}
		/** Normal **/
		else{
			
			/************** INITIAL *****************/
			idleInitInstanceAndLog(abstractAF);
			String e01ret =abstractAF.getEquinoxUtils().getGlobalData().getGlobaldataEventType();
			if (GlobaldataEventType.NORMAL.equals(e01ret)) {
				
				/** E01 Return ResultCode is 0 **/
				if(E01ResultCode.SUCCESS.getCode().equals(e01Data.getResultCode())){
					
					String dataString = e01Data.getData();	
					String keyObject1 = e01Data.getKeyObject().getKey(1);
		
					writeLogSuccess(equinoxRawDataIn,keyObject1,e01Data);
					
					if(keyObject1.contains("?wsdl")){
						
						dataString = dataString.replaceAll("<IP Address>", ConfigureTool.getConfigure(ConfigName.WSDL_IP_ADDRESS));
						dataString = dataString.replaceAll("<Port>", ConfigureTool.getConfigure(ConfigName.WSDL_PORT));
					}
					
					equinoxRawDataOut = new EquinoxRawData();
					equinoxRawDataOut.setName(EventName.HTTP);
					equinoxRawDataOut.setCType(EventCtype.XML);
					equinoxRawDataOut.putAttribute(EquinoxAttribute.METHOD,EventMethod.GET.getMethod());
					equinoxRawDataOut.setTo(appInstance.getOrig());
					equinoxRawDataOut.setInvoke(appInstance.getOrigInvoke());
					equinoxRawDataOut.setType(EventAction.RESPONSE);
//					equinoxRawDataOut.putAttribute("val", dataString);
					equinoxRawDataOut.setRawMessage(dataString);
					
					this.equinoxRawDatas = new ArrayList<EquinoxRawData>();
					this.equinoxRawDatas.add(equinoxRawDataOut);
					this.ec02Instance.setEquinoxRawDatas(equinoxRawDatas);
					summaryResultCode = "200";
					summaryResultCodeDesc ="SUCCESS";
					outputLog(equinoxRawDataOut);
				}
				/** E01 Return ResultCode is 32 **/
				else if(E01ResultCode.NO_SUCH_OBJECT.getCode().equals(e01Data.getResultCode())){
					
					String stringData = "<WSDLQueryTemplateResponse>"+
							"<code value= \"999\" />"+
							"<description value= \"E01_ERROR\" />"+
							"<isSuccess value= \"false\" />"+
							"<orderRef value= \""+GssoGenerator.generateOrderReferenceWSDL()+"\" />"+
							"</WSDLQueryTemplateResponse>";
					
					equinoxRawDataOut = new EquinoxRawData();
					equinoxRawDataOut.setName(EventName.HTTP);
					equinoxRawDataOut.setCType(EventCtype.XML);
					equinoxRawDataOut.putAttribute(EquinoxAttribute.METHOD,EventMethod.GET.getMethod());
					equinoxRawDataOut.setTo(appInstance.getOrig());
					equinoxRawDataOut.setInvoke(appInstance.getOrigInvoke());
					equinoxRawDataOut.setType(EventAction.RESPONSE);
					equinoxRawDataOut.setRawMessage(stringData);
					
					this.equinoxRawDatas = new ArrayList<EquinoxRawData>();
					this.equinoxRawDatas.add(equinoxRawDataOut);
					this.ec02Instance.setEquinoxRawDatas(equinoxRawDatas);
					
					statisticInput = Statistic.GSSO_RECEIVED_E01_QUERY_WSDL_TEMPLATE_REQUEST_NOT_FOUND.getStatistic();
					statisticOutput = Statistic.GSSO_RETURN_QUERY_WSDL_TEMPLATE_RESPONSE_ERROR.getStatistic();
					
					this.ec02Instance.incrementsStat(statisticInput);

					this.destNodeResultDescription = LogDestNodeResultDesc.RESULT_CODE_ERROR.getLogDestNodeResultDesc();

					if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
						this.composeDebugLog.addStatisticIn(statisticInput);
					}
					// ============WRITE DETAILS==============
					String id =  InvokeFilter.getOriginInvoke(e01Data.getId());
					appInstance.getMapOrigInvokeEventDetailInput().put(id, EventLog.QUERY_WSDL_TEMPLATE.getEventLog());
					appInstance.getMapOrigInvokeDetailScenario().put(id, LogScenario.SEND_WSDL.getLogScenario());
					
					// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
					// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					
					//Return Error code
					//this.jsonCode = JsonResultCode.E01_ERROR;
					
					// ===============================================WRITE
					// DETAILS======================================================
					try {
						this.composeDetailsLog.initialIncomingFromE01(abstractAF, appInstance, equinoxRawDataIn, e01Data);
						this.composeDetailsLog.addScenarioFromE01(appInstance, e01Data, SubStates.W_WSDL.name(), this.nextState,
								equinoxRawDataIn);
					}
					catch (Exception ex) {
						Log.e(ex.getMessage());
					}
					// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
					// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					outputLog(equinoxRawDataOut);
					
//					try {
//						this.composeDetailsLog.initialOutgoing(equinoxRawDataOut, appInstance, 1);
//					}
//					catch (Exception e) {
//						e.printStackTrace();
//					}		
					destNodeName				= "E01";
					destNodeCommand				= EventLog.QUERY_WSDL_TEMPLATE.getEventLog();
					destNodeResultCode			= E01ResultCode.NO_SUCH_OBJECT.getCode();
					destNodeResultDescription	= E01ResultCode.NO_SUCH_OBJECT.getDescription();
					summaryResultCode = "999";
					summaryResultCodeDesc ="E01_ERROR";
					
				}
				else{
					
					String stringData = "<WSDLQueryTemplateResponse>"+
							"<code value= \"999\" />"+
							"<description value= \"E01_ERROR\" />"+
							"<isSuccess value= \"false\" />"+
							"<orderRef value= \""+GssoGenerator.generateOrderReferenceWSDL()+"\" />"+
							"</WSDLQueryTemplateResponse>";
					
					equinoxRawDataOut = new EquinoxRawData();
					equinoxRawDataOut.setName(EventName.HTTP);
					equinoxRawDataOut.setCType(EventCtype.XML);
					equinoxRawDataOut.putAttribute(EquinoxAttribute.METHOD,EventMethod.GET.getMethod());
					equinoxRawDataOut.setTo(appInstance.getOrig());
					equinoxRawDataOut.setInvoke(appInstance.getOrigInvoke());
					equinoxRawDataOut.setType(EventAction.RESPONSE);
					equinoxRawDataOut.setRawMessage(stringData);
					
					this.equinoxRawDatas = new ArrayList<EquinoxRawData>();
					this.equinoxRawDatas.add(equinoxRawDataOut);
					this.ec02Instance.setEquinoxRawDatas(equinoxRawDatas);
					
					statisticInput = Statistic.GSSO_RECEIVED_E01_QUERY_WSDL_TEMPLATE_REQUEST_NOT_FOUND.getStatistic();
					statisticOutput = Statistic.GSSO_RETURN_QUERY_WSDL_TEMPLATE_RESPONSE_ERROR.getStatistic();
					

					this.ec02Instance.incrementsStat(statisticInput);

					this.destNodeResultDescription = LogDestNodeResultDesc.RESULT_CODE_ERROR.getLogDestNodeResultDesc();

					if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
						this.composeDebugLog.addStatisticIn(statisticInput);
					}
					// ============WRITE DETAILS==============
					String id =  InvokeFilter.getOriginInvoke(e01Data.getId());
					appInstance.getMapOrigInvokeEventDetailInput().put(id, EventLog.QUERY_WSDL_TEMPLATE.getEventLog());
					appInstance.getMapOrigInvokeDetailScenario().put(id, LogScenario.SEND_WSDL.getLogScenario());
					
					// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
					// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					
					//Return Error code
					//this.jsonCode = JsonResultCode.E01_ERROR;
					
					// ===============================================WRITE
					// DETAILS======================================================
					try {
						this.composeDetailsLog.initialIncomingFromE01(abstractAF, appInstance, equinoxRawDataIn, e01Data);
						this.composeDetailsLog.addScenarioFromE01(appInstance, e01Data, SubStates.W_WSDL.name(), this.nextState,
								equinoxRawDataIn);
					}
					catch (Exception ex) {
						Log.e(ex.getMessage());
					}
					// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
					// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					outputLog(equinoxRawDataOut);
					
//					try {
//						this.composeDetailsLog.initialOutgoing(equinoxRawDataOut, appInstance, 1);
//					}
//					catch (Exception e) {
//						e.printStackTrace();
//					}		
					destNodeName				= "E01";
					destNodeCommand				= EventLog.QUERY_WSDL_TEMPLATE.getEventLog();
					destNodeResultCode			= e01Data.getResultCode();
					destNodeResultDescription	= E01ResultCode.OTHER_CODE.getDescription();
					summaryResultCode = "999";
					summaryResultCodeDesc ="E01_ERROR";
				}

			}
			else if(GlobaldataEventType.ERROR.equals(e01ret)){
				
				String dataString = "<WSDLQueryTemplateResponse>"+
									"<code value= \"999\" />"+
									"<description value= \"E01_ERROR\" />"+
									"<isSuccess value= \"false\" />"+
									"<orderRef value= \""+GssoGenerator.generateOrderReferenceWSDL()+"\" />"+
									"</WSDLQueryTemplateResponse>";
				
				equinoxRawDataOut = new EquinoxRawData();
				equinoxRawDataOut.setName(EventName.HTTP);
				equinoxRawDataOut.setCType(EventCtype.XML);
				equinoxRawDataOut.putAttribute(EquinoxAttribute.METHOD,EventMethod.GET.getMethod());
				equinoxRawDataOut.setTo(appInstance.getOrig());
				equinoxRawDataOut.setInvoke(appInstance.getOrigInvoke());
				equinoxRawDataOut.setType(EventAction.RESPONSE);
				equinoxRawDataOut.setRawMessage(dataString);
				
				this.equinoxRawDatas = new ArrayList<EquinoxRawData>();
				this.equinoxRawDatas.add(equinoxRawDataOut);
				this.ec02Instance.setEquinoxRawDatas(equinoxRawDatas);
				
				statisticInput = Statistic.GSSO_RECEIVED_E01_QUERY_WSDL_TEMPLATE_REQUEST_ERROR.getStatistic();
				statisticOutput = Statistic.GSSO_RETURN_QUERY_WSDL_TEMPLATE_RESPONSE_ERROR.getStatistic();
				

				this.ec02Instance.incrementsStat(statisticInput);

				this.destNodeResultDescription = LogDestNodeResultDesc.ERROR.getLogDestNodeResultDesc();

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					this.composeDebugLog.addStatisticIn(statisticInput);
				}
				// ============WRITE DETAILS==============
				String id =  InvokeFilter.getOriginInvoke(e01Data.getId());
				appInstance.getMapOrigInvokeEventDetailInput().put(id, EventLog.QUERY_WSDL_TEMPLATE.getEventLog());
				appInstance.getMapOrigInvokeDetailScenario().put(id,
						LogScenario.SEND_WSDL.getLogScenario());
				
				// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
				// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				
				//Return Error code
				//this.jsonCode = JsonResultCode.E01_ERROR;
				
				// ===============================================WRITE
				// DETAILS======================================================
				try {
					this.composeDetailsLog.initialIncomingFromE01(abstractAF, appInstance, equinoxRawDataIn, e01Data);
					this.composeDetailsLog.addScenarioFromE01(appInstance, e01Data, SubStates.W_WSDL.name(), this.nextState,
							equinoxRawDataIn);
				}
				catch (Exception ex) {
					Log.e(ex.getMessage());
				}
				// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
				// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				outputLog(equinoxRawDataOut);
				
//				try {
//					this.composeDetailsLog.initialOutgoing(equinoxRawDataOut, appInstance, 1);
//				}
//				catch (Exception e) {
//					e.printStackTrace();
//				}		
				destNodeName				= "E01";
				destNodeCommand				= EventLog.QUERY_WSDL_TEMPLATE.getEventLog();
				destNodeResultCode			= "null";
				destNodeResultDescription	= "Error";
				summaryResultCode = "999";
				summaryResultCodeDesc ="E01_ERROR";
				
			}
			else if(GlobaldataEventType.REJECT.equals(e01ret)){
				
				String dataString = "<WSDLQueryTemplateResponse>"+
						"<code value= \"999\" />"+
						"<description value= \"E01_ERROR\" />"+
						"<isSuccess value= \"false\" />"+
						"<orderRef value= \""+GssoGenerator.generateOrderReferenceWSDL()+"\" />"+
						"</WSDLQueryTemplateResponse>";
				
				equinoxRawDataOut = new EquinoxRawData();
				equinoxRawDataOut.setName(EventName.HTTP);
				equinoxRawDataOut.setCType(EventCtype.XML);
				equinoxRawDataOut.putAttribute(EquinoxAttribute.METHOD,EventMethod.GET.getMethod());
				equinoxRawDataOut.setTo(appInstance.getOrig());
				equinoxRawDataOut.setInvoke(appInstance.getOrigInvoke());
				equinoxRawDataOut.setType(EventAction.RESPONSE);
				equinoxRawDataOut.setRawMessage(dataString);
				
				this.equinoxRawDatas = new ArrayList<EquinoxRawData>();
				this.equinoxRawDatas.add(equinoxRawDataOut);
				this.ec02Instance.setEquinoxRawDatas(equinoxRawDatas);
				statisticInput = Statistic.GSSO_RECEIVED_E01_QUERY_WSDL_TEMPLATE_REQUEST_REJECT.getStatistic();
				statisticOutput = Statistic.GSSO_RETURN_QUERY_WSDL_TEMPLATE_RESPONSE_ERROR.getStatistic();
				
				this.ec02Instance.incrementsStat(statisticInput);

				this.destNodeResultDescription = LogDestNodeResultDesc.REJECT.getLogDestNodeResultDesc();

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					this.composeDebugLog.addStatisticIn(statisticInput);
				}
				// ============WRITE DETAILS==============
				String id =  InvokeFilter.getOriginInvoke(e01Data.getId());
				appInstance.getMapOrigInvokeEventDetailInput().put(id, EventLog.QUERY_WSDL_TEMPLATE.getEventLog());
				appInstance.getMapOrigInvokeDetailScenario().put(id,
						LogScenario.SEND_WSDL.getLogScenario());
				
				// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
				// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				
				//Return Error code
				//this.jsonCode = JsonResultCode.E01_ERROR;
				
				// ===============================================WRITE
				// DETAILS======================================================
				try {
					this.composeDetailsLog.initialIncomingFromE01(abstractAF, appInstance, equinoxRawDataIn, e01Data);
					this.composeDetailsLog.addScenarioFromE01(appInstance, e01Data, SubStates.W_WSDL.name(), this.nextState,
							equinoxRawDataIn);
				}
				catch (Exception ex) {
					Log.e(ex.getMessage());
				}
				// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
				// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				outputLog(equinoxRawDataOut);
				
//				try {
//					this.composeDetailsLog.initialOutgoing(equinoxRawDataOut, appInstance, 1);
//				}
//				catch (Exception e) {
//					e.printStackTrace();
//				}		
				destNodeName				= "E01";
				destNodeCommand				= EventLog.QUERY_WSDL_TEMPLATE.getEventLog();
				destNodeResultCode			= "null";
				destNodeResultDescription	= "Reject";
				summaryResultCode = "999";
				summaryResultCodeDesc ="E01_ERROR";
			}
			else if(GlobaldataEventType.ABORT.equals(e01ret)){
				
				String dataString = "<WSDLQueryTemplateResponse>"+
						"<code value= \"999\" />"+
						"<description value= \"E01_ERROR\" />"+
						"<isSuccess value= \"false\" />"+
						"<orderRef value= \""+GssoGenerator.generateOrderReferenceWSDL()+"\" />"+
						"</WSDLQueryTemplateResponse>";
				
				equinoxRawDataOut = new EquinoxRawData();
				equinoxRawDataOut.setName(EventName.HTTP);
				equinoxRawDataOut.setCType(EventCtype.PLAIN);
				equinoxRawDataOut.putAttribute(EquinoxAttribute.METHOD,EventMethod.GET.getMethod());
				equinoxRawDataOut.setTo(appInstance.getOrig());
				equinoxRawDataOut.setInvoke(appInstance.getOrigInvoke());
				equinoxRawDataOut.setType(EventAction.RESPONSE);
				equinoxRawDataOut.setRawMessage(dataString);
				
				this.equinoxRawDatas = new ArrayList<EquinoxRawData>();
				this.equinoxRawDatas.add(equinoxRawDataOut);
				this.ec02Instance.setEquinoxRawDatas(equinoxRawDatas);
				statisticInput = Statistic.GSSO_RECEIVED_E01_QUERY_WSDL_TEMPLATE_REQUEST_ABORT.getStatistic();
				statisticOutput = Statistic.GSSO_RETURN_QUERY_WSDL_TEMPLATE_RESPONSE_ERROR.getStatistic();
				

				this.ec02Instance.incrementsStat(statisticInput);

				this.destNodeResultDescription = LogDestNodeResultDesc.ABORT.getLogDestNodeResultDesc();

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					this.composeDebugLog.addStatisticIn(statisticInput);
				}
				// ============WRITE DETAILS==============
				String id =  InvokeFilter.getOriginInvoke(e01Data.getId());
				appInstance.getMapOrigInvokeEventDetailInput().put(id, EventLog.QUERY_WSDL_TEMPLATE.getEventLog());
				appInstance.getMapOrigInvokeDetailScenario().put(id,
						LogScenario.SEND_WSDL.getLogScenario());
				
				// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
				// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				
				//Return Error code
				//this.jsonCode = JsonResultCode.E01_ERROR;
				
				// ===============================================WRITE
				// DETAILS======================================================
				try {
					this.composeDetailsLog.initialIncomingFromE01(abstractAF, appInstance, equinoxRawDataIn, e01Data);
					this.composeDetailsLog.addScenarioFromE01(appInstance, e01Data, SubStates.W_WSDL.name(), this.nextState,
							equinoxRawDataIn);
				}
				catch (Exception ex) {
					Log.e(ex.getMessage());
				}
				// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
				// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				outputLog(equinoxRawDataOut);
				
//				try {
//					this.composeDetailsLog.initialOutgoing(equinoxRawDataOut, appInstance, 1);
//				}
//				catch (Exception e) {
//					e.printStackTrace();
//				}		
				destNodeName				= "E01";
				destNodeCommand				= EventLog.QUERY_WSDL_TEMPLATE.getEventLog();
				destNodeResultCode			= "null";
				destNodeResultDescription	= "Abort";
				summaryResultCode = "999";
				summaryResultCodeDesc ="E01_ERROR";
			}
	
		/** Timeout **/
		else{
			
			String dataString = "<WSDLQueryTemplateResponse>"+
					"<code value= \"999\" />"+
					"<description value= \"E01_TIMEOUT\" />"+
					"<isSuccess value= \"false\" />"+
					"<orderRef value= \""+GssoGenerator.generateOrderReferenceWSDL()+"\" />"+
					"</WSDLQueryTemplateResponse>";
			
			equinoxRawDataOut = new EquinoxRawData();
			equinoxRawDataOut.setName(EventName.HTTP);
			equinoxRawDataOut.setCType(EventCtype.XML);
			equinoxRawDataOut.putAttribute(EquinoxAttribute.METHOD,EventMethod.GET.getMethod());
			equinoxRawDataOut.setTo(appInstance.getOrig());
			equinoxRawDataOut.setInvoke(appInstance.getOrigInvoke());
			equinoxRawDataOut.setType(EventAction.RESPONSE);
			equinoxRawDataOut.setRawMessage(dataString);
			
			this.equinoxRawDatas = new ArrayList<EquinoxRawData>();
			this.equinoxRawDatas.add(equinoxRawDataOut);
			this.ec02Instance.setEquinoxRawDatas(equinoxRawDatas);
			statisticInput = Statistic.GSSO_RECEIVED_E01_QUERY_WSDL_TEMPLATE_REQUEST_TIMEOUT.getStatistic();
			statisticOutput = Statistic.GSSO_RETURN_QUERY_WSDL_TEMPLATE_RESPONSE_ERROR.getStatistic();
			
			this.ec02Instance.incrementsStat(statisticInput);

			this.destNodeResultDescription = LogDestNodeResultDesc.CONNECTION_TIMEOUT.getLogDestNodeResultDesc();

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				this.composeDebugLog.addStatisticIn(statisticInput);
			}
			// ============WRITE DETAILS==============
			String id =  InvokeFilter.getOriginInvoke(e01Data.getId());
			appInstance.getMapOrigInvokeEventDetailInput().put(id, EventLog.QUERY_WSDL_TEMPLATE.getEventLog());
			appInstance.getMapOrigInvokeDetailScenario().put(id,
					LogScenario.SEND_WSDL.getLogScenario());
			
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
			// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			
			//Return Error code
			//this.jsonCode = JsonResultCode.E01_ERROR;
			
			// ===============================================WRITE
			// DETAILS======================================================
			try {
				this.composeDetailsLog.initialIncomingFromE01(abstractAF, appInstance, equinoxRawDataIn, e01Data);
				this.composeDetailsLog.addScenarioFromE01(appInstance, e01Data, States.W_WSDL.getState(), this.nextState,equinoxRawDataIn);
			}
			catch (Exception ex) {
				Log.e(ex.getMessage());
			}
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
			// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			outputLog(equinoxRawDataOut);
			
//			try {
//				this.composeDetailsLog.initialOutgoing(equinoxRawDataOut, appInstance, 1);
//			}
//			catch (Exception e) {
//				e.printStackTrace();
//			}
			destNodeName				= "E01";
			destNodeCommand				= EventLog.QUERY_WSDL_TEMPLATE.getEventLog();
			destNodeResultCode			= "null";
			destNodeResultDescription	= "Timeout";
			summaryResultCode = "999";
			summaryResultCodeDesc ="E01_TIMEOUT";
			}
		}
		
		idleWSDLSaveLog(equinoxRawDataOut);
		
		return States.IDLE.getState();
	}
	

	private void idleInitInstanceAndLog(AbstractAF abstractAF) {
		equinoxRawDataIn = new EquinoxRawData();
		equinoxRawDataIn.setInvoke(appInstance.getOutgoingInvoke());
		e01Data = abstractAF.getEquinoxUtils().getGlobalData().getDataResult().get(appInstance.getOutgoingInvoke());
		
		this.abstractAF = abstractAF;
		
		appInstance.setTimeStampIncoming(System.currentTimeMillis());
		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			// ======DEBUG LOG=====
			this.composeDebugLog = new GssoComposeDebugLog(appInstance, ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED));
			this.composeDebugLog.setSubState(this.nextState);
			
		}

		// =======WRITE DETAILS======
		/** INITIAL LOG **/
		this.composeDetailsLog = new GssoComposeDetailsLog(appInstance, abstractAF.getEquinoxProperties().getState() + "." + "BEGIN",
				abstractAF);
		this.composeDetailsLog.setDataOrig(appInstance.getOrigInvoke(), equinoxRawDataIn, appInstance);
		this.composeDetailsLog.thisIdleState();
		this.mapDetails = new MapDetailsAndConfigType();
		
		/** SET DTAILS IDENTITY **/
		this.composeDetailsLog.setIdentity(e01Data.getKeyObject().getKey(1));
		
		// =======WRITE SUMMARY==========
			/** INITIATE SUMMARY-LOG **/
			this.composeSummary = new GssoComposeSummaryLog(abstractAF, e01Data.getKeyObject().getKey(1));
			
			//try
			this.composeSummary.setWriteSummary();
			
//			this.composeSummary.addDestinationBean(destNodeName, destNodeCommand, destNodeResultCode,
//					destNodeResultDescription);
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
			// SUMMARY^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	}

	private void idleInitInstanceAndLog_Timeout(AbstractAF abstractAF) {
		equinoxRawDataIn = new EquinoxRawData();
		equinoxRawDataIn.setInvoke(appInstance.getOutgoingInvoke());
		
		this.abstractAF = abstractAF;
		
		appInstance.setTimeStampIncoming(System.currentTimeMillis());
		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			// ======DEBUG LOG=====
			this.composeDebugLog = new GssoComposeDebugLog(appInstance, ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED));
			this.composeDebugLog.setSubState(this.nextState);
			
		}

		// =======WRITE DETAILS======
		/** INITIAL LOG **/
		this.composeDetailsLog = new GssoComposeDetailsLog(appInstance, abstractAF.getEquinoxProperties().getState() + "." + "BEGIN",
				abstractAF);
		this.composeDetailsLog.setDataOrig(appInstance.getOrigInvoke(), equinoxRawDataIn, appInstance);
//		this.composeDetailsLog.thisIdleState();
		this.mapDetails = new MapDetailsAndConfigType();
		
		/** SET DTAILS IDENTITY **/
		String url = appInstance.getUrl();
		this.composeDetailsLog.setIdentity(url);
		
		// =======WRITE SUMMARY==========
			/** INITIATE SUMMARY-LOG **/
			this.composeSummary = new GssoComposeSummaryLog(abstractAF, url);
			
			//try
			this.composeSummary.setWriteSummary();
			
//			this.composeSummary.addDestinationBean(destNodeName, destNodeCommand, destNodeResultCode,
//					destNodeResultDescription);
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
			// SUMMARY^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	}
	
	private void writeLogSuccess(EquinoxRawData rawData,String key1,E01Data e01Data) {
		
		/** VALID WSDL STATICTIC **/
		if(key1.contains("GSSO_WS/GSSOWeb?wsdl")){
			statisticInput = Statistic.GSSO_RECEIVED_E01_QUERY_WSDL_TEMPLATE_WS_V1_WSDL_RESPONSE_SUCCESS.getStatistic();
			statisticOutput = Statistic.GSSO_RETURN_QUERY_WSDL_TEMPLATE_WS_V1_WSDL_RESPONSE_SUCCESS.getStatistic();
		}
		else if(key1.contains("GSSO_WS/GSSOWeb?xsd=1")){
			statisticInput = Statistic.GSSO_RECEIVED_E01_QUERY_WSDL_TEMPLATE_WS_V1_XSD_RESPONSE_SUCCESS.getStatistic();
			statisticOutput = Statistic.GSSO_RETURN_QUERY_WSDL_TEMPLATE_WS_V1_XSD_RESPONSE_SUCCESS.getStatistic();
		}
		else if(key1.contains("GSSO_WS/GSSOWebV2?wsdl")){
			statisticInput = Statistic.GSSO_RECEIVED_E01_QUERY_WSDL_TEMPLATE_WS_V2_WSDL_RESPONSE_SUCCESS.getStatistic();
			statisticOutput = Statistic.GSSO_RETURN_QUERY_WSDL_TEMPLATE_WS_V2_WSDL_RESPONSE_SUCCESS.getStatistic();
		}
		else if(key1.contains("GSSO_WS/GSSOWebV2?xsd=1")){
			statisticInput = Statistic.GSSO_RECEIVED_E01_QUERY_WSDL_TEMPLATE_WS_V2_XSD_RESPONSE_SUCCESS.getStatistic();
			statisticOutput = Statistic.GSSO_RETURN_QUERY_WSDL_TEMPLATE_WS_V2_XSD_RESPONSE_SUCCESS.getStatistic();
		}
		else if(key1.contains("GSSO_WS/GSSOLotto?wsdl")){
			statisticInput = Statistic.GSSO_RECEIVED_E01_QUERY_WSDL_TEMPLATE_LOTTO_WSDL_RESPONSE_SUCCESS.getStatistic();
			statisticOutput = Statistic.GSSO_RETURN_QUERY_WSDL_TEMPLATE_LOTTO_WSDL_RESPONSE_SUCCESS.getStatistic();
		}
		else if(key1.contains("GSSO_WS/GSSOLotto?xsd=1")){
			statisticInput = Statistic.GSSO_RECEIVED_E01_QUERY_WSDL_TEMPLATE_LOTTO_XSD_RESPONSE_SUCCESS.getStatistic();
			statisticOutput = Statistic.GSSO_RETURN_QUERY_WSDL_TEMPLATE_LOTTO_XSD_RESPONSE_SUCCESS.getStatistic();
		}
		else if(key1.contains("GSSO-SSO/GssoSsoWeb?wsdl")){
			statisticInput = Statistic.GSSO_RECEIVED_E01_QUERY_WSDL_TEMPLATE_SSO_V1_WSDL_RESPONSE_SUCCESS.getStatistic();
			statisticOutput = Statistic.GSSO_RETURN_QUERY_WSDL_TEMPLATE_WS_V1_WSDL_RESPONSE_SUCCESS.getStatistic();
		}
		else if(key1.contains("GSSO-SSO/GssoSsoWeb?xsd=1")){
			statisticInput = Statistic.GSSO_RECEIVED_E01_QUERY_WSDL_TEMPLATE_SSO_V1_XSD_RESPONSE_SUCCESS.getStatistic();
			statisticOutput = Statistic.GSSO_RETURN_QUERY_WSDL_TEMPLATE_SSO_V1_XSD_RESPONSE_SUCCESS.getStatistic();
		}
		else if(key1.contains("GSSO-SSO/GssoSsoWebV2?wsdl")){
			statisticInput = Statistic.GSSO_RECEIVED_E01_QUERY_WSDL_TEMPLATE_SSO_V2_WSDL_RESPONSE_SUCCESS.getStatistic();
			statisticOutput = Statistic.GSSO_RETURN_QUERY_WSDL_TEMPLATE_SSO_V2_WSDL_RESPONSE_SUCCESS.getStatistic();
		}
		else if(key1.contains("GSSO-SSO/GssoSsoWebV2?xsd=1")){
			statisticInput = Statistic.GSSO_RECEIVED_E01_QUERY_WSDL_TEMPLATE_SSO_V2_XSD_RESPONSE_SUCCESS.getStatistic();
			statisticOutput = Statistic.GSSO_RETURN_QUERY_WSDL_TEMPLATE_SSO_V2_XSD_RESPONSE_SUCCESS.getStatistic();
		}
		
		this.ec02Instance.incrementsStat(statisticInput);
		
		// ======WRITE DETAILS======
		String id =  InvokeFilter.getOriginInvoke(e01Data.getId());
		appInstance.getMapOrigInvokeEventDetailInput().put(id, EventLog.QUERY_WSDL_TEMPLATE.getEventLog());
		appInstance.getMapOrigInvokeDetailScenario().put(id, LogScenario.SEND_WSDL.getLogScenario());
		
		
		try {
			this.composeDetailsLog.initialIncomingFromE01(abstractAF, appInstance, rawData, e01Data);
//			this.composeDetailsLog.addScenario(appInstance, rawData, this.nextState);
			this.composeDetailsLog.addScenarioFromE01(appInstance, e01Data, SubStates.W_WSDL.name(), this.nextState, equinoxRawDataIn);
		}
		catch (Exception e) {
			Log.e(e.getMessage());
		}
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			// ===============================================DEBUG
			// LOG==========================================================
			this.composeDebugLog.addStatisticIn(statisticInput);
			this.composeDebugLog.setMessageValidator(EventName.COMPLETE);
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
			// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		}

	}
	

	private void idleWSDLSaveLog(EquinoxRawData equinoxRawData) {
		// ===========WRITE DETAILS===========
//		int outPutSize = this.rawDatasOutgoing.size();

		try {
			this.composeDetailsLog.initialOutgoing(equinoxRawData, appInstance, 1);
		}
		catch (Exception e) {
			Log.e(e.getMessage());
		}

		mapDetails.setDetail(this.composeDetailsLog.getDetailsLog());
		// ^^^^^^^^^^WRITE DETAILS^^^^^^^^^^
		// ===========SAVE DETAILS===========
		appInstance.getListDetailsLog().add(mapDetails);
		// ===============================================SAVE
		// SUMMARY======================================================
		try {
			this.composeSummary.initialSummary(this.appInstance ,appInstance.getTimeStampIncoming(), appInstance.getOrigInvoke(), EventLog.SEND_WSDL.getEventLog(), summaryResultCode, summaryResultCodeDesc);
		}
		catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		this.composeSummary.addDestinationBean(destNodeName, destNodeCommand, destNodeResultCode,
				destNodeResultDescription);
		
		try {
			this.composeSummary.getSummaryLog(appInstance.getMapDestinationBean(), appInstance.getOrigInvoke());
		}
		catch (Exception e1) {
			e1.printStackTrace();
		}
		
		if (this.composeSummary.isWriteSummary()) {
			appInstance.getListSummaryLog().add(this.composeSummary.getSummaryLog());
		}

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			// ===========WRITE DEBUG LOG===========
			this.composeDebugLog.writeDebugSubStateLog();
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE DEBUG
			// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		}
		
		long outTime = System.currentTimeMillis();
		this.appInstance.setTimeStampOutgoing(outTime);
		
		/** SET OUT PUT **/
		// ===============================================WRITE
		// DETAILS======================================================
		for (MapDetailsAndConfigType mapDetailLog : appInstance.getListDetailsLog()) {
			
			mapDetailLog.getDetail().setOutputTimestamp(outTime);
			try {
				/** ALL = 2 **/
				if (ConfigureTool.isWriteLogDetails(ConfigName.LOG_DETAIL_ENABLED) == 2) {
					String detailLog = mapDetailLog.getDetail().print().replaceAll("\\\\t", "").replaceAll("\\\\n", "");
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
			}
		}
		
		appInstance.getListDetailsLog().clear();
		
		//////// Summary Log/////////
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
		
		
	}
	private void outputLog(EquinoxRawData equinoxRawData){
		/** Write output stat **/
		/** GSSO Send E01 QueryServiceTemplate Request STATICTIC **/
		this.ec02Instance.incrementsStat(statisticOutput);
		
		// =======WRITE DETAILS========
		appInstance.getMapOrigInvokeEventDetailOutput().put(equinoxRawData.getInvoke(), EventLog.SEND_WSDL.getEventLog());
		
		// ^^^^^^^^^^WRITE DETAILS^^^^^^^^^^
		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			composeDebugLog.addStatisticOut(statisticOutput);
		}
		
		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			// =========== DEBUG LOG ==========
			/** writeLog LOG **/
			composeDebugLog.initialGssoSubStateLogE01Res(abstractAF, equinoxRawData, e01Data);
			// ^^^^^^^^^^^ DEBUG LOG ^^^^^^^^^^
		}
	}

}
