package org.arep.taller2.webclient;

import org.arep.taller2.api.HttpConnection;

import java.awt.image.BufferedImage;
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.json.*;

import javax.imageio.ImageIO;

/**
 * Clase responsable de crear un Socket entre el cliente y el servidor y entregar las peticiones que el cliente pueda necesitar
 * @author Brian Camilo Fajardo Sanchez
 * @author Daniel Benavides
 */
public class HttpServer {

    /**
     * Método principal que lanza el servidor, acepta y administra la conexión con el cliente y maneja las peticiones del
     * cliente
     * @param args Argumentos necesarios para realizar un método main
     * @throws IOException Excepción que se lanza si se encuentra un problema con la conexión
     */
    public static void main(String[] args) throws IOException, URISyntaxException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(35000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }

        boolean running = true;

        while(running){
            Socket clientSocket = null;
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String inputLine;

            boolean firstLine = true;
            String path = null;


            while ((inputLine = in.readLine()) != null) {
                if (firstLine){
                    path = inputLine.split(" ")[1];
                    firstLine = false;
                }
                System.out.println("Received: " + inputLine);
                if (!in.ready()) {
                    break;
                }
            }

            assert path != null;
            URI requestUri = new URI(path);

            String header = "HTTP/1.1 200 OK \r\n";
            String fileType = getFileType(requestUri);

            try{

                if (path.startsWith("/movie")){
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                    String outputLine = "HTTP/1.1 200 OK \r\n";
                    outputLine += getMovie(path);

                    out.println(outputLine);

                    out.close();

                } else if (fileType.equals("html") || fileType.equals("js") || fileType.equals("css") || fileType.isEmpty()){
                    sendResponseText(clientSocket, requestUri, fileType, header);
                }else if (fileType.equals("png") || fileType.equals("jpg") || fileType.equals("ico")){
                    header = "HTTP/1.1 200 OK \r\n" +
                            "Content-Type: image/" + fileType + " \r\n" +
                            "\r\n";
                    sendResponseImg(clientSocket, requestUri, fileType, header);
                }

            }catch (Exception e){
                header = "HTTP/1.1 400 Not Found\r\n";
                sendResponseText(clientSocket, new URI("/notFound.html"), "html", header);
            }

            in.close();
            clientSocket.close();

        }

        serverSocket.close();
    }

    /**
     * Método encargado de construir la respuesta de la búsqueda de la información de la película
     * @param path Nombre de la película visto como el path al hacer la petición tipo API REST
     * @return Respuesta con la información de la película
     * @throws IOException Excepción que se lanza si se encuentra un problema con la conexión
     */
    private static String getMovie(String path) throws IOException {
        return "Content-Type: text/json \r\n"
                + "\r\n"
                + movieInformation(path);
    }

    /**
     * Método que construye el elemento que se va a pintar en el cliente con la información de la película
     * @param path Título de la película
     * @return Elementos html que contienen la información de la película
     * @throws IOException Excepción que se lanza si se encuentra un problema con la conexión
     */
    private static String movieInformation(String path) throws IOException {
        String response = HttpConnection.getMovie(path.split("=")[1]);
        JSONObject object = new JSONObject(response);
        return "<div id=\"movie-container\">" +
                "<div id=\"info-movie\">"+
                "<h2 id=\"movie-title\">"+ object.get("Title") + "</h2>" +
                "<h3 id=\"\"> Year: "+ object.get("Year") + "</h3>" +
                "<p id=\"\"> Director: " + object.get("Director") + "</p>" +
                "<p id=\"\"> Genre: " + object.get("Genre") + "</p>" +
                "<p id=\"\"> Rating: " + object.get("Rated") + "</p>" +
                "<p id=\"\">" + object.get("Plot") + "</p>" +
                "</div>"+
                "<div id=\"img-movie\">"+
                "<img id=\"\" src=\"" + object.get("Poster") + "\"/>" +
                "</div>"+
                "</div>\n";
    }

    /**
     * Método que convierte una imágen en bytes  y la envía al cliente
     * @param clientSocket Es el socket de la sesión
     * @param filePath Es el path del archivo
     * @param fileType Es el tipo del archivo ej. png y jpg
     * @param header Es el encabezado de la petición http
     * @throws IOException se lanza cuando no encuentra el archivo o no puede establecer la conexión
     */
    public static void sendResponseImg(Socket clientSocket, URI filePath, String fileType, String header) throws IOException {
        System.out.println("--------------jpg ICO-------------------");
        OutputStream out = clientSocket.getOutputStream();
        System.out.println(filePath);
        BufferedImage img = ImageIO.read(new File("src/main/resources/public/" + filePath));

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(img, fileType, byteArrayOutputStream);

        byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
        out.write(header.getBytes());
        out.write(byteArrayOutputStream.toByteArray());

        out.close();
        clientSocket.close();
    }

    /**
     * Método que lee archivos y los envía al cliente
     * @param clientSocket Es el socket de la sesión
     * @param filePath Es el path del archivo
     * @param fileType Es el tipo del archivo ej. html, js, css
     * @param header Es el encabezado de la petición http
     * @throws IOException se lanza cuando no encuentra el archivo o no puede establecer la conexión
     */
    public static void sendResponseText(Socket clientSocket, URI filePath, String fileType, String header) throws IOException {
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        String outputLine;

        outputLine = header + "Content-Type: text/" + fileType + " \r\n" +
                "\r\n";

        Charset charset = StandardCharsets.UTF_8;
        BufferedReader reader = Files.newBufferedReader(Paths.get("src/main/resources/public" + filePath), charset);

        String line = null;

        while ((line = reader.readLine()) != null) {
            System.out.println(line);
            outputLine += line;
        }

        out.println(outputLine);

        out.close();
        clientSocket.close();
    }

    /**
     * Retorna el tipo del archivo
     * @param path dirección original del archivo
     * @return extensión del archivo
     */
    private static String getFileType(URI path) {
        String fileFormat = "";
        try {
            fileFormat = path.getPath().split("\\.")[1];
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
        return fileFormat;
    }

}