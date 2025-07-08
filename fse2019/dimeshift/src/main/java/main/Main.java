package main;

import custom_classes.*;
import po_utils.ResetAppState;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        WalletNames COMPANY = WalletNames.fromString("Company");
        WalletNames PERSONAL = WalletNames.fromString("Personal");


        ResetAppState.reset();
        ClassUnderTest classUnderTest0 = new ClassUnderTest();
        classUnderTest0.addWalletWalletsManagerPage();
        classUnderTest0.addAddWalletPage(COMPANY);
        classUnderTest0 = new ClassUnderTest();
        ResetAppState.reset();
        classUnderTest0.addWalletWalletsManagerPage();
        classUnderTest0.addAddWalletPage(PERSONAL);
    }
}
