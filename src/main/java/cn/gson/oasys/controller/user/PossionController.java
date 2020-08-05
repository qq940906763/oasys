package cn.gson.oasys.controller.user;

import java.util.List;

import cn.gson.oasys.model.dao.user.UserDao;
import cn.gson.oasys.model.entity.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import cn.gson.oasys.model.dao.user.DeptDao;
import cn.gson.oasys.model.dao.user.PositionDao;
import cn.gson.oasys.model.entity.user.Dept;
import cn.gson.oasys.model.entity.user.Position;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/")
public class PossionController {
	
	@Autowired
	PositionDao pdao;
	@Autowired
	DeptDao ddao;

	@Autowired
	UserDao udao;
	
	@RequestMapping("positionmanage")
	public String positionmanage(HttpServletRequest req, Model model) {
		
		//List<Position> positions = (List<Position>) pdao.findAll();
		HttpSession session = req.getSession();
		if(StringUtils.isEmpty(session.getAttribute("userId"))){
			return "login/login";
		}
		Long userId = Long.parseLong(session.getAttribute("userId") + "");
		User user=udao.findOne(userId);
		List<Position> positions = (List<Position>) pdao.findBySysId(user.getDept().getSysId());

		model.addAttribute("positions",positions);
		
		return "user/positionmanage";
	}
	
	@RequestMapping(value = "positionedit" ,method = RequestMethod.GET)
	public String positioneditget(HttpServletRequest req,@RequestParam(value = "positionid",required=false) Long positionid,Model model){
		HttpSession session = req.getSession();
		if(StringUtils.isEmpty(session.getAttribute("userId"))){
			return "login/login";
		}
		Long userId = Long.parseLong(session.getAttribute("userId") + "");
		User user=udao.findOne(userId);
		List<Dept> depts = ddao.findDeptBySysId(user.getDept().getSysId());
		if(positionid!=null){
			
			Position position = pdao.findOne(positionid);
			System.out.println(position);
			Dept dept = ddao.findOne(position.getDeptid());
			model.addAttribute("positiondept",dept);
			model.addAttribute("position",position);
		}
		//List<Dept> depts = (List<Dept>) ddao.findAll();
		model.addAttribute("depts", depts);
		return "user/positionedit";
	}
	
	@RequestMapping(value = "positionedit" ,method = RequestMethod.POST)
	public String positioneditpost(Position position,Model model){
		System.out.println(position);
		
		Position psition2 = pdao.save(position);
		
		if(psition2!=null){
			model.addAttribute("success",1);
			return "/positionmanage";
		}
		
		model.addAttribute("errormess","数据插入失败");
		return "user/positionedit";
	}
	
	
	@RequestMapping("removeposition")
	public String removeposition(@RequestParam("positionid") Long positionid,Model model){
		pdao.delete(positionid);
		model.addAttribute("success",1);
		return "/positionmanage";
	}
	
	
	
}
