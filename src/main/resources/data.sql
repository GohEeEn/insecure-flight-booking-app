-- specify that id is autogenerated
ALTER TABLE application.flights MODIFY flightID bigint NOT NULL AUTO_INCREMENT;

INSERT INTO flights(source, destination, departure_date_time, arrival_date_time) VALUES
('Sofia, Bulgaria', 'Dublin, Ireland', '2021-03-05 20:30:00', '2021-03-06 00:30:00'),
('Stockholm, Sweden', 'Dublin, Ireland', '2021-03-06 11:30:00', '2021-03-06 13:30:00'),
('Oslo, Norway', 'Dublin, Ireland', '2021-03-07 08:00:00', '2021-03-05 10:30:00'),
('Rio de Janeiro, Brazil', 'Dublin, Ireland', '2021-03-05 20:30:00', '2021-03-05 00:30:00'),
('Bucharest, Romania', 'Dublin, Ireland', '2021-03-05 20:30:00', '2021-03-05 00:30:00'),
('Manila, Philippines', 'Dublin, Ireland', '2021-03-05 20:30:00', '2021-03-05 00:30:00'),
('Seoul, South Korea', 'Dublin, Ireland', '2021-03-05 20:30:00', '2021-03-05 00:30:00'),
('Pyongyang, North Korea', 'Dublin, Ireland', '2021-03-05 20:30:00', '2021-03-05 00:30:00'),
('Tokyo, Japan', 'Dublin, Ireland', '2021-03-05 20:30:00', '2021-03-05 00:30:00'),
 ('Sofia, Bulgaria', 'Dublin, Ireland', '2021-03-01 20:30:00', '2021-03-02 00:30:00'),
('Sofia, Bulgaria', 'Dublin, Ireland', '2021-03-03 20:30:00', '2021-03-04 00:30:00'),
('Sofia, Bulgaria', 'Dublin, Ireland', '2021-03-12 20:30:00', '2021-03-13 00:30:00'),
('Stockholm, Sweden', 'Dublin, Ireland', '2021-03-12 11:30:00', '2021-03-12 13:30:00'),
('Oslo, Norway', 'Dublin, Ireland', '2021-03-12 08:00:00', '2021-03-12 10:30:00'),
('Rio de Janeiro, Brazil', 'Dublin, Ireland', '2021-03-12 20:30:00', '2021-03-13 00:30:00'),
('Bucharest, Romania', 'Dublin, Ireland', '2021-03-12 20:30:00', '2021-03-13 00:30:00'),
('Manila, Philippines', 'Dublin, Ireland', '2021-03-12 20:30:00', '2021-03-13 00:30:00'),
('Seoul, South Korea', 'Dublin, Ireland', '2021-03-12 20:30:00', '2021-03-13 00:30:00'),
('Pyongyang, North Korea', 'Dublin, Ireland', '2021-03-12 20:30:00', '2021-03-13 00:30:00'),
('Tokyo, Japan', 'Dublin, Ireland', '2021-03-12 20:30:00', '2021-03-13 00:30:00'),
('Sofia, Bulgaria', 'Dublin, Ireland', '2021-03-11 20:30:00', '2021-03-12 00:30:00'),
('Sofia, Bulgaria', 'Dublin, Ireland', '2021-03-10 20:30:00', '2021-03-11 00:30:00'),
('Sofia, Bulgaria', 'Dublin, Ireland', '2021-03-19 20:30:00', '2021-03-20 00:30:00'),
('Stockholm, Sweden', 'Dublin, Ireland', '2021-03-19 11:30:00', '2021-03-19 13:30:00'),
('Oslo, Norway', 'Dublin, Ireland', '2021-03-19 08:00:00', '2021-03-20 10:30:00'),
('Rio de Janeiro, Brazil', 'Dublin, Ireland', '2021-03-19 20:30:00', '2021-03-20 00:30:00'),
('Bucharest, Romania', 'Dublin, Ireland', '2021-03-19 20:30:00', '2021-03-20 00:30:00'),
('Manila, Philippines', 'Dublin, Ireland', '2021-03-19 20:30:00', '2021-03-20 00:30:00'),
('Seoul, South Korea', 'Dublin, Ireland', '2021-03-19 20:30:00', '2021-03-20 00:30:00'),
('Pyongyang, North Korea', 'Dublin, Ireland', '2021-03-19 20:30:00', '2021-03-20 00:30:00'),
('Tokyo, Japan', 'Dublin, Ireland', '2021-03-19 20:30:00', '2021-03-20 00:30:00'),
('Sofia, Bulgaria', 'Dublin, Ireland', '2021-03-18 20:30:00', '2021-03-19 00:30:00'),
('Sofia, Bulgaria', 'Dublin, Ireland', '2021-03-17 20:30:00', '2021-03-18 00:30:00'),
('Sofia, Bulgaria', 'Dublin, Ireland', '2021-03-26 20:30:00', '2021-03-27 00:30:00'),
('Stockholm, Sweden', 'Dublin, Ireland', '2021-03-26 11:30:00', '2021-03-26 13:30:00'),
('Oslo, Norway', 'Dublin, Ireland', '2021-03-26 08:00:00', '2021-03-26 10:30:00'),
('Rio de Janeiro, Brazil', 'Dublin, Ireland', '2021-03-26 20:30:00', '2021-03-27 00:30:00'),
('Bucharest, Romania', 'Dublin, Ireland', '2021-03-26 20:30:00', '2021-03-27 00:30:00'),
('Manila, Philippines', 'Dublin, Ireland', '2021-03-26 20:30:00', '2021-03-27 00:30:00'),
('Seoul, South Korea', 'Dublin, Ireland', '2021-03-26 20:30:00', '2021-03-27 00:30:00'),
('Pyongyang, North Korea', 'Dublin, Ireland', '2021-03-26 20:30:00', '2021-03-27 00:30:00'),
('Tokyo, Japan', 'Dublin, Ireland', '2021-03-26 20:30:00', '2021-03-27 00:30:00'),
('Sofia, Bulgaria', 'Dublin, Ireland', '2021-03-25 20:30:00', '2021-03-26 00:30:00'),
('Sofia, Bulgaria', 'Dublin, Ireland', '2021-03-24 20:30:00', '2021-03-25 00:30:00');

ALTER TABLE application.credit_cards MODIFY id bigint NOT NULL AUTO_INCREMENT;

ALTER TABLE application.reservations MODIFY id bigint NOT NULL AUTO_INCREMENT;

INSERT INTO reservations(email, flight_flightid, cancelled) VALUES
('slav@ucdconnect.ie', 3, false),
('sean@ucdconnect.ie', 2, false),
('vincent@ucdconnect.ie', 5, false),
('ola@ucdconnect.ie', 1, false);


ALTER TABLE application.users MODIFY registrationID bigint NOT NULL AUTO_INCREMENT;

INSERT INTO users(name, surname, username, password, role, phone, email, address) VALUES
('slav', 'denisov', 'Slav', 'Slav1234', 'member', '0830881242', 'slav@ucdconnect.ie', '10 beach bay, smithfield'),
('ola', 'akintola', 'Ola', 'Ola1234', 'member', '0830881243', 'ola@ucdconnect.ie', '13 gunners bay , automaticfield'),
('vincent', 'goh', 'Vincent', 'Vincent1234', 'member', '0830881244', 'vincent@ucdconnect.ie', '69 miami bay area, awesomefield'),
('sean', 'mcloughlin', 'Sean', 'Sean1234', 'member', '0830881245', 'sean@ucdconnect.ie', '27 day dreams, bringfield');

ALTER TABLE application.guests MODIFY id bigint NOT NULL AUTO_INCREMENT;

INSERT INTO guests(name, surname, phone, email, address) VALUES
('joe', 'blogg', '0892209191', 'olamoiakin@gmail.com', '1 no mans land, mainfield');

ALTER TABLE application.passengers MODIFY id bigint NOT NULL AUTO_INCREMENT;

INSERT INTO passengers(name, surname, phone, email, address) VALUES
('passengername', 'passengerlastname', '0892209191', 'p1@gmail.com', 'no known residence, mainfield');

INSERT INTO credit_cards(cardholder_name, card_number, type, expiration_month, expiration_year, security_code) VALUES
("Sean", "111-222-3333", "VISA", 10, 2022, "666");

