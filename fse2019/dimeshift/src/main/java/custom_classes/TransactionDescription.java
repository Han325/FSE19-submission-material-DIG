package custom_classes;

import po_utils.TestData;

public class TransactionDescription implements TestData {

    public final String value;

    public static final String[] examples = {
        "amazon",
        "ebay"
    };

    private TransactionDescription(String transactionDescriptionString) {
        this.value = transactionDescriptionString;
    }

    public static TransactionDescription fromString(String s) {
        return new TransactionDescription(s);
    }

    public String value() {
        return this.value;
    }
}