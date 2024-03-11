package com.example.demo.common;

import org.springframework.stereotype.Component;

@Component
public class StringUtils {

    final static String[] sidoArray = {"서울","인천","대전","세종","부산","대구","울산","광주","경기","강원도","충청","경상","전라","제주"};

    /**
     * 텍스트에 시도 존재 여부 확인   
     * 
     * @param String
     * @return Boolean
     */
    public static Boolean sidoContains(String text){
        for(String sido : sidoArray){
            if(text.contains(sido)){
                return true;
            }
        }
        return false;
    }

    /**
     * 텍스트를 시도로 시작하는지 확인 
     * 
     * @param String
     * @return Boolean
     */
    public static Boolean sidoStartsWith(String text){
        for(String sido : sidoArray){
            if(text.startsWith(sido)){
                return true;
            }
        }
        return false;
    }

    /**
     * 주소라인 시도 앞에 있는 문자 제외 처리
     * 
     * @param String
     * @return String
     */
    public static String removeSidoPostText(String text){
        for(String sido : sidoArray){
            if(text.contains(sido)){

                // 시도 앞에 텍스트 가 존재하여 제거 처리
                int gkIndex = text.indexOf(sido);
                text = text.substring(gkIndex);
            }
        }
        return text;
    }

    /**
     * 타켓 문자열에 해당 문자열이 모두 존재하는지 확인
     * 
     * @param String
     * @param String
     * @return boolean
     */
    public static boolean containsChar(String text, String target){
        for(int i=0; i<text.length(); i++){ 
            String charStr = Character.toString(text.charAt(i));
            if(!target.contains(charStr)){
                return false;
            }
        }
        return true;
    }

    /**
     * 주민번호에서 생일 추출
     * 
     * @param String 
     * @return String
     */
    public static String convertJmToBirth(String juminRegNo){
        String birth = juminRegNo;
        String[] jms = juminRegNo.split("-");
        if(jms.length == 2){
            String year1 = "19";
            String gender = jms[1].substring(0,1);
            if("0".equals(gender) || "9".equals(gender)){
                year1 = "18";
            } else if("3".equals(gender) || "4".equals(gender) 
                || "7".equals(gender) || "8".equals(gender)){
                year1 = "20";
            }
            String year2 = jms[0].substring(0, 2);
            String month = jms[0].substring(2, 4);
            String day = jms[0].substring(4, 6);
            birth = year1 + year2 + "년 " + month + "월 " + day + "일";
        }
        return birth;
    }
}
