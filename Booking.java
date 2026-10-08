import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Booking {
    private final String firstName;
    private final String surname;
    private final String phone;
    private final String movieTitle;
    private final String movieTime;
    private final List<String> seatIds;
    private final double totalPrice;
    private final boolean regular;
private final double discount;
    private final LocalDateTime timestamp;

    public Booking(String firstName, String surname, String phone, String movieTitle, String movieTime, List<String> seatIds) {
        this.firstName = firstName;
        this.surname = surname;
        this.phone = phone;
        this.movieTitle = movieTitle;
        this.movieTime = movieTime;
        this.seatIds = new ArrayList<>(seatIds);
        Collections.sort(this.seatIds);
        double subtotal = calculateTotal();
this.regular = countPastBookings() >= 3;
this.discount = regular ? subtotal * 0.10 : 0.0;
this.totalPrice = subtotal - discount;
        this.timestamp = LocalDateTime.now();

        // Automatically save to local file upon creation
        saveToFile();
    }

    private int countPastBookings() {
    int count = 0;
    java.io.File file = new java.io.File("cinema_bookings.txt");
    if (!file.exists()) return 0;
    try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file))) {
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length > 2 && parts[2].equals(phone)) count++;
        }
    } catch (IOException e) {
        System.err.println("Could not read past bookings: " + e.getMessage());
    }
    return count;
}

    private double calculateTotal() {
        double total = 0.0;
        for (String seatId : seatIds) {
            char row = seatId.charAt(0);
            if (row == 'A' || row == 'B') total += 200.0;
            else if (row == 'C') total += 150.0;
            else total += 120.0;
        }
        return total;
    }
    private String initials() {
    return firstName.substring(0, 1).toUpperCase() + "." +
           surname.substring(0, 1).toUpperCase() + ".";
}

    private void saveToFile() {
        String fileName = "cinema_bookings.txt";
        // Appends to the file so previous bookings aren't overwritten
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            String formattedTime = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
           String record = String.format("%s,%s,%s,%d,%s,%s,%s,%.2f,%s,%s\n",
        firstName, surname, phone, seatIds.size(),
        movieTitle, "Universal-Day", movieTime, totalPrice,
        String.join("|", seatIds), formattedTime);
            writer.write(record);
        } catch (IOException e) {
            System.err.println("Error saving booking to file: " + e.getMessage());
        }
    }

    public void saveTicket(String email) {
    String name = "ticket_" + phone + ".txt";
    try (BufferedWriter w = new BufferedWriter(new FileWriter(name, true))) {
        w.write("To: " + email + "\n" + getReceipt() + "\n\n");
    } catch (IOException e) {
        System.err.println("Could not save ticket: " + e.getMessage());
    }
}

    public String getReceipt() {
        return "========================================\n" +
                "       KBY CINEMA BOOKING RECEIPT       \n" +
                "========================================\n" +
                " Customer: " + firstName + " " + surname + "\n" +
                " Customer: " + firstName + " " + surname + "\n" +
                " Initials: " + initials() + "\n" +
                " Contact:  " + phone + "\n" +
                " Day:      Today (Universal)\n" +
                " Movie:    " + movieTitle + "\n" +
                " Time:     " + movieTime + "\n" +
                " Seats:    " + String.join(" ", seatIds) + "\n" +
                "----------------------------------------\n" +
                (regular ? " Regular discount (10%): -R" + String.format("%.2f", discount) + "\n" : "") +
                "========================================\n" +
                " Saved locally to: cinema_bookings.txt\n" +
                " Thank you for choosing KBY Cinema!";
    }
}