/*
 * Copyright 2019 The caver-java Authors
 *
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.klaytn.caver.tx.type;

import com.klaytn.caver.crypto.KlaySignatureData;
import com.klaytn.caver.utils.KlayTransactionUtils;
import org.web3j.rlp.RlpDecoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.List;

/**
 * TxTypeFeeDelegatedValueTransferMemoWithRatio transfers KLAY with a data.
 * The given ratio of the transaction fee is paid by the fee payer.
 */
public class TxTypeFeeDelegatedValueTransferMemoWithRatio extends AbstractTxType implements TxTypeFeeDelegate {

    /**
     * memo
     */
    private final byte[] payload;

    /**
     * Fee ratio of the fee payer. If it is 30, 30% of the fee will be paid by the fee payer.
     * 70% will be paid by the sender.
     */
    private final BigInteger feeRatio;

    protected TxTypeFeeDelegatedValueTransferMemoWithRatio(BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit,
                                                           String to, BigInteger value, String from, byte[] payload, BigInteger feeRatio) {
        super(nonce, gasPrice, gasLimit, from, to, value);
        this.payload = payload;
        this.feeRatio = feeRatio;
    }

    public static TxTypeFeeDelegatedValueTransferMemoWithRatio createTransaction(BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String to, BigInteger value, String from, byte[] payload, BigInteger feeRatio) {
        return new TxTypeFeeDelegatedValueTransferMemoWithRatio(nonce, gasPrice, gasLimit, to, value, from, payload, feeRatio);
    }

    public byte[] getPayload() {
        return payload;
    }

    public BigInteger getFeeRatio() {
        return feeRatio;
    }

    /**
     * This method is overridden as FEE_DELEGATED_VALUE_TRANSFER_MEMO_WITH_RATIO type.
     * The return value is used for rlp encoding.
     *
     * @return Type transaction type
     */
    @Override
    public Type getType() {
        return Type.FEE_DELEGATED_VALUE_TRANSFER_MEMO_WITH_RATIO;
    }

    /**
     * create RlpType List which contains nonce, gas price, gas limit, to, value, from, payload and feeRatio.
     * List elements can be different depending on transaction type.
     *
     * @return List RlpType List
     */
    @Override
    public List<RlpType> rlpValues() {
        List<RlpType> result = super.rlpValues();
        result.add(RlpString.create(Numeric.hexStringToByteArray(getTo())));
        result.add(RlpString.create(getValue()));
        result.add(RlpString.create(Numeric.hexStringToByteArray(getFrom())));
        result.add(RlpString.create(getPayload()));
        result.add(RlpString.create(getFeeRatio()));
        return result;
    }

    /**
     * decode transaction hash from sender to reconstruct transaction with fee payer signature.
     *
     * @param rawTransaction signed transaction hash from sender
     * @return TxTypeFeeDelegatedValueTransferMemoWithRatio decoded transaction
     */
    public static TxTypeFeeDelegatedValueTransferMemoWithRatio decodeFromRawTransaction(byte[] rawTransaction) {
        byte[] rawTransactionExceptType = KlayTransactionUtils.getRawTransactionNoType(rawTransaction);

        RlpList rlpList = RlpDecoder.decode(rawTransactionExceptType);
        RlpList values = (RlpList) rlpList.getValues().get(0);
        BigInteger nonce = ((RlpString) values.getValues().get(0)).asPositiveBigInteger();
        BigInteger gasPrice = ((RlpString) values.getValues().get(1)).asPositiveBigInteger();
        BigInteger gasLimit = ((RlpString) values.getValues().get(2)).asPositiveBigInteger();
        String to = ((RlpString) values.getValues().get(3)).asString();
        BigInteger value = ((RlpString) values.getValues().get(4)).asPositiveBigInteger();
        String from = ((RlpString) values.getValues().get(5)).asString();
        byte[] payload = ((RlpString) values.getValues().get(6)).getBytes();
        BigInteger feeRatio = ((RlpString) values.getValues().get(7)).asPositiveBigInteger();
        TxTypeFeeDelegatedValueTransferMemoWithRatio tx
                = TxTypeFeeDelegatedValueTransferMemoWithRatio.createTransaction(nonce, gasPrice, gasLimit, to, value, from, payload, feeRatio);
        if (values.getValues().size() > 7) {
            RlpList vrs = (RlpList) ((RlpList) (values.getValues().get(8))).getValues().get(0);
            byte[] v = ((RlpString) vrs.getValues().get(0)).getBytes();
            byte[] r = ((RlpString) vrs.getValues().get(1)).getBytes();
            byte[] s = ((RlpString) vrs.getValues().get(2)).getBytes();
            tx.setSenderSignatureData(new KlaySignatureData(v, r, s));
        }
        return tx;
    }

    /**
     * @param rawTransaction signed transaction hash from sender
     * @return TxTypeFeeDelegatedValueTransferMemoWithRatio decoded transaction
     */
    public static TxTypeFeeDelegatedValueTransferMemoWithRatio decodeFromRawTransaction(String rawTransaction) {
        return decodeFromRawTransaction(Numeric.hexStringToByteArray(Numeric.cleanHexPrefix(rawTransaction)));
    }
}
