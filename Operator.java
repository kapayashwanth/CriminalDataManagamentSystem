import java.io.Serializable;

public class Operator implements Serializable {
    private static final long serialVersionUID = 2L; // Different ID

    private int id;
    private String name;
    private String password; // !! INSECURE: Store hashed password in real apps !!

    public Operator(int id, String name, String password) {
        this.id = id;
        this.name = name;
        this.password = password;
    }

    // --- Getters ---
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    } // Used for login validation

    @Override
    public String toString() {
        return "ID: " + id + ", Name: " + name;
    }
}

