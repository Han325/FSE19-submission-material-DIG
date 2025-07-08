package custom_classes;

import po_utils.TestData;

public class LastNames implements TestData {

    public final String value;

    public static final String[] examples = {
        "Franklin",
        "Davis",
        "Rodriquez",
        "Black"
    };

    private LastNames(String lastNameString) {
        this.value = lastNameString;
    }

    public static LastNames fromString(String s) {
        return new LastNames(s);
    }

    public String value() {
        return this.value;
    }
}