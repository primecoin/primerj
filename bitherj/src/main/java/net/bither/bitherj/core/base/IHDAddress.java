package net.bither.bitherj.core.base;

import net.bither.bitherj.core.AbstractHD;

public interface IHDAddress {

    int getHdAccountId();

    void setHdAccountId(int hdAccountId);

    AbstractHD.PathType getPathType();

    void setPathType(AbstractHD.PathType pathType);

    int getAddressIndex();

    void setAddressIndex(int addressIndex);

    boolean isIssued();

    void setIssued(boolean isIssued);

    String getAddress();

    void setAddress(String address);

    byte[] getRedeem();

    void setRedeem(byte[] redeem);

    boolean isSyncComplete();

    void setSyncComplete(boolean isSyncComplete);

}
