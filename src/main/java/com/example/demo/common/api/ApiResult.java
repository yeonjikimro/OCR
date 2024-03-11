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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ApiResult<T> {

	private String resultCd;
	
	private String resultMsg;
	
	private T resultData;

	public ApiResult(String resultCd) {
		this.resultCd = resultCd;
	}
	
	public ApiResult(String resultCd, String resultMsg) {
		this.resultCd = resultCd;
		this.resultMsg = resultMsg;
	}

	public ApiResult(String resultCd, T resultData) {
		this.resultCd = resultCd;
		this.resultData = resultData;
	}

	public ApiResult(String resultCd, String resultMsg, T resultData) {
		this.resultCd = resultCd;
		this.resultMsg = resultMsg;
		this.resultData = resultData;
	}
	
	public String getResultCd() {
		return resultCd;
	}

	public void setResultCd(String resultCd) {
		this.resultCd = resultCd;
	}

	public String getResultMsg() {
		return resultMsg;
	}

	public void setResultMsg(String resultMsg) {
		this.resultMsg = resultMsg;
	}

	public T getResultData() {
		return resultData;
	}

	public void setResultData(T resultData) {
		this.resultData = resultData;
	}
}