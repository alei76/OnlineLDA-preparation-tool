/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package infrascructure.data.launch;

import infrascructure.data.Config;
import infrascructure.data.util.IOHelper;
import infrascructure.data.util.Trace;
import infrascructure.data.vocabulary.Vocabulary;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;



/**
 * @author shredinger
 *
 */
public class SimpleDocsRepository extends DocsRepository{

    private FilesQueue files;
    
     /**
     * 
     */
    public SimpleDocsRepository() {
	init();
    }
    
    /* (non-Javadoc)
     * @see vagueobjects.ir.lda.online.DocsRepository#getBatchDocs()
     */
    @Override
    public List<String> getBatchDocs(int batchSize) throws IOException {
	
	int count = 0;
	try {
	    ArrayList<String> batch = new ArrayList<>(batchSize);
	    while(count ++ < batchSize) {
		if(!files.hasNext()) {
		    return null;
		}
		String path = files.getNextEntry();
		String data = IOHelper.readFromoFile(path);		
		batch.add(data);
	    }	
	    return batch;
	}finally {
	    files.flush();
	}
	
    }

    /* (non-Javadoc)
     * @see vagueobjects.ir.lda.online.DocsRepository#getCurrentVocabulary()
     */
    @Override
    public List<String> getCurrentVocabulary() throws IOException {
	String path = Config.getProperty("vocabulary_path");	
	return IOHelper.readLinesFromoFile(path);
    }
    
    public void init() {
	
	try {
	    String path = Config.getProperty("queue_docs");
	    files = FilesQueue.createFilesQueue(null, path);
	    if(!files.hasNext()) {
		DirectoryreaderFactory fact = new DirectoryreaderFactory();
		DirectoryReader directoryReader = fact.getDirectoryReader();
		List<String> allFiles = directoryReader.getFiles();
		Trace.trace("Files: " + allFiles.size());
		files = FilesQueue.createFilesQueue(allFiles, path);
	    }    
	} catch (IOException e) {	    
	    e.printStackTrace();
	}
    }
    
    public static class FilesQueue{
	private Queue<String> files;	
	private String path;
	
	public static FilesQueue createFilesQueue(List<String> files, String path) throws IOException {
	    return new FilesQueue(files, path);
	}
	
	public String getNextEntry() {
	    return files.poll();
	}
	
	public boolean hasNext() {
	    return !files.isEmpty();
	}
	
	/**
	 * @throws IOException 
	 * 
	 */
	protected FilesQueue(Collection<String> files, String path) throws IOException {
	    this.path = path;
	    this.files = new LinkedList<>();
	    restore();
	    if(files != null) {
		this.files.addAll(files);
	    }	        
	}
	
	private void restore() throws IOException {
	    File f = new File(path);
	    if(f.exists()) {
		List<String> files = IOHelper.readLinesFromoFile(path);
		this.files.addAll(files);
	    }	    
	}
	
	private void store() throws IOException {	
	    try(PrintWriter writer = new PrintWriter(path)){
		 while(!this.files.isEmpty()) {
		     String entry = files.poll();
		     writer.println(entry);		     
		 }	    
	    }	    
	}

	public void flush() throws IOException {
	    store();	    
	}
	
    }

}
