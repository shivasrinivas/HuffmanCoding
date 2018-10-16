import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.BitSet;

public class henc {
	public static void main(String args[]){
		Utilities.log("main method started");
		Utilities.setDebug(false);
		
		String fileName = args[0];
		List<Node> characters = new ArrayList<Node>();
		File outputFile, file;
		FileOutputStream fileOutputStream;
		ObjectOutputStream objectOutputStream;

		try {
			file = new File(fileName);
			outputFile = new File(fileName + ".huff");
			fileOutputStream = new FileOutputStream(outputFile);
			objectOutputStream = new ObjectOutputStream(fileOutputStream);
			Path filePath = Paths.get(file.getAbsolutePath());
			byte[] fileBytes = Files.readAllBytes(filePath);
			
			if (!outputFile.exists()) {
				outputFile.createNewFile();
			}

			characters = getCharactersList(fileBytes, characters);
			System.out.println("Total Characters found N = " + fileBytes.length);
			System.out.println("Unique Characters found n = " + characters.size());

			characters = Utilities.createMinHeap(characters);
			List<Node> sortedCharacters = new ArrayList<Node>(characters);
			Node huffmanTree = Utilities.createHuffmanTree(characters);
			Utilities.generateBits(sortedCharacters, huffmanTree);

			if(Utilities.isDebug()){
				for (Node character : sortedCharacters) {
					System.out.println("Character : " + character.getCharacter() + " , Frequency : " + character.getFrequency() + " , Bits : " + character.getBits());
				}
			}

			BitSet bitStream = new BitSet();
			int bitCount = getCharacterBitStream(fileBytes, sortedCharacters, bitStream);
			FileObject fileObject = new FileObject();

			fileObject.setCharacters(sortedCharacters);
			fileObject.setBits(bitStream.toByteArray());
			fileObject.setBitCount(bitCount);
			objectOutputStream.writeObject(fileObject);
			file.delete();
			System.out.println("File encoded Successfully");

			objectOutputStream.close();
			fileOutputStream.close();

		} catch (IOException e) {
			e.printStackTrace();
		} 

		Utilities.log("main method ended");
	}

	public static List<Node> getCharactersList(byte[] fileBytes, List<Node> characters){
		Utilities.log("getCharactersList started");
		for (int i = 0; i < fileBytes.length; i++) {
			boolean found = false;
			
			for (int j = 0; j < characters.size(); j++) {
				Node character = characters.get(j);
				if (character.getCharacter() == fileBytes[i]) {
					int count = character.getFrequency() + 1;
					character.setFrequency(count);
					found = true;
					break;
				}
			}

			if (!found) {
				Node character = new Node();
				character.setCharacter(fileBytes[i]);
				character.setIsCharacter(true);
				character.setFrequency(1);
				characters.add(character);
			}
		}
		Utilities.log("getCharactersList ended");
		return characters;
	}

	public static int getCharacterBitStream(byte[] fileBytes, List<Node> characters, BitSet bitStream) {
		Utilities.log("getCharacterBitStream ended");
		int currentBitIndex = 0;
		for (int i = 0; i < fileBytes.length; i++) {
			for (int j = characters.size() - 1; j >= 0; j--) {
				if (fileBytes[i] == characters.get(j).getCharacter()) {
					char[] bits = characters.get(j).getBits().toCharArray();
					for (int k = 0; k < bits.length; k++) {
						if (bits[k] == '1') {
							bitStream.set(currentBitIndex, true);
						}
						currentBitIndex++;
					}
					break;
				}
			}
		}
		Utilities.log("getCharacterBitStream ended");
		return currentBitIndex;
	}

}

class Utilities {

	private static boolean isDebug;

	public static void setDebug(boolean debug){
		isDebug = debug;
	}

	public static boolean isDebug(){
		return isDebug;
	}

	public static void log(String message){
		if(isDebug) {
			System.out.println("LOG : " + message);
		}
	}

	public static List<Node> createMinHeap(List<Node> characters) {
		log("createMinHeap started");
		List<Node> minHeap = new ArrayList<Node>();
		for (int i = 0; i < characters.size(); i++) {
			minHeap.add(characters.get(i));
			upHeap(minHeap);
		}

		log("createMinHeap ended");
		return minHeap;
	}

	public static void downHeap(List<Node> minHeap) {
		log("downHeap started");
		if (minHeap.size() > 1) {
			for (int childIndex = 1; childIndex < minHeap.size() - 1; childIndex++) {
				int child = minHeap.get(childIndex).getFrequency();
				int parentIndex = -1;
				if (childIndex % 2 == 0) {
					parentIndex = (childIndex - 2) / 2;
				} else {
					parentIndex = (childIndex - 1) / 2;
				}
				int parent = minHeap.get(parentIndex).getFrequency();
				if (parent > child) {
					Node swapCharacter = minHeap.get(parentIndex);
					minHeap.set(parentIndex, minHeap.get(childIndex));
					minHeap.set(childIndex, swapCharacter);
				}
			}
		}
		log("downHeap ended");
	}

	public static void upHeap(List<Node> minHeap) {
		log("upHeap ended");
		if (minHeap.size() > 1) {
			for (int childIndex = minHeap.size() - 1; childIndex > 0; childIndex--) {
				int child = minHeap.get(childIndex).getFrequency();
				int parentIndex = -1;
				if (childIndex % 2 == 0) {
					parentIndex = (childIndex - 2) / 2;
				} else {
					parentIndex = (childIndex - 1) / 2;
				}
				int parent = minHeap.get(parentIndex).getFrequency();
				if (parent > child) {
					Node swapCharacter = minHeap.get(parentIndex);
					minHeap.set(parentIndex, minHeap.get(childIndex));
					minHeap.set(childIndex, swapCharacter);
				}
			}
		}
		log("upHeap ended");
	}

	public static Node removeMinimumCharacter(List<Node> characters) {
		log("removeMinimumCharacter started");
		if (characters.size() > 0) {
			Node minimumCharacter = characters.get(0);
			characters.set(0, characters.get(characters.size() - 1));
			characters.remove(characters.size() - 1);
			downHeap(characters);
			log("removeMinimumCharacter ended");
			return minimumCharacter;
		}
		log("removeMinimumCharacter ended");
		return null;
	}

	public static Node createHuffmanTree(List<Node> characters){
		log("createHuffmanTree started");
		
		if(characters.size() == 0) {
			log("createHuffmanTree ended");
			return null;
		}
		
		if(characters.size() == 1) {
			Node currentNode = removeMinimumCharacter(characters);
			currentNode.setLeftNode(characters.get(0));
			currentNode.setFrequency(characters.get(0).getFrequency());
			log("createHuffmanTree ended");
			return currentNode;
		}

		while(characters.size() > 1) {
			Node leftNode = removeMinimumCharacter(characters);
			Node rightNode = removeMinimumCharacter(characters);
			Node parent = new Node();
			parent.setLeftNode(leftNode);
			parent.setRightNode(rightNode);
			parent.setFrequency(leftNode.getFrequency() + rightNode.getFrequency());
			characters.add(parent);
			upHeap(characters); 
		}
		
		log("createHuffmanTree ended");
		return removeMinimumCharacter(characters);
	}

	public static void generateBits(List<Node> characters, Node huffmanTree) {
		log("generateBits started");
		Node currentHuffmanTree = huffmanTree;
		Node leftNode = currentHuffmanTree.getLeftNode();
		Node rightNode = currentHuffmanTree.getRightNode();

		if (leftNode != null) {
			String bits;
			if (currentHuffmanTree.getBits() == null) {
				bits = "0";	
			} else {
				bits = currentHuffmanTree.getBits() + "0";
			}

			leftNode.setBits(bits);
			if(leftNode.isCharacter()){
				setCharacterBits(characters, leftNode, bits);	
			} else {
				generateBits(characters, leftNode);
			}
			
		}	

		if (rightNode != null) {
			String bits;
			if (currentHuffmanTree.getBits() == null) {
				bits = "1";	
			} else {
				bits = currentHuffmanTree.getBits() + "1";
			}

			rightNode.setBits(bits);
			if(rightNode.isCharacter()){
				setCharacterBits(characters, rightNode, bits);	
			} else {
				generateBits(characters, rightNode);
			}
			
		}
		log("generateBits ended");
	}

	public static void setCharacterBits(List<Node> characters, Node character, String bits) {
		log("setCharacterBits Started");
		for (int i = 0; i < characters.size(); i++) {
			if (characters.get(i).getCharacter() == character.getCharacter()) {
				characters.get(i).setBits(bits);
			}
		}
		log("setCharacterBits Ended");
	}
}

class FileObject implements Serializable {
	private List<Node> characters;
	private byte[] bits;
	private int bitCount;

	public List<Node> getCharacters(){
		return characters;
	}

	public void setCharacters(List<Node> characters){
		this.characters = characters;
	}

	public byte[] getBits(){
		return bits;
	}

	public void setBits(byte[] bits){
		this.bits = bits;
	}

	public int getBitCount(){
		return bitCount;
	}

	public void setBitCount(int bitCount){
		this.bitCount = bitCount;
	}
}

class Node implements Serializable{
	private int character;
	private boolean isCharacter;
	private int frequency;
	private String bits;
	private Node leftNode;
	private Node rightNode;

	public int getCharacter(){
		return character;
	}

	public void setCharacter(int character){
		this.character = character;
	}

	public boolean isCharacter(){
		return isCharacter;
	}

	public void setIsCharacter(boolean isCharacter){
		this.isCharacter = isCharacter;
	}

	public int getFrequency(){
		return frequency;
	}

	public void setFrequency(int frequency){
		this.frequency = frequency;
	}

	public String getBits(){
		return bits;
	}

	public void setBits(String bits){
		this.bits = bits;
	}

	public Node getLeftNode(){
		return leftNode;
	}

	public void setLeftNode(Node leftNode){
		this.leftNode = leftNode;
	}

	public Node getRightNode(){
		return rightNode;
	}

	public void setRightNode(Node rightNode){
		this.rightNode = rightNode;
	}
}