USE oasip;

DELETE FROM profile;

ALTER TABLE profile AUTO_INCREMENT = 1;

DELETE FROM user;

ALTER TABLE user AUTO_INCREMENT = 1;

INSERT INTO
  user (createdOn, updatedOn)
VALUES
  (NOW(), NOW()),
  (NOW(), NOW()),
  (NOW(), NOW()),
  (NOW(), NOW()),
  (NOW(), NOW()),
  (NOW(), NOW()),
  (NOW(), NOW()),
  (NOW(), NOW()),
  (NOW(), NOW());

INSERT INTO
  profile (
    name,
    email,
    password,
    role,
    createdOn,
    updatedOn,
    userId
  )
VALUES
  (
    'OASIP ADMIN',
    'oasip.admin@kmutt.ac.th',
    '$argon2id$v=19$m=4096,t=3,p=1$sYXzbUOqBoHY1NfhJ8cjnw$H6+adWySiFPgcUogJK3hEhcF6Y4fusy7tcXYEL+f0cQ',
    'admin',
    '2022-08-01 00:00:00+07:00',
    '2022-08-01 00:00:00+07:00',
    1
  ),
  (
    'Olarn Rojanapornpun',
    'olarn.roj@kmutt.ac.th',
    '$argon2id$v=19$m=4096,t=3,p=1$Sx7y2jxKZSjpWUV4srd8eg$AMH09iFiPQgAZ00cAdN3Gucqfhx2kRo3tQbHeLSR0RE',
    'lecturer',
    '2022-08-08 15:00:00+07:00',
    '2022-08-08 15:00:00+07:00',
    2
  ),
  (
    'Pichet Limvachiranan',
    'pichet.limv@kmutt.ac.th',
    '$argon2id$v=19$m=4096,t=3,p=1$46EB43gQ46Z1/EmdqxtKNA$7m6cWGO2iDlFl/ETDYuYf+ArnSjRnsNwXLIP18DTYQY',
    'lecturer',
    '2022-08-08 15:00:01+07:00',
    '2022-08-08 15:00:01+07:00',
    3
  ),
  (
    'Umaporn Supasitthimethee',
    'umaporn.sup@kmutt.ac.th',
    '$argon2id$v=19$m=4096,t=3,p=1$1Z2UK1zC76FIQeLH54GVAQ$qfXcHF31LnuWpt37QAcWyNp8PdbOQ+jjaV1xWXixS0M',
    'lecturer',
    '2022-08-08 15:00:02+07:00',
    '2022-08-08 15:00:02+07:00',
    4
  ),
  (
    'Siam Yamsaengsung',
    'siam.yam@kmutt.ac.th',
    '$argon2id$v=19$m=4096,t=3,p=1$C4pPaNWKTnZQX2mPs14jlg$rQ5W5NYKqGOu1B4GkUWq8cFbcg2peFWGjpUMr9Nkm8g',
    'lecturer',
    '2022-08-08 15:00:03+07:00',
    '2022-08-08 15:00:03+07:00',
    5
  ),
  (
    'Sunisa Sathapornvajana',
    'sunisa.sat@kmutt.ac.th',
    '$argon2id$v=19$m=4096,t=3,p=1$29/ffaszvjvi3CZO45bSCg$kKpfq5WEswoqa/LfyIZzQaQ6AFdjhyiYjXRCfMiTnwg',
    'lecturer',
    '2022-08-08 15:00:04+07:00',
    '2022-08-08 15:00:04+07:00',
    6
  ),
  (
    'Somchai Jaidee',
    'somchai.jai@kmutt.ac.th',
    '$argon2id$v=19$m=4096,t=3,p=1$dmsOy7LPTjmooPu+P2oTZA$NZFTFd3f0K1Sp19aaUwyn3jgiy15yFcXhp8E4/1yXoI',
    'student',
    '2022-08-08 16:00:00+07:00',
    '2022-08-08 16:00:00+07:00',
    7
  ),
  (
    'Komkrid Rakdee',
    'komkrid.rak@mail.kmutt.ac.th',
    '$argon2id$v=19$m=4096,t=3,p=1$8W61ZOC5RU7sJP5kKRbSqg$OLwZNPeMqxp+g0Vbn+odcA47XMClFN+IswTueVah7F0',
    'student',
    '2022-08-08 16:00:00+07:00',
    '2022-08-08 16:00:00+07:00',
    8
  ),
  (
    'สมเกียรติ ขยันเรียน',
    'somkiat.kay@kmutt.ac.th',
    '$argon2id$v=19$m=4096,t=3,p=1$gBqgjspF45FcIKQEw8GmaQ$alrOCZ0YrDqOu8/aZiLDMGZo4vFkSEAXA0YoHhY0BDQ',
    'student',
    '2022-08-16 09:00:00+07:00',
    '2022-08-16 09:00:00+07:00',
    9
  );