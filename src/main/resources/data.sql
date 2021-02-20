-- specify that id is autogenerated
ALTER TABLE library.flights MODIFY flightID int NOT NULL AUTO_INCREMENT;

INSERT INTO flights(source, destination, departure_date_time, arrival_date_time) VALUES
('Sofia, Bulgaria', 'Dublin, Ireland', '2021-03-05 20:30', '2021-03-05 00:30'),
('Stockholm, Sweden', 'Dublin, Ireland', '2021-03-06 11:30', '2021-03-06 13:30'),
('Oslo, Norway', 'Dublin, Ireland', '2021-03-07 08:00', '2021-03-05 10:30'),
('Rio de Janeiro, Brazil', 'Dublin, Ireland', '2021-03-05 20:30', '2021-03-05 00:30'),
('Bucharest, Romania', 'Dublin, Ireland', '2021-03-05 20:30', '2021-03-05 00:30'),
('Manila, Philippines', 'Dublin, Ireland', '2021-03-05 20:30', '2021-03-05 00:30'),
('Seoul, South Korea', 'Dublin, Ireland', '2021-03-05 20:30', '2021-03-05 00:30'),
('Pyongyang, North Korea', 'Dublin, Ireland', '2021-03-05 20:30', '2021-03-05 00:30'),
('Tokyo, Japan', 'Dublin, Ireland', '2021-03-05 20:30', '2021-03-05 00:30');



-- ALTER TABLE library.reservations MODIFY id int NOT NULL AUTO_INCREMENT;

INSERT INTO reservations(email, flight_reference) VALUES
('slav@ucdconnect.ie', 3),
('sean@ucdconnect.ie', 2),
('vincent@ucdconnect.ie', 5),
('ola@ucdconnect.ie', 1);


ALTER TABLE library.users MODIFY registrationID int NOT NULL AUTO_INCREMENT;

INSERT INTO users(name, surname, phone, email, address, credit_card_details, reservation_history, upcoming_reservations) VALUES
('slav', 'denisov', '0830881242', 'slav@ucdconnect.ie', '10 beach bay, smithfield', '8418 5823 1153 8818', '1,2', '7,12'),
('ola', 'akintola', '0830881243', 'ola@ucdconnect.ie', '13 gunners bay , automaticfield', '8418 5823 1153 8819', '3,4', '5,6'),
('vincent', 'goh', '0830881244', 'vincent@ucdconnect.ie', '69 miami bay area, awesomefield', '8418 5823 1153 8811', '17,18', '23,'),
('sean', 'mcloughlin', '0830881245', 'sean@ucdconnect.ie', '27 day dreams, bringfield', '8418 5823 1153 8812', '11,22', '');

-- ALTER TABLE library.guests MODIFY id int NOT NULL AUTO_INCREMENT;

INSERT INTO guests(name, surname, phone, email, address, credit_card_details ) VALUES
('joe', 'blogg', '0892209191', 'olamoiakin@gmail.com', '1 no mans land, mainfield', '1234 5678 1234 5678');