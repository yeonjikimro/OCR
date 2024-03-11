package com.example.demo.ocr.service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.example.demo.common.OcrUtils;
import com.example.demo.common.StringUtils;
import com.example.demo.config.Constant;
import com.example.demo.ocr.vo.OcrCiVo;
import com.example.demo.ocr.vo.OcrJuminDeungbonVo;
import com.example.demo.ocr.vo.OcrPageVo;
import com.example.demo.ocr.vo.OcrWordCiVo;

import lombok.extern.slf4j.Slf4j;

/**
 * 주민등록등본 OCR
 * @version 1.0
 * @author lcg
 */
@Slf4j
@Service
public class OcrJuminDeungbonService {

    @Autowired
	private OcrService ocrService;

    @Autowired
	private OcrJsonParserService ocrJsonParserService;

    // 표준 날짜 형식
    final String dateFormat = "yyyy-MM-dd";
    final SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);

    // 좌표지정 텍스트
    final String APPLICANT_TXT = "신청인:";
    final String USE_PURPOSE_TXT = "용도및목적:";
    final String CHANGE_RESON_TXT = "변동사유";
    final String NOW_ADDRESS_TXT = "현주소:";
    final String NUMBER_TXT = "번호";
    final String JUMIN_REGNO_TXT = "주민등록번호";
    final String BELOW_MARGIN_TXT = "이하여백";

    /**
     * 등본 추출 데이터 - 발급일, 성명, 주민번호, 현주소, 구성원   
     * 
     * @param String
     * @return Map<String,Long>
     * @throws Exception
     */
    public Map<String,Object> getDeungbonData(String fileName) throws Exception {
        Map<String,Object> resultData = new HashMap<String,Object>();
        return resultData;
    }

    /**
     * 등본 추출 데이터 - 발급일, 성명, 주민번호, 현주소, 구성원   
     * 
     * @param String
     * @return Map<String,Long>
     * @throws Exception
     */
    public Map<String,Object> getDeungbonCiData(String fileName) throws Exception {
        Map<String,Object> resultData = new HashMap<String,Object>();
        // JSON 파일에서 OCR 추출 테이터 가져오기
        JSONArray images = ocrJsonParserService.convertFileToJson(Constant.JUMIN_DEUNGBON, fileName);
        // 주민등록초본 추출 데이터 - file, orgFileName, fileExt 셋팅
        OcrJuminDeungbonVo ojd = new OcrJuminDeungbonVo(ocrService.getOcrFile(Constant.JUMIN_DEUNGBON, fileName));

        List<OcrCiVo> boxList = new ArrayList<>();
        
        //'용도및목적' 좌표
        OcrWordCiVo uptCi = new OcrWordCiVo(USE_PURPOSE_TXT);

        //주소 > '변동사유' 좌표
        OcrWordCiVo acrCi = new OcrWordCiVo(CHANGE_RESON_TXT);

        //'현주소:' 좌표
        OcrWordCiVo nadCi = new OcrWordCiVo(NOW_ADDRESS_TXT);

        // 세대원 '번호' 좌표
        OcrWordCiVo numCi = new OcrWordCiVo(NUMBER_TXT);

        //'주민등록번호' 좌표
        OcrWordCiVo jrnCi = new OcrWordCiVo(JUMIN_REGNO_TXT);

        //'이하여백' 좌표
        OcrWordCiVo bmnCi = new OcrWordCiVo(BELOW_MARGIN_TXT);

        //신청인 추출
        OcrCiVo applicantCi = new OcrCiVo();

        //발행일 추출 임시
        String tempIssueDt = "";
        OcrCiVo tempIssueDtCi = new OcrCiVo();

        //현주소 추출 임시
        String tempAddr = "";
        OcrCiVo tempAddrCi = new OcrCiVo();
        int tempAddrYci = 0;

        // 세대 구성원 
        Map<String,String> familyTemp = new HashMap<String,String>();
        OcrCiVo tempFamilyCi = new OcrCiVo();
        boolean isIssueNm = false; // 신청인과 세대원 동일 여부
        boolean familyAllYn = true; //세대구성원 전체 출력 여부 true : 신청자만, false : 가족전체

        // 등본 PDF 첫장 만 처리 
        JSONObject image = (JSONObject)images.get(0);
        JSONArray fields = (JSONArray)image.get("fields");
        OcrPageVo opv = new OcrPageVo();

        // 페이지 사이즈 셋팅
        JSONObject convertedImageInfo = (JSONObject)image.get("convertedImageInfo");
        Long width = (Long)convertedImageInfo.get("width");
        Long height = (Long)convertedImageInfo.get("height");
        opv.setFileWidth(width.intValue());
        opv.setFileHeight(height.intValue());

        // OCR 추축 텍스트 분류  
        for(int i = 0; i < fields.size(); i++){
            JSONObject field = (JSONObject)fields.get(i);
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
            // boolean lineBreak = (boolean)field.get("lineBreak");
            // 현재 문자 좌표
            OcrCiVo currentCi = OcrUtils.getCoordinate(boundingPoly);


            // 기준 좌표 지정 START

            // '용도및목적' 좌표 정의
            if(!uptCi.isCompareEnd()){
                OcrUtils.findWordCi(uptCi, inferText, currentCi);
            }

            // 주소 > 변동사유 좌표 정의
            if(!acrCi.isCompareEnd()){
                OcrUtils.findWordCi(acrCi, inferText, currentCi);
            }
            // 현주소: 좌표 정의
            if(!nadCi.isCompareEnd()){
                OcrUtils.findWordCi(nadCi, inferText, currentCi);
            }
            
            // 번호 좌표 정의
            if(!numCi.isCompareEnd() && currentCi.getX1() >= nadCi.getWordCi().getX1() 
                && currentCi.getX2() <= nadCi.getWordCi().getX2() ){
                OcrUtils.findWordCi(numCi, inferText, currentCi);
            }

            // 주민등록번호 좌표 정의
            if(!jrnCi.isCompareEnd()){
                OcrUtils.findWordCi(jrnCi, inferText, currentCi);
            }

            // 이하여백 좌표 정의
            if(!bmnCi.isCompareEnd()){
                OcrUtils.findWordCi(bmnCi, inferText, currentCi);
            }
            // log.debug("#####acrCi.isCompareEnd() = " + acrCi.isCompareEnd());
            // log.debug("#####nadCi.isCompareEnd() = " + nadCi.isCompareEnd());
            // log.debug("#####numCi.isCompareEnd() = " + numCi.isCompareEnd());
            // log.debug("#####jrnCi.isCompareEnd() = " + jrnCi.isCompareEnd());
            // log.debug("#####bmnCi.isCompareEnd() = " + bmnCi.isCompareEnd());
            // log.debug("#####inferText = " + inferText);
            // 기준 좌표 지정 END
            
            //신청인
            if(ojd.getIssueNm().isEmpty()){
                if(applicantCi.getX1() > 0){
                    ojd.setIssueNm(inferText);
                    applicantCi.setX2(currentCi.getX2());
                    applicantCi.setX3(currentCi.getX3());
                    boxList.add(applicantCi);
                    applicantCi = new OcrCiVo();
                } else if(APPLICANT_TXT.equals(inferText)){
                    applicantCi = currentCi;
                } else if(inferText.contains(APPLICANT_TXT)){
                    // 신청인:이름 형식
                    String[] t1 = inferText.split(":");
                    if(t1.length == 2){
                        ojd.setIssueNm(t1[1].trim());
                        boxList.add(currentCi);
                    }
                }
            }

            // 발행일 
            // 발행일이 없고 용도및목적 ci 확인
            if(ojd.getIssueDt().isEmpty() && uptCi.isCompareEnd()){
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
                    ojd.setIssueDt(issueDtMatchYMD.group());
                    boxList.add(tempIssueDtCi);
                }
            }

            // 현주소
            // 주소 데이터가 없고, 변동사유 좌표가 있고, 변동사유 x1 좌표보다 앞인 좌표표
            if(ojd.getAddress().isEmpty() && acrCi.isCompareEnd() 
                && currentCi.getX2() < acrCi.getWordCi().getX1()){

                // 시도 주소 시작 여부 확인, '현주소:' 좌표보다 이후
                if(tempAddrYci > 0 && currentCi.getX1() > nadCi.getWordCi().getX2()){
                    if(currentCi.getY1() > tempAddrYci){
                        ojd.setAddress(tempAddr);
                        boxList.add(tempAddrCi);
                    } else {
                        tempAddr += " " + inferText;
                        tempAddrCi.setX2(currentCi.getX2());
                        tempAddrCi.setY2(currentCi.getY2());
                        tempAddrCi.setX3(currentCi.getX3());
                        tempAddrCi.setY3(currentCi.getY3());
                        tempAddrCi.setX4(currentCi.getX4());
                        tempAddrCi.setY4(currentCi.getY4());
                    }
                }

                // 주소 시작
                if(StringUtils.sidoStartsWith(inferText)){
                    tempAddr = inferText;
                    tempAddrCi = currentCi;
                    tempAddrYci = currentCi.getY4() + (currentCi.getY4() - currentCi.getY1()) + 5;
                } 
            }

            // 세대 구성원 - 세대주관계, 성명, 주민등록번호 추출
            // '주민등록번호' 위치추출, '주민등록번호' 아래, '주민등록번호' X 좌표 이전, '번호' X좌표 이후, '이하여백' 이전
            if(jrnCi.isCompareEnd() && currentCi.getY1() > jrnCi.getWordCi().getY4()
                && currentCi.getX1() < jrnCi.getWordCi().getX2()
                && currentCi.getX1() > numCi.getWordCi().getX2()
                && !bmnCi.isCompareEnd()
                ){

                if(currentCi.getX2() < jrnCi.getWordCi().getX1()){
                    // 관계
                    familyTemp.put("relation", inferText.trim());
                    // 관계 ci로 세대구성원 box 앞영역을 정한다.
                    // 관계 ci가 기존보다 앞쪽이면 변경
                    if(ojd.getFamilyList().isEmpty() || tempFamilyCi.getX1() > currentCi.getX1()){
                        tempFamilyCi.setX1(currentCi.getX1());
                        tempFamilyCi.setX4(currentCi.getX4());
                    }
                } else {
                    // 주민번호
                    boolean isJumin = false;
                    Matcher juminMatch1 = Pattern.compile(OcrUtils.juminNoRegEx1).matcher(inferText);
                    if(juminMatch1.find()){
                        isJumin = true;
                        familyTemp.put("juminRegNo", inferText.trim());
                    }
                    Matcher juminMatch2 = Pattern.compile(OcrUtils.juminNoRegEx2).matcher(inferText);
                    if(juminMatch2.find()){
                        isJumin = true;
                        familyTemp.put("juminRegNo", inferText.trim());
                    }

                    if(isJumin){
                        // 세대구성원 박스 X2 좌표가 이전보다 크면 변경
                        if(tempFamilyCi.getX2() < currentCi.getX2()){
                            tempFamilyCi.setX2(currentCi.getX2());
                            tempFamilyCi.setX3(currentCi.getX3());
                        }
                        // 마지막 주민번호 위치로 아래 Y좌표 변경
                        tempFamilyCi.setY3(currentCi.getY3());
                        tempFamilyCi.setY4(currentCi.getY4());
                        if(isIssueNm){
                            ojd.setJuminRegNo(inferText.trim());
                            isIssueNm = false;
                        }
                    } else {
                        // 이름
                        if(tempFamilyCi.getY1() == 0){
                            // 최초 구성원 이름
                            tempFamilyCi.setY1(currentCi.getY1());
                            tempFamilyCi.setY2(currentCi.getY2());
                        }
                        familyTemp.put("name", inferText.trim());

                        // 이름이 신청자와 동일할 경우 다음 주민등록번호 정보 셋팅
                        if(inferText.trim().equals(ojd.getIssueNm())){
                            isIssueNm = true;
                        }
                    }

                    // 이름, 관계, 주민등록 번호가 모두 존재하여야 추가 처리
                    if(familyTemp.containsKey("name") && familyTemp.containsKey("relation") 
                        && familyTemp.containsKey("juminRegNo") && (familyAllYn || isIssueNm)){
                        ojd.getFamilyList().add(familyTemp);
                        familyTemp = new HashMap<String,String>();
                    }
                }
            }
        }

        // 사본생성 box 처리
        // 세대 구성원 박스
        boxList.add(tempFamilyCi);
        opv.setBoxList(boxList);
        ojd.setOcrPage(opv);
        
        if("PDF".equals(ojd.getFileExt())){
            createBoxPdfFile(ojd);
        } else {
            createBoxImageFile(ojd);
        }

        resultData.put("issueDt", ojd.getIssueDt()); 
        resultData.put("issueNm", ojd.getIssueNm()); 
        resultData.put("issuejuminResNo", ojd.getJuminRegNo()); 
        resultData.put("birth", StringUtils.convertJmToBirth(ojd.getJuminRegNo())); 
        resultData.put("address", ojd.getAddress()); 
        resultData.put("familyList", ojd.getFamilyList()); 

        return resultData;
    }

    /**
     * 이미지 파일 추출영역 box 처리   
     * 
     * @param String
     * @return Map<String,Long>
     * @throws Exception
     */
    public void createBoxImageFile(OcrJuminDeungbonVo ojd) throws IOException{
        String newFileDir = Constant.JSON_DIR_ROOT+File.separator+Constant.JUMIN_DEUNGBON+"_copy";
        BufferedImage image = ImageIO.read(ojd.getFile());
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND,0));
        g2d.setFont(new Font("NanumGothic", Font.PLAIN, 30));
        int boxCorrection = 2;
        for(OcrCiVo boxCi : ojd.getOcrPage().getBoxList()){
            g2d.drawRect(boxCi.getX1() - boxCorrection, boxCi.getY1() - boxCorrection
                , (boxCi.getX2() - boxCi.getX1() + (boxCorrection * 2))
                , (boxCi.getY4() - boxCi.getY1() + (boxCorrection * 2)));
            if(!boxCi.getText().isEmpty()){
                g2d.drawString(boxCi.getText(), boxCi.getX2() + 5, boxCi.getY2()); // 문자열 삽입
            }
        }
        g2d.dispose();
        ImageIO.write(image, ojd.getFileExt(), new File(newFileDir+File.separator+ojd.getFile().getName()));
    }

     /**
     * pdf 파일 추출영역 box 처리   
     * 
     * @param OcrJuminChobonVo
     * @return Map<String,Long>
     * @throws Exception
     */
    public void createBoxPdfFile(OcrJuminDeungbonVo ojd) throws IOException{
        String newFileDir = Constant.JSON_DIR_ROOT+File.separator+Constant.JUMIN_DEUNGBON+"_copy";

        PDDocument document = PDDocument.load(ojd.getFile()); 

        // 폰트 설정
        ClassPathResource resource = new ClassPathResource("\\static\\font\\NanumGothic.ttf");
        PDType0Font font = PDType0Font.load(document, resource.getFile());

        PDPage page = document.getPage(0);
        OcrPageVo opv = ojd.getOcrPage();

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
                contentStream.newLineAtOffset(boxCi.getX2()
                    , opv.getFileHeight() - boxCi.getY2());
                contentStream.showText(boxCi.getText());
                contentStream.endText();
            }
        }    
        
        contentStream.close();

        document.save(new File(newFileDir+File.separator+ojd.getFileName()));
        document.close();
    }
}
