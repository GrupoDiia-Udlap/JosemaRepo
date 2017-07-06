package tables;

import com.github.javafaker.Faker;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Random;

/**
 * Created by Thagus on 18/06/17.
 */
public class Material {
    private long id_material;
    private Curso curso;
    private String tipo;            //Tipo del material {consulta, evaluacion}
    private Date fecha_creacion;
    private String contenido;       //Tipo del contenido, tema
    private String url_ubicacion;

    private static String[] tipos_material = {"consulta", "evaluacion"};

    public Material(long id_material, Curso curso) {
        Faker faker = new Faker(new Locale("es"));
        Random random = new Random();

        this.id_material = id_material;
        this.curso = curso;

        this.tipo = tipos_material[random.nextInt(tipos_material.length)];

        //Obtener el periodo del curso para generar el material en ese rango
        this.fecha_creacion = new Date(curso.getRandomDateInRange().getTime());

        this.contenido = faker.lorem().characters(5, 20);

        this.url_ubicacion = faker.internet().url();
    }

    public long getId_material() {
        return id_material;
    }

    public Curso getCurso() {
        return curso;
    }

    public String getTipo() {
        return tipo;
    }

    public Date getFecha_creacion() {
        return fecha_creacion;
    }

    public String getContenido() {
        return contenido;
    }

    public String getUrl_ubicacion() {
        return url_ubicacion;
    }
}
