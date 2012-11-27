package crowdtrust;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;

import db.LoginDb;

import java.util.Properties;

import java.io.IOException;
import java.io.PrintWriter;

import web.*;

public class LoginServlet extends HttpServlet {

  protected void doPost(HttpServletRequest request, HttpServletResponse response)
                 throws ServletException, IOException {
    if(request.isRequestedSessionIdValid()) {
      response.sendRedirect("/lobby.html");
    }  
    String username = request.getParameter("username");
    String password = request.getParameter("password");
    int id = LoginDb.checkUserDetails(username, password);
    if(!request.isRequestedSessionIdValid() && id > 0) {
    	System.out.println("Hello World");
      HttpSession session = request.getSession();
      session.setMaxInactiveInterval(1200);
      session.setAttribute("account_id", id);
      session.setAttribute("account_name", username);
      Lobby userLobby = new Lobby(username);
      userLobby.addClientTable();
      PrintWriter out = response.getWriter();
      out.print(userLobby.generate());
    }
  }
  
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
	  doPost(request, response);
  }
  
}
