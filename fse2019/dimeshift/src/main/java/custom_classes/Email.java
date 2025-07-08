package custom_classes;
import po_utils.TestData;

public class Email implements TestData {

    public final String value;

    private Email(String emailString) {
        this.value = emailString;
    }

    public static Email fromString(String s) {
        return new Email(s);
    }

    public String value() {
        return this.value;
    }

    // Optional: Add equals() and hashCode() for robustness
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return java.util.Objects.equals(value, email.value);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(value);
    }
}