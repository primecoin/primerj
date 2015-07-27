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

package net.bither.bitherj.db;

import net.bither.bitherj.core.*;
import net.bither.bitherj.core.base.IHDAddress;

import java.util.HashSet;
import java.util.List;

public interface IHDAddressProvider {

    void addAddress(List<IHDAddress> addresses);

    int maxHDMAddressPubIndex(int hdAccountId);

    String externalAddress(int hdAccountId);

    boolean hasAddress(int hdAccountId);

    long getHDAccountConfirmedBalance(int hdAccountId);

    HashSet<String> getBelongAccountAddresses(int hdAccountId, List<String> addressList);
    HashSet<String> getBelongAccountAddresses(List<String> addressList);

    void updateIssuedIndex(int hdAccountId, AbstractHD.PathType pathType, int index);

    int issuedIndex(int hdAccountId, AbstractHD.PathType pathType);

    int allGeneratedAddressCount(int hdAccountId, AbstractHD.PathType pathType);

    void updateSyncedForIndex(int hdAccountId, AbstractHD.PathType pathType, int index);

    void updateSyncedComplete(HDAddress address);

    List<Tx> getHDAccountUnconfirmedTx(int hdAccountId);

    List<byte[]> getPubs(int hdAccountId, AbstractHD.PathType pathType);

    int getUnspendOutCountByHDAccountWithPath(int hdAccountId, AbstractHD.PathType pathType);

    List<Out> getUnspendOutByHDAccountWithPath(int hdAccountId, AbstractHD.PathType pathType);

    HDAddress addressForPath(int hdAccountId, AbstractHD.PathType type, int index);

    List<HDAddress> getSigningAddressesForInputs(int hdAccountId, List<In> inList);

    List<HDAddress> belongAccount(int hdAccountId, List<String> addresses);

    List<Out> getUnspendOutByHDAccount(int hdAccountId);

    int unSyncedAddressCount(int hdAccountId);
    void setSyncedNotComplete();

    Tx updateOutHDAccountId(Tx tx);
    List<Integer> getRelatedHDAccountIdList(List<String> addresses);
}
