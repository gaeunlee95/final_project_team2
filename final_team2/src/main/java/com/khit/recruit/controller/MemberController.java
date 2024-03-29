package com.khit.recruit.controller;

import java.io.IOException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.khit.recruit.config.SecurityCompany;
import com.khit.recruit.config.SecurityUser;
import com.khit.recruit.dto.CompanyDTO;
import com.khit.recruit.dto.JobDTO;
import com.khit.recruit.dto.MemberDTO;
import com.khit.recruit.entity.Resume;
import com.khit.recruit.service.JobService;
import com.khit.recruit.service.MemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/member")
@RequiredArgsConstructor
@Controller
public class MemberController {
	
	private final MemberService memberService;
	
	private final JobService jobService;

	
	@GetMapping("/identity")
	public String certified() {
		return "member/identity";
	}
	
	@GetMapping("/terms")
	public String terms() {
		return "member/terms";
	}
	
	@GetMapping("/mjoin")
	public String mjoinForm(MemberDTO memberDTO) {
		return "member/mjoin";
	}
	
	@PostMapping("/mjoin")
	public String mjoin(MemberDTO memberDTO) {
		memberService.msave(memberDTO);
		return "redirect:/member/login";
	}
	
	@GetMapping("/cpjoin")
	public String confirm() {
		return "member/cpjoin";
	}
	
	@PostMapping("/cpjoin")
	public String cpjoinForm(CompanyDTO companyDTO) {
		memberService.cpsave(companyDTO);
		return "redirect:/member/login";
	}
	
	@GetMapping("/login")
	public String login() {
		return "member/login";
	}
	
	@GetMapping("/mypage")
	public String myPage(@AuthenticationPrincipal SecurityUser principal, Model model) {
		MemberDTO memberDTO = memberService.findById(principal.getMember().getMid());
		model.addAttribute("member", memberDTO);
		return "member/mypage";
	}
	
	@PostMapping("/profileUpdate")
	public String profileUpdate(
			@RequestParam(name = "memberFile", required = false) MultipartFile memberFile,
			Authentication authentication) throws Exception {
		//로그인한 유저의 mid
		SecurityUser userDetails = (SecurityUser) authentication.getPrincipal();
	    Long mid = userDetails.getMember().getMid();
	    //mid를 통해 로그인한 유저의 정보 받아옴
	    MemberDTO memberDTO = memberService.findByMid(mid);
	    memberDTO = memberService.update(memberDTO, memberFile);
		
	    return "redirect:/member/mypage";
	}
	
	@GetMapping("/cpmypage")
	public String cpmyPage(@AuthenticationPrincipal SecurityCompany principal, Model model) {
		CompanyDTO companyDTO = memberService.findByCId(principal.getCompany().getCid());
		model.addAttribute("company", companyDTO);
		return "member/cpmypage";
	}
	
	@PostMapping("/cpProfileUpdate")
	public String cpProfileUpdate(
			@RequestParam(name = "companyFile", required = false) MultipartFile companyFile,
			@AuthenticationPrincipal SecurityCompany principal) throws Exception {
		//로그인한 유저의 정보를 받아옴
		CompanyDTO companyDTO = memberService.findByCId(principal.getCompany().getCid());;
		companyDTO = memberService.update(companyDTO, companyFile);
		
	    return "redirect:/member/cpmypage";
	}
	

	
	
	@GetMapping("/announ")
	public String announ() {
		return "member/announ";
	}
	
	//지원 리스트
	@GetMapping("/resume")
	public String GetJobAppPageList(
			@PageableDefault(page = 1) Pageable pageable,
			Model model) {
			
			Page<Resume> resumeList = null;
			resumeList = memberService.findListAll(pageable);
			log.info("ResumeList : " + resumeList );
			
			//하단의 페이지 블럭 만들기
			int blockLimit = 10;  //하단에 보여줄 페이지 개수
			//시작 페이지 1, 11, 21    12/10 = 1.2 -> 2.2 -> 2-1, 1*10+1 =11
			int startPage = ((int)(Math.ceil((double)pageable.getPageNumber()/blockLimit))-1)*blockLimit+1;
			//마지막 페이지 10, 20, 30 //12page -> 12 마지막
			int endPage = (startPage+blockLimit-1) > resumeList.getTotalPages() ?
					resumeList.getTotalPages() : startPage+blockLimit-1;
			endPage = Math.max(endPage, startPage); // 마지막 페이지는 시작 페이지와 같거나 큼
			
			model.addAttribute("resumeList", resumeList);
			model.addAttribute("startPage", startPage);
			model.addAttribute("endPage", endPage);
		return "member/resume";
	}
	
	//* 회원정보 수정 */
	@GetMapping("/update/{mid}")
	public String updateForm(@PathVariable Long mid, Model model,
							 @AuthenticationPrincipal SecurityUser principal) {
		MemberDTO memberDTO = memberService.findById(mid);
		model.addAttribute("member", memberDTO);
		return "member/detail";
	}
	
	@PostMapping("/update")
	public String update(@ModelAttribute MemberDTO memberDTO) {
		memberService.update(memberDTO);
		
		return "redirect:/" + memberDTO.getMid();
	}
	
	/* 아이디 중복검사 부분 */
	//아이디 중복 검사
	@PostMapping("/check-id")
	public @ResponseBody String checkId(@RequestParam("memberId") String memberId) {
		String resultText = memberService.memberIdCheck(memberId);
		return resultText;
	}
	
	@ResponseBody
	@GetMapping("/auth/kakao/callback")
	public String kLogin(String code) {
		return "카카오 로그인 성공!: " + code;
	}
	
	
	
}