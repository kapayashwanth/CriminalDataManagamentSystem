import java.io.Serializable;
import java.time.LocalDate; // Assuming you might want a date later

public class Criminal implements Serializable {
    // Required for Serialization
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String crimeCommitted;
    private String location;
    private String complainantName;
    private String firDescription;
    private String imagePath; // Store path to the image file in images/

    // Constructor
    public Criminal(int id, String name, String crimeCommitted, String location,
                    String complainantName, String firDescription, String imagePath) {
        this.id = id;
        this.name = name;
        this.crimeCommitted = crimeCommitted;
        this.location = location;
        this.complainantName = complainantName;
        this.firDescription = firDescription;
        this.imagePath = imagePath; // Path relative to project root, e.g., "images/criminal_1.jpg"
    }

    // --- Getters (Setters might be needed if you allow updates) ---
    public int getId() { return id; }
    public String getName() { return name; }
    public String getCrimeCommitted() { return crimeCommitted; }
    public String getLocation() { return location; }
    public String getComplainantName() { return complainantName; }
    public String getFirDescription() { return firDescription; }
    public String getImagePath() { return imagePath; }

    @Override
    public String toString() {
        return "ID: " + id + ", Name: " + name + ", Crime: " + crimeCommitted + ", Location: " + location
                + ", Complainant: " + complainantName + ", ImagePath: " + imagePath;
    }
}