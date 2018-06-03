import java.util.*;


public class TxHandler {
	private UTXOPool utxoPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        this.utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        UTXOPool pool = new UTXOPool();
	
	double prevTxOutput = 0;
	double curTxOutput = 0;

	for (int i = 0; i < tx.numInputs(); i++) {
		Transaction.Input transIn = tx.getInput(i);
		UTXO newutxo = new UTXO(transIn.prevTxHash, transIn.outputIndex);
		if (!utxoPool.contains(newutxo)) //condition 1
			return false;
		Transaction.Output transOut = utxoPool.getTxOutput(newutxo);
		if (!Crypto.verifySignature(transOut.address, tx.getRawDataToSign(i), transIn.signature)) //condition 2
			return false;
		if (pool.contains(newutxo)) //condition 3
			return false;
		pool.addUTXO(newutxo, transOut);
		prevTxOutput += transOut.value;
	}
	
	for (int i = 0; i < tx.numOutputs(); i++){
		Transaction.Output transOut2 = tx.getOutput(i);
		if (transOut2.value < 0)
			return false; //condition 4
		curTxOutput += transOut2.value;	
	}
	boolean condition5 = (prevTxOutput >= curTxOutput);
	return condition5;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
    	if (possibleTxs == null){
		return new Transaction[0];
	}
	ArrayList<Transaction> validTxs = new ArrayList<>();

	for (int i = 0; i < possibleTxs.length; i++){
		Transaction tx = possibleTxs[i];
		if (isValidTx(tx) == false)
			continue;
		
		validTxs.add(tx);
		for (int j = 0; j < tx.numInputs(); j++){
			Transaction.Input transIn = tx.getInput(j);
			UTXO newutxo = new UTXO(transIn.prevTxHash, transIn.outputIndex);
			this.utxoPool.removeUTXO(newutxo);
		}

		byte[] txHash = tx.getHash();
		int index = 0;
		for (int k = 0; k < tx.numOutputs(); k++){
			Transaction.Output transOut = tx.getOutput(k);
			UTXO newutxo = new UTXO(txHash, index);
			index += 1;
			this.utxoPool.addUTXO(newutxo, transOut);
		}


	}
	return validTxs.toArray(new Transaction[validTxs.size()]);

    }

}
