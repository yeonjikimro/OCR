package com.example.demo.ocr.service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.util.Matrix;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.example.demo.common.DateUtils;
import com.example.demo.common.OcrUtils;
import com.example.demo.common.StringUtils;
import com.example.demo.config.Constant;
import com.example.demo.ocr.vo.OcrAddrVo;
import com.example.demo.ocr.vo.OcrCiVo;
import com.example.demo.ocr.vo.OcrJuminChobonVo;
import com.example.demo.ocr.vo.OcrPageVo;
import com.example.demo.ocr.vo.OcrWordCiVo;

import lombok.extern.slf4j.Slf4j;

/**
 * 주민등록초본 정보 추출
 * @version 1.0
 * @author lcg
 */
@Slf4j
@Service
public class OcrJuminChobonService {

    @Autowired
	private OcrService ocrService;

    @Autowired
	private OcrJsonParserService ocrJsonParserService;

    // 표준 날짜 형식
    final String dateFormat = "yyyy-MM-dd";
    final SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);

    // 좌표지정 텍스트
    final String APPLICANT_NAME_TXT = "성명(한자)";
    final String USE_PURPOSE_TXT = "용도및목적:";
    final String NUMBER_TXT = "번호";
    final String CHANGE_RESON_TXT = "변동사유";
    final String REG_STATE_TXT = "등록상태";

    // 주소구분 텍스트
    final String GYEONGGI_TXT = "경기도";
    // OCR 이미지 오차범위
    final int ERROR_RANGE = 10;

     /**
     * 초본 추출 데이터 - 발급일, 성명, 주민번호, 생년월일, 거주 주소 이력, 마지막 거주 주소
     * , 경기도 연속 거주일수, 경기도 합산 거주일수
     * 
     * @param String
     * @return Map<String,Object>
     * @throws Exception
     */
    public Map<String,Object> getChoBonData(String fileName) throws Exception {
        Map<String,Object> resultData = new HashMap<String,Object>();

        // JSON 파일에서 OCR 추출 테이터 가져오기
        JSONArray images = ocrJsonParserService.convertFileToJson(Constant.JUMIN_CHOBON, fileName);

        // 주민등록초본 추출 데이터
        OcrJuminChobonVo ojc = new OcrJuminChobonVo();
    
        // '용도및목적' 좌표
        OcrWordCiVo uptCi = new OcrWordCiVo(USE_PURPOSE_TXT);

        // '성명(한자)' 좌표
        OcrWordCiVo anmCi = new OcrWordCiVo(APPLICANT_NAME_TXT);

        // 발행일 추출 임시
        String tempIssueDt = "";

        // OCR 추축 텍스트 분류  
        for(int i = 0; i < images.size(); i++){
            // 주소 > '번호' 좌표
            OcrWordCiVo numCi = new OcrWordCiVo(NUMBER_TXT);

            // 주소 > '변동사유' 좌표
            OcrWordCiVo acrCi = new OcrWordCiVo(CHANGE_RESON_TXT);

            // 주소 > '등록상태' 좌표
            OcrWordCiVo rstCi = new OcrWordCiVo(REG_STATE_TXT);

            OcrPageVo opv = new OcrPageVo();
            opv.setPage(i);

            // 주소 데이터 
            List<OcrAddrVo> addrList = new ArrayList<>();

            JSONObject image = (JSONObject)images.get(i);
            JSONArray fields = (JSONArray)image.get("fields");


            // 주소 추출 임시
            String tempAddr = "";
            String tempAddrDt = "";
            OcrCiVo tempAddrCi = new OcrCiVo();
            String addrLine = "";
            int addrLineCnt = 0;
            // int addrCnt = 0;
            int addrStartXci = 0; // 주소시작x1 좌표
            
            for(int f = 0; f < fields.size(); f++){
                JSONObject field = (JSONObject)fields.get(f);
                // 추출 텍스트 좌표정보
                JSONObject boundingPoly = (JSONObject)field.get("boundingPoly");
                // 추출 텍스트
                String inferText = (String)field.get("inferText");
                // 추출 정확도
                Double inferConfidence = (Double)field.get("inferConfidence");
                // 추출 정확도 특정 포인트 미만은 제외 처리
                if(inferConfidence < 0.7){
                    inferText = "";
                }
                // 추출 텍스트 다음 줄바꿈 정보
                boolean lineBreak = (boolean)field.get("lineBreak");
                // 현재 문자 좌표
                OcrCiVo currentCi = OcrUtils.getCoordinate(boundingPoly);

                // 발행일 
                // 발행일이 없고 용도및목적 ci 확인
                if(ojc.getIssueDt().isEmpty() && uptCi.isCompareEnd()){
                    if(inferText.contains("년") || inferText.contains("월") || inferText.contains("일")){
                        tempIssueDt += inferText.replaceAll(" ", "") + " ";
                    }
                    Matcher issueDtMatchYMD = Pattern.compile(OcrUtils.dateRegEx2).matcher(tempIssueDt);
                    if(issueDtMatchYMD.find()){
                        ojc.setIssueDt(issueDtMatchYMD.group());
                    }
                }

                // 이름
                // 발행자 이름이 없고, 성명(한자) 다음에 오는 텍스트가 이름름
                if(ojc.getIssueNm().isEmpty() && anmCi.isCompareEnd()){
                    ojc.setIssueNm(inferText.trim());
                }

                // 주민등록번호
                if(ojc.getJuminRegNo().isEmpty() && anmCi.isCompareEnd()){
                    Matcher juminMatch1 = Pattern.compile(OcrUtils.juminNoRegEx1).matcher(inferText);
                    if(juminMatch1.find()){
                        ojc.setJuminRegNo(juminMatch1.group());
                    }
                    Matcher juminMatch2 = Pattern.compile(OcrUtils.juminNoRegEx2).matcher(inferText);
                    if(juminMatch2.find()){
                        ojc.setJuminRegNo(juminMatch2.group());
                    }
                }

                // 거주지주소
                // 번호 영역 - 주소시작 X1 좌표보다 작은 영역
                if(numCi.isCompareEnd() && currentCi.getX2() < addrStartXci){
                    // 번호 영역에 숫자가 아니면 주소 처리 초기화 - 숫자 마지막 라인 
                    Matcher numMatch = Pattern.compile(OcrUtils.numRegEx).matcher(inferText);
                    if(!numMatch.find() && addrLineCnt > 0){
                        // 주소정보 셋팅
                        OcrAddrVo oav = new OcrAddrVo();
                        oav.setAddr(tempAddr);
                        oav.setAddrDate(tempAddrDt);
                        oav.setAddrCi(tempAddrCi);
                        addrList.add(oav);

                        addrLineCnt = 0;
                        addrLine = "";
                        tempAddr = "";
                        tempAddrDt = "";
                    }
                }
                // 주소 영역
                if(acrCi.isCompareEnd() 
                    && currentCi.getX1() > addrStartXci
                    && currentCi.getX2() < acrCi.getWordCi().getX1()){
                    // 시도로 시작시 주소 시작 
                    if(addrLineCnt == 0 && StringUtils.sidoStartsWith(inferText)){
                        addrLineCnt = 1;
                        addrLine = inferText;
                        tempAddr = "";
                        tempAddrDt = "";
                        if(addrStartXci == 0){
                            // 주소텍스트 시작점 최초 1회 셋팅
                            addrStartXci = currentCi.getX1() - ERROR_RANGE;
                        }
                    } else if(addrLineCnt > 0) {
                        if(StringUtils.sidoStartsWith(inferText)){
                            // 주소 취합중 시도가 나오는 경우 주소 라인 재취합
                            OcrAddrVo oav = new OcrAddrVo();
                            oav.setAddr(tempAddr);
                            oav.setAddrDate(tempAddrDt);
                            addrList.add(oav);
                            
                            addrLineCnt = 1;
                            addrLine = inferText;
                            tempAddr = "";
                            tempAddrDt = "";
                            
                        } else if(inferText.startsWith("[")){
                            // 주소라인 텍스트가 '['로 시작하는 경우 
                            OcrAddrVo oav = new OcrAddrVo();
                            oav.setAddr(tempAddr);
                            oav.setAddrDate(tempAddrDt);
                            addrList.add(oav);

                            addrLineCnt = 0;
                            addrLine = "";
                            tempAddr = "";
                            tempAddrDt = "";
                        } else {
                            // 주소 텍스트 라인 취합 
                            addrLine += " " + inferText;
                        }
                    }
                }

                // 거주지 날짜 영역
                // 등록상태 ci 확인완료, 변동사유 시작점 좌표 이후 텍스트, 주소라인
                if(acrCi.isCompareEnd() && currentCi.getX1() > (acrCi.getWordCi().getX1() + ERROR_RANGE ) && addrLineCnt == 1 ) {
                    Matcher addrDateMatch = Pattern.compile(OcrUtils.dateRegEx1).matcher(inferText);
                    if(addrDateMatch.find()){
                        tempAddrDt = addrDateMatch.group();
                    }
                }

                // 줄바꿈
                if(lineBreak){
                    // 주소처리
                    if(addrLineCnt == 1){
                        if(tempAddrDt.isEmpty()){
                            // 첫라인에 날짜 정보가 없으면 주소가 아니라고 판단. > 오류
                            addrLineCnt = 0;
                        } else {
                            tempAddr = addrLine;
                            addrLine = "";
                            addrLineCnt = 2;
                        }
                    } else if(addrLineCnt > 1){
                        // 주소 셋팅 2번째라인시
                        if(!addrLine.isEmpty()){
                            tempAddr += " " + addrLine;
                            OcrAddrVo oav = new OcrAddrVo();
                            oav.setAddr(tempAddr);
                            oav.setAddrDate(tempAddrDt);
                            addrList.add(oav);

                            addrLineCnt = 0;
                            addrLine = "";
                            tempAddr = "";
                            tempAddrDt = "";
                        }
                    }
                }
                
                // 기준 좌표 지정 START

                // '용도및목적' 좌표 정의
                if(!uptCi.isCompareEnd()){
                    OcrUtils.findWordCi(uptCi, inferText, currentCi);
                }

                // '성명(한자)' 좌표 정의
                if(!anmCi.isCompareEnd()){
                    OcrUtils.findWordCi(anmCi, inferText, currentCi);
                }

                // 주소 > '번호' 좌표 정의
                if(!numCi.isCompareEnd() && currentCi.getX2() <= anmCi.getWordCi().getX2() ){
                    OcrUtils.findWordCi(numCi, inferText, currentCi);
                }
                
                // 주소 > '변동사유' 좌표 정의
                if(!acrCi.isCompareEnd()){
                    OcrUtils.findWordCi(acrCi, inferText, currentCi);
                }

                // 주소 > '등록상태' 좌표 정의
                if(!rstCi.isCompareEnd()){
                    OcrUtils.findWordCi(rstCi, inferText, currentCi);
                }

                // 기준 좌표 지정 END
            }

            // 페이지 셋팅
            opv.setAddrList(addrList);
            // 초본 셋팅
            ojc.getPageList().add(opv);
            ojc.getAddrList().addAll(addrList);
        }

        setGyeonggi(ojc, resultData);

        resultData.put("issueDt", ojc.getIssueDt());  
        resultData.put("issueNm", ojc.getIssueNm()); 
        resultData.put("juminRegNo", ojc.getJuminRegNo()); 
        resultData.put("birth", StringUtils.convertJmToBirth(ojc.getJuminRegNo())); 
        resultData.put("addrList", ojc.getAddrList()); 

        return resultData;
    }   

    /**
     * 초본 추출 데이터 분석 및 박스처리 사본생성
     * 
     * @param String - json 파일이름
     * @return Map<String,Object>
     * @throws Exception
     */
    public Map<String,Object> getChoBonCiData(String fileName) throws Exception {
        Map<String,Object> resultData = new HashMap<String,Object>();

        // JSON 파일에서 OCR 추출 테이터 가져오기
        JSONArray images = ocrJsonParserService.convertFileToJson(Constant.JUMIN_CHOBON, fileName);

        // 주민등록초본 추출 데이터 - file, orgFileName, fileExt 셋팅
        OcrJuminChobonVo ojc = new OcrJuminChobonVo(ocrService.getOcrFile(Constant.JUMIN_CHOBON, fileName));
        
        // '용도및목적' 좌표
        OcrWordCiVo uptCi = new OcrWordCiVo(USE_PURPOSE_TXT);

        // '성명(한자)' 좌표
        OcrWordCiVo anmCi = new OcrWordCiVo(APPLICANT_NAME_TXT);

        // 발행일 추출 임시
        String tempIssueDt = "";
        OcrCiVo tempIssueDtCi = new OcrCiVo();
        
        // OCR 추축 텍스트 분류  
        for(int i = 0; i < images.size(); i++){

            // 주소 > '번호' 좌표
            OcrWordCiVo numCi = new OcrWordCiVo(NUMBER_TXT);

            // 주소 > '변동사유' 좌표
            OcrWordCiVo acrCi = new OcrWordCiVo(CHANGE_RESON_TXT);

            // 주소 > '등록상태' 좌표
            OcrWordCiVo rstCi = new OcrWordCiVo(REG_STATE_TXT);

            OcrPageVo opv = new OcrPageVo();
            opv.setPage(i);

            // 추출데이터 좌표
            List<OcrCiVo> boxList = new ArrayList<>();

            // 주소 데이터 
            List<OcrAddrVo> addrList = new ArrayList<>();

            JSONObject image = (JSONObject)images.get(i);
            JSONArray fields = (JSONArray)image.get("fields");
            JSONObject convertedImageInfo = (JSONObject)image.get("convertedImageInfo");
            Long width = (Long)convertedImageInfo.get("width");
            Long height = (Long)convertedImageInfo.get("height");
            opv.setFileWidth(width.intValue());
            opv.setFileHeight(height.intValue());

            // 주소 추출 임시
            String tempAddr = "";
            String tempAddrDt = "";
            OcrCiVo tempAddrCi = new OcrCiVo();
            String addrLine = "";
            int addrLineCnt = 0;
            // int addrCnt = 0;
            int addrStartXci = 0;
            
            for(int f = 0; f < fields.size(); f++){
                JSONObject field = (JSONObject)fields.get(f);
                // 추출 텍스트 좌표정보
                JSONObject boundingPoly = (JSONObject)field.get("boundingPoly");
                // 추출 텍스트
                String inferText = (String)field.get("inferText");
                // 추출 정확도
                Double inferConfidence = (Double)field.get("inferConfidence");
                // 추출 정확도 특정 포인트 미만은 제외 처리
                if(inferConfidence < 0.7){
                    inferText = "";
                }
                // 추출 텍스트 다음 줄바꿈 정보
                boolean lineBreak = (boolean)field.get("lineBreak");
                // 현재 문자 좌표
                OcrCiVo currentCi = OcrUtils.getCoordinate(boundingPoly);
                // // 주출 좌표 페이지 정보
                // currentCi.setPage(i);

                // 발행일 
                // 발행일이 없고 용도및목적 ci 확인
                if(ojc.getIssueDt().isEmpty() && uptCi.isCompareEnd()){
                    if(inferText.contains("년") || inferText.contains("월") || inferText.contains("일")){
                        if(tempIssueDt.isEmpty()){
                            tempIssueDtCi = currentCi;
                        }else{
                            tempIssueDtCi.setX2(currentCi.getX2());
                            tempIssueDtCi.setY2(currentCi.getY2());
                            tempIssueDtCi.setX3(currentCi.getX3());
                            tempIssueDtCi.setY3(currentCi.getY3());
                        }
                        tempIssueDt += inferText.replaceAll(" ", "") + " ";
                    }
                    Matcher issueDtMatchYMD = Pattern.compile(OcrUtils.dateRegEx2).matcher(tempIssueDt);
                    if(issueDtMatchYMD.find()){
                        ojc.setIssueDt(issueDtMatchYMD.group());
                        boxList.add(tempIssueDtCi);
                    }
                }

                // 이름
                if(ojc.getIssueNm().isEmpty() && anmCi.isCompareEnd()){
                    boxList.add(currentCi);
                    ojc.setIssueNm(inferText.trim());
                }

                // 주민등록번호
                if(ojc.getJuminRegNo().isEmpty() && anmCi.isCompareEnd()){
                    Matcher juminMatch1 = Pattern.compile(OcrUtils.juminNoRegEx1).matcher(inferText);
                    if(juminMatch1.find()){
                        boxList.add(currentCi);
                        ojc.setJuminRegNo(juminMatch1.group());
                    }
                    Matcher juminMatch2 = Pattern.compile(OcrUtils.juminNoRegEx2).matcher(inferText);
                    if(juminMatch2.find()){
                        boxList.add(currentCi);
                        ojc.setJuminRegNo(juminMatch2.group());
                    }
                }

                // 거주지 주소
                // 번호 영역 - 주소시작 X1 좌표보다 작은 영역
                if(numCi.isCompareEnd() && currentCi.getX2() < addrStartXci){
                    // 번호 영역에 숫자가 아니면 주소 처리 초기화 - 숫자 마지막 라인 
                    Matcher numMatch = Pattern.compile(OcrUtils.numRegEx).matcher(inferText);
                    if(!numMatch.find() && addrLineCnt > 0){
                        // 주소정보 셋팅
                        OcrAddrVo oav = new OcrAddrVo();
                        oav.setAddr(tempAddr);
                        oav.setAddrDate(tempAddrDt);
                        oav.setAddrCi(tempAddrCi);
                        addrList.add(oav);

                        addrLineCnt = 0;
                        addrLine = "";
                        tempAddr = "";
                        tempAddrDt = "";
                    }
                }

                // 주소 영역
                if(acrCi.isCompareEnd() 
                    && currentCi.getX1() > addrStartXci
                    && currentCi.getX2() < acrCi.getWordCi().getX1()){
                    // 시도로 시작시 주소 시작 
                    if(addrLineCnt == 0 && StringUtils.sidoStartsWith(inferText)){
                        addrLineCnt = 1;
                        addrLine = inferText;
                        tempAddrCi = currentCi;
                        tempAddr = "";
                        tempAddrDt = "";
                        if(addrStartXci == 0){
                            // 주소텍스트 시작점 최초 1회 셋팅
                            addrStartXci = currentCi.getX1() - ERROR_RANGE;
                        }
                    } else if(addrLineCnt > 0) {
                        if(StringUtils.sidoStartsWith(inferText)){
                            // 주소 취합중 시도가 나오는 경우 주소 라인 재취합
                            OcrAddrVo oav = new OcrAddrVo();
                            oav.setAddr(tempAddr);
                            oav.setAddrDate(tempAddrDt);
                            oav.setAddrCi(tempAddrCi);
                            addrList.add(oav);
                        
                            addrLineCnt = 1;
                            addrLine = inferText;
                            tempAddrCi = currentCi;
                            tempAddr = "";
                            tempAddrDt = "";
                        } else if(inferText.startsWith("[")){
                            // 주소라인 텍스트가 '['로 시작하는 경우 
                            OcrAddrVo oav = new OcrAddrVo();
                            oav.setAddr(tempAddr);
                            oav.setAddrDate(tempAddrDt);
                            oav.setAddrCi(tempAddrCi);
                            addrList.add(oav);

                            addrLineCnt = 0;
                            addrLine = "";
                            tempAddr = "";
                            tempAddrDt = "";
                        } else {
                            // 주소 텍스트 라인 취합 
                            addrLine += " " + inferText;
                            tempAddrCi.setY3(currentCi.getY3());
                            tempAddrCi.setY4(currentCi.getY4());
                        }
                    }
                }

                // 거주지 날짜 영역
                // 등록상태 ci 확인완료, 변동사유 시작점 좌표 이후 텍스트, 주소라인
                if(acrCi.isCompareEnd() && currentCi.getX1() > (acrCi.getWordCi().getX1() + ERROR_RANGE ) && addrLineCnt > 0 ) {
                    Matcher addrDateMatch = Pattern.compile(OcrUtils.dateRegEx1).matcher(inferText);
                    if(addrDateMatch.find() && addrLineCnt == 1){
                        // 날짜 형식이고 주소 첫리인일경우
                        tempAddrDt = addrDateMatch.group();
                        // 주소 박스 끝라인 셋팅
                        tempAddrCi.setX2(currentCi.getX2());
                        tempAddrCi.setY2(currentCi.getY2());
                        tempAddrCi.setX3(currentCi.getX3());
                        tempAddrCi.setY3(currentCi.getY3());
                    }
                    
                    if(addrLineCnt > 1){
                        // 2번째라인부터 끝라인 처리
                        tempAddrCi.setX3(currentCi.getX3());
                        tempAddrCi.setY3(currentCi.getY3());
                    }
                }

                // 줄바꿈
                if(lineBreak){
                    // 주소처리
                    if(addrLineCnt == 1){
                        if(tempAddrDt.isEmpty()){
                            // 첫라인에 날짜 정보가 없으면 주소가 아니라고 판단. > 오류
                            addrLineCnt = 0;
                        } else {
                            tempAddr = addrLine;
                            addrLine = "";
                            addrLineCnt = 2;
                        }
                    } else if(addrLineCnt > 1){
                        // 주소 셋팅 2번째라인시
                        if(!addrLine.isEmpty()){
                            tempAddr += " " + addrLine;
                            OcrAddrVo oav = new OcrAddrVo();
                            oav.setAddr(tempAddr);
                            oav.setAddrDate(tempAddrDt);
                            oav.setAddrCi(tempAddrCi);
                            addrList.add(oav);
                            addrLine = "";
                            tempAddr = "";
                            addrLineCnt = 0;
                        }
                    }
                }
                
                // 기준 좌표 지정 START

                // '용도및목적' 좌표 정의
                if(!uptCi.isCompareEnd()){
                    OcrUtils.findWordCi(uptCi, inferText, currentCi);
                }

                // '성명(한자)' 좌표 정의
                if(!anmCi.isCompareEnd()){
                    OcrUtils.findWordCi(anmCi, inferText, currentCi);
                }

                // 주소 > '번호' 좌표 정의
                if(!numCi.isCompareEnd() && currentCi.getX2() <= anmCi.getWordCi().getX2() ){
                    OcrUtils.findWordCi(numCi, inferText, currentCi);
                }
                
                // 주소 > '변동사유' 좌표 정의
                if(!acrCi.isCompareEnd()){
                    OcrUtils.findWordCi(acrCi, inferText, currentCi);
                }

                // 주소 > '등록상태' 좌표 정의
                if(!rstCi.isCompareEnd()){
                    OcrUtils.findWordCi(rstCi, inferText, currentCi);
                }

                // 기준 좌표 지정 END
            }

            if(i == 0){
                // 첫페이지 번호 ci  - 계산값 노출 기준
                ojc.setNumCi(numCi.getWordCi());
            }
            // 페이지 셋팅
            opv.setBoxList(boxList);
            opv.setAddrList(addrList);
            // 초본 셋팅
            ojc.getPageList().add(opv);
            ojc.getAddrList().addAll(addrList);
        }
        
        setGyeonggiCi(ojc, resultData);
 
        if("PDF".equals(ojc.getFileExt())){
            createBoxPdfFile(ojc);
        } else {
            createBoxImageFile(ojc);
        }

        resultData.put("issueDt", ojc.getIssueDt());  
        resultData.put("issueNm", ojc.getIssueNm()); 
        resultData.put("juminRegNo", ojc.getJuminRegNo()); 
        resultData.put("birth", StringUtils.convertJmToBirth(ojc.getJuminRegNo())); 
        resultData.put("addrList", ojc.getAddrList()); 

        return resultData;
    }

    /**
     * @param jusoList
     * @param cbData
     * @throws ParseException
     * @throws IOException
     * @see 경기도 거주 시작일
     * @see 경기도 마지막 거주 이전일
     * @see 경기도 거주 총 일수
     * @see 경기도 연속 거주 총 일수
     * @see 경기도 거주 기간, 거주 주소
     */
    public void setGyeonggi(OcrJuminChobonVo ojc, Map<String,Object> ocrData) throws java.text.ParseException, IOException{
        int day = 0; // 경기도 거주 일수
        int conDay = 0; // 경기도 연속 거주 일수
        String residStartDt = ""; // 경기도 거주 시작 일자
        String residEndDt = ""; // 경기도 거주 마지막 이전 일자
        String residConStartDt = ""; // 경기도 연속 거주 시작 일자
        String residDt = ""; // 경기도 거주 일자
        boolean ggResid = false; // 경기도 거주 여부
        String currentAddr = ""; // 마지막 주소 현거주지
        List<String> ggAddrList = new ArrayList<String>();
        for(int p=0; p < ojc.getPageList().size(); p++){
            OcrPageVo opv = ojc.getPageList().get(p);

            for(int i=0; i < opv.getAddrList().size(); i++){
                OcrAddrVo oav = opv.getAddrList().get(i);

                // 마지막 주소를 현주소로 셋팅
                if(p == ojc.getPageList().size() - 1 && i == opv.getAddrList().size()-1){
                    currentAddr = oav.getAddr();
                }

                if(oav.getAddr().startsWith(GYEONGGI_TXT)){
                    // 경기도 주소 리스트업
                    ggAddrList.add(oav.getAddr());
                    // 경기도 주소 박스 처리
                    opv.getBoxList().add(oav.getAddrCi());
                    // 경기도 연속 거주 시작 일자가 초기화 되어있으면 셋팅
                    if(residConStartDt.isEmpty()){
                        residConStartDt = oav.getAddrDate();
                    }
                    // 거주일 계산
                    if(residDt.isEmpty()){
                        // 최초 경기도 거주 시작일
                        residDt = oav.getAddrDate();
                        residStartDt = oav.getAddrDate();
                        residEndDt = oav.getAddrDate();
                        opv.getBoxList().add(oav.getAddrCi());
                        ggResid = true;
                    } else {
                        if(ggResid){
                            // 경기도에서 경기도 거주 이전 기간 산정
                            int addrTerm = DateUtils.dateTermDays(residDt, oav.getAddrDate(), dateFormat);
                            day += addrTerm;
                            residDt = oav.getAddrDate();
                        } else{
                            // 다른지역에서 경기도 거주 이전
                            residDt = oav.getAddrDate();
                            ggResid = true;
                        }
                        
                        residEndDt = oav.getAddrDate();
                    }
                } else {
                    if(ggResid){
                        // 경기도에서 다른지역 거주 이전
                        day += DateUtils.dateTermDays(residDt, oav.getAddrDate(), dateFormat);
                        residDt = oav.getAddrDate();
                        ggResid = false;
                        residConStartDt = "";
                    }
                }
            }
        }

        if(ggResid){
            // 경기도에서 거주중일경우 현재 날짜 포함 거주기간 포함, 현재 날짜기준 정의 필요
            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            String currentDt = formatter.format(date);
            day += DateUtils.dateTermDays(residDt, currentDt, dateFormat);
            conDay = DateUtils.dateTermDays(residConStartDt, currentDt, dateFormat);
        }

        // 현거주지 
        ocrData.put("currentAddr", currentAddr); 
        // 경기도 거주 시작일
        ocrData.put("residStartDt", residStartDt); 
        // 경기도 연속거주 시작일
        ocrData.put("residConStartDt", residConStartDt); 
        // 마지막 경기도 거주 이전일
        ocrData.put("residEndDt", residEndDt); 
        // 경기도 연속 거주 일수
        ocrData.put("conResidenceDay", Integer.toString(conDay));
        // 경기도 연속 거주 년월일
        ocrData.put("conResidenceYmd", DateUtils.convertDayToYMD(conDay));
        // 경기도 합산 거주 일수
        ocrData.put("residenceDay", Integer.toString(day));
        // 경기도 합산 거주 년월일
        ocrData.put("residenceYmd", DateUtils.convertDayToYMD(day)); 
        // 경기도 거주 리스트
        ocrData.put("ggAddrList", ggAddrList); 
    }

    /**
     * @param jusoList
     * @param cbData
     * @throws ParseException
     * @throws IOException
     * @see 경기도 거주 시작일
     * @see 경기도 마지막 거주 이전일
     * @see 경기도 거주 총 일수
     * @see 경기도 연속 거주 총 일수
     * @see 경기도 거주 기간, 거주 주소
     */
    public void setGyeonggiCi(OcrJuminChobonVo ojc, Map<String,Object> ocrData) throws java.text.ParseException, IOException{
        int day = 0; // 경기도 거주 일수
        int conDay = 0; // 경기도 연속 거주 일수
        String residStartDt = ""; // 경기도 거주 시작 일자
        String residEndDt = ""; // 경기도 거주 마지막 이전 일자
        String residConStartDt = ""; // 경기도 연속 거주 시작 일자
        String residDt = ""; // 경기도 거주 일자
        boolean ggResid = false; // 경기도 거주 여부
        String currentAddr = ""; // 마지막 주소 현거주지
        List<String> ggAddrList = new ArrayList<String>();
        for(int p=0; p < ojc.getPageList().size(); p++){
            OcrPageVo opv = ojc.getPageList().get(p);

            for(int i=0; i < opv.getAddrList().size(); i++){
                OcrAddrVo oav = opv.getAddrList().get(i);
                // 마지막 주소를 현주소로 셋팅
                if(p == ojc.getPageList().size() - 1 && i == opv.getAddrList().size()-1){
                    currentAddr = oav.getAddr();
                }

                if(oav.getAddr().startsWith(GYEONGGI_TXT)){
                    // 경기도 주소 리스트업
                    ggAddrList.add(oav.getAddr());
                    // 경기도 주소 박스 처리
                    opv.getBoxList().add(oav.getAddrCi());
                    // 경기도 연속 거주 시작 일자가 초기화 되어있으면 셋팅
                    if(residConStartDt.isEmpty()){
                        residConStartDt = oav.getAddrDate();
                    }
                    // 거주일 계산
                    if(residDt.isEmpty()){
                        // 최초 경기도 거주 시작일
                        residDt = oav.getAddrDate();
                        residStartDt = oav.getAddrDate();
                        residEndDt = oav.getAddrDate();
                        opv.getBoxList().add(oav.getAddrCi());
                        ggResid = true;
                    } else {
                        if(ggResid){
                            // 경기도에서 경기도 거주 이전 기간 산정
                            int addrTerm = DateUtils.dateTermDays(residDt, oav.getAddrDate(), dateFormat);
                            day += addrTerm;
                            oav.getAddrCi().setText(Integer.toString(addrTerm) + "일");
                            residDt = oav.getAddrDate();
                        } else{
                            // 다른지역에서 경기도 거주 이전
                            oav.getAddrCi().setText("0일");
                            opv.getBoxList().add(oav.getAddrCi());
                            residDt = oav.getAddrDate();
                            ggResid = true;
                        }
                        
                        residEndDt = oav.getAddrDate();
                    }
                } else {
                    if(ggResid){
                        // 경기도에서 다른지역 거주 이전
                        day += DateUtils.dateTermDays(residDt, oav.getAddrDate(), dateFormat);
                        residDt = oav.getAddrDate();
                        ggResid = false;
                        residConStartDt = "";
                    }
                }
            }
        }

        if(ggResid){
            // 경기도에서 거주중일경우 현재 날짜 포함 거주기간 포함, 현재 날짜기준 정의 필요
            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            String currentDt = formatter.format(date);
            day += DateUtils.dateTermDays(residDt, currentDt, dateFormat);
            conDay = DateUtils.dateTermDays(residConStartDt, currentDt, dateFormat);
        }
        // 경기도 거주기간 계산값
        String sumText = GYEONGGI_TXT+ " 연속거주일 : " + Integer.toString(conDay)+ "일, " 
            + GYEONGGI_TXT +" 합산거주일 : " + Integer.toString(day)+"일";
        // 첫페이지 번호 영역 상단에 위치
        OcrCiVo sumCi = new OcrCiVo();
        sumCi.setX2(ojc.getNumCi().getX1());
        sumCi.setY2(ojc.getNumCi().getY1() - 100);
        sumCi.setText(sumText);
        ojc.setSumCi(sumCi);

        // 현거주지 
        ocrData.put("currentAddr", currentAddr); 
        // 경기도 거주 시작일
        ocrData.put("residStartDt", residStartDt); 
        // 경기도 연속거주 시작일
        ocrData.put("residConStartDt", residConStartDt); 
        // 마지막 경기도 거주 이전일
        ocrData.put("residEndDt", residEndDt); 
        // 경기도 연속 거주 일수
        ocrData.put("conResidenceDay", Integer.toString(conDay));
        // 경기도 연속 거주 년월일
        ocrData.put("conResidenceYmd", DateUtils.convertDayToYMD(conDay));
        // 경기도 합산 거주 일수
        ocrData.put("residenceDay", Integer.toString(day));
        // 경기도 합산 거주 년월일
        ocrData.put("residenceYmd", DateUtils.convertDayToYMD(day)); 
        // 경기도 거주 리스트
        ocrData.put("ggAddrList", ggAddrList); 
    }

    /**
     * 초본 사본 파일에 박스처리 및 계산값 그리기
     * 
     * @param String
     * @param List<OcrCiVo> 
     * @throws IOException
     */
    public void createBoxImageFile(OcrJuminChobonVo ojc) throws IOException{
        String newFileDir = Constant.JSON_DIR_ROOT+File.separator+Constant.JUMIN_CHOBON+"_copy";

        BufferedImage image = ImageIO.read(ojc.getFile());
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND,0));
        g2d.setFont(new Font("NanumGothic", Font.PLAIN, 20));

        int boxCorrection = 2;
        for(int p = 0; p < ojc.getPageList().size(); p++){
            OcrPageVo opv = ojc.getPageList().get(p);
            if(p == 0 && !ojc.getSumCi().getText().isEmpty()){
                // 거주기간 합산 값
                g2d.drawString(ojc.getSumCi().getText(), ojc.getSumCi().getX2(), ojc.getSumCi().getY2()); // 문자열 삽입
            }
            for(OcrCiVo boxCi : opv.getBoxList()){
                if(boxCi.getX1() > 0){
                    g2d.drawRect(boxCi.getX1() - boxCorrection, boxCi.getY1() - boxCorrection
                    , (boxCi.getX2() - boxCi.getX1() + (boxCorrection * 2))
                    , (boxCi.getY4() - boxCi.getY1() + (boxCorrection * 2)));
                }
                if(!boxCi.getText().isEmpty()){
                    g2d.drawString(boxCi.getText(), boxCi.getX2() + 5, boxCi.getY2() + 10); // 문자열 삽입
                }
            }
        }

        g2d.dispose();
        ImageIO.write(image, ojc.getFileExt(), new File(newFileDir + File.separator + ojc.getFileName()));
    }
    
     /**
     * pdf 파일 추출영역 box 처리   
     * 
     * @param OcrJuminChobonVo
     * @return Map<String,Long>
     * @throws Exception
     */
    public void createBoxPdfFile(OcrJuminChobonVo ojc) throws IOException{
        String newFileDir = Constant.JSON_DIR_ROOT+File.separator+Constant.JUMIN_CHOBON+"_copy";

        PDDocument document = PDDocument.load(ojc.getFile()); 

        // 폰트 설정
        ClassPathResource resource = new ClassPathResource("\\static\\font\\NanumGothic.ttf");
        PDType0Font font = PDType0Font.load(document, resource.getFile());

        for(int p = 0; p < document.getPages().getCount(); p++){
            PDPage page = document.getPage(p);
            OcrPageVo opv = ojc.getPageList().get(p);

            // 컨텐츠 스트림 열기
            PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
            
            // 스케일링
            log.debug("#####page.getMetadata() width = " + page.getMediaBox().getWidth());
            log.debug("#####page.getMetadata() height = " + page.getMediaBox().getHeight());
            log.debug("#####page.org width = " + opv.getFileWidth());
            log.debug("#####page.org height = " + opv.getFileHeight());
            float w = page.getMediaBox().getWidth()/opv.getFileWidth();
            float h = page.getMediaBox().getHeight()/opv.getFileHeight();

            contentStream.transform(Matrix.getScaleInstance(w, h));
            log.debug("#####width per= " + w);
            log.debug("#####height per= " + h);
            
            // 선 색상 설정 (검은색으로 설정)
            PDColor color = new PDColor(new float[]{1, 0, 0}, PDDeviceRGB.INSTANCE);
            contentStream.setStrokingColor(color);
            contentStream.setNonStrokingColor(color);
            contentStream.setFont(font, 30); 
            // 선 두께 설정
            contentStream.setLineWidth(2);

            // 첫페이지
            if(p == 0){
                if(!ojc.getSumCi().getText().isEmpty()){
                    // 문자열 삽입
                    contentStream.beginText();
                    // contentStream.setFont(font, fontSize);
                    contentStream.newLineAtOffset(ojc.getSumCi().getX2()
                    , opv.getFileHeight() - ojc.getSumCi().getY2());
                    contentStream.showText(ojc.getSumCi().getText());
                    contentStream.endText();
                }
            }

            // 박스 그리기 및 텍스트 적용
            for(OcrCiVo boxCi : opv.getBoxList()){
                float x1 = boxCi.getX1();
                float y1 = boxCi.getY4();
                float width = boxCi.getX2() - boxCi.getX1();
                float height = boxCi.getY4() - boxCi.getY1();

                // 사각형 그리기
                if(width > 0 && height > 0){
                    contentStream.addRect(x1, opv.getFileHeight() - y1, width, height);
                    contentStream.stroke();
                }

                if(!boxCi.getText().isEmpty()){
                    // 문자열 삽입
                    contentStream.beginText();
                    // contentStream.setFont(font, fontSize);
                    contentStream.newLineAtOffset(boxCi.getX2()
                        , opv.getFileHeight() - boxCi.getY2() - 20);
                    contentStream.showText(boxCi.getText());
                    contentStream.endText();
                }
            }    
            
            contentStream.close();
        }

        document.save(new File(newFileDir+File.separator+ojc.getFileName()));
        document.close();
    }
}
