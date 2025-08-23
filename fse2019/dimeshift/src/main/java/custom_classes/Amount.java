// The NEW, aight custom_classes/Amount.java
package custom_classes;

import po_utils.TestData;

public class Amount implements TestData {

    // It only holds the raw string. That's it.
    public final String value;

    // The constructor is 100% safe. It just stores the string.
    private Amount(String rawValue) {
        this.value = rawValue;
    }

    // The factory method is also 100% safe.
    public static Amount fromString(String s) {
        return new Amount(s);
    }
    
    // The PO method calls this to get the payload for the browser.
    public String value() {
        return this.value;
    }
}