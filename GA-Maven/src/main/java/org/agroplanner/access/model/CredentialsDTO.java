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
    public String username;
    public String password;

    // Profile Data (Registration only)
    public String firstName;
    public String lastName;
    public String email;
    public String phone;
    public Role requestedRole;

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
}
