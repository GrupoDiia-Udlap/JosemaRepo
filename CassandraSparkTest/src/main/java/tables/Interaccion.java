package tables;

import com.github.javafaker.Faker;

import java.sql.Timestamp;
import java.util.Locale;
import java.util.Random;

/**
 * Created by Thagus on 18/06/17.
 */
public class Interaccion {
    private long id_interaccion;
    private long id_origen;
    private String tipo_origen;
    private long id_destino;
    private String tipo_destino;
    private String tipo_interaccion;
    private String valor_interaccion;
    private long id_interaccion_precendete;
    private Curso curso;
    private Timestamp fecha;
    private String plataforma;

    public static final String[] tipos_interacciones = {"publicacion", "comentario", "mencion", "reaccion", "mensaje", "consulta"};

    private static final String[] plataformas = {"facebook", "crea"};
    private static final String[] reacciones = {"me_gusta", "me_encanta", "me_divierte", "me_asombra", "me_entristece", "me_enoja"};

    public Interaccion(long id_interaccion, long id_origen, String tipo_origen, long id_destino, String tipo_destino, String tipo_interaccion, long id_interaccion_precendete, Curso curso) {
        Faker faker = new Faker(new Locale("es"));
        Random random = new Random();

        this.id_interaccion = id_interaccion;
        this.id_origen = id_origen;
        this.tipo_origen = tipo_origen;
        this.id_destino = id_destino;
        this.tipo_destino = tipo_destino;
        this.tipo_interaccion = tipo_interaccion;
        this.id_interaccion_precendete = id_interaccion_precendete;
        this.curso = curso;

        //Obtener el periodo del curso para generar la interacción en ese rango
        this.fecha = new Timestamp(curso.getRandomDateInRange().getTime());

        if(tipo_interaccion.equals("mencion") || tipo_interaccion.equals("reaccion")){
            this.plataforma = "facebook";
        }
        else if(tipo_interaccion.equals("mensaje")){
            this.plataforma = "crea";
        }
        else {
            this.plataforma = plataformas[random.nextInt(plataformas.length)];
        }


        if(tipo_interaccion.equals("publicacion")){
            this.valor_interaccion = faker.lorem().sentence(15, 7);
        }
        else if(tipo_interaccion.equals("comentario")){
            this.valor_interaccion = faker.lorem().sentence(10, 7);
        }
        else if(tipo_interaccion.equals("mencion")){
            this.valor_interaccion = "@" + faker.lorem().word();
        }
        else if(tipo_interaccion.equals("reaccion")){
            this.valor_interaccion = reacciones[random.nextInt(reacciones.length)];
        }
        else if(tipo_interaccion.equals("mensaje")){
            this.valor_interaccion = faker.lorem().sentence(15, 7);
        }
        else if(tipo_interaccion.equals("consulta")){
            this.valor_interaccion = null;
        }
    }

    public long getId_interaccion() {
        return id_interaccion;
    }

    public long getId_origen() {
        return id_origen;
    }

    public String getTipo_origen() {
        return tipo_origen;
    }

    public long getId_destino() {
        return id_destino;
    }

    public String getTipo_destino() {
        return tipo_destino;
    }

    public String getTipo_interaccion() {
        return tipo_interaccion;
    }

    public String getValor_interaccion() {
        return valor_interaccion;
    }

    public long getInteraccion_precendete() {
        return id_interaccion_precendete;
    }

    public Curso getCurso() {
        return curso;
    }

    public Timestamp getFecha() {
        return fecha;
    }

    public String getPlataforma() {
        return plataforma;
    }
}
