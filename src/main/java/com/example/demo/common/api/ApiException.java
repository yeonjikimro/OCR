package com.example.demo.common.api;

public class ApiException extends Exception {

	public ApiException() {
		super();
	}

	public ApiException(String msg) {
		super(msg);
	}

	public ApiException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public ApiException(Throwable cause) {
		super(cause);
	}

}
