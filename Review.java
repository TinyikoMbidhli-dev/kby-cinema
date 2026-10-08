import java.io.*;
import java.util.*;

public class Review {
    private static final String FILE = "cinema_reviews.txt";

    public static void save(String movie, int stars, String comment) {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(FILE, true))) {
            String clean = comment.replace("|", "/").replace("\n", " ");
            w.write(movie + "|" + stars + "|" + clean + "\n");
        } catch (IOException e) {
            System.err.println("Could not save review: " + e.getMessage());
        }
    }

    public static java.util.List<String[]> load(String movie) {
        java.util.List<String[]> list = new ArrayList<>();
        File file = new File(FILE);
        if (!file.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|", 3);
                if (p.length == 3 && p[0].equals(movie)) list.add(p);
            }
        } catch (IOException e) {
            System.err.println("Could not read reviews: " + e.getMessage());
        }
        return list;
    }

    public static double average(String movie) {
        java.util.List<String[]> list = load(movie);
        if (list.isEmpty()) return 0;
        double sum = 0;
        for (String[] r : list) sum += Integer.parseInt(r[1]);
        return sum / list.size();
    }
}