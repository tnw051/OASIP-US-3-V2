USE oasip;

DELETE FROM user;
ALTER TABLE user AUTO_INCREMENT = 1;

INSERT INTO
  user (name, email, role, createdOn, updatedOn)
VALUES
  (
    'OASIP ADMIN',
    'oasip.admin@kmutt.ac.th',
    'admin',
    '2022-08-01 00:00:00+07:00',
    '2022-08-01 00:00:00+07:00'
  ),
  (
    'Somchai Jaidee',
    'somchai.jai@kmutt.ac.th',
    'lecturer',
    '2022-08-08 15:00:00+07:00',
    '2022-08-08 15:00:00+07:00'
  ),
  (
    'Komkrid Rakdee',
    'komkrid.rak@mail.kmutt.ac.th',
    'student',
    '2022-08-08 15:00:01+07:00',
    '2022-08-08 15:00:01+07:00'
  ),
  (
    'สมเกียรติ ขยันเรียน',
    'somkiat.kay@kmutt.ac.th',
    'student',
    '2022-08-16 09:00:00+07:00',
    '2022-08-16 09:00:00+07:00'
  );