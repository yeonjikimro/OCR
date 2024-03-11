package com.example.demo.ocr.vo;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * 주민등록초본/등본 OCR 데이터 분석 페이지 정보 ANALYSIS
 * @version 1.0
 * @author lcg
 */
@Getter
@Setter
public class OcrPageVo {
    // 페이지 번호
    int page = 0;
    // ocr 파일 넓이
    int fileWidth = 0;
    // ocr 파일 높이
    int fileHeight = 0;
    // 추출 데이터 박스 
    List<OcrCiVo> boxList = new ArrayList<>();
    // 페이지별 주소, 날짜, 좌표
    List<OcrAddrVo> addrList = new ArrayList<OcrAddrVo>();
}
