import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import tables.*;
import tables.Interaccion;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created by Thagus on 18/06/17.
 */
public class Main {
    private final static int NUM_ESCUELAS = 1;
    private final static int MIN_DOCENTES_ESCUELA = 5;
    private final static int VAR_DOCENTES_ESCUELA = 5;
    private final static int MIN_CURSOS_POR_DOCENTE = 5;
    private final static int VAR_CURSOS_POR_DOCENTE = 3;
    private final static int MIN_MATERIALES_POR_CURSO = 25;
    private final static int VAR_MATERIALES_POR_CURSO = 5;
    private final static int MIN_ALUMNOS_POR_CURSO = 25;
    private final static int VAR_ALUMNOS_POR_CURSO = 5;
    private final static int MIN_MATERIALES_DE_ALUMNO_POR_ALUMNO = 15;
    private final static int VAR_MATERIALES_DE_ALUMNO_POR_ALUMNO = 15;
    private final static int NUM_INTERACCIONES_POR_CURSO = 250;


    private static ArrayList<Escuela> escuelas = new ArrayList<Escuela>();
    private static ArrayList<Docente> docentes = new ArrayList<Docente>();
    private static ArrayList<Curso> cursos = new ArrayList<Curso>();
    private static ArrayList<Alumno> alumnos = new ArrayList<Alumno>();
    private static ArrayList<Material> materiales = new ArrayList<Material>();
    private static ArrayList<MaterialDeAlumno> materialesDeAlumno = new ArrayList<MaterialDeAlumno>();

    private static ArrayList<Interaccion> interacciones = new ArrayList<Interaccion>();

    private static ArrayList<AlumnoCurso> alumnoCursos = new ArrayList<AlumnoCurso>();
    private static ArrayList<EscuelaDocente> escuelasDocentes = new ArrayList<EscuelaDocente>();
    private static ArrayList<Evaluacion> evaluaciones = new ArrayList<Evaluacion>();


    public static void main(String[] args){
        generateBaseRelationalData();

        try {
            exportToExcel();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateBaseRelationalData(){
        Random random = new Random();

        long interactions_count=0, alumnos_count=0, docentes_count=0, curso_count=0, material_count=0, material_alumno_count=0;

        //Crear escuelas
        for(int i=0; i<NUM_ESCUELAS; i++){
            ArrayList<Docente> tDocentes = new ArrayList<Docente>();
            ArrayList<Alumno> tAlumnos = new ArrayList<Alumno>();

            Escuela escuela = new Escuela(i);
            escuelas.add(escuela);

            //Crear alumnos de forma aleatoria entre el máximo de alumnos y el minimo
            for(int j = 0; j<MIN_ALUMNOS_POR_CURSO * MIN_CURSOS_POR_DOCENTE * MIN_DOCENTES_ESCUELA; j++){
                tAlumnos.add(new Alumno(alumnos_count));
                alumnos_count++;
            }

            int docentes_escuela = random.nextInt(VAR_DOCENTES_ESCUELA)+MIN_DOCENTES_ESCUELA;
            for(int j = 0; j<docentes_escuela; j++){
                Docente docente = new Docente(docentes_count);
                docentes_count++;
                tDocentes.add(docente);
                escuelasDocentes.add(new EscuelaDocente(escuela, docente));

                ArrayList<Curso> tCursos = new ArrayList<Curso>();

                int cursos_docente = random.nextInt(VAR_CURSOS_POR_DOCENTE)+MIN_CURSOS_POR_DOCENTE;
                for(int k = 0; k<cursos_docente; k++){
                    ArrayList<Material> tMateriales = new ArrayList<Material>();
                    ArrayList<Evaluacion> tEvaluaciones = new ArrayList<Evaluacion>();
                    ArrayList<MaterialDeAlumno> tMaterialesDeAlumno = new ArrayList<MaterialDeAlumno>();
                    ArrayList<AlumnoCurso> tAlumnoCurso = new ArrayList<AlumnoCurso>();
                    ArrayList<Alumno> alumnosLocales = new ArrayList<Alumno>();

                    Curso curso = new Curso(curso_count, docente, escuela);
                    curso_count++;
                    tCursos.add(curso);

                    int materiales_curso = random.nextInt(VAR_MATERIALES_POR_CURSO)+MIN_MATERIALES_POR_CURSO;
                    for(int l = 0; l<materiales_curso; l++){
                        tMateriales.add(new Material(material_count, curso));
                        material_count++;
                    }

                    int alumnos_curso = random.nextInt(VAR_ALUMNOS_POR_CURSO)+MIN_ALUMNOS_POR_CURSO;
                    for(int l = 0; l<alumnos_curso; l++){
                        Alumno alumno = tAlumnos.get(random.nextInt(tAlumnos.size()));
                        tAlumnoCurso.add(new AlumnoCurso(alumno, curso));
                        alumnosLocales.add(alumno);

                        //If creates materials, choose how many, without exceeding limit
                        if(random.nextBoolean()){
                            int materiales_alumno = random.nextInt(VAR_MATERIALES_DE_ALUMNO_POR_ALUMNO)+MIN_MATERIALES_DE_ALUMNO_POR_ALUMNO;
                            for(int m = 0; m<materiales_alumno; m++){
                                tMaterialesDeAlumno.add(new MaterialDeAlumno(material_alumno_count, alumno, curso));
                                material_alumno_count++;
                            }
                        }

                        //Evaluate materials
                        for(Material material : tMateriales){
                            if(material.getTipo().equals("evaluacion")){
                                tEvaluaciones.add(new Evaluacion(alumno, material));
                            }
                        }
                    }

                    //Save array of interactions of posts, comments, and messajes to build chains
                    ArrayList<Interaccion> publicaciones = new ArrayList<Interaccion>();
                    ArrayList<Interaccion> comentarios = new ArrayList<Interaccion>();
                    ArrayList<Interaccion> mensajes = new ArrayList<Interaccion>();


                    //consultas solo pueden tener como destino a materiales y de origen alumnos


                    for(int l=0; l<NUM_INTERACCIONES_POR_CURSO; l++){
                        String tipo_interaccion = Interaccion.tipos_interacciones[random.nextInt(Interaccion.tipos_interacciones.length)];

                        long id_origen;
                        String tipo_origen;

                        //Elegir origen de la interaccion
                        if(random.nextDouble()>0.85){   //Docente con un 15% de probabilidad
                            id_origen = docente.getId_docente();
                            tipo_origen = "docente";
                        }
                        else {  //Alumno
                            id_origen = alumnosLocales.get(random.nextInt(alumnosLocales.size())).getId_alumno();
                            tipo_origen = "alumno";
                        }

                        if(tipo_interaccion.equals("publicacion")){
                            Interaccion publicacion = new Interaccion(interactions_count, id_origen, tipo_origen, -1, "null", tipo_interaccion, -1, curso);

                            interacciones.add(publicacion);
                            publicaciones.add(publicacion);
                        }
                        else if(tipo_interaccion.equals("comentario") || tipo_interaccion.equals("mencion") || tipo_interaccion.equals("reaccion")){
                            Interaccion comentario;

                            if(random.nextBoolean() && publicaciones.size()>0){   //Si le precede una publicacion
                                Interaccion publicacion_precedente = publicaciones.get(random.nextInt(publicaciones.size()));
                                comentario = new Interaccion(
                                        interactions_count,
                                        id_origen,
                                        tipo_origen,
                                        publicacion_precedente.getId_origen(),
                                        publicacion_precedente.getTipo_origen(),
                                        tipo_interaccion,
                                        publicacion_precedente.getId_interaccion(),
                                        curso
                                );

                                interacciones.add(comentario);
                                comentarios.add(comentario);
                            }
                            else if(comentarios.size()>0) {  //Si le precede un comentario
                                Interaccion comentario_precedente = comentarios.get(random.nextInt(comentarios.size()));
                                comentario = new Interaccion(
                                        interactions_count,
                                        id_origen,
                                        tipo_origen,
                                        comentario_precedente.getId_origen(),
                                        comentario_precedente.getTipo_origen(),
                                        tipo_interaccion,
                                        comentario_precedente.getId_interaccion(),
                                        curso
                                );

                                interacciones.add(comentario);
                                comentarios.add(comentario);
                            }
                            else {
                                Interaccion publicacion = new Interaccion(interactions_count, id_origen, tipo_origen, -1, "null", "publicacion", -1, curso);

                                interacciones.add(publicacion);
                                publicaciones.add(publicacion);
                            }
                        }
                        else if(tipo_interaccion.equals("mensaje")){
                            Interaccion mensaje;

                            long id_destino;
                            String tipo_destino;

                            //Elegir destino del mensaje
                            if(random.nextDouble()>0.85){   //Docente con un 15% de probabilidad
                                id_destino = docente.getId_docente();
                                tipo_destino = "docente";
                            }
                            else {  //Alumno
                                id_destino = alumnosLocales.get(random.nextInt(alumnosLocales.size())).getId_alumno();
                                tipo_destino = "alumno";
                            }

                            if(random.nextBoolean() && mensajes.size()>0){   //Si le precede un mensaje
                                mensaje = new Interaccion(
                                        interactions_count,
                                        id_origen,
                                        tipo_origen,
                                        id_destino,
                                        tipo_destino,
                                        tipo_interaccion,
                                        mensajes.get(random.nextInt(mensajes.size())).getId_interaccion(),
                                        curso
                                );
                            }
                            else {  //Es un mensaje nuevo
                                mensaje = new Interaccion(
                                        interactions_count,
                                        id_origen,
                                        tipo_origen,
                                        id_destino,
                                        tipo_destino,
                                        tipo_interaccion,
                                        -1,
                                        curso
                                );
                            }

                            interacciones.add(mensaje);
                            mensajes.add(mensaje);
                        }
                        else if(tipo_interaccion.equals("consulta")){
                            id_origen = alumnosLocales.get(random.nextInt(alumnosLocales.size())).getId_alumno();
                            tipo_origen = "alumno";

                            interacciones.add(new Interaccion(
                                    interactions_count,
                                    id_origen,
                                    tipo_origen,
                                    tMateriales.get(random.nextInt(tMateriales.size())).getId_material(),
                                    "material",
                                    tipo_interaccion,
                                    -1,
                                    curso
                            ));
                        }

                        interactions_count++;
                    }

                    materiales.addAll(tMateriales);
                    evaluaciones.addAll(tEvaluaciones);
                    cursos.addAll(tCursos);
                    materialesDeAlumno.addAll(tMaterialesDeAlumno);
                    alumnoCursos.addAll(tAlumnoCurso);

                    System.out.println("Finished course " + (curso_count-1));
                }
            }


            alumnos.addAll(tAlumnos);
            docentes.addAll(tDocentes);

            System.out.println("Finished school " + i);
        }
        System.out.println("======================================");
    }

    public static void exportToExcel() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();

        XSSFSheet alumnoSheet = workbook.createSheet("Alumno");
        XSSFSheet alumnoCursoSheet = workbook.createSheet("AlumnoCurso");
        XSSFSheet cursoSheet = workbook.createSheet("Curso");
        XSSFSheet docenteSheet = workbook.createSheet("Docente");
        XSSFSheet escuelaSheet = workbook.createSheet("Escuela");
        XSSFSheet escuelaDocenteSheet = workbook.createSheet("EscuelaDocente");
        XSSFSheet evaluacionSheet = workbook.createSheet("Evaluacion");
        XSSFSheet materialSheet = workbook.createSheet("Material");
        XSSFSheet materialAlumnoSheet = workbook.createSheet("MaterialAlumno");
        XSSFSheet interaccionesSheet = workbook.createSheet("Interacciones");

        System.out.println("Starting to write");

        /***************************************************************
         ************************ALUMNOS********************************
         ***************************************************************/
        Map<Integer, Object[]> alumnoData = new TreeMap<Integer, Object[]>();
        alumnoData.put(0, new Object[]{"id_alumno", "nombre", "apellido", "sexo", "fecha_nacimiento", "perfil_academico", "especialidad", "email", "telefono", "id_facebook", "id_crea"});

        for(int i=0; i<alumnos.size(); i++){
            Alumno alumno = alumnos.get(i);
            alumnoData.put(i+1, new Object[]{
                    alumno.getId_alumno(),
                    alumno.getNombre(),
                    alumno.getApellidos(),
                    alumno.getSexo(),
                    alumno.getFecha_nacimiento(),
                    alumno.getPerfil_academico(),
                    alumno.getEspecialidad(),
                    alumno.getEmail(),
                    alumno.getTelefono(),
                    alumno.getId_facebook(),
                    alumno.getId_crea()
            });
        }

        writeToSheet(alumnoSheet, alumnoData);

        System.out.println("Finished Alumnos");


        /***************************************************************
         ************************ALUMNO_CURSO***************************
         ***************************************************************/

        Map<Integer, Object[]> alumnoCursoData = new TreeMap<Integer, Object[]>();
        alumnoCursoData.put(0, new Object[]{"id_alumno", "id_curso"});

        for(int i=0; i< alumnoCursos.size(); i++){
            AlumnoCurso alumnoCurso = alumnoCursos.get(i);
            alumnoCursoData.put(i+1, new Object[]{
                    alumnoCurso.getAlumno().getId_alumno(),
                    alumnoCurso.getCurso().getId_curso()
            });
        }

        writeToSheet(alumnoCursoSheet, alumnoCursoData);

        System.out.println("Finished AlumnoCurso");


        /***************************************************************
         **************************CURSO********************************
         ***************************************************************/

        Map<Integer, Object[]> cursoData = new TreeMap<Integer, Object[]>();
        cursoData.put(0, new Object[]{"id_curso", "titulo", "codigo", "periodo", "seccion", "id_docente", "id_escuela", "id_facebook", "id_crea"});

        for(int i=0; i< cursos.size(); i++){
            Curso curso = cursos.get(i);
            cursoData.put(i+1, new Object[]{
                    curso.getId_curso(),
                    curso.getTitulo(),
                    curso.getCodigo(),
                    curso.getPeriodo(),
                    curso.getSeccion(),
                    curso.getDocente().getId_docente(),
                    curso.getEscuela().getId_escuela(),
                    curso.getId_facebook(),
                    curso.getId_crea()
            });
        }

        writeToSheet(cursoSheet, cursoData);

        System.out.println("Finished Curso");


        /***************************************************************
         ************************DOCENTE********************************
         ***************************************************************/

        Map<Integer, Object[]> docenteData = new TreeMap<Integer, Object[]>();
        docenteData.put(0, new Object[]{"id_docente", "nombre", "apellido", "sexo", "estado_civil", "fecha_nacimiento", "id_facebook", "id_crea"});

        for(int i=0; i< docentes.size(); i++){
            Docente docente = docentes.get(i);
            docenteData.put(i+1, new Object[]{
                    docente.getId_docente(),
                    docente.getNombre(),
                    docente.getApellidos(),
                    docente.getSexo(),
                    docente.getEstado_civil(),
                    docente.getFecha_nacimiento(),
                    docente.getId_facebook(),
                    docente.getId_crea()
            });
        }

        writeToSheet(docenteSheet, docenteData);

        System.out.println("Finished Docente");


        /***************************************************************
         ************************ESCUELA********************************
         ***************************************************************/

        Map<Integer, Object[]> escuelaData = new TreeMap<Integer, Object[]>();
        escuelaData.put(0, new Object[]{"id_escuela", "nombre", "direccion", "ciudad"});

        for(int i=0; i< escuelas.size(); i++){
            Escuela escuela = escuelas.get(i);
            escuelaData.put(i+1, new Object[]{
                    escuela.getId_escuela(),
                    escuela.getNombre(),
                    escuela.getDireccion(),
                    escuela.getCiudad()
            });
        }

        writeToSheet(escuelaSheet, escuelaData);

        System.out.println("Finished Escuela");


        /***************************************************************
         ************************ESCUELA_DOCENTE************************
         ***************************************************************/

        Map<Integer, Object[]> escuelaDocenteData = new TreeMap<Integer, Object[]>();
        escuelaDocenteData.put(0, new Object[]{"id_escuela", "id_docente"});

        for(int i=0; i< escuelasDocentes.size(); i++){
            EscuelaDocente escuelaDocente = escuelasDocentes.get(i);
            escuelaDocenteData.put(i+1, new Object[]{
                    escuelaDocente.getEscuela().getId_escuela(),
                    escuelaDocente.getDocente().getId_docente()
            });
        }

        writeToSheet(escuelaDocenteSheet, escuelaDocenteData);

        System.out.println("Finished EscuelaDocente");


        /***************************************************************
         ************************EVALUACION*****************************
         ***************************************************************/

        Map<Integer, Object[]> evaluacionData = new TreeMap<Integer, Object[]>();
        evaluacionData.put(0, new Object[]{"id_alumno", "id_curso"});

        for(int i=0; i< evaluaciones.size(); i++){
            Evaluacion evaluacion = evaluaciones.get(i);
            evaluacionData.put(i+1, new Object[]{
                    evaluacion.getAlumno().getId_alumno(),
                    evaluacion.getMaterial().getId_material(),
                    evaluacion.getCalificacion()
            });
        }

        writeToSheet(evaluacionSheet, evaluacionData);

        System.out.println("Finished Evaluacion");


        /***************************************************************
         ************************MATERIAL********************************
         ***************************************************************/

        Map<Integer, Object[]> materialData = new TreeMap<Integer, Object[]>();
        materialData.put(0, new Object[]{"id_material", "id_curso", "tipo", "fecha_creacion", "contenido", "url_ubicacion"});

        for(int i=0; i< materiales.size(); i++){
            Material material = materiales.get(i);
            materialData.put(i+1, new Object[]{
                    material.getId_material(),
                    material.getCurso().getId_curso(),
                    material.getTipo(),
                    material.getFecha_creacion(),
                    material.getContenido(),
                    material.getUrl_ubicacion()
            });
        }

        writeToSheet(materialSheet, materialData);

        System.out.println("Finished Material");


        /***************************************************************
         ************************MATERIAL_ALUMNO********************************
         ***************************************************************/

        Map<Integer, Object[]> materialAlumnoData = new TreeMap<Integer, Object[]>();
        materialAlumnoData.put(0, new Object[]{"id_material_alumno", "id_alumno", "id_curso", "fecha_creacion", "contenido", "url_ubicacion"});

        for(int i=0; i< materialesDeAlumno.size(); i++){
            MaterialDeAlumno materialDeAlumno = materialesDeAlumno.get(i);
            materialAlumnoData.put(i+1, new Object[]{
                    materialDeAlumno.getId_material_alumno(),
                    materialDeAlumno.getAlumno().getId_alumno(),
                    materialDeAlumno.getCurso().getId_curso(),
                    materialDeAlumno.getFecha_creacion(),
                    materialDeAlumno.getContenido(),
                    materialDeAlumno.getUrl_ubicacion()
            });
        }

        writeToSheet(materialAlumnoSheet, materialAlumnoData);

        System.out.println("Finished MaterialAlumno");


        /***************************************************************
         ************************INTERACCIONES**************************
         ***************************************************************/

        Map<Integer, Object[]> interaccionesData = new TreeMap<Integer, Object[]>();
        interaccionesData.put(0, new Object[]{"id_interaccion", "id_origen", "tipo_origen", "id_destino", "tipo_destino", "tipo_interaccion", "valor_interaccion", "interaccion_precedente", "id_curso", "fecha", "plataforma"});

        for(int i=0; i< interacciones.size(); i++){
            Interaccion interaccion = interacciones.get(i);
            interaccionesData.put(i+1, new Object[]{
                    interaccion.getId_interaccion(),
                    interaccion.getId_origen(),
                    interaccion.getTipo_origen(),
                    interaccion.getId_destino(),
                    interaccion.getTipo_destino(),
                    interaccion.getTipo_interaccion(),
                    interaccion.getValor_interaccion(),
                    interaccion.getInteraccion_precendete(),
                    interaccion.getCurso().getId_curso(),
                    interaccion.getFecha(),
                    interaccion.getPlataforma()
            });
        }

        writeToSheet(interaccionesSheet, interaccionesData);

        System.out.println("Finished Interacciones");


        /*****************************
         ******* WRITE  WORKBOOK *****
         *****************************/
        String filename = "data.xlsx";
        File file = new File(filename);
        file.createNewFile();
        FileOutputStream out = new FileOutputStream(file, false);
        workbook.write(out);
        out.close();

        System.out.println("Finished ");
    }

    private static void writeToSheet(XSSFSheet sheet, Map<Integer, Object[]> data){
        //Iterate over data and write to sheet
        Set<Integer> keyid = data.keySet();
        int rowid = 0;
        for (Integer key : keyid) {
            Row row = sheet.createRow(rowid++);
            Object [] objectArr = data.get(key);
            int cellid = 0;
            for (Object obj : objectArr) {
                Cell cell = row.createCell(cellid++);
                if(obj!=null)
                    cell.setCellValue(obj.toString());
                else
                    cell.setCellValue("null");
            }
        }
    }
}