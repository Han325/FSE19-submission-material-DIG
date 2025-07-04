package custom_classes;

import po_utils.TestData;
import java.util.Random;

public class IncomeDescription implements TestData {

    // public static final IncomeDescription SCHOLARSHIP = new IncomeDescription("scholarship");
    // public static final IncomeDescription SALARY = new IncomeDescription("salary");
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