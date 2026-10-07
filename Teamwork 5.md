Team: Exchange student
2601764 Seungheon Sa
2601783 WASIER Marin
2601760 Hein, Patrick


1: 
Primary stakeholders (use the system directly):
- Owner : beneficial, application works, security, accesability, reports on occupancy and revenue
- Receptionist: reservation, payment, identification of the guest
- Housekeeping staff (service): room status (full or empty), which room has to be cleaned
- Administratior: edit room details and website information, gets notified about system failures
- Guest (users) : make resevation, details of the room, security of the application, contact to the service

Secondary stakholders (supports the system form outside):
- payment provider: processes the payments and returns the payment status


2:
- The application needs to show every room with its detail (functional)
- It must be possible to reserve a room and then pay for it (functional
- There is a sign in / sign up function (functional)
- Housekeeping staff need to know which room to clean (functional)
- The receptionist needs to know that the identification and the payment are correct. (functional)
- There are different pages for different levels of admin (functional)
- The website needs to be quick (a room search shows its result in max. 2 seconds) and it updates for other users when a room is booked or not (within 5 seconds) (functional)
- The website needs to be reliable (available 99 % of the time). In case of a failure we show our service contact, and an automatic monitoring service notifies the staff (functional)
- The ID and the password of the users and the payment details need to be secured (security)
- Normal users can't change a reservation (security)
- Employees can modify and extend reservations and room details, and they can change the website's information (functional & Maintainability)
- The application uses an external payment service (Compatibility)
- If the dates of a reservation are not valid, we block the booking (functional)

Additional requirements (so that all categories of the task are covered):
- Functional: guests filter rooms by dates and number of people; users reset a forgotten password; guests view and cancel their own reservations; confirmation email after a booking; owner sees occupancy and revenue reports; optional reminder email before arrival
- Performance: 100 users at the same time without slowdown; reports within 5 seconds
- Usability: booking in max. 5 steps; works on phone, tablet and desktop; accessible, with readable contrast and keyboard use; clear error messages, e.g. "password incorrect"
- Reliability: no double bookings; regular backups
- Security: role-based access for each employee level; input validation and session timeout
- Maintainability: modular subsystems; automated tests in the CI pipeline; Git version control
- Compatibility: common browsers; email service; monitoring service
- Constraints: GDPR; security rules of the payment provider; limited time and a team of three


3:
S1: Account and authentication
purpose: manage accounts, sign in, sign up, roles and security
input : email, password, name, credentials for sign in, passoword reset request
output: account create, error (password incorect, etc...), session with roles
functional: sign in, sign up, password reset
non-functional: passwords hashed, input validation and session timeout, clear error message

S2: Room catalogue
Purpose: show every room with its details
Input: none from the user
Output: rooms and details
Functional: show photos, price and capacity of each room
Non-functional: loads in max. 2 seconds, usable on phone and desktop, accessible, common browsers

S3: Room filters
Purpose: find the rooms that fit the request of the guest
Input: details of the reservation (number of people, dates, ...)
Output: list of rooms with these conditions
Functional: filters the rooms depending on the conditions, only rooms that are free are shown
Non-functional: result in max. 2 seconds, room status is up to date for all users

S4: Reservation management
Purpose: handle all reservations
Input: information about the reservation (room, dates, guest), request to cancel, modify or extend, ID check
Output: processed reservation, confirmation email, error if the dates are not valid
Functional: guests create, view and cancel reservations, the receptionist modifies and extendc them and checks ID and payment, invalid dates are blocked, confirmation is sent
Non-functional: no double bookings, backups, only the receptionist can modify, 100 users at the same time, up to date status, booking in max. 5 steps, email service

S5: Payment
Purpose: process the payments
Input: Card/PayPal/etc. information, amount
Output: payment status
Functional: proceed the payment, check the authentication of the payment, handle a failed payment
Non-functional: card data is not stored, rules of the payment provider, interface to the provider, payment status and reservation stay consistent

S6: Administration (Back-office)
Purpose: admin-only pages for the employees
Input: edits to room details, prices, website information and service contact; request for a report
Output: updated data, reports
Functional: show the admin-only pages by level, verify info and edit details on the website, show reports, show the service contact
Non-functional: role-based access, changes without code, reports in max. 5 seconds 

S7: Housekeeping Service
Purpose: tell the cleaners which room to clean
Input: room status update after cleaning
Output: cleaning lists, updated room status
Functional: list the rooms to clean, update the room status after cleaning
Non-functional: new status is visible to other users within 5 seconds

S8: Monitoring and Notification
Purpose: watch the system automatically
Input: system health data (automatic)
Output: notification for the admins or engineers
Functional: auto monitoring, system failure detection
Non-functional: availability of 99 %, service contact is shown when a failure happens, interface to the monitoring servic

Interaction of the subsystems:
- S1 checks who the user is and applies the role before S4, S6, S7 allow actions
- S3 takes the rooms from S2 and the availability from S4
- S4 asks S5 for the payment status and gives a confirmation/cancellation for the reservation
- S5 sends the payment request to the external payment provider
- S6 maintains the room data that S2 shows and is building the reports form S4 and S5 data
- S7 sets the room status that S3 uses for the availability
- watches all subsystems and notifies the staff (admins/engineers)


4: QFD
For the apporach we need firstly to collect the needs of the stakeholders and sort them into Normal, Expected and Exciting. We need to rate how important is each need to the customer (scale from 1 to 10 is helpful). After this we are going to transalte each need into a measurable technical requirement and rank the needs by importance

Customer voice table & technical requirements: customer need / stakeholder / type / importance (1 to 10)
1. reserve a room and pay online / guest / normal / 10
2. no double bookings / guest, owner / expected / 10
3. personal and payment data are safe / guest, owner / expected / 9
4. see quickly which rooms are free / guest, receptionist / normal / 9
5. website works reliably / owner / expected / 8
6. cleaning rooms / Housekeeping staff / normal / 7
7. check identification and payment correctly / receptionist / normal / 7
8. see occupancy and revenue / owner / normal / 6
9. edit room and website information / Admin / normal / 6
10. easy use on phone / guest / expected / 5
11. clear error message / guest / expected / 5
12. reminder before arrival / guest / exciting / 3

The priority for steps 1 to 4 is "high". They need to be built first.
Steps 5 to 10 have a "medium" priority while 11 and 12 are "low" priority and are built last or optional

5:
![Alt text](<Images/Hotel Room Reservation Software.jpg>)
