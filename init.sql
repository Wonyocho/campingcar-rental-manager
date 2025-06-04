DROP USER IF EXISTS 'user1'@'localhost';
CREATE USER 'user1'@'localhost' IDENTIFIED BY 'user1';

GRANT SELECT, INSERT, UPDATE, DELETE ON DBTEST.* TO 'user1'@'localhost';
FLUSH PRIVILEGES;

DROP DATABASE IF EXISTS DBTEST;
CREATE DATABASE DBTEST CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE DBTEST;

CREATE TABLE RentalCompany (
    company_id      INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    address         VARCHAR(200) NOT NULL,
    phone           VARCHAR(30),
    manager_name    VARCHAR(50),
    manager_email   VARCHAR(100)
);

CREATE TABLE Car (
    car_id              INT AUTO_INCREMENT PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    plate_number        VARCHAR(30) NOT NULL,
    capacity            INT NOT NULL,
    image               VARCHAR(200),
    description         TEXT,
    daily_price         INT NOT NULL,
    registration_date   DATE,
    company_id          INT,
    FOREIGN KEY (company_id) REFERENCES RentalCompany(company_id)
);

CREATE TABLE PartInventory (
    part_id         INT AUTO_INCREMENT PRIMARY KEY,
    part_name       VARCHAR(100) NOT NULL,
    price           INT,
    quantity        INT,
    arrival_date    DATE,
    supplier_name   VARCHAR(100)
);

CREATE TABLE Employee (
    employee_id     INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    phone           VARCHAR(30),
    address         VARCHAR(200),
    salary          INT,
    dependents      INT,
    department      VARCHAR(30),
    role            ENUM('관리', '사무', '정비')
);

CREATE TABLE Customer (
    customer_id     INT AUTO_INCREMENT PRIMARY KEY,
    login_id        VARCHAR(30) UNIQUE NOT NULL,
    password        VARCHAR(100) NOT NULL,
    license_number  VARCHAR(30) UNIQUE,
    name            VARCHAR(50) NOT NULL,
    address         VARCHAR(200),
    phone           VARCHAR(30),
    email           VARCHAR(100),
    last_use_date   DATE,
    last_car_type   VARCHAR(100)
);

CREATE TABLE CarRental (
    rental_id           INT AUTO_INCREMENT PRIMARY KEY,
    car_id              INT,
    license_number      VARCHAR(30),
    company_id          INT,
    start_date          DATE,
    duration_days       INT,
    total_price         INT,
    pay_due_date        DATE,
    extra_detail        VARCHAR(100),
    extra_price         INT,
    FOREIGN KEY (car_id) REFERENCES Car(car_id),
    FOREIGN KEY (license_number) REFERENCES Customer(license_number),
    FOREIGN KEY (company_id) REFERENCES RentalCompany(company_id)
);

CREATE TABLE Maintenance (
    maintenance_id      INT AUTO_INCREMENT PRIMARY KEY,
    car_id              INT,
    part_id             INT,
    date                DATE,
    duration_min        INT,
    employee_id         INT,
    FOREIGN KEY (car_id) REFERENCES Car(car_id),
    FOREIGN KEY (part_id) REFERENCES PartInventory(part_id),
    FOREIGN KEY (employee_id) REFERENCES Employee(employee_id)
);

CREATE TABLE RepairShop (
    shop_id         INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100),
    address         VARCHAR(200),
    phone           VARCHAR(30),
    manager_name    VARCHAR(50),
    manager_email   VARCHAR(100)
);

CREATE TABLE ExternalRepair (
    repair_id           INT AUTO_INCREMENT PRIMARY KEY,
    car_id              INT,
    shop_id             INT,
    company_id          INT,
    license_number      VARCHAR(30),
    detail              TEXT,
    repair_date         DATE,
    repair_price        INT,
    pay_due_date        DATE,
    extra_detail        VARCHAR(100),
    FOREIGN KEY (car_id) REFERENCES Car(car_id),
    FOREIGN KEY (shop_id) REFERENCES RepairShop(shop_id),
    FOREIGN KEY (company_id) REFERENCES RentalCompany(company_id),
    FOREIGN KEY (license_number) REFERENCES Customer(license_number)
);

INSERT INTO RentalCompany (name, address, phone, manager_name, manager_email) VALUES
('캠핑월드', '서울 강남구 역삼동 123-4', '010-1234-5678', '홍길동', 'hong@campingworld.com'),
('아웃도어렌트', '경기 성남시 분당구 567-8', '031-888-9999', '이순신', 'lee@outdoorrent.com'),
('캠핑팩토리', '부산 해운대구 44-9', '051-222-8888', '강감찬', 'kang@campfactory.com'),
('바이크투어', '대전 서구 갈마동 12-34', '042-111-2222', '정약용', 'jeong@biketour.com'),
('카라반하우스', '경남 창원시 팔용동 123', '055-556-9999', '신사임당', 'shin@caravan.com'),
('캠핑렌터', '전북 전주시 덕진구 56-7', '063-222-1111', '최무선', 'choi@camprenter.com'),
('로드위즈', '서울 중랑구 상봉동 33-2', '02-444-1212', '이황', 'hwang@roadwiz.com'),
('오토캠프', '광주 광산구 수완동 98', '062-789-6543', '장영실', 'jang@autocamp.com'),
('유니캠프', '충북 청주시 상당구 17-99', '043-123-8888', '유관순', 'yoo@unicamp.com'),
('캠프나우', '경북 경주시 신평동 25', '054-323-7272', '김유신', 'kim@campnow.com'),
('카캠퍼스', '강원 춘천시 동면 45-88', '033-444-2323', '이도', 'lee@carcampus.com'),
('캠핑플러스', '제주 서귀포시 표선면 78-1', '064-222-3333', '이순재', 'soonjae@campplus.com');

INSERT INTO Car (name, plate_number, capacity, image, description, daily_price, registration_date, company_id) VALUES
('카라반A', '12가1234', 4, 'img1.jpg', '고급형 캠핑카', 120000, '2023-01-10', 1),
('카라반B', '34나5678', 6, 'img2.jpg', '대가족용 캠핑카', 170000, '2023-05-22', 1),
('캠핑카C', '56다2468', 2, 'img3.jpg', '소형 커플 전용', 90000, '2024-03-12', 2),
('오토RV', '23나9876', 4, 'img4.jpg', '자동차 개조형', 130000, '2023-02-17', 2),
('럭셔리카라반', '45다3456', 8, 'img5.jpg', '프리미엄 럭셔리', 200000, '2024-04-20', 3),
('패밀리캠퍼', '11라2222', 5, 'img6.jpg', '가족용 맞춤', 140000, '2023-12-28', 3),
('미니캠핑카', '67마1234', 2, 'img7.jpg', '경차형 캠핑카', 80000, '2024-02-05', 4),
('에버카라반', '89바4321', 7, 'img8.jpg', '최신형 고급', 180000, '2023-08-15', 1),
('포레스트', '34바7777', 4, 'img9.jpg', '자연친화형', 130000, '2023-11-30', 2),
('솔로캠퍼', '45사8888', 1, 'img10.jpg', '1인용 특화', 65000, '2024-01-17', 4),
('스마트카라반', '56아9999', 6, 'img11.jpg', '스마트 IoT 적용', 160000, '2023-09-25', 3),
('스탠다드캠퍼', '78자1000', 4, 'img12.jpg', '표준형', 110000, '2023-07-14', 2);

INSERT INTO PartInventory (part_name, price, quantity, arrival_date, supplier_name) VALUES
('타이어', 80000, 10, '2024-01-15', '미쉐린'),
('배터리', 120000, 5, '2024-02-10', '삼성SDI'),
('오일필터', 20000, 20, '2024-02-01', '모비스'),
('브레이크패드', 30000, 12, '2024-02-14', '모비스'),
('에어필터', 15000, 18, '2024-02-18', '현대모비스'),
('냉각수', 25000, 25, '2024-02-21', 'SK화학'),
('와이퍼', 12000, 22, '2024-02-25', '불스원'),
('라이트', 9000, 16, '2024-03-01', '오스람'),
('점화플러그', 14000, 14, '2024-03-04', '보쉬'),
('에어컨필터', 17000, 13, '2024-03-08', '삼성'),
('파워핸들오일', 21000, 10, '2024-03-12', 'GS칼텍스'),
('엔진오일', 50000, 28, '2024-03-16', 'S-OIL');

INSERT INTO Employee (name, phone, address, salary, dependents, department, role) VALUES
('김철수', '010-5678-1234', '서울 마포구', 3200000, 2, '정비팀', '정비'),
('박영희', '010-8888-7777', '경기 일산서구', 2800000, 0, '관리팀', '관리'),
('오수진', '010-1212-3434', '부산 사하구', 3000000, 1, '정비팀', '정비'),
('최영수', '010-8765-4321', '대전 동구', 2700000, 3, '사무팀', '사무'),
('이성민', '010-7777-6666', '광주 남구', 3500000, 0, '정비팀', '정비'),
('윤경호', '010-5555-6666', '전북 전주시', 3200000, 2, '관리팀', '관리'),
('송가인', '010-1111-2222', '경남 창원시', 3100000, 1, '사무팀', '사무'),
('정은지', '010-3333-4444', '제주 서귀포', 3300000, 2, '정비팀', '정비'),
('홍길동', '010-9999-8888', '서울 송파구', 3250000, 1, '정비팀', '정비'),
('유재석', '010-2323-2323', '강원 춘천시', 3120000, 2, '관리팀', '관리'),
('박보검', '010-9876-5432', '경기 평택시', 2900000, 0, '정비팀', '정비'),
('아이유', '010-2424-6868', '충북 청주시', 3400000, 1, '사무팀', '사무');

INSERT INTO Customer (login_id, password, license_number, name, address, phone, email, last_use_date, last_car_type) VALUES
('user1', 'user1', '98서울123456', '최원영', '서울 구로구', '010-2222-1111', 'wychoi@mail.com', '2024-03-10', '카라반A'),
('camper2', 'pw5678', '12경기234567', '이지은', '경기 고양시', '010-3333-4444', 'jieun@mail.com', '2024-01-08', '캠핑카C'),
('blueboy', 'pw9898', '11강원121212', '박준형', '강원 춘천시', '010-5555-1212', 'jun@mail.com', '2024-02-21', '오토RV'),
('maria', 'pw5555', '25부산454545', '김마리아', '부산 해운대', '010-6789-1212', 'maria@mail.com', '2024-04-20', '럭셔리카라반'),
('kingkong', 'pw3434', '12서울434343', '이정재', '서울 강남구', '010-3131-2323', 'leejae@mail.com', '2024-03-15', '카라반B'),
('bambi', 'pw4141', '99경북191919', '배수지', '경북 포항시', '010-6565-7474', 'bambi@mail.com', '2024-01-01', '미니캠핑카'),
('newlife', 'pw5151', '88경기262626', '정유미', '경기 안양시', '010-5151-5252', 'yumi@mail.com', '2024-05-10', '패밀리캠퍼'),
('warren', 'pw6262', '33충남010101', '김워렌', '충남 천안시', '010-2020-3030', 'warren@mail.com', '2024-02-02', '에버카라반'),
('cityman', 'pw7272', '77서울878787', '박도현', '서울 관악구', '010-9898-1111', 'city@mail.com', '2024-04-14', '포레스트'),
('songyi', 'pw8282', '44경기161616', '송이', '경기 평택시', '010-2323-5656', 'songyi@mail.com', '2024-01-19', '솔로캠퍼'),
('hyomin', 'pw9393', '66인천343434', '박효민', '인천 연수구', '010-5551-2222', 'hyo@mail.com', '2024-03-30', '스마트카라반'),
('candyman', 'pw1010', '55서울484848', '임동원', '서울 중구', '010-8484-9999', 'dongwon@mail.com', '2024-02-15', '스탠다드캠퍼');

INSERT INTO RepairShop (name, address, phone, manager_name, manager_email) VALUES
('오토정비', '서울 중구 을지로', '02-555-1111', '정비장', 'auto@repair.com'),
('차수리특공대', '경기 부천시', '032-888-1234', '박수리', 'special@repair.com'),
('베스트카정비', '부산 해운대구', '051-321-4321', '김베스트', 'best@carrepair.com'),
('제로정비', '대전 서구', '042-232-1212', '최제로', 'zero@repair.com'),
('슈퍼카', '경기 남양주시', '031-222-2222', '이슈퍼', 'super@repair.com'),
('정직한정비', '강원 원주시', '033-777-9999', '장정직', 'honest@repair.com'),
('빅카', '전북 전주시', '063-123-4567', '문빅', 'big@carrepair.com'),
('스피드정비', '충남 천안시', '041-987-6543', '유스피드', 'speed@repair.com'),
('매직카', '울산 남구', '052-246-8000', '김매직', 'magic@repair.com'),
('퍼펙트카', '경북 포항시', '054-334-9090', '이퍼펙트', 'perfect@repair.com'),
('이지정비', '경남 창원시', '055-555-1111', '신이지', 'easy@repair.com'),
('올카정비', '제주 제주시', '064-444-1212', '박올', 'all@carrepair.com');

INSERT INTO CarRental (car_id, license_number, company_id, start_date, duration_days, total_price, pay_due_date, extra_detail, extra_price) VALUES
(1, '98서울123456', 1, '2024-04-01', 2, 240000, '2024-04-01', '추가침구', 10000),
(2, '12경기234567', 1, '2024-05-01', 3, 270000, '2024-05-01', '차박세트', 20000),
(3, '11강원121212', 2, '2024-04-15', 5, 450000, '2024-04-16', '냉장고', 12000),
(4, '25부산454545', 2, '2024-03-20', 2, 180000, '2024-03-21', '', 0),
(5, '12서울434343', 3, '2024-06-01', 1, 200000, '2024-06-01', '', 0),
(6, '99경북191919', 3, '2024-05-11', 4, 480000, '2024-05-12', 'TV', 15000),
(7, '88경기262626', 4, '2024-03-29', 2, 160000, '2024-03-29', '', 0),
(8, '33충남010101', 4, '2024-04-09', 3, 540000, '2024-04-10', '', 0),
(9, '77서울878787', 1, '2024-05-24', 1, 130000, '2024-05-24', '', 0),
(10, '44경기161616', 2, '2024-06-05', 5, 325000, '2024-06-06', '', 0),
(11, '66인천343434', 3, '2024-05-03', 2, 680000, '2024-05-04', '', 0),
(12, '55서울484848', 4, '2024-06-11', 1, 110000, '2024-06-11', '', 0);

INSERT INTO Maintenance (car_id, part_id, date, duration_min, employee_id) VALUES
(1, 1, '2024-03-21', 60, 1),
(2, 2, '2024-04-05', 40, 2),
(3, 3, '2024-04-10', 30, 3),
(4, 4, '2024-05-03', 45, 4),
(5, 5, '2024-03-22', 60, 5),
(6, 6, '2024-03-25', 70, 6),
(7, 7, '2024-05-18', 20, 7),
(8, 8, '2024-06-10', 50, 8),
(9, 9, '2024-05-20', 30, 9),
(10, 10, '2024-06-12', 25, 10),
(11, 11, '2024-06-14', 55, 11),
(12, 12, '2024-06-15', 30, 12);

INSERT INTO ExternalRepair (car_id, shop_id, company_id, license_number, detail, repair_date, repair_price, pay_due_date, extra_detail) VALUES
(1, 1, 1, '98서울123456', '엔진오일 교체', '2024-03-25', 150000, '2024-03-25', '없음'),
(2, 2, 2, '12경기234567', '타이어 교환', '2024-04-12', 220000, '2024-04-12', '없음'),
(3, 3, 3, '11강원121212', '와이퍼 교체', '2024-03-29', 30000, '2024-03-29', ''),
(4, 4, 4, '25부산454545', '배터리 교체', '2024-05-02', 110000, '2024-05-02', ''),
(5, 5, 5, '12서울434343', '타이어 교환', '2024-05-18', 88000, '2024-05-18', ''),
(6, 6, 6, '99경북191919', '라이트 교체', '2024-03-19', 33000, '2024-03-19', ''),
(7, 7, 7, '88경기262626', '브레이크 패드', '2024-04-01', 35000, '2024-04-01', ''),
(8, 8, 8, '33충남010101', '오일필터 교체', '2024-04-17', 20000, '2024-04-17', ''),
(9, 9, 9, '77서울878787', '에어컨필터 교체', '2024-04-19', 17000, '2024-04-19', ''),
(10, 10, 10, '44경기161616', '에어필터 교체', '2024-05-21', 15000, '2024-05-21', ''),
(11, 11, 11, '66인천343434', '점화플러그 교체', '2024-06-01', 14000, '2024-06-01', ''),
(12, 12, 12, '55서울484848', '엔진오일 교체', '2024-06-12', 50000, '2024-06-12', '');

