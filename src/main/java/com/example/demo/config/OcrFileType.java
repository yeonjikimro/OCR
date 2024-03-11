package com.example.demo.config;

import java.util.Arrays;

public enum OcrFileType {
    // 주민등록초본
    JUMIN_CHOBON("1", Constant.JUMIN_CHOBON, Constant.OCR_GENERAL),
    // 건강보험 자격득실 확인서 Confirmation of Health Insurance Eligibility Acquisition/Loss
    HIEALC("2", Constant.HIEALC, Constant.OCR_GENERAL),
    // 결재영수증
    RECEIPT("3", Constant.RECEIPT, Constant.OCR_TEMPLATE),
    // 통장 사본
    BANKBOOK_DETAILS("4", Constant.BANKBOOK, Constant.OCR_DOCUMENT),
    // 주민등록등본
    JUMIN_DEUNGBON("5", Constant.JUMIN_DEUNGBON, Constant.OCR_GENERAL);

    private String code;
	private String value;
    private String ocrType;

	private OcrFileType(String code, String value, String ocrType) {
		this.code = code;
		this.value = value;
        this.ocrType = ocrType;
	}

    public String getCode(){
        return code;
    }

    public String getValue(){
        return value;
    }

    public String getOcrType(){
        return ocrType;
    }

	public static OcrFileType valueOfOcrType(String value) {
		return Arrays.stream(values())
				.filter(e -> value.equals(e.value))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException(String.format("%s OcrFileType NOT FOUND", value)));
	}

}
