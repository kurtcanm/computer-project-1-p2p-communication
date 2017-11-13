import java.util.*;
import java.util.Map;
import java.lang.String;


final class MyEntry<K, V> implements Map.Entry<K, V> {
    private final K key;
    private V value;

    public MyEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        V old = this.value;
        this.value = value;
        return old;
    }
}


abstract class HuffmanTree implements Comparable<HuffmanTree> {
    public final int frequency; // the frequency of the tree
    public HuffmanTree(int freq) { frequency = freq; }
 
    // compares the frequency
    public int compareTo(HuffmanTree tree) {
        return frequency - tree.frequency;
    }
}
 
class HuffmanLeaf extends HuffmanTree {
    public final char value; 
 
    public HuffmanLeaf(int freq, char val) {
        super(freq);
        value = val;
    }
}
 
class HuffmanNode extends HuffmanTree {
    public final HuffmanTree left, right; // subtrees
 
    public HuffmanNode(HuffmanTree l, HuffmanTree r) {
        super(l.frequency + r.frequency);
        left = l;
        right = r;
    }
}
 
public class HuffmanCode {
    // input is an array of frequencies, indexed by character code
    public static HuffmanTree buildTree(int[] charFreqs) {
        PriorityQueue<HuffmanTree> trees = new PriorityQueue<HuffmanTree>();
        // initially, we have a forest of leaves
        for (int i = 0; i < charFreqs.length; i++)
            if (charFreqs[i] > 0)
                trees.offer(new HuffmanLeaf(charFreqs[i], (char)i));
 
        assert trees.size() > 0;
        // loop until there is only one tree left
        while (trees.size() > 1) {
            // two trees with least frequency
            HuffmanTree a = trees.poll();
            HuffmanTree b = trees.poll();
 
            // put into new node and re-insert into queue
            trees.offer(new HuffmanNode(a, b));
        }
        return trees.poll();
    }
 
    public static void printCodes(HuffmanTree tree, StringBuffer prefix, StringBuffer code, 
    Map<String, String> codeBook ) {
        String str = "this is an example for huffman encoding";
        
        assert tree != null;
        if (tree instanceof HuffmanLeaf) {
            HuffmanLeaf leaf = (HuffmanLeaf)tree;
            // System.out.println(leaf.value + "\t" + leaf.frequency + "\t" + prefix);
            String symbol = Character.toString(leaf.value);
            String coding = prefix.toString();
            codeBook.put(symbol, coding);
            
            for (int i = 0; i < str.length(); i++){
                char c = str.charAt(i);
                
                if ( c == leaf.value){
                    code.append(prefix);
                    code.append(" ");
                }
            }
 
        } else if (tree instanceof HuffmanNode) {
            HuffmanNode node = (HuffmanNode)tree;
 
            // traverse left
            prefix.append('0');
            printCodes(node.left, prefix, code, codeBook);
            prefix.deleteCharAt(prefix.length()-1);
 
            // traverse right
            prefix.append('1');
            printCodes(node.right, prefix, code, codeBook);
            prefix.deleteCharAt(prefix.length()-1);
        }
        
    }
    public static void HuffmanCompress(String test){
        
        StringBuffer compressed_text = new StringBuffer("");
        Map<String, String> codeBook = new HashMap<String, String>();
    
        int[] charFreqs = new int[256];
		
        for (char c : test.toCharArray())
            charFreqs[c]++;
 
        HuffmanTree tree = buildTree(charFreqs);
 
        printCodes(tree, new StringBuffer(),compressed_text, codeBook);
        
        System.out.println(compressed_text);
        System.out.println(codeBook);
        
        for (String s : codeBook.keySet()) {
            System.out.println(s + " is " + codeBook.get(s));
        }
        
    }
    
    public static void HuffmanDecompress(String encodedString, HuffmanNode node) {
        
        StringBuilder decodedString = new StringBuilder();
        
        HuffmanNode base = node;
        
        while (!encodedString.isEmpty()){
            if (encodedString.charAt(0) == '0'){
                base = base.left;
                encodedString = encodedString.removeFirstChar;
            } else {
                base = base.right;
                encodedString = encodedString.removeFirstChar;
            }
            
            if (base.left == null && base.right == null){
                decodedString.append(base.data);
                base = node;
            }
            
        }
        
        System.out.println(decodedString.toString());
    }
    
    public String removeFirstChar(String s){
        return s.substring(1);
    }
    
    
    public static void main(String[] args) {
        String test = "this is an example for huffman encoding";
        HuffmanCompress(test);
        
       
    
    }
    
}
