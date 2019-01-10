package com.blockeq.stellarwallet.interfaces;

import com.blockeq.stellarwallet.models.BasicBalance;

import java.util.ArrayList;

public interface CloudNodeStorage {
    /**
     * the operation is async.
     */
    void saveAccountId(String accountId);
    /**
     * the operation is async.
     */
    void saveBalances(ArrayList<BasicBalance> list);
    /**
     * the operation is async.
     */
    void clearNode();
}
