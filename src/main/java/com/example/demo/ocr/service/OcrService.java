package com.example.demo.ocr.service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.example.demo.config.Constant;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OcrService {

    // 김태현_GENERAL
    // final String API_ROOT_URL = "https://ndy7q877yp.apigw.ntruss.com/custom/v1/22891/4142ebf34ae5a813dba0bde3ebd8701dbd2a6072a7f60bef1fb543828b099813";
    // final String SECRET_KEY = "ZXRNQWZ1TFRmR3B0VUV4Rk9vYmlGWHNrcEhMYmFxQk0=";
    // 이창균_GENERAL
    final String API_ROOT_URL = "https://mb43ydn8qz.apigw.ntruss.com/custom/v1/24105/9a211fa4af2d0139f8178949bc75c1864475ac884270b0e8118e9bee60c6ebe3";
    final String SECRET_KEY = "TWhxWGZJRnpaTU9xcmtPUGZQQml0dE9pbGZnc1lxdGE=";

    // 네이버 클로바 연동 FILE > JSON String > general
    public String ocrGeneralUpload(MultipartHttpServletRequest mReq, String fileType) throws Exception{
        log.debug("#####OcrService > ocrGeneralUpload START");
		String apiURL = API_ROOT_URL + "/general";
        String resResult = "";
        try {
            Map<String, MultipartFile> files = mReq.getFileMap();
         
            Iterator<Entry<String, MultipartFile>> itr = files.entrySet().iterator();
    
            MultipartFile mFile;
    
            while(itr.hasNext()){
                Entry<String, MultipartFile> entry = itr.next();
                mFile = entry.getValue();
                String orgFileName = mFile.getOriginalFilename();
                if(orgFileName != null){
                    int lastIndex =  orgFileName.lastIndexOf(".");
                    String fileName = orgFileName.substring(0, lastIndex);
                    String ext = orgFileName.substring(lastIndex + 1);
                    log.debug("orgFileName = " + orgFileName);
                    log.debug("fileName = " + fileName);
                    log.debug("ext = " + ext);

                    URL url = new URL(apiURL);
                    HttpURLConnection con = (HttpURLConnection)url.openConnection();
                    con.setUseCaches(false);
                    con.setDoInput(true);
                    con.setDoOutput(true);
                    con.setReadTimeout(30000);
                    con.setRequestMethod("POST");
                    String boundary = "----" + UUID.randomUUID().toString().replaceAll("-", "");
                    con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                    con.setRequestProperty("X-OCR-SECRET", SECRET_KEY);
        
                    JSONObject json = new JSONObject();
                    json.put("version", "V2");
                    json.put("requestId", UUID.randomUUID().toString());
                    json.put("timestamp", System.currentTimeMillis());
                    json.put("lang","ko");
                    JSONObject image = new JSONObject();
                    image.put("format", ext);
                    image.put("name", fileName);
                    JSONArray images = new JSONArray();
                    images.put(image);
                    json.put("images", images);
                    // json.put("enableTableDetection", true); // 표추출 여부 옵션
                    
                    String postParams = json.toString();
        
                    con.connect();
                    DataOutputStream dos = new DataOutputStream(con.getOutputStream());
                    // long start = System.currentTimeMillis();
                    File file = multipartFileConvert(mFile);
                    writeMultiPart(dos, postParams, file, boundary);
                    dos.close();
                    int responseCode = con.getResponseCode();
                    BufferedReader br;
                    log.debug("#####OcrService > ocrGeneralUpload responseCode = " + responseCode);
                    if (responseCode == 200) {
                        br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
                    } else {
                        br = new BufferedReader(new InputStreamReader(con.getErrorStream(), "UTF-8"));
                    }
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    while ((inputLine = br.readLine()) != null) {
                        response.append(inputLine);
                    }
                    br.close();
                    resResult = response.toString();
                    // file.delete();
                    createJsonFile(fileType, fileName+".json", resResult);
                } else {
                    log.debug("ocrGeneralUpload File null! orgFileName = " + orgFileName);
                }
            } 
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
   
        return resResult;
    }

    // 네이버 클로바 연동 FILE > JSON String > Template
    public String ocrTemplateUpload(MultipartHttpServletRequest mReq, String fileType) throws Exception{
        log.debug("#####OcrService > ocrTemplateUpload START");
		String apiURL = API_ROOT_URL + "/infer";
        String resResult = "";
        try {
            Map<String, MultipartFile> files = mReq.getFileMap();
         
            Iterator<Entry<String, MultipartFile>> itr = files.entrySet().iterator();
    
            MultipartFile mFile;
    
            while(itr.hasNext()){
                Entry<String, MultipartFile> entry = itr.next();
                mFile = entry.getValue();
                String orgFileName = mFile.getOriginalFilename();
                if(orgFileName != null){
                    int lastIndex =  orgFileName.lastIndexOf(".");
                    String fileName = orgFileName.substring(0, lastIndex);
                    String ext = orgFileName.substring(lastIndex + 1);
                    log.debug("orgFileName = " + orgFileName);
                    log.debug("fileName = " + fileName);
                    log.debug("ext = " + ext);

                    URL url = new URL(apiURL);
                    HttpURLConnection con = (HttpURLConnection)url.openConnection();
                    con.setUseCaches(false);
                    con.setDoInput(true);
                    con.setDoOutput(true);
                    con.setReadTimeout(30000);
                    con.setRequestMethod("POST");
                    String boundary = "----" + UUID.randomUUID().toString().replaceAll("-", "");
                    con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                    con.setRequestProperty("X-OCR-SECRET", SECRET_KEY);
        
                    JSONObject json = new JSONObject();
                    json.put("version", "V1");
                    json.put("requestId", UUID.randomUUID().toString());
                    json.put("timestamp", System.currentTimeMillis());
                    json.put("lang","ko");
                    JSONObject image = new JSONObject();
                    image.put("format", ext);
                    image.put("name", fileName);
                    int[] templateIds = {25339};
                    json.put("templateIds", templateIds);
                    JSONArray images = new JSONArray();
                    images.put(image);
                    json.put("images", images);
                    String postParams = json.toString();
        
                    con.connect();
                    DataOutputStream dos = new DataOutputStream(con.getOutputStream());
                    // long start = System.currentTimeMillis();
                    File file = multipartFileConvert(mFile);
                    writeMultiPart(dos, postParams, file, boundary);
                    dos.close();
                    int responseCode = con.getResponseCode();
                    BufferedReader br;
                    log.debug("#####OcrService > ocrTemplateUpload responseCode = " + responseCode);
                    if (responseCode == 200) {
                        br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
                    } else {
                        br = new BufferedReader(new InputStreamReader(con.getErrorStream(), "UTF-8"));
                    }
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    while ((inputLine = br.readLine()) != null) {
                        response.append(inputLine);
                    }
                    br.close();
                    resResult = response.toString();
                    file.delete();
                    createJsonFile(fileType, fileName+".json", resResult);
                } else {
                    log.debug("ocrTemplateUpload File null! orgFileName = " + orgFileName);
                }
            } 
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
   
        return resResult;
    }

    // 네이버 클로바 연동 FILE > JSON String > /document/receipt
    public String ocrDocReceiptUpload(MultipartHttpServletRequest mReq, String fileType) throws Exception{
        log.debug("#####OcrService > ocrDocReceiptUpload START");
		String apiURL = API_ROOT_URL + "/document/receipt";
        String resResult = "";
        try {
            Map<String, MultipartFile> files = mReq.getFileMap();
         
            Iterator<Entry<String, MultipartFile>> itr = files.entrySet().iterator();
    
            MultipartFile mFile;
    
            while(itr.hasNext()){
                Entry<String, MultipartFile> entry = itr.next();
                mFile = entry.getValue();
                String orgFileName = mFile.getOriginalFilename();
                if(orgFileName != null){
                    int lastIndex =  orgFileName.lastIndexOf(".");
                    String fileName = orgFileName.substring(0, lastIndex);
                    String ext = orgFileName.substring(lastIndex + 1);
                    log.debug("orgFileName = " + orgFileName);
                    log.debug("fileName = " + fileName);
                    log.debug("ext = " + ext);

                    URL url = new URL(apiURL);
                    HttpURLConnection con = (HttpURLConnection)url.openConnection();
                    con.setUseCaches(false);
                    con.setDoInput(true);
                    con.setDoOutput(true);
                    con.setReadTimeout(30000);
                    con.setRequestMethod("POST");
                    String boundary = "----" + UUID.randomUUID().toString().replaceAll("-", "");
                    con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                    con.setRequestProperty("X-OCR-SECRET", SECRET_KEY);
        
                    JSONObject json = new JSONObject();
                    json.put("version", "V2");
                    json.put("requestId", UUID.randomUUID().toString());
                    json.put("timestamp", System.currentTimeMillis());
                    JSONObject image = new JSONObject();
                    image.put("format", ext);
                    image.put("name", fileName);
                    JSONArray images = new JSONArray();
                    images.put(image);
                    json.put("images", images);
                    String postParams = json.toString();
        
                    con.connect();
                    DataOutputStream dos = new DataOutputStream(con.getOutputStream());
                    File file = multipartFileConvert(mFile);
                    writeMultiPart(dos, postParams, file, boundary);
                    dos.close();
                    int responseCode = con.getResponseCode();
                    BufferedReader br;
                    log.debug("#####OcrService > ocrDocReceiptUpload responseCode = " + responseCode);
                    if (responseCode == 200) {
                        br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
                    } else {
                        br = new BufferedReader(new InputStreamReader(con.getErrorStream(), "UTF-8"));
                    }
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    while ((inputLine = br.readLine()) != null) {
                        response.append(inputLine);
                    }
                    br.close();
                    resResult = response.toString();
                    file.delete();
                    createJsonFile(fileType, fileName+".json", resResult);
                } else {
                    log.debug("ocrDocReceiptUpload File null! orgFileName = " + orgFileName);
                }
            } 
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
   
        return resResult;
    }

	private static void writeMultiPart(OutputStream out, String jsonMessage, File file, String boundary) throws
		IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("--").append(boundary).append("\r\n");
		sb.append("Content-Disposition:form-data; name=\"message\"\r\n\r\n");
		sb.append(jsonMessage);
		sb.append("\r\n");

		out.write(sb.toString().getBytes("UTF-8"));
		out.flush();

		if (file != null && file.isFile()) {
			out.write(("--" + boundary + "\r\n").getBytes("UTF-8"));
			StringBuilder fileString = new StringBuilder();
			fileString
				.append("Content-Disposition:form-data; name=\"file\"; filename=");
			fileString.append("\"" + file.getName() + "\"\r\n");
			fileString.append("Content-Type: application/octet-stream\r\n\r\n");
			out.write(fileString.toString().getBytes("UTF-8"));
			out.flush();

			try (FileInputStream fis = new FileInputStream(file)) {
				byte[] buffer = new byte[8192];
				int count;
				while ((count = fis.read(buffer)) != -1) {
					out.write(buffer, 0, count);
				}
				out.write("\r\n".getBytes());
			}

			out.write(("--" + boundary + "--\r\n").getBytes("UTF-8"));
		}
		out.flush();
	}

    // MultipartFile > File
    public File multipartFileConvert(MultipartFile mf) throws IOException {
        File file = new File(mf.getOriginalFilename());
        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(mf.getBytes());
        fos.close();
        return file;
    }

    public void createJsonFile(String fileType, String fileName, String jsonStr) throws IOException{
        // 파일 구분 별 디렉토리
        String filePath =  Constant.JSON_DIR_ROOT;
        if(fileType != null && !fileType.isEmpty()){
            filePath =  Constant.JSON_DIR_ROOT+File.separator+fileType;
        }

        File file = new File(filePath+File.separator+fileName);
        if(file.exists()){
            log.debug("FILE EXISTS = " + filePath);
            file.delete();
            log.debug("FILE DELETE = " + filePath);
        }
        log.debug("#####OcrService > createJsonFile = " + filePath+File.separator+fileName);
        FileOutputStream fos = new FileOutputStream(filePath+File.separator+fileName, true);
        OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");
        out.write(jsonStr);
        out.close();
    }

    // JSON 파일 리스트
    public List<Map<String,Object>> getJsonList(String fileType) throws IOException {
        List<Map<String,Object>> fileList = new ArrayList<>();
        String fileDir = Constant.JSON_DIR_ROOT+File.separator+fileType;
        File dir = new File(fileDir);
        FilenameFilter filter= new FilenameFilter() {
            public boolean accept(File f, String name) {
                return name.contains("json");
            }
        };
        File files[] = dir.listFiles(filter);
        for (File file : files) {
           Map<String,Object> fm = new HashMap<>();
           fm.put("filePath", file.getPath());
           fm.put("fileName", file.getName());
           fileList.add(fm);
        }
        return fileList;
    }

    // JSON 파일 읽어오기
    public String readJsonFile(String filePath) throws IOException{
        File file = new File(filePath);
        String jsonStr = "";
        log.debug("readJsonFile File filePath = " + filePath);
        if(file.exists()){
            log.debug("readJsonFile File exists!");
            Charset cs = StandardCharsets.UTF_8;
            Path path = Paths.get(filePath);
            List<String> list = Files.readAllLines(path,cs);
            for(String readLine : list){
                jsonStr += readLine;
            }

        } else {
            log.debug("readJsonFile File not exists!");
            jsonStr = "File not exists!";
        }

        return jsonStr;
    }

    // json 파일명으로 ocr 대상파일 가져오기
    public File getOcrFile(String fileType, String fileName){
        String fileDir = Constant.JSON_DIR_ROOT + File.separator + fileType + "_file";

        int lastIndex =  fileName.lastIndexOf(".");
        String fileOnlyName = fileName.substring(0, lastIndex);
        
        File dir = new File(fileDir);
        FilenameFilter filter= new FilenameFilter() {
            public boolean accept(File f, String name) {
                return name.contains(fileOnlyName);
            }
        };
        File files[] = dir.listFiles(filter);
        return files[0];
    }
}
