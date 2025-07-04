// The NEW, aight custom_classes/Amount.java
package custom_classes;

import po_utils.TestData;
import java.util.Random;

public class Amount implements TestData {

    // The core value field - now a double for realistic money
    public final double value;

    // The private constructor
    private Amount(double amountValue) {
        this.value = amountValue;
    }

    // The LLM's Door: Creates an Amount from any string.
    public static Amount fromString(String s) {
        // Use Double.parseDouble to handle values like "125.50"
        return new Amount(Double.parseDouble(s));
    }

    // The Page Object compatibility method. Now returns double.
    // NOTE: The Page Object method `typeJS` will need to handle a double.
    // `String.valueOf(amount.value())` will work perfectly.
    public double value() {
        return this.value;
    }
}