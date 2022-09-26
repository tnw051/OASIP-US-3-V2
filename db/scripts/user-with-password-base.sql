USE oasip;

DELETE FROM user;
ALTER TABLE user AUTO_INCREMENT = 1;

INSERT INTO
  user (name, email, password, role, createdOn, updatedOn)
VALUES
  (
    'OASIP ADMIN',
    'oasip.admin@kmutt.ac.th',
    '$argon2id$v=19$m=4096,t=3,p=1$sYXzbUOqBoHY1NfhJ8cjnw$H6+adWySiFPgcUogJK3hEhcF6Y4fusy7tcXYEL+f0cQ',
    'admin',
    '2022-08-01 00:00:00+07:00',
    '2022-08-01 00:00:00+07:00'
  ),
  (
    'Somchai Jaidee',
    'somchai.jai@kmutt.ac.th',
    '$argon2id$v=19$m=4096,t=3,p=1$dmsOy7LPTjmooPu+P2oTZA$NZFTFd3f0K1Sp19aaUwyn3jgiy15yFcXhp8E4/1yXoI',
    'lecturer',
    '2022-08-08 15:00:00+07:00',
    '2022-08-08 15:00:00+07:00'
  ),
  (
    'Komkrid Rakdee',
    'komkrid.rak@mail.kmutt.ac.th',
    '$argon2id$v=19$m=4096,t=3,p=1$8W61ZOC5RU7sJP5kKRbSqg$OLwZNPeMqxp+g0Vbn+odcA47XMClFN+IswTueVah7F0',
    'student',
    '2022-08-08 15:00:01+07:00',
    '2022-08-08 15:00:01+07:00'
  ),
  (
    'สมเกียรติ ขยันเรียน',
    'somkiat.kay@kmutt.ac.th',
    '$argon2id$v=19$m=4096,t=3,p=1$gBqgjspF45FcIKQEw8GmaQ$alrOCZ0YrDqOu8/aZiLDMGZo4vFkSEAXA0YoHhY0BDQ',
    'student',
    '2022-08-16 09:00:00+07:00',
    '2022-08-16 09:00:00+07:00'
  );