package com.sails.hkiademo;

import com.sails.engine.SAILS;
import com.sails.engine.SAILSMapView;

/**
 * Created by Richard on 2014/11/17.
 */
public class IconPlacer {
    String [][] mapper={{"raincycle","ecs_water"},{"flower","ecs_garden"},{"solar","ecs_parking"}
    ,{"door_front","ecs_door"},{"door_second","ecs_door"},{"info","ecs_info"},{"tatung","ecs_notes"}
    ,{"bulletin","ecs_enotes"},{"coffee","ecs_cafe"},{"atm_1f","ecs_atm"},{"bulletin","ecs_enotes"}
    ,{"familymart","ecs_myfamily"},{"elevator_1f","ecs_elevator"},{"toilet_1f_m","ecs_male"},{"toilet_1f_w","ecs_female"}};

    public static void place(SAILS sails,SAILSMapView mapView) {
//        mapView.getMarkerManager().setLocationRegionMarker();
    }
}
