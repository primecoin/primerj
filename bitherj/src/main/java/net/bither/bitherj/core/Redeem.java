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

package net.bither.bitherj.core;

import net.bither.bitherj.exception.ScriptException;
import net.bither.bitherj.script.Script;
import net.bither.bitherj.script.ScriptBuilder;
import net.bither.bitherj.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static net.bither.bitherj.script.ScriptOpCodes.OP_1;
import static net.bither.bitherj.script.ScriptOpCodes.OP_16;
import static net.bither.bitherj.script.ScriptOpCodes.OP_CHECKMULTISIG;

public class Redeem {
    public static final byte[] EmptyBytes = new byte[]{0};

    protected List<byte[]> pubs;
    protected int n;
    protected int m;

    public Redeem() {
    }

    public Redeem(byte[] redeem) {
        Script script = new Script(redeem);

        boolean result = OP_1 <= script.getChunks().get(0).opcode && script.getChunks().get(0).opcode <= OP_16;
        this.m = script.getChunks().get(0).opcode - 80;
        this.pubs = new ArrayList<byte[]>();
        for (int i = 1; i < script.getChunks().size() - 2; i++) {
            result &= script.getChunks().get(i).data != null;
            if (script.getChunks().get(i).data != null) {
                if (script.getChunks().get(i).data.length == 0) {
                    this.pubs.add(EmptyBytes);
                } else {
                    this.pubs.add(script.getChunks().get(i).data);
                }
            }
        }
        result &= OP_1 <= script.getChunks().get(script.getChunks().size() - 2).opcode && script.getChunks().get(script.getChunks().size() - 2).opcode <= OP_16;
        this.n = script.getChunks().get(script.getChunks().size() - 2).opcode - 80;
        result &= script.getChunks().get(script.getChunks().size() - 1).opcode == OP_CHECKMULTISIG;
        result &= this.m <= this.n;
        result &= this.pubs.size() == n;

        if (!result) {
            throw new ScriptException("can no parse redeem script!");
        }
    }

    public Redeem(int m, int n, List<byte[]> pubs) {
        this.m = m;
        this.n = n;
        this.pubs = pubs;
    }

    public Script getMultiSigScript() {
        assert isCompleted();
        return ScriptBuilder.createMultiSigOutputScript(this.m, this.pubs);
    }

    public Script getScript() {
        return ScriptBuilder.createMultiSigOutputScript(this.m, this.pubs);
    }

    public boolean isCompleted() {
        for (int i = 0; i < this.m; i++) {
            if (!this.isCompleted(i)) {
                return false;
            }
        }
        return true;
    }

    public boolean isCompleted(int index) {
        return this.pubs.get(index).length != 0;
    }

    public void completePub(int index, byte[] pub) {
        this.pubs.set(index, pub);
    }

    public String getAddress() {
        return Utils.toP2SHAddress(Utils.sha256hash160(getMultiSigScript().getProgram()));
    }
}
