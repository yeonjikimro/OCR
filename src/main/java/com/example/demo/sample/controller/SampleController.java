package com.example.demo.sample.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.sample.service.SampleService;

@Controller
@RequestMapping("/sample")
public class SampleController {

    @Autowired
	private SampleService sampleService;

    @GetMapping("/main")
    public String sampleMain(){
        return "sample/main";
    }

    @GetMapping("/simMetrics")
    public String simMetrics(){
        return "sample/simMetrics";
    }

    @ResponseBody
    @GetMapping("/sim/matchString")
    public String matchString(@RequestParam("targetStr1") String targetStr1 
        ,   @RequestParam("targetStr2") String targetStr2) throws Exception{

        String simmetricsPoint = sampleService.matchStringSimmetrics(targetStr1, targetStr2);
        String commontextPoint = sampleService.matchStringCommontext(targetStr1, targetStr2);
            
        return "simmetricsPoint = " + simmetricsPoint + " / commontextPoint = " + commontextPoint;
    }
}
