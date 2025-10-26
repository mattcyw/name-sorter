package com.example.dd.nameSorter.model;

import lombok.Data;

/**
 * a person's name with given name and last name.
 */
@Data
public class Name implements Comparable<Name> {

    private static final int MIN_NAMES = 2;
    private static final int MAX_NAMES = 4;

    private String givenName;
    private String lastName;

    public Name(String fullName) {

        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }

        String[] parts = fullName.trim().split("\\s+");
        if (parts.length < MIN_NAMES) {
            throw new IllegalArgumentException("Full name must contain at least a given name and a last name");
        }

        if (parts.length > MAX_NAMES) {
            throw new IllegalArgumentException("Full name cannot contain more than three given names and a last name");
        }

        this.lastName = parts[parts.length - 1];
        this.givenName = String.join(" ", java.util.Arrays.copyOf(parts, parts.length - 1));
    }

    @Override
    public int compareTo(Name other) {
        int lastNameComparison = this.lastName.compareToIgnoreCase(other.lastName);
        if (lastNameComparison != 0) {
            return lastNameComparison;
        }
        return this.givenName.compareToIgnoreCase(other.givenName);
    }

    public String toString() {
        return this.givenName + " " + this.lastName;
    }
}
