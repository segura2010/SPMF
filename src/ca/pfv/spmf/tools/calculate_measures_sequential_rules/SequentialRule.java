/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.pfv.spmf.tools.calculate_measures_sequential_rules;

import ca.pfv.spmf.patterns.itemset_array_integers_with_tids.Itemset;

/**
 *
 * @author alberto
 */
public class SequentialRule {
    /** antecedent */
    private Itemset itemset1; 
    /** consequent */
    private Itemset itemset2;
    /** absolute support */
    private int transactioncount; 

    
    public SequentialRule(){
            this.itemset1 = null;
            this.itemset2 = null;
    }
    
    /**
     * Constructor
     * @param itemset1  the left itemset
     * @param itemset2  the right itemset
     */
    public SequentialRule(Itemset itemset1, Itemset itemset2){
            this.itemset1 = itemset1;
            this.itemset2 = itemset2;
    }

    /**
     * Get the antecedent of the rule (left itemset)
     * @return an Itemset
     */
    public Itemset getItemset1() {
            return itemset1;
    }

    /**
     * Get the consequent of the rule (right itemset)
     * @return an Itemset
     */
    public Itemset getItemset2() {
            return itemset2;
    }

    /**
     * Get the  support of this rule as a percentage.
     * @param sequencecount the number of sequence in the sequence database
     * @return the support as a double
     */
    public double getAbsoluteSupport(int sequencecount) {
            return ((double)transactioncount) / ((double) sequencecount);
    }

    // Could these terms possibly be reversed?  Is this the absolute and above the relative?
    public int getRelativeSupport(){
            return transactioncount;
    }

    /**
     * Get the confidence of this rule.
     * @return a double value.
     */
    public double getConfidence() {
            return ((double)transactioncount) / ((double) itemset1.getAbsoluteSupport());
    }

    /**
     * Get the lift of this rule.
     * @return a double value.
     */
    public double getLift(int sequencecount) {

            //Verbose for review purposes
            double firstTerm = ((double) transactioncount) / ((double) sequencecount);
            double secondTerm = ((double) itemset2.getAbsoluteSupport()) / ((double) sequencecount);
            double thirdTerm = ((double) itemset1.getAbsoluteSupport()) / ((double) sequencecount);
            double lift = firstTerm / (secondTerm * thirdTerm);
            return lift;
    }

    /**
     * Get the lift of this rule. - TEMP!!
     * @return a String value.
     */
    public String getLiftAsString(int sequencecount) {
//		if(itemset1.contains(2) && itemset2.contains(6)){
//			System.out.println();
//		}

            //Verbose for review purposes
//		double itemset2_rel_support = ((double) transactioncount) / ((double) sequencecount);
//		return (String)" itemset2  abs sup:" + itemset2.getAbsoluteSupport()  + "/  sequencecount:" + sequencecount +
//				"|itemset2_rel_support:" + itemset2_rel_support + "| lift:" + (getConfidence()) / (itemset2_rel_support);
            double firstTerm = ((double) transactioncount) / ((double) sequencecount);
            double secondTerm = ((double) itemset2.getAbsoluteSupport()) / ((double) sequencecount);
            double thirdTerm = ((double) itemset1.getAbsoluteSupport()) / ((double) sequencecount);
            double lift = firstTerm / (secondTerm * thirdTerm);
            return Double.toString(lift);
    }

    /**
     * Print this rule to System.out
     */
    public void print(){
            System.out.println(toString());
    }

    /**
     * Get a string representation of this rule.
     */
    public String toString(int sequencecount){
        String antecedent = itemset1.toString().replace(" ", ",");
        antecedent = antecedent.substring(0, antecedent.length()-1);
        String consequent = itemset2.toString().replace(" ", ",");
        consequent = consequent.substring(0, consequent.length()-1);
        
        String out = antecedent +  " ==> " + consequent;
        // add measures
        out += " #SUP: " + transactioncount;
        out += " #CONF: " + getConfidence();
        out += " #LIFT: " + getLift(sequencecount);
        
        return out;
    }

    /**
     * Increase the support of this rule.
     */
    void incrementTransactionCount() {
            this.transactioncount++;
    }

    /**
     * Set the relative support of this rule.
     * @param transactioncount the support as an integer.
     */
    void setTransactioncount(int transactioncount) {
            this.transactioncount = transactioncount;
    }

    public void setItemset1(Itemset itemset1) {
        this.itemset1 = itemset1;
    }

    public void setItemset2(Itemset itemset2) {
        this.itemset2 = itemset2;
    }
    
    
}
