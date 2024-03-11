package com.example.demo.ocr.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.example.demo.common.api.ApiException;
import com.example.demo.common.api.ApiResponse;
// import com.example.demo.ocr.service.OcrService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/ocrApi")
public class OcrApiController {

    // @Autowired
	// private OcrService ocrService;

    /** 
     * @param mReq
     * @return ResponseEntity<Object>
     * @throws Exception
     * @see 자동심사 처리 API
     */
    @PostMapping("/autoAudit")
    public ResponseEntity<Object> ocrUpload(MultipartHttpServletRequest mReq) throws ApiException{
        String fileDir = "D://upload";
        try {
            log.debug("#####mReq.getHeader() = " + mReq.getHeader("X-FORWARDED-FOR"));
            log.debug("#####mReq.getRemoteAddr() = " + mReq.getRemoteAddr());
            Map<String,MultipartFile> files = mReq.getFileMap();
            Enumeration<String> params = mReq.getParameterNames();

            Iterator<Entry<String,MultipartFile>> itr = files.entrySet().iterator();
            MultipartFile file;

            while(itr.hasNext()) {
                Entry<String,MultipartFile> entry = itr.next();
                file = entry.getValue();
                log.debug("#####getName = " + file.getName());
                log.debug("#####getOriginalFilename = " + file.getOriginalFilename());
                Path directory = Paths.get(fileDir).toAbsolutePath().normalize();

                // directory 해당 경로까지 디렉토리를 모두 만든다.
                Files.createDirectories(directory);
                
                // 파일명을 바르게 수정한다.
                String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            
                // 파일명에 '..' 문자가 들어 있다면 오류를 발생하고 아니라면 진행(해킹및 오류방지)
                Assert.state(!fileName.contains(".."), "Name of file cannot contain '..'");
                // 파일을 저장할 경로를 Path 객체로 받는다.
                Path targetPath = directory.resolve(fileName).normalize();
            
                // 파일이 이미 존재하는지 확인하여 존재한다면 오류를 발생하고 없다면 저장한다.
                if(!Files.exists(targetPath)){
                    Assert.state(!Files.exists(targetPath), fileName + " File alerdy exists.");
                    file.transferTo(targetPath);
                }
                // File savefile = ocrService.multipartFileConvert(file);
            }

            while(params.hasMoreElements()) {
                String param = params.nextElement();
                log.debug("#####param = " + param);
                log.debug("#####param value = " + mReq.getParameter(param));
            }
            return ApiResponse.success();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException(e.getLocalizedMessage(), e);
        }
    }
}
