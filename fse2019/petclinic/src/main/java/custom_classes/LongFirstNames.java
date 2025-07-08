package custom_classes;

import po_utils.TestData;

public class LongFirstNames implements TestData {

    public final String value;

    public static final String[] examples = {
        "George",
        "Betty",
        "Eduardo",
        "HaroldHaroldHaroldHaroldHaroldHaroldHaroldHaroldHaroldHarold"
    };

    private LongFirstNames(String firstNameString) {
        this.value = firstNameString;
    }

    public static LongFirstNames fromString(String s) {
        return new LongFirstNames(s);
    }

    public String value() {
        return this.value;
    }
}