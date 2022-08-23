package com.newlecture.web.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.newlecture.web.entity.Notice;
import com.newlecture.web.entity.NoticeView;

public class NoticeService {
	
	private final String url = "jdbc:oracle:thin:@localhost:1521/xepdb1";
	private final String dID = "";
	private final String dPW = "";
	private final int noticeLength = 10;
	

	public int removeNoticeAll(int[] ids){
		
		return 0;
	}
	
	public int pubNoticeAll(int[] oids, int[] cids){
		
		List<String> oidsList = new ArrayList<>();
		for (int i = 0; i < oids.length; i++) {
			oidsList.add(String.valueOf(oids));
		}
		List<String> cidsList = new ArrayList<>();
		for (int i = 0; i < cids.length; i++) {
			cidsList.add(String.valueOf(cids));
		}
		// 스트링 리스트 객체를 만든다. for문 돌려서 문자열로 형변환해서 차곡차곡 담는다
		// for문 하나로 해결될 걸 라이브러리를 쓰는 건 역으로 성능 저하를 일으킬 수 있다
		// 아래의 오버로드에서는 스트링 리스트 타입을 매개변수로 받고있다
		
		return pubNoticeAll(oidsList, cidsList);
	}
	
	public int pubNoticeAll(List<String> oids, List<String> cids){
		
		// 위에서 정수 배열 인자로 호출당한 친구가 만들어 보내준 스트링 리스트를 이어받는다 
		String oidsCSV = String.join(",", oids);
		String cidsCSV = String.join(",", cids);
		// CSV 형으로 만들어서 아래의 친구를 호출한다
		
		return pubNoticeAll(oidsCSV, cidsCSV);
	}

	public int pubNoticeAll(String oidsCSV, String cidsCSV){
		
		// 원하는 타입의 자료형을 얻었다
		
		int result = 0;
		
		String sqlOpen = String.format("update notice set pub=1 where id in (%s)", oidsCSV);
		String sqlClose = String.format("update notice set pub=0 where id in (%s)", cidsCSV);
		// "+odisCSV+" 이러한 형식이 보기 싫다면 위처럼 format 하면 된다
		
		try {
			
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection con = DriverManager.getConnection(url, dID, dPW);
			Statement stOpen = con.createStatement();
			result += stOpen.executeUpdate(sqlOpen);

			Statement stClose = con.createStatement();
			result += stClose.executeUpdate(sqlClose);
			// 현재 Transaction 처리를 하고 있지 않음. 해야한다.
			
			stOpen.close();
			stClose.close();
			con.close();
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// DB에 잘 등록되었다면 적용된 행의 갯수를 반환한다
		return result;
	}

	public int insertNotice(Notice notice){
		
		int result = 0;
		
		String sql = "insert into notice(title, content, writer_id, pub, files) values(?, ?, ?, ?, ?)";
		
		try {
			
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection con = DriverManager.getConnection(url, dID, dPW);
			PreparedStatement st = con.prepareStatement(sql);
			st.setString(1, notice.getTitle());
			st.setString(2, notice.getContent());
			st.setString(3, notice.getWriterId());
			st.setBoolean(4, notice.getPub());
			st.setString(5, notice.getFiles());

			result = st.executeUpdate();
			// insult, update, delete 를 사용할 때는 executeUpdate() 서비스 메소드를 사용한다.
			// 받아올 데이터가 없기 때문에 ResulSet 객체는 필요없음
			// executeUpdate는 Db의 행삽입 행삭제 갯수 결과값을 int 값으로 return 한다
			
			st.close();
			con.close();
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// db에 잘 등록되었다면 1을 반환한다
		return result;
		
	}
	
	public int deleteNotice(int id){
		
		return 0;
	}
	
	public int updateNotice(Notice notice){
		
		return 0;
	}	

	public List<Notice> getNoticeNewestList(){
		
		return null;
	}
	
	public List<NoticeView> getNoticeList() {
		
		return getNoticeList("title", "", 1);
		
	}
	
	public List<NoticeView> getNoticeList(int page) {
		
		return getNoticeList("title", "", page);
		
	}
	
	public List<NoticeView> getNoticeList(String field, String query,int page) {
		
		List<NoticeView> list = new ArrayList<>();
		
		String sql = "select * from "
				+ "(select rownum NUM, N.* from "
				+ "(select * from NOTICE_VIEW where "+field+" like ? order by regdate desc) N "
				+ ") "
				+ "where NUM between ? and ?";
				
		try {
			
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection con = DriverManager.getConnection(url, dID, dPW);
			PreparedStatement st = con.prepareStatement(sql);
			st.setString(1, "%"+query+"%");
			st.setInt(2, 1+(page-1)*noticeLength);
			st.setInt(3, page*noticeLength);
			ResultSet rs = st.executeQuery();
			
			while(rs.next()){
				int id = rs.getInt("ID");
				String title = rs.getString("TITLE");
				String writerId = rs.getString("WRITER_ID");
				Date regdate = rs.getDate("REGDATE");
				String hit = rs.getString("HIT");
				String files = rs.getString("FILES");
				//String content = rs.getString("CONTENT");
				boolean pub = rs.getBoolean("PUB");
				int cmtCount = rs.getInt("CMT_COUNT");
				
				NoticeView notice = new NoticeView(
						id,
						title,
						writerId,
						regdate,
						hit,
						files,
						pub,
						//content,
						cmtCount
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
	
	public List<NoticeView> getNoticePubList(String field, String query, int page) {
		List<NoticeView> list = new ArrayList<>();
		
		String sql = "select * from "
				+ "(select rownum NUM, N.* from "
				+ "(select * from NOTICE_VIEW where "+field+" like ? order by regdate desc) N "
				+ ") "
				+ "where pub=1 and NUM between ? and ?";
				
		try {
			
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection con = DriverManager.getConnection(url, dID, dPW);
			PreparedStatement st = con.prepareStatement(sql);
			st.setString(1, "%"+query+"%");
			st.setInt(2, 1+(page-1)*noticeLength);
			st.setInt(3, page*noticeLength);
			ResultSet rs = st.executeQuery();
			
			while(rs.next()){
				int id = rs.getInt("ID");
				String title = rs.getString("TITLE");
				String writerId = rs.getString("WRITER_ID");
				Date regdate = rs.getDate("REGDATE");
				String hit = rs.getString("HIT");
				String files = rs.getString("FILES");
				//String content = rs.getString("CONTENT");
				boolean pub = rs.getBoolean("PUB");
				int cmtCount = rs.getInt("CMT_COUNT");
				
				NoticeView notice = new NoticeView(
						id,
						title,
						writerId,
						regdate,
						hit,
						files,
						pub,
						//content,
						cmtCount
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

		String sql = "select COUNT(ID) COUNT from "
				+ "(select rownum NUM, N.* from "
				+ "(select * from notice where "+field+" like ? order by regdate desc) N "
				+ ")";
		
		int count = 0;
				
		try {
			
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection con = DriverManager.getConnection(url, dID, dPW);
			PreparedStatement st = con.prepareStatement(sql);
			st.setString(1, "%"+query+"%");
			ResultSet rs = st.executeQuery();
			
			if(rs.next())
				count = rs.getInt("count");
			
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
		
		return count;
	}
	
	public Notice getNotice(int id) {
		
		Notice notice = null;
		
		String sql = "select * from notice where id=?";
		
		try {
			
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection con = DriverManager.getConnection(url, dID, dPW);
			PreparedStatement st = con.prepareStatement(sql);
			st.setInt(1, id);
			ResultSet rs = st.executeQuery();
			
			if(rs.next()){
				int nid = rs.getInt("ID");
				String title = rs.getString("TITLE");
				String writerId = rs.getString("WRITER_ID");
				Date regdate = rs.getDate("REGDATE");
				String hit = rs.getString("HIT");
				String files = rs.getString("FILES");
				String content = rs.getString("CONTENT");
				boolean pub = rs.getBoolean("PUB");
				
				notice = new Notice(
						nid,
						title,
						writerId,
						regdate,
						hit,
						files,
						content,
						pub
						);
				
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
		
		return notice;
		
	}
	
	public Notice getNextNotice(int id) {
		
		Notice notice = null;
		
		String sql = "select * from notice where id = ( "
				+ "select id from notice where regdate > "
				+ "(select regdate from notice where id=?)"
				+ " and rownum=1 "
				+ ")";
		
		try {
			
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection con = DriverManager.getConnection(url, dID, dPW);
			PreparedStatement st = con.prepareStatement(sql);
			st.setInt(1, id);
			ResultSet rs = st.executeQuery();
			
			if(rs.next()){
				int nid = rs.getInt("ID");
				String title = rs.getString("TITLE");
				String writerId = rs.getString("WRITER_ID");
				Date regdate = rs.getDate("REGDATE");
				String hit = rs.getString("HIT");
				String files = rs.getString("FILES");
				String content = rs.getString("CONTENT");
				boolean pub = rs.getBoolean("PUB");
				
				notice = new Notice(
						nid,
						title,
						writerId,
						regdate,
						hit,
						files,
						content,
						pub
						);
				
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
		
		return notice;
		
	}
	
	public Notice getPrevNotice(int id) {
		
		Notice notice = null;
		
		String sql = "select * from notice where id = ("
				+ " select * from (select * from notice order by regdate desc)"
				+ " where regdate < (select regdate from notice where id=?) and rownum=1"
				+ " )";
		
		try {
			
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection con = DriverManager.getConnection(url, dID, dPW);
			PreparedStatement st = con.prepareStatement(sql);
			st.setInt(1, id);
			ResultSet rs = st.executeQuery();
			
			if(rs.next()){
				int nid = rs.getInt("ID");
				String title = rs.getString("TITLE");
				String writerId = rs.getString("WRITER_ID");
				Date regdate = rs.getDate("REGDATE");
				String hit = rs.getString("HIT");
				String files = rs.getString("FILES");
				String content = rs.getString("CONTENT");
				boolean pub = rs.getBoolean("PUB");
				
				
				notice = new Notice(
						nid,
						title,
						writerId,
						regdate,
						hit,
						files,
						content,
						pub
						);
				
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
		
		return notice;
	}

	public int deleteNoticeAll(int[] ids) {
		
		int result = 0;
		
		String params = "";
		
		for (int i = 0; i < ids.length; i++) {
			params += ids[i];
			
			if (i < ids.length-1) {
				params += ",";
				// 마지막 숫자만 빼고 ","를 붙여줌
			}
		}
		
		String sql = "delete notice where id in ("+params+")";
		
		try {
			
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection con = DriverManager.getConnection(url, dID, dPW);
			Statement st = con.createStatement();

			result = st.executeUpdate(sql);
			// insult, update, delete 를 사용할 때는 executeUpdate() 서비스 메소드를 사용한다.
			// 받아올 데이터가 없기 때문에 ResulSet 객체는 필요없음
			// executeUpdate는 Db의 행삽입 행삭제 갯수 결과값을 int 값으로 return 한다
			
			st.close();
			con.close();
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
	
}
