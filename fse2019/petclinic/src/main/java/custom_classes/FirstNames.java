package custom_classes;

import po_utils.TestData;

public class FirstNames implements TestData {

    public final String value;

    public static final String[] examples = {
        "George",
        "Betty",
        "Eduardo",
        "Harold"
    };

    private FirstNames(String firstNameString) {
        this.value = firstNameString;
    }

    public static FirstNames fromString(String s) {
        return new FirstNames(s);
    }

    public String value() {
        return this.value;
    }
}