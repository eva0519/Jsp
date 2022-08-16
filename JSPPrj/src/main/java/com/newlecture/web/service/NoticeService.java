package com.newlecture.web.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

import com.newlecture.web.entity.Notice;

public class NoticeService {
	
	public List<Notice> getNoticeList() {
		
		return getNoticeList("title", "", 1);
		
	}
	
	public List<Notice> getNoticeList(int page) {
		
		return getNoticeList("title", "", page);
		
	}
	
	public List<Notice> getNoticeList(String field, String query,int page) {
		
		List<Notice> list = new ArrayList();
		
		String sql = "select * from ("
				+ "select rownum NUM, N.* from (select * from notice order by regdate desc) N"
				+ ") where NUM between ? and ?";
		
		String url = "jdbc:oracle:thin:@localhost:1521/xepdb1";
		
		try {
			
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection con = DriverManager.getConnection(url, "", "");
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(sql);
			
			while(rs.next()){
				int id = rs.getInt("ID");
				String title = rs.getString("TITLE");
				String writerId = rs.getString("WRITER_ID");
				Date regdate = rs.getDate("REGDATE");
				String hit = rs.getString("HIT");
				String files = rs.getString("FILES");
				String content = rs.getString("CONTENT");
				
				Notice notice = new Notice(
						id,
						title,
						writerId,
						regdate,
						hit,
						files,
						content
						);
				
				list.add(notice);
				
			}
			
			rs.close();
			st.close();
			con.close();
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		return list;
	}
	
	public int getNoticeCount() {
		
		return getNoticeCount("title", "");
	}
	
	public int getNoticeCount(String field, String query) {

		String sql = "select * from ("
				+ "select rownum NUM, N.* from (select * from notice order by regdate desc) N"
				+ ") where NUM between ? and ?";
		
		return 0;
	}
	
	public Notice getNotice(int id) {
		
		String sql = "select * from notice where id=?";
		
		return null;
	}
	
	public Notice getNextNotice(int id) {
		
		String sql = "select * from notice where id = ( "
				+ "select id from notice where regdate > "
				+ "(select regdate from notice where id=?)"
				+ " and rownum=1"
				+ ")";
		
		return null;
	}
	
	public Notice getPrevNotice(int id) {
		return null;
	}
	
	
}
