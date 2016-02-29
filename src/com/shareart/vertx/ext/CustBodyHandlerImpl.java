package com.shareart.vertx.ext;

import java.io.File;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.impl.FileUploadImpl;

/**
 * Ref io.vertx.ext.web.handler.impl.BodyHandlerImpl.java vertx file
 * updated to save file with extension
 * @author Buddhadev
 *
 */
public class CustBodyHandlerImpl implements BodyHandler {
	private static Logger log = Logger.getLogger(CustBodyHandlerImpl.class);
	  private long bodyLimit = DEFAULT_BODY_LIMIT;
	  private File uploadsDir;
	  private boolean mergeFormAttributes = DEFAULT_MERGE_FORM_ATTRIBUTES;

	  public CustBodyHandlerImpl() {
	    setUploadsDirectory(DEFAULT_UPLOADS_DIRECTORY);
	  }

	  @Override
	  public void handle(RoutingContext context) {
	    HttpServerRequest request = context.request();
	    BHandler handler = new BHandler(context);
	    request.handler(handler);
	    request.endHandler(v -> handler.end());
	  }

	  @Override
	  public BodyHandler setBodyLimit(long bodyLimit) {
	    this.bodyLimit = bodyLimit;
	    return this;
	  }

	  @Override
	  public BodyHandler setUploadsDirectory(String uploadsDirectory) {
	    this.uploadsDir = new File(uploadsDirectory);
	    if (!uploadsDir.exists()) {
	      uploadsDir.mkdirs();
	    }
	    return this;
	  }

	  @Override
	  public BodyHandler setMergeFormAttributes(boolean mergeFormAttributes) {
	    this.mergeFormAttributes = mergeFormAttributes;
	    return this;
	  }

	  private class BHandler implements Handler<Buffer> {

	    RoutingContext context;
	    Buffer body = Buffer.buffer();
	    boolean failed;
	    AtomicInteger uploadCount = new AtomicInteger();
	    boolean ended;

	    private BHandler(RoutingContext context) {
	      this.context = context;
	      try {
			Set<FileUpload> fileUploads = context.fileUploads();
			  context.request().setExpectMultipart(true);
			  context.request().exceptionHandler(context::fail);
			  context.request().uploadHandler(upload -> {
			    // We actually upload to a file with a generated filename
			    uploadCount.incrementAndGet();
			    String fileExt = "";
			    log.info("upload.filename() :: "+upload.filename());
			    try {
					fileExt = upload.filename().substring(upload.filename().lastIndexOf("."));
				} catch (Exception e) {
				}
			    String uploadedFileName = new File(uploadsDir, UUID.randomUUID().toString()+fileExt).getPath();
			    upload.streamToFileSystem(uploadedFileName);
			    FileUploadImpl fileUpload = new FileUploadImpl(uploadedFileName, upload);
			    fileUploads.add(fileUpload);
			    upload.exceptionHandler(context::fail);
			    upload.endHandler(v -> uploadEnded());
			  });
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    }

	    @Override
	    public void handle(Buffer buff) {
	      if (failed) {
	        return;
	      }
	      if (bodyLimit != -1 && (body.length() + buff.length()) > bodyLimit) {
	        failed = true;
	        context.fail(413);
	      } else {
	        body.appendBuffer(buff);
	      }
	    }

	    void uploadEnded() {
	      int count = uploadCount.decrementAndGet();
	      if (count == 0) {
	        doEnd();
	      }
	    }

	    void end() {
	      if (uploadCount.get() == 0) {
	        doEnd();
	      }
	    }

	    void doEnd() {
	      if (failed || ended) {
	        return;
	      }
	      ended = true;
	      HttpServerRequest req = context.request();
	      if (mergeFormAttributes && req.isExpectMultipart()) {
	        req.params().addAll(req.formAttributes());
	      }
	      context.setBody(body);
	      context.next();
	    }
	  }
}