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
    
    /**
    * It reads a sequence DB and a sequencial rules file to calculate more quality measures and write them in an output file
    * @param dbFile path to the sequential database file
    * @param sequencialRulesFile path to the sequential rules file
    * @param outputFile path and name of the output file
    */
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
    
    /**
    * It reads a file (DB) of sequences
    * @param dbFile path to the sequential database file
    * @return SequenceDatabase
    */
    private SequenceDatabase readDatabase(String dbFile) throws IOException{
        SequenceDatabase db = new SequenceDatabase();
        db.loadFile(dbFile);
        
        return db;
    }
    
    /**
    * It reads a file of sequential rules
    * @param sequencialRulesFile path to the sequential rules file
    * @return ArrayList of the readed rules
    */
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
    
    /**
    * It finds the range of itemsets in which an itemset is contained
    * @param it itemset to find
    * @param seq sequence in which find it
    * @param fromItemset start looking for it from an specific itemset index in the sequence
    * @return the range of itemsets in which the itemset have been found in the sequence
    */
    private int[] findItemsetInSequence(Itemset it, Sequence seq, int fromItemset){
        int[] range = new int[2];
        range[0] = -1; range[1] = -1;
        
        List<List<Integer>> sequenceItemsets = seq.getItemsets();
        
        for(int item : it.getItems()){
            Boolean itemFound = false;
            for(int i=fromItemset;i<sequenceItemsets.size();i++){
                List<Integer> actSequence = sequenceItemsets.get(i);
                Boolean containsItem = actSequence.contains(item);
                if(containsItem){
                    itemFound = true;
                    if(range[0] == -1){
                        // if range[0] not updated, update both
                        range[0] = i; range[1] = i;
                    }else if(range[0] > i){
                        range[0] = i;
                    }
                    if(range[1] < i){
                        range[1] = i;
                    }
                    break;
                }
            }
            if(!itemFound){
                // item not found in any itemset of this sequence
                // so this itemset is not contained in this sequence
                range[0] = -1; range[1] = -1;
                return range;
            }
        }
        
        return range;
    }
    
    /**
    * It updates itemsets and rules information in order to calculate the support
    * @param db Sequence database
    * @param rules ArrayList of SequentialRule rules
    * @return updated set of rules
    */
    private ArrayList<SequentialRule> calculateItemsetsSupport(SequenceDatabase db, ArrayList<SequentialRule> rules){
        
        for (Sequence seq : db.getSequences()) {
            // for each sequence, check each rule and update the support of their itemsets and the rule itself
            for(SequentialRule rule : rules){
                int[] antecedentRange = findItemsetInSequence(rule.getItemset1(), seq, 0);
                Boolean containsAntecedent = (antecedentRange[0] != -1) && (antecedentRange[1] != -1);
                Boolean containsConsequent = false, containsConsequentAfterAntecedent = false;
                if(containsAntecedent){
                    // check if contains consequent after antecedent
                    int[] consequentRangeAfterAntecedent = findItemsetInSequence(rule.getItemset2(), seq, antecedentRange[1]);
                    containsConsequentAfterAntecedent = (consequentRangeAfterAntecedent[0] != -1) && (consequentRangeAfterAntecedent[1] != -1);
                    containsConsequent = containsConsequentAfterAntecedent;
                    
                    // update antecedent info
                    rule.getItemset1().transactionsIds.add(seq.getId());
                }
                if(!containsConsequentAfterAntecedent){
                    // if it does not contain the consequent after antecedet, the rule is not matched,
                    // but I still have to check if the full sequence contains the consequent in order to update 
                    // the consequent info (for later support calculation)
                    int[] consequentRange = findItemsetInSequence(rule.getItemset2(), seq, 0);
                    containsConsequent = (consequentRange[0] != -1) && (consequentRange[1] != -1);
                }
                if(containsConsequentAfterAntecedent || containsConsequent){
                    // update consequent info
                    rule.getItemset2().transactionsIds.add(seq.getId());
                }
                
                if(containsAntecedent && containsConsequentAfterAntecedent){
                    // finally, update rule info if the sequence matches the rule
                    rule.incrementTransactionCount();
                }
            }
        }
        
        return rules;
    }
    
    /**
    * It writes the set of rules with their quality measures in a file
    * @param outputFile Output file path and name
    * @param rules ArrayList of SequentialRule rules
    * @param sequenceCount number of sequences in the database (needed to finally calculate and write the quality measures)
    */
    private void writeSequencialRules(String outputFile, ArrayList<SequentialRule> rules, int sequenceCount) throws IOException{
        PrintWriter writer = new PrintWriter(new File(outputFile));
        
        for(SequentialRule rule : rules){
            writer.println(rule.toString(sequenceCount));
        }
        writer.close();
    }
    
    /**
    * It gets the integer representation of a string or null if the string is not an integer.
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
   
   /**
    * It reads a sequence DB and calculate the quality measures for a rule
    * @param dbFile path to the sequential database file
    * @param antecedent antecedent itemset of the rule
    * @param consequent consequent itemset of the rule
    * @return SequentialRule
    */
    public SequentialRule calculateForRule(String dbFile, Itemset antecedent, Itemset consequent) throws IOException{
        // 1. read sequence DB
        SequenceDatabase db = readDatabase(dbFile);
        
        // 2. add the sequencial assocuation rule
        ArrayList<SequentialRule> rules = new ArrayList<SequentialRule>();
        rules.add(new SequentialRule(antecedent, consequent));
        
        // 3. calculate itemsets support
        rules = calculateItemsetsSupport(db, rules);
        
        return rules.get(0);
    }
    
    /**
    * It process a sequence DB and calculate the quality measures for a rule
    * @param db a SequenceDatabase
    * @param antecedent antecedent itemset of the rule
    * @param consequent consequent itemset of the rule
    * @return SequentialRule
    */
    public SequentialRule calculateForRule(SequenceDatabase db, Itemset antecedent, Itemset consequent) throws IOException{
        
        // 1. add the sequencial assocuation rule
        ArrayList<SequentialRule> rules = new ArrayList<SequentialRule>();
        rules.add(new SequentialRule(antecedent, consequent));
        
        // 2. calculate itemsets support
        rules = calculateItemsetsSupport(db, rules);
        
        return rules.get(0);
    }
    
    /**
    * It process a sequence DB and calculate the quality measures for a rule
    * @param db a SequenceDatabase
    * @param antecedent antecedent itemset of the rule
    * @param consequent consequent itemset of the rule
    * @return SequentialRule
    */
    public SequentialRule calculateForRule(SequenceDatabase db, int[] antecedent, int[] consequent) throws IOException{
        
        // 1. add the sequencial assocuation rule
        ArrayList<SequentialRule> rules = new ArrayList<SequentialRule>();
        rules.add(new SequentialRule(new Itemset(antecedent), new Itemset(consequent)));
        
        // 2. calculate itemsets support
        rules = calculateItemsetsSupport(db, rules);
        
        return rules.get(0);
    }
    
}
