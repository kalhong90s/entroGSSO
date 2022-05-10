package com.ais.eqx.gsso.interfaces;

public interface EQX {
	
	public interface MessageType {
		final public String REQUEST 			= "request";
		final public String RESPONSE 			= "response";
		final public String REGISTER 			= "REGISTER";
		final public String INVITE				=	"INVITE";
		final public String BYE					=	"BYE";
		final public String ACK					=	"ACK";
		final public String CANCEL				=	"CANCEL";
		final public String POST 				= "POST";
		final public String GET 				= "GET";
		final public String DELETE 				= "DELETE";
		
		
	}
	
	public interface Service {
		final public String DIAMETERCLIENT 			= "ES06";
		final public String DIAMETERSERVER 			= "ES07";
		final public String HTTPCLIENT 				= "ES04";
		final public String HTTPSERVER 				= "ES05";
		final public String LDAPCLIENT 				= "ES03";
		final public String LDAPSERVER 				= "ES13";
	}
	
	public interface Protocol {
		
		final public String HTTP 				= "HTTP";
		final public String LDAP 				= "LDAP";
		final public String DIAMETER 			= "DIAMETER";
		final public String SIP					= "SIP";
		
	}
	
	public interface Ctype {
		
		final public String CREDITCONTROL 				= "Credit-Control";
		final public String EXTENDED 					= "extended";
		final public String SEARCH 						= "search";
		final public String SPECIALIZED_RESOURCE		= "Specialized-Resource";
		final public String TEXTPLAIN 					= "text/plain";
		final public String TEXTXML 					= "text/xml";
		final public String USER_AUTHORIZATION 			= "User-Authorization";
		final public String LOCATION_INFO 				= "Location-Info";
		final public String TRANSFER					= "TRANSFER";

	}
	
	public interface Attribute {	
		
		final public String NAME					=	"name";
		final public String CTYPE					=	"ctype";
		final public String TYPE					=	"type";
		final public String TO						=	"to";
		final public String METHOD					=	"method";
		final public String INVOKE					=	"invoke";
		final public String VAL						=	"val";
		final public String ECODE 					=	"ecode";
		final public String IP 						=	"ip";
		final public String PORT 					=	"port";
		final public String URL 					=	"url";
		final public String ORIG 					=	"orig";
		final public String RET 					=	"ret";
	
	}

	public interface Ecode {
		
		final public String ECODE_100					=	"100";
		final public String ECODE_180					=	"180";
		final public String ECODE_200					=	"200";
		final public String ECODE_250					=	"250";
		final public String ECODE_300					=	"300";
		final public String ECODE_400					=	"400";
		final public String ECODE_401					=	"401";
		final public String ECODE_403					=	"403";
		final public String ECODE_404					=	"404";
		final public String ECODE_500					=	"500";

	}

	public interface ResultCode {
		
		final public String RESULTCODE_200					=	"200";
		final public String RESULTCODE_250					=	"250";

	}

	public interface typeUserInstance{
		public String ORDER = "order";
		public String CONSIGNEE = "consignee";
	}
	
	public interface TypeLog{
		public String REQ = "REQ";
		public String RES = "RES";
	}
	
	public interface CommandMethod{
		public String CREATE = "create";
		public String UPDATE = "update";
		public String DELETE = "delete";
		public String SEARCH = "search";
		public String GENERATE = "generate";
	}
	
	public interface CommandNameOfURL{
		public String userInstance = "userInstance";
		public String generateOrderID = "generateOrderID";
	}
	
	public interface QueryStringOfURL{
		public String type = "type";
		public String orderID = "orderID";
		public String prefix = "prefix";
		public String transactionID = "transactionID";
	}


	
}
