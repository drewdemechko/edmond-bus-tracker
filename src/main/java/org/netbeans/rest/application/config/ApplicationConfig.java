/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.rest.application.config;

import java.util.Set;
import javax.ws.rs.core.Application;

/**
 *
 * @author Drew Demechko
 */
@javax.ws.rs.ApplicationPath("webresources")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        addRestResourceClasses(resources);
        return resources;
    }

    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically populated with
     * all resources defined in the project.
     * If required, comment out calling this method in getClasses().
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(edu.uco.edmond.bus.tracker.Services.BusRouteStopService.class);
        resources.add(edu.uco.edmond.bus.tracker.Services.BusService.class);
        resources.add(edu.uco.edmond.bus.tracker.Services.BusStopService.class);
        resources.add(edu.uco.edmond.bus.tracker.Services.FavoriteService.class);
        resources.add(edu.uco.edmond.bus.tracker.Services.RouteService.class);
        resources.add(edu.uco.edmond.bus.tracker.Services.UserService.class);
    }
    
}
