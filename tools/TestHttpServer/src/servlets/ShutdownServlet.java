package servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import server.TestHttpTileServer;

public class ShutdownServlet extends HttpServlet {

	@Override
	protected void doDelete(HttpServletRequest arg0, HttpServletResponse response)
			throws ServletException, IOException {
		response.setStatus(202);
		response.flushBuffer();
		TestHttpTileServer.shutdown();
	}

}
