package textractS3Image;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.AmazonTextractClientBuilder;
import com.amazonaws.services.textract.model.Block;
import com.amazonaws.services.textract.model.BoundingBox;
import com.amazonaws.services.textract.model.DetectDocumentTextRequest;
import com.amazonaws.services.textract.model.DetectDocumentTextResult;
import com.amazonaws.services.textract.model.Document;
import com.amazonaws.services.textract.model.S3Object;

public class Demo {
	private static final String documentInput = "JomyAdharBack.jpg";
	private static final String bucketName = "textractimgtesting";
	private static final String documentOutput = "./documents/text.txt";
	private static final String awsCredentials = "./documents/credentials.txt";
	private static String aws_access_key_id;
	private static String aws_secret_access_key;
	
	public static void main(String args[]) {

        try {
        	
            System.out.println("Started Extracting  text from "+ documentInput+" Object from S3");
            List<TextLine> lines = extractText(bucketName, documentInput);
            File file = new File(documentOutput);
            if(!file.exists())
            	file.createNewFile();
            FileWriter writer = new FileWriter(file); 
            for(TextLine line: lines) {
            	writer.write(line.text + System.lineSeparator());
            }
            writer.close();
            System.out.println("Ended Extracting  text from "+documentInput+" Object from S3documentOutput\nSaved in plaintext "+documentOutput+" file");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static List<TextLine> extractText(String bucketName, String documentName){

    	com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration endpoint = new com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration(
                "https://textract.us-east-1.amazonaws.com", "us-east-1");
      
    
    	try {
			aws_access_key_id = Files.readAllLines(Paths.get(awsCredentials), Charset.defaultCharset()).get(1);
			aws_secret_access_key = Files.readAllLines(Paths.get(awsCredentials), Charset.defaultCharset()).get(2);
			aws_access_key_id = aws_access_key_id.substring((aws_access_key_id.indexOf("=")+1), aws_access_key_id.length());
			aws_secret_access_key = aws_secret_access_key.substring((aws_secret_access_key.indexOf("=")+1), aws_secret_access_key.length());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	AWSCredentials awsCredentials = new BasicAWSCredentials(aws_access_key_id, aws_secret_access_key);
    	
    	AmazonTextractClientBuilder amazonTextractClientBuilder = AmazonTextractClientBuilder.standard().withEndpointConfiguration(endpoint).withCredentials(new AWSStaticCredentialsProvider(awsCredentials));
        AmazonTextract client = amazonTextractClientBuilder.build();
       
        DetectDocumentTextRequest request = new DetectDocumentTextRequest()
                .withDocument(new Document()
                        .withS3Object(new S3Object()
                                .withName(documentName)
                                .withBucket(bucketName)));

        DetectDocumentTextResult result = client.detectDocumentText(request);

        List<TextLine> lines = new ArrayList<TextLine>();
        List<Block> blocks = result.getBlocks();
        BoundingBox boundingBox = null;
        for (Block block : blocks) {
            if ((block.getBlockType()).equals("LINE")) {
                boundingBox = block.getGeometry().getBoundingBox();
                lines.add(new TextLine(boundingBox.getLeft(),
                        boundingBox.getTop(),
                        boundingBox.getWidth(),
                        boundingBox.getHeight(),
                        block.getText()));
            }
        }

        return lines;
    }
}
