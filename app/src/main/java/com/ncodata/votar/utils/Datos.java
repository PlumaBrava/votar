package com.ncodata.votar.utils;

/**
 * Created by perez.juan.jose on 11/01/2018.
 */

public  class Datos {

    Boolean banderaVoto;
    Boolean banderaTexto;
    String titulo;
    String texto;
    int duracionVotacion;

    public Datos() {
        this.banderaVoto = false;
        this.banderaTexto = false;
        this.titulo = "";
        this.texto = "";
        this.duracionVotacion = 1000 * 10;
    }

    public Datos(Boolean banderaVoto, Boolean banderaTexto, String titulo, String texto, int duracionVotacion) {
        this.banderaVoto = banderaVoto;
        this.banderaTexto = banderaTexto;
        this.titulo = titulo;
        this.texto = texto;
        this.duracionVotacion = 1000 * duracionVotacion;
    }

    public void reset() {
        this.banderaVoto = false;
        this.banderaTexto = false;
        this.titulo = "";
        this.texto = "";
        this.duracionVotacion = 1000 * 10;
    }

    public String toString() {

        String s;
        s = "banderaVoto:" + this.banderaVoto
                + " BanderaTexto:" + this.banderaTexto
                + " titulo: " + this.titulo
                + " texto " + this.texto
                + " duracionVotacion: " + this.duracionVotacion;
        return s;
    }


    public Boolean getBanderaTexto() {
        return this.banderaTexto;
    }

    public void setBanderaTexto(Boolean banderaTexto) {
        this.banderaTexto = banderaTexto;
    }

    public Boolean getBanderaVoto() {
        return this.banderaVoto;
    }

    public void setBanderaVoto(Boolean banderaVoto) {
        this.banderaVoto = banderaVoto;
    }

    public String getTitulo() {
        return this.titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getTexto() {
        return this.texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public int getDuracionVotacion() {
        return this.duracionVotacion;
    }

    public void setDuracionVotacion(int duracionVotacion) {
        this.duracionVotacion = duracionVotacion;
    }


}