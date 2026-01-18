package org.agroplanner.access.model;

/**
 * Represents a registered user entity in the system domain.
 * <p>
 * This class uses the Builder pattern to handle the optionality of profile fields
 * and ensure object consistency during instantiation.
 * </p>
 */
public class User {
    private int id;
    private String username;
    private String password;
    private Role role;

    // Profile Information
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    public User() {}

    private User(Builder builder) {
        this.username = builder.username;
        this.password = builder.password;
        this.role = builder.role;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.email = builder.email;
        this.phone = builder.phone;
    }

    // --- Accessors ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getFullName() { return firstName + " " + lastName; }

    /**
     * Factory method creates a temporary Guest session user.
     * @return A non-persistent User instance with limited privileges.
     */
    public static User createGuestUser() {
        User guest = new User.Builder("guest_demo", "", Role.GUEST)
                .firstName("Guest")
                .lastName("User")
                .email("demo@agroplanner.org")
                .phone("0000000000")
                .build();
        guest.setId(-1);
        return guest;
    }

    // --- Builder Pattern ---
    public static class Builder {
        private String username;
        private String password;
        private Role role;

        private String firstName;
        private String lastName;
        private String email;
        private String phone;

        public Builder(String username, String password, Role role) {
            this.username = username;
            this.password = password;
            this.role = role;
        }

        public Builder firstName(String val) { this.firstName = val; return this; }
        public Builder lastName(String val) { this.lastName = val; return this; }
        public Builder email(String val) { this.email = val; return this; }
        public Builder phone(String val) { this.phone = val; return this; }

        public User build() {
            return new User(this);
        }
    }
}
