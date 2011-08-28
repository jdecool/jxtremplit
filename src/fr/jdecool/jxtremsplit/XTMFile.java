package fr.jdecool.jxtremsplit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Formatter;

/**
 * XTM file format class
 * @author Jérémy DECOOL
 * @see http://xtremsplit.fr/xtremsplit-developpeurs-format-extension-xtm.html
 */
public class XTMFile 
{
	/**
	 * Library name
	 */
	private static final String LIBRARY_NAME = "JXtremsplit";
	
	/**
	 * Program which created the file
	 */
	private String programName;
	
	/**
	 * Version program which created the file
	 */
	private String versionName;
	
	/**
	 * File date creation
	 */
	private String date;
	
	/**
	 * Original filename
	 */
	private String originalFilename;
	
	/**
	 * Number of split file
	 */
	private int fileCount;
	
	/**
	 * Original file size
	 */
	private int originalFileSize;
	
	/**
	 * Indicate if the have hash
	 */
	private boolean hasHash;
	
	/**
	 * Extract file XTM file
	 * @param file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void extract(String file) throws FileNotFoundException, IOException
	{
		extract(file, "");
	}
	
	/**
	 * Extract file XTM file
	 * @param file
	 * @param destination
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void extract(String file, String destination) throws FileNotFoundException, IOException
	{
		extract(new File(file), destination);
	}
	
	/**
	 * Extract file XTM file
	 * @param file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void extract(File file) throws FileNotFoundException, IOException
	{
		extract(file, "");
	}
	
	/**
	 * Split source file
	 * @param source
	 * @param destination
	 * @param size
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void split(String source, String destination, int size) throws FileNotFoundException, IOException
	{
		split(new File(source), new File(destination), size);
	}
	
	/**
	 * Split source file
	 * @param source
	 * @param destination
	 * @param size Byte size
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void split(File source, File destination, int size) throws FileNotFoundException, IOException
	{
		FileOutputStream output = new FileOutputStream(destination);
		
		writeHeader(source, output);
		executeSplit(source, destination, output, size);
	}
	
	/**
	 * Extract file XTM file
	 * @param file
	 * @param destination
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void extract(File file, String destination) throws FileNotFoundException, IOException
	{
		FileOutputStream output = new FileOutputStream(destination);
	
		// read first file
		FileInputStream stream = new FileInputStream(file);
		readHeader(stream);
		executeExtraction(stream, output);
		
		// read other file
		String nextFile = getNextFile(file.getAbsolutePath(), true);
		while(nextFile != null)
		{
			stream = new FileInputStream(nextFile);
			executeExtraction(stream, output);
			
			nextFile = getNextFile(nextFile, true);
		}
	}
	
	/**
	 * Parse XTM header
	 * @param stream
	 * @throws IOException
	 */
	private void readHeader(FileInputStream stream) throws IOException
	{
		byte buff[];
		
		// read program name
		stream.skip(1);
		
		buff = new byte[20];
		stream.read(buff, 0, 20);
		programName = new String(buff);
		
		// read version
		stream.skip(1);
		
		buff = new byte[4];
		stream.read(buff, 0, 4);
		versionName = new String(buff);
		
		// parse unsed bytes
		stream.skip(10);
		
		// read date
		buff = new byte[4];
		stream.read(buff, 0, 4);
		
		// read filename
		stream.skip(1);
		
		buff = new byte[50];
		stream.read(buff, 0, 50);
		originalFilename = new String(buff);
		
		// has hash
		buff = new byte[1];
		stream.read(buff, 0, 1);
		
		// file count
		buff = new byte[4];
		stream.read(buff, 0, 4);
		
		// original filesize
		buff = new byte[8];
		stream.read(buff, 0, 8);
	}
	
	/**
	 * Write header content into file
	 * @param output
	 * @throws IOException 
	 */
	private void writeHeader(File source, FileOutputStream output) throws IOException
	{
		String strToWrite;
		byte buff[];
		
		// write program name
		strToWrite = String.valueOf(LIBRARY_NAME.length());
		buff = strToWrite.getBytes();
		output.write(buff, 0, 1);
		
		buff = LIBRARY_NAME.getBytes();    output.write(buff, 0, buff.length);
		buff = new byte[20 - buff.length]; output.write(buff, 0, buff.length);
		
		// write version
		strToWrite = String.valueOf(LIBRARY_NAME.length());
		buff = strToWrite.getBytes();
		output.write(buff, 0, 1);
		
		strToWrite = "1.2";
		buff = strToWrite.getBytes();     output.write(buff, 0, buff.length);
		buff = new byte[4 - buff.length]; output.write(buff, 0, buff.length);
		
		// blank
		output.write(new byte[10], 0, 10);
		
		// date
		output.write(new byte[4], 0, 4);
		
		// original filname
		strToWrite = String.valueOf(source.getName().length());
		buff = strToWrite.getBytes();
		output.write(buff, 0, 1);
		
		buff = source.getName().getBytes(); output.write(buff, 0, buff.length);
		buff = new byte[50 - buff.length];  output.write(buff, 0, buff.length);
		
		// has hash
		strToWrite = String.valueOf(0);
		buff = strToWrite.getBytes();
		output.write(buff, 0, 1);
		
		// file count
		strToWrite = String.valueOf(4);
		buff = strToWrite.getBytes();     output.write(buff, 0, buff.length);
		buff = new byte[4 - buff.length]; output.write(buff, 0, buff.length);
		
		// original filesize
		buff = new byte[8]; output.write(buff, 0, buff.length);
	}
	
	/**
	 * Join 1 XTM file with output
	 * @param stream
	 * @param destination
	 * @throws IOException
	 */
	private void executeExtraction(FileInputStream stream, FileOutputStream destination) throws IOException
	{
		int readLength;
		byte buff[] = new byte[1024];
		
		readLength = stream.read(buff, 0, 1024);
		while(readLength > 0)
		{
			destination.write(buff, 0, readLength);
			readLength = stream.read(buff, 0, 1024);
		}
	}
	
	/**
	 * Split input into output file part in byteLength size part
	 * @param input
	 * @param output
	 * @param byteLength
	 * @throws IOException 
	 */
	private void executeSplit(File source, File destination, FileOutputStream output, int byteLength) throws IOException
	{
		int toRead;
		int totalSizeWrite = 0;
		byte buff[] = new byte[1024];
		String fileToWrite = destination.getAbsolutePath();
		
		FileInputStream input = new FileInputStream(source);
		int inputRead = input.read(buff, 0, 1024);
		while(inputRead > 0)
		{
			if(totalSizeWrite + inputRead > byteLength)
			{
				// complete part file
				toRead = byteLength - totalSizeWrite;
				output.write(buff, 0, toRead);
				
				// open next part file
				fileToWrite = getNextFile(fileToWrite, false);
				
				totalSizeWrite = inputRead - toRead;
				
				output = new FileOutputStream(fileToWrite);
				output.write(buff, toRead, totalSizeWrite);
			}
			else
				output.write(buff, 0, inputRead);
			
			totalSizeWrite += inputRead;
			inputRead = input.read(buff, 0, 1024);
		}
	}
	
	/**
	 * Get the next XTM filename to concat
	 * @param currentFile
	 * @return
	 */
	private String getNextFile(String currentFile, boolean verifyFileExistance)
	{
		int filenameLength = currentFile.length();
		String filename = currentFile.substring(0, filenameLength - 8);
		int index = Integer.parseInt(currentFile.substring(filenameLength - 7, filenameLength - 4));
		
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb);
		formatter.format("%s.%03d.xtm", filename, ++index);
		
		File next = new File(formatter.toString());
		if(!verifyFileExistance || next.exists())
			return next.getAbsolutePath();
		
		return null;
	}
	
	/**
	 * String object representation
	 */
	public String toString()
	{
		StringBuilder str = new StringBuilder();
		str.append(programName);
		str.append("@" + versionName);
		str.append("@" + date);
		str.append("@" + originalFilename);
		str.append("@" + fileCount);
		str.append("@" + originalFileSize);
		str.append("@" + hasHash);
		
		return str.toString();
	}
}
