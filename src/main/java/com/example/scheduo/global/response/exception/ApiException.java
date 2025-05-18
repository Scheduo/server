package com.example.scheduo.global.response.exception;

import com.example.scheduo.global.response.status.ResponseStatus;

public class ApiException extends RuntimeException {
	private final ResponseStatus responseStatus;

	public ApiException(ResponseStatus responseStatus) {
		super(responseStatus.getMessage());
		this.responseStatus = responseStatus;
	}

	public ResponseStatus getResponseStatus() {
		return responseStatus;
	}
}