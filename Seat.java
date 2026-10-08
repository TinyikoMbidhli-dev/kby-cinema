public class Seat {
    private final String id;
    private final int row;
    private final int col;
    private boolean reserved;

    public Seat(String id, int row, int col) {
        this.id = id;
        this.row = row;
        this.col = col;
        this.reserved = false;
    }

    public String getId() { return id; }
    public int getRow() { return row; }
    public int getCol() { return col; }
    public boolean isReserved() { return reserved; }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    public double getPrice() {
        char r = id.charAt(0);
        if (r == 'A' || r == 'B') return 200.0;
        if (r == 'C') return 150.0;
        return 120.0;
    }

    @Override
    public String toString() {
        return id + (reserved ? " (Booked)" : " (Available)");
    }
}