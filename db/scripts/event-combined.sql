USE oasip;

DELETE FROM event;
ALTER TABLE event AUTO_INCREMENT = 1;

INSERT INTO
  event (
    bookingName,
    bookingEmail,
    eventCategoryId,
    eventStartTime,
    eventDuration,
    eventNotes
  )
VALUES
(
    'Somchai Jaidee (OR-7)',
    'somchai.jai@mail.kmutt.ac.th',
    2,
    '2022-05-23 13:30:00',
    30,
    NULL
  ),
  (
    'Somsri Rakdee (SJ-3)',
    'somsri.rak@mail.kmutt.ac.th',
    1,
    '2022-04-27 09:30:00',
    30,
    'ขอปรึกษาปัญหาเพื่อนไม่ช่วยงาน'
  ),
  (
    'สมเกียรติ ขยันเรียน กลุ่ม TT-4',
    'somkiat.kay@kmutt.ac.th',
    3,
    '2022-05-23 16:30:00',
    15,
    NULL
  ),
(
    'INFRA-26-11:15',
    'INFRA-26-11:15@kmutt.ac.th',
    2,
    '2022-05-26 11:15:00',
    20,
    NULL
  ),
(
    'INFRA-26-09:20',
    'INFRA-26-09:20@kmutt.ac.th',
    2,
    '2022-05-26 09:20:00',
    30,
    NULL
),
(
    'INFRA-26-10:40',
    'INFRA-26-10:40@kmutt.ac.th',
    2,
    '2022-05-26 10:40:00',
    20,
    NULL
),
(
    'INFRA-26-12:00',
    'INFRA-26-12:00@kmutt.ac.th',
    2,
    '2022-05-26 12:00:00',
    30,
    NULL
),
(
    'DB-26-10:20',
    'DB-26-10:20@kmutt.ac.th',
    3,
    '2022-05-26 10:20:00',
    15,
    NULL
  ),
  (
    'INFRA-27-10:20',
    'INFRA-27-10:20@kmutt.ac.th',
    2,
    '2022-05-27 10:20:00',
    20,
    NULL
),
 (
    'INFRA-26-10:00',
    'INFRA-26-10:00@kmutt.ac.th',
    2,
    '2022-05-26 10:00:00',
    20,
    NULL
),
 (
    'INFRA-26-11:45',
    'INFRA-26-11:45@kmutt.ac.th',
    2,
    '2022-05-26 11:45:00',
    10,
    NULL
)