package com.ais.eqx.gsso.states;

import java.util.ArrayList;

import org.apache.commons.lang3.StringEscapeUtils;

import com.ais.eqx.gsso.enums.ConfigName;
import com.ais.eqx.gsso.enums.EventLog;
import com.ais.eqx.gsso.enums.EventMethod;
import com.ais.eqx.gsso.enums.JsonResultCode;
import com.ais.eqx.gsso.enums.LogScenario;
import com.ais.eqx.gsso.enums.States;
import com.ais.eqx.gsso.enums.Statistic;
import com.ais.eqx.gsso.instances.APPInstance;
import com.ais.eqx.gsso.instances.EC02Instance;
import com.ais.eqx.gsso.instances.MapDetailsAndConfigType;
import com.ais.eqx.gsso.interfaces.EquinoxAttribute;
import com.ais.eqx.gsso.interfaces.EventName;
import com.ais.eqx.gsso.interfaces.WSDLUrlLists;
import com.ais.eqx.gsso.model.GSSOHandler;
import com.ais.eqx.gsso.utils.ConfigureTool;
import com.ais.eqx.gsso.utils.GssoComposeDebugLog;
import com.ais.eqx.gsso.utils.GssoComposeDetailsLog;
import com.ais.eqx.gsso.utils.GssoComposeSummaryLog;
import com.ais.eqx.gsso.utils.InvokeSubStates;

import ais.mmt.sand.comlog.SummaryLogPrototype;
import ais.mmt.sand.comlog.exception.CommonLogException;
import ec02.af.abstracts.AbstractAF;
import ec02.af.data.EquinoxRawData;
import ec02.af.interfaces.IAFState;
import ec02.af.utils.Log;
import ec02.common.data.E01Data;
import ec02.common.data.GlobalData;
import ec02.common.data.KeyObject;


public class IDLE implements IAFState {

//	private ArrayList<EquinoxRawData> equinoxRawDatas;
	private EC02Instance 	ec02Instance;
	private APPInstance 	appInstance;
	private String 			nextState;

	private EquinoxRawData 	rawDataIncoming;
	private AbstractAF		abstractAF;
	
	private GssoComposeDetailsLog		composeDetailsLog;
	private GssoComposeSummaryLog		composeSummary;
	private GssoComposeDebugLog			composeDebugLog;
	private MapDetailsAndConfigType		mapDetails;

	private String statisticInput;
	
	
	private String						destNodeName				= "null";
	private String						destNodeResultDescription	= "null";
	private String						destNodeResultCode			= "null";
	private String						destNodeCommand				= "null";
	
	
	@Override
	public String doAction(AbstractAF abstractAF, Object ec02Instance, ArrayList<EquinoxRawData> rawDatas) {
		this.nextState = States.IDLE.getState();
		
		/************** INITIAL *****************/
		if(rawDatas == null || rawDatas.isEmpty()){
			EquinoxRawData equinoxRawData = new EquinoxRawData();
			rawDatas.add(equinoxRawData);
		}
		
		this.rawDataIncoming = rawDatas.get(0);
		
		
		this.ec02Instance = (EC02Instance) ec02Instance;
		
		boolean isWDSL = false;
		String url = "";
		
		if((rawDataIncoming.getRawDataAttribute(EquinoxAttribute.URL)!=null && !rawDataIncoming.getRawDataAttribute(EquinoxAttribute.URL).isEmpty())
				&&(rawDataIncoming.getRawDataAttribute(EquinoxAttribute.METHOD).equals(EventMethod.GET.getMethod()))){
			url = rawDataIncoming.getRawDataAttribute(EquinoxAttribute.URL);
		}
		
		if(url.contains("?wsdl")||url.contains("?xsd")){
			isWDSL = true;
		}
	
		if (isWDSL) {
			
			/************** INITIAL *****************/
			idleInitInstanceAndLog(rawDataIncoming, abstractAF, this.ec02Instance);
			
			String invoke = rawDataIncoming.getRawDataAttribute(EquinoxAttribute.INVOKE);
			appInstance.setOrigInvoke(invoke);
			appInstance.setOrig(rawDataIncoming.getRawDataAttribute(EquinoxAttribute.ORIG));
//			appInstance.setWsdl_url(url);
			

			writeLogSuccess(this.rawDataIncoming,url);
			/************** CODING ******************/
			
			 /* Initial invoke for query */
			String invokeOutgoing = InvokeSubStates.getInvokeOutgoing(appInstance.getOrigInvoke(), States.W_WSDL.getState());
			GlobalData globalData = abstractAF.getEquinoxUtils().getGlobalData();

			KeyObject keyobj = new KeyObject();
			keyobj.setObjectType(ConfigureTool.getConfigure(ConfigName.E01_WSDL));
			keyobj.setKey0("0");
			keyobj.setKey1(url);
			keyobj.setKey2("def");
			keyobj.setKey3("def");
			keyobj.setKey4("def");

			globalData.setTransactionId(invokeOutgoing);
			globalData.search(keyobj, invokeOutgoing);
			appInstance.setOutgoingInvoke(invokeOutgoing);
			
			/** Write output stat **/
			/** GSSO Send E01 QueryServiceTemplate Request STATICTIC **/
			this.ec02Instance.incrementsStat(Statistic.GSSO_SEND_E01_QUERY_WSDL_TEMPLATE_REQUEST.getStatistic());
			
			// =======WRITE DETAILS========
			appInstance.getMapOrigInvokeEventDetailOutput().put(invokeOutgoing, EventLog.QUERY_WSDL_TEMPLATE.getEventLog());
			
			// ^^^^^^^^^^WRITE DETAILS^^^^^^^^^^
			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				composeDebugLog.addStatisticOut(Statistic.GSSO_SEND_E01_QUERY_WSDL_TEMPLATE_REQUEST.getStatistic());
			}
			
			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				// =========== DEBUG LOG ==========
				/** writeLog LOG **/
				composeDebugLog.initialGssoSubStateLog(rawDataIncoming);
				// ^^^^^^^^^^^ DEBUG LOG ^^^^^^^^^^
			}
			
			this.nextState = States.W_WSDL.getState();
			
			idleWSDLSaveLog();
			
			return nextState;
			
		}
		else {
			return GSSOHandler.handle(abstractAF, ec02Instance, rawDatas);
		}

	}
	
	private void idleInitInstanceAndLog(EquinoxRawData equinoxRawData, AbstractAF abstractAF, EC02Instance ec02Instance) {
		this.rawDataIncoming = equinoxRawData;
//		this.rawDatasOutgoing = new ArrayList<EquinoxRawData>();
//		this.ec02Instance = (EC02Instance) ec02Instance;
		this.appInstance = this.ec02Instance.getAppInstance();
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
		this.composeDetailsLog.setDataOrig(rawDataIncoming.getInvoke(), rawDataIncoming, appInstance);
		this.composeDetailsLog.thisIdleState();
		this.mapDetails = new MapDetailsAndConfigType();
		
		/** SET DTAILS IDENTITY **/
		String url = rawDataIncoming.getRawDataAttribute(EquinoxAttribute.URL);
		
		this.composeDetailsLog.setIdentity(url);
		appInstance.setUrl(url);
		// =======WRITE SUMMARY==========
		/** INITIATE SUMMARY-LOG **/
		this.composeSummary = new GssoComposeSummaryLog(abstractAF, url);
		
		//try
//		this.composeSummary.setWriteSummary();
		
//			destNodeCommand = "QueryServiceTemplate";
//			destNodeResultCode = "null";
//			destNodeResultDescription = "Oper is not found";
		
		this.composeSummary.addDestinationBean(destNodeName, destNodeCommand, destNodeResultCode,
				destNodeResultDescription);
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// SUMMARY^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	}

	private void writeLogSuccess(EquinoxRawData rawData,String url) {
		
		/** VALID WSDL STATICTIC **/
		if(url.equals(WSDLUrlLists.WS_V1_WSDL)){
			statisticInput = Statistic.GSSO_RECEIVED_QUERY_WSDL_TEMPLATE_WS_V1_WSDL_REQUEST.getStatistic();
		}
		else if(url.equals(WSDLUrlLists.WS_V1_XSD)){
			statisticInput = Statistic.GSSO_RECEIVED_QUERY_WSDL_TEMPLATE_WS_V1_XSD_REQUEST.getStatistic();
		}
		else if(url.equals(WSDLUrlLists.WS_V2_WSDL)){
			statisticInput = Statistic.GSSO_RECEIVED_QUERY_WSDL_TEMPLATE_WS_V2_WSDL_REQUEST.getStatistic();
		}
		else if(url.equals(WSDLUrlLists.WS_V2_XSD)){
			statisticInput = Statistic.GSSO_RECEIVED_QUERY_WSDL_TEMPLATE_WS_V2_XSD_REQUEST.getStatistic();
		}
		else if(url.equals(WSDLUrlLists.Lotto_WSDL)){
			statisticInput = Statistic.GSSO_RECEIVED_QUERY_WSDL_TEMPLATE_LOTTO_WSDL_REQUEST.getStatistic();
		}
		else if(url.equals(WSDLUrlLists.Lotto_XSD)){
			statisticInput = Statistic.GSSO_RECEIVED_QUERY_WSDL_TEMPLATE_LOTTO_XSD_REQUEST.getStatistic();
		}
		else if(url.equals(WSDLUrlLists.SSO_V1_WSDL)){
			statisticInput = Statistic.GSSO_RECEIVED_QUERY_WSDL_TEMPLATE_SSO_V1_WSDL_REQUEST.getStatistic();
		}
		else if(url.equals(WSDLUrlLists.SSO_V1_XSD)){
			statisticInput = Statistic.GSSO_RECEIVED_QUERY_WSDL_TEMPLATE_SSO_V1_XSD_REQUEST.getStatistic();
		}
		else if(url.equals(WSDLUrlLists.SSO_V2_WSDL)){
			statisticInput = Statistic.GSSO_RECEIVED_QUERY_WSDL_TEMPLATE_SSO_V2_WSDL_REQUEST.getStatistic();
		}
		else if(url.equals(WSDLUrlLists.SSO_V2_XSD)){
			statisticInput = Statistic.GSSO_RECEIVED_QUERY_WSDL_TEMPLATE_SSO_V2_XSD_REQUEST.getStatistic();
		}
		else{
			statisticInput = Statistic.GSSO_RECEIVED_QUERY_WSDL_TEMPLATE_UNKNOWN_URL_REQUEST.getStatistic();
		}
		
		this.ec02Instance.incrementsStat(statisticInput);
		
		// ======WRITE DETAILS======
		appInstance.getMapOrigInvokeEventDetailInput().put(rawData.getInvoke(), EventLog.SEND_WSDL.getEventLog());
		appInstance.getMapOrigInvokeDetailScenario().put(rawData.getInvoke(), LogScenario.SEND_WSDL.getLogScenario());

		try {
			this.composeDetailsLog.initialIncoming(rawData, appInstance);
			this.composeDetailsLog.addScenario(appInstance, rawData, this.nextState);
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
	
	
	private void idleWSDLSaveLog() {
		// ===========WRITE DETAILS===========
//		int outPutSize = this.rawDatasOutgoing.size();

		try {
			this.composeDetailsLog.initialOutgoingToE01(abstractAF, appInstance);
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

	
//		/** SET OUT TIME FOR REQ **/
//		appInstance.getMapInvokeByOutputTime().put(appInstance.getOutgoingInvoke(), outTime);
//		

		/** ADD NEW TIME E01 OUT **/
		if (abstractAF.getEquinoxUtils().getDataBuffer().getE01Commands().size() != 0) {
			/** SET OUT TIME FOR REQ **/
			for (E01Data e01Data : abstractAF.getEquinoxUtils().getDataBuffer().getE01Commands()) {
				appInstance.getMapInvokeByOutputTime().put(e01Data.getId(), outTime);
			}
		}
		
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
			
			try {
				this.composeSummary.initialSummary(this.appInstance ,appInstance.getTimeStampIncoming(), rawDataIncoming.getInvoke(), EventLog.SEND_WSDL.getEventLog(), JsonResultCode.SUCCESS.getCode(), JsonResultCode.SUCCESS.getDescription());
				this.composeSummary.getSummaryLog(appInstance.getMapDestinationBean(), rawDataIncoming.getInvoke());
			}
			catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
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
}
