import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;

/**
 * 
 * @author Nate Tomlin
 * @date 2/22/2019
 * @note Adapted From: https://www.youtube.com/watch?v=zSsTG3Flo-I
 * 					   https://www.youtube.com/watch?v=oNPyYF_Cz5I
 * @Summary This program takes a users text file and encodes it using Huffman encoding and then outputs that into a file called encoded.huff.
 * 			It then takes that encoded message of 1s and 0s and decodes it back to the orginal text and outputs the new text into a file called result.txt.
 * 
 *
 */


public class HuffmanEncoder {
	
	//Sets ASCII alphabet size
	private static final int ALPHABET_SIZE = 256;
	
	//Puts all the steps together to create a binary data for the characters in the text
	public HuffmanEncodedResult compress(final String data) {
		final int[] freq = buildFrequencyTable(data);
		final Node root = buildHuffmanTree(freq);
		final Map<Character, String> lookupTable = buildLookupTable(root);
		
		return new HuffmanEncodedResult(generateEncodedData(data, lookupTable), root);
	}
	
	//Creates String of binary encoded text
	private static String generateEncodedData(String data, Map<Character, String> lookupTable) {
		final StringBuilder builder = new StringBuilder();
		for(final char character : data.toCharArray()){
			builder.append(lookupTable.get(character));
		}
		return builder.toString();	//Returns string of binary encoded text
	}

	//Maps character to encoding based on frequency
	private static Map<Character, String> buildLookupTable(final Node root){
		final Map<Character, String> lookupTable = new HashMap<>();
		buildLookupTableImpl(root,"", lookupTable);
		
		return lookupTable;
	}
	
	//Assigns a 0 or 1 to the Children
	//Putting together the binary code for each alphabet element
	private static void buildLookupTableImpl(final Node node, final String s, final Map<Character, String> lookupTable) {
		if(!node.isLeaf()){
			buildLookupTableImpl(node.leftChild, s + '0', lookupTable);
			buildLookupTableImpl(node.rightChild, s + '1', lookupTable);
		} else {
			lookupTable.put(node.character, s);
		}
	}

	//Builds Huffman Tree
	private static Node buildHuffmanTree(int[] freq){
		final PriorityQueue<Node> priorityQueue = new PriorityQueue<>();
		
		for(char i = 0; i <ALPHABET_SIZE; i++){
			//Creates new node (leaf) if the frequency of alphabet variable occours in text
			if(freq[i] > 0){
				priorityQueue.add(new Node(i, freq[i], null, null));
			}
		}
		
		//converts to none leaf node
		if(priorityQueue.size() == 1){
			priorityQueue.add(new Node('\0', 1, null, null));
		}
		
		//When priority queue is greater than 1 mergers left and right node together to create new node
		while(priorityQueue.size() > 1){
			final Node left = priorityQueue.poll();
			final Node right = priorityQueue.poll();
			final Node parent = new Node('\0', left.frequency + right.frequency, left, right);
			priorityQueue.add(parent);	//adds new node to priority queue
		}
		return priorityQueue.poll();	//returns nodes
	}
	
	//Goes through data and alphabet to create frequency table
	private static int[] buildFrequencyTable(final String data){
		final int[] freq = new int[ALPHABET_SIZE];
		for(final char character : data.toCharArray()){
			freq[character]++;
		}
		return freq;
	}
	
	//Converts the binary encoding to a string
	public String decompress(final  HuffmanEncodedResult result){
		final StringBuilder resultBuilder = new StringBuilder();
		Node current = result.getRoot();
		int i = 0;
		
		//Traverses tree to find bits and then appended the characters
		while(i < result.getEncodedData().length()){
			
			//Looks for bits in the message
			while(!current.isLeaf()){
				char bit = result.getEncodedData().charAt(i);
				if(bit == '1'){
					current = current.rightChild;
				} else if(bit == '0'){
					current = current.leftChild;
				} else {
					throw new IllegalArgumentException("Invalid Bit in Message: " + bit);
				}
				i++;
			}
			resultBuilder.append(current.character);
			current = result.getRoot();
		}
		return resultBuilder.toString();	//Returns characters to a string
	}
	
	//Builds and compares Nodes
	static class Node implements Comparable<Node>{
		
		//Sets up variables
		private final char character;
		private final int frequency;
		private final Node leftChild;
		private final Node rightChild;
		
		//Builds variables
		private Node(final char character,final int frequency,final Node leftChild,final Node rightChild){
			this.character = character;
			this.frequency = frequency;
			this.leftChild = leftChild;
			this.rightChild = rightChild;
		}
		
		//If you don't have a left or right child you are a leaf
		boolean isLeaf(){
			return this.leftChild == null && this.rightChild == null;
		}
		
		@Override	//Compares Nodes
		public int compareTo(Node that) {
			final int frequencyComparison = Integer.compare(this.frequency, that.frequency);
			if(frequencyComparison != 0){
				return frequencyComparison;
			}
			return Integer.compare(this.character, that.character);
		}
	}
	
	//Puts together the results
	static class HuffmanEncodedResult {
		final Node root;
		final String encodedData;
		
		//Shows the encoded result of text
		HuffmanEncodedResult(final String encodedData, final Node root){
			this.encodedData = encodedData;
			this.root = root;
		}
		
		//Picks the node for the root
		public Node getRoot(){
			return this.root;
		}
		
		//Gets the string of binary bits
		public String getEncodedData(){
			return this.encodedData;
		}
	}
	
	public static void main(String[] args){
		
		try {
			//Getting input from user
			@SuppressWarnings("resource")
			Scanner in = new Scanner(System.in);
			System.out.print("Insert file name you would like to encode with the file extention: ");
			String input = in.next();	//String input = "source.txt"
			String content = new String(Files.readAllBytes(Paths.get(input)), "UTF-8");	//Reads text file
			
			//Calling Functions for results of enc
			final HuffmanEncoder encoder = new HuffmanEncoder();
			final HuffmanEncodedResult result = encoder.compress(content);
			
			//Creates Huffman encoded file from the source.txt file and writes the encoded message on the file
			File file = new File("encoded.huff");
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write(result.encodedData);
			fileWriter.close();
			
			//Used for testing output to file
			//String content2 = new String(Files.readAllBytes(Paths.get("encoded.huff")), "UTF-8");
			//System.out.println(content2);
			
			//Creates new file in which the Huffman encoding is decoded back to characters
			File file2 = new File("result.txt");
			FileWriter fileWriter2 = new FileWriter(file2);
			fileWriter2.write(encoder.decompress(result));
			fileWriter2.close();
			
			//Used for testing output to file
			//String content3 = new String(Files.readAllBytes(Paths.get("result.txt")), "UTF-8");
			//System.out.println(content3);
			
			//Message for where to find the files
			System.out.println("Note: Files of Huffman encoded message and Huffman decoded message can be found in the project folder.");
			
			//Use for testing - Can be commented out
			System.out.println();
			System.out.println("Orginal Text in " + input + ": " + content);
			System.out.println("Huffman Encoding: " + result.encodedData);
			System.out.println("Huffman Decoding: " + encoder.decompress(result));
			
		//Error Detection
		} catch (UnsupportedEncodingException e) {
			System.out.println("Unsupported Encoding ... Must be UTF-8");
			System.out.println("Try Again ... \n");
			main(args);
		} catch (IOException e) {
			System.out.println("Error in Reading the File ... Maybe Wrong File Name ... Don't Forget to Add the File Extention");
			System.out.println("Try Again ... \n");
			main(args);
		}
		
	}
	
}
