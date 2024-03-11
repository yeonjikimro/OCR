package com.example.demo.common;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import com.example.demo.ocr.vo.OcrCiVo;
import com.example.demo.ocr.vo.OcrWordCiVo;

@Component
public class OcrUtils {

    // OCR 정규식
    public final static String numRegEx = "^[0-9]+$"; //숫자일경우
    public final static String juminNoRegEx1 = "\\d{6}\\-[1-4](\\d{6}|\\*{6})"; //주민번호 111111-1111111
    public final static String juminNoRegEx2 = "\\d{6}\\-[1-4](\\d*|\\*+)"; //주민번호2 111111-1******
    public final static String dateRegEx1 = "(19|20|21)\\d{2}\\-((10|11|12)|(0?(\\d)))\\-(30|31|((0|1|2)?\\d))"; // 날짜일경우 ****-**-**
    public final static String dateRegEx2 = "(19|20|21)\\d{2}년\\s?((10|11|12)|(0?(\\d)))월\\s?(30|31|((0|1|2)?\\d))일"; // 날짜일경우 ****년 **월 **일
    
    /**
     * 문자 좌표 JSONObject > Map<String,Long>
     * 1-좌상, 2-우상, 3-우하, 4-좌하
     * 
     * @param JSONObject - 좌표 JSON DATA
     * @return Map<String,Long>
     * @throws Exception
     */
    public static Map<String,Long> getCoordinateMap(JSONObject boundingPoly) throws Exception{
        Map<String,Long> ci = new HashMap<>();
        JSONArray vertices = (JSONArray)boundingPoly.get("vertices");
        for(int i = 0; i < vertices.size(); i++){
            JSONObject vt = (JSONObject)vertices.get(i);
            ci.put("x"+(i+1), Math.round((Double) vt.get("x")));
            ci.put("y"+(i+1), Math.round((Double) vt.get("y")));
        }
        return ci;
    }

    /**
     * 문자 좌표 JSONObject > Map<String,Long>
     * 1-좌상, 2-우상, 3-우하, 4-좌하
     * 
     * @param JSONObject - 좌표 JSON DATA
     * @return OcrCiVo
     * @throws Exception
     */
    public static OcrCiVo getCoordinate(JSONObject boundingPoly) throws Exception{
        OcrCiVo ocv = new OcrCiVo();
        JSONArray vertices = (JSONArray)boundingPoly.get("vertices");
        for(int i = 0; i < vertices.size(); i++){
            JSONObject vt = (JSONObject)vertices.get(i);
            switch(i){
                case 0: 
                    ocv.setX1((int)Math.round((Double) vt.get("x")));
                    ocv.setY1((int)Math.round((Double) vt.get("y")));
                    break;
                case 1: 
                    ocv.setX2((int)Math.round((Double) vt.get("x")));
                    ocv.setY2((int)Math.round((Double) vt.get("y")));
                    break;
                case 2: 
                    ocv.setX3((int)Math.round((Double) vt.get("x")));
                    ocv.setY3((int)Math.round((Double) vt.get("y")));
                    break;
                case 3: 
                    ocv.setX4((int)Math.round((Double) vt.get("x")));
                    ocv.setY4((int)Math.round((Double) vt.get("y")));
                    break;
            }
        }
        return ocv;
    }

    /**
     * 문자열 CI 맵핑
     * 
     * @param OcrWordCiVo
     * @param String
     * @param Map<String,Long>
     */
    public static void findWordCi(OcrWordCiVo owc, String inferText, OcrCiVo currentCi){
        // 모든 공백 제거
        String reText = inferText.replaceAll(" ", "");
        if(!reText.isEmpty()){
            if(!owc.isCompareStart()){
                if(reText.equals(owc.getWord()) || StringUtils.containsChar(owc.getWord(), reText)){
                    // 비교 문자와 모두 일치, 전체 포함 일치
                    owc.setCompareStart(true);
                    owc.setCompareEnd(true);
                    owc.setWordCi(currentCi);
                } else if(owc.getWord().startsWith(reText)){
                    // 비교 문자가 시작점일때
                    owc.setCompareStart(true);
                    owc.setTempWord(reText);
                    owc.setTempWordCi(currentCi);
                }
            } else {
                // 문자비교 1회 이상인경우
                String tempText = owc.getTempWord() + reText;
                if(tempText.equals(owc.getWord()) || StringUtils.containsChar(owc.getWord(), tempText)){
                    // 최종문자열
                    OcrCiVo tempCi = owc.getTempWordCi();
                    tempCi.setX2(currentCi.getX2());
                    tempCi.setY2(currentCi.getY2());
                    tempCi.setX3(currentCi.getX3());
                    tempCi.setY3(currentCi.getY3());
                    owc.setCompareEnd(true);
                    owc.setWordCi(tempCi);
                } else if(owc.getWord().startsWith(tempText)){
                    owc.setTempWord(tempText);
                } else {
                    // 연속된 문자열이 아닌경우 초기화
                    owc.setCompareStart(false);
                    owc.setTempIdx(0);
                    owc.setTempWord("");
                }
            }
        }
    }

    /**
     * X좌표가 동일한지 확인, 보정범위 포함
     * 
     * @param orgCi - 비교 좌표
     * @param targetCi - 대상 좌표
     * @param correctRange - 좌표 보정 범위
     * @return boolean
     * @throws Exception
     */
    public static boolean rangeXMatched(Map<String,Long> orgCi, Map<String,Long> targetCi
        , int correctRange) throws Exception{
        boolean result = false;
        int orgx1 = orgCi.get("x1").intValue();
        int orgx2 = orgCi.get("x2").intValue();
        int targetx1 = targetCi.get("x1").intValue();
        int targetx2 = targetCi.get("x2").intValue();
        if(orgx1-correctRange <= targetx1 && orgx1+correctRange >= targetx1
            && orgx2-correctRange <= targetx2 && orgx2+correctRange >= targetx2){
                result = true;
        }

        return result;
    }

    /**
     * X1좌표가 동일한지 확인, 보정범위 포함
     * 
     * @param orgCi - 비교 좌표
     * @param targetCi - 대상 좌표
     * @param correctRange - 좌표 보정 범위
     * @return boolean
     * @throws Exception
     */
    public static boolean rangeXStartMatched(Map<String,Long> orgCi, Map<String,Long> targetCi
        , int correctRange) throws Exception{
        boolean result = false;
        int orgx1 = orgCi.get("x1").intValue();
        int targetx1 = targetCi.get("x1").intValue();
        if(orgx1-correctRange <= targetx1 && orgx1+correctRange >= targetx1){
                result = true;
        }

        return result;
    }

    /**
     * Y좌표가 동일한지 확인, 보정범위 포함
     * 
     * @param orgCi - 비교 좌표
     * @param targetCi - 대상 좌표
     * @param correctRange - 좌표 보정 범위
     * @return boolean
     * @throws Exception
     */
    public static boolean rangeYMatched(Map<String,Long> orgCi, Map<String,Long> targetCi
        , int correctRange) throws Exception{
        boolean result = false;
        int orgy1 = orgCi.get("y1").intValue();
        int orgy4 = orgCi.get("y4").intValue();
        int targety1 = targetCi.get("y1").intValue();
        int targety4 = targetCi.get("y4").intValue();
        if(orgy1-correctRange <= targety1 && orgy1+correctRange >= targety1
            && orgy4-correctRange <= targety4 && orgy4+correctRange >= targety4){
                result = true;
        }

        return result;
    }
}
