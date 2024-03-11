package com.example.demo.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import org.springframework.stereotype.Component;

@Component
public class DateUtils {
    // 날짜 간격 일수 구하기
    public static int dateTermDays(String startDt, String endDt, String dateFormat) throws ParseException{
        SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
        Date startDate = dateFormatter.parse(startDt);
        Date endDate = dateFormatter.parse(endDt);
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate); 
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate); 
        Long diffSec = (endCal.getTimeInMillis() - startCal.getTimeInMillis()) / 1000;
        Long diffDays = diffSec / (24*60*60); //일자수 차이
        return diffDays.intValue();
    }

    // 일수를 년월일로 표기
    public static String convertDayToYMD(int day){
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, -1*day);
        LocalDate startDate = cal.getTime().toInstant()   // Date -> Instant
        .atZone(ZoneId.systemDefault())  // Instant -> ZonedDateTime
        .toLocalDate();
        LocalDate currentDate = LocalDate.now();
        Period diff = Period.between(startDate, currentDate);
        return diff.getYears()+"년"+diff.getMonths()+"월"+diff.getDays()+"일";
    }
}
