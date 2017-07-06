/**
 * Created by joseluis on 6/09/17.
 */
import java.io.Serializable;
import com.datastax.driver.core.LocalDate;
import org.apache.spark.sql.Dataset;

public class Interaccion implements Serializable{
    private String idinteraccion;
    private String idorigen;
    private String tipoorigen;//Alumno, Profesor, Material
    private String iddestino;
    private String tipodestino;//Alumno, Profesor, Material
    private String tipointeraccion;
    private String valorinteraccion;
    private String idprecedente;
    private String idcurso;
    private String fecha;
    private String plataforma;//Facebook, PAM, CREA

    public Interaccion(){

    }

    public Interaccion(String idinteraccion, String idorigen, String tipoorigen, String iddestino, String tipodestino, String tipointeraccion, String valorinteraccion, String idprecedente, String idcurso, String fecha, String plataforma) {
        this.idinteraccion = idinteraccion;
        this.idorigen = idorigen;
        this.tipoorigen = tipoorigen;
        this.iddestino = iddestino;
        this.tipodestino = tipodestino;
        this.tipointeraccion = tipointeraccion;
        this.valorinteraccion = valorinteraccion;
        this.idprecedente = idprecedente;
        this.idcurso = idcurso;
        this.fecha = fecha;
        this.plataforma = plataforma;
    }

    public String getidinteraccion() {
        return idinteraccion;
    }

    public void setidinteraccion(String idinteraccion) {
        this.idinteraccion = idinteraccion;
    }

    public String getidorigen() {
        return idorigen;
    }

    public void setidorigen(String idorigen) {
        this.idorigen = idorigen;
    }

    public String gettipoorigen() {
        return tipoorigen;
    }

    public void settipoorigen(String tipoorigen) {
        this.tipoorigen = tipoorigen;
    }

    public String getiddestino() {
        return iddestino;
    }

    public void setiddestino(String iddestino) {
        this.iddestino = iddestino;
    }

    public String gettipodestino() {
        return tipodestino;
    }

    public void settipodestino(String tipodestino) {
        this.tipodestino = tipodestino;
    }

    public String gettipointeraccion() {
        return tipointeraccion;
    }

    public void settipointeraccion(String tipointeraccion) {
        this.tipointeraccion = tipointeraccion;
    }

    public String getvalorinteraccion() {
        return valorinteraccion;
    }

    public void setvalorinteraccion(String valorinteraccion) {
        this.valorinteraccion = valorinteraccion;
    }

    public String getidprecedente() {
        return idprecedente;
    }

    public void setidprecedente(String idprecedente) {
        this.idprecedente = idprecedente;
    }

    public String getidcurso() {
        return idcurso;
    }

    public void setidcurso(String idcurso) {
        this.idcurso = idcurso;
    }

    public String getfecha() {
        return fecha;
    }

    public void setfecha(String fecha) {
        this.fecha = fecha;
    }

    public String getplataforma() {
        return plataforma;
    }

    public void setplataforma(String plataforma) {
        this.plataforma = plataforma;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Interaccion that = (Interaccion) o;

        return idinteraccion != null ? idinteraccion.equals(that.idinteraccion) : that.idinteraccion == null;
    }

    @Override
    public int hashCode() {
        return idinteraccion != null ? idinteraccion.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Interaccion{" +
                "idinteraccion='" + idinteraccion + '\'' +
                '}';
    }
}