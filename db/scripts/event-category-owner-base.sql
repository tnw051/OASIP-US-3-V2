USE oasip;

DELETE FROM
  eventCategoryOwner;

ALTER TABLE
  eventCategoryOwner AUTO_INCREMENT = 1;

INSERT INTO
  eventCategoryOwner (userId, eventCategoryId)
VALUES
  (2, 1),
  (5, 2),
  (2, 2),
  (6, 3),
  (4, 4),
  (3, 5),
  (2, 5);