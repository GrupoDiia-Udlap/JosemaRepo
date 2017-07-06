package tables;

import com.github.javafaker.Address;
import com.github.javafaker.Faker;

import java.util.Locale;

/**
 * Created by Thagus on 18/06/17.
 */
public class Escuela {
    private long id_escuela;
    private String nombre;
    private String direccion;
    private String ciudad;

    public Escuela(long id_escuela) {
        Faker faker = new Faker(new Locale("es"));
        this.id_escuela = id_escuela;
        this.nombre = faker.university().prefix() + " " + faker.university().name() + " " + faker.university().suffix();

        Address address = faker.address();
        this.direccion = address.streetAddress();
        this.ciudad = address.cityName();
    }

    public long getId_escuela() {
        return id_escuela;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getCiudad() {
        return ciudad;
    }
}
