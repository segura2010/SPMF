/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.pfv.spmf.tools.calculate_measures_sequential_rules;
import ca.pfv.spmf.input.sequence_database_list_integers.Sequence;
import ca.pfv.spmf.input.sequence_database_list_integers.SequenceDatabase;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import ca.pfv.spmf.patterns.itemset_array_integers_with_tids.Itemset;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author alberto
 */
public class MeasureCalculator {
    
    public void calculate(String dbFile, String sequencialRulesFile, String outputFile) throws IOException{
        // 1. read sequence DB
        SequenceDatabase db = readDatabase(dbFile);
        
        // 2. read sequencial assocuation rules
        ArrayList<SequentialRule> rules = readSequencialRules(sequencialRulesFile);
        
        // 3. calculate itemsets support
        rules = calculateItemsetsSupport(db, rules);
        
        // 4. write to file
        writeSequencialRules(outputFile, rules, db.size());
    }
    
    private SequenceDatabase readDatabase(String dbFile) throws IOException{
        SequenceDatabase db = new SequenceDatabase();
        db.loadFile(dbFile);
        
        return db;
    }
    
    private ArrayList<SequentialRule> readSequencialRules(String sequencialRulesFile) throws IOException{
        FileInputStream rulesIS = new FileInputStream(new File(sequencialRulesFile));
        BufferedReader bfReaderRules = new BufferedReader(new InputStreamReader(rulesIS));
        
        ArrayList<SequentialRule> rules = new ArrayList<SequentialRule>();
        
        boolean firstLine = true;
        boolean isAntecedent = true;
        String line = null;
        while ((line = bfReaderRules.readLine()) != null) {
				
            boolean noItemsLeft = false;
            isAntecedent = true;
            SequentialRule actRule = new SequentialRule();
            
            if(line.isEmpty() == false){
                // Example of rule:
                // 1,2,3 ==> 4,5 #SUP: 5193 #CONF: 0.7291491154170177 #LIFT: 2.0805217539107037
                // split the line according to spaces into tokens
                String [] split = line.split(" ");
                // for each token
                for(int i=0; i<split.length; i++){
                    String token = split[i];
                    // if the character "#" is met, it means that the rest of the line
                    // does not contains items (it is a comment or they are the quality measures)
                    if(token.startsWith("#")){
                        break;
                    }else if(token.contains("=>")){
                        continue; // skip "==>"
                    }else{
                        // We split the parts by ","
                        String[] parts = token.split(",");
                        int[] items = new int[parts.length];
                        for(int m = 0; m < parts.length; m++){
                            Integer itemId = isInteger(parts[m]);
                            if(itemId == null){
                                continue; // it is not an integer...
                            }
                            
                            items[m] = itemId;
                        }
                        Itemset its = new Itemset(items);
                        if(isAntecedent){
                            actRule.setItemset1(its);
                            isAntecedent = false;
                        }else{
                            actRule.setItemset2(its);
                            // rule finished, save and continue with the next one
                            rules.add(actRule);
                            break;
                        }
                    }
                }
            }
        }
        rulesIS.close();
        
        return rules;
    }
    
    private ArrayList<SequentialRule> calculateItemsetsSupport(SequenceDatabase db, ArrayList<SequentialRule> rules){
        
        // It doesnt work...
        // the items from the antecedent or consequent can be on the same itemset in the sequence or in different itemsets..
        // if I have the rule: 1,2 -> 3
        // 1 and 2 must occur before 3, but they can occur in different sequences..
        // A better approach:
        // First check the consequent, from the last itemset to the first itemset
        // save the last checked itemset for the consequent
        // start looking for the antecedent from the saved itemset
        // With this approach you will be sure that the antecedent occurs before consequent
        // and the items can be in different itemsets
        
        for (Sequence seq : db.getSequences()) {
            // for each sequence, check each rule and update the support of their itemsets and the rule itself
            for(SequentialRule rule : rules){
                Boolean hasContainedItemset1 = false, hasContainedItemset2 = false;
                for(List<Integer> sequenceItemset : seq.getItemsets()){
                    // add transaction ID to itemset 1 and 2 if it contains all the items
                    Boolean containsItemset1 = true, containsItemset2 = true;
                    for(Integer item : rule.getItemset1().itemset){
                        Boolean containsItem = sequenceItemset.contains(item);
                        if(!containsItem){
                            containsItemset1 = false;
                            break;
                        }
                    }
                    if(containsItemset1){
                        hasContainedItemset1 = true;
                        System.out.println("Sequence "+seq.getId()+" contains antecedent");
                    }
                    for(Integer item : rule.getItemset2().itemset){
                        Boolean containsItem = sequenceItemset.contains(item);
                        if(!containsItem){
                            containsItemset2 = false;
                            break;
                        }
                    }
                    if(containsItemset2 && hasContainedItemset1){
                        // only set to true if we already saw the antecedent
                        // because antecedent MUST happen before consequent (in sequential rules)
                        hasContainedItemset2 = true;
                        System.out.println("Sequence "+seq.getId()+" contains consequent&antecedent");
                    }
                    // yes, if the sequence contains multiple itemsets that contains our itemset
                    // we will add the same transaction multiple times to the transactions list
                    // BUT the transaction list is a HashSet, so repeated elements will not be 
                    // inserted
                    if(containsItemset1){
                        rule.getItemset1().transactionsIds.add(seq.getId());
                    }
                    if(containsItemset2){
                        rule.getItemset2().transactionsIds.add(seq.getId());
                    }
                }
                // check if some sequence has matched the sequential rule
                if(hasContainedItemset1 && hasContainedItemset2){
                    rule.incrementTransactionCount();
                }
            }
        }
        
        return rules;
    }
    
    private void writeSequencialRules(String outputFile, ArrayList<SequentialRule> rules, int sequenceCount) throws IOException{
        PrintWriter writer = new PrintWriter(new File(outputFile));
        
        for(SequentialRule rule : rules){
            writer.println(rule.toString(sequenceCount));
        }
        writer.close();
    }
    
    /**
    * Get the integer representation of a string or null if the string is not an integer.
    * @param string a string
    * @return an integer or null if the string is not an integer.
    */
   private Integer isInteger(String string) {
           Integer result = null;
       try { 
           result = Integer.parseInt(string); 
       } catch(NumberFormatException e) { 
           return null; 
       }
       // only got here if we didn't return false
       return result;
   }
    
}
