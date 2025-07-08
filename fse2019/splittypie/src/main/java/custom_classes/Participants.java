package custom_classes;

import po_utils.TestData;

public class Participants implements TestData {

    public final String value;

    public static final String[] examples = {
        "Matteo",
        "Marco",
        "John",
        "Mike",
        "Mark",
        "Luke"
    };

    private Participants(String participantString) {
        this.value = participantString;
    }

    public static Participants fromString(String s) {
        return new Participants(s);
    }

    public String value() {
        return this.value;
    }
}