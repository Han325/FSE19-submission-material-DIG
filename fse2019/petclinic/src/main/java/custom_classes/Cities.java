package custom_classes;

import po_utils.TestData;

public class Cities implements TestData {

    public final String value;

    public static final String[] examples = {
        "Madison",
        "McFarland",
        "Windsor",
        "Monona"
    };

    private Cities(String cityString) {
        this.value = cityString;
    }

    public static Cities fromString(String s) {
        return new Cities(s);
    }

    public String value() {
        return this.value;
    }
}