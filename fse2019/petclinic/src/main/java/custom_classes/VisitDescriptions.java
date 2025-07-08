package custom_classes;

import po_utils.TestData;

public class VisitDescriptions implements TestData {

    public final String value;

    public static final String[] examples = {
        "I don't know",
        "Should be ok soon",
        "He is not fine isn't he",
        "I should check"
    };

    private VisitDescriptions(String visitDescriptionString) {
        this.value = visitDescriptionString;
    }

    public static VisitDescriptions fromString(String s) {
        return new VisitDescriptions(s);
    }

    public String value() {
        return this.value;
    }
}