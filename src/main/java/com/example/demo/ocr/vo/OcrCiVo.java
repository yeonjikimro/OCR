package com.example.demo.ocr.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * OCR 문자열 위치 정보
 * @version 1.0
 * @author lcg
 */
@Getter
@Setter
public class OcrCiVo {
    int x1;
    int y1;
    int x2;
    int y2;
    int x3;
    int y3;
    int x4;
    int y4;
    String text = "";
    int page;
    public String toString(){
        return "page:" + page + ", x1:" + x1 + ", y1:" + y1 + ", x2:" + x2 + ", y2:" + y2
            + ", x3:" + x3 + ", y3:" + y3 + ", x4:" + x4 + ", y4:" + y4;
    }
}
