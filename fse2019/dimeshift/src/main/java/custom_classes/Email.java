package custom_classes;
import po_utils.TestData;
import java.util.Random; // Make sure this import is present

public class Email implements TestData {

    // 1. The core value field
    // public static final Email ASD = new Email("asd@asd.com");
    // public static final Email COMPANY = new Email("name@company.com");
    public final String value;

    // 2. The placeholder values for EvoSuite
    private static final String[] placeholders = {
        "asd@asd.com",
        "name@company.com"
    };

    // 3. The private constructor to control instantiation
    private Email(String emailString) {
        this.value = emailString;
    }

    // 4. The LLM's Door: Creates an Email from any string.
    public static Email fromString(String s) {
        return new Email(s);
    }

    // 6. The Page Object compatibility method.
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