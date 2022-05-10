package com.ais.eqx.gsso.enums;

import java.util.HashMap;

public enum MailServerResultCode {

    	_101("101", "Cannot open connection")
    ,	_111("111", "Connection refused")
    ,	_211("211", "System Status message or System Help Reply")
    ,	_214("214", "Help Reply message")
    ,	_220("220", "MailServer service is running")
    ,	_221("221", "The domain service is closing the transmission channel")
    ,	_250("250", "Requested mail action OK completed")
    ,	_251("251", "User not local will forward")
    ,	_252("252", "Cannot VRFY (verify) the user")
    ,	_354("354", "Start mail input end with <CRLF>.<CRLF>, or, as a less cryptic description")
    ,	_420("420", "Timeout communication problem encountered during transmission")
    ,	_421("421", "The SMTP service/server you use has a limit on the number of concurrent SMTP streams your server can use")
    ,	_422("422", "The recipient’s mailbox is over its storage limit")
    ,	_431("431", "The recipient’s mail server is experiencing a Disk Full condition")
    ,	_432("432", "The recipient’s Exchange Server incoming mail queue has been stopped")
    ,	_441("441", "The recipient’s server is not responding")
    ,	_442("442", "The connection was dropped during transmission")
    ,	_446("446", "The maximum hop count was exceeded for the message")
    ,	_447("447", "Your outgoing message timed out")
    ,	_449("449", "Routing error")
    ,	_450("450", "Requested action not taken - The mailbox was unavailable at the remote end")
    ,	_451("451", "Requested action aborted - Local error in processing")
    ,	_452("452", "Requested action not taken - Insufficient storage")
    ,	_465("465", "Code Page unavailable on the recipient server")
    ,	_471("471", "This is a local error with the sending server and is often followed with “Please try again later”")
    ,	_500("500", "Syntax error command not recognized")
    ,	_501("501", "Syntax error in parameters or arguments")
    ,	_502("502", "Command not implemented")
    ,	_503("503", "Bad sequence of commands")
    ,	_504("504", "Command parameter not implemented")
    ,	_510("510", "Bad Email Address")
    ,	_511("511", "Bad Email Address")
    ,	_512("512", "The host server for the recipient’s domain name cannot be found (DNS error)")
    ,	_513("513", "Address type is incorrect (most mail servers)")
    ,	_523("523", "The Recipient’s mailbox cannot receive messages this big")
    ,	_530("530", "Authentication is required")
    ,	_541("541", "Recipient Address Rejected - Access denied")
    ,	_550("550", "Requested actions not taken as the mailbox is unavailable")
    ,	_551("551", "User not local or invalid address - Relay denied")
    ,	_552("552", "Requested mail actions aborted - Exceeded storage allocation")
    ,	_553("553", "Requested action not taken - Mailbox name invalid")
    ,	_554("554", "Transaction failed")
    ,	_571("571", "I have been told not to work with you !!!")
    
    ;

	private String	code;
	private String	errorMessage;

	private MailServerResultCode(final String code, final String errorMessage) {
		this.setCode(code);
		this.setErrorMessage(errorMessage);
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	private static final HashMap<String, MailServerResultCode>	lookup	= new HashMap<String, MailServerResultCode>();
	static {
		for (final MailServerResultCode e : MailServerResultCode.values()) {
			lookup.put(e.getCode(), e);
		}
	}

	public static String getErrorMessageFrom(final String code) {
		try {
			if (lookup.get(code).getErrorMessage() != null && !lookup.get(code).getErrorMessage().isEmpty()) {
				return lookup.get(code).getErrorMessage();
			}
			else {
				return "UNKNOWN";
			}
		}
		catch (Exception e) {
			return "UNKNOWN";
		}
	}

}
