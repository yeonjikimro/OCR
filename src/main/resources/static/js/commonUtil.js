$(document).ready(function () {
	/**
	 * @설명      : form 내부 값들을 array -> object 형식으로 변경
	 * @param    :
	 * @return   : Object, {brCd: ["01"], bizCd: [], ...}
	 * */
	$.fn.serializeObject = function () {
		// "use strict"
		var result = {};
		var extend = function (i, element) {
			var node = result[element.name];
			if ("undefined" !== typeof node && node !== null) {
				if ($.isArray(node)) {
					node.push(element.value);
				} else {
					result[element.name] = [node, element.value]
				}
			} else {
				result[element.name] = element.value
			}
		};
		$.each(this.serializeArray(), extend);
		return result;
	};

	$.fn.clearForm = function () {
		return this.each(function () {
			var type = this.type;
			var tag  = this.tagName.toLowerCase();

			if (tag === 'form') {
				return $(':input', this).clearForm();
			}
			if (type === 'text' || tag === 'textarea') {
				this.value = '';
			} else if (type === 'checkbox') {
				this.checked = false;
			// } else if (	type === 'radio') {
			// 	this.eq(0).attr("checked", true);
			} else if (tag === 'select') {
				this.selectedIndex = -1;
			}
		});
	};

	/**
	 * @설명      : 검색조건 유효성 검사, POPUP 오픈 후 호출하는게 아닌이상 windowId는 넘겨주지 않아도된다.
	 *             특별한 경우가 아닌 이상 rules, messages, submitHandler에 대해 정의해서 넘겨주면 된다
	 * @param    :
	 *      options  : validate option
	 *      callback : 실행 함수
	 *      label    : error label 처리 (Y)
	 *      		   붙이고 싶은 부분에 복사  <label for="name" class="validation_error_message form_txt"></label>
	 *      alertYn  : error alert 처리 (Y)
	 * @return   :
	 * */
	$.fn.formValidate = function (options, callback, label, alertYn) {
		var defaultOption = {
			ignore        : ":hidden",  //정의된 form hidden input 객체도 유효성체크(default> : hidden)
			focusInvalid  : true,
			onclick       : false,      //클릭시 발생됨 꺼놓음(default > true)
			onfocusout    : false,      //포커스가 아웃되면 발생됨 꺼놓음(default > true)
			onkeyup       : false,      //키보드 키가 올라가면 발생됨 꺼놓음(default > true)
			// 유효성 검사 항목 기입
			rules         : {},
			messages      : {},
			submitHandler : callback,
			errorPlacement: function(error, element) { // 유효성검사 실패 시 label 처리(오류메시지 처리)
				if(label == 'Y') {
					var name = element.attr('name');
					var errorSelector = '.validation_error_message[for="' + name + '"]';
					var $element = $(errorSelector);
					if ($element.length) {
						$(errorSelector).html(error.html());
					} else {
						error.insertAfter(element);
					}
				}
			},
			invalidHandler: function (form, validator) {  // 유효성검사 실패 시 alert 처리(오류메시지 처리)
				if(alertYn == 'Y') {
					var errors = validator.numberOfInvalids();
					if (errors) {
						var errMsg = validator.errorList[0].message;
						COMM.alertPopup(errMsg);
					}
				}
			},
		};
		var mergeOptions = $.extend(true, {}, defaultOption, options);
		$(this.selector).validate(mergeOptions);
	};
});
var COMM = {
	/**
	 * 페이지 로딩
	 */
	loadingShow : function() {
		$(".loader_flip").show();
	},
	loadingHide : function() {
		$(".loader_flip").hide();
	},
	/**
	 * 팝업 페이지 로딩
	 */
	modalLoadingShow : function() {
		$(".loader_modal").show();
	},
	modalLoadingHide : function() {
		$(".loader_modal").hide();
	},
	/**
	 * 팝업 페이지 로딩
	 */
	modalLoadingShow : function(_tag) {
		$("#"+_tag).show();
	},
	modalLoadingHide : function(_tag) {
		$("#"+_tag).hide();
	},
	/**
	 * 공통 alert
	 * @param : message  : 메세지 (필수)
	 *			callback : 확인 클릭 시 실행 될 함수
	 * 			title 	 : 타이틀
	 * 			desc  	 : 메세지 아래 설명
	 */
	alertPopup : function (message, callback , title, desc) {
		$(".alertPop .modal-header").hide();
		$(".alertDesc").html(desc).hide();

		if( ! $(".alertMessage")[0] ){
			console.error(".alertMessage 영역을 찾을 수 없습니다.");
			return;
		}
		if( typeof message === 'undefined' ){
			console.error('메시지를 입력하세요.');
			return;
		}

		if( typeof title !== 'undefined' && title != '' ){
			$(".alertTitle").html(title);
			$(".alertMessage").addClass('tits');
			$(".alertPop .modal-header").show();
		}
		if( typeof desc !== 'undefined' && desc != '' ){
			$(".alertDesc").html(desc).show();

		}
		$(".alertMessage").html(message);
		$('.alertPop').modal('show');

		// callback이 필수가 아니므로 값이 있을때만 확인버튼 클릭 시 callback 반환
		if (typeof callback === 'function' ){
			$('.alertPop .ok').off('click').on('click',function(){
				callback(true);
				$('.alertPop').modal('hide');
				$(".alertTitle").html(title);
			});
		// 없는 경우에는 hide만 처리
		}else{
			$('.alertPop .ok').off('click').on('click',function(){
				$('.alertPop').modal('hide');
				$(".alertTitle").html('');
			});
		}
	},
	/**
	 * 공통 confirm alert
	 * @param option  - message  : 메세지 (필수)
	 * 				  - callback : 확인 클릭 시 실행 될 함수 (필수)
	 * 				  - title 	 : 타이틀
	 * 				  - desc  	 : 메세지 아래 설명
	 */
	confirmPopup : function(option){
		// 필수값 체크
		if( typeof option.callback !== 'function'){
			console.error('콜백 함수를 찾을 수 없습니다.');
			return;
		}
		if( typeof option.message === 'undefined' ){
			console.error('메시지를 입력하세요.');
			return;
		}
		if( ! $(".confirmMessage")[0] ){
			console.error(".confirmMessage 영역을 찾을 수 없습니다.");
			return;
		}
		var message = option.message;
		var callback = option.callback;

		// 확인버튼 클릭 시 true 반환
		$('.confirmPop .ok').off('click').on('click',function(){
			callback(true);
			$('.confirmPop').modal('hide');
		});
		// 취소버튼 클릭 시 false 반환
		$('.confirmPop .cancel').off('click').on('click',function(){
			callback(false);
			$('.confirmPop').modal('hide');
		});

		if( typeof option.title !== 'undefined' && option.title != '' ){
			$(".confirmTitle").html(option.title);
			$(".confirmMessage").addClass('tits');
			$(".confirmPop .modal-header").show();
		}
		if( typeof option.desc !== 'undefined' && option.desc != '' ){
			$(".confirmDesc").html(option.desc).show();
		}
		if (typeof option.btn !== 'undefined' && option.btn != '') {
			if (typeof option.btn.msg !== 'undefined' && option.btn.msg != '') {
				$(".btnChk").html(option.btn.msg);
			}
			if (typeof option.btn.color !== 'undefined' && option.btn.color != '') {
				if (option.btn.color == 'red') {
					$(".btnChk").addClass('btn_primary');
				}
			}
		}
		$(".confirmMessage").html(message);
		$('.confirmPop').modal('show');
	},
	
	/**
	 * 자동이체설정 기준 사용기간, 다음 자동이체일 계산
	 *
	 * return   stepPeriod : 이체일 구분 선택
	 *          nextPeriod : 다음 이체일
	 *          paymentDay : 사용기간
	 *
	 */
	paymentPeriod: function (choicePeriod) {
		// 리턴값 초기화
		var periodInfo = {
			stepPeriod: "",
			nextPeriod: "",
			paymentDay: 0,
		};
		// 다음 결제일 계산 30일~59일 사이
		var nextPeriod = COMM.getYear() + ((((COMM.getMonth() * 1 + 1) < 10)
				? "0" : "") + (COMM.getMonth() * 1
				+ 1)) + choicePeriod;
		// 다음 결제일 계산 30일이내
		var targetPeriod = COMM.getYear() + ((((COMM.getMonth() * 1 + 2) < 10)
				? "0" : "") + (COMM.getMonth()
				* 1 + 2)) + choicePeriod;
		//비교할 오늘 정보
		var todayInfo = COMM.getYear() + COMM.getMonth() + COMM.getDay();
	
		// 최초 결제일 30일 초과 여부 확인
		if (COMM.getDayInterval(todayInfo, nextPeriod) > 30) {
			periodInfo.stepPeriod = choicePeriod;
			periodInfo.nextPeriod = nextPeriod;
			periodInfo.paymentDay = COMM.getDayInterval(todayInfo, nextPeriod);
		} else {
			periodInfo.stepPeriod = choicePeriod;
			periodInfo.nextPeriod = targetPeriod;
			periodInfo.paymentDay = (COMM.getDayInterval(todayInfo, nextPeriod)
					+ 31);
		}
		return periodInfo;
	},
	
	/**
	 *	업로드 파일 사이즈 체크
	 * @param file : 업로드 파일
	 * @param maxSize : 최대사이즈 (MB 단위)
	 * @returns {boolean} isDupl
	 */
	fileSizeChk : function(file , maxSize) {
		var file = file ;
		var fileSize = file.size;
		var _maxSize = maxSize * 1024 * 1024 ;

		if(fileSize > _maxSize) {
			COMM.alertPopup("첨부파일 사이즈는 " + maxSize + "MB 이하로 등록 가능합니다.");
			return false;
		}
		return true;
	},
	/**
	 * 업로드 파일 확장자 체크
	 * @param file : 업로드 파일
	 */
	fileExtChk : function(file) {
		var ext = file.name.split('.').pop().toLowerCase();
		if($.inArray(ext, ['gif','png','jpg','jpeg']) == -1) {
			COMM.alertPopup('등록 할수 없는 파일명입니다.');
			ext = ""; // input file 파일명을 다시 지워준다.
			return false;
		}
		return true;
	},
    /**
     * @설명     : 메뉴 목록 조회, 사용자 정보 조회
     * @param    :
     * @return   :
     * */
    menuDatasource: function () {
		UTIL.comAjax({
			url        : "/api/common/getMenuList",
			async      : false, // 동기
			data       : JSON.stringify({}),
			success    : function(result){
				userNo      = result.resultData.userNo;      //회원번호
				userId      = result.resultData.userId;      //아이디
				userNm      = result.resultData.userNm;      //이름
				userHpNo    = result.resultData.userHpNo;    //휴대폰
				userEmail   = result.resultData.userEmail;   //이메일
				brokerageCd = result.resultData.brokerageCd; //중개업소코드
				userRole    = result.resultData.userRole;    //회원구분
				joinStatus  = result.resultData.joinStatus;  //가입상태

				//네이버맵 관련
				naverGeocodeUrl   = result.resultData.naverGeocodeUrl;   //네이버주소 API URL
				naverGeocodeKeyId = result.resultData.naverGeocodeKeyId; //네이버주소 API 키 ID
				naverGeocodeKey   = result.resultData.naverGeocodeKey;   //네이버주소 API 키

                //코드 리스트
				memulJongList     = result.resultData.memulJong;         //매물종류코드 리스트
				directionBaseList = result.resultData.directionBase;     //방향기준코드 리스트
				directionList     = result.resultData.direction;     	 //방향코드 리스트
				doorTypeList      = result.resultData.doorType;     	 //현관구조코드 리스트
				useYnList         = result.resultData.useYn;     		 //사용여부코드 리스트
				mnexItemsList     = result.resultData.mnexItems;     	 //월관리비항목코드 리스트
				lawUsageCodeList  = result.resultData.lawUsageCode;      //건축물용도코드 리스트
				csInquireList     = result.resultData.csInquireList;     //건축물용도코드 리스트
				otelUsageList     = result.resultData.otelUsage;     	 //용도코드 리스트
				duplexTypeList    = result.resultData.duplexType;        //단층복층코드 리스트
				floorLevelList    = result.resultData.floorLevel;     	 //층레벨코드 리스트
				floorTypeList     = result.resultData.floorType;         //층정보코드 리스트
				trdTypeList       = result.resultData.trdType;           //거래종류코드 리스트
				loanTypeList      = result.resultData.loanType;       	 //층레벨코드 리스트
				saleTypeList      = result.resultData.saleType;          //층정보코드 리스트
				coolingFacList    = result.resultData.coolingFac;        //냉방시설코드 리스트
				lifeFacList       = result.resultData.lifeFac;           //생활시설코드 리스트
				securityFacList   = result.resultData.securityFac;       //보안시설코드 리스트
				etcFacList        = result.resultData.etcFac;            //기타시설코드 리스트
				oneroomTypeList   = result.resultData.oneroomType;       //원룸구조코드 리스트
				oneroomFeatureList= result.resultData.oneroomFeature;    //원룸특징코드 리스트
                expsList          = result.resultData.exps;              //네이버부동산연락처노출코드 리스트
				rentPeriodList    = result.resultData.rentPeriod;        //계약기간코드 리스트
				memulStatusList   = result.resultData.memulStatus;       //매물상태코드 리스트
				addrAreaList      = result.resultData.addrArea;          //주소위치코드 리스트
				exposureYnList    = result.resultData.exposureYn;        //노출여부코드 리스트
				transactionType    = result.resultData.transactionType;  //노출여부코드 리스트
				
				aInsList     	  = result.resultData.aIns;              //면적검수기준코드 리스트
				UnderList         = result.resultData.Under;             //지하총층코드 리스트
				parkingYnList     = result.resultData.parkingYn;         //주차가능여부코드 리스트
				reDevList         = result.resultData.reDev;             //재건축재개발여부코드 리스트
				abUseInfoList     = result.resultData.abUseInfo;         //준공사용일정보코드 리스트
				structureList     = result.resultData.structure;         //건축구조코드 리스트
				recommList        = result.resultData.recomm;            //추천업종코드 리스트
				pointList         = result.resultData.point;             //지목코드 리스트
				usageList         = result.resultData.usage;             //용도지역코드 리스트
				powerList         = result.resultData.power;             //동력코드 리스트
				optionList        = result.resultData.option;            //옵션코드 리스트
				requestTypeList   = result.resultData.requestType;       //요청구분 리스트
				statusTypeList    = result.resultData.statusType;        //상태구분 리스트
				schdTypeList      = result.resultData.schdType;          //일정분류 리스트
				alarmList         = result.resultData.alarm;             //알림 리스트
				landTypeList      = result.resultData.landType;          //대지권종류 리스트
				buildStructureList = result.resultData.buildStructure;   //건물구조 리스트
				buildUsageList    = result.resultData.buildUsage;      	 //건물용도 리스트
				acceptTypeList    = result.resultData.acceptType;        //인수방식 리스트
				contractorTypeList = result.resultData.contractorType;   //계약인구분 리스트
				priceMarkList = result.resultData.priceMark;             //금액표시 리스트
				userTypeList = result.resultData.userType;             // 회원구분 리스트
				joinTypeList = result.resultData.joinType;             // 가입구분 리스트
				joinStatusList = result.resultData.joinStatus;             // 가입상태 리스트
				adAddrTypeList = result.resultData.adAddrType;             // 광고노출소재지주소구분 리스트
				emailDomainList = result.resultData.emailDomain;             // 이메일 도메인 리스트
				usageJiguList     = result.resultData.usageJigu;         //용도지구 리스트
				usageGuyorkList   = result.resultData.usageGuyork;       //용도구역 리스트
				receiveSettingList = result.resultData.receiveSetting; //상태구분 리스트
				answerYnList = result.resultData.answerYn; //상태구분 리스트
				receptionTypeList = result.resultData.receptionType; //상태구분 리스트
				heatTypeList      = result.resultData.heatType;          //난방방식 리스트
				heatFuelList      = result.resultData.heatFuel;          //난방연료 리스트
				newBuildList      = result.resultData.newBuild;          //신축빌라 리스트
				yesNoList         = result.resultData.yesNo;             //예아니오 리스트
				bangOption1List   = result.resultData.bangOption1;       //방콜옵션1 리스트
				withdrawRsnList   = result.resultData.withdrawRsn;       //탈퇴사유 리스트
				processTypeList   = result.resultData.processType;       //처리구분 리스트
				memulNaverList   = result.resultData.memulNaver;         //네이버매물 리스트
				recProTermList   = result.resultData.recProTerm;         //접수처리기간 리스트
				bankCdList        = result.resultData.bankCd;            //은행코드 리스트
				applyReasonList    = result.resultData.applyReason;            //신청사유 리스트
			}
        });
    },
	
	/**
	 * @설명            : 코드를 selectBox에 등록
	 * @param :
	 *      tag         : 바인딩 시킬 input 태그 id
	 *      store       : 공통코드 리스트
	 *      checkItem  : check될 아이템
	 *      exceptItems : 리스트에서 제외할 목록( 키값만 배열로 전달)
	 *      defaultText : 추가옵션
	 *      ex          : COMM.bindCodeFromSelect('doorType', doorTypeList, '복합식', ['계단식'], '선택');
	 * @return   :
	 * */
	bindCodeFromSelect: function (tag, store, checkItem, exceptItems, defaultText) {
		var option   = "";
		var selected = "";
		var preSel   = "selected";
		if(!COMM.isEmpty(store)) {
			$.each(store, function (index, item) {
				selected = "";

				if(checkItem == item.CODE_VALUE){ //check값이 있을경우 해당 checkItem으로 selected
					selected = "selected";
					preSel   = "";
				}
				option += "<option value='" + item.CODE_VALUE + "' " + selected + " >" + item.CODE_NAME + "</option>";
				//해당 id에 option 추가
				$("#"+tag).html(option);
			});
		} else {
			$("#"+tag).html(option);
		}

		//기본값이 있으면 맨위에 추가
		if(!COMM.isEmpty(defaultText)) {
			$("#"+tag).prepend("<option value='' " + preSel + ">" + defaultText + "</option>");
		}

		//제외 옵션 삭제
		if(!COMM.isEmpty(exceptItems)) {
			$.each(exceptItems, function (index, item) {
				$("#" + tag + " option[value='" + item + "']").remove();
			});
		}
	},

	/**
	 * @설명            : 코드를 checkbox 등록
	 * @param :
	 *      tag         : 바인딩 시킬 input 태그 id
	 *      store       : 공통코드 리스트
	 *      name        : checkbox name
	 *      checkItem   : select 될 리스트
	 *      exceptItems : 리스트에서 제외할 목록( 키값만 배열로 전달)
	 *      ex          : COMM.bindCodeFromCheck('mnexItems', mnexItemsList, 'mnexItems', ['전기세', '수도'], ['인터넷']);
	 * @return   :
	 * */
	bindCodeFromCheck: function (tag, store, name, checkItem, exceptItems) {
		var option  = "";
		var html    = "";
		$.each(store, function (index, item) {
			option = "<div class=\"checkbox\">"
				   + "	<input type=\"checkbox\" id=\"" + name + "_" + index + "\" name=\"" + name + "\" value=\"" + item.CODE_VALUE + "\" >"
				   + "	<label for=\"" + name + "_" + index + "\" class=\"checkbox_label\">" + item.CODE_NAME + "</label>"
				   + "</div>";
			if(!COMM.isEmpty(exceptItems)) {
				$.each(exceptItems, function (exceptIndex, exceptItems) {
					if(item.CODE_VALUE != exceptItems) {
						html += option;
						//해당 id에 option 추가
						$("#"+tag).html(html);
					}
				});
			} else {
				html += option;
				//해당 id에 option 추가
				$("#"+tag).html(html);
			}
		});

		$.each(checkItem, function (index, item) {
			$("input:checkbox[name=\"" + name + "\"]").each(function() {
				if(this.value == item){ //값 비교
					this.checked = true; //checked 처리
				}
			});
		});
	},

	/**
	 * @설명            : 코드를 radio 등록
	 * @param :
	 *      tag         : 바인딩 시킬 input 태그 id
	 *      store       : 공통코드 리스트
	 *      name        : checkbox name
	 *      checkItem   : check될 리스트
	 *      exceptItems : 리스트에서 제외할 목록( 키값만 배열로 전달)
	 *      ex          : COMM.bindCodeFromRadio('directionBase', directionBaseList, 'directionBase', ['LVRM'], ['BDRM']);
	 * @return   :
	 * */
	bindCodeFromRadio: function (tag, store, name, checkItem, exceptItems) {
		var option  = "";
		var html    = "";
		$.each(store, function (index, item) {
			option = "<div class=\"radio\">"
				   + "	<input type=\"radio\" id=\"" + name + "_" + index + "\" name=\"" + name + "\" value=\"" + item.CODE_VALUE + "\" >"
				   + "	<label for=\"" + name + "_" + index + "\" class=\"radio_label\">" + item.CODE_NAME + "</label>"
				   + "</div>";
			if(!COMM.isEmpty(exceptItems)) {
				var flag = true;
				$.each(exceptItems, function (exceptIndex, exceptItems) {
					if(item.CODE_VALUE == exceptItems) {
						flag = false;
					}
				});

				if(flag) {
					html += option;
					//해당 id에 option 추가
					$("#"+tag).html(html);
				}
			} else {
				//해당 id에 option 추가
				html += option;
				$("#"+tag).html(html);
			}
		});

		$.each(checkItem, function (index, item) {
			$("input:radio[name=\"" + name + "\"]").each(function() {
				if(this.value == item){ //값 비교
					this.checked = true; //checked 처리
				}
			});
		});
	},

	//serializeObject 사용시 체크박스 1개도 배열로 들어가도록 처리
	checkData: function (formData, data) {
		var node;
		$.each(data, function(idx, el) {
			if(!COMM.isEmpty(el)) {
				node = [];
				$("input:checkbox[name='"+el+"']:checked").each(function(){ node.push(this.value); });
				formData[el] = node;
			}
		});
		return formData;
	},

	//serializeObject 사용시 셀렉트박스 1개도 배열로 들어가도록 처리
	selectData: function (formData, data) {
		var node;
		$.each(data, function(idx, el) {
			if(!COMM.isEmpty(el)) {
				node = [];
				$("select[name='"+el+"'] option:selected").each(function(){ node.push(this.value); });
				formData[el] = node;
			}
		});
		return formData;
	},

	//str 값이 없을 경우 defaultString 으로 대체 이것도 없으면 null 반환
	NVL: function (str, defaultString) {
	    if (COMM.isEmpty(str)) {
	      if (COMM.isNotEmpty(defaultString)) {
	        return defaultString;
	      } else {
	        return '';
	      }
	    } else {
	      return str;
	    }
	  },

	  /**
	   * @설명      : 자바스크립트 변수의 값이 비어 있는지 확인
	   * @param    : value
	   * @return   : Null인 경우 true 아니면 false
	   * */
	  isEmpty : function (value) {
	    if (value == "" || value == null || value == "null" || value == "0" || value == undefined || value == "undefined" || (value != null && typeof value == "object" && !Object.keys(value).length)) {
	      return true;
	    } else {
	      return false;
	    }
	  },

	  /**
	   * @설명      : 자바스크립트 변수의 값이 비어 있는지 확인
	   * @param    : value
	   * @return   : Null인 경우 false 아니면 값이 있을경우 true
	   * */
	  isNotEmpty: function (str) {
	    return !COMM.isEmpty(str);
	  },

	  /*
	  * @Method Name : inputRegExpLimit
	  * @Method 설명 : input 객체 정규표현식 입력제한
	  * @date:  2020.05.13
	  * @author : 이수정
	  * @param    :
	  *      value   : input 객체(this)
	  *      regEx   : 정규표현식
	  */
	  inputRegExpLimit       : function (value, regEx) {
	    var inputStr = $(value).val();
	    $(value).val(inputStr.replace(regEx, ""));
	  },

	  /**
	   * @설명      : ajax 통신 error 처리
	   * @param    :
	   * @return   :
	   * */
	  requestError  : function (error) {

	  },

	  /**
	   * @설명      : String 키를 Array에서 찾는 비교 함수
	   * @param    : COMM.isKeyFindOut(strKey, items)
	   * @return   : 키값이 존재할 경우 True 리턴
	   * */
	  isKeyFindOut  : function (strKey, items) {
	    if (COMM.isNull(strKey) || COMM.isNull(items)) {
	      return false;
	    }

	    if (items instanceof Array) {
	      for (var i = 0; i < items.length; i++) {
	        if (strKey === items[i]) {
	          return true;
	        }
	      }
	    }

	    return false;
	  },
	  /**
	   * @설명      : array화 된 form을 json으로 변환
	   * @param    : formArray($("#form").serializeArray() 값)
	   * @return   : json 형식 반환
	   * */
	  objectifyForm          : function (formArray) {//serialize data function
	    var returnArray = {};

	    for (var i = 0; i < formArray.length; i++) {
	      returnArray[formArray[i]['name']] = formArray[i]['value'];
	    }

	    return returnArray;
	  },

	/**
	* Time 스트링을 자바스크립트 Date 객체로 변환
	* parameter time: Time 형식의 String
	*/
	toTimeObject: function (time) { //parseTime(time)
		var year = time.substr(0, 4);
		var month = time.substr(4, 2) - 1; // 1월=0,12월=11
		var day = time.substr(6, 2);
		var hour = time.substr(8, 2);
		var min = time.substr(10, 2);

		return new Date(year, month, day, hour, min);
	},

	/**
	 * Time 스트링을 자바스크립트 Date 객체로 변환
	 * parameter time: Time 형식의 String
	 */
	toTimeObjectString: function (time, sep) {
		var year = time.substr(0, 4);
		var month = time.substr(4, 2);
		var day = time.substr(6, 2);
		var hour = time.substr(8, 2);
		var min = time.substr(10, 2);
		var sec = time.substr(12, 2);

		return year + sep + month + sep + day + ' ' + hour + ':' + min + ':' + sec;
	},
	  /**
	   * 자바스크립트 Date 객체를 Time 스트링으로 변환
	   * parameter date: JavaScript Date Object
	   */
	  toTimeString: function (date) { //formatTime(date)
	    var year = date.getFullYear();
	    var month = date.getMonth() + 1; // 1월=0,12월=11이므로 1 더함
	    var day = date.getDate();
	    var hour = date.getHours();
	    var min = date.getMinutes();
	    var sec = date.getSeconds();

	    if (("" + month).length == 1) {
	      month = "0" + month;
	    }
	    if (("" + day).length == 1) {
	      day = "0" + day;
	    }
	    if (("" + hour).length == 1) {
	      hour = "0" + hour;
	    }
	    if (("" + min).length == 1) {
	      min = "0" + min;
	    }
	    if (("" + sec).length == 1) {
	      sec = "0" + sec;
	    }

	    return ("" + year + month + day + hour + min + sec);
	  },

	getCurrentDate: function(_date, _tag) {
		var strTime = "";
		var year    = _date.getFullYear();
		var month   = _date.getMonth() + 1; // 1월=0,12월=11이므로 1 더함
		var day     = _date.getDate();
		var hours   = _date.getHours();
		var minutes = _date.getMinutes();
		var ampm = hours >= 12 ? 'PM' : 'AM';
		hours   = hours % 12;
		hours   = hours ? hours : 12; // the hour '0' should be '12'
		month   = month   < 10 ? '0'+month   : month;
		day     = day     < 10 ? '0'+day     : day;
		minutes = minutes < 10 ? '0'+minutes : minutes;

		strTime = year + '/' + month + '/' + day;
		if(_tag == '1') {
			strTime += ' ' + hours + ':' + '00' + ' ' + ampm;
		} else if(_tag == '2') {
			strTime += ' ' + '00' + ':' + '00' + ' ' + 'AM';
		}
		return strTime;
	},

	  /**
	   * Time이 현재시각 이후(미래)인지 체크
	   */
	  isFutureTime: function (time) {
	    return (COMM.toTimeObject(time) > new Date());
	  },

	  /**
	   * Time이 현재시각 이전(과거)인지 체크
	   */
	  isPastTime: function (time) {
	    return (COMM.toTimeObject(time) < new Date());
	  },

	  /**
	   * 주어진 Time 과 y년 m월 d일 h시 차이나는 Time을 리턴
	   * ex) var time = form.time.value; //'20000101000'
	   *     alert(shiftTime(time,0,0,-100,0));
	   *     => 2000/01/01 00:00 으로부터 100일 전 Time
	   */
	  shiftTime: function (time, y, m, d, h) { //moveTime(time,y,m,d,h)
	    var date = COMM.toTimeObject(time);

	    date.setFullYear(date.getFullYear() + y); //y년을 더함
	    date.setMonth(date.getMonth() + m);       //m월을 더함
	    date.setDate(date.getDate() + d);         //d일을 더함
	    date.setHours(date.getHours() + h);       //h시를 더함

	    return COMM.toTimeString(date);
	  },

	  /**
	   * 두 Time이 몇 개월 차이나는지 구함
	   * time1이 time2보다 크면(미래면) minus(-)
	   */
	  getMonthInterval: function (time1, time2) { //measureMonthInterval(time1,time2)
	    var date1 = COMM.toTimeObject(time1);
	    var date2 = COMM.toTimeObject(time2);

	    var years = date2.getFullYear() - date1.getFullYear();
	    var months = date2.getMonth() - date1.getMonth();
	    var days = date2.getDate() - date1.getDate();

	    return (years * 12 + months + (days >= 0 ? 0 : -1));
	  },

	  /**
	   * 두 Time이 며칠 차이나는지 구함
	   * time1이 time2보다 크면(미래면) minus(-)
	   */
	  getDayInterval: function (time1, time2) {
	    var date1 = COMM.toTimeObject(time1);
	    var date2 = COMM.toTimeObject(time2);
	    var day = 1000 * 3600 * 24; //24시간

	    return parseInt((date2 - date1) / day, 10);
	  },

	  /**
	   * 두 Time이 몇 시간 차이나는지 구함
	   * time1이 time2보다 크면(미래면) minus(-)
	   */
	  getHourInterval: function (time1, time2) {
	    var date1 = COMM.toTimeObject(time1);
	    var date2 = COMM.toTimeObject(time2);
	    var hour = 1000 * 3600; //1시간

	    return parseInt((date2 - date1) / hour, 10);
	  },

	  /**
	   * 현재 시각을 Time 형식으로 리턴
	   */
	  getCurrentTime: function () {
	    return COMM.toTimeString(new Date());
	  },

	  /**
	   * 현재 시각과 y년 m월 d일 h시 차이나는 Time을 리턴
	   */
	  getRelativeTime: function (y, m, d, h) {
	    return COMM.shiftTime(COMM.getCurrentTime(), y, m, d, h);
	  },

	  /**
	   * 현재 年을 YYYY형식으로 리턴
	   */
	  getYear: function () {
	    return COMM.getCurrentTime().substr(0, 4);
	  },

	  /**
	   * 현재 月을 MM형식으로 리턴
	   */
	  getMonth: function () {

	    return COMM.getCurrentTime().substr(4, 2);
	  },

	  /**
	   * 현재 日을 DD형식으로 리턴
	   */
	  getDay: function () {
	    return COMM.getCurrentTime().substr(6, 2);
	  },

	  /**
	   * 현재 時를 HH형식으로 리턴
	   */
	  getHour: function () {
	    return COMM.getCurrentTime().substr(8, 2);
	  },

	  //숫자형 데이터 3자리수 ',' 찍어 표현
	  numberWithCommas: function (num) {
	  	  var result = 0;
		  var special_pattern = /[\.]/gi;
		  if(special_pattern.test(num) == true) {
			  var _valArr = [];
			  var _valRt  = 0;
			  _valArr = num.split('.');
			  result = _valArr[0].toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",") + '.' + _valArr[1];
		  } else {
			  result = num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
		  }

	    return result;
	  },

	  /**
	   * 오늘 날짜 기준
	   */
	  getMinusDay: function(day) {
	      var mydate = new Date();
	      mydate.setDate(mydate.getDate() +day);
	      return COMM.formatDate(mydate);
	  },
	  formatDate : function (date) {
	      var mymonth = date.getMonth() + 1;
	      var myweekday = date.getDate();
	      return (date.getFullYear() + "-" + ((mymonth < 10) ? "0" : "") + mymonth + "-" + ((myweekday < 10) ? "0" : "") + myweekday);
	  },

	  /**
	   * 날짜 변환
	   * "yyyy.mm.dd" 형태를 "yyyy년 mm월 dd일" 형태로 리턴
	   */
	  changeDateStr: function (date) {
		date = date.split(".");
		return date[0] + "년 " + date[1] + "월 " + date[2] + "일";
	  },

	  /**
	   * json data key name rename
	   *
	   * @param params
	   */
	  replaceKey  : function (param, split) {
	    if (!COMM.isEmpty(param)) {
	      for (var key in param) {
	        if (key.indexOf(split) > -1) {
	          param[key.substring(0, key.indexOf("_"))] = param[key];
	          delete param[key];
	        }
	      }
	    }
	    return param;
	  },

	  renameKey  : function (param, keyName, rename) {
	    if (!COMM.isEmpty(param)) {
	      for (var key in param) {
	        if(key == keyName) {
	          param[rename] = param[key];
	          delete param[key];
	        }
	      }
	    }
	    return param;
	  },

	  //8자리의 문자열 형식의 날짜를 date형으로 파싱후 반환
	  stringToDate: function (str) {
	    var _date = str;
	    var year = _date.substring(0, 4);
	    var month = _date.substring(4, 6);
	    var day = _date.substring(6, 8);
	    return new Date(year, month, day);  // date로 변경
	  },

	  /**
	   * IE 여부 확인
	   * @returns
	   */
	  isIE  : function () {
	    return (/msie|trident/gi).test(navigator.userAgent);
	  },

	  jsontoString  : function (object) {
	    var results = [];
	    for (var property in object) {
	      var value = object[property];
	      if (value) {
	        results.push(property.toString() + ': ' + value);
	      }
	    }
	    return '{' + results.join(', ') + '}';
	  },

	  // Left 빈자리 만큼 padStr 을 붙인다.
	  lpad  : function(src, len, padStr) {
	    var retStr = "";
	    var padCnt = Number(len) - String(src).length;
	    for (var i = 0; i < padCnt; i++) retStr += String(padStr);
	    return retStr + src;
	  },


	  // Right 빈자리 만큼 padStr 을 붙인다.
	  rpad  : function (src, len, padStr) {
	    var retStr = "";
	    var padCnt = Number(len) - String(src).length;
	    for (var i = 0; i < padCnt; i++) retStr += String(padStr);
	    return src + retStr;
	  },

	  // 대문자변환
	  toUpperCase : function (str) {
	    if (COMM.isEmpty(str)) return str;
	    return str.toUpperCase();
	  },

	  // 실제길이 반환( 한글 2byte 계산 )
	  getByteLength : function (str) {
	    var len = 0;
	    if (str == null) return 0;
	    for (var i = 0; i < str.length; i++) {
	      var c = escape(str.charAt(i));
	      if (c.length == 1) len++;
	      else if (c.indexOf("%u") != -1) len += 2;
	      else if (c.indexOf("%") != -1) len += c.length / 3;
	    }
	    return len;
	  },

	  ruleParamstoJson : function (rule) {
	    var params = {};

	    rule.toString().replace(/([^=;]+)=([^;]*)/gi,
	        function (str, key, value) {
	          params[key.replace(";", "")] = value;
	        });
	    return params;
	  },

	  getParameterToJson : function() {
	    var params = {};

	    window.location.search.toString().replace(/[?&]+([^=&]+)=([^&]*)/gi,
	        function (str, key, value) {
	          params[key.replace(";", "")] = value;
	        });
	    return params;
	  },

	  // 글자 길이 계산후 최대값에 따른 내용 반환
	  textLength : function (o) {

		  var str = String($(o).val());
		  var max = $(o).attr("data-max-length");
		  // 현재 입력 값 길이 표시: HTML 구조가 상이할 경우 재작업 필요
		  var num = $(o).next().find("i");

		  num.text(str.length);
		  // 메세지 처리가 필요할 경우 재작업 필요
		  if (str.length > max) {
		      $(o).val(str.substring(0, max));
		      num.text("" + max);
		  }
	  },

	  // 숫자만 입력되게
	  numberCheck : function (o) {
		  $(o).val($(o).val().replace(/[^0-9]/g,""));
	  }
	  ,
	  /**
	   * 쿠키저장
	   * @param cname 키값
	   * @param cvalue 저장할 문자열
	   * @param exdays 쿠키 저장 일수
	   */
	  setCookie: function( cname, cvalue, exdays ) {
	      var d = new Date();
	      d.setTime(d.getTime() + (exdays*24*60*60*1000));
	      var expires = "expires="+d.toUTCString();
	      document.cookie = cname + "=" + cvalue + "; " + expires + ';path=/' + ";"; // SameSite=None; Secure ;" // SameSite=None; Secure ;  ==> PC / MOBILE COOKIE 공유 위한 설정
	  },

	  /**
	   * 쿠키 가져오기
	   * @param cname 키값
	   * @return str
	   */
	  getCookie: function( cname ) {
	      var name = cname + "=";
	      var ca = document.cookie.split(';');
	      for(var i = 0; i < ca.length; i++) {
	          var c = ca[i];
	          while (c.charAt(0) == ' ') {
	              c = c.substring(1);
	          }
	          if (c.indexOf(name) == 0) {
	              return c.substring(name.length, c.length);
	          }
	      }
	      return "";
	  },

	  /**
	   * 쿠키 삭제
	   * @param cname 키값
	   */
	  delCookie: function( cname ) {
		COMM.setCookie( cname , "" , 10);
	  },

	  /**
	   * 배열데이타 쿠키 저장
	   * @param cname 키값
	   * @param carray 저장할 배열
	   * @param exdays 쿠키 저장 일수
	   */
	  setCookieArray: function( cname, carray, exdays ) {
	   var str = "";
	   for( var key in carray ){
	    if(str != "" ) str += ",";
	    str += key+":"+carray[key];
	   }
	   this.setCookie( cname, str, exdays );
	  },

	  /**
	   * 쿠키에서 배열로 저장된 데이타 가져옴
	   * @param cname
	   * @return array
	   */
	  getCookieArray: function( cname ) {
	   var str = this.getCookie( cname );
	   var tmp1 = str.split(",");
	   var reData = new Array();
	   for( var i in tmp1 ){
	    var tmp2 = tmp1[i].split(":");
	    reData.push(tmp2[1]);
	   }
	   return reData;
	  }
	  ,
	  /**
	   * 쿠키에 최근 검색 이력 쌓기
	   *
	   */
	  setSearchKeyword : function( keyword ) {

		  if( typeof COMM.getCookie("search-recent") == undefined || COMM.getCookie("search-recent") == null){
				var cookiesList = new Array();
				cookiesList.push(keyword);
				COMM.setCookieArray("search-recent" ,cookiesList , 10);
			}else{
				var cookiesList = COMM.getCookieArray("search-recent");
				var cookiesList0 = cookiesList[0]+"";
				if( cookiesList.length && cookiesList0 == 'undefined' ){
					cookiesList[0] = keyword;
				}else{

					var dupKeywordIndex = cookiesList.indexOf(keyword)  ;
					if(dupKeywordIndex != -1){
						cookiesList.splice(dupKeywordIndex, 1);	// 중복제거
					}
					if(cookiesList.length == 10){
						cookiesList.splice(0, 1);
					}
					cookiesList.push(keyword);

				}
				COMM.setCookieArray("search-recent" ,cookiesList , 10);
			}
	  },
};

var HTTP_CONNUTIL = {
	/**
	 * @설명      : 화면에서 GET 방식 RestAPI 호출
	 * @param    : reqUrl 요청 URL
	 * @param    : formParams - $("#form").serializeArray() 값
	 * @param    : callback - callback 함수
	 * @return   : 
	 * */
	sendGetRestApi : function(reqUrl, formParams, callback) {
		var xhr = new XMLHttpRequest();     
		xhr.open('GET', reqUrl + HTTP_CONNUTIL.makeFormArrToGetParams(formParams));     
		xhr.onreadystatechange = function () {     
			if (this.readyState == 4) {     
				if (this.status == 200) {
                    callback ? callback(this.responseText) : callback(null);
                }
			}     
  		};     
  		xhr.send('');
	},
	
	/**
	 * @설명      : array화 된 form을 Get parameter Stirng 형식으로 반환
	 * @param    : formArray($("#form").serializeArray() 값)
	 * @return   : 
	 * */
	makeFormArrToGetParams : function(formArray) {
		var getParams	= "";
		for (var i = 0; i < formArray.length; i++) {
			getParams += (getParams.length > 0 ? "&" : "?");
			getParams += encodeURIComponent(formArray[i]['name']) + "=" + encodeURIComponent(formArray[i]['value']);
		}
		return getParams;
	}
};

var ADDRESS = {
	/*
	 * preTag    : 추가 Tag
	 * tag       : id tag
	 * value     : 선택값
	 * menulType : 매물종류
	 * trdType   : 거래종류
	 */
	addrChk: function(idx, preTag, value, menulType, trdType, flag) {
		var val1 = $("#"+preTag+"addr1").val();
		var val2 = $("#"+preTag+"addr2").val();
		var val3 = $("#"+preTag+"addr3").val();

		if(flag) {
			menulType = $("#"+menulType).val();
			trdType   = $("input[name="+trdType+"]").val();
		}

		if(idx != 4) {
			ADDRESS.add(preTag, (idx+1), val1, val2, val3, '', menulType, trdType);
		} else {
			ADDRESS.getBdName(val1, val2, val3, menulType, trdType, '', preTag);
		}
	},

	/*
	 * 일반 주소 조회
	 */
	normalAddr: function(tag, idx, addr1, addr2, addr3, val) {
		var addrGu   = '';
		if(!COMM.isEmpty(addr2) && addr2.indexOf(" ") > 0) {
			addrGu = addr2.split(" ")[1];
			addr2  = addr2.split(" ")[0];
		}

		UTIL.comAjax({
			url        : "/api/common/getNormalAddress",
			async      : false, // 동기
			data       : JSON.stringify({level: idx, addr1: addr1, addr2: addr2, addr3: addr3, addrGu: addrGu}),
			success    : function(result){
				var dataList = result.resultData.data;
				$("#" + tag + "4").hide();
				if(idx == '1') {  //시도
					COMM.bindCodeFromSelect(tag+'1', dataList,          val, '', '시/도');
					COMM.bindCodeFromSelect(tag+'2', '', '', '', '구/시/군');
					COMM.bindCodeFromSelect(tag+'3', '', '', '', '읍/면/동');
					COMM.bindCodeFromSelect(tag+'4', '', '', '', '상세리');
				} else if(idx == '2') {  //구시군
					COMM.bindCodeFromSelect(tag+'2', dataList,          val, '', '구/시/군');
					COMM.bindCodeFromSelect(tag+'3', '', '', '', '읍/면/동');
					COMM.bindCodeFromSelect(tag+'4', '', '', '', '상세리');
				} else if(idx == '3') {  //읍면동
					COMM.bindCodeFromSelect(tag+'3', dataList,          val, '', '읍/면/동');
					COMM.bindCodeFromSelect(tag+'4', '', '', '', '상세리');
				} else if(idx == '4') {  //상세리
					if(!COMM.isEmpty(dataList[0].CODE_NAME)) {
						$("#" + tag + "4").show();
						COMM.bindCodeFromSelect(tag+'4', dataList,          val, '', '상세리');
					}
				}
			}
		});
	},
	/*
	 * 매물 주소 조회
	 * tag  : 추가태그
	 * idx  : 인텍스
	 * addr1: 시도
	 * addr2: 구시군
	 * addr3: 읍면동
	 * val  : 선택값
	 * chk1 : 매물종류코드
	 * chk2 : 거래종류코드
	 */
	add: function(tag, idx, addr1, addr2, addr3, val, memulType, trdType) {
		var emptyNum = "1";
		var addrGu   = '';

		if(!COMM.isEmpty(addr2) && addr2.indexOf(" ") > 0) {
			addrGu = addr2.split(" ")[1];
			addr2  = addr2.split(" ")[0];
		}

		$(".naverInfo").hide();  //네이버원룸정보
		UTIL.comAjax({
			url        : "/api/common/getAddress",
			async      : false, // 동기
			data       : JSON.stringify({level: idx, addr1: addr1, addr2: addr2, addr3: addr3, memulType: memulType, trdType: trdType, addrGu: addrGu}),
			success    : function(result){
				var dataList = result.resultData.data;
				$("#"+tag+"addr4").hide();

				if(idx == '1') {  //시도
					COMM.bindCodeFromSelect(tag+'addr1', dataList,          val, '', '시/도');
					COMM.bindCodeFromSelect(tag+'addr2', '', '', '', '구/시/군');
					COMM.bindCodeFromSelect(tag+'addr3', '', '', '', '읍/면/동');
					COMM.bindCodeFromSelect(tag+'addr4', '', '', '', '상세리');
				} else if(idx == '2') {  //구시군
					COMM.bindCodeFromSelect(tag+'addr2', dataList,          val, '', '구/시/군');
					COMM.bindCodeFromSelect(tag+'addr3', '', '', '', '읍/면/동');
					COMM.bindCodeFromSelect(tag+'addr4', '', '', '', '상세리');
				} else if(idx == '3') {  //읍면동
					COMM.bindCodeFromSelect(tag+'addr3', dataList,          val, '', '읍/면/동');
					COMM.bindCodeFromSelect(tag+'addr4', '', '', '', '상세리');
				} else if(idx == '4') {  //상세리
					if(!COMM.isEmpty(dataList)) {
						if(!COMM.isEmpty(dataList[0].CODE_NAME)) {
							$("#"+tag+"addr4").show();
							COMM.bindCodeFromSelect(tag+'addr4', dataList,          val, '', '상세리');
						}
					} else {
						emptyNum = "2";
						ADDRESS.getBdName(addr1, addr2, addr3, memulType, trdType, '', tag);
					}
				}
			}
		});
		$("#dirWriteYn").prop("checked", false); //직접등록여부
		ADDRESS.setupAptDetail(emptyNum, tag);
	},
	/*
	 * 아파트 단지명 정보 조회
	 */
	getBdName: function(addr1, addr2, addr3, memulType, trdType, val, tag) {
		UTIL.comAjax({
			url        : "/api/common/getBdName",
			async      : false, // 동기
			data       : JSON.stringify({addr1: addr1, addr2: addr2, addr3: addr3, memulType: memulType, trdType: trdType}),
			success    : function(result){
				var dataList = result.resultData.data;
				COMM.bindCodeFromSelect(tag + 'aptName'  , dataList, val, '', '단지');
				ADDRESS.setupAptDetail("2", tag);
			}
		});
	},
	/*
	 * 면적 정보 조회
	 */
	getExclusive: function(memulType, trdType, defaultCode, defaultEtc, val, tag) {
		UTIL.comAjax({
			url        : "/api/common/getExclusive",
			async      : false, // 동기
			data       : JSON.stringify({memulType: memulType, trdType: trdType, defaultCode: defaultCode, defaultEtc: defaultEtc}),
			success    : function(result){
				var dataList = result.resultData.data;
				COMM.bindCodeFromSelect('exclusive'  , dataList, val, '', '면적');
				ADDRESS.setupAptDetail("3", tag);
			}
		});
	},
	/*
	 * 동/호 정보 조회
	 */
	getDongHo: function(level, aptName, dong, exclusive, val, tag, type) {
		var defaultCode     = aptName.split("|")[0];
		var equilibrium     = exclusive.split("|")[0];
		var equilibriumType = exclusive.split("|")[1];
		var txt             = "";

		UTIL.comAjax({
			url        : "/api/common/getDongHo",
			async      : false, // 동기
			data       : JSON.stringify({level: level, defaultCode: defaultCode, equilibrium: equilibrium, equilibriumType: equilibriumType, dong: dong}),
			success    : function(result){
				var dataList = result.resultData.data;
				if(COMM.isEmpty(dataList)) {
					if(type == 'U') {
						$("#dong, #ho").attr('disabled', true);
						$(".dong, .ho").attr('disabled', false);
						$(".selectChk").hide();
						$(".inputChk").show();
						$("#donghoYn").val('N');  //동호 없음
						$("#dirWriteYn").prop("checked", true); //직접등록여부
					} else {
						COMM.alertPopup('\"호\"정보가 없으니, 직접입력해주세요.', function() {
							$("#dong, #ho").attr('disabled', true);
							$(".dong, .ho").attr('disabled', false);
							$(".selectChk").hide();
							$(".inputChk").show();
							$("#donghoYn").val('N');  //동호 없음
							$("#dirWriteYn").prop("checked", true); //직접등록여부
						}, '상세주소');
					}

					return false;
				} else {
					$(".dong, .ho").attr('disabled', true);
					$("#dong, #ho").attr('disabled', false);
					$(".inputChk").hide();
					$(".selectChk").show();
					$("#donghoYn").val('Y');  //동호 있음
					$("#dirWriteYn").prop("checked", false); //직접등록여부

					if(level == 'dong') {
						dongTFList = dataList;
						txt = "동";
					} else if(level == 'ho') {
						hoFList = dataList;
						txt = "호";
					}
				}
				COMM.bindCodeFromSelect(level  , dataList, val, '', txt);
				if(level == 'dong') {
					ADDRESS.setupAptDetail("4", tag);
				}
			}
		});
	},
	/*
	 * 해당층, 총층 자동 체크
	 */
	floorChk: function() {
		var totalFloor = "";
		var floor      = "";
		if($("#donghoYn").val() == 'Y') {
			if(!COMM.isEmpty(dongTFList)) {  //총층
				var dong = $("#dong").val();
				$.each(dongTFList, function (index, item) {
					if(item.CODE_VALUE == dong) {
						totalFloor = item.totalFloor;
						COMM.bindCodeFromSelect('totalFloor', floorTypeList, [totalFloor], total_exceptFloor, '선택'); //총층 옵션추가
					}
				});
			}
			if(!COMM.isEmpty(hoFList)) {  //해당층
				var ho = $("#ho").val();
				$.each(hoFList, function (index, item) {
					if(item.CODE_VALUE == ho) {
						floor = item.floor;
						COMM.bindCodeFromSelect('isFloor', floorTypeList,  [floor], exceptFloor, '선택'); //해당층 옵션추가
					}
				});
			}
		}
		//층레벨
		var val = "저";
		var avg = Number(10/totalFloor*floor);
		if(avg > 7) {
			val = "고";
		} else if(avg > 4) {
			val = "중";
		}

		COMM.bindCodeFromRadio('floorLevel'     , floorLevelList   , 'floorLevel'     , [val]    ,     ''); //층레벨 옵션추가
	},
	/*
	 * 아파트 단지명, 면적, 상세주소 정보 초기화
	 */
	setupAptDetail: function(idx, tag) {
		$(".dong, .ho").attr('disabled', true);
		$("#dong, #ho").attr('disabled', false);
		$(".inputChk").hide();
		$(".selectChk").show();
		switch(idx) {
			case "1":
				COMM.bindCodeFromSelect(tag+'aptName'  , '', '', '', '단지'); //단지명
			case "2":
				COMM.bindCodeFromSelect(tag+'exclusive', '', '', '', '면적'); //면적
			case "3":
				COMM.bindCodeFromSelect(tag+'dong'     , '', '', '', '동'); //동
				$("#dirWriteYn").prop("checked", false); //직접등록여부
			case "4":
				COMM.bindCodeFromSelect(tag+'ho'       , '', '', '', '호'); //호
		}
	},
	/*
	 * 재개발
	 * preTag    : 추가 Tag
	 * tag       : id tag
	 * value     : 선택값
	 * menulType : 매물종류
	 * trdType   : 거래종류
	 */
	devAddrChk: function(idx, preTag, value, _menulType, _trdType) {
		var val1 = $("#"+preTag+"addr1").val();
		var val2 = $("#"+preTag+"addr2").val();
		var val3 = $("#"+preTag+"addr3").val();
		var menulType = $("#" + _menulType).val();
		var trdType   = $("input[name=" + _trdType+"]").val();

		if(idx == 2) {
			ADDRESS.devBdName(val1, val2, val3, menulType, trdType, '', preTag);
		} else if(idx == 3) {
			ADDRESS.devAddr2(preTag, value, '');
		} else {
			ADDRESS.devAddr(preTag, (idx+1), val1, val2, val3, '', menulType, trdType);
		}
	},
	/*
	 * 재개발 주취급단지
	 * preTag    : 추가 Tag
	 * tag       : id tag
	 * value     : 선택값
	 * menulType : 매물종류
	 * trdType   : 거래종류 id value 가져오기
	 */
	devAddrChkArea: function(idx, preTag, value, _menulType, _trdType) {
		var val1 = $("#"+preTag+"addr1").val();
		var val2 = $("#"+preTag+"addr2").val();
		var val3 = $("#"+preTag+"addr3").val();
		var menulType = $("#" + _menulType).val();
		var trdType   = $("#" + _trdType).val();
		if(idx == 2) {
			ADDRESS.devBdName(val1, val2, val3, menulType, trdType, '', preTag);
		} else if(idx == 3) {
			ADDRESS.devAddr2(preTag, value, '');
		} else {
			ADDRESS.devAddr(preTag, (idx+1), val1, val2, val3, '', menulType, trdType);
		}
	},
	/*
	 * 매물 주소 조회 - 재개발
	 * tag  : 추가태그
	 * idx  : 인텍스
	 * addr1: 시도
	 * addr2: 구시군
	 * addr3: 읍면동
	 * val  : 선택값
	 * chk1 : 매물종류코드
	 * chk2 : 거래종류코드
	 */
	devAddr: function(tag, idx, addr1, addr2, addr3, val, memulType, trdType) {
		UTIL.comAjax({
			url        : "/api/common/getAddress",
			async      : false, // 동기
			data       : JSON.stringify({level: idx, addr1: addr1, addr2: addr2, addr3: addr3, memulType: memulType, trdType: trdType, addrGu: ''}),
			success    : function(result){
				var dataList = result.resultData.data;
				if(idx == '1') {  //시도
					COMM.bindCodeFromSelect(tag + 'addr1'  , dataList,           val, '', '시/도');
					COMM.bindCodeFromSelect(tag + 'addr2'  , '', '', '', '구/시/군');
					COMM.bindCodeFromSelect(tag + 'aptName', '', '', '', '단지');
					COMM.bindCodeFromSelect(tag + 'addr3'  , '', '', '', '읍/면/동');
				} else if(idx == '2') {  //구시군
					COMM.bindCodeFromSelect(tag + 'addr2'  , dataList, 	     val, '', '구/시/군');
					COMM.bindCodeFromSelect(tag + 'aptName', '', '', '', '단지');
					COMM.bindCodeFromSelect(tag + 'addr3'  , '', '', '', '읍/면/동');
				}
			}
		});
	},
	/*
	 * 매물 주소(읍면동) 조회 - 재개발
	 * tag  : 추가태그
	 * val  : 선택값
	 */
	devAddr2: function(tag, val, addr3) {

        var aptDefaultCode = "";
		if(!COMM.isEmpty(val) && val.indexOf("|") > 0) {
			aptDefaultCode = val.split("|")[0];
		}

		UTIL.comAjax({
			url        : "/api/common/getAddress2",
			async      : false, // 동기
			data       : JSON.stringify({aptDefaultCode: aptDefaultCode}),
			success    : function(result){
				var dataList = result.resultData.data;
				COMM.bindCodeFromSelect(tag + 'addr3'  , dataList, addr3, '', '읍/면/동');
			}
		});
	},
	/*
	 * 아파트 단지명 정보 조회 - 재개발
	 */
	devBdName: function(addr1, addr2, addr3, memulType, trdType, val, tag) {
		if(!COMM.isEmpty(addr2) && addr2.indexOf(" ") > 0) {
			addr2  = addr2.split(" ")[0];
		}

		UTIL.comAjax({
			url        : "/api/common/getBdName",
			async      : false, // 동기
			data       : JSON.stringify({addr1: addr1, addr2: addr2, addr3: addr3, memulType: memulType, trdType: trdType}),
			success    : function(result){
				var dataList = result.resultData.data;
				COMM.bindCodeFromSelect(tag + 'aptName'  , dataList, val, '', '단지');
				COMM.bindCodeFromSelect(tag + 'addr3', '', '', '', '읍/면/동');
			}
		});
	},
}

//매물 종류 selectBox 모듈
var MEMUL_TYPE = {
	memulSelect: function(idx, tag, val) {
		var val1 = "", val2 = "";
		switch(idx) {
			case '2' :
				val1 = $("#"+tag+"1 option").index($("#"+tag+"1 option:selected"));
				break;
			case '3' :
				val1 = $("#"+tag+"1 option").index($("#"+tag+"1 option:selected"));
				val2 = $("#"+tag+"2 option").index($("#"+tag+"2 option:selected"));
				break;
		}
		MEMUL_TYPE.bindSelect(idx, val1, val2, tag, val);
	},

	bindSelect: function(no, idx1, idx2, tag, val) {
		var list = memulJongList;
		switch (no) {
			case '2':
				COMM.bindCodeFromSelect(tag+'3'    , '', '', '', '선택');
				list = memulJongList[idx1-1].CHILD_LIST;
				break;
			case '3':
				list = memulJongList[idx1-1].CHILD_LIST[idx2-1].CHILD_LIST;
				break;
			default:
				COMM.bindCodeFromSelect(tag+'2'    , '', '', '', '선택');
				COMM.bindCodeFromSelect(tag+'3'    , '', '', '', '선택');
				break;
		}
		COMM.bindCodeFromSelect(tag + no, list, val, '', '선택');
	},
	/*
	 * 매물종류에 따른 거래종류 옵션 변경
	 * tag : 적용할 selectbox id
	 * memulType1 : 매물종류 1뎁스
	 * memulType2 : 매물종류 2뎁스
	 *
	 * 거래종류코드 : 매매(A1), 전세(B1), 월세(B2), 임대(R1)
	 * 1. 매매, 전세, 월세, 임대 : A1아파트(A)
	 * 2. 매매, 전세, 월세 : A1주상복합(C), A1재건축(H), A1오피스텔(J), A1도시형생활주택(O5),
	 * 					 B1일반원룸(BA), B1오피스텔(BB), B1도시형생활주택(BC), B1원룸형아파트(BD), B1기타(BE),
	 * 					 C1빌라/연립(O2), C1단독/다가구(O), C1전원/농가(O3), C1한옥주택(M),
	 * 					 D1상가점포(E), D1사무실(P)
	 * 3. 매매, 임대 : A1아파트분양권(B), A1주상복합분양권(D), A1오피스텔분양권(K), A1생활숙박시설(V),
	 * 				C1상가주택(Q),
	 * 	            D1빌딩/건물(F), D1숙박/콘도(I), D1상가건물(R), D1공장(S), D1창고(T), D1지식산업센터(U), D1토지/임야(G)
	 * 4. 매매 : A1재개발(L)
	 * todo: 상가/업무/공장/토지 > 기타  => 기타 안쓴다고 함
	 */
	setTrdType: function (id, memulType1, memulType2) {
		switch (memulType1) {
			case "A1": //1. 아파트/오피스텔
				if (memulType2 === "A") {
					COMM.bindCodeFromSelect(id, trdTypeList, '', '', '선택');
				} else if (memulType2 === "C" || memulType2 === "H" || memulType2 === "J" || memulType2 === "O5") {
					COMM.bindCodeFromSelect(id, trdTypeList, '', ['R1'], '선택');
				} else if (memulType2 === "B" || memulType2 === "D" || memulType2 === "K" || memulType2 === "V") {
					COMM.bindCodeFromSelect(id, trdTypeList, '', ['B1', 'B2'], '선택');
				} else if (memulType2 === "L") {
					COMM.bindCodeFromSelect(id, trdTypeList, '', ['B1', 'B2', 'R1'], '선택');
				} else {
					COMM.bindCodeFromSelect(id, trdTypeList, '', '', '선택');
				}
				break;
			case "B1": //2. 원룸
					COMM.bindCodeFromSelect(id, trdTypeList, '', ['R1'], '선택');
				break;
			case "C1": //3. 빌라/주택
				if (memulType2 === "O2" || memulType2 === "O" || memulType2 === "O3" || memulType2 === "M") {
					COMM.bindCodeFromSelect(id, trdTypeList, '', ['R1'], '선택');
				} else if (memulType2 === "Q") {
					COMM.bindCodeFromSelect(id, trdTypeList, '', ['B1', 'B2'], '선택');
				} else {
					COMM.bindCodeFromSelect(id, trdTypeList, '', '', '선택');
				}
				break;
			case "D1": //4. 상가/업무/공장/토지
				if (memulType2 === "E" || memulType2 === "P") {
					COMM.bindCodeFromSelect(id, trdTypeList, '', ['R1'], '선택');
				} else if (memulType2 === "F" || memulType2 === "I" || memulType2 === "R"
					|| memulType2 === "S" || memulType2 === "T" || memulType2 === "U" || memulType2 === "G") {
					COMM.bindCodeFromSelect(id, trdTypeList, '', ['B1', 'B2'], '선택');
				} else {
					COMM.bindCodeFromSelect(id, trdTypeList, '', '', '선택');
				}
				break;
			case "E1": //5.신축빌라분양
				COMM.bindCodeFromSelect(id, trdTypeList, '', '', '선택');
				break;
		}
	},
}

var UTIL = {
	comAjax: function(config) {
		var defaultConf = {
			type: 'POST',
			dataType: 'json',
			contentType: 'application/json;charset=UTF-8',
			beforeSend : function(xhr){
				xhr.setRequestHeader(_csrf_header, _csrf_token);
			},
			error      : function(jqXHR, textStatus, errorThrown){
				COMM.alertPopup('시스템에러가 발생했습니다.</br>관리자에게 문의하세요.', function() {}, '시스템 에러');
				console.log(jqXHR);
				console.log(jqXHR.status);
				if (jqXHR.status == 401) {
					$('#logoutForm').submit();
				}
				// 로딩종료
				COMM.modalLoadingHide();
			}
		};
		var mergeConf = $.extend(true, {}, defaultConf, config);
		$.ajax(mergeConf);
	},
	/**
	 * textLenCheck 		: 글자 수 계산 함수
	 * @param targetId		: 체크할 ID
	 * @param maxLength  	: 제한 글자 수
	 * @param displayId		: 글자 수를 보여줄 ID
	 */
	textLenCheck : function(targetId , maxLength, displayId ){
		var targetTextVal = $("#"+targetId).val();
		var length = targetTextVal.length;
		if(length > maxLength){
			COMM.alertPopup(maxLength+"자를 초과 입력할 수 없습니다. <br> 초과된 내용은 자동으로 삭제 됩니다.");
			var _targetText = targetTextVal.substring(0 , maxLength);
			$("#"+targetId).val(_targetText);
			$("#"+displayId).text(maxLength);
			return;
		}
		$("#"+displayId).text(length);
	},
	/*
	 * input박스 onkeyup 이벤트시 해당 _obj 체크해제
	 */
	nonChecked: function(_obj) {
		$("#"+_obj).attr("checked", false);
	},
	/*
	 * 한글입력 제외
	 */
	disableHanType: function(_obj) {
		var pattern = /[^0-9]/gi;
		if (pattern.test($("#"+_obj).val())) {
			$("#"+_obj).val('');
			return false;
		}

		if(_obj == 'non_realNewPrice') {
			if($("#"+_obj).val() == 0) {
				$("input[name=callRequest]").prop("checked", true);
			} else {
				$("input[name=callRequest]").prop("checked", false);
			}
		}
	},
	/*
	 * 숫자 -> 한글
	 */
	price2text: function (_val, _fieldname, _setPosition) {
		_val = _val + "";
		var validData = /[0-9]/;
		var _strVal;
		var _minusYn;

		if (_val.indexOf('-') >= 0) {
			_minusYn = '-';
		} else {
			_minusYn = '';
		}

		_strVal = UTIL.replaceChar(_val,'-','');

		if (validData.test(_strVal) != true) {
			document.getElementById(_fieldname).innerHTML = "-";
			return false;
		}

		var strNumber = _strVal.replace(new RegExp(",", "g"), "");
		var arrayAmt  = new Array("일", "이", "삼", "사", "오", "육", "칠", "팔", "구", "십");
		var arraypos  = new Array("", "십", "백", "천");
		var arrayUnit = new Array("", "만", "억", "조");

		var pos = strNumber.length%4; //자리수
		var len = (1 + strNumber.length*0.25).toString();

		if( len.indexOf(".") > 0 ) {
			var unit = len.substring(0, len.indexOf(".")); //단위(0:일단위, 1:만단위...)
		} else {
			var unit = strNumber.length / 4;
		}
		var korNumber = "";
		var op = 0;

		for( i=0; i<strNumber.length; i++ ) {
			if(pos==0) {pos=4;}
			var num = parseInt( strNumber.substring( i, i+1 ) );

			if( num != 0 ) {
				korNumber += arrayAmt[ num-1 ];
				korNumber += arraypos[ pos-1 ];
				op=1;
			}
			if(pos == 1) {
				if(op == 1) korNumber += arrayUnit[unit];
				unit--;
				op = 0;
			}
			pos--;
		}

		if (document.getElementById(_fieldname)) {
			if (_setPosition == "val") {
				if (korNumber.length == 0 || korNumber.length == null) {
					document.getElementById(_fieldname).innerHTML = "-";
				} else {
					document.getElementById(_fieldname).value = _minusYn + korNumber;
				}
			} else if (_setPosition == "txt") {
				if (korNumber.length == 0 || korNumber.length == null) {
					document.getElementById(_fieldname).innerHTML = "-";
				} else {
					document.getElementById(_fieldname).innerHTML = _minusYn + korNumber;
				}
			} else {
				return _minusYn + korNumber
			}
		} 
	},
	/*
	 * 숫자 -> 한자
	 */
	price2HanJa: function (_val, _fieldname, _setPosition) {
		var validData = /[0-9]/;
		var _strVal;
		var _minusYn;

		if (_val.indexOf('-') >= 0) {
			_minusYn = '-';
		} else {
			_minusYn = '';
		}

		_strVal = UTIL.replaceChar(_val, '-', '');

		if (validData.test(_strVal) != true) {
			document.getElementById(_fieldname).innerHTML = "-";
			return false;
		}

		var strNumber = _strVal.replace(new RegExp(",", "g"), "");
		var arrayAmt = new Array("壹", "貳", "參", "四", "五", "六", "七", "八", "九", "拾");
		var arraypos = new Array("", "拾", "百", "千");
		var arrayUnit = new Array("", "萬", "億", "兆");

		var pos = strNumber.length % 4; //자리수
		var len = (1 + strNumber.length * 0.25).toString();

		if (len.indexOf(".") > 0) {
			var unit = len.substring(0, len.indexOf(".")); //단위(0:일단위, 1:만단위...)
		} else {
			var unit = strNumber.length / 4;
		}
		var HanJaNumber = "";
		var op = 0;

		for (i = 0; i < strNumber.length; i++) {
			if (pos == 0) { pos = 4; }
			var num = parseInt(strNumber.substring(i, i + 1));

			if (num != 0) {
				HanJaNumber += arrayAmt[num - 1];
				HanJaNumber += arraypos[pos - 1];
				op = 1;
			}
			if (pos == 1) {
				if (op == 1) HanJaNumber += arrayUnit[unit];
				unit--;
				op = 0;
			}
			pos--;
		}

		if (document.getElementById(_fieldname)) {
			if (_setPosition == "val") {
				if (HanJaNumber.length == 0 || HanJaNumber.length == null) {
					document.getElementById(_fieldname).value = "-";
				} else {
					document.getElementById(_fieldname).value = _minusYn + HanJaNumber;
				}
			} else if (_setPosition == "txt") {
				if (HanJaNumber.length == 0 || HanJaNumber.length == null) {
					document.getElementById(_fieldname).innerHTML = "-";
				} else {
					document.getElementById(_fieldname).innerHTML = _minusYn + HanJaNumber;
				}
			} else {
				return _minusYn + HanJaNumber;
			}
		}
	},
	replaceChar: function(str, fChar,rChar) {
        str = str.replace(/\,/gi, '')
        var tar = '';
        var len = str.length;
        for (var i = 0; i < len; i++) {
            if (str.charAt(i) == fChar)
                tar += rChar;
            else
                tar += str.charAt(i);
        }
        return tar;
    },
	/*
	 * 숫자 - 가격 (억,조,경 단위만)
	 */
	numberToKorean: function(number, flag){
		var inputNumber  = number < 0 ? false : number;
		var unitWords    = ['만', '억', '조', '경'];
		var splitUnit    = 10000;
		var splitCount   = unitWords.length;
		var resultArray  = [];
		var resultString = "";
		var br           = "";

		for (var i = 0; i < splitCount; i++){
			var unitResult = (inputNumber % Math.pow(splitUnit, i + 1)) / Math.pow(splitUnit, i);
			unitResult = Math.floor(unitResult);
			if (unitResult > 0){
				resultArray[i] = unitResult;
			}
		}

		if(flag) { br = "<br/>"; }

		for (var i = 0; i < resultArray.length; i++){
			if(!resultArray[i]) continue;
			resultString = String(resultArray[i]) + unitWords[i] + br + resultString;
		}

		if(COMM.isEmpty(resultString)) {
			resultString = 0;
		}

		return resultString;
	},

	/*
	 * 두개의 input 값 SUM
	 * 숫자만 가능,그외 입력 불가능
	 */
	setAptiSalePrice: function() {
		UTIL.disableNumType('isalePrice');
		UTIL.disableNumType('isalePremium');
		var _iSalePrice   = $('#isalePrice').val().replace(/\,/gi,'') == '' ? _iSalePrice = 0 : _iSalePrice = $('#isalePrice').val().replace(/\,/gi,'');
		var _iSalePremium = $('#isalePremium').val() == '' || $('#isalePremium').val() == '-' ? _iSalePremium = 0 : _iSalePremium = $('#isalePremium').val().replace(/\,/gi,'');
		$('#isalePriceSum').val(COMM.numberWithCommas(parseInt(_iSalePrice) + parseInt(_iSalePremium)));
		$('#isalePriceSum').trigger('keyup');
	},
	
	/*
	 * 코드값을 코드명으로 변환
	 * _store: codeList, _val:코드값
	 */
	getCodeValue: function(_store, _val){
		var result = "";
		$.each(_store, function (idx, el) {
			if(el.CODE_VALUE == _val) {
				result = el;
				return false;
			}
		});
		if(!COMM.isEmpty(result)) {
			return result.CODE_NAME;
		}
		return '-';
	},

	/*
	 * 배열 코드값을 코드명으로 변환
	 * _store: codeList, _valArray:코드 배열값, _separator: 받을 값
	 */
	getArrayCodeValue: function(_store, _valArray, _separator){
		var result 	  = _store;
		var resultPre = "0";
		for(var i = 0 ; _valArray.length > i ; i++) {
			if(!COMM.isEmpty(_valArray[i])) {
				$.each(result, function (idx, el) {
					if(el.CODE_VALUE == _valArray[i]) {
						result = el;
						return false;
					}
				});

				if((i+1) == _valArray.length) {
					if(!COMM.isEmpty(result)) {
						if(_separator== 'idx') {
							return result.CODE_IDX;
						}
						return result.CODE_NAME;
					}
				} else {
					if(!COMM.isEmpty(result)) {
						if(_separator== 'idx') {
							resultPre = result.CODE_IDX;
						} else {
							resultPre = result.CODE_NAME;
						}
						result = result.CHILD_LIST;
					}
				}
			} else {
				return 0;
			}
		}
		return resultPre;
	},

	/*
	 * keyCheck 자릿수 체크 및 특수문자 제외
	 * _obj       : this
	 * _target    : 글자수 체크할 타겟 아이디
	 * _targetCnt : 글자수 등록할 class명
	 * _cnt       : 제한글자수
	 */
	keyCheck: function(_target, _targetCnt, _cnt) {
		UTIL.textCnt(_target, _targetCnt, _cnt);
		var _val = $("#"+_target).val();
		var result = UTIL.regExp(_val);
		$("#"+_target).val(result);
	},

	/*
	 * text 자릿수 체크
	 * _obj       : this
	 * _targetCnt : 글자수 등록할 class명
	 * _cnt       : 제한글자수
	 */
	textCnt: function(_target, _targetCnt, _cnt){
		var _val = $("#"+_target).val();
		$("."+_targetCnt).html(_val.length);

		if(_val.length > _cnt) {
			var txt = _val.substring(0, _cnt);
			$("#"+_target).val(txt);
			$("."+_targetCnt).html(_cnt);
		}
	},

	/*
	 * 특수문자 제외 정규화 (마침표: . , 쉼표: , 제외)
	 */
	regExp: function(str){
		var reg = /[\{\}\[\]\/?;:|\)*~`!^\-+<>@\#$%&\\\=\(\'\"]/gi;
		//특수문자 검증
		if(reg.test(str)){
			//특수문자 제거후 리턴
			return str.replace(reg, "");
		} else {
			//특수문자가 없으므로 본래 문자 리턴
			return str;
		}
	},

	//string -> array 처리
	splitData: function(_val) {
		if(!COMM.isEmpty(_val)) {
			return _val.split("$");
		}
		return [];
	},

	//단독 checkbox checked
	checkedData: function(_arr) {
		$.each(_arr, function(idx, el) {
			$("input[name='"+el+"']").prop("checked", true);
		});
	},

	/*
	 *selectbox readonly
	 * 선택된 값 제외하고 disabled시킴
	 */
	
	selectReadOnly: function(_tag) {
		$("#"+_tag).attr("readonly", true);
		$("#"+_tag+" option").not(":selected").attr("disabled", "disabled");
	},

	priceDiff: function(_price, _val, _txt) {
        _val = UTIL.replaceChk(_val);
        var _up = (_price + (_price * 20 / 100));
        var _down = (_price - (_price * 20 / 100));

        if (_up < Number(_val) || Number(_val) < _down) {
            COMM.alertPopup(_txt + " (은/는) ±20%까지만 수정가능합니다.");
            return false;
        }
        return true;
    },
    //replace체크
    replaceChk: function (_val) {
		var special_pattern = /[,]/gi;
		if(special_pattern.test(_val) == true) {
			_val = _val.replace(/\,/gi, '');
		}
        return _val;
    },
    // 소수점 버림
    humanFileSize: function (size) {
        var i = size == 0 ? 0 : Math.floor(Math.log(size) / Math.log(1024));
        var floorSize = Math.floor((size / Math.pow(1024, i)).toFixed(2) * 1);
        return floorSize + ' ' + ['B', 'KB', 'MB', 'GB', 'TB'][i];
    },

    arraytoStringSep: function (_arr) {
        var txt = "";
        $.each(_arr, function (idx, el) {
			$("input[name='"+el+"']").prop("checked", true);
			txt += '\'' + el + '\'\,';
		});
		return txt.substring(0, (txt.length-1));
	},
	
	// 특수문자, 특정문자열(sql예약어의 앞뒤공백포함) 제거
	fnCheckSearchedWord : function(obj) {
		if (obj.value.length > 0) {
			// 특수문자 제거
			var expText = /[%=><]/;
			if (expText.test(obj.value) == true) {
				COMM.alertPopup("특수문자를 입력 할수 없습니다.", function() {});
				obj.value = obj.value.split(expText).join("");
				return false;
			}

			// 특정문자열(sql예약어의 앞뒤공백포함) 제거
			var sqlArray = new Array(
			// sql 예약어
			"OR", "SELECT", "INSERT", "DELETE", "UPDATE", "CREATE", "DROP", "EXEC", "UNION", "FETCH", "DECLARE", "TRUNCATE");

			var regex;
			for (var i = 0; i < sqlArray.length; i++) {
				regex = new RegExp(sqlArray[i], "gi");

				if (regex.test(obj.value)) {
					COMM.alertPopup("\"" + sqlArray[i] + "\"와(과) 같은 특정문자로 검색할 수 없습니다.", function() {});
					obj.value = obj.value.replace(regex, "");
					return false;
				}
			}
		}
		return true;
	},

	/*
	* 주민등록번호 마스킹
	* setId : input id
	* str : 주민번호(maxlength='7', maxlength='13')
	* type : full전체번호, part뒷번호만 적용
	*/
	rrnMasking: function (setId, str, type) {
		var originStr = str;
		var rrnStr;
		var maskingStr;

		if (!COMM.isEmpty(originStr)) {
			// 숫자, 특수문자'*' 만 입력 가능
			var reg = /[^0-9*]/g;
			if (reg.test(originStr)) {
				$("#" + setId).val(originStr.replace(reg, ""));
			}

			rrnStr = (type == "full") ? originStr.match(/\d{13}/gi) : originStr.match(/\d{7}/gi);
			if (!COMM.isEmpty(rrnStr)) {
				maskingStr = originStr.toString().replace(rrnStr, rrnStr.toString().replace(/([0-9]{6})$/gi, "******"));
			} else {
				return originStr;
			}

			$("#" + setId).val(maskingStr).attr("data-value", originStr);
		}
	},
	
	// 숫자 개수 세기
	fnCountDigit : function(value) {
		var digitCnt = 0;
		for (var i = 0; i < value.length; i++) {
			if (isNaN(value.charAt(i)) == false) {
				digitCnt++;
			}
		}
		return digitCnt;
	},
	
	// 값에서 특수문자에 없는 문자 개수 세기 
	fnNotAllowSpecialChar : function(value, specialChars) {
		var notAllowedSpecialCharCnt = 0;
		for (var i = 0; i < value.length; i++) {
			if (specialChars.indexOf(value.charAt(i) + "") < 0) {
				notAllowedSpecialCharCnt++;
			}
		}
		return notAllowedSpecialCharCnt > 0;
	},
	
	// 특수문자 개수 세기
	fnCountSpecialChar : function(value, specialChar) {
		var specialCharCnt = 0;
		for (var i = 0; i < value.length; i++) {
			if (specialChar == (value.charAt(i) + "")) {
				specialCharCnt++;
			}
		}
		return specialCharCnt;
	},
	
	// 특수문자 종류 개수 세기
	fnCountDistinctSpecialChar : function(value, specialChars) {
		var distinctSpecialCharCnt = 0;
		for (var i = 0; i < specialChars.length; i++) {
			if (value.length > 0) {
				var specialCharCnt = UTIL.fnCountSpecialChar(value, (specialChars.charAt(i) + ""));
				if (specialCharCnt > 0) {
					distinctSpecialCharCnt++;
					value = value.replaceAll((specialChars.charAt(i) + ""), "");
				}
			}
		}
		return distinctSpecialCharCnt;
	},
	
	// 연속된 숫자 또는 동일한 숫자 확인
	fnPwNumIsValid : function(str, max) {
		if (!max) {
			max = 4; // 글자수를 지정하지 않으면 4로 지정
		}
		var i, j, k, x, y;
		var buff = ["0123456789"];
		var src, src2, ptn = "";
	
		for (i = 0; i < buff.length; i++) {
			src = buff[i]; // 0123456789
			src2 = buff[i] + buff[i]; // 01234567890123456789
			for (j = 0; j < src.length; j++) {
				x = src.substr(j, 1); // 0
				y = src2.substr(j, max); // 0123
				ptn += "[" + x + "]{" + max + ",}|"; // [0]{4,}|0123|[1]{4,}|1234|...
				ptn += y + "|";
			}
		}
		ptn = new RegExp(ptn.replace(/.$/, "")); // 맨마지막의 글자를 하나 없애고 정규식으로 만든다.
	
		if (ptn.test(str)) {
			return true;
		}
		return false;
	},
	
	// 동일한 숫자 확인
	fnLoopNum : function(str, max) {
		var tmp = 0;
		var loopCnt = 0;
		for (var i = 0; i < str.length; i++) {
			if (str.charAt(i) == tmp) {
				loopCnt++;
			} else {
				loopCnt = 0;
			}
			if (loopCnt == (max - 1)) {
				return true;
			}
			tmp = str.charAt(i);
		}
		return false;
	},
	
	// 연속된 숫자 확인
	fnContinuosNum : function(str, max) {
		var tmp = 0;
		var reverseLoopCnt = 0;
		var loopCnt = 0;
		for (var i = 0; i < str.length; i++) {
			var gap = str.charAt(i) - tmp;
			if (gap == 1) {
				reverseLoopCnt = 0;
				loopCnt++;
			} else if (gap == -1) {
				reverseLoopCnt++;
				loopCnt = 0;
			} else {
				reverseLoopCnt = 0;
				loopCnt = 0;
			}
			if (loopCnt == (max-1) || reverseLoopCnt == (max-1)) {
				return true;
			}
			tmp = str.charAt(i);
		}
		return false;
	},
	
	//면적 평수 계산
	areaToCalculation: function(_val, _cnt) {
		return (Number(_val) / 3.3058).toFixed(_cnt);
	},
	//면적 변환
	areaToCalculationChk: function(_val, _cnt, _tag) {
		_val = UTIL.removeSep(_val);
		_val = UTIL.replaceChk(_val);
        var _chVal = UTIL.areaToCalculation(_val, _cnt);
        $("#" + _tag).val(_chVal);
    },

	removeSep: function(_val) {
		var special_pattern = /[\.]/gi;
		if(special_pattern.test(_val) == true) {
			var _valArr = [];
			var _valRt  = 0;
			_valArr = _val.toString().split(".");
			for (var i = 0; i < _valArr.length; i++) {
				if (i == 0) {
					_valRt = _valArr[i] + '.'
				} else {
					_valRt = _valRt + _valArr[i];
				}
			}
		} else {
			_valRt = _val
		}
		return _valRt;
	},
	//배열 -> string
	arrayToString: function(arr, sep, flag) {
		var txt= "";
		$.each(arr, function (index, item) {
			if(!COMM.isEmpty(sep)) {
				txt += "'" + item + "',";
			} else {
				txt += item + ",";
			}
		});

		if(flag) {
			txt = txt.substring(0, (txt.length-1));
		}
		return txt;
	},
    /*
     * 숫자, ','만입력
     */
    disableNumType: function (_obj) {
        var pattern = /^[\,0-9]*$/; // 숫자와 , 만 가능
        if (!pattern.test($("#" + _obj).val())) {
            $("#" + _obj).val('');
            return false;
        }
    },
    /*
     * 숫자, ',', '.'만입력
     */
    disableAddNumType: function (_obj) {
        var pattern = /^[\,.0-9]*$/; // 숫자와 ',', '.' 만 가능
        if (!pattern.test($("#" + _obj).val())) {
            $("#" + _obj).val('');
            return false;
        }
    },
    //숫자형 데이터 3자리수 ',' 찍어 해당 id에 표현
    commaAdd: function (_num, _tag) {
		_num = UTIL.removeSep(_num);
        _num = UTIL.replaceChk(_num);
        _num = COMM.numberWithCommas(_num);
        $("#" + _tag).val(_num);
    },
    nullChk: function (_tag1, _tag2) {
        var _val = $("#" + _tag1).val();
        if (_val.length == 0) {
            $("input[name=" + _tag2 + "]:eq(0)").prop("checked", true);
        } else {
            $("input[name=" + _tag2 + "]").each(function (i, el) {
                $(this).prop("checked", false);
            });
        }
    },
	//해당층 총층 비교
	floorChk: function(_val1, _val2, _tag) {
		var _val1 = $("#"+_val1).val();
		var _val2 = $("#"+_val2).val();
		if(COMM.isNotEmpty(_val1) && COMM.isNotEmpty(_val2)) {
			if (_val1.indexOf('B') >= 0) {
				_val1 = Number(_val1.replace("B", "-"));
			}
			_val2 = Number(_val2);
			if(_val1 > _val2) {
				COMM.alertPopup("해당층이 총층 보다 높습니다. 다시 확인해 주세요.");
			}

			var val = "저";
			if(_val1 > 0) {
				var avg = Number(10/_val2*_val1);
				if(avg > 7) {
					val = "고";
				} else if(avg > 4) {
					val = "중";
				}
			}
			COMM.bindCodeFromRadio(_tag+'floorLevel'     , floorLevelList   , _tag+'floorLevel'     , [val]    ,     ''); //층레벨 옵션추가
		}
	},
	//방콜 희망조건 나이 비교
	ageChk: function(_val1, _val2) {
		var _val1 = $("#"+_val1).val();
		var _val2 = $("#"+_val2).val();
		if(COMM.isNotEmpty(_val1) && COMM.isNotEmpty(_val2)) {
			_val1 = Number(_val1);
			_val2 = Number(_val2);
			if(_val1 > _val2) {
				COMM.alertPopup("최소나이가 최대나이보다 높습니다. 다시 확인해 주세요.");
			}
		}
	},
	//공급//전용 비교
	areaMChk: function() {
    	var non_supplyAreaM    = $("#non_supplyAreaM").val();
		var non_exclusiveAreaM = $("#non_exclusiveAreaM").val();
		var step1 = $("#btnTypes1val").val(); //매물종류 I
		var step2 = $("#btnTypes2val").val(); //매물종료 II
		var flag  = false;

		if(step1 == 'B1' || step1 == 'E1') { //원룸, 신축빌라분양
			flag = true;
		} else if(step1 == 'C1') {
			if(step2 == 'O2') { //빌라/연립 전체
				flag = true;
			}
		} else if(step1 == 'D1') { // 상가/업무/공장/토지
			if(step2 == 'E' || step2 == 'P' || step2 == 'U') { //상가점포 전체, 사무실 전체, 지식산업센터
				flag = true;
			}
		}

		if(flag) {
			if(COMM.isNotEmpty(non_supplyAreaM) && COMM.isNotEmpty(non_exclusiveAreaM)) {
				non_supplyAreaM =  Number(UTIL.replaceChk(non_supplyAreaM));
				non_exclusiveAreaM =  Number(UTIL.replaceChk(non_exclusiveAreaM));
				if(Number(non_exclusiveAreaM) > Number(non_supplyAreaM)) {
					COMM.alertPopup("전용면적이 공급(계약) 면적 보다 </br>크게 입력되었습니다. </br>전용면적을 다시 확인해 주세요.");
					// UTIL.areaToCalculationChk(0, 2, 'non_exclusiveAreaP');
					// UTIL.disableAddNumType('non_exclusiveAreaM');
					// UTIL.commaAdd(0, 'non_exclusiveAreaM');
					$("#non_exclusiveAreaM").val('');
					$("#non_exclusiveAreaP").val('');
					return false;
				}

				if(step1 == 'D1') { // 상가/업무/공장/토지
					if(step2 == 'E' || step2 == 'P') { //상가점포 전체, 사무실 전체
						UTIL.areaToCalculationChk(non_supplyAreaM, 2, 'non_contractInsP');
						UTIL.disableAddNumType('non_contractInsM');
						UTIL.commaAdd(non_supplyAreaM, 'non_contractInsM');
					}
				}
			}
		}
	},
	toFixedChk: function(_val, num) {
    	var result = "";
		var special_pattern = /[\.]/gi;
		var arr = [];

		if(special_pattern.test(_val) == true) {
			arr = _val.split('.');
			result = arr[0] + "." + arr[1].substring(0, num);
		} else {
			result = _val;
		}
    	return result;
	},
	//한글,영문,숫자만
	regExpChk: function(_val, _tag) {
		var reg = /^[ㄱ-ㅎ|가-힣|a-z|A-Z|0-9|]+$/;
		//특수문자 검증
		if(!reg.test(_val)) {
			_val = _val.substring(0, _val.length-1);
		}
		$("#"+_tag).val(_val);
	}
}