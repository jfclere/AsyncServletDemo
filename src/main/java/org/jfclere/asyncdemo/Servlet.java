package org.jfclere.asyncdemo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;

import javax.servlet.ReadListener;

import javax.servlet.annotation.WebServlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class
 */
@WebServlet(description = "Demo for Asyn", urlPatterns = { "/demo" }, asyncSupported = true)
public class Servlet extends HttpServlet implements AsyncListener {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
           throws ServletException, IOException {
        if (req.isAsyncStarted()) {
            req.getAsyncContext().complete();
        } else if (req.isAsyncSupported()) {
            AsyncContext actx = req.startAsync();
            actx.addListener(this);
            resp.setContentType("text/plain");

            DemoReader listener = new DemoReader(
                actx, req.getInputStream(), resp.getOutputStream());

        } else {
            new Exception("Async Not Supported").printStackTrace();
            resp.sendError(400,"Async is not supported.");
        }
    }

    @Override
    public void onComplete(AsyncEvent event) throws IOException {
        System.out.println("onComplete");
    }

    @Override
    public void onError(AsyncEvent event) throws IOException {
        System.out.println("onError");
    }

    @Override
    public void onTimeout(AsyncEvent event) throws IOException {
        System.out.println("onTimeout");
    }

    @Override
    public void onStartAsync(AsyncEvent event) throws IOException {
        System.out.println("onStartAsync");
    }

    private static class DemoReader implements ReadListener {
        private final AsyncContext ac;
        private final ServletInputStream sis;
        private final ServletOutputStream sos;

        private byte[] buffer = new byte[128];

        private DemoReader(AsyncContext ac, ServletInputStream sis,
                ServletOutputStream sos) {
            this.ac = ac;
            this.sis = sis;
            this.sos = sos;
            sis.setReadListener(this);
        }

        @Override
        public void onDataAvailable() throws IOException {
            System.out.println("onDataAvailable");
            int read = 0;
            while (sis.isReady() && read > -1) {
                read = sis.read(buffer);
                /* echo stuff back */
                sos.write(buffer);
                sos.flush();
                System.out.println("onDataAvailable: read " + read);
            }
        }
        @Override
        public void onError(Throwable throwable) {
            ac.complete();
        }

        @Override
        public void onAllDataRead() throws IOException {
            System.out.println("onAllDataRead");
            ac.complete();
        }
    }
}
