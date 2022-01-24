-- 테이블 먼저 삭제
drop table reservation;
drop table member;
drop table program;


-- 테이블 생성 3개
-- 1. 회원 테이블
CREATE TABLE MEMBER (
    memNo INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(45),
    startDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    locker TINYINT,
    sportsWear TINYINT
);

-- 2. 운동 프로그램 테이블
CREATE TABLE PROGRAM (
	sportNo INT PRIMARY KEY,
    sportName VARCHAR(45),
    capacity INT
    -- 1번요가 2번 필라테스 3번 줌바댄스 4번파워댄스
);

-- 3. 운동 예약 테이블
CREATE TABLE RESERVATION(
	rvNo INT AUTO_INCREMENT PRIMARY KEY,
	memNo INT,
	sportNo int,
    FOREIGN KEY(memNo) REFERENCES MEMBER(MEMNO) ON DELETE CASCADE,
    FOREIGN KEY(sportNo) REFERENCES PROGRAM(sportNo) ON UPDATE CASCADE ON DELETE SET NULL
);


-- 데이터 삽입
-- 멤버 테이블 데이터 삽입
insert into member(name, locker, sportsWear) values('수현', 1, 0);
insert into member(name, locker, sportsWear) values('인우', 1, 1);
insert into member(name, locker, sportsWear) values('희수', 0, 1);

-- 운동 프로그램 데이터 삽입
insert into program values(1,'요가',3);
insert into program values(2,'필라테스',5);
insert into program values(3,'줌바댄스',6);
insert into program values(4,'파워댄스',4);

-- 예약 테이블 데이터 삽입
-- 프로그램 실행 시 정원이 초과된 운동을 확인하기 위해 요가에 3명을 삽입
insert into reservation(memno, sportno) values(1,1);
insert into reservation(memno, sportno) values(2,1);
insert into reservation(memno, sportno) values(3,1);


-- 데이터 삽입 확인
select m.memno, m.name, DATE_FORMAT(startdate, '%Y-%m-%d') "startdate", 
IF(locker, '사용함', '사용 안함') "락커사용여부", 
IF(sportswear, '대여함', '대여 안함') "운동복 대여여부", p.sportname 
from member m left join reservation r 
on m.memno = r.memno 
left join program p 
on p.sportNo = r.sportNo;