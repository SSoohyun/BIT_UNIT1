package project1122;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class gym_aClient {

	public static void main(String[] args) {
		try {
			// 소켓 및 입출력 변수
			Socket socketOfClient = new Socket("192.168.0.46", 9999); // IP주소와 포트번호로 클라이언트 소켓 생성
			InputStream in = socketOfClient.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			OutputStream out = socketOfClient.getOutputStream();
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(out));
			BufferedReader stin = new BufferedReader(new InputStreamReader(System.in)); // 키보드 입력용

			// 메뉴 선택 변수
			int menu = 0;

			BREAK: 
			while (true) {
				System.out.println("===================================================");
				System.out.println("1.관리자 2.회원 3.프로그램 종료");
				System.out.println("===================================================");

				// 메뉴 선택 및 입력
				System.out.print("메뉴를 선택하시오 >> ");
				menu = Integer.valueOf(stin.readLine());
				pw.println(menu);
				pw.flush();

				switch (menu) {

				case 1: // 관리자
					System.out.print("관리자 비밀번호를 입력하시오. >> ");
					String pwd = stin.readLine(); // 관리자 비밀번호
					pw.println(pwd);
					pw.flush();
					System.out.println(br.readLine());

					if (br.readLine().equals("환영합니다.")) { // 관리자 비밀번호 일치

						while (true) {
							System.out.println();
							System.out.println("---------------------------------------------------");
							System.out.println("1.회원추가 2.회원조회 3.회원삭제 4.전체 회원 확인 5.프로그램 종료");

							menu = Integer.valueOf(stin.readLine());
							pw.println(menu);
							pw.flush();

							switch (menu) {

							case 1: // 회원추가
								System.out.println("추가할 회원의 이름, 락커 사용여부(1 or 0), 운동복 사용여부(1 or 0)를 입력하시오");
								String msg = stin.readLine();
								pw.println(msg);
								pw.flush();
								System.out.println(br.readLine());
								System.out.println(br.readLine());
								break;

							case 2: // 회원조회
								System.out.print("조회할 회원 번호를 입력하세요 >> ");
								int memNo = Integer.valueOf(stin.readLine());
								pw.println(memNo);
								pw.flush();

								System.out.println("---------------------------------------------------------------");
								System.out.println(br.readLine());
								System.out.println("---------------------------------------------------------------");
								
								System.out.println(br.readLine());
								break;

							case 3: // 회원삭제
								System.out.print("삭제할 회원 번호를 입력하세요 >> ");
								memNo = Integer.valueOf(stin.readLine()); // 회원 번호 입력
								pw.println(memNo);
								pw.flush();

								System.out.println(br.readLine());

								break;

							case 4: // 전체 회원 확인
								System.out.println("--------------------------------------------------------------------------");
								System.out.println(br.readLine());
								System.out.println("--------------------------------------------------------------------------");

								String input = null;
								while ((input = br.readLine()) != null) { // 모든 회원 출력
									if (input.equalsIgnoreCase("quit")) {
										break;
									}
									System.out.println(input);
								}
								System.out.println("--------------------------------------------------------------------------");
								
								break;

							case 5:// 프로그램 종료
								System.out.println(br.readLine());
								br.close();
								pw.close();
								stin.close();
								socketOfClient.close();
								break BREAK;
							}
						}
					}

					break;
				case 2: // 회원
					System.out.print("회원 번호를 입력하세요 >> ");
					int memNo = Integer.valueOf(stin.readLine()); // 회원 번호 입력
					pw.println(memNo);
					pw.flush();

					System.out.println(br.readLine());
					// 회원 메뉴 시작부분
					while (true) {
						System.out.println();
						System.out.println("--------------------------------------------------------------------------");
						System.out.println("1.예약 조회 2.새 예약 3.예약 변경 4.프로그램 종료");
						menu = Integer.valueOf(stin.readLine());
						pw.println(menu);
						pw.flush();

						switch (menu) {
						case 1: // 예약 조회
							System.out.println(br.readLine());
							break;

						case 2: // 새 예약
							if (br.readLine().equals("이미 예약")) {// 이미 예약된 운동이 존재
								System.out.println(br.readLine());
								System.out.println(br.readLine());
							} else {// 이미 예약된게 없으면 예약을 시작
								System.out.println();
								System.out.println("예약할 운동을 골라주세요");
								System.out.println("1.요가 2.필라테스 3.줌바댄스 4.파워댄스");
								int s_menu = Integer.valueOf(stin.readLine()); // 운동 번호 입력
								pw.println(s_menu);
								pw.flush();
								System.out.println(br.readLine());
							}
							break;

						case 3: // 예약 변경
							if (br.readLine().equals("이미 예약")) {// 예약 내역 변경
								System.out.println(br.readLine());
								System.out.println();
								System.out.println("변경할 운동을 골라주세요");
								System.out.println("1.요가 2.필라테스 3.줌바댄스 4.파워댄스 5.예약취소");
								int s_menu = Integer.valueOf(stin.readLine()); // 운동 번호 입력
								pw.println(s_menu);
								pw.flush();
								System.out.println(br.readLine());
							} else // 예약내역이 없으므로 변경 불가
								System.out.println(br.readLine());
							break;

						case 4: // 프로그램 종료
							System.out.println("프로그램을 종료합니다");
							br.close();
							pw.close();
							stin.close();
							socketOfClient.close();
							break BREAK;
						}
					}

				case 3: // 종료
					System.out.println("프로그램을 종료합니다");
					break BREAK;
				}
			}
		} catch (UnknownHostException e) { // 이상한 주소
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}