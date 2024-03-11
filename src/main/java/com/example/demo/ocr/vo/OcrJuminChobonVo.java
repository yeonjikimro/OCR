package com.example.demo.ocr.vo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * 주민등록초본 OCR 정보
 * @version 1.0
 * @author lcg
 */
@Getter
@Setter
public class OcrJuminChobonVo {
    //초본 파일
    File file;
    //초본 파일이름
    String fileName = "";
    //초본 파일확장자
    String fileExt = "";
    //OCR 데이터 분석 페이지 리스트
    List<OcrPageVo> pageList = new ArrayList<>();
    // 번호 ci 정보 - 계산값 작성기준 위치
    OcrCiVo numCi = new OcrCiVo();
    // 주소계산값 ci 정보
    OcrCiVo sumCi = new OcrCiVo();
    //발급일
    String issueDt="";
    //이름
    String issueNm="";
    //주민등록번호
    String juminRegNo="";
    //현 거주지
    String currentAddr="";
    //전체 주소,날짜리스트
    List<OcrAddrVo> addrList = new ArrayList<OcrAddrVo>();

    public OcrJuminChobonVo(){
    }

    public OcrJuminChobonVo(File file){
        String orgFileName = file.getName();
        int lastIndex =  orgFileName.lastIndexOf(".");
        this.fileExt = orgFileName.substring(lastIndex + 1).toUpperCase();
        this.fileName = orgFileName;
        this.file = file;
    }
}
