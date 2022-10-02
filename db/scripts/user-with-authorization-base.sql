USE oasip;

DELETE FROM
  user;

ALTER TABLE
  user AUTO_INCREMENT = 1;

INSERT INTO
  user (
    name,
    email,
    password,
    role,
    createdOn,
    updatedOn
  )
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
    'Olarn Rojanapornpun',
    'olarn.roj@kmutt.ac.th',
    '$argon2id$v=19$m=4096,t=3,p=1$Sx7y2jxKZSjpWUV4srd8eg$AMH09iFiPQgAZ00cAdN3Gucqfhx2kRo3tQbHeLSR0RE',
    'lecturer',
    '2022-08-08 15:00:00+07:00',
    '2022-08-08 15:00:00+07:00'
  ),
  (
    'Pichet Limvachiranan',
    'pichet.limv@kmutt.ac.th',
    '$argon2id$v=19$m=4096,t=3,p=1$46EB43gQ46Z1/EmdqxtKNA$7m6cWGO2iDlFl/ETDYuYf+ArnSjRnsNwXLIP18DTYQY',
    'lecturer',
    '2022-08-08 15:00:01+07:00',
    '2022-08-08 15:00:01+07:00'
  ),
  (
    'Umaporn Supasitthimethee',
    'umaporn.sup@kmutt.ac.th',
    '$argon2id$v=19$m=4096,t=3,p=1$1Z2UK1zC76FIQeLH54GVAQ$qfXcHF31LnuWpt37QAcWyNp8PdbOQ+jjaV1xWXixS0M',
    'lecturer',
    '2022-08-08 15:00:02+07:00',
    '2022-08-08 15:00:02+07:00'
  ),
  (
    'Siam Yamsaengsung',
    'siam.yam@kmutt.ac.th',
    '$argon2id$v=19$m=4096,t=3,p=1$wTQSpCANrDkwNngTIuy6Tw$MbNNuhG/wYxqkBi4mET2AT6hyEDSHs7pnhfaZeXE4Qs',
    'lecturer',
    '2022-08-08 15:00:03+07:00',
    '2022-08-08 15:00:03+07:00'
  ),
  (
    'Sunisa Sathapornvajana',
    'sunisa.sat@kmutt.ac.th',
    '$argon2id$v=19$m=4096,t=3,p=1$29/ffaszvjvi3CZO45bSCg$kKpfq5WEswoqa/LfyIZzQaQ6AFdjhyiYjXRCfMiTnwg',
    'lecturer',
    '2022-08-08 15:00:04+07:00',
    '2022-08-08 15:00:04+07:00'
  ),
  (
    'Somchai Jaidee',
    'somchai.jai@kmutt.ac.th',
    '$argon2id$v=19$m=4096,t=3,p=1$dmsOy7LPTjmooPu+P2oTZA$NZFTFd3f0K1Sp19aaUwyn3jgiy15yFcXhp8E4/1yXoI',
    'student',
    '2022-08-08 16:00:00+07:00',
    '2022-08-08 16:00:00+07:00'
  ),
  (
    'Komkrid Rakdee',
    'komkrid.rak@mail.kmutt.ac.th',
    '$argon2id$v=19$m=4096,t=3,p=1$8W61ZOC5RU7sJP5kKRbSqg$OLwZNPeMqxp+g0Vbn+odcA47XMClFN+IswTueVah7F0',
    'student',
    '2022-08-08 16:00:00+07:00',
    '2022-08-08 16:00:00+07:00'
  ),
  (
    'สมเกียรติ ขยันเรียน',
    'somkiat.kay@kmutt.ac.th',
    '$argon2id$v=19$m=4096,t=3,p=1$gBqgjspF45FcIKQEw8GmaQ$alrOCZ0YrDqOu8/aZiLDMGZo4vFkSEAXA0YoHhY0BDQ',
    'student',
    '2022-08-16 09:00:00+07:00',
    '2022-08-16 09:00:00+07:00'
  );