package com.example.demo.ocr.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.example.demo.config.Constant;
import com.example.demo.config.OcrFileType;
import com.example.demo.ocr.service.OcrHiealcService;
import com.example.demo.ocr.service.OcrJsonParserService;
// import com.example.demo.ocr.service.OcrJuminChobon1Service;
// import com.example.demo.ocr.service.OcrJuminChobon2Service;
// import com.example.demo.ocr.service.OcrJuminDeungbonPdfService;
import com.example.demo.ocr.service.OcrJuminChobonService;
import com.example.demo.ocr.service.OcrJuminDeungbonService;
import com.example.demo.ocr.service.OcrService;

@Controller
@RequestMapping("/ocr")
public class OcrController {

    @Autowired
	private OcrService ocrService;

    @Autowired
	private OcrJsonParserService ocrJsonParserService;

    @Autowired
	private OcrJuminChobonService ocrJuminChobonService;

    @Autowired
	private OcrHiealcService ocrHiealcService;

    @Autowired
    private OcrJuminDeungbonService ocrJuminDeungbonService;
    
    @GetMapping("/main")
    public String ocrMain(){
        return "ocr/main";
    }

    // 초본업로드 페이지
    @GetMapping("/upload")
    public String upload(){
        return "ocr/upload";
    }

    // 업로드 및 JSON리턴
    @PostMapping("/upload")
    public ModelAndView ocrUpload(MultipartHttpServletRequest mReq, @RequestParam("fileType") String fileType) throws Exception{
        ModelAndView mav = new ModelAndView("ocr/jsonData");
        if(Constant.OCR_TEMPLATE.equals(OcrFileType.valueOfOcrType(fileType).getOcrType())){
            mav.addObject("ocrResult", ocrService.ocrTemplateUpload(mReq, fileType));
        // } else if(Constant.OCR_DOCUMENT.equals(OcrFileType.valueOfOcrType(fileType).getOcrType()){
        //     mav.addObject("ocrResult", ocrService.ocrTemplateUpload(mReq, fileType));
        } else { // else general
            mav.addObject("ocrResult", ocrService.ocrGeneralUpload(mReq, fileType));
        }
        return mav;
    }

    // JSON 파일리스트 페이지
    @GetMapping("/jsonList")
    public ModelAndView getJsonList(@RequestParam("fileType") String fileType) throws Exception{
        ModelAndView mav = new ModelAndView("ocr/jsonList");
        mav.addObject("fileType", fileType);
        mav.addObject("jsonList", ocrService.getJsonList(fileType));
        return mav;
    }

    // JSON 데이터 페이지
    @GetMapping("/jsonData")
    public ModelAndView getJsonData(@RequestParam("filePath") String filePath) throws Exception{
        ModelAndView mav = new ModelAndView("ocr/jsonData");
        mav.addObject("ocrResult", ocrService.readJsonFile(filePath));
        return mav;
    }

    // json 파일 row데이터 조회
    @GetMapping("/{fileType}/jsonRowData")
    public ModelAndView getJsonRowData(@PathVariable("fileType") String fileType
        , @RequestParam("fileName") String fileName) throws Exception{
        ModelAndView mav = new ModelAndView("ocr/jsonRowData");
        mav.addObject("fileType",fileType);
        mav.addObject("fileName",fileName);
        List<String> jsonRowList = ocrJsonParserService.getJsonRowList(fileType, fileName);
        mav.addObject("jsonRowList", jsonRowList);
        return mav;
    }

    // 건강보험 자격득실 확인서 추출 데이터 파싱 페이지!
    @GetMapping("/hiealc/parseData")
    public ModelAndView hiealcParseData(@RequestParam("fileName") String fileName) throws Exception{
        ModelAndView mav = new ModelAndView("ocr/hiealc/parseData");
        mav.addObject("fileName",fileName);
        mav.addObject("ocrData", ocrHiealcService.getHiealcData(fileName));
        return mav;
    }

    // 초본 추출 데이터 파싱 페이지
    @GetMapping("/juminChobon/parseData")
    public ModelAndView juminChobonParseData(@RequestParam("fileName") String fileName) throws Exception{
        ModelAndView mav = new ModelAndView("ocr/juminChobon/parseData");
        mav.addObject("fileName",fileName);
        // mav.addObject("ocrData", ocrJuminChobonService.getChoBonData(fileName));
        mav.addObject("ocrData", ocrJuminChobonService.getChoBonCiData(fileName));
        return mav;
    }

    // 등본 추출 데이터 파싱 페이지
    @GetMapping("/juminDeungbon/parseData")
    public ModelAndView juminDeungbonParseData(@RequestParam("fileName") String fileName) throws Exception{
        ModelAndView mav = new ModelAndView("ocr/juminDeungbon/parseData");
        mav.addObject("fileName",fileName);
        mav.addObject("ocrData", ocrJuminDeungbonService.getDeungbonCiData(fileName));
        return mav;
    }
}
