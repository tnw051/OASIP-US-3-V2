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
  )