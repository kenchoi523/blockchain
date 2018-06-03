import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import static java.util.stream.Collectors.toSet;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {

    private double p_graph;
    private double p_malicious;
    private double p_txDistribution;
    private int numRounds;

    private boolean[] followees;

    private Set<Transaction> pendingTransactions;

    private boolean[] blacklisted;


    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        // IMPLEMENT THIS
	this.p_graph = p_graph;
	this.p_malicious = p_malicious;
	this.p_txDistribution = p_txDistribution;
	this.numRounds = numRounds;
    }

    public void setFollowees(boolean[] followees) {
        // IMPLEMENT THIS
	this.followees = followees;
	this.blacklisted = new boolean[followees.length];
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        // IMPLEMENT THIS
	this.pendingTransactions = pendingTransactions;
    }

    public Set<Transaction> sendToFollowers() {
        // IMPLEMENT THIS
	Set<Transaction> toSend = new HashSet<>(pendingTransactions);
	pendingTransactions.clear();
	return toSend;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        // IMPLEMENT THIS
	Set<Integer> senders = candidates.stream().map(c -> c.sender).collect(toSet());
	for (int i = 0; i < followees.length; i++){
		if (followees[i] && !senders.contains(i))
			blacklisted[i] = true;
	}
	for (Candidate c: candidates){
		if(!blacklisted[c.sender]) {
			pendingTransactions.add(c.tx);
		}

	}
    }
}
