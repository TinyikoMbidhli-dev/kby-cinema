import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;

public class GuiProject {

    // --- MODERN COLOR PALETTE ---
    private static final Color GREEN_AVAIL = new Color(46, 204, 113);
    private static final Color YELLOW_SEL  = new Color(241, 196, 15);
    private static final Color RED_BOOKED  = new Color(231, 76, 60);
    private static final Color ACCENT_RED  = new Color(231, 76, 60);
    private static final Color DARK_BG     = new Color(20, 20, 30);
    private static final Color CARD_BG     = new Color(40, 40, 60);

    // --- MOVIE DATA ---
    private static final String[] MOVIES = {"Avengers", "Black Panther", "Avatar", "Mission Impossible", "Inside Out"};
    private static final String[] TIMES = {"10:00", "13:00", "16:00", "19:00"};
    
    // Map images to movies (Make sure these files are in your 'src' folder!)
    private static final Map<String, String> MOVIE_IMAGES = new HashMap<>();
    static {
        MOVIE_IMAGES.put("Avengers", "avengers.jpg");
        MOVIE_IMAGES.put("Black Panther", "blackpanther.jpg");
        MOVIE_IMAGES.put("Avatar", "avatar.jpg");
        MOVIE_IMAGES.put("Mission Impossible", "missionimpossible.jpg");
        MOVIE_IMAGES.put("Inside Out", "insideout.jpg");
    }

    // Reads saved bookings so booked seats stay red after the program is restarted
private static void loadSavedBookings() {
    File file = new File("cinema_bookings.txt");
    if (!file.exists()) return;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
        String line;
        while ((line = br.readLine()) != null) {
            String[] p = line.split(",");
            if (p.length < 7) continue;
            // find the field that looks like seats, e.g. A1|A2
            for (int i = 7; i < p.length; i++) {
                if (p[i].matches("[A-E]\\d+(\\|[A-E]\\d+)*")) {
                    Seat[][] grid = getGrid(p[4], p[6]); // p[4] = movie, p[6] = time
                    for (String id : p[i].split("\\|")) {
                        int r = id.charAt(0) - 'A';
                        int c = Integer.parseInt(id.substring(1)) - 1;
                        grid[r][c].setReserved(true);
                    }
                    break;
                }
            }
        }
    } catch (Exception e) {
        System.out.println("Could not load saved bookings: " + e.getMessage());
    }
}

    private static final Map<String, Seat[][]> theatreGrids = new HashMap<>();

    public static void main(String[] args) {
        loadSavedBookings();
        UIManager.put("OptionPane.background", DARK_BG);
        UIManager.put("Panel.background", DARK_BG);
        UIManager.put("OptionPane.messageForeground", Color.WHITE);
        SwingUtilities.invokeLater(GuiProject::showWelcomeScreen);
        new Thread(() -> {
    for (String m : MOVIES) {
        loadImage(MOVIE_IMAGES.get(m), 170, 250, 0.7f);
        loadImage(MOVIE_IMAGES.get(m), 220, 320, 1.0f);
    }
}).start();
    }

private static ImageIcon loadImage(String filename, int width, int height) {
    return loadImage(filename, width, height, 1.0f);
}

private static final Map<String, ImageIcon> IMAGE_CACHE = new HashMap<>();

private static synchronized ImageIcon loadImage(String filename, int width, int height, float alpha) {
    String key = filename + "_" + width + "x" + height + "_" + alpha;
    if (IMAGE_CACHE.containsKey(key)) return IMAGE_CACHE.get(key);
    try {
        InputStream is = GuiProject.class.getResourceAsStream("/" + filename);
        if (is == null) is = new FileInputStream(filename);

        BufferedImage img = ImageIO.read(is);
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(DARK_BG);
        g.fillRect(0, 0, width, height);
        g.setComposite(AlphaComposite.SrcOver.derive(alpha));
        g.drawImage(img, 0, 0, width, height, null);
        g.dispose();
        ImageIcon icon = new ImageIcon(out);
        IMAGE_CACHE.put(key, icon);
        return icon;
    } catch (Exception e) {
        System.out.println("Could not load image: " + filename);
    }
    return null;
}

    // --- UTILITY: STYLED BUTTON WITH HOVER EFFECT ---
    private static JButton styleBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setForeground(Color.WHITE);
        b.setBackground(ACCENT_RED);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(200, 45));
        
        // Add Hover Effect
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(new Color(255, 100, 100)); }
            public void mouseExited(MouseEvent e) { b.setBackground(ACCENT_RED); }
        });
        return b;
    }

    private static Seat[][] getGrid(String movie, String time) {
        String key = movie + "_" + time;
        if (!theatreGrids.containsKey(key)) {
            Seat[][] grid = new Seat[5][10];
            String[] rows = {"A", "B", "C", "D", "E"};
            for (int r = 0; r < 5; r++) {
                for (int c = 0; c < 10; c++) {
                    grid[r][c] = new Seat(rows[r] + (c + 1), r, c);
                }
            }
            theatreGrids.put(key, grid);
        }
        return theatreGrids.get(key);
    }

public static void showWelcomeScreen() {
    JFrame f = new JFrame("KBY Cinema");
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.setSize(1000, 700);
    f.setLocationRelativeTo(null);

    // Panel with a dark-to-wine gradient background
    JPanel p = new JPanel(new BorderLayout(20, 20)) {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setPaint(new GradientPaint(0, 0, new Color(10, 10, 20),
                    0, getHeight(), new Color(80, 20, 45)));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    };

    // --- Title ---
    JLabel title = new JLabel("K B Y   C I N E M A", SwingConstants.CENTER);
    title.setFont(new Font("Segoe UI", Font.BOLD, 52));
    title.setForeground(Color.WHITE);

    JLabel sub = new JLabel("Premium Movie Booking Experience", SwingConstants.CENTER);
    sub.setFont(new Font("Segoe UI", Font.PLAIN, 20));
    sub.setForeground(new Color(255, 120, 120));

    JPanel header = new JPanel(new GridLayout(2, 1));
    header.setOpaque(false);
    header.setBorder(BorderFactory.createEmptyBorder(50, 10, 0, 10));
    header.add(title);
    header.add(sub);
    p.add(header, BorderLayout.NORTH);

    // --- Poster strip (click a poster to book that movie) ---
    JPanel posters = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
    posters.setOpaque(false);

    for (String movie : MOVIES) {
        JLabel poster = new JLabel();
        ImageIcon icon = loadImage(MOVIE_IMAGES.get(movie), 170, 250, 0.7f);
        if (icon != null) {
            poster.setIcon(icon);
        } else {
            poster.setText(movie);
            poster.setForeground(Color.WHITE);
            poster.setHorizontalAlignment(SwingConstants.CENTER);
            poster.setPreferredSize(new Dimension(170, 250));
        }
        poster.setToolTipText("Book " + movie);
        poster.setCursor(new Cursor(Cursor.HAND_CURSOR));
        poster.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 80), 3));

        poster.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                poster.setBorder(BorderFactory.createLineBorder(ACCENT_RED, 3));
            }
            public void mouseExited(MouseEvent e) {
                poster.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 80), 3));
            }
            public void mouseClicked(MouseEvent e) {
                f.dispose();
                showTimeSelection(movie);
            }
        });
        posters.add(poster);
    }

    JPanel center = new JPanel(new GridBagLayout()); // keeps posters in the middle
    center.setOpaque(false);
    center.add(posters);
    p.add(center, BorderLayout.CENTER);

    // --- Buttons ---
    JButton b1 = styleBtn("Book a Movie");
    JButton b2 = styleBtn("View Schedule");
    JButton bReviews = styleBtn("Reviews");
bReviews.addActionListener(e -> { f.dispose(); showReviews(); });
    JButton b3 = styleBtn("Exit");

    JPanel btnP = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 10));
    btnP.setOpaque(false);
    btnP.setBorder(BorderFactory.createEmptyBorder(0, 0, 50, 0));
    btnP.add(b1); btnP.add(b2); btnP.add(bReviews); btnP.add(b3);
    p.add(btnP, BorderLayout.SOUTH);

    b1.addActionListener(e -> { f.dispose(); showMovieGallery(); });
    b2.addActionListener(e -> { f.dispose(); showSchedule(); });
    b3.addActionListener(e -> System.exit(0));

    f.setContentPane(p);
    f.setExtendedState(JFrame.MAXIMIZED_BOTH);
    f.setVisible(true);
}

    // ==========================================
    // 2. MOVIE GALLERY (WITH IMAGES)
    // ==========================================
    public static void showMovieGallery() {
        JFrame f = new JFrame("Select a Movie");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(950, 600);
        f.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(DARK_BG);

        JLabel title = new JLabel("Now Showing", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        mainPanel.add(title, BorderLayout.NORTH);

        // Grid for movie cards
        JPanel gallery = new JPanel(new GridLayout(1, 5, 15, 15)); // 1 row, 5 columns
        gallery.setOpaque(false);
        gallery.setBorder(BorderFactory.createEmptyBorder(20, 40, 40, 40));

        for (String movie : MOVIES) {
            gallery.add(createMovieCard(movie, f));
        }

        mainPanel.add(gallery, BorderLayout.CENTER);

        JButton back = styleBtn("← Back to Home");
        back.setPreferredSize(new Dimension(150, 40));
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        backPanel.setOpaque(false);
        backPanel.add(back);
        mainPanel.add(backPanel, BorderLayout.SOUTH);

        back.addActionListener(e -> { f.dispose(); showWelcomeScreen(); });

        f.setContentPane(mainPanel);
        f.setExtendedState(JFrame.MAXIMIZED_BOTH);
f.setVisible(true);
        f.setVisible(true);
    }

    private static JPanel createMovieCard(String movie, JFrame parentFrame) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 80), 2));
        
        // Load Image (200x300 for poster aspect ratio)
        String imgFile = MOVIE_IMAGES.get(movie);
        ImageIcon icon = loadImage(imgFile, 150, 220);
        
        JLabel imgLabel = new JLabel();
        if (icon != null) {
            imgLabel.setIcon(icon);
        } else {
            // Fallback if image missing
            imgLabel.setText("🎬 " + movie);
            imgLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            imgLabel.setForeground(Color.WHITE);
            imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
            imgLabel.setBackground(Color.DARK_GRAY);
            imgLabel.setOpaque(true);
            imgLabel.setPreferredSize(new Dimension(150, 220));
        }
        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(imgLabel, BorderLayout.CENTER);

        // Bottom info panel
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        double avg = Review.average(movie);
String text = avg > 0 ? movie + "  ★" + String.format("%.1f", avg) : movie;
JLabel titleLabel = new JLabel(text, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);
        infoPanel.add(titleLabel, BorderLayout.NORTH);

        JButton selectBtn = new JButton("Select");
        selectBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        selectBtn.setBackground(ACCENT_RED);
        selectBtn.setForeground(Color.WHITE);
        selectBtn.setFocusPainted(false);
        selectBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        infoPanel.add(selectBtn, BorderLayout.SOUTH);

        card.add(infoPanel, BorderLayout.SOUTH);

        selectBtn.addActionListener(e -> {
            parentFrame.dispose();
            showTimeSelection(movie);
        });

        return card;
    }

    // ==========================================
    // 3. TIME SELECTION (Styled)
    // ==========================================
    public static void showTimeSelection(String movie) {
        JFrame f = new JFrame("Select Time - " + movie);
        f.setSize(400, 350);
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel p = new JPanel(new BorderLayout(20, 20));
        p.setBackground(DARK_BG);

        JLabel title = new JLabel("Select Time for " + movie, SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(30, 10, 20, 10));
        p.add(title, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 80, 30, 80));

        for (String time : TIMES) {
            JButton btn = styleBtn(time);
            btn.setPreferredSize(new Dimension(200, 40));
            btn.addActionListener(e -> {
                f.dispose();
                showCustomerForm(movie, time);
            });
            btnPanel.add(btn);
        }

        p.add(btnPanel, BorderLayout.CENTER);

        JButton back = styleBtn("← Back");
        back.setPreferredSize(new Dimension(120, 35));
        JPanel backP = new JPanel(); backP.setOpaque(false); backP.add(back);
        p.add(backP, BorderLayout.SOUTH);
        back.addActionListener(e -> { f.dispose(); showMovieGallery(); });

        f.setContentPane(p);
        f.setExtendedState(JFrame.MAXIMIZED_BOTH);
f.setVisible(true);
        f.setVisible(true);
    }

    // ==========================================
    // 4. CUSTOMER FORM (Modernized)
    // ==========================================
    public static void showCustomerForm(String movie, String time) {
        JFrame f = new JFrame("Booking Details");
        f.setSize(500, 500);
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel p = new JPanel(new BorderLayout(15, 15));
        p.setBackground(DARK_BG);

        JLabel title = new JLabel("Enter Your Details", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(30, 10, 10, 10));
        p.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(5, 2, 15, 15));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        JTextField first = new JTextField();
        JTextField last = new JTextField();
        JTextField phone = new JTextField();
        JTextField tickets = new JTextField();
        
        // Style text fields
        for (JTextField tf : new JTextField[]{first, last, phone, tickets}) {
            tf.setBackground(new Color(40, 40, 60));
            tf.setForeground(Color.WHITE);
            tf.setCaretColor(Color.WHITE);
            tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 80)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
        }

        addRow(form, "First Name:", first);
        addRow(form, "Surname:", last);
        addRow(form, "Phone (10 digits):", phone);
        addRow(form, "No. of Tickets:", tickets);

        JLabel movieL = new JLabel("Movie: " + movie + " @ " + time);
        movieL.setForeground(Color.YELLOW);
        movieL.setFont(new Font("Segoe UI", Font.BOLD, 12));
        form.add(new JLabel("")); 
        form.add(movieL);

        p.add(form, BorderLayout.CENTER);

        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnP.setOpaque(false);
        JButton back = styleBtn("← Back");
        JButton next = styleBtn("Next: Select Seats →");
        btnP.add(back); btnP.add(next);
        p.add(btnP, BorderLayout.SOUTH);

        back.addActionListener(e -> { f.dispose(); showTimeSelection(movie); });
        next.addActionListener(e -> {
            String firstName = first.getText().trim();
            String lastName = last.getText().trim();
            String phoneNum = phone.getText().trim();
            String ticketsStr = tickets.getText().trim();

            if (firstName.isEmpty() || lastName.isEmpty()) {
                JOptionPane.showMessageDialog(f, "Please enter your full name.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!phoneNum.matches("\\d{10}")) {
                JOptionPane.showMessageDialog(f, "Phone number must be exactly 10 digits.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int numberOfTickets;
            try {
                numberOfTickets = Integer.parseInt(ticketsStr);
                if (numberOfTickets <= 0 || numberOfTickets > 10) throw new Exception();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(f, "Please enter a valid number of tickets (1-10).", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            f.dispose();
            showSeatScreen(movie, time, firstName, lastName, phoneNum, numberOfTickets);
        });

        f.setContentPane(p);
        f.setExtendedState(JFrame.MAXIMIZED_BOTH);
f.setVisible(true);
        f.setVisible(true);
    }

    private static void addRow(JPanel p, String label, JTextField f) {
        JLabel l = new JLabel(label);
        l.setForeground(Color.LIGHT_GRAY);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        p.add(l); p.add(f);
    }

    // ==========================================
    // 5. SEAT SELECTION (The Big Upgrade)
    // ==========================================
    public static void showSeatScreen(String movie, String time, String first, String last, String phone, int numTix) {
        JFrame f = new JFrame("Select Seats");
        f.setSize(1000, 750);
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

// --- PLAIN DARK BACKGROUND ---
JLabel bgLabel = new JLabel();
bgLabel.setBackground(DARK_BG);
bgLabel.setOpaque(true);
bgLabel.setLayout(new BorderLayout(10, 10));

        // --- TOP INFO BAR ---
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel info = new JLabel("Select " + numTix + " seat(s)  |  A-B: R200  |  C: R150  |  D-E: R120", SwingConstants.CENTER);
        info.setFont(new Font("Segoe UI", Font.BOLD, 14));
        info.setForeground(Color.WHITE);
        topBar.add(info, BorderLayout.CENTER);

        // Legend
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        legend.setOpaque(false);
        legend.add(createLegendItem("Available", GREEN_AVAIL));
        legend.add(createLegendItem("Selected", YELLOW_SEL));
        legend.add(createLegendItem("Booked", RED_BOOKED));
        topBar.add(legend, BorderLayout.EAST);
        
        bgLabel.add(topBar, BorderLayout.NORTH);

        // --- SEAT GRID WITH AISLE ---
        JPanel seatContainer = new JPanel(new GridLayout(6, 11, 10, 10)); 
        seatContainer.setOpaque(false);
        seatContainer.setBorder(BorderFactory.createEmptyBorder(20, 100, 20, 100));

        // Row 0: SCREEN label
        for (int c = 0; c < 11; c++) {
            JLabel screenLabel = new JLabel();
            if (c == 5) { 
                screenLabel.setText("");
            } else {
                screenLabel.setText("S C R E E N");
                screenLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                screenLabel.setForeground(new Color(100, 200, 255));
                screenLabel.setHorizontalAlignment(SwingConstants.CENTER);
            }
            seatContainer.add(screenLabel);
        }

        JButton[][] seats = new JButton[5][10];
        Set<String> selected = new HashSet<>();
        Seat[][] grid = getGrid(movie, time);
        String[] rows = {"A", "B", "C", "D", "E"};

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 11; c++) {
                if (c == 5) {
                    JPanel aisle = new JPanel();
                    aisle.setOpaque(false);
                    seatContainer.add(aisle);
                } else {
                    int actualCol = (c < 5) ? c : c - 1; 
                    String id = rows[r] + (actualCol + 1);
                    
                    JButton btn = new JButton(id);
                    btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    btn.setFocusPainted(false);
                    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    btn.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(Color.GRAY, 1, true), 
                        new EmptyBorder(5, 5, 5, 5)
                    ));

                    // Add Tooltip for Price!
                    btn.setToolTipText("Seat " + id + " - R" + grid[r][actualCol].getPrice());

                    if (grid[r][actualCol].isReserved()) {
                        btn.setBackground(RED_BOOKED);
                        btn.setForeground(Color.WHITE);
                        btn.setEnabled(false);
                    } else {
                        btn.setBackground(GREEN_AVAIL);
                        btn.setForeground(Color.WHITE);
                    }

                    final int rowIdx = r;
                    final int colIdx = actualCol;
                    
                    btn.addActionListener(e -> {
                        JButton clickedBtn = (JButton) e.getSource();
                        String sId = clickedBtn.getText();
                        
                        if (grid[rowIdx][colIdx].isReserved()) {
                            JOptionPane.showMessageDialog(f, "This seat is already booked!", "Unavailable", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        
                        if (selected.contains(sId)) {
                            clickedBtn.setBackground(GREEN_AVAIL);
                            clickedBtn.setForeground(Color.WHITE);
                            selected.remove(sId);
                        } else {
                            if (selected.size() >= numTix) {
                                JOptionPane.showMessageDialog(f, "You can only select " + numTix + " seat(s)!", "Limit Reached", JOptionPane.WARNING_MESSAGE);
                                return;
                            }
                            clickedBtn.setBackground(YELLOW_SEL);
                            clickedBtn.setForeground(Color.BLACK);
                            selected.add(sId);
                        }
                    });
                    seats[r][actualCol] = btn;
                    seatContainer.add(btn);
                }
            }
        }
        bgLabel.add(seatContainer, BorderLayout.CENTER);

        // --- BOTTOM BUTTONS ---
        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        btnP.setOpaque(false);
        
        JButton back = styleBtn("← Back");
        JButton confirm = styleBtn("Confirm Booking");
        
        btnP.add(back); btnP.add(confirm);
        bgLabel.add(btnP, BorderLayout.SOUTH);

        back.addActionListener(e -> { f.dispose(); showCustomerForm(movie, time); });
        
        confirm.addActionListener(e -> {
            if (selected.size() != numTix) {
                JOptionPane.showMessageDialog(f, "Please select exactly " + numTix + " seat(s).", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            for (String sId : selected) {
                int r = sId.charAt(0) - 'A';
                int c = Integer.parseInt(sId.substring(1)) - 1;
                grid[r][c].setReserved(true);
                seats[r][c].setBackground(RED_BOOKED);
                seats[r][c].setForeground(Color.WHITE);
                seats[r][c].setEnabled(false);
            }
            
            Booking booking = new Booking(first, last, phone, movie, time, new ArrayList<>(selected));
            
            JTextArea receiptArea = new JTextArea(booking.getReceipt());
            receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
            receiptArea.setEditable(false);
            receiptArea.setBackground(Color.WHITE);
            receiptArea.setForeground(Color.BLACK);
            
            String email = JOptionPane.showInputDialog(f, "Enter your email to receive your ticket:");
if (email.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.-]+$")) {
    booking.saveTicket(email);
    final String toEmail = email;
    new Thread(() -> {
        String error = Mailer.send(toEmail, "Your KBY Cinema Ticket", booking.getReceipt());
        SwingUtilities.invokeLater(() -> {
            if (error == null) {
                JOptionPane.showMessageDialog(null, "Ticket sent to " + toEmail + "!");
            } else {
                JOptionPane.showMessageDialog(null, "Booking saved, but the email failed:\n" + error, "Email problem", JOptionPane.WARNING_MESSAGE);
            }
        });
    }).start();
}
     else {
        JOptionPane.showMessageDialog(f, "That email doesn't look valid, so no ticket was sent.", "Invalid email", JOptionPane.WARNING_MESSAGE);
    }

            JOptionPane.showMessageDialog(f, receiptArea, "Booking Confirmed!", JOptionPane.INFORMATION_MESSAGE);
            Object[] options = {"1", "2", "3", "4", "5"};
int choice = JOptionPane.showOptionDialog(f, "Rate " + movie + " (1-5 stars)?", "Rate this movie",
        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[4]);
if (choice >= 0) {
    String comment = JOptionPane.showInputDialog(f, "Short comment (optional):");
    Review.save(movie, choice + 1, comment == null ? "" : comment.trim());
}
            selected.clear();
            f.dispose();
            showWelcomeScreen(); 
        });

        f.setContentPane(bgLabel);
        f.setExtendedState(JFrame.MAXIMIZED_BOTH);
f.setVisible(true);
        f.setVisible(true);
    }

    private static JPanel createLegendItem(String text, Color color) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        p.setOpaque(false);
        JLabel dot = new JLabel("●");
        dot.setForeground(color);
        dot.setFont(new Font("Segoe UI", Font.BOLD, 16));
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        p.add(dot); p.add(label);
        return p;
    }

    // ==========================================
    // 6. SCHEDULE SCREEN
    // ==========================================
    public static void showSchedule() {
        JFrame f = new JFrame("Movie Schedule");
        f.setSize(500, 600);
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(DARK_BG);

        JLabel title = new JLabel("Today's Schedule", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(30, 10, 20, 10));
        p.add(title, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setOpaque(false);
        list.setBorder(BorderFactory.createEmptyBorder(20, 60, 20, 60));

        for (String movie : MOVIES) {
            JLabel mLabel = new JLabel(" " + movie);
            mLabel.setForeground(new Color(100, 200, 255));
            mLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            list.add(mLabel);
            for (String time : TIMES) {
                JLabel tLabel = new JLabel("    ⏰ " + time);
                tLabel.setForeground(Color.WHITE);
                tLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                list.add(tLabel);
            }
            list.add(Box.createVerticalStrut(20));
        }

        JScrollPane scroll = new JScrollPane(list);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        p.add(scroll, BorderLayout.CENTER);

        JButton back = styleBtn("← Back");
        back.addActionListener(e -> { f.dispose(); showWelcomeScreen(); });
        JPanel bp = new JPanel(); bp.setOpaque(false); bp.add(back);
        p.add(bp, BorderLayout.SOUTH);

        f.setContentPane(p);
        f.setExtendedState(JFrame.MAXIMIZED_BOTH);
f.setVisible(true);
        f.setVisible(true);
    }

    public static void showReviews() {
    JFrame f = new JFrame("Reviews");
    f.setSize(600, 600);
    f.setLocationRelativeTo(null);
    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    JPanel p = new JPanel(new BorderLayout(10, 10));
    p.setBackground(DARK_BG);
    p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    JComboBox<String> box = new JComboBox<>(MOVIES);
    p.add(box, BorderLayout.NORTH);

    JTextArea area = new JTextArea();
    area.setEditable(false);
    area.setBackground(CARD_BG);
    area.setForeground(Color.WHITE);
    area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    area.setMargin(new Insets(10, 10, 10, 10));
    p.add(new JScrollPane(area), BorderLayout.CENTER);

    Runnable refresh = () -> {
        String movie = (String) box.getSelectedItem();
        java.util.List<String[]> list = Review.load(movie);
        StringBuilder sb = new StringBuilder();
        if (list.isEmpty()) {
            sb.append("No reviews yet for ").append(movie).append(".");
        } else {
            sb.append(String.format("Average: %.1f / 5  (%d reviews)%n%n", Review.average(movie), list.size()));
            for (String[] r : list) {
                sb.append("★".repeat(Integer.parseInt(r[1]))).append("\n");
                sb.append(r[2].isEmpty() ? "(no comment)" : r[2]).append("\n\n");
            }
        }
        area.setText(sb.toString());
        area.setCaretPosition(0);
    };
    box.addActionListener(e -> refresh.run());
    refresh.run();

    JButton back = styleBtn("← Back");
    back.addActionListener(e -> { f.dispose(); showWelcomeScreen(); });
    JPanel bp = new JPanel();
    bp.setOpaque(false);
    bp.add(back);
    p.add(bp, BorderLayout.SOUTH);

    f.setContentPane(p);
    f.setVisible(true);
}
}