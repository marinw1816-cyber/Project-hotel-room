Team: Exchange student
2601764 Seungheon Sa
2601783 WASIER Marin
2601760 Hein, Patrick


1: 
Owner : beneficial, application works, security, accesability
Receptionist: reservation, payment, identification,
Service custom: room (empty or full)
Users : make resevation, details of the room, security app, contact (service)


2:
-The application needs to show every room with their details. Have the posibility to reserve the room and them pay for it. sign in/ sign up function Service custom needs to know wich room to clean. 
For the receptionist needs to know the identification and payment are correct. Create diferent pages for diferentes level of admin. 
The website needs to be quicly and update for other users if the room is booked or no. 
The ebsite needs to be reliable and in case of failures we will write our service contact: Automatic monitoring service.
Secure the id and password of users and the payment detail. Make sure that the reversation can't be change bby normal users.
Employes should modify and extend reservations and room details. Change website's infos. 
Payment service
If the dates of reservations are not good, we need to block that access.

3:
S1: Account and authentication
input : email, password, name
sign in, sign up, security
output: account create, error (password incorect, etc...) 

S2: Room catalogue
input: none

output: rooms and details

S3: Room filters
input: Details of the reservation (nbr of people, dates...)
Filters the rooms depends on the condition
output: Lists of rooms with this conditions

S4: Reservation management
input: information about the reservation
Create, modify, delete, view the reservation
output: processed revervation

S5: Payment
input: Card/Paypal..etc information
Proceed the payment, check the authentication of the payment
output: Payments status

S6: Administration (Back-office)
input: none
Show the admin-only page for the admins. In admin-only page, they can verify info and edit details on website.
output: none

S7: Housekeeping Service
input: none
Tell cleaners which room to clean. Update room status after cleaning.
output: cleaning Lists

S8: Monitoring and Notification
input: none
Auto monitoring, system failure detections.
output: Notification for the admins or engineers

4:

5:
![Alt text](<Images/Hotel Room Reservation Software.jpg>)
