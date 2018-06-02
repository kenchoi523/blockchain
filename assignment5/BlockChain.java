// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;

    public class BlockWrapper {
        public Block block;
        public int height;
        public UTXOPool utxo_pool;

        public BlockWrapper(Block block, int height, UTXOPool pool) {
            this.block = block;
            this.height = height;
            this.utxo_pool = pool;
	}

        public UTXOPool getUTXOPoolCopy() {
            return new UTXOPool(utxo_pool);
        }
    }

    private HashMap<ByteArrayWrapper, BlockWrapper> hash_to_block_;
    private BlockWrapper max_height_block_wrapper_;
    private TransactionPool tx_pool_;

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        hash_to_block_ = new HashMap<ByteArrayWrapper, BlockWrapper>();
        tx_pool_ = new TransactionPool();

        Transaction genesis_coinbase_tx = genesisBlock.getCoinbase();
        UTXO u = new UTXO(genesis_coinbase_tx.getHash(), 0);
        UTXOPool pool = new UTXOPool();
        pool.addUTXO(u, genesis_coinbase_tx.getOutput(0));

        ByteArrayWrapper key = new ByteArrayWrapper(genesisBlock.getHash());
        BlockWrapper value = new BlockWrapper(genesisBlock, 1, pool);
        hash_to_block_.put(key, value);
        max_height_block_wrapper_ = value;

    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        return max_height_block_wrapper_.height;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        return max_height_block_wrapper_.getUTXOPoolCopy();
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        return tx_pool_;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        if (block.getPrevBlockHash() == null) 
	    return false;

        ByteArrayWrapper parent_hash = new ByteArrayWrapper(block.getPrevBlockHash());
        BlockWrapper parent_wrapper = hash_to_block_.get(parent_hash);
        if (parent_wrapper == null)
      	    return false;

    	int proposed_height = parent_wrapper.height + 1;
    	if (proposed_height < getMaxHeight() - CUT_OFF_AGE + 1)
      	    return false;

    	UTXOPool parent_pool = parent_wrapper.utxo_pool;
    	TxHandler tx_handler = new TxHandler(parent_pool);
    	Transaction[] block_txs = block.getTransactions().toArray(new Transaction[0]);
    	Transaction[] handled_txs = tx_handler.handleTxs(block_txs);
    	if (block_txs.length != handled_txs.length)
      	    return false;

    	UTXOPool block_pool = tx_handler.getUTXOPool();
    	Transaction block_coinbase_tx = block.getCoinbase();
    	block_pool.addUTXO(new UTXO(block_coinbase_tx.getHash(), 0),
            block_coinbase_tx.getOutput(0));
    	for (Transaction tx : block_txs) {
      	    tx_pool_.removeTransaction(tx.getHash());
    	}

    	BlockWrapper added_block_wrapper =
      	    new BlockWrapper(block, proposed_height, block_pool);
    	hash_to_block_.put(new ByteArrayWrapper(block.getHash()),
            added_block_wrapper);

    	if (proposed_height > getMaxHeight())
      	    max_height_block_wrapper_ = added_block_wrapper;

    	return true;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        tx_pool_.addTransaction(tx);
    }
}
