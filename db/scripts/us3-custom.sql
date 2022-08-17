USE oasip;

INSERT INTO
  eventCategory (eventCategoryName, eventDuration)
VALUES
  ('Frontend', 30),
  ('Backend', 30),
  ('Database', 30),
  ('DevOps', 30),
  ('Project Management', 30);

INSERT INTO
  event (
    bookingName,
    bookingEmail,
    eventStartTime,
    eventDuration,
    eventNotes,
    eventCategoryId
  )
VALUES
  (
    'Manee Meerai',
    'maneemeerai@gmail.com',
    '2022-04-25 11:00:00',
    45,
    'Where am I going? Help.',
    2
  ),
  (
    'Somchai Jaidee',
    'somchai007@gmail.com',
    '2022-04-26 09:00:00',
    30,
    NULL,
    1
  ),
  (
    'Thanawat Naeching',
    'thanawat@gmail.com',
    '2022-04-27 10:00:00',
    15,
    'bing bong',
    5
  ),
  (
    'Tawan Muadmuenwai',
    'tawan@gmail.com',
    '2022-05-27 23:00:00',
    30,
    'brrrrrr',
    2
  ),
  (
    'Manassinee Vejvithan',
    'manassinee@gmail.com',
    '2022-05-28 11:00:00',
    25,
    'wat now',
    3
  );