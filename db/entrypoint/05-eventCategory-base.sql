USE oasip;

DELETE FROM eventCategory;
ALTER TABLE eventCategory AUTO_INCREMENT = 1;

INSERT INTO
  eventCategory (
    eventCategoryName,
    eventCategoryDescription,
    eventDuration
  )
VALUES
  (
    'Project Management Clinic',
    'ตารางนัดหมายนี้ใช้สำหรับนัดหมาย project management clinic ในวิชา INT221 integrated project I ให้นักศึกษาเตรียมเอกสารที่เกี่ยวข้องเพื่อแสดงระหว่างขอคำปรึกษา',
    30
  ),
  (
    'DevOps/Infra Clinic',
    'Use this event category for DevOps/Infra clinic.',
    20
  ),
  (
    'Database Clinic',
    'ตารางนัดหมายนี้ใช้สำหรับนัดหมาย database clinic ในวิชา INT221 integrated project I',
    15
  ),
  (
    'Client-side Clinic',
    'ตารางนัดหมายนี้ใช้สำหรับนัดหมาย client-side clinic ในวิชา INT221 integrated project I',
    30
  ),
  ('Server-side Clinic', NULL, 30);