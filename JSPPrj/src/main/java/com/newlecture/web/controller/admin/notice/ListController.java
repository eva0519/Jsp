package com.newlecture.web.controller.admin.notice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.newlecture.web.entity.NoticeView;
import com.newlecture.web.service.NoticeService;

@WebServlet("/admin/board/notice/list")
public class ListController extends HttpServlet{
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String[] openIds = request.getParameterValues("open-id");
		String[] delIds = request.getParameterValues("del-id");
		String cmd = request.getParameter("cmd");
		String ids_ = request.getParameter("ids");
		String[] ids = ids_.trim().split(" ");
		// jsp에서 받아올 때 맨앞에도 빈공백이 하나 더 있어서 [ , id1, id2...] 이런식으로 나와버림
		// trim으로 양쪽 여백을 지운 상태에서 split 해야함 value="${ids} ${n.id}" 여기서 ids n.id 사이에 
		// 여백이 0번째에도 적용되서 그런 듯
		
		NoticeService service = new NoticeService();
		
		switch(cmd) {
		case "일괄공개":
			for(String openId : openIds)
				System.out.printf("open id : %s\n", openId);
			
			List<String> oids = Arrays.asList(openIds);
			List<String> cids = new ArrayList(Arrays.asList(ids));
			cids.removeAll(oids);
			//바로 Arrays.asList(ids).removeAll(oids) 하는 건 정적배열 상태라 안됨
			//그렇기에 바로 삭제나 추가를 할 수 없어 새로운 List에 담아 removeAll 해야함 
			System.out.println(ids_);
			System.out.println(Arrays.asList(ids));
			System.out.println(oids);
			System.out.println(cids);
			
			// 실제 Transaction 처리 부분이다
			// service.pubNoticeList(openIds);
			// service.closeNoticeList(cids);
			// 일괄공개 기능이라는 한가지의 논리적 기능을 수행하지만 DB로 쿼리 명령문을 전달하는 동작은 두 함수에 걸쳐 두번 실행된다
			// 어떠한 영향으로 둘중 하나의 동작만 실행 될 경우 그것이 Transaction 처리를 못한 것이 된다
			service.pubNoticeAll(oids, cids);
			// 바람직한 Transaction 처리 (이지만 서비스메소드 부분에서 아직 처리하지 않고 있다)
			
			break;
			
		case "일괄삭제":
			int[] ids1 = new int[delIds.length];
			for(int i=0; i<delIds.length;i++)
				ids1[i] = Integer.parseInt(delIds[i]);
			
			int result = service.deleteNoticeAll(ids1);
			break;
		}
		
		// post 처리 후 자신의 get요청을 호출해 url 재요청
		response.sendRedirect("list");
		
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String field_ = request.getParameter("f");
		String query_ = request.getParameter("q");
		String page_ = request.getParameter("p");
		
		String field = "title";
		if (field_ != null && !field_.equals(""))
			field = field_;

		String query = "";
		if (query_ != null && !query_.equals(""))
			query = query_;
		
		int page = 1;
		if (page_ != null && !page_.equals(""))
			page = Integer.parseInt(page_);
		
		NoticeService service = new NoticeService();
		List<NoticeView> list = service.getNoticeList(field, query, page);
		int count = service.getNoticeCount(field, query);

		request.setAttribute("list", list);
		request.setAttribute("count", count);
		
		request.getRequestDispatcher("/WEB-INF/view/admin/board/notice/list.jsp").forward(request, response);
		
	}
	
}
