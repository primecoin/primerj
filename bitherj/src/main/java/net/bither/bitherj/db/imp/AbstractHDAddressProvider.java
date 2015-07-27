/*
 * Copyright 2014 http://Bither.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.bither.bitherj.db.imp;

import com.google.common.base.Function;
import net.bither.bitherj.core.*;
import net.bither.bitherj.core.base.IHDAddress;
import net.bither.bitherj.db.AbstractDb;
import net.bither.bitherj.db.IHDAddressProvider;
import net.bither.bitherj.db.imp.base.ICursor;
import net.bither.bitherj.db.imp.base.IDb;
import net.bither.bitherj.exception.AddressFormatException;
import net.bither.bitherj.utils.Base58;
import net.bither.bitherj.utils.Sha256Hash;
import net.bither.bitherj.utils.Utils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public abstract class AbstractHDAddressProvider extends AbstractProvider implements IHDAddressProvider {

    @Override
    public void addAddress(List<IHDAddress> addresses) {
        String sql = "insert into hd_addresses(hd_account_id,path_type,address_index,is_issued,address,redeem,is_synced) values(?,?,?,?,?,?,?)";
        IDb writeDb = this.getWriteDb();
        writeDb.beginTransaction();
        for (IHDAddress address : addresses) {
            this.execUpdate(writeDb, sql, new String[] {
                    Integer.toString(address.getHdAccountId())
                    , Integer.toString(address.getPathType().getValue())
                    , Integer.toString(address.getAddressIndex())
                    , address.isIssued() ? "1" : "0"
                    , address.getAddress()
                    , Base58.encode(address.getRedeem())
                    , address.isSyncComplete() ? "1" : "0"
            });
        }
        writeDb.endTransaction();
    }

    @Override
    public int maxHDMAddressPubIndex(int hdAccountId) {
        final int[] maxIndex = {-1};
        String sql = "select ifnull(max(address_index),-1) address_index from hd_addresses ";
        this.execQueryOneRecord(sql, null, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(ICursor c) {
                maxIndex[0] = c.getInt(0);
                return null;
            }
        });
        return maxIndex[0];
    }

    @Override
    public String externalAddress(int hdAccountId) {
        String sql = "select address from hd_addresses" +
                " where path_type=? and is_issued=? and hd_account_id=? order by address_index asc limit 1 ";
        final String[] address = {null};
        this.execQueryOneRecord(sql, new String[]{Integer.toString(AbstractHD.PathType.EXTERNAL_ROOT_PATH.getValue())
                , "0", Integer.toString(hdAccountId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor c) {
                int idColumn = c.getColumnIndex(AbstractDb.HDAddressesColumns.ADDRESS);
                if (idColumn != -1) {
                    address[0] = c.getString(idColumn);
                }
                return null;
            }
        });
        return address[0];
    }

    @Override
    public boolean hasAddress(int hdAccountId) {
        String sql = "select count(address) cnt from hd_addresses where hd_account_id=? ";
        final boolean[] hasAddress = {false};
        this.execQueryOneRecord(sql, new String[]{Integer.toString(hdAccountId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor c) {
                int idColumn = c.getColumnIndex("cnt");
                if (idColumn != -1) {
                    hasAddress[0] = c.getInt(idColumn) > 0;
                }
                return null;
            }
        });
        return hasAddress[0];
    }

    @Override
    public long getHDAccountConfirmedBalance(int hdAccountId) {
        final long[] sum = {0};
        String unspendOutSql = "select ifnull(sum(a.out_value),0) sum from outs a,txs b where a" +
                ".tx_hash=b.tx_hash " +
                "  and a.out_status=? and a.hd_account_id=? and b.block_no is not null";
        this.execQueryOneRecord(unspendOutSql, new String[]{Integer.toString(Out.OutStatus.unspent.getValue()), Integer.toString
                (hdAccountId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor c) {
                int idColumn = c.getColumnIndex("sum");
                if (idColumn != -1) {
                    sum[0] = c.getLong(idColumn);
                }
                return null;
            }
        });
        return sum[0];
    }

    @Override
    public HashSet<String> getBelongAccountAddresses(int hdAccountId, List<String> addressList) {
        final HashSet<String> addressSet = new HashSet<String>();

        List<String> temp = new ArrayList<String>();
        if (addressList != null) {
            for (String str : addressList) {
                temp.add(Utils.format("'%s'", str));
            }
        }
        String sql = Utils.format("select address from hd_addresses where hd_account_id=? and address in (%s) "
                , Utils.joinString(temp, ","));
        this.execQueryLoop(sql, new String[]{Integer.toString(hdAccountId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor c) {
                int idColumn = c.getColumnIndex(AbstractDb.HDAddressesColumns.ADDRESS);
                if (idColumn != -1) {
                    addressSet.add(c.getString(idColumn));
                }
                return null;
            }
        });
        return addressSet;
    }

    @Override
    public HashSet<String> getBelongAccountAddresses(List<String> addressList) {
        final HashSet<String> addressSet = new HashSet<String>();

        List<String> temp = new ArrayList<String>();
        if (addressList != null) {
            for (String str : addressList) {
                temp.add(Utils.format("'%s'", str));
            }
        }
        String sql = Utils.format("select address from hd_addresses where address in (%s) "
                , Utils.joinString(temp, ","));
        this.execQueryLoop(sql, null, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor c) {
                int idColumn = c.getColumnIndex(AbstractDb.HDAddressesColumns.ADDRESS);
                if (idColumn != -1) {
                    addressSet.add(c.getString(idColumn));
                }
                return null;
            }
        });
        return addressSet;
    }

    @Override
    public void updateIssuedIndex(int hdAccountId, AbstractHD.PathType pathType, int index) {
        String sql = "update hd_addresses set is_issued=? where path_type=? and address_index<=? and hd_account_id=?";
        this.execUpdate(sql, new String[]{"1", Integer.toString(pathType.getValue()), Integer.toString(index), Integer.toString(hdAccountId)});
    }

    @Override
    public int issuedIndex(int hdAccountId, AbstractHD.PathType pathType) {
        String sql = "select ifnull(max(address_index),-1) address_index " +
                " from hd_addresses" +
                " where path_type=? and is_issued=? and hd_account_id=?";
        final int[] issuedIndex = {-1};
        this.execQueryOneRecord(sql, new String[]{Integer.toString(pathType.getValue()), "1", String.valueOf(hdAccountId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(ICursor c) {
                int idColumn = c.getColumnIndex(AbstractDb.HDAddressesColumns.ADDRESS_INDEX);
                if (idColumn != -1) {
                    issuedIndex[0] = c.getInt(idColumn);
                }
                return null;
            }
        });
        return issuedIndex[0];
    }

    @Override
    public int allGeneratedAddressCount(int hdAccountId, AbstractHD.PathType pathType) {
        String sql = "select ifnull(count(address),0) count " +
                " from hd_addresses " +
                " where path_type=? and hd_account_id=?";
        final int[] count = {0};
        this.execQueryOneRecord(sql, new String[]{Integer.toString(pathType.getValue()), String.valueOf(hdAccountId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor c) {
                int idColumn = c.getColumnIndex("count");
                if (idColumn != -1) {
                    count[0] = c.getInt(idColumn);
                }
                return null;
            }
        });
        return count[0];
    }

    @Override
    public void updateSyncedForIndex(int hdAccountId, AbstractHD.PathType pathType, int index) {
        String sql = "update hd_addresses set is_synced=? where path_type=? and address_index>? and hd_account_id=?";
        this.execUpdate(sql, new String[]{"1", Integer.toString(pathType.getValue())
                , Integer.toString(index), Integer.toString(hdAccountId)});
    }

    @Override
    public void updateSyncedComplete(HDAddress address) {
        String sql = "update hd_addresses set is_synced=? where path_type=? and address_index>? and hd_account_id=?";
        this.execUpdate(sql, new String[]{"1", Integer.toString(address.getPathType().getValue())
                , Integer.toString(address.getAddressIndex()), Integer.toString(address.getHdAccountId())});
    }

    @Override
    public List<Tx> getHDAccountUnconfirmedTx(int hdAccountId) {
        String sql = "select distinct a.* " +
                " from txs a,addresses_txs b,hd_addresses c" +
                " where a.tx_hash=b.tx_hash and b.address=c.address and c.hd_account_id=? and a.block_no is null" +
                " order by a.tx_hash";
        final List<Tx> txList = new ArrayList<Tx>();
        final HashMap<Sha256Hash, Tx> txDict = new HashMap<Sha256Hash, Tx>();

        IDb db = this.getReadDb();
        this.execQueryLoop(db, sql, new String[]{Integer.toString(hdAccountId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor c) {
                Tx txItem = AbstractTxProvider.applyCursor(c);
                txItem.setIns(new ArrayList<In>());
                txItem.setOuts(new ArrayList<Out>());
                txList.add(txItem);
                txDict.put(new Sha256Hash(txItem.getTxHash()), txItem);
                return null;
            }
        });
        sql = "select distinct a.* " +
                " from ins a, txs b,addresses_txs c,hd_addresses d" +
                " where a.tx_hash=b.tx_hash and b.tx_hash=c.tx_hash and c.address=d.address" +
                "   and b.block_no is null and d.hd_account_id=?" +
                " order by a.tx_hash,a.in_sn";
        this.execQueryLoop(db, sql, new String[]{Integer.toString(hdAccountId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor c) {
                In inItem = AbstractTxProvider.applyCursorIn(c);
                Tx tx = txDict.get(new Sha256Hash(inItem.getTxHash()));
                if (tx != null) {
                    tx.getIns().add(inItem);
                }
                return null;
            }
        });
        sql = "select distinct a.* " +
                " from outs a, txs b,addresses_txs c,hd_addresses d" +
                " where a.tx_hash=b.tx_hash and b.tx_hash=c.tx_hash and c.address=d.address" +
                "   and b.block_no is null and d.hd_account_id=?" +
                " order by a.tx_hash,a.out_sn";
        this.execQueryLoop(db, sql, new String[]{Integer.toString(hdAccountId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor c) {
                Out out = AbstractTxProvider.applyCursorOut(c);
                Tx tx = txDict.get(new Sha256Hash(out.getTxHash()));
                if (tx != null) {
                    tx.getOuts().add(out);
                }
                return null;
            }
        });
        return txList;
    }

    @Override
    public List<byte[]> getPubs(int hdAccountId, AbstractHD.PathType pathType) {
        String sql = "select redeem from hd_addresses where path_type=? and hd_account_id=?";
        final List<byte[]> addressRedeemList = new ArrayList<byte[]>();
        this.execQueryLoop(sql, new String[]{Integer.toString(pathType.getValue()), Integer.toString(hdAccountId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor c) {
                int idColumn = c.getColumnIndex(AbstractDb.HDAddressesColumns.REDEEM);
                if (idColumn != -1) {
                    try {
                        addressRedeemList.add(Base58.decode(c.getString(idColumn)));
                    } catch (AddressFormatException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        });
        return addressRedeemList;
    }

    @Override
    public int getUnspendOutCountByHDAccountWithPath(int hdAccountId, AbstractHD.PathType pathType) {
        final int[] result = {0};
        String sql = "select count(tx_hash) cnt from outs where out_address in " +
                "(select address from hd_addresses where path_type =? and out_status=?) " +
                "and hd_account_id=?";
        this.execQueryOneRecord(sql, new String[]{Integer.toString(pathType.getValue())
                , Integer.toString(Out.OutStatus.unspent.getValue())
                , Integer.toString(hdAccountId)
        }, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor c) {
                int idColumn = c.getColumnIndex("cnt");
                if (idColumn != -1) {
                    result[0] = c.getInt(idColumn);
                }
                return null;
            }
        });
        return result[0];
    }

    @Override
    public List<Out> getUnspendOutByHDAccountWithPath(int hdAccountId, AbstractHD.PathType pathType) {
        String sql = "select * from outs where out_address in " +
                "(select address from hd_addresses where path_type =? and " +
                "out_status=?) " +
                "and hd_account_id=?";
        final List<Out> outList = new ArrayList<Out>();
        this.execQueryLoop(sql, new String[]{Integer.toString(pathType.getValue())
                , Integer.toString(Out.OutStatus.unspent.getValue())
                , Integer.toString(hdAccountId)
        }, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor c) {
                outList.add(AbstractTxProvider.applyCursorOut(c));
                return null;
            }
        });
        return outList;
    }

    @Override
    public HDAddress addressForPath(int hdAccountId, AbstractHD.PathType type, int index) {
        String sql = "select address,pub,path_type,address_index,is_issued," +
                "is_synced,hd_account_id from hd_addresses" +
                " where path_type=? and address_index=? and hd_account_id=?";
        final HDAddress[] accountAddress = {null};
        this.execQueryOneRecord(sql, new String[]{Integer.toString(type.getValue()), Integer.toString(index), Integer.toString(hdAccountId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor c) {
                accountAddress[0] = formatAddress(c);
                return null;
            }
        });
        return accountAddress[0];
    }

    @Override
    public List<HDAddress> getSigningAddressesForInputs(int hdAccountId, List<In> inList) {
        final List<HDAddress> hdAddressList = new ArrayList<HDAddress>();
        for (In in : inList) {
            String sql = "select a.address,a.path_type,a.address_index,a.is_synced,a.hd_account_id" +
                    " from hd_addresses a ,outs b" +
                    " where a.address=b.out_address" +
                    " and b.tx_hash=? and b.out_sn=? and a.hd_account_id=?";
            OutPoint outPoint = in.getOutpoint();
            this.execQueryOneRecord(sql, new String[]{Base58.encode(in.getPrevTxHash()), Integer.toString
                    (outPoint.getOutSn()), Integer.toString(hdAccountId)}, new Function<ICursor, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable ICursor c) {
                    hdAddressList.add(formatAddress(c));
                    return null;
                }
            });
        }
        return hdAddressList;
    }

    @Override
    public List<HDAddress> belongAccount(int hdAccountId, List<String> addresses) {
        final List<HDAddress> hdAddressList = new ArrayList<HDAddress>();
        List<String> temp = new ArrayList<String>();
        for (String str : addresses) {
            temp.add(Utils.format("'%s'", str));
        }
        String sql = "select address,redeem,path_type,address_index,is_issued,is_synced,hd_account_id " +
                " from hd_addresses" +
                " where hd_account_id=? and address in (" + Utils.joinString(temp, ",") + ")";
        this.execQueryLoop(sql, new String[]{Integer.toString(hdAccountId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor c) {
                hdAddressList.add(formatAddress(c));
                return null;
            }
        });
        return hdAddressList;
    }

    @Override
    public List<Out> getUnspendOutByHDAccount(int hdAccountId) {
        final List<Out> outItems = new ArrayList<Out>();
        String unspendOutSql = "select a.* from outs a,txs b where a.tx_hash=b.tx_hash " +
                " and a.out_status=? and a.hd_account_id=?";
        this.execQueryLoop(unspendOutSql, new String[]{Integer.toString(Out.OutStatus.unspent.getValue()), Integer.toString(hdAccountId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor c) {
                outItems.add(AbstractTxProvider.applyCursorOut(c));
                return null;
            }
        });
        return outItems;
    }

    @Override
    public int unSyncedAddressCount(int hdAccountId) {
        String sql = "select count(address) cnt from hd_addresses where is_synced=? and hd_account_id=? ";
        final int[] cnt = {0};
        this.execQueryOneRecord(sql, new String[]{"0", Integer.toString(hdAccountId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor c) {
                int idColumn = c.getColumnIndex("cnt");
                if (idColumn != -1) {
                    cnt[0] = c.getInt(idColumn);
                }
                return null;
            }
        });
        return cnt[0];
    }

    private HDAddress formatAddress(ICursor c) {
        HDAddress address = new HDAddress();
        int idColumn = c.getColumnIndex(AbstractDb.HDAddressesColumns.ADDRESS);
        if (idColumn != -1) {
            address.setAddress(c.getString(idColumn));
        }
        idColumn = c.getColumnIndex(AbstractDb.HDAddressesColumns.REDEEM);
        if (idColumn != -1) {
            try {
                address.setRedeem(Base58.decode(c.getString(idColumn)));
            } catch (AddressFormatException e) {
                e.printStackTrace();
            }
        }
        idColumn = c.getColumnIndex(AbstractDb.HDAddressesColumns.PATH_TYPE);
        if (idColumn != -1) {
            address.setPathType(AbstractHD.getTernalRootType(c.getInt(idColumn)));
        }
        idColumn = c.getColumnIndex(AbstractDb.HDAddressesColumns.ADDRESS_INDEX);
        if (idColumn != -1) {
            address.setAddressIndex(c.getInt(idColumn));
        }
        idColumn = c.getColumnIndex(AbstractDb.HDAddressesColumns.IS_ISSUED);
        if (idColumn != -1) {
            address.setIssued(c.getInt(idColumn) == 1);
        }
        idColumn = c.getColumnIndex(AbstractDb.HDAddressesColumns.IS_SYNCED);
        if (idColumn != -1) {
            address.setSyncComplete(c.getInt(idColumn) == 1);
        }
        idColumn = c.getColumnIndex(AbstractDb.HDAddressesColumns.HD_ACCOUNT_ID);
        if (idColumn != -1) {
            address.setHdAccountId(c.getInt(idColumn));
        }
        return address;
    }
}
