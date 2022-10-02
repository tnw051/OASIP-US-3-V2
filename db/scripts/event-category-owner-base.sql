USE oasip;

DELETE FROM
  event_category_owner;

ALTER TABLE
  event_category_owner AUTO_INCREMENT = 1;

INSERT INTO
  event_category_owner (userId, eventCategoryId)
VALUES
  (2, 1),
  (5, 2),
  (2, 2),
  (6, 3),
  (4, 4),
  (3, 5),
  (2, 5);