package com.todoc.web.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.todoc.web.dto.KakaoPayApprovalVO;
import com.todoc.web.dto.PayLog;
import com.todoc.web.security.jwt.JwtAuthorizationFilter;
import com.todoc.web.service.KakaoPayService;
import com.todoc.web.service.UserService;
import com.todoc.web.util.StringUtil;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping
public class KakaoPayController {
	
	@Autowired
	private KakaoPayService kakaoPayService;

	private final JwtAuthorizationFilter jwtFilter;
	
	public KakaoPayController(JwtAuthorizationFilter jwtFilter){
		this.jwtFilter = jwtFilter;
	}
    
    @PostMapping("/kakaoPay")
    @ResponseBody
    public String kakaoPay() {
        log.info("kakaoPay post............................................");
        
        return kakaoPayService.kakaoPayReady();
 
    }
    
    @GetMapping("/kakaoPaySuccess")
    public String kakaoPaySuccess(@RequestParam("pg_token") String pg_token, Model model) {
        log.info("kakaoPaySuccess get............................................");
        log.info("kakaoPaySuccess pg_token : " + pg_token);

        model.addAttribute("info", kakaoPayService.kakaoPayInfo(pg_token));

        return "pay/kakaoPaySuccess";
    }
    
    @GetMapping("/kakaoPayResult")
    public String kakaoPayResult(Model model, HttpServletRequest request, HttpServletResponse response ) throws ParseException {
    	String token = jwtFilter.extractJwtFromCookie(request);
    	String userEmail = jwtFilter.getUsernameFromToken(token);
    	//String untactSeq = request.getParameter("untactSeq");
    	String untactSeq = "6";
    	String tid = request.getParameter("tid");
    	
    	PayLog payLog = new PayLog();
    	
    	if(!StringUtil.isEmpty(userEmail) && !StringUtil.isEmpty(untactSeq)) {
    		payLog.setPayMethod("카카오페이");
    		payLog.setPayPrice("5500");
    		payLog.setPaySeq(tid);
    		payLog.setUserEmail(userEmail);
    		//todo:마이페이지연결후 고칠 값
    		payLog.setUntactSeq(untactSeq);
    	}
    	
    	int res = kakaoPayService.insertPayLog(payLog);
    	String payDate = payLog.getPayDate();
    	
        model.addAttribute("count", res);
        model.addAttribute("payLog", payLog);
        model.addAttribute("payDate", payDate);
    	return "pay/kakaoPaySuccessResult";
    }
    
	
}
