package project1122;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

class ServerThread extends Thread { // 다중 클라이언트 처리를 하기 위해
	// 소캣변수
	private Socket socketOfServer;
	// 입출력 변수
	private InputStream in = null;
	private BufferedReader br = null;
	private OutputStream out = null;
	private PrintWriter pw = null;
	// db연결
	// 원격 연결을 위해 IP 주소 설정
	private String url = "jdbc:mysql://192.168.0.13:3306/gym_data?useSSL=false";
	private String dbId = "gym_server";
	private String dbPwd = "1234";
	// 쿼리 변수
	private Connection conn = null;
	private PreparedStatement pstmt = null;
	private ResultSet rs = null;
	
	
	// 변수
	// 사용자 저장을 위한 hashmap
	private static HashMap<String, PrintWriter> clients = new HashMap<String, PrintWriter>();
	// 관리자: 비밀번호, 회원: 회원번호
	private String name = null;	//Integer.valueOf(name) = memno
	// 선택을 위한 변수
	private int menu = 0;
	// num[0]: 예약한 회원 수, num[1]: 정원 저장을 위한 배열
	private int[] num = new int[2];

	// 생성자
	public ServerThread(Socket socketOfServer) {
		this.socketOfServer = socketOfServer;

		try {
			// 입력과 관련
			in = this.socketOfServer.getInputStream(); // 소켓에 대한 입력 스트림 반환
			br = new BufferedReader(new InputStreamReader(in));

			// 출력과 관련
			out = this.socketOfServer.getOutputStream(); // 소켓에 대한 출력 스트림 반환
			pw = new PrintWriter(new OutputStreamWriter(out));

			
			menu = Integer.valueOf(br.readLine()); // 메뉴 입력
			conn = gym_method.getConnectivity(url, dbId, dbPwd); // 연결

			// 메뉴 선택
			if (menu == 1) { // 관리자
				name = br.readLine();
				if ("393".equals(name)) { // 비밀번호 확인
					addClient(name, pw);
					sendMsg(name, "환영합니다.");
				}
			} else if (menu == 2) { // 회원
				name = br.readLine();
				addClient(name, pw);
			} else {
				br.close();
				pw.close();
				socketOfServer.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {	

			if (menu == 1) { // 관리자
				while (true) {
					int mg_menu = Integer.valueOf(br.readLine()); // 관리자 메뉴 입력
					switch (mg_menu) {
					case 1: // 회원추가
						String msg = br.readLine();
						String[] part = msg.split(" "); // 이름, 라커여부, 운동복 대여 여부를 공백으로 나눔
						gym_method.insert(conn, pstmt, part[0], Integer.valueOf(part[1]), Integer.valueOf(part[2])); // 회원 추가
						sendMsg(name, "회원이 추가 되었습니다 ^.^");

						// ooo의 회원번호는 oo입니다.
						String sql = "select name, memno "
								+ "from (select name, memno, startdate from member where name = ?) temp "
								+ "order by startdate desc limit 1";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, part[0]);
						rs = pstmt.executeQuery();
						rs.next();
						String str = String.format("%s님의 회원번호는 %d입니다.", rs.getString(1), rs.getInt(2));
						sendMsg(name, str);

						break;

					case 2: // 회원조회
						int memNo = Integer.valueOf(br.readLine()); // 회원번호 입력
						rs = gym_method.selectMember(conn, pstmt, rs, memNo); // 특정 회원 조회

						sendMsg(name, "회원번호\t이름\t시작날짜\t\t라커 사용여부\t운동복 사용여부");
						if (rs.next()) {
							// 내용 출력
							sendMsg(name, rs.getInt(1) + "\t" + rs.getString(2) + "\t" + rs.getString(3) + "\t"
									+ rs.getString(4) + "\t\t" + rs.getString(5));
						} else {
							sendMsg(name, "회원이 존재하지 않습니다.");
						}

						break;

					case 3: // 회원삭제
						memNo = Integer.valueOf(br.readLine()); // 회원번호 입력
						gym_method.deleteMember(conn, pstmt, memNo); // 특정 회원 삭제
						sendMsg(name, "회원이 삭제 되었습니다");

						break;
					case 4: // 전체회원조회
						sql = "select m.memno, m.name, " + "DATE_FORMAT(startdate, '%Y-%m-%d'), "
								+ "IF(locker, '사용함', '사용 안함'), " + "IF(sportswear, '대여함', '대여 안함'), " + "p.sportname "
								+ "from member m left join reservation r " + "on m.memno = r.memno "
								+ "left join program p " + "on p.sportNo = r.sportNo";

						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery();

						sendMsg(name, "회원번호\t이름\t시작날짜\t\t라커 사용여부\t운동복 사용여부\t예약한 운동");
						while (rs.next()) { // 회원 모두 출력
							sendMsg(name, rs.getInt(1) + "\t" + rs.getString(2) + "\t" + rs.getString(3) + "\t"
									+ rs.getString(4) + "\t\t" + rs.getString(5) + "\t\t" + rs.getString(6));
						}

						sendMsg(name, "quit");
						break;
					case 5: // 프로그램종료
						sendMsg(name, "프로그램을 종료합니다");
						br.close();
						pw.close();
						socketOfServer.close();
						break;
					}
				}

			} else if (menu == 2) { // 회원
				while (true) {
					int m_menu = Integer.valueOf(br.readLine()); // 회원 메뉴 입력
					switch (m_menu) {
					case 1: // 예약조회
						if (gym_method.isReser(conn, pstmt, rs, Integer.valueOf(name))) { // 예약이 있는지 확인
							// 내용 출력
							rs = gym_method.getReser(conn, pstmt, rs, Integer.valueOf(name)); // 예약 정보 출력
							sendMsg(name, rs.getString(1));
						} else {
							sendMsg(name, "예약한 내역이 없습니다.");
						}
						break;

					case 2: // 새 예약
						if (gym_method.isReser(conn, pstmt, rs, Integer.valueOf(name))) { // 예약이 있는지 확인
							rs = gym_method.getReser(conn, pstmt, rs, Integer.valueOf(name)); // 예약 정보 출력
							sendMsg(name, "이미 예약");
							sendMsg(name, "이미 예약한 내역이 있습니다.");
							sendMsg(name, rs.getString(1)); // ooo님이 예약하신 운동은 oo 입니다.
						} else {
							sendMsg(name, "예약안함");
							int s_menu = Integer.valueOf(br.readLine()); // 운동 번호 입력

							num = gym_method.getCount(conn, pstmt, s_menu); // 예약한 회원 수, 정원 받아옴
							sendMsg(name, gym_method.compareNum(conn, pstmt, s_menu, Integer.valueOf(name), num)); // 정원 비교 후 출력
						}
						break;

					case 3: // 예약 변경
						if (gym_method.isReser(conn, pstmt, rs, Integer.valueOf(name))) { // 예약이 있는지 확인
							rs = gym_method.getReser(conn, pstmt, rs, Integer.valueOf(name)); // 예약 정보 출력
							sendMsg(name, "이미 예약");
							sendMsg(name, rs.getString(1)); // ooo님이 예약하신 운동은 oo 입니다.
							int s_menu = Integer.valueOf(br.readLine()); // 운동 번호 입력
							
							if (s_menu == 5) { // 예약 취소
								try {
									String sql = "delete from reservation where memno = ?";
									pstmt = conn.prepareStatement(sql);
									pstmt.setInt(1, Integer.valueOf(name));
									pstmt.executeUpdate();
									sendMsg(name, "예약이 취소되었습니다.");
								} catch (SQLException e) {
									e.printStackTrace();
								}
							} else { // 예약 변경
								num = gym_method.getCount(conn, pstmt, s_menu); // 예약한 회원 수, 정원 받아옴
								sendMsg(name, gym_method.updateReser(conn, pstmt, s_menu, Integer.valueOf(name), num)); // 예약 변경
							}
						} else {
							sendMsg(name, "예약안함");
							sendMsg(name, "예약한 내역이 없습니다.");
						}

						break;
					case 4: // 프로그램 종료
						sendMsg(name, "프로그램을 종료합니다");
						br.close();
						pw.close();
						socketOfServer.close();
						break;
					}
				}

			} else { // 프로그램 종료
				br.close();
				pw.close();
				socketOfServer.close();
			}

		} catch (IOException e) {

		} catch (NullPointerException e) {

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 사용자 추가
	public void addClient(String name, PrintWriter pw) {
		clients.put(name, pw);
		String pwd = "393"; // 관리자 비밀번호

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd, hh:mm:ss"); // 날짜 포맷 지정
		Date now = new Date(System.currentTimeMillis()); // 현재 시각 받아옴
		
		if (pwd.equals(name)) { // 관리자
			System.out.printf("%s  접속한 사용자는 관리자 입니다.\n", sdf.format(now)); // 서버에서 출력
			sendMsg(name, "관리자님이 접속하였습니다.");
		} else { // 회원
			try {
				String sql = "select name from member where memno = ?"; // 회원번호로 회원 이름 받아옴
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, Integer.valueOf(name));
				rs = pstmt.executeQuery();
				rs.next();
				String c_name = rs.getString(1);
				System.out.printf("%s  접속한 사용자는 %s님 입니다.\n", sdf.format(now), c_name); // 서버에서 출력
				sendMsg(name, c_name + "님이 접속하였습니다.");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	// 특정 클라이언트에서 출력하게 메시지 보내줌
	public void sendMsg(String name, String msg) {
		clients.get(name).println(msg);
		clients.get(name).flush();
	}
}

public class gym_aServer {
	public static void main(String[] args) {
		// 연결 받아서 쓰레드 돌려주기
		try {
			ServerSocket server = new ServerSocket(9999); // 포트번호 9999로 서버 소켓 생성
			System.out.println("접속을 기다립니다.");
			while (true) { // 다중 클라이언트를 받기 위해
				Socket socketOfServer = server.accept(); // 연결
				ServerThread serverThread = new ServerThread(socketOfServer); // 쓰레드 생성
				serverThread.start(); // 쓰레드 실행
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}