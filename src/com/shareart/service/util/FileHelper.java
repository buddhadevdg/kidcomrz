package com.shareart.service.util;

import java.io.File;

import org.apache.log4j.Logger;

import io.vertx.core.Vertx;

public class FileHelper {
	private static Vertx vertx;
	private static Logger log = Logger.getLogger(FileHelper.class);
	
	private FileHelper(){
		
	}
	
	public static void init(Vertx vtx) {
		vertx = vtx;
	}
	
	public static void delteFile(String fielPath){
		vertx.executeBlocking(future -> {
			try {
				File file = new File(fielPath);
				file.delete();
				future.complete();
			} catch (Exception e) {
				log.error(e);
				future.fail(e);
			}
		} , res -> {
			if (res.succeeded()) {
				log.info("File Deltee successfully! ");
			} else {
				log.info("Failed to delete File ");
			}
		});	
	}
}