package com.example.demo.ocr.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * OCR 주소 및 주소 좌표 정보
 * @version 1.0
 * @author lcg
 */
@Getter
@Setter
public class OcrAddrVo {
    String addr = "";
    String addrDate = "";
    OcrCiVo addrCi = new OcrCiVo();
}
