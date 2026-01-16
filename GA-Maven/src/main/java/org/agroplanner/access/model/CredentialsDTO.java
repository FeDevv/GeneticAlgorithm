package org.agroplanner.access.model;

/**
 * Data Transfer Object (DTO) for transporting authentication and registration data.
 * <p>
 * Used to decouple the View layer (which populates this object) from the Service layer
 * (which consumes it), facilitating data aggregation without exposing domain entities directly.
 * </p>
 */
public class CredentialsDTO {

    // Core Authentication Data
    private String username;
    private String password;

    // Profile Data (Registration only)
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Role requestedRole;

    public CredentialsDTO() { }

    // Constructor for Login
    public CredentialsDTO(String u, String p) {
        this.username = u;
        this.password = p;
    }

    // Constructor for Registration
    public CredentialsDTO(String u, String p, String fname, String lname, String email, String phone, Role role) {
        this.username = u;
        this.password = p;
        this.firstName = fname;
        this.lastName = lname;
        this.email = email;
        this.phone = phone;
        this.requestedRole = role;
    }

    // --- GETTERS & SETTERS ---

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Role getRequestedRole() { return requestedRole; }
    public void setRequestedRole(Role requestedRole) { this.requestedRole = requestedRole; }
}
