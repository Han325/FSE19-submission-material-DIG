package custom_classes;

import po_utils.TestData;

public class IncomeDescription implements TestData {

    public final String value;

    public static final String[] examples = {
        "Monthly Salary",
        "Consulting Fee",
        "Stock Dividend Payout",
        "Freelance Project Payment",
        "Rental Property Income"
    };

    private IncomeDescription(String incomeDescriptionString) {
        this.value = incomeDescriptionString;
    }

    public static IncomeDescription fromString(String s) {
        return new IncomeDescription(s);
    }

    public String value() {
        return this.value;
    }
}