

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet(// sets the URL of the page and the load sequence
        name = "supportTicketServlet",
        urlPatterns = {"/support"},
        loadOnStartup = 1
)
@MultipartConfig(// sets the max file size for each form submit
        fileSizeThreshold = 5_242_880, //5MB
        maxFileSize = 20_971_520L, //20MB
        maxRequestSize = 41_943_040L //40MB
)
public class SupportTicketServlet extends HttpServlet
{
    private volatile int TICKET_ID_SEQUENCE = 1;// counts the number of forms submitted

    private Map<Integer, SupportTicket> ticketDatabase = new LinkedHashMap<>();// stores the support tickets

    @Override// method for getting the page action options
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        String action = request.getParameter("action");
        if(action == null)
            action = "list";
        switch(action)// cases for the different option types
        {
            case "create":// presents the ticket form
                this.showTicketForm(response);
                break;
            case "view":// shows the ticket contents after submission
                this.viewTicket(request, response);
                break;
            case "download":// allows for the attachment to be downloaded
                this.downloadAttachment(request, response);
                break;
            case "list":// shows a list of all tickets submitted
            default:
                this.listTickets(response);
                break;
        }
    }

    @Override// method to post the actions on the page.
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        String action = request.getParameter("action");
        if(action == null)
            action = "list";
        switch(action)
        {
            case "create":
                this.createTicket(request, response);
                break;
            case "list":
            default:
                response.sendRedirect("support");
                break;
        }
    }

    private void showTicketForm(HttpServletResponse response)
            throws ServletException, IOException
    {// method that presents the form on the page to create a ticket
        PrintWriter writer = this.writeHeader(response);

        writer.append("<h2>Create a Ticket</h2>\r\n");// title
        writer.append("<form method=\"POST\" action=\"support\" ")//post location
              .append("enctype=\"multipart/form-data\">\r\n");
        writer.append("<input type=\"hidden\" name=\"action\" ")
              .append("value=\"create\"/>\r\n");
        writer.append("Your Name<br/>\r\n");
        writer.append("<input type=\"text\" name=\"customerName\"/><br/><br/>\r\n");// name text field
        writer.append("Subject<br/>\r\n");
        writer.append("<input type=\"text\" name=\"subject\"/><br/><br/>\r\n");// subject text field
        writer.append("Body<br/>\r\n");
        writer.append("<textarea name=\"body\" rows=\"5\" cols=\"30\">")
              .append("</textarea><br/><br/>\r\n");// body textarea field
        writer.append("<b>Attachments</b><br/>\r\n");
        writer.append("<input type=\"file\" name=\"file1\"/><br/><br/>\r\n");// attachment field
        writer.append("<input type=\"submit\" value=\"Submit\"/>\r\n");// submit button
        writer.append("</form>\r\n");

        this.writeFooter(writer);
    }

    private void viewTicket(HttpServletRequest request, 
                            HttpServletResponse response)
            throws ServletException, IOException
    {// method to show ticket contents after submission
        String idString = request.getParameter("ticketId");
        SupportTicket ticket = this.getTicket(idString, response);// gets the sequence number of the ticket
        if(ticket == null)
            return;

        PrintWriter writer = this.writeHeader(response);

        writer.append("<h2>Ticket #").append(idString)
              .append(": ").append(ticket.getSubject()).append("</h2>\r\n");// shows ticket number and subject line
        writer.append("<i>Customer Name - ").append(ticket.getCustomerName())
              .append("</i><br/><br/>\r\n");// shows the customer name
        writer.append(ticket.getBody()).append("<br/><br/>\r\n");// shows the ticket body contents

        if(ticket.getNumberOfAttachments() > 0)// shows the attached files of the ticket if they exist
        {
            writer.append("Attachments: ");
            int i = 0;
            for(Attachment attachment : ticket.getAttachments())
            {
                if(i++ > 0)
                    writer.append(", ");
                writer.append("<a href=\"support?action=download&ticketId=")
                      .append(idString).append("&attachment=")
                      .append(attachment.getName()).append("\">")
                      .append(attachment.getName()).append("</a>");
            }
            writer.append("<br/><br/>\r\n");
        }

        writer.append("<a href=\"support\">Return to list tickets</a>\r\n");// link to return to ticket list

        this.writeFooter(writer);
    }

    private void downloadAttachment(HttpServletRequest request,// method to download attachments to ticket
                                    HttpServletResponse response)
            throws ServletException, IOException
    {
        String idString = request.getParameter("ticketId");// links attachment to ticket sequence
        SupportTicket ticket = this.getTicket(idString, response);
        if(ticket == null)
            return;

        String name = request.getParameter("attachment");// links method to action on page
        if(name == null)
        {
            response.sendRedirect("support?action=view&ticketId=" + idString);
            return;
        }

        Attachment attachment = ticket.getAttachment(name);// gets attachment
        if(attachment == null)
        {
            response.sendRedirect("support?action=view&ticketId=" + idString);
            return;
        }

        response.setHeader("Content-Disposition",
                "attachment; filename=" + attachment.getName());// shows file name
        response.setContentType("application/octet-stream");

        ServletOutputStream stream = response.getOutputStream();// posts attachment
        stream.write(attachment.getContents());
    }

    private void listTickets(HttpServletResponse response)// method for listing all tickets
            throws ServletException, IOException
    {
        PrintWriter writer = this.writeHeader(response);

        writer.append("<h2>Tickets</h2>\r\n");// header for page
        writer.append("<a href=\"support?action=create\">Create Ticket")
              .append("</a><br/><br/>\r\n");

        if(this.ticketDatabase.size() == 0)// output if there are no tickets
            writer.append("<i>There are no tickets in the system.</i>\r\n");
        else
        {
            for(int id : this.ticketDatabase.keySet())// output of tickets that exist
            {
                String idString = Integer.toString(id);
                SupportTicket ticket = this.ticketDatabase.get(id);
                writer.append("Ticket #").append(idString)
                      .append(": <a href=\"support?action=view&ticketId=")
                      .append(idString).append("\">").append(ticket.getSubject())
                      .append("</a> (customer: ").append(ticket.getCustomerName())
                      .append(")<br/>\r\n");
            }
        }

        this.writeFooter(writer);
    }

    private void createTicket(HttpServletRequest request,// method for creating a ticket
                              HttpServletResponse response)
            throws ServletException, IOException
    {
        SupportTicket ticket = new SupportTicket();
        ticket.setCustomerName(request.getParameter("customerName"));// gets customer name parameter
        ticket.setSubject(request.getParameter("subject"));//gets subject parameter
        ticket.setBody(request.getParameter("body"));// gets body parameter

        Part filePart = request.getPart("file1");
        if(filePart != null && filePart.getSize() > 0)
        {// gets file if applicable
            Attachment attachment = this.processAttachment(filePart);
            if(attachment != null)
                ticket.addAttachment(attachment);
        }

        int id;
        synchronized(this)
        {
            id = this.TICKET_ID_SEQUENCE++;// assigns the sequence number
            this.ticketDatabase.put(id, ticket);
        }

        response.sendRedirect("support?action=view&ticketId=" + id);
    }

    private Attachment processAttachment(Part filePart)// method for getting the attachment
            throws IOException
    {
        InputStream inputStream = filePart.getInputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        int read;
        final byte[] bytes = new byte[1024];

        while((read = inputStream.read(bytes)) != -1)
        {
            outputStream.write(bytes, 0, read);
        }

        Attachment attachment = new Attachment();
        attachment.setName(filePart.getSubmittedFileName());
        attachment.setContents(outputStream.toByteArray());

        return attachment;
    }

    private SupportTicket getTicket(String idString, HttpServletResponse response)// method to get individual tickets in the hash map
            throws ServletException, IOException
    {
        if(idString == null || idString.length() == 0)
        {
            response.sendRedirect("support");
            return null;
        }

        try
        {
            SupportTicket ticket = this.ticketDatabase.get(Integer.parseInt(idString));
            if(ticket == null)
            {
                response.sendRedirect("support");
                return null;
            }
            return ticket;
        }
        catch(Exception e)
        {
            response.sendRedirect("support");
            return null;
        }
    }

    private PrintWriter writeHeader(HttpServletResponse response)// page header
            throws ServletException, IOException
    {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        PrintWriter writer = response.getWriter();
        writer.append("<!DOCTYPE html>\r\n")
              .append("<html>\r\n")
              .append("    <head>\r\n")
              .append("        <title>Customer Support</title>\r\n")
              .append("    </head>\r\n")
              .append("    <body>\r\n");

        return writer;
    }

    private void writeFooter(PrintWriter writer)// page footer
    {
        writer.append("    </body>\r\n").append("</html>\r\n");
    }
}
