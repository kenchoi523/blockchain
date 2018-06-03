
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class BlockChain {
    private HashMap<ByteArrayWrapper, BlockNode> blockChain;
    // save the current BlockNode with the max height
    private BlockNode maxHeightBlockNode;
    // keep a global TransactionPool
    private TransactionPool txPool;
    // the height of the oldestBlock
    private int oldestBlockHeight;
    
    public static final int CUT_OFF_AGE = 10;
    
    private class BlockNode {
        public Block block;
        // the height of the block
        public int height;
        // maintain a UTXOPool corresponding to every block on top of which a new block might create
        public UTXOPool utxoPool;
        
        // since there can be multiple forks, blocks form a tree rather than a list
        // a tree should contain multiple tree nodes
        // each node has a record of its parentNode (only one) and its childrenNodes (multiple)
        public BlockNode parentNode;
        public ArrayList<BlockNode> childrenNodes;
        
        public BlockNode(Block block, BlockNode parentNode, UTXOPool utxoPool) {
            this.block = block;
            this.parentNode = parentNode;
            this.utxoPool = utxoPool;
            this.childrenNodes = new ArrayList<>();
            if (parentNode == null) {
                this.height = 1;
            } else {
                this.height = parentNode.height + 1;
                this.parentNode.childrenNodes.add(this);
            }
        }
    }

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
<<<<<<< HEAD
        this.blockChain = new HashMap<>();
        UTXOPool utxoPool = new UTXOPool();
        // add outputs of genesisBlock into the global UTXOPool
        for (int i = 0; i < genesisBlock.getCoinbase().getOutputs().size(); i++) {
            UTXO utxo = new UTXO(genesisBlock.getCoinbase().getHash(), i);
            utxoPool.addUTXO(utxo, genesisBlock.getCoinbase().getOutput(i));
        }
        
        BlockNode genesisBlockNode = new BlockNode(genesisBlock, null, utxoPool);

        blockChain.put(new ByteArrayWrapper(genesisBlock.getHash()), genesisBlockNode);
        this.txPool = new TransactionPool();
        
        this.maxHeightBlockNode = genesisBlockNode;
        this.oldestBlockHeight = 1;
=======
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

>>>>>>> b53ebae13ef0bfbd6dd0db4d08b2627b5a4df65f
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
<<<<<<< HEAD
        return maxHeightBlockNode.block;
=======
        return max_height_block_wrapper_.height;
>>>>>>> b53ebae13ef0bfbd6dd0db4d08b2627b5a4df65f
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
<<<<<<< HEAD
        return maxHeightBlockNode.utxoPool;
=======
        return max_height_block_wrapper_.getUTXOPoolCopy();
>>>>>>> b53ebae13ef0bfbd6dd0db4d08b2627b5a4df65f
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
<<<<<<< HEAD
        return txPool;
=======
        return tx_pool_;
>>>>>>> b53ebae13ef0bfbd6dd0db4d08b2627b5a4df65f
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
<<<<<<< HEAD
        // a null block should not be added
        if (block == null) {
            return false;
        }
            
        // a new genesis block wouldn't be mined
        if (block.getPrevBlockHash() == null) {
            return false;
        }
        
        // the block should be valid
        // all the transactions in the block should be valid
        ByteArrayWrapper tempblock = new ByteArrayWrapper(block.getPrevBlockHash());
        BlockNode blockParentNode = blockChain.get(tempblock);

        // can not find any parentNode in the blockChain
        // the block should be rejected
        if (blockParentNode == null) {
            return false;
        }

        TxHandler txHandler = new TxHandler(blockParentNode.utxoPool);
        Transaction[] blockTxs = new Transaction[block.getTransactions().size()];
        for (int i = 0; i < block.getTransactions().size(); i++) {
            blockTxs[i] = block.getTransaction(i);
        }
        Transaction[] validTxs = txHandler.handleTxs(blockTxs);
        // if the return transactions list has length smaller than the input list
        // there are some invalid transaction in the block
        // the block should be rejected
        if (validTxs.length != blockTxs.length) {
            return false;
        }
        
        // the issue about the height of the blockNode
        if (blockParentNode.height + 1 <= maxHeightBlockNode.height - CUT_OFF_AGE) {
            return false;
        }
        
        // add the block into blockChain
        // first add the coin base outputs into the UTXOPool
        for (int i = 0; i < block.getCoinbase().getOutputs().size(); i++) {
            UTXO utxo = new UTXO(block.getCoinbase().getHash(), i);
            txHandler.getUTXOPool().addUTXO(utxo, block.getCoinbase().getOutput(i));
        }
        BlockNode blockNode = new BlockNode(block, blockParentNode, txHandler.getUTXOPool());
        blockChain.put(new ByteArrayWrapper(block.getHash()), blockNode);
        
        // update the maxHeightBlockNode
        if (blockParentNode.height + 1 > maxHeightBlockNode.height) {
            maxHeightBlockNode = blockNode;
        }
        
        // only keep the recent blocks
        // since block with proposed height less than maxHeightBlockNode.height - CUT_OFF_AGE can not be added
        // keeping 15 height of blocks should be safe
        if (maxHeightBlockNode.height - oldestBlockHeight >= 15) {
            Iterator<ByteArrayWrapper> it = blockChain.keySet().iterator();
            while (it.hasNext()) {
                ByteArrayWrapper key = it.next();
                BlockNode node = blockChain.get(key);
                if (node.height <= maxHeightBlockNode.height - 15) {
                    it.remove();
                }
            }
            oldestBlockHeight = maxHeightBlockNode.height - 14;
        }
        
        return true;
=======
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
>>>>>>> b53ebae13ef0bfbd6dd0db4d08b2627b5a4df65f
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
<<<<<<< HEAD
        this.txPool.addTransaction(tx);
=======
        tx_pool_.addTransaction(tx);
>>>>>>> b53ebae13ef0bfbd6dd0db4d08b2627b5a4df65f
    }
}
