// The NEW, aight custom_classes/Amount.java
package custom_classes;

import po_utils.TestData;

public class Amount implements TestData {

    // The core value field - now a double for realistic money
    public final double value;

    // The private constructor
    private Amount(double amountValue) {
        this.value = amountValue;
    }

    public static Amount fromString(String s) {
        return new Amount(Double.parseDouble(s));
    }

    public double value() {
        return this.value;
    }
}