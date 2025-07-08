package custom_classes;

import po_utils.TestData;

public class Goals implements TestData {

    public final String value;

    public static final String[] examples = {
        "ski",
        "museum",
        "house",
        "car"
    };

    private Goals(String goalString) {
        this.value = goalString;
    }

    public static Goals fromString(String s) {
        return new Goals(s);
    }

    public String value() {
        return this.value;
    }
}