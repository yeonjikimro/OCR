package com.example.demo.ocr.vo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * 주민등록등본 OCR 정보
 * @version 1.0
 * @author lcg
 */
@Getter
@Setter
public class OcrJuminDeungbonVo {
    //초본 파일
    File file;
    //초본 파일이름
    String fileName = "";
    //초본 파일확장자
    String fileExt = "";
    //OCR 데이터 분석 페이지 리스트
    OcrPageVo ocrPage = new OcrPageVo();

    //신청인
    String issueNm = "";
    //발급일
    String issueDt = "";
    //지원자
    String applyNm = "";
    //주민등록번호
    String juminRegNo = "";
    //현 거주지
    String address = "";
    //구성원 리스트
    List<Map<String,String>> familyList = new ArrayList<Map<String,String>>();

    public OcrJuminDeungbonVo(){
    }

    public OcrJuminDeungbonVo(File file){
        String orgFileName = file.getName();
        int lastIndex =  orgFileName.lastIndexOf(".");
        this.fileExt = orgFileName.substring(lastIndex + 1).toUpperCase();
        this.fileName = orgFileName;
        this.file = file;
    }
}
