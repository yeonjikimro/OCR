package com.example.demo.ocr.service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.config.Constant;
import com.example.demo.ocr.vo.OcrCiVo;
import com.example.demo.ocr.vo.OcrJuminDeungbonVo;

import groovy.util.logging.Slf4j;

@Service
@Slf4j
public class OcrPractice {

    @Autowired
	private OcrJsonParserService ocrJsonParserService;


        public Map<String,Object> getDeungbonData(String fileName) throws Exception {
        Map<String,Object> ocrData = new HashMap<String,Object>();
        // JSON 파일에서 텍스트 추출 테이터 가져오기
        JSONArray fields = ocrJsonParserService.convertFileToJson(Constant.JUMIN_DEUNGBON, fileName);
        OcrJuminDeungbonVo ojd = new OcrJuminDeungbonVo();
        List<OcrCiVo> boxList = new ArrayList<>();




        return ocrData;

        }



}
