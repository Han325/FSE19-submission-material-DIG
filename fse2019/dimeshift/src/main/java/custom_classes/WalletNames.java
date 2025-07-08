package custom_classes;

import po_utils.TestData;

public class WalletNames implements TestData {

    public final String value;

    public static final String[] examples = {
        "Personal",
        "Company",
        "Private"
    };

    private WalletNames(String walletNameString) {
        this.value = walletNameString;
    }

    public static WalletNames fromString(String s) {
        return new WalletNames(s);
    }

    public String value() {
        return this.value;
    }
}