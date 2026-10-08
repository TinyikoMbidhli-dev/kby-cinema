# KBY Cinema

A desktop movie ticket booking system written in **Java Swing**. Built as a group project for NCOS516 (Object Oriented Programming) at Sol Plaatje University.

## Features

- Choose from 5 movies and 4 show times
- Customer details form with input validation (10-digit phone number, 1 to 10 tickets)
- Seat map of 50 seats (rows A to E, 10 seats each): green is available, yellow is selected, red is booked
- No double booking, and booked seats stay red after the program is restarted
- Seat prices: rows A and B cost R200, row C costs R150, rows D and E cost R120
- 10% discount for regular customers (3 or more past bookings, matched by phone number)
- Receipt with the customer's initials
- Movie reviews with 1 to 5 star ratings and an average shown on each movie
- Ticket sent by email (optional, see below)
- Bookings and reviews are saved to text files

## Project structure

| File | What it does |
|---|---|
| `GuiProject.java` | All screens and the program entry point (`main`) |
| `Seat.java` | One seat: ID, reserved status, price by row |
| `Booking.java` | One booking: customer details, total, discount, receipt, saving to file |
| `Review.java` | Saves and loads reviews, calculates the average rating |
| `Mailer.java` | Sends the ticket by email using Jakarta Mail |
| `lib/` | Jakarta Mail and Jakarta Activation libraries (.jar files) |

## How to run

**Requirements:** Java JDK 17 or newer, and VS Code or IntelliJ IDEA.

1. Clone or download this repository.
2. Open the folder in your IDE.
3. Add the two `.jar` files in `lib/` to your project's libraries. VS Code does this automatically for a folder called `lib`.
4. Run `GuiProject.java`.

## Movie posters

The poster images are not included in this repository. The program still runs without them and shows the movie name instead.

To add your own, put image files in the project folder with these exact names:

`avengers.jpg`, `blackpanther.jpg`, `avatar.jpg`, `missionimpossible.jpg`, `insideout.jpg`

Only use images you have the right to use.

## Email tickets (optional)

The program reads the email login from two environment variables, so **no passwords are stored in the code**:

| Variable | Value |
|---|---|
| `CINEMA_MAIL_USER` | A Gmail address that will send the tickets |
| `CINEMA_MAIL_PASS` | A Gmail **app password** (16 characters, with no spaces) |

To get an app password: turn on 2-Step Verification for the Gmail account, then create one under **Google Account → Security → App passwords**.

After setting the variables, **close and reopen your IDE** so it can read them.

If the variables are not set, the booking still works, and the program shows a message that the email could not be sent.

**Never commit your app password or upload it to GitHub.**

## Data files

When the program runs, it creates these files in the project folder. They are listed in `.gitignore` and should not be uploaded, because they can hold customers' names, phone numbers and emails:

- `cinema_bookings.txt`
- `cinema_reviews.txt`
- `ticket_<phone>.txt`

To reset all bookings, close the program and delete `cinema_bookings.txt`.

## Known limitations

- Data is stored in text files, not a database
- Reviews are collected right after booking, before the customer has watched the film
- SMS tickets are not supported

## Authors

Group project, Sol Plaatje University.
