package com.blockeq.stellarwallet.interfaces;

public interface WalletStore extends LocalStore {
    void setCloudStorageEnabled(boolean isEnabled);
    void clearCloudStorage();
}
