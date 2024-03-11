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

@Slf4j
@Service
public class OcrBankbookService {

    
    /** 
     * @param fileName
     * @return List<String>
     * @throws IOException
     * @throws ParseException
     * @see 초본 ROW 데이터
     */
    public List<String> getJsonRowList(String fileName) throws IOException, ParseException {
        List<String> textList = new ArrayList<>();
        String fileDir = Constant.JSON_DIR_ROOT+File.separator+Constant.BANKBOOK;
        String filePath = fileDir+File.separator+fileName;
        File jsonFile = new File(filePath);
        String jsonStr = "";
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
}
