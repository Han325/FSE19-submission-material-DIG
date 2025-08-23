package po_utils;

import org.openqa.selenium.WebDriver;

public class ResetAppState {

    public static void reset(WebDriver driver){
        driver.get("http://webapp:4200");
        driver.quit();
    }

}