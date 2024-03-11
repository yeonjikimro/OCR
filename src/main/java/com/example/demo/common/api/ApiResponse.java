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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Slf4j
public class ApiResponse {
	
	private static final ObjectMapper objectMapper = new ObjectMapper();
		
	/**
	 * 성공 프로세스에 대하여 ResponseEntity<ApiResult<T>> 정보를 리턴
	 * 
	 * @return ResponseEntity<Object>
	 */
	public static <T> ResponseEntity<Object> success() {
		return success(null);
	}
	
	/**
	 * 성공 프로세스에 대하여 ResponseEntity<ApiResult<T>> 정보를 리턴
	 * 
	 * @param data
	 * @return
	 */
	public static <T> ResponseEntity<Object> success(T data) {
		final ApiResult<T> body = new ApiResult<T>(ApiResultCodes.SUCCESS.code(), ApiResultCodes.SUCCESS.msg(), data);
		return new ResponseEntity<Object>(body, HttpStatus.OK);
	}

	/**
	 * 실패 프로세스에 대하여 ResponseEntity<ApiResult<T>> 정보를 리턴
	 *
	 * @return ResponseEntity<Object>
	 */
	public static <T> ResponseEntity<Object> fail() {
		return fail(null);
	}

	/**
	 * 실패 프로세스에 대하여 ResponseEntity<ApiResult<T>> 정보를 리턴
	 *
	 * @param data
	 * @return
	 */
	public static <T> ResponseEntity<Object> fail(T data) {
		final ApiResult<T> body = new ApiResult<T>(ApiResultCodes.ERROR_0900.code(), ApiResultCodes.ERROR_0900.msg(), data);
		return new ResponseEntity<Object>(body, HttpStatus.OK);
	}
	
	/**
	 * httpStatus에 따른 API 응답
	 * @param httpStatus
	 * @param responseData
	 * @param response
	 */
	public static void sendJsonResponse(HttpStatus httpStatus, HttpServletResponse response) {
		sendJsonResponse(httpStatus, null, response);
	}
	
	/**
	 * httpStatus에 따른 API 응답
	 * @param httpStatus
	 * @param responseData
	 * @param response
	 */
	@SuppressWarnings("deprecation")
	public static void sendJsonResponse(HttpStatus httpStatus, Object responseData, HttpServletResponse response) {
		response.setStatus(httpStatus.value());
		response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);

		ApiResult<Object> apiResult = new ApiResult<>(String.valueOf(httpStatus.value()));
		if (responseData != null) {
			apiResult.setResultData(responseData);
		}
		try (PrintWriter printWriter = response.getWriter()) {
			printWriter.write(objectMapper.writeValueAsString(apiResult));
		} catch (JsonProcessingException ex) {
			log.error(ex.getLocalizedMessage(), ex);
		} catch (IOException ex) {
			log.error(ex.getLocalizedMessage(), ex);
		}
	}
	
	/**
	 * httpStatus에 따른 API 응답
	 * @param <T>
	 * @param httpStatus
	 * @return ResponseEntity
	 */
	public static <T> ResponseEntity<Object> responseResult(HttpStatus httpStatus) {
		return responseResult(null, httpStatus);
	}
	
	/**
	 * httpStatus에 따른 API 응답
	 * @param <T>
	 * @param httpStatus
	 * @return ResponseEntity
	 */
	public static <T> ResponseEntity<Object> responseResult(T responseData, HttpStatus httpStatus) {
		final ApiResult<T> body = new ApiResult<T>(String.valueOf(httpStatus.value()), responseData);
		return new ResponseEntity<Object>(body, httpStatus);
	}
	
	/**
	 * httpStatus에 따른 API 응답 (HttpStatus 200)
	 * @param <T>
	 * @param httpStatus
	 * @return ResponseEntity
	 */
	public static <T> ResponseEntity<Object> responseResultOk() {
		return responseResultOk(null);
	}
	
	/**
	 * httpStatus에 따른 API 응답 (HttpStatus 200)
	 * @param <T>
	 * @param httpStatus
	 * @return ResponseEntity
	 */
	public static <T> ResponseEntity<Object> responseResultOk(T responseData) {
		final ApiResult<T> body = new ApiResult<T>(String.valueOf(HttpStatus.OK.value()), responseData);
		return new ResponseEntity<Object>(body, HttpStatus.OK);
	}

}
