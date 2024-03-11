package com.example.demo.ocr.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;

import com.example.demo.config.Constant;

import lombok.extern.slf4j.Slf4j;

/**
 * JSON 파일 파싱 
 * @version 1.0
 * @author lcg
 */
@Slf4j
@Service
public class OcrJsonParserService {

    // 초본 ROW 데이터
    public List<String> getJsonRowList(String fileType, String fileName) throws IOException, ParseException {
        List<String> textList = new ArrayList<>();
        String fileDir = Constant.JSON_DIR_ROOT+File.separator+fileType;
        String filePath = fileDir+File.separator+fileName;
        File jsonFile = new File(filePath);
        String jsonStr = "";
        if(jsonFile.exists()){
            log.debug("File exists!");
            Charset cs = StandardCharsets.UTF_8;
            Path path = Paths.get(filePath);
            List<String> list = Files.readAllLines(path,cs);
            for(String readLine : list){
                jsonStr += readLine;
            }
        } else {
            log.debug("File not exists!");
        }

        if(!jsonStr.isEmpty()){
            JSONParser jsonParser = new JSONParser();
            Object obj = jsonParser.parse(jsonStr);
            JSONObject jsonObj = (JSONObject) obj;
            JSONArray images = (JSONArray)jsonObj.get("images");
            String lineText = "";
            for(int i = 0; i < images.size(); i++){
                JSONObject image = (JSONObject)images.get(i);
                JSONArray fields = (JSONArray)image.get("fields");
                for(int ii = 0; ii < fields.size(); ii++){
                    JSONObject field = (JSONObject)fields.get(ii);
                    boolean lineBreak = (boolean)field.get("lineBreak");
                    lineText += (String)field.get("inferText") + " ";
                    if(lineBreak){
                        textList.add(lineText);
                        lineText = "";
                    } 
                }
            }
        }
        return textList;
    }

    /**
     * @param fileName
     * @return
     * @throws IOException
     * @throws ParseException
     * @see JSON 데이터 추출 텍스트 가져오기
     */
    public JSONArray convertFileToJson(String fileType, String fileName) throws IOException, ParseException{
        // JSON 파일 정보
        String fileDir = Constant.JSON_DIR_ROOT+File.separator+fileType;
        String filePath = fileDir+File.separator+fileName;
        File jsonFile = new File(filePath);
        String jsonStr = "";
        JSONArray images = null;
        // 파일존재 여부 확인
        if(jsonFile.exists()){
            log.debug("File exists! = " + fileName);
            Charset cs = StandardCharsets.UTF_8;
            Path path = Paths.get(filePath);
            List<String> list = Files.readAllLines(path,cs);
            for(String readLine : list){
                jsonStr += readLine;
            }
        } else {
            log.debug("File not exists! = " + fileName);
        }

        if(!jsonStr.isEmpty()){
            JSONParser jsonParser = new JSONParser();
            Object obj = jsonParser.parse(jsonStr);
            JSONObject jsonObj = (JSONObject) obj;
            // 추출 이미지 리스트
            images = (JSONArray)jsonObj.get("images");
            // // images 리턴값이 단일일 경우 처리
            // if(images != null && images.size() > 0){
            //     for(int i=0; i < images.size(); i++){
            //         JSONObject image = (JSONObject)images.get(i);
            //         JSONArray fields = (JSONArray)image.get("fields");
            //         jsonArray.addAll(fields);
            //     }
            // }
        }

        return images;
    }
}
