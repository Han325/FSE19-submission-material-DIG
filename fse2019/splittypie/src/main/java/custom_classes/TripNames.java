package custom_classes;

import po_utils.TestData;

public class TripNames implements TestData {

    public final String value;

    public static final String[] examples = {
        "Trip To Barcelona",
        "Trip to Rome",
        "Mirabilandia",
        "Restaurant"
    };

    private TripNames(String tripNameString) {
        this.value = tripNameString;
    }

    public static TripNames fromString(String s) {
        return new TripNames(s);
    }

    public String value() {
        return this.value;
    }
}