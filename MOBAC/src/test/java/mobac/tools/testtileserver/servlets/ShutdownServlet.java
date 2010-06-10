package mobac.tools.testtileserver.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Acme.Serve.Serve;

public class ShutdownServlet extends HttpServlet {

	private final Serve server;

	public ShutdownServlet(Serve server) {
		super();
		this.server = server;
	}

	@Override
	protected void doDelete(HttpServletRequest arg0, HttpServletResponse response) throws ServletException, IOException {
		response.setStatus(202);
		response.flushBuffer();
		server.notifyStop();
	}

}
