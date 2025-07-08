package custom_classes;

import po_utils.TestData;

public class Currencies implements TestData {

    public final String value;

    public static final String[] examples = {
        "United States dollar (USD)",
        "Euro (EUR)",
        "Pound sterling (GBP)",
        "Polish złoty (PLN)",
        "Swiss franc (CHF)",
        "Czech koruna (CZK)",
        "Croatian kuna (HRK)",
        "Romanian leu (RON)",
        "Bulgarian lev (BGN)",
        "Russian ruble (RUB)"
    };

    private Currencies(String currencyString) {
        this.value = currencyString;
    }

    public static Currencies fromString(String s) {
        return new Currencies(s);
    }

    public String value() {
        return this.value;
    }
}