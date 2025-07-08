package custom_classes;

import po_utils.TestData;

public class Addresses implements TestData {

    public final String value;

    public static final String[] examples = {
        "110 W. Liberty St.",
        "638 Cardinal Ave.",
        "2693 Commerce St.",
        "563 Friendly St."
    };

    private Addresses(String addressString) {
        this.value = addressString;
    }

    public static Addresses fromString(String s) {
        return new Addresses(s);
    }

    public String value() {
        return this.value;
    }
}