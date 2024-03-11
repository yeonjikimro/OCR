/*******************************************************************************
 *
 * Copyright ⓒ 2018 kth corp. All rights reserved.
 *
 * This is a proprietary software of kt corp, and you may not use this file except in
 * compliance with license agreement with kt corp. Any redistribution or use of this
 * software, with or without modification shall be strictly prohibited without prior written
 * approval of kt corp, and the copyright notice above does not evidence any actual or
 * intended publication of such software.
 *
 *******************************************************************************/
package com.example.demo.common.api;

public enum ApiResultCodes {

	/** 정상 처리 */
	SUCCESS("0000", "정상 처리되었습니다."),

	/** 요청 오류(Bad Request) */
	ERROR_0400("0400", "잘못된 사용자 요청 입니다."),

	/** Unauthorized */
	ERROR_0401("0401", "인증이 필요합니다."),

	/** Forbidden */
	ERROR_0403("0403", "권한이 없습니다."),

	/** Not Found */
	ERROR_0404("0404", "요청한 API를 찾을수 없습니다."),

	/** Method Not Allowed */
	ERROR_0405("0405", "허용하지 않는 메서드 입니다."),

	/** Request Timeout */
	ERROR_0408("0408", "Request Timeout"),

	/** MediaType 오류 */
	ERROR_0415("0415", "Unsupported Media Type"),

	/** 서버 실패 */
	ERROR_0500("0500", "오류가 발생하였습니다."),

	/** 프로세스 오류 (공통) */
	ERROR_0900("0900", "요청 처리를 실패하였습니다.");

	private String code;

	private String msg;

	private ApiResultCodes(String code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public String code() {
		return this.code;
	}

	public String msg() {
		return this.msg;
	}
}