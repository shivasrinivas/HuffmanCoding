import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.util.BitSet;
import java.util.List;

public class hdec {
	
	public static void main(String args[]) {
		Utilities.log("main method started");
		Utilities.setDebug(false);

		String fileName = args[0];
		File outputFile, file;
		FileInputStream fileInputStream;
		ObjectInputStream objectInputStream;
		BitSet bitStream;
		
		try{
				String outputFileName = fileName.replace(".huff", "");
				file = new File(fileName);
				outputFile = new File(outputFileName);
				fileInputStream = new FileInputStream(file);
				objectInputStream = new ObjectInputStream(fileInputStream);

				if (!outputFile.exists()) {
				outputFile.createNewFile();
				}

				FileObject fileObject = (FileObject) objectInputStream.readObject();
				bitStream = BitSet.valueOf(fileObject.getBits());
				int bitCount = fileObject.getBitCount();
				List<Node> sortedCharacters = fileObject.getCharacters();
				Node huffmanTree = Utilities.createHuffmanTree(sortedCharacters);
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				getByteString(huffmanTree, byteArrayOutputStream, bitCount, bitStream);
				byte[] byteString = byteArrayOutputStream.toByteArray();

				FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
				fileOutputStream.write(byteString);
				file.delete();
				System.out.println("File decoded Successfully");
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

		Utilities.log("main method ended");
	}

	public static void getByteString(Node huffmanTree, ByteArrayOutputStream byteArrayOutputStream, 
		int bitCount, BitSet bitStream){
		Utilities.log("getByteString started");
		
		Node currentNode = huffmanTree;

		for(int i = 0; i < bitCount; i++){
			if(bitStream.get(i) == true){
				currentNode = currentNode.getRightNode();
			} else {
				currentNode = currentNode.getLeftNode();
			}

			if(currentNode.isCharacter()){
				byteArrayOutputStream.write(currentNode.getCharacter());
				currentNode = huffmanTree;
			}
		}

		Utilities.log("getByteString ended");
	}

}