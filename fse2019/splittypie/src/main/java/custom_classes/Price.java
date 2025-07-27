package custom_classes;

import po_utils.TestData;

public class Price implements TestData {

    public final String value;

    // The private constructor
    private Price(String priceValue) {
        this.value = priceValue;
    }

    public static Price fromString(String s) {
        return new Price(s);
    }

    public String value() {
        return this.value;
    }

}
