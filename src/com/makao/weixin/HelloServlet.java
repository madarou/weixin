package com.makao.weixin;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
/*
       name == <servlet-name>
       urlPatterns == <url-pattern>,
       loadOnStartup == <load-on-startup>
       initParam == <init-param>
       name == <param-name>
       value == <param-value>
*/
@WebServlet(name="HelloServlet" ,urlPatterns={"/HelloServlet"},loadOnStartup=1,
                    initParams={
                           @WebInitParam(name="name",value="xiazdong"),
                           @WebInitParam(name="age",value="20")
                    })
public class HelloServlet extends HttpServlet{
       public void init(ServletConfig config)throws ServletException{
              super.init(config);
       }
       public void service(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
              request.setCharacterEncoding("GBK");
              ServletConfig config = getServletConfig();
              PrintWriter out = response.getWriter();
              out.println("<html>");
              out.println("<body>");
              out.println("Hello world"+"<br />");
              out.println(config.getInitParameter("name"));
              out.println("</body>");
              out.println("</html>");
       }
}

