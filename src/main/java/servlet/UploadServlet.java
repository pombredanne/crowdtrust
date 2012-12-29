package servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

@MultipartConfig
public class UploadServlet extends HttpServlet {

	private static final long serialVersionUID = -8279791785237277465L;
	private static final String TASKS_DIRECTORY = "/vol/project/2012/362/g1236218/TaskFiles";
	
	
	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
		
		response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
		List<FileItem> files = null;
		try {
			files = (List<FileItem>) new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
		} catch (FileUploadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
    	String task = "";
    	String taskDir = TASKS_DIRECTORY;
    	FileItem song = null;
        for( FileItem file : files ) {
        	if(!file.isFormField()) {
        		song = file;
        	}
        	else {
        		String fieldName = file.getFieldName();
        		task = file.getString();
        		if( task == null ) {
        			//TODO need to input task name
        		}
        		else {
        			//task is task name - add task to db if not yet inputted
        			taskDir = taskDir + task;
        			File f = new File(taskDir);
        			if( !f.exists() ) {
        				if( !f.mkdirs() ) {
        					//directory creation failed
        					System.err.println("COULDN'T CREATE DIRECTORY: " + taskDir);
        					return;
        				}
        			}
        		}
        	}
        }
        InputStream fileIn = song.getInputStream();
        OutputStream fileOut = new FileOutputStream(taskDir + "/" + song.getName());
        IOUtils.copy(fileIn, fileOut);
        fileOut.close();
        out.println("<html>");
        out.println("<body>");
        out.println("uploaded \""+song.getName() + "\" to task " + taskDir + "<br>");                	
        out.println("click <a href=index.jsp>here</a> to return to the homepage");
        out.println("</body>");
        out.println("</html>");
    }
	
}
