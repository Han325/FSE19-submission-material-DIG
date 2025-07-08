package custom_classes;

import po_utils.TestData;

public class Transactions implements TestData {

    public final String value;

    public static final String[] examples = {
        "Shopping",
        "Tickets",
        "Museum",
        "Lunch",
        "Dinner",
        "Disco"
    };

    private Transactions(String transactionString) {
        this.value = transactionString;
    }

    public static Transactions fromString(String s) {
        return new Transactions(s);
    }

    public String value() {
        return this.value;
    }
}