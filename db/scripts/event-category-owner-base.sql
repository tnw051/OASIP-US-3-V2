USE oasip;

DELETE FROM
  eventCategoryOwner;

ALTER TABLE
  eventCategoryOwner AUTO_INCREMENT = 1;

INSERT INTO
  eventCategoryOwner (ownerEmail, eventCategoryId)
VALUES
  ('olarn.roj@kmutt.ac.th', 1),
  ('siam.yam@kmutt.ac.th', 2),
  ('olarn.roj@kmutt.ac.th', 2),
  ('sunisa.sat@kmutt.ac.th', 3),
  ('umaporn.sup@kmutt.ac.th', 4),
  ('pichet.limv@kmutt.ac.th', 5),
  ('olarn.roj@kmutt.ac.th', 5);