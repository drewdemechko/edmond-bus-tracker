/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uco.edmond.bus.tracker.Services;

import edu.uco.edmond.bus.tracker.Dtos.Bus;
import edu.uco.edmond.bus.tracker.Dtos.BusRoute;
import edu.uco.edmond.bus.tracker.Dtos.BusRouteStop;
import edu.uco.edmond.bus.tracker.Dtos.BusStop;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author omidnowrouzi
 */
@Path("routeservice")
public class RouteService extends Service {
     private List<BusRoute> routes;
     private List<BusRouteStop> busRouteStops;
     private List<BusStop> busStops;
    
    public RouteService() throws SQLException
    {
        this.routes = new ArrayList<>();
        this.busRouteStops = new ArrayList<>();
        this.busStops = new BusStopService().busStops();
        getAllRoutes();
        getAllBusRouteStops();
    }
    
    public List<BusRoute> routes()
    {
        return routes;
    }
    
    private void getAllRoutes()
    {
        try{
            Statement stmt = getDatabase().createStatement();

            ResultSet rs = stmt.executeQuery("SELECT * FROM tblbusroute");

            while(rs.next()){
                BusRoute route = new BusRoute(rs.getInt("id"), rs.getString("name"));
                routes.add(route);
            }

            stmt.close();
            
            //Close out current SQL connection
            getDatabase().close();
        }catch(SQLException s){
            System.out.println(s.toString()); //SQL error
        }
    }
    
    private void getAllBusRouteStops()
    {
        try{
            Statement stmt = getDatabase().createStatement();

            ResultSet rs = stmt.executeQuery("SELECT * FROM tblbusroutestop ORDER BY stoponroute");

            while(rs.next()){
                BusRouteStop busStopRoute = new BusRouteStop(rs.getInt("id"), 
                        rs.getString("route"), rs.getString("stop"), rs.getInt("stoponroute"));
                busRouteStops.add(busStopRoute);
            }

            stmt.close();
            
            //Close out current SQL connection
            getDatabase().close();
        }catch(SQLException s){
            System.out.println(s.toString()); //SQL error
        }
    }
    
    public BusRoute find(int id)
    {
        for(BusRoute route : routes)
        {
            if(route.getId() == id)
            {
                return route; //route found
            }
        }
            
        return null;
    }
    
    public BusRoute find(String query)
    {
        for(BusRoute route : routes)
        {
            if(route.getName().equals(query))
            {
                return route; //route found
            }
        }
        
        //no route found
        return null;
    }   
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("routes")
    public String getRoutes()
    {
        if(routes.isEmpty())
            return getGson().toJson("No routes currently registered."); // no routes in system
        
        for(BusRouteStop busStopRoute : busRouteStops)
            for(BusRoute route : routes)
                if(busStopRoute.getRoute().equals(route.getName()))
                {
                    for(BusStop busStop : busStops)
                        if(busStop.getName().equals(busStopRoute.getStop()))
                            route.getBusStops().add((new BusStop(busStopRoute.getStop(), busStop.getLat(), busStop.getLng())));
                }
                
        return getGson().toJson(routes);
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("routes/active")
    public String getActiveRoutes() throws SQLException
    {
        if(routes.isEmpty())
            return getGson().toJson("No routes currently registered."); // no routes in system
        
        List<Bus> buses = new BusService().buses();
        List<BusRoute> tempRoute = new ArrayList<>();
        
        for(BusRouteStop busStopRoute : busRouteStops)
            for(BusRoute route : routes)
            {
                if(busStopRoute.getRoute().equals(route.getName()))
                {
                    for(BusStop busStop : busStops)
                        if(busStop.getName().equals(busStopRoute.getStop()))
                            route.getBusStops().add((new BusStop(busStopRoute.getStop(), busStop.getLat(), busStop.getLng())));
                }
            }
        
        for(BusRoute route : routes)
            for(Bus bus : buses)
                if(bus.getRoute().equals(route.getName()) && bus.getActive()){
                    route.setBusName(bus.getName());
                    route.setBusId(bus.getId());
                    tempRoute.add(route);
                    break;
                }
                
        return getGson().toJson(tempRoute);
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("routes/byid/{id}")
    public String getRoute(@PathParam("id") int id){
        return getGson().toJson(find(id));
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("routes/{name}")
    public String getRoute(@PathParam("name") String name){
        
        for(BusRouteStop busStopRoute : busRouteStops)
            for(BusRoute route : routes)
                if(busStopRoute.getRoute().equals(route.getName()))
                {
                    for(BusStop busStop : busStops)
                        if(busStop.getName().equals(busStopRoute.getStop()))
                            route.getBusStops().add((new BusStop(busStopRoute.getStop(), busStop.getLat(), busStop.getLng())));
                }
        
        for(BusRoute route : routes)
            if(route.getName().equals(name))
                    return getGson().toJson(route);
        
        return getGson().toJson("No Route exists with that name."); //no route found with that name
    }
   
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("routes/create/{name}")
    public String create(@PathParam("name") String name)
    {
        BusRoute route = find(name);
        
        if(route != null)
            return getGson().toJson(null); //send error message on client --route exists
        
        try{
            try (PreparedStatement stmt = getDatabase()
                    .prepareStatement("INSERT INTO tblbusroute (name) VALUES(?)")) {
                stmt.setString(1, name);
                
                System.out.println("STATEMENT 1: " + stmt);
                
                int count = stmt.executeUpdate();
            }

            try ( //get id of new route
                    PreparedStatement stmt2 = getDatabase().prepareStatement("SELECT id FROM tblbusroute WHERE name=?")) {
                stmt2.setString(1, name);
                
                System.out.println("STATEMENT 2: " + stmt2);
                
                ResultSet rs = stmt2.executeQuery();
                
                rs.first();
                
                int id = rs.getInt("id");
                route = new BusRoute(id,name);
                routes.add(route);
            }
            
            //Close out current SQL connection
            getDatabase().close();
            
        }catch(SQLException s){
            return getGson().toJson(s.toString()); //SQL failed
        }
        
        return getGson().toJson(route);
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("routes/delete/{name}")
    public String delete(@PathParam("name") String name)
    {
        BusRoute route = find(name);
        
        if(route == null)
            return getGson().toJson(null); //send error message on client --route does not exist
        
        try{
            PreparedStatement stmt1 = getDatabase().prepareStatement("DELETE FROM tblbusroutestop WHERE route=?");
            stmt1.setString(1, name);
            
            int count = stmt1.executeUpdate();
            
            try (PreparedStatement stmt2 = getDatabase().prepareStatement("DELETE FROM tblbusroute WHERE id=?")) {
                stmt2.setInt(1, route.getId());
                
                count = stmt2.executeUpdate();
                
                //Close out current SQL connection
                getDatabase().close();
            }
        }catch(SQLException s){
            return getGson().toJson(s.toString());
        }
        
        routes.remove(route);
        
        return getGson().toJson(route);
    }
}

