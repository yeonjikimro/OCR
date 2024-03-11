package com.example.demo.ocr.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.common.OcrUtils;
import com.example.demo.config.Constant;

/**
 * 건강보험 자격득실 확인서 정보 추출
 * @version 1.0
 * @author lcg
 */
@Service
public class OcrHiealcService {

    @Autowired
	private OcrJsonParserService ocrJsonParserService;

    // 정규식
    final String numReqExp = "^[0-9]+$"; //숫자일경우
    final String juminNoReqExp = "\\d{6}\\-[1-4](\\d{6}|\\*{6})"; //주민번호
    final String dateReqExp = "(19|20|21)\\d{2}(\\W|년)(\\s*?)((10|11|12)|(0?(\\d)))(\\W|월)(\\s*?)(30|31|((0|1|2)?\\d))(\\W?)"; // 날짜일경우
    // final String dateReqExp2 = "(19|20|21)\\d{2}년\\s((10|11|12)|(0?(\\d)))월\\s(30|31|((0|1|2)?\\d))일"; // 날짜일경우 ****년 **월 **일

    //광역시도
    final String[] sidoArray = {"서울","인천","대전","세종","부산","대구","울산","광주","경기","강원도","충청","경상","전라","제주"};

    // 표준 날짜 형식
    final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    // 건강보험 자격득실 확인서데이터 - 발급일, 성명, 주민등록번호, 자격 득실이력
    public Map<String,Object> getHiealcData(String fileName) throws Exception {
        Map<String,Object> ocrData = new HashMap<String,Object>();
        // JSON 파일에서 텍스트 추출 테이터 가져오기
        JSONArray fields = ocrJsonParserService.convertFileToJson(Constant.HIEALC, fileName);

        // 성명
        int nmCiStart = 0; // 성명 텍스트 시작 좌표
        int nmCiEnd = 0; // 성명 텍스트 종료 좌표
        // 발급자 성명
        String issueNm = "";
        // 주민번호
        String juminNo = "";
        // 발급일자
        boolean issueDtStart = false; // 발급일자 셋팅 시작
        boolean ealcStart = false; // 자격득실 확인내역 셋팅 시작

        List<String> ealcList = new ArrayList<String>();
        String issueDt = ""; 
        String lineText = "";

        // OCR 추축 텍스트 분류  
        for(int i = 0; i < fields.size(); i++){
            JSONObject field = (JSONObject)fields.get(i);
            // 추출 텍스트 좌표정보
            JSONObject boundingPoly = (JSONObject)field.get("boundingPoly");
            // 추출 텍스트
            String inferText = (String)field.get("inferText");
            // 추출 텍스트 다음 줄바꿈 정보
            boolean lineBreak = (boolean)field.get("lineBreak");
            // 현재 문자 좌표
            Map<String,Long> currentCi = OcrUtils.getCoordinateMap(boundingPoly);

            lineText += inferText + " ";

            // 성명 텍스트 좌표가 있고, 성명값이 없으며 성명 좌표를 포함하거나 좌표 안에있는 단어 추출
            if(nmCiStart > 0 && nmCiEnd > 0 && issueNm.isEmpty()){
                int nmTempCiStart = currentCi.get("x1").intValue();
                int nmTempCiEnd = currentCi.get("x2").intValue();
                if(nmTempCiStart > (nmCiStart - 30) && nmTempCiEnd < (nmCiEnd + 30)){
                    issueNm = inferText;
                }
            }

            // 성명 텍스트 좌표 추출
            if(nmCiStart == 0){
                if("성명".equals(inferText) || "성 명".equals(inferText)){
                    nmCiStart = currentCi.get("x1").intValue();
                    nmCiEnd = currentCi.get("x2").intValue();
                } else if("성".equals(inferText)) {
                    nmCiStart = currentCi.get("x1").intValue();
                }
            } else {
                if(nmCiEnd == 0 && "명".equals(inferText)){
                    nmCiEnd = currentCi.get("x2").intValue();
                }
            }

            // 주민번호
            if(!issueNm.isEmpty() && juminNo.isEmpty()){
                Matcher lineMatchJuminNo = Pattern.compile(juminNoReqExp).matcher(inferText);
                if(lineMatchJuminNo.find()){
                    juminNo = lineMatchJuminNo.group();
                }
            }

            // 발급일자
            if(issueDtStart){
                Matcher lineMatchIssueDt = Pattern.compile(dateReqExp).matcher(lineText);
                if(lineMatchIssueDt.find()){
                    issueDt = lineMatchIssueDt.group();
                    issueDtStart = false;
                }
            }
            // 발급일 시작
            if(inferText.contains("확인합니다")){
                issueDtStart = true;
            }

            // 줄바꿈일 경우 라인텍스트 초기화
            if(lineBreak){
                // 자격득실확인내역
                if(ealcStart){
                    // 날짜 값이 존재하고 숫자로 시작하는 라인만 추가
                    Matcher lineMatchEaDt = Pattern.compile(dateReqExp).matcher(lineText);
                    if(lineMatchEaDt.find()){
                        String[] lineTests = lineText.split(" ");
                        if(lineTests != null && lineTests.length > 0 && StringUtils.isNumeric(lineTests[0])){
                            ealcList.add(lineText);
                        }
                    }
                }

                // 발급일 시작
                if(lineText.toUpperCase().contains("NO") && lineText.contains("가입자구분")){
                    ealcStart = true;
                }

                lineText = "";
            }
        }
        
        ocrData.put("issueDt", issueDt); 
        ocrData.put("issueNm", issueNm); 
        ocrData.put("juminNo", juminNo); 
        ocrData.put("ealcList", ealcList); 

        return ocrData;
    }
}
