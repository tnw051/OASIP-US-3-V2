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
