package custom_classes;

import po_utils.TestData;

public class Telephones implements TestData {

    public final String value;

    public static final String[] examples = {
        "6085551023",
        "6085551749",
        "6085558763",
        "6085553198"
    };

    private Telephones(String telephoneString) {
        this.value = telephoneString;
    }

    public static Telephones fromString(String s) {
        return new Telephones(s);
    }

    public String value() {
        return this.value;
    }
}