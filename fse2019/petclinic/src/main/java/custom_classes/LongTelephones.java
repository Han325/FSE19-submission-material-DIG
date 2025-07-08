package custom_classes;

import po_utils.TestData;

public class LongTelephones implements TestData {

    public final String value;

    public static final String[] examples = {
        "60855510230093423423948029384",
        "6085551023",
        "6085551749",
        "6085558763"
    };

    private LongTelephones(String telephoneString) {
        this.value = telephoneString;
    }

    public static LongTelephones fromString(String s) {
        return new LongTelephones(s);
    }

    public String value() {
        return this.value;
    }
}