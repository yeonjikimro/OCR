package com.example.demo.sample.service;

import java.util.List;
import java.util.Locale;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.simmetrics.StringMetric;
import org.simmetrics.metrics.CosineSimilarity;
// import org.simmetrics.metrics.StringMetrics;
import org.simmetrics.simplifiers.Simplifiers;
import org.simmetrics.tokenizers.Tokenizers;
import org.springframework.stereotype.Service;

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.co.shineware.nlp.komoran.model.Token;

import static org.simmetrics.builders.StringMetricBuilder.with;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SampleService {

    // Simmetrics 텍스트 유사도 측정
    public String matchStringSimmetrics(String str1, String str2) throws Exception{
        String result = "Error";
        try {
            log.debug("Simmetrics str1 = " + str1);
            log.debug("Simmetrics str2 = " + str2);
            str1 = morpheme(str1);
            str2 = morpheme(str2);
            log.debug("Simmetrics str1 = " + str1);
            log.debug("Simmetrics str2 = " + str2);
            // StringMetric metric = StringMetrics.cosineSimilarity();
            StringMetric metric =
            with(new CosineSimilarity<String>())
            .simplify(Simplifiers.toLowerCase(Locale.ENGLISH))
            .simplify(Simplifiers.replaceNonWord())
            .tokenize(Tokenizers.whitespace())
            .build();

            float resultF = metric.compare(str1, str2);
            log.debug("resultF = " + resultF);
            result =  String.valueOf(resultF);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
   
        return result;
    }

    // Commontext 텍스트 유사도 측정
    public String matchStringCommontext(String str1, String str2) throws Exception{
        String result = "Error";
        try {
            log.debug("Commontext str1 = " + str1);
            log.debug("Commontext str2 = " + str2);
            str1 = morpheme(str1);
            str2 = morpheme(str2);
            log.debug("Commontext str1 = " + str1);
            log.debug("Commontext str2 = " + str2);
            int maxLen = str1.length() > str2.length() ? str1.length() : str2.length();
            
            LevenshteinDistance ld = new LevenshteinDistance();
            
            double temp = ld.apply(str1, str2);
            log.debug("apply temp = " + temp);
            double tempResult = (maxLen - temp) / maxLen; 
            result =  String.valueOf(tempResult);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
   
        return result;
    }

    /**
     * @param orgStr
     * @return
     * @throws Exception
     */
    public String morpheme(String orgStr) throws Exception{
        String compareStr = "";
        try {
            log.debug("orgStr = " + orgStr);
            Komoran komoran = new Komoran(DEFAULT_MODEL.FULL);
            
            KomoranResult analyzeResultList = komoran.analyze(orgStr);
            List<Token> tokenList = analyzeResultList.getTokenList();
            for (Token token : tokenList) {
                log.debug("token.getBeginIndex() = " + token.getBeginIndex());
                log.debug("token.getEndIndex() = " + token.getEndIndex());
                log.debug("token.getMorph() = " + token.getMorph());
                log.debug("token.getPos() = " + token.getPos());
            }
            
            List<String> wordList = komoran.analyze(orgStr).getMorphesByTags("NP","NNP","NNG","MAG");
            for (String word : wordList) {
                compareStr += word+" ";
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return compareStr;    
    }
}
