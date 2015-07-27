package net.bither.bitherj.core;

import net.bither.bitherj.core.base.IHDAddress;

public class HDAddress extends Address implements IHDAddress {
    protected int hdAccountId;
    protected AbstractHD.PathType pathType;
    protected int addressIndex;
    protected boolean isIssued;
    protected byte[] redeem;

    public int getHdAccountId() {
        return hdAccountId;
    }

    public void setHdAccountId(int hdAccountId) {
        this.hdAccountId = hdAccountId;
    }

    public AbstractHD.PathType getPathType() {
        return this.pathType;
    }

    public void setPathType(AbstractHD.PathType pathType) {
        this.pathType = pathType;
    }

    public int getAddressIndex() {
        return this.addressIndex;
    }

    public void setAddressIndex(int addressIndex) {
        this.addressIndex = addressIndex;
    }

    public boolean isIssued() {
        return this.isIssued;
    }

    public void setIssued(boolean isIssued) {
        this.isIssued = isIssued;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public byte[] getRedeem() {
        return this.redeem;
    }

    public void setRedeem(byte[] redeem) {
        this.redeem = redeem;
    }

    public HDAddress() {
        super();
    }

    public HDAddress(String address, byte[] pubKey, long sortTime, boolean isSyncComplete,
                   boolean isFromXRandom, boolean isTrashed, String encryptPrivKey) {
        super(address, pubKey, sortTime, isSyncComplete, isFromXRandom, isTrashed, encryptPrivKey);
    }
}
