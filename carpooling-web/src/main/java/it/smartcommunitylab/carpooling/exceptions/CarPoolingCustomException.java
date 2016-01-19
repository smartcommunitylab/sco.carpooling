package it.smartcommunitylab.carpooling.exceptions;

import it.smartcommunitylab.carpooling.model.Response;

public class CarPoolingCustomException extends Exception {

	public String erroMsg;
	public int errorCode;
	public Response body;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CarPoolingCustomException(String msg) {
		super(msg);
	}

	public CarPoolingCustomException(int errorCode, String msg) {
		super(msg);
		this.errorCode = errorCode;
		this.erroMsg = msg;
		this.body = new Response<Void>(errorCode, msg);
	}

	public Response getBody() {
		return body;
	}

}