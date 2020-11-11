

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@WebServlet(// sets the URL of the page and the load sequence
        name = "flightTicketServlet",
        urlPatterns = {"/flights"},
        loadOnStartup = 1
)
@MultipartConfig(// sets the max file size for each form submit
        fileSizeThreshold = 5_242_880, //5MB
        maxFileSize = 20_971_520L, //20MB
        maxRequestSize = 41_943_040L //40MB
)

public class FlightTicketServlet extends HttpServlet {

    private volatile int TICKET_ID_SEQUENCE = 1;// counts the number of forms submitted

    private Map<Integer, FlightTicket> flightDatabase = new LinkedHashMap<>();// stores the support tickets

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
                this.showFlightForm(response);
                break;
            case "view":// shows the ticket contents after submission
                this.viewFlights(request, response);
                break;
            case "list":// shows a list of all tickets submitted
            default:
                this.listFlights(response);
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
                response.sendRedirect("flights");
                break;
        }
    }
    
    private void showFlightForm(HttpServletResponse response)
            throws ServletException, IOException
    {// method that presents the form on the page to select flights
        PrintWriter writer = this.writeHeader(response);

        writer.append("<h2>Select Flight</h2>\r\n");// title
        writer.append("<form method=\"POST\" action=\"flights\" ")
              .append("enctype=\"multipart/form-data\">\r\n");//post location
        writer.append("<input type=\"hidden\" name=\"action\" ")
              .append("value=\"create\"/>\r\n");
        writer.append("Departing City<br/>\r\n");
        //series of checkboxes
        writer.append("<input type=\"checkbox\" name=\"departCity\" value=\"Denver\"/>")
        	  .append(" Denver</br>\r\n");
        writer.append("<input type=\"checkbox\" name=\"departCity\" value=\"San Diego\"/>")
  	  		  .append(" San Diego</br>\r\n");
        writer.append("<input type=\"checkbox\" name=\"departCity\" value=\"Seattle\"/>")
        	  .append(" Seattle</br></br>\r\n");
        writer.append("Arriving City<br/>\r\n");
        //series of checkboxes
        writer.append("<input type=\"checkbox\" name=\"arriveCity\" value=\"Oklahoma City\"/>")
  	  		  .append(" Oklahoma City</br>\r\n");
        writer.append("<input type=\"checkbox\" name=\"arriveCity\" value=\"New York City\"/>")
  	 	      .append(" New York City</br>\r\n");
        writer.append("<input type=\"checkbox\" name=\"arriveCity\" value=\"Pittsburgh\"/>")
  		  	  .append(" Pittsburgh</br></br>\r\n");
        writer.append("Travel Date<br/>\r\n");//travel date
        writer.append("<input type=\"text\" name=\"travelDate\"/></br></br>\r\n");
        writer.append("Number of Travelers</br>\r\n");//number of travelers
        writer.append("<input type=\"text\" name=\"numTravelers\"/></br></br>\r\n");
        writer.append("<input type=\"submit\" value=\"Submit\"/>\r\n");
        writer.append("</form>\r\n");

        this.writeFooter(writer);
    }
    
    private void viewFlights(HttpServletRequest request, HttpServletResponse response)
    		throws ServletException, IOException
    {// method to show ticket contents after submission
    	String idString = request.getParameter("ticketId");
    	FlightTicket ticket = this.getTicket(idString, response);// gets the sequence number of the ticket
    	if(ticket == null)
    		return;

    	PrintWriter writer = this.writeHeader(response);

    	writer.append("<h2>Flight #").append(idString).append(": ")
    		.append(ticket.getTravelDate()).append("</h2>\r\n");// shows ticket number and travel date
    	writer.append("<i>Departing From - ").append(ticket.getDepartCity())
    		.append("</i><br/><br/>\r\n");//shows the departing city
    	writer.append("<i>Arriving At - ").append(ticket.getArriveCity())
			.append("</i><br/><br/>\r\n");//shows the arriving city
    	writer.append("<i>Date - ").append(ticket.getTravelDate())
			.append("</i><br/><br/>\r\n");// shows the travel date
    	writer.append("<i>Number Traveling - ").append(ticket.getNumTravelers())
			.append("</i><br/><br/>\r\n");// shows number of people traveling
    	writer.append("<a href=\"flights\">Return to list tickets</a>\r\n");// link to return the tickets page

    	this.writeFooter(writer);
    }
    
    private void listFlights(HttpServletResponse response)
            throws ServletException, IOException
    {// method for listing all tickets
        PrintWriter writer = this.writeHeader(response);

        writer.append("<h2>Flights</h2>\r\n");// header for page
        writer.append("<a href=\"flights?action=create\">Find Flight")
              .append("</a><br/><br/>\r\n");

        if(this.flightDatabase.size() == 0)//output if there are no tickets
            writer.append("<i>There are no purchased flights in the system.</i>\r\n");
        else
        {
            for(int id : this.flightDatabase.keySet())//output if tickets exist
            {
                String idString = Integer.toString(id);
                FlightTicket ticket = this.flightDatabase.get(id);
                writer.append("Flight #").append(idString)
                      .append(": <a href=\"flights?action=view&ticketId=")
                      .append(idString).append("\">").append(ticket.getTravelDate())
                      .append("</a> (departing: ").append(ticket.getDepartCity())
                      .append(")<br/>\r\n");
            }
        }

        this.writeFooter(writer);
    }
    
    private void createTicket(HttpServletRequest request,
            HttpServletResponse response)
            		throws ServletException, IOException
    {// method for creating a ticket
    	FlightTicket ticket = new FlightTicket();
    	ticket.setDepartCity(request.getParameter("departCity"));//gets departing city parameter
    	ticket.setArriveCity(request.getParameter("arriveCity"));//gets arriving city parameter
    	ticket.setTravelDate(request.getParameter("travelDate"));//gets travel date parameter
    	ticket.setNumTravelers(request.getParameter("numTravelers"));//gets number of travelers parameter

    	int id;
    	synchronized(this)
    	{
    		id = this.TICKET_ID_SEQUENCE++;
    		this.flightDatabase.put(id, ticket);//assigns the sequence number
    	}

    	response.sendRedirect("flights?action=view&ticketId=" + id);
    }
    
    private FlightTicket getTicket(String idString, HttpServletResponse response)
            throws ServletException, IOException
    {// method to get individual tickets in the hash map
        if(idString == null || idString.length() == 0)
        {
            response.sendRedirect("flights");
            return null;
        }

        try
        {
            FlightTicket ticket = this.flightDatabase.get(Integer.parseInt(idString));
            if(ticket == null)
            {
                response.sendRedirect("flights");
                return null;
            }
            return ticket;
        }
        catch(Exception e)
        {
            response.sendRedirect("flights");
            return null;
        }
    }
    
    private PrintWriter writeHeader(HttpServletResponse response)
            throws ServletException, IOException
    {// page header
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        PrintWriter writer = response.getWriter();
        writer.append("<!DOCTYPE html>\r\n")
              .append("<html>\r\n")
              .append("    <head>\r\n")
              .append("        <title>Flights</title>\r\n")
              .append("    </head>\r\n")
              .append("    <body>\r\n");

        return writer;
    }

    private void writeFooter(PrintWriter writer)
    {//page footer
        writer.append("    </body>\r\n").append("</html>\r\n");
    }
}
