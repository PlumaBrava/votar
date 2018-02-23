package com.ncodata.votar.utils;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by perez.juan.jose on 26/01/2018.
 */
@IgnoreExtraProperties
public class Proyecto {

    public Integer cantidaddeinstalacionesrealizadas;
    public  Integer cantidaddeinstalacionesmax;
//    public List<String> macs;
    public Map<String, Boolean> macs = new HashMap<>();
    public Proyecto() {
    }



    public Integer getCantidaddeinstalacionesrealizadas() {
        return cantidaddeinstalacionesrealizadas;
    }

    public void setCantidaddeinstalacionesrealizadas(Integer cantidaddeinstalacionesrealizadas) {
        this.cantidaddeinstalacionesrealizadas = cantidaddeinstalacionesrealizadas;
    }

    public Integer getCantidaddeinstalacionesmax() {
        return cantidaddeinstalacionesmax;
    }

    public void setCantidaddeinstalacionesmax(Integer cantidaddeinstalacionesmax) {
        this.cantidaddeinstalacionesmax = cantidaddeinstalacionesmax;
    }

    public Map<String, Boolean> getMacs() {
        return macs;
    }

    public void setMacs(Map<String, Boolean> macs) {
        this.macs = macs;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("cantidaddeinstalacionesmax", cantidaddeinstalacionesmax);
        result.put("cantidaddeinstalacionesrealizadas", cantidaddeinstalacionesrealizadas);
        result.put("macs", macs);
        return result;
    }

}




