package custom_classes;

import po_utils.TestData;

public class Price implements TestData {

    public final double value;

    // The private constructor
    private Price(double priceValue) {
        this.value = priceValue;
    }

    public static Price fromString(String s) {
        return new Price(Double.parseDouble(s));
    }

    public double value() {
        return this.value;
    }

}
