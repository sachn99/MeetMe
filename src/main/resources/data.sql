DROP SEQUENCE IF EXISTS calendar_seq;
CREATE SEQUENCE calendar_seq START WITH 1 INCREMENT BY 1;

INSERT INTO calendar (id) VALUES (NEXT VALUE FOR calendar_seq);
INSERT INTO calendar (id) VALUES (NEXT VALUE FOR calendar_seq);
INSERT INTO calendar (id) VALUES (NEXT VALUE FOR calendar_seq);

INSERT INTO app_user (name, calendar_id) VALUES ('sachin', 1);
INSERT INTO app_user (name, calendar_id) VALUES ('virat', 2);
INSERT INTO app_user (name, calendar_id) VALUES ('ponting', 3);


