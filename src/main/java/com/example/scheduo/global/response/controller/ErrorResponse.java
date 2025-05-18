package com.example.scheduo.global.response.controller;

import lombok.Getter;

@Getter
public class ErrorResponse {
	private int code;
	private String status;
	private boolean success;
	private String message;

	public ErrorResponse(int code, String status, String message) {
		this.code = code;
		this.status = status;
		this.success = false;
		this.message = message;
	}
}