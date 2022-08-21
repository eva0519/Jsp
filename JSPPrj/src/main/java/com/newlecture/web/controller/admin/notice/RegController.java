package com.newlecture.web.controller.admin.notice;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.newlecture.web.entity.Notice;
import com.newlecture.web.service.NoticeService;

@MultipartConfig(
		  fileSizeThreshold=1024*1024,
		  maxFileSize=1024*1024*50,
		  maxRequestSize=1024*1024*50*5
		  )
@WebServlet("/admin/board/notice/reg")
public class RegController extends HttpServlet{
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		request.getRequestDispatcher("/WEB-INF/view/admin/board/notice/reg.jsp")
		.forward(request, response);
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String title = request.getParameter("title");
		String content = request.getParameter("content");
		String isOpen = request.getParameter("open");
		// checkbox의 open param은 체크시 true, 비체크시 null을 반환한다
		
		Collection<Part> parts = request.getParts();
		StringBuilder builder = new StringBuilder();
		
		for(Part p : parts) {
			if(!p.getName().equals("file")) continue;
			
			Part filepart = p;
			String fileName = filepart.getSubmittedFileName();
			InputStream fis = filepart.getInputStream();
			// 단일 파일, 파일 이름 가져오기
			builder.append(fileName);
			builder.append(",");
			// DB 저장을 위해 파일명들로 문자열 만들기
			
			String realPath = request.getServletContext().getRealPath("/upload");
			String filePath = realPath + File.separator + fileName;
			// 절대 경로 설정하기
			FileOutputStream fos = new FileOutputStream(filePath);
			// 설정한 절대 경로로 파일 출력 경로 설정하기
			
			byte[] buf = new byte[1024];
			int size = 0;
			while((size=fis.read(buf)) != -1)
				fos.write(buf, 0, size);
			// 1 byte 씩 읽어오면 하루종일 걸리니 byte 타입 배열 만들어서 1kb씩 받아오기
			// 반복문 돌며 설정한 출력스트림에 파일 쓰기 (inputStream의 read() 메서드는 read 끝에 -1을 반환한다)
			// 마지막 buf는 1024 byte 배열을 꽉 채울리가 없으니 write 메서드의 offset 설정으로 size만큼만 받아오고 while문 종료
			
			fos.close();
			fis.close();
			// 다 썼으면 닫아준다	
		}
		
		builder.delete(builder.length()-1, builder.length());
		// 마지막 "," 빼기
		
		
		boolean pub = false;
		if(isOpen != null)
			pub = true;
		
		Notice notice = new Notice();
		notice.setTitle(title);
		notice.setContent(content);
		notice.setPub(pub);
		notice.setWriterId("newlec");
		// 작성자 임시 등록 (로그인 구현 아직 안함)
		notice.setFiles(builder.toString());
		// StringBuilder 타입이므로 String 참조변수형 타입으로 변경해서 넣어준다
		NoticeService service = new NoticeService();
		int result = service.insertNotice(notice);
		// db에 잘 insert 되었다면 행의 갯수인 1을 반환
		
		response.sendRedirect("list");
		
	}
	
}
