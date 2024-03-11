package com.example.demo.ocr.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * OCR 특정 문자열 좌표 추출 정보
 * @version 1.0
 * @author lcg
 */
@Getter
@Setter
public class OcrWordCiVo {
    boolean compareStart; // 문자열 비교 시작
    boolean compareEnd; // 문자열 비교 완료
    String word; // 전체문자열
    // Map<String,Long> wordCi; // 전체문자열 ci
    OcrCiVo wordCi;
    int tempIdx = 0; // 문자열 비교 순서
    String tempWord =""; // 비교 및 축적문자열
    // Map<String,Long> tempWordCi; // 임시문자 ci
    OcrCiVo tempWordCi;
    String compareWord = ""; // 비교문자
    // Map<String,Long> compareWordCi; // 비교문자 ci
    OcrCiVo compareWordCi;

    public  OcrWordCiVo(String  word){
        this.compareStart = false;
        this.compareEnd = false;
        this.word = word;
        // this.wordCi = new HashMap<String,Long>();
        this.wordCi = new OcrCiVo();
        this.tempIdx = 0;
        this.tempWord = "";
        // this.tempWordCi = new HashMap<String,Long>();
        this.tempWordCi = new OcrCiVo();
        this.compareWord = "";
        // this.compareWordCi = new HashMap<String,Long>();
        this.compareWordCi = new OcrCiVo();
    }
}
