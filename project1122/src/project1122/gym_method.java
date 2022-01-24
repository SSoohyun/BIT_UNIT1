package project1122;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class gym_method {

	public static void main(String[] args) {
	}

	// db연결
	public static Connection getConnectivity(String url, String dbId, String dbPwd) {
		Connection conn = null;
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(url, dbId, dbPwd);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return conn;
	}

	// 관리자가 회원 추가(이름, 라커 사용여부, 운동복 대여 여부)
	public static void insert(Connection conn, PreparedStatement pstmt, String name, int locker, int sportswear) {
		try {
			String sql = "insert into member(name, locker, sportswear) values(?, ?, ?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, name);
			pstmt.setInt(2, locker);
			pstmt.setInt(3, sportswear);
			pstmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 회원이 예약을 추가
	public static void insert(Connection conn, PreparedStatement pstmt, int memno, int sportno) {
		try {
			String sql = "insert into reservation(memno, sportno) values(?, ?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, memno);
			pstmt.setInt(2, sportno);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 관리자가 회원번호로 1명 조회
	public static ResultSet selectMember(Connection conn, PreparedStatement pstmt, ResultSet rs, int memno) {
		try {
			String sql = "SELECT memno,name, DATE_FORMAT(startdate, '%Y-%m-%d'), IF(locker, '사용함', '사용 안함'), IF(sportswear, '대여함', '대여 안함') FROM member where memno = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, memno);
			rs = pstmt.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
	}

	// 회원이 예약 내역 확인
	public static boolean isReser(Connection conn, PreparedStatement pstmt, ResultSet rs, int memno) {

		try {
			String sql = "select * from reservation where memno = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, memno);
			rs = pstmt.executeQuery();
			
			if (rs.next()) {// 예약 내역이 있으면 true 리턴
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	// 정원 초과인지 확인하여 예약 변경
	public static String updateReser(Connection conn, PreparedStatement pstmt, int sportno, int memno, int[] num) {
		try {
			if (num[0] >= num[1]) {// 정원 초과
				return String.format("자리가 부족하여 예약을 할 수 없습니다 %d/%d", num[0], num[1]);
			} else {// 운동번호, 회원번호를 받아서 예약 변경
				String sql = "update reservation set sportNo= ? where memno = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, sportno);
				pstmt.setInt(2, memno);
				pstmt.executeUpdate();
				
				// 운동번호 입력받은 후 운동이름 출력
				sql = "select sportname from program where sportno = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, sportno);
				ResultSet rs = pstmt.executeQuery();
				rs.next();
				return String.format("%s로 예약이 변경 되었습니다. %d/%d", rs.getString(1), ++num[0], num[1]);
			}
		} catch (SQLException e) {
			e.getMessage();
		}
		return null;
	}

	// 관리자가 회원삭제
	public static void deleteMember(Connection conn, PreparedStatement pstmt, int memno) {
		try {
			String sql = "delete from member where memno = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, memno);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.getMessage();
		}
	}

	// 정원초과 판별을 위한 배열 반환
	public static int[] getCount(Connection conn, PreparedStatement pstmt, int sportno) {
		// num[0]: 예약한 회원 수, num[1]: 정원
		int[] num = new int[2];

		try {
			String sql = "select count(*) from reservation where sportno = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, sportno);
			ResultSet rs = pstmt.executeQuery();
			rs.next();
			num[0] = rs.getInt(1); // count

			sql = "select capacity from program where sportno = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, sportno);
			rs = pstmt.executeQuery();
			rs.next();
			num[1] = rs.getInt(1); // capacity
		} catch (SQLException e) {
			e.getMessage();
		}
		return num;
	}

	// getCount에서 반환한 배열 비교
	public static String compareNum(Connection conn, PreparedStatement pstmt, int sportno, int memno, int[] num) {
		if (num[0] >= num[1]) { // 정원 초과
			return String.format("자리가 부족하여 예약을 할 수 없습니다 %d/%d", num[0], num[1]);
		} else {
			insert(conn, pstmt, memno, sportno); // 새 예약
			return String.format("예약이 완료되었습니다. %d/%d", ++num[0], num[1]);
		}
	}

	// 회원이 예약한 운동 출력
	public static ResultSet getReser(Connection conn, PreparedStatement pstmt, ResultSet rs, int memno) {
		try {
			String sql = "select concat(name,'님이 예약하신 운동은 ',sportname,' 입니다') from member m, reservation r, program p where r.memno = m.memno and p.sportNo = r.sportNo and r.memNo = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, memno);
			rs = pstmt.executeQuery();
			rs.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
	}
}