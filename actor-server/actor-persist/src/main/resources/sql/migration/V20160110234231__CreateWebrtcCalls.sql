CREATE TABLE webrtc_calls (
  id BIGINT NOT NULL,
  initiator_user_id INT NOT NULL,
  receiver_user_id INT NOT NULL,
  PRIMARY KEY id
)