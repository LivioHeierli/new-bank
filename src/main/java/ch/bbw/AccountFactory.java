package ch.bbw;

import ch.bbw.accounts.*;

public class AccountFactory {
    /**
     * Next account id.
     */
    private long nextAccountId;

    public AccountFactory() {
        this.nextAccountId = 1000;
    }

    public Account createPromoYouthSavingsAccount() {
        String id = "Y-" + nextAccountId++;

        return new SavingsAccount(id);
    }

    public Account createSalaryAccount(long creditLimit) {
        String id = "P-" + nextAccountId++;

        return new SavingsAccount(id);
    }

    public Account createSavingsAccount(){
        String id = "S-" + nextAccountId++;

        return new SavingsAccount(id);
    }

}
