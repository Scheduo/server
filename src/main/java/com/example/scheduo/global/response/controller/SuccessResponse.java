package com.example.scheduo.global.response.controller;

import lombok.Getter;

@Getter
public class SuccessResponse<T> {
	private int code;
	private boolean success;
	private String message;
	private T data;

	public SuccessResponse(int code, String message, T data) {
		this.code = code;
		this.success = true;
		this.message = message;
		this.data = data;
	}
}